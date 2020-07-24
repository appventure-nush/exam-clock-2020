let io;

class Clock {
    exams;
    clockID;
    socketID;
    clockName; // purely for display

    constructor(clockID, socketID, clockName, exams) {
        this.exams = exams;
        this.clockID = clockID;
        this.socketID = socketID;
        this.clockName = clockName;
        this.controllers = [];
    }

    accepts(controllerID) {
        return this.controllers.includes(controllerID);
    }

    acceptsSocket(socket) {
        return this.accepts(socket.request.user.id);
    }

    request(nick, socket) {
        if (this.accepts(socket.request.user.id)) socket.emit('request_callback', "accepted")
        else io.to(this.socketID).emit("request", socket.request.user.id, nick);
    }

    request_callback(controllerID) {
        if (!this.accepts(controllerID)) this.controllers.push(controllerID);
    }

    new_exam(exam, socket) {
        if (this.acceptsSocket(socket)) {
            io.to(this.socketID).emit("new_exam", socket.request.user.id, exam.name, exam.date, exam.start, exam.end);
            console.log("[ADD]", socket.request.user.id, "->", this.clockID, "(" + this.clockName + "):", exam.name);
        }
    }

    edit_exam(exam, socket) {
        if (this.acceptsSocket(socket)) {
            io.to(this.socketID).emit("edit_exam", socket.request.user.id, exam.id, exam.name, exam.date, exam.start, exam.end);
            let index = this.exams.findIndex(e => e.id === exam.id);
            this.exams[index].name = exam.name;
            this.exams[index].date = exam.date;
            this.exams[index].start = exam.start;
            this.exams[index].end = exam.end;
            console.log("[EDIT]", socket.request.user.id, "->", this.clockID, "(" + this.clockName + "):", exam.name);
        }
    }

    delete_exam(examID, socket) {
        if (this.acceptsSocket(socket)) {
            io.to(this.socketID).emit("delete_exam", socket.request.user.id, examID);
            console.log("[DELETE]", socket.request.user.id, "->", this.clockID, "(" + this.clockName + "):", examID);
        }
    }

    toilet(socket) {
        if (this.acceptsSocket(socket)) {
            io.to(this.socketID).emit("toilet", socket.request.user.id);
            console.log("[TOILET]", socket.request.user.id, "->", this.clockID, "(" + this.clockName + ")");
        }
    }

    newExam(exam) {
        this.exams.push({id: exam.id, name: exam.name, date: exam.date, start: exam.start, end: exam.end})
    }

    deleteExam(examID) {
        let index = this.exams.findIndex(exam => exam.id === examID);
        if (index !== -1) this.exams.splice(index, 1);
    }
}

module.exports = function (ioc) {
    io = ioc;
    return Clock;
}