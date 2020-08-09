package app.nush.examclock.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Base64;
import java.util.Objects;
import java.util.Random;

/**
 * Exam data class
 */
public class Exam {
    /**
     * Ideally this is unique
     * There is a 0.00000596046448% chance that it collides with another exam
     */
    public String id;
    /**
     * The Name.
     */
    public String name;
    /**
     * The Date.
     */
    public String date;
    /**
     * The Starting time.
     */
    public String start;
    /**
     * The Ending time.
     */
    public String end;

    /**
     * Instantiates a new Exam
     * <p>
     * To support JSON conversion
     */
    public Exam() {
    }

    /**
     * Instantiates a new exam with all data provided
     *
     * @param id    the id
     * @param name  the name
     * @param date  the date
     * @param start the start
     * @param end   the end
     */
    public Exam(String id, String name, LocalDate date, LocalTime start, LocalTime end) {
        if (name.isEmpty()) throw new RuntimeException("Name is Empty!");
        if (end.isBefore(start)) throw new RuntimeException("Start is before end!");
        this.id = id;
        this.name = name;
        this.date = date.toString();
        this.start = start.withNano(0).withSecond(0).toString();
        this.end = end.withNano(0).withSecond(0).toString();
    }

    /**
     * Instantiates a new exam with all data provided except the ID
     *
     * @param name  the name
     * @param date  the date
     * @param start the start
     * @param end   the end
     */
    public Exam(String name, LocalDate date, LocalTime start, LocalTime end) {
        this(createID(), name, date, start, end);
    }

    private static String createID() {
        byte[] bytes = new byte[3];
        new Random().nextBytes(bytes);
        return new String(Base64.getEncoder().encode(bytes));
    }

    /**
     * Is the exam running right now
     *
     * @return running
     */
    public boolean isRunning() {
        return LocalDate.parse(date).isEqual(LocalDate.now()) && LocalTime.parse(start).isBefore(LocalTime.now()) && LocalTime.parse(end).isAfter(LocalTime.now());
    }

    /**
     * Has the exam ended
     *
     * @return ended
     */
    public boolean hasEnded() {
        LocalDate date = LocalDate.parse(this.date);
        return date.isBefore(LocalDate.now()) || date.isEqual(LocalDate.now()) && LocalTime.parse(end).isBefore(LocalTime.now());
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public LocalTime getStartTimeObj() {
        return LocalTime.parse(start);
    }

    public LocalTime getEndTimeObj() {
        return LocalTime.parse(end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam exam = (Exam) o;
        return Objects.equals(id, exam.id) &&
                Objects.equals(name, exam.name) &&
                Objects.equals(this.date, exam.date) &&
                Objects.equals(start, exam.start) &&
                Objects.equals(end, exam.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, date, start, end);
    }
}
