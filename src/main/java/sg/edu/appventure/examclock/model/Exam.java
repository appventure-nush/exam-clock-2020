package sg.edu.appventure.examclock.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.regex.Pattern;

public class Exam {
    private static final Pattern MODULE_CODE_PATTERN = Pattern.compile("^[A-Z]{2}[1-6]\\d{3}$");

    public static boolean validateModuleCode(String code) {
        return MODULE_CODE_PATTERN.matcher(code).matches();
    }

    public String code;
    public String name;
    public String examDate;
    public String startTime;
    public String endTime;

    public Exam() {
    }

    public Exam(String code, String name, LocalDate examDate, LocalTime startTime, LocalTime endTime) {
        if (!validateModuleCode(code)) throw new RuntimeException("Invalid Module Code!");
        this.code = code;
        this.name = name;
        this.examDate = examDate.toString();
        this.startTime = startTime.withNano(0).withSecond(0).toString();
        this.endTime = endTime.withNano(0).withSecond(0).toString();
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
        return Objects.equals(code, exam.code) &&
                Objects.equals(name, exam.name) &&
                Objects.equals(examDate, exam.examDate) &&
                Objects.equals(startTime, exam.startTime) &&
                Objects.equals(endTime, exam.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, examDate, startTime, endTime);
    }
}
