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
// app.use(logger('dev'));
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
const CONTROLLER_ROOMS = {};

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
                let exams = clock.exams;
                clock = CLOCKS[clock.clockID] = new Clock(clock.clockID, socket.id, clock.clockName, exams);
                io.in("controllers").emit("new_clock", JSON.stringify({id: clock.clockID, name: clock.clockName}));
                socket.on('disconnect', () => {
                    console.log('clock "' + clock.clockID + '" disconnected');
                    io.in("controllers").emit("clock_died", clock.clockID);
                    delete CLOCKS[clock.clockID];
                });
                socket.on('request_callback', (controllerID, response) => {
                    console.log('clock "' + clock.clockID + '" ' + response + ' the request from ' + controllerID);
                    if (response === "accepted") {
                        clock.request_callback(controllerID);
                        CONTROLLERS[controllerID].join('c_' + clock.clockID);
                        CONTROLLER_ROOMS[controllerID].push('c_' + clock.clockID);
                    }
                    io.to(CONTROLLERS[controllerID].id).emit('request_callback', response);
                });
                socket.on('toilet', occupied => {
                    console.log("[CLOCK TOILET] " + clock.clockID + " " + occupied);
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
        });
        socket.on('controller_connected', msg => {
            socket.join("controllers");
            let controllerID = socket.handshake.session.sessionID;
            console.log('a controller "', socket.id, '" connected with session \"' + controllerID + '\"');
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
                console.log("[REQUEST]", nick, clockID, "accepts =", CLOCKS[clockID].accepts(controllerID));
                CLOCKS[clockID].request(nick, socket);
            });
        });
    });
}
