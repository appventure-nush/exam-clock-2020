package app.nush.examclock.model;

import app.nush.examclock.MainController;
import app.nush.examclock.PreferenceController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public class ExamHolder extends HBox {

    public static final SimpleBooleanProperty showCountDownForExamProperty = new SimpleBooleanProperty(true);
    public static final SimpleBooleanProperty useSimplifiedCountdownForExamProperty = new SimpleBooleanProperty(false);
    public static final SimpleBooleanProperty showExamsProperty = new SimpleBooleanProperty(false);
    public static final SimpleListProperty<Orientation> displayOrientationList = new SimpleListProperty<>(
            FXCollections.observableArrayList(Arrays.asList(Orientation.HORIZONTAL, Orientation.VERTICAL))
    );
    public static final ObjectProperty<Orientation> displayOrientationProperty = new SimpleObjectProperty<>(Orientation.HORIZONTAL);

    private static final DateTimeFormatter FORMAT_12_HOURS = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter FORMAT_24_HOURS = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FORMAT_12_HOURS_NO_SECONDS = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter FORMAT_24_HOURS_NO_SECONDS = DateTimeFormatter.ofPattern("HH:mm");
    @FXML
    private Label nameLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label countLabel;

    private LocalDate date;
    private LocalTime start;
    private LocalTime end;
    private Exam exam;
    private final MainController controller;

    public ExamHolder(MainController controller) {
        this.controller = controller;
        FXMLLoader fxmlLoader = new FXMLLoader(ClassLoader.getSystemClassLoader().getResource("/app/nush/examclock/res/exam_holder.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        countLabel.visibleProperty().bind(showCountDownForExamProperty);
        countLabel.managedProperty().bind(countLabel.visibleProperty());

        setBorder(new Border(new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        setOnMouseClicked(event -> {
            if (controller.selectedExamHolder != null) {
                controller.selectedExamHolder.getStyleClass().remove("selected");
                if (controller.selectedExamHolder == this) {
                    controller.selectedExamHolder = null;
                    return;
                }
            }
            getStyleClass().add("selected");
            controller.selectedExamHolder = this;
        });
    }

    public ExamHolder(MainController controller, Exam exam) {
        this(controller);
        setExam(exam);
    }

    public void update(LocalDate today, LocalTime now) {
        if (today.isEqual(date)) {
            if (now.isBefore(start)) {
                countLabel.setText(String.format("%02d", ChronoUnit.HOURS.between(start, end)) + ":" + String.format("%02d", ChronoUnit.MINUTES.between(start, end) % 60) + ":" + String.format("%02d", ChronoUnit.SECONDS.between(start, end) % 60));
                getStyleClass().removeAll("started", "ended");
            } else if (now.isAfter(end)) {
                countLabel.setText("00:00:00");
                getStyleClass().removeAll("started", "ended");
                getStyleClass().add("ended");
            } else {
                float percentage = ChronoUnit.MILLIS.between(start, now) * 100f / ChronoUnit.MILLIS.between(start, end);
                setStyle("-fx-background-color: linear-gradient(to left, rgba(255, 0, 0, 0.13) 0%, rgba(255, 0, 0, 0.13) " + (percentage - 1) + "%, rgba(0, 255, 0, 0.13) " + (percentage + 1) + "%, rgba(0, 255, 0, 0.13) 100%);");
                if (useSimplifiedCountdownForExamProperty.get()) {
                    long hours = ChronoUnit.HOURS.between(now, end);
                    long minutes = ChronoUnit.MINUTES.between(now, end);
                    long seconds = ChronoUnit.MINUTES.between(now, end);
                    if (hours > 0) countLabel.setText(hours + " hrs");
                    else if (minutes > 0) countLabel.setText(minutes + " min");
                    else if (seconds > 0) countLabel.setText(seconds + " sec");
                    else countLabel.setText("STOP");
                } else {
                    countLabel.setText(String.format("%02d", ChronoUnit.HOURS.between(now, end)) + ":" + String.format("%02d", ChronoUnit.MINUTES.between(now, end) % 60) + ":" + String.format("%02d", ChronoUnit.SECONDS.between(now, end) % 60));
                    getStyleClass().removeAll("started", "ended");
                    getStyleClass().add("started");
                }
            }
        } else {
            countLabel.setText(date.format(DateTimeFormatter.ofPattern("dd MMM")));
            getStyleClass().removeAll("started", "ended");
        }
        timeLabel.setText(
                (exam.getDate().equals(LocalDate.now().toString()) ? "" : exam.getDate()) +
                        start.format(start.getSecond() == 0 ? PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS_NO_SECONDS : FORMAT_24_HOURS_NO_SECONDS : PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS : FORMAT_24_HOURS) +
                        " → " +
                        end.format(end.getSecond() == 0 ? PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS_NO_SECONDS : FORMAT_24_HOURS_NO_SECONDS : PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS : FORMAT_24_HOURS));
    }

    public Exam getExam() {
        return exam;
    }

    public ExamHolder setExam(Exam exam) {
        this.exam = exam;
        nameLabel.setText(exam.getName());
        date = LocalDate.parse(exam.getDate());
        start = LocalTime.parse(exam.getStart());
        end = LocalTime.parse(exam.getEnd());
        return this;
    }

    public ExamHolder reset() {
        if (controller.selectedExamHolder == this) {
            controller.selectedExamHolder = null;
            getStyleClass().remove("selected");
        }
        return this;
    }

    public void onDupe(ActionEvent e) {
        controller.exams.add(new Exam(exam.name, LocalDate.parse(exam.getDate()), exam.getStartTimeObj(), exam.getEndTimeObj()));
    }

    public void onEdit(ActionEvent e) {
        controller.exams.remove(exam);
        controller.showAddExamStage(exam);
    }

    public void onDelete(ActionEvent e) {
        controller.exams.remove(exam);
    }
}
