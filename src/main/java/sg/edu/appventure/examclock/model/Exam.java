package sg.edu.appventure.examclock.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Exam {
    private String code;
    private String name;
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;

    public Exam(String code, String name, LocalDate examDate, LocalTime startTime, LocalTime endTime) {
        this.code = code;
        this.name = name;
        this.examDate = examDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isRunning() {
        return examDate.isEqual(LocalDate.now()) && startTime.isBefore(LocalTime.now()) && endTime.isAfter(LocalTime.now());
    }

    public boolean hasEnded() {
        return examDate.isBefore(LocalDate.now()) || examDate.isEqual(LocalDate.now()) && endTime.isBefore(LocalTime.now());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
