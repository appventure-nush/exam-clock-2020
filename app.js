const CLOCKS = require("./clocks.js");

const path = require('path');
const logger = require('morgan');
const express = require('express');
const server = require('socket.io');
const passport = require('passport');
const session = require('express-session');
const createError = require('http-errors');
const exphbs = require('express-handlebars');
const cookieParser = require('cookie-parser');
const MemoryStore = require('memorystore')(session)
const passportSocketIo = require("passport.socketio");
const MicrosoftStrategy = require('passport-microsoft').Strategy;

const indexRouter = require('./routes/index');
const loginRouter = require('./routes/login');

const app = express();

// view engine setup
app.engine('.hbs', exphbs({extname: '.hbs'}));
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', '.hbs');

passport.serializeUser((user, done) => done(null, user));
passport.deserializeUser((obj, done) => done(null, obj));

app.use(express.json());
app.use(cookieParser());
// app.use(logger('dev'));
const store = new MemoryStore({
    checkPeriod: 86400000 // prune expired entries every 24h
});
app.use(session({
    secret: "exam clocks are cool",
    resave: false,
    saveUninitialized: false,
    cookie: {
        secure: "auto"
    },
    store: store
}));
app.use(express.urlencoded({extended: false}));
app.use(express.static(path.join(__dirname, 'public')));

// *************************
// ******* MICROSOFT *******
// *************************
passport.use(new MicrosoftStrategy({
    clientID: process.env.MS_CLIENT_ID,
    clientSecret: process.env.MS_CLIENT_SECRET,
    callbackURL: process.env.MS_CALLBACK_URL || "http://localhost:3000/auth/microsoft/callback",
    scope: ['user.read']
}, (accessToken, refreshToken, profile, done) => process.nextTick(() => done(null, profile))));
app.use(passport.initialize(null));
app.use(passport.session(null));
app.get('/auth/microsoft', passport.authenticate('microsoft', null, null));
app.get('/auth/microsoft/callback', passport.authenticate('microsoft', {failureRedirect: '/login'}, null), (req, res) => res.redirect('/'));
app.get('/logout', function (req, res) {
    req.logout();
    res.redirect('/');
});
// *************************
// ***** END MICROSOFT *****
// *************************

app.use('/', indexRouter);
app.use('/login', loginRouter);

// catch 404 and forward to error handler
app.use(function (req, res, next) {
    next(createError(404));
});

// error handler
app.use(function (err, req, res, next) {
    // set locals, only providing error in development
    res.locals.message = err.message;
    res.locals.error = req.app.get('env') === 'development' ? err : {};

    // render the error page
    res.status(err.status || 500);
    res.render('error');
});

module.exports = {
    app: app, callback: initSocket
};

const CONTROLLERS = {};
const CONTROLLER_ROOMS = {};

function initSocket(http) {
    const io = server(http);
    const ioc = server(http, {
        path: "/socket.clocks"
    });
    const Clock = require('./clock')(ioc);
    io.use(passportSocketIo.authorize({
        cookieParser: cookieParser,
        key: 'connect.sid',
        secret: 'exam clocks are cool',
        store: store,
    }));
    ioc.on('connection', socket => {
        try {
            let clock = JSON.parse(decodeURIComponent(socket.handshake.query.clock));
            if (CLOCKS.hasOwnProperty(clock.clockID)) {
                CLOCKS[clock.clockID].socketID = socket.id;
                console.log(`[CONNECTION] clock "${clock.clockID}" reconnected via "${socket.id}"`);
            } else { // A **NEW** FULLY VERIFIED CLOCK
                console.log(`[CONNECTION] clock "${clock.clockID}" connected via "${socket.id}"`);
                socket.join("clocks");
                let exams = clock.exams;
                clock = CLOCKS[clock.clockID] = new Clock(clock.clockID, socket.id, clock.clockName, exams);
                io.emit("new_clock", JSON.stringify({id: clock.clockID, name: clock.clockName}));
                socket.on('disconnect', () => {
                    console.log(`[CONNECTION] clock "${clock.clockID}" disconnected`);
                    io.emit("clock_died", clock.clockID);
                    delete CLOCKS[clock.clockID];
                });
                socket.on('request_callback', (controllerID, response) => {
                    console.log(`[REQUEST] clock "${clock.clockID}" ${response} request from ${controllerID}`);
                    if (response === "accepted") {
                        clock.request_callback(controllerID);
                        CONTROLLERS[controllerID].join('c_' + clock.clockID);
                        CONTROLLER_ROOMS[controllerID].push('c_' + clock.clockID);
                    }
                    io.to(CONTROLLERS[controllerID].id).emit('request_callback', response);
                });
                socket.on('toilet', occupied => {
                    console.log(`[CLOCK TOILET] ${clock.clockID} toilet status = ${occupied}`);
                    io.to('c_' + clock.clockID).emit("toilet", clock.clockID, occupied);
                });
                socket.on('new_exam', json => {
                    clock.newExam(JSON.parse(json));
                    io.to('c_' + clock.clockID).emit("new_exam", clock.clockID, json);
                });
                socket.on('delete_exam', examID => {
                    clock.deleteExam(examID);
                    io.to('c_' + clock.clockID).emit("delete_exam", clock.clockID, examID);
                });
                socket.on('exam_update', exams => {
                    clock.exams = JSON.parse(exams);
                    io.to('c_' + clock.clockID).emit("exam_update", clock.clockID, exams);
                });
            }
        } catch (err) {
            // ignore, idk, maybe i should handle it
        }
    });
    io.on('connection', socket => {
        socket.join("controllers");
        let controllerID = socket.request.user.id;
        console.log(`[CONNECTION] controller "${controllerID}" connected via "${socket.id}"`);
        CONTROLLERS[controllerID] = socket;
        if (!CONTROLLER_ROOMS[controllerID]) CONTROLLER_ROOMS[controllerID] = [];
        CONTROLLER_ROOMS[controllerID].forEach(room => socket.join(room));
        socket.on('new_exam', json => {
            let req = JSON.parse(json);
            if (!CLOCKS[req.clockID] || !CLOCKS[req.clockID].acceptsSocket(socket)) return;
            CLOCKS[req.clockID].new_exam(req, socket);
        });
        socket.on('edit_exam', json => {
            let req = JSON.parse(json);
            if (!CLOCKS[req.clockID] || !CLOCKS[req.clockID].acceptsSocket(socket)) return;
            CLOCKS[req.clockID].edit_exam(req, socket);
        });
        socket.on('delete_exam', (clockID, examID) => {
            if (!CLOCKS[clockID] || !CLOCKS[clockID].acceptsSocket(socket)) return;
            CLOCKS[clockID].delete_exam(examID, socket);
        });
        socket.on('toilet', clockID => {
            if (!CLOCKS[clockID] || !CLOCKS[clockID].acceptsSocket(socket)) return;
            CLOCKS[clockID].toilet(socket);
        });
        socket.on("request", (clockID, nick) => {
            if (!CLOCKS[clockID]) {
                socket.emit('request_callback', 'not_found');
                return;
            }
            nick = nick + (socket.request.user.emails[0] ? ` (${socket.request.user.emails[0].value})` : "");
            console.log("[REQUEST]", nick, clockID, "accepts =", CLOCKS[clockID].accepts(controllerID));
            CLOCKS[clockID].request(nick, socket);
        });
    });
}