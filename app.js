const CLOCKS = require("./clocks.js");

const path = require('path');
const logger = require('morgan');
const express = require('express');
const server = require('socket.io');
const createError = require('http-errors');
const exphbs = require('express-handlebars');
const cookieParser = require('cookie-parser');
const sharedsession = require("express-socket.io-session");
const indexRouter = require('./routes/index');

const app = express();

// view engine setup
app.engine('.hbs', exphbs({extname: '.hbs'}));
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', '.hbs');

const session = require('express-session')({
    resave: false,
    saveUninitialized: false,
    secret: 'exam clocks are cool',
    cookie: {maxAge: 86400000}
});
app.use(session);
app.use(express.json());
app.use(cookieParser());
app.use(logger('dev'));
app.use((req, res, next) => {
    req.session.sessionID = req.sessionID;
    next();
})
app.use(express.urlencoded({extended: false}));
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);

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

function initSocket(http) {
    const io = server(http);
    const Clock = require('./clock')(io);
    io.use(sharedsession(session, {
        autoSave: true
    }));
    io.on('connection', socket => {
        socket.on('clock_connected', json => {
            let clock = JSON.parse(json);
            if (CLOCKS.hasOwnProperty(clock.clockID)) {
                CLOCKS[clock.clockID].socketID = socket.id;
                console.log('clock "', clock.clockID, '" reconnected');
            } else { // A **NEW** FULLY VERIFIED CLOCK
                console.log('clock "', clock.clockID, '" connected');
                socket.join("clocks");
                clock = CLOCKS[clock.clockID] = new Clock(clock.clockID, socket.id, clock.clockName);
                io.in("controllers").emit("new_clock", JSON.stringify({id: clock.clockID, name: clock.clockName}));
                socket.on('disconnect', () => {
                    console.log('clock "' + clock.clockID + '" disconnected');
                    io.in("controllers").emit("clock_died", clock.clockID);
                });
                socket.on('request_callback', (controllerID, response) => {
                    console.log('clock "' + clock.clockID + '" ' + response + ' the request from ' + controllerID);
                    if (response === "accepted") clock.request_callback(controllerID);
                    io.to(CONTROLLERS[controllerID]).emit('request_callback', response);
                });
            }
        });
        socket.on('controller_connected', msg => {
            socket.join("controllers");
            console.log('a controller "', socket.id, '" connected with session \"' + socket.handshake.session.sessionID + '\"');
            CONTROLLERS[socket.handshake.session.sessionID] = socket.id;
            socket.on('add_exam', json => {
                let req = JSON.parse(json);
                if (!CLOCKS[req.clockID]) return;
                CLOCKS[req.clockID].add_exam(req, socket);
            });
            socket.on('delete_exam', json => {
                let req = JSON.parse(json);
                if (!CLOCKS[req.clockID]) return;
                CLOCKS[req.clockID].delete_exam(req.id, socket);
            });
            socket.on('toilet', json => {
                let req = JSON.parse(json);
                if (!CLOCKS[req.clockID]) return;
                CLOCKS[req.clockID].toilet(req.occupied, socket);
            });
            socket.on("request", (clockID, nick) => {
                if (!CLOCKS[clockID]) {
                    socket.emit('request_callback', 'not_found');
                    return;
                }
                console.log("[REQUEST]", nick, clockID, "accepts =", CLOCKS[clockID].accepts(socket.handshake.session.sessionID));
                CLOCKS[clockID].request(nick, socket);
            });
        });
    });
}
