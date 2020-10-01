require('dotenv').config();
const CLOCKS = require("./clocks.js");

const path = require('path');
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

const {logger, expressLogger} = require('./logger.js');

const indexRouter = require('./routes/index');
const clocksRouter = require('./routes/clocks');

const app = express();

app.use(expressLogger);

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
app.get('/login', (req, res) => res.render('login', {user: req.user}));
app.get('/auth/microsoft', passport.authenticate('microsoft', null, null));
app.get('/auth/microsoft/callback', passport.authenticate('microsoft', {failureRedirect: '/login'}, null), (req, res) => res.redirect('/'));
app.get('/logout', (req, res) => {
    req.logout();
    res.redirect('/');
});
// *************************
// ***** END MICROSOFT *****
// *************************

app.use('/', indexRouter);
app.use('/clock', clocksRouter);

// catch 404 and forward to error handler
app.use((req, res, next) => {
    res.render('404', {layout: false});
});

// error handler
app.use((err, req, res) => {
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
                logger.info(`[CONNECTION] clock "${clock.clockID}" reconnected via "${socket.id}"`, {
                    type: "clock_reconnected",
                    clockID: clock.clockID
                });
            } else {
                // A **NEW** FULLY VERIFIED CLOCK
                logger.info(`[CONNECTION] clock "${clock.clockID}" connected via "${socket.id}"`, {
                    type: "clock_connected",
                    clockID: clock.clockID
                });
            }
            socket.join("clocks");
            let exams = clock.exams;
            clock = CLOCKS[clock.clockID] = new Clock(clock.clockID, socket.id, clock.clockName, exams);
            io.emit("new_clock", JSON.stringify({id: clock.clockID, name: clock.clockName}));
            socket.on('disconnect', () => {
                logger.info(`[CONNECTION] clock "${clock.clockID}" disconnected`, {
                    type: "clock_disconnected",
                    clockID: clock.clockID
                });
                io.emit("clock_died", clock.clockID);
                delete CLOCKS[clock.clockID];
            });
            socket.on('rename', name => {
                logger.info(`[CLOCK RENAME] ${clock.clockName} => ${name}`);
                clock.clockName = name;
                io.emit('clock_name_change', clock.clockID, name);
            });
            socket.on('request_callback', (controllerID, response) => {
                logger.info(`[REQUEST] clock "${clock.clockID}" ${response} request from ${controllerID}`, {
                    type: "clock_request_response",
                    clockID: clock.clockID,
                    controllerID: controllerID,
                    response: response
                });
                if (response === "accepted") {
                    clock.request_callback(controllerID);
                    CONTROLLERS[controllerID].join('c_' + clock.clockID);
                    CONTROLLER_ROOMS[controllerID].push('c_' + clock.clockID);
                }
                io.to(CONTROLLERS[controllerID].id).emit('request_callback', response, clock.clockID, clock.clockName);
            });
            socket.on('toilet', (occupied, gender) => {
                logger.info(`[CLOCK TOILET] ${clock.clockID} toilet (${gender}) status = ${occupied}`, {
                    type: "clock_toilet_update",
                    clockID: clock.clockID,
                    occupied: occupied,
                    gender: gender
                });
                io.to('c_' + clock.clockID).emit("toilet", clock.clockID, occupied, gender);
            });
            socket.on('new_exam', json => {
                let exam = JSON.parse(json);
                clock.newExam(exam);
                logger.info(`[NEW EXAM] ${clock.clockID} exam = ${json}`, {
                    type: "clock_add_exam",
                    clockID: clock.clockID,
                    exam: exam
                });
                io.to('c_' + clock.clockID).emit("new_exam", clock.clockID, json);
            });
            socket.on('delete_exam', examID => {
                clock.deleteExam(examID);
                logger.info(`[DELETE EXAM] ${clock.clockID} examID = ${examID}`, {
                    type: "clock_add_exam",
                    clockID: clock.clockID,
                    examID: examID
                });
                io.to('c_' + clock.clockID).emit("delete_exam", clock.clockID, examID);
            });
            socket.on('exam_update', exams => {
                exams = JSON.parse(exams);
                logger.info(`[FORCED UPDATE EXAM] ${clock.clockID} totalExams = ${clock.exams.length}`, {
                    type: "clock_add_exam",
                    clockID: clock.clockID,
                    oldExams: Object.assign({}, clock.exams), // it will die lol, so clone it
                    newExams: exams
                });
                clock.exams = exams;
                io.to('c_' + clock.clockID).emit("exam_update", clock.clockID, JSON.stringify(exams));
            });
        } catch (err) {
            logger.error(`[SERVER ERROR] ${err.message}`, {
                type: "clock_server_error",
                error: err
            });
        }
    });
    io.on('connection', socket => {
        socket.join("controllers");
        let controllerID = socket.request.user.id;
        logger.info(`[CONNECTION] controller "${controllerID}" connected via "${socket.id}"`, {
            type: "controller_connected",
            controllerID: controllerID
        });
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
        socket.on('toilet', (clockID, gender) => {
            if (!CLOCKS[clockID] || !CLOCKS[clockID].acceptsSocket(socket)) return;
            CLOCKS[clockID].toilet(socket, gender);
        });
        socket.on("request", (clockID, nick) => {
            if (!CLOCKS[clockID]) {
                socket.emit('request_callback', 'not_found');
                return;
            }
            nick = nick + (socket.request.user.emails[0] ? ` (${socket.request.user.emails[0].value})` : "");
            logger.info(`[REQUEST] ${nick}=>${clockID} current accepts = ${CLOCKS[clockID].accepts(controllerID)}`, {
                type: "request",
                nick: nick,
                email: socket.request.user.emails[0].value,
                controllerID: controllerID,
                clockID: clockID
            });
            CLOCKS[clockID].request(nick, socket);
        });
    });
}