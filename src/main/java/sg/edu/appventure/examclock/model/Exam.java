package sg.edu.appventure.examclock.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Exam {
    private String code;
    private String name;
    private String examDate;
    private String startTime;
    private String endTime;

    public Exam() {
    }

    public Exam(String code, String name, LocalDate examDate, LocalTime startTime, LocalTime endTime) {
        this.code = code;
        this.name = name;
        this.examDate = examDate.toString();
        this.startTime = startTime.toString();
        this.endTime = endTime.toString();
    }

    public boolean isRunning() {
        return LocalDate.parse(examDate).isEqual(LocalDate.now()) && LocalTime.parse(startTime).isBefore(LocalTime.now()) && LocalTime.parse(endTime).isAfter(LocalTime.now());
    }

    public boolean hasEnded() {
        LocalDate date = LocalDate.parse(examDate);
        return date.isBefore(LocalDate.now()) || date.isEqual(LocalDate.now()) && LocalTime.parse(endTime).isBefore(LocalTime.now());
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
