const CLOCKS = []; // ALL HAIL THE ALMIGHTY ARRAY

const path = require('path');
const logger = require('morgan');
const express = require('express');
const session = require('express-session');
const createError = require('http-errors');
const exphbs = require('express-handlebars');
const cookieParser = require('cookie-parser');

const clocksRouter = require('./routes/clocks');
const indexRouter = require('./routes/index')(CLOCKS);

const app = express();

// view engine setup
app.engine('.hbs', exphbs({extname: '.hbs'}));
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', '.hbs');

app.use(session({
    resave: false,
    saveUninitialized: false,
    secret: 'exam clocks are cool',
    cookie: {secure: true, maxAge: 86400000}
}))
app.use(express.json());
app.use(cookieParser());
app.use(logger('dev'));
app.use(express.urlencoded({extended: false}));
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);
app.use('/clocks', clocksRouter);

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
    app: app, callback: http => {
        const io = require('socket.io')(http);
        io.on('connection', (socket) => {
            console.log('a user connected');
        });
    }
};
