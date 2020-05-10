package sg.edu.appventure.examclock.model;

import sg.edu.appventure.examclock.connection.Base64;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Random;

public class Exam {
    public String id;
    public String name;
    public String examDate;
    public String startTime;
    public String endTime;

    public Exam() {
    }

    public Exam(String id, String name, LocalDate examDate, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.name = name;
        this.examDate = examDate.toString();
        this.startTime = startTime.withNano(0).withSecond(0).toString();
        this.endTime = endTime.withNano(0).withSecond(0).toString();
    }

    public Exam(String name, LocalDate examDate, LocalTime startTime, LocalTime endTime) {
        this(createID(), name, examDate, startTime, endTime);
    }

    public boolean isRunning() {
        return LocalDate.parse(examDate).isEqual(LocalDate.now()) && LocalTime.parse(startTime).isBefore(LocalTime.now()) && LocalTime.parse(endTime).isAfter(LocalTime.now());
    }

    public boolean hasEnded() {
        LocalDate date = LocalDate.parse(examDate);
        return date.isBefore(LocalDate.now()) || date.isEqual(LocalDate.now()) && LocalTime.parse(endTime).isBefore(LocalTime.now());
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return examDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public LocalTime getStartTimeObj() {
        return LocalTime.parse(startTime);
    }

    public LocalTime getEndTimeObj() {
        return LocalTime.parse(endTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam exam = (Exam) o;
        return Objects.equals(id, exam.id) &&
                Objects.equals(name, exam.name) &&
                Objects.equals(examDate, exam.examDate) &&
                Objects.equals(startTime, exam.startTime) &&
                Objects.equals(endTime, exam.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, examDate, startTime, endTime);
    }

    private static String createID() {
        byte[] bytes = new byte[6 * 8];
        new Random().nextBytes(bytes);
        return new String(Base64.encode(bytes));
    }
}
