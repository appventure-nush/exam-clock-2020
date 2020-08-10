const winston = require('winston');
const expressWinston = require('express-winston');
const logger = winston.createLogger({
    level: 'info',
    format: winston.format.combine(winston.format.errors({stack: true}), winston.format.json(), winston.format.metadata()),
    transports: [
        new winston.transports.File({filename: 'log.error.json', level: 'error'}),
        new winston.transports.File({filename: 'log.combined.json'})
    ]
});
const expressLogger = expressWinston.logger({
    level: 'info',
    format: winston.format.combine(winston.format.errors({stack: true}), winston.format.json(), winston.format.metadata()),
    expressFormat: true,
    transports: [
        new winston.transports.File({filename: 'express.error.json', level: 'error'}),
        new winston.transports.File({filename: 'express.combined.json'})
    ]
});
module.exports = {
    logger: logger, expressLogger: expressLogger
};