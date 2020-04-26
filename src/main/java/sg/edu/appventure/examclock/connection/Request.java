package sg.edu.appventure.examclock.connection;

import sg.edu.appventure.examclock.model.Exam;

public class Request {
    public static final String ADD = "add";
    public static final String REMOVED = "remove";
    public static final String EDIT = "edit";
    public static final String TOILET_ON = "toilet_on";
    public static final String TOILET_OFF = "toilet_off";

    public String type;
    public Exam exam;

    public Request() {
    }

    public Request(String type, Exam exam) {
        this.type = type;
        this.exam = exam;
    }
}
