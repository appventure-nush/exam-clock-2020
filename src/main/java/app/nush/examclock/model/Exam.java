package app.nush.examclock.model;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Objects;
import java.util.Random;

/**
 * Exam data class
 */
public class Exam {
    public static final JsonSerializer<Exam> serializer;
    public static final JsonDeserializer<Exam> deserializer;

    static {
        serializer = (src, typeOfSrc, context) -> {
            JsonObject exam = new JsonObject();
            exam.addProperty("id", src.getID());
            exam.addProperty("name", src.getName());
            exam.addProperty("date", src.getDate().toString());
            exam.addProperty("start", src.getStart().toString());
            exam.addProperty("end", src.getEnd().toString());
            return exam;
        };
        deserializer = (json, typeOfT, context) -> {
            JsonObject object = json.getAsJsonObject();
            return new Exam(object.get("id").getAsString(), object.get("name").getAsString(), LocalDate.parse(object.get("date").getAsString()), LocalTime.parse(object.get("start").getAsString()), LocalTime.parse(object.get("end").getAsString()));
        };
    }

    /**
     * Ideally this is unique
     * There is a 0.00000596046448% chance that it collides with another exam
     */
    private String id;
    /**
     * The Name.
     */
    private String name;
    /**
     * The Date.
     */
    private LocalDate date;
    /**
     * The Starting time.
     */
    private LocalTime start;

    /**
     * Instantiates a new Exam
     * <p>
     * To support JSON conversion
     */
    public Exam() {
    }

    /**
     * The Ending time.
     */
    private LocalTime end;

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
        this.date = date;
        this.start = start.withNano(0).withSecond(0);
        this.end = end.withNano(0).withSecond(0);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Exam setStart(LocalDate date, LocalTime time) {
        long seconds = ChronoUnit.SECONDS.between(getStart(), getEnd());
        setDate(date);
        setStart(time);
        setEnd(time.plusSeconds(seconds));
        return this;
    }

    public Exam setEnd(LocalDate date, LocalTime time) {
        long seconds = ChronoUnit.SECONDS.between(getStart(), getEnd());
        setDate(date);
        setStart(time.minusSeconds(seconds));
        setEnd(time);
        return this;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, date, start, end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam exam = (Exam) o;
        return Objects.equals(id, exam.id) &&
                Objects.equals(name, exam.name) &&
                Objects.equals(date, exam.date) &&
                Objects.equals(start, exam.start) &&
                Objects.equals(end, exam.end);
    }
}
