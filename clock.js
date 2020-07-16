let io;

class Clock {
    clockID;
    socketID;
    clockName; // purely for display

    constructor(clockID, socketID, clockName) {
        this.clockID = clockID;
        this.socketID = socketID;
        this.clockName = clockName;
        this.controllers = [];
    }

    accepts(controllerID) {
        return this.controllers.includes(controllerID);
    }

    acceptsSocket(socket) {
        return this.accepts(socket.handshake.session.sessionID);
    }

    request(nick, socket) {
        if (this.accepts(socket.handshake.session.sessionID)) socket.emit('request_callback', "accepted")
        else io.to(this.socketID).emit("request", socket.handshake.session.sessionID, nick);
    }

    request_callback(controllerID) {
        if (!this.accepts(controllerID)) this.controllers.push(controllerID);
    }

    new_exam(exam, socket) {
        if (this.accepts(socket.handshake.session.sessionID)) {
            io.to(this.socketID).emit("new_exam", socket.handshake.session.sessionID, exam.name, exam.date, exam.start, exam.end);
            console.log("[ADD]", socket.handshake.session.sessionID, "->", this.clockID, "(" + this.clockName + "):", exam.name);
        }
    }

    delete_exam(examID, socket) {
        if (this.accepts(socket.handshake.session.sessionID)) {
            io.to(this.socketID).emit("delete_exam", socket.handshake.session.sessionID, examID);
            console.log("[DELETE]", socket.handshake.session.sessionID, "->", this.clockID, "(" + this.clockName + "):", examID);
        }
    }

    toilet(status, socket) {
        if (this.accepts(socket.handshake.session.sessionID)) {
            io.to(this.socketID).emit("toilet", socket.handshake.session.sessionID, status);
            console.log("[TOILET]", socket.handshake.session.sessionID, "->", this.clockID, "(" + this.clockName + "): TOILET STATUS =", status);
        }
    }
}

module.exports = function (sio) {
    io = sio;
    return Clock;
}