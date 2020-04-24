package sg.edu.appventure.examclock.model;

import javafx.scene.layout.Region;

public class ExamHolder extends Region {
    private Exam exam;

    public ExamHolder() {

    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }
}
