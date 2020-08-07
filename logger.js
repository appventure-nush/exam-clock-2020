const winston = require('winston');
const expressWinston = require('express-winston');
require('winston-mongodb');
const logger = winston.createLogger({
    level: 'info',
    format: winston.format.combine(winston.format.errors({stack: true}), winston.format.json(), winston.format.metadata()),
    transports: [
        new winston.transports.MongoDB({
            name: "log",
            metaKey: "meta",
            db: process.env.MONGODB_URL,
            options: {
                poolSize: 2, useNewUrlParser: true,
                useUnifiedTopology: true
            },
            tryReconnect: true,
            collection: 'log',
            level: 'info'
        })
    ]
});
const expressLogger = expressWinston.logger({
    level: 'info',
    format: winston.format.combine(winston.format.errors({stack: true}), winston.format.json(), winston.format.metadata()),
    expressFormat: true,
    transports: [
        new winston.transports.MongoDB({
            name: "http",
            metaKey: "meta",
            db: process.env.MONGODB_URL,
            options: {
                poolSize: 2, useNewUrlParser: true,
                useUnifiedTopology: true
            },
            tryReconnect: true,
            collection: 'http',
            level: 'info'
        })
    ]
});
module.exports = {
    logger: logger, expressLogger: expressLogger
};