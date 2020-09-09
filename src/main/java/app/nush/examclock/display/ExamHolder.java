package app.nush.examclock.display;

import app.nush.examclock.controllers.MainController;
import app.nush.examclock.controllers.PreferenceController;
import app.nush.examclock.model.Exam;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
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

/**
 * Exam holder.
 * holds a exam, obviously
 * <p>
 * Reusable, Resettable, Renewable (3Rs)
 */
public class ExamHolder extends HBox {

    /**
     * should exams be saved automatically
     */
    public static final SimpleBooleanProperty autoSaveProperty = new SimpleBooleanProperty(true);
    /**
     * if exams are shown
     */
    public static final SimpleBooleanProperty showExamsProperty = new SimpleBooleanProperty(true);
    /**
     * if count down are shown, i.e. "00:23:23"
     */
    public static final SimpleBooleanProperty showCountDownForExamProperty = new SimpleBooleanProperty(true);
    /**
     * if exams that are on different days are shown
     */
    public static final SimpleBooleanProperty showExamsFromOtherDaysProperty = new SimpleBooleanProperty(true);
    /**
     * if count down should be simplified, i.e. "12min" instead of "00:12:04"
     */
    public static final SimpleBooleanProperty useSimplifiedCountdownForExamProperty = new SimpleBooleanProperty(false);
    /**
     * feather value (fade) of the prgress gradient
     */
    public static final SimpleDoubleProperty gradientFeatherProperty = new SimpleDoubleProperty(1);
    /**
     * this is used by the setting displayOrientationProperty
     */
    public static final SimpleListProperty<Orientation> displayOrientationList = new SimpleListProperty<>(
            FXCollections.observableArrayList(Arrays.asList(Orientation.HORIZONTAL, Orientation.VERTICAL))
    );
    /**
     * the orientation of the exam list and the clock, i.e. if clock is above exam list, or if its beside exam list
     */
    public static final ObjectProperty<Orientation> displayOrientationProperty = new SimpleObjectProperty<>(Orientation.HORIZONTAL);

    public static final SimpleListProperty<String> progressDirectionList = new SimpleListProperty<>(
            FXCollections.observableArrayList(Arrays.asList("Left", "Right", "Top", "Bottom"))
    );
    public static final SimpleObjectProperty<String> progressDirectionProperty = new SimpleObjectProperty<>("Left");

    public static final ObjectProperty<Color> colorSpentProgressProperty = new SimpleObjectProperty<>(Color.rgb(255, 0, 0, 0.13));
    public static final ObjectProperty<Color> colorRemainProgressProperty = new SimpleObjectProperty<>(Color.rgb(0, 255, 0, 0.13));

    private static final DateTimeFormatter FORMAT_12_HOURS = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter FORMAT_24_HOURS = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FORMAT_12_HOURS_NO_SECONDS = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter FORMAT_24_HOURS_NO_SECONDS = DateTimeFormatter.ofPattern("HH:mm");
    private final MainController controller;
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

    /**
     * Instantiates a new exam holder.
     *
     * @param controller the controller
     */
    public ExamHolder(MainController controller) {
        this.controller = controller;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/exam_holder.fxml"));
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
        managedProperty().bind(visibleProperty());
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

    /**
     * Instantiates a new exam holder, with exam provided
     *
     * @param controller the controller
     * @param exam       the exam
     */
    public ExamHolder(MainController controller, Exam exam) {
        this(controller);
        setExam(exam);
    }

    private static String formatColor(Color color) {
        int r = (int) Math.round(color.getRed() * 255.0);
        int g = (int) Math.round(color.getGreen() * 255.0);
        int b = (int) Math.round(color.getBlue() * 255.0);
        int o = (int) Math.round(color.getOpacity() * 255.0);
        return String.format("#%02x%02x%02x%02x", r, g, b, o);
    }

    public Exam getExam() {
        return exam;
    }

    /**
     * Update function
     *
     * @param today the today
     * @param now   the now
     */
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
                setStyle(String.format("-fx-background-color: linear-gradient(to %s, %s 0%%, %s %f%%, %s %f%%, %s 100%%);", progressDirectionProperty.get(), formatColor(colorSpentProgressProperty.get()),
                        formatColor(colorSpentProgressProperty.get()), percentage - gradientFeatherProperty.get(),
                        formatColor(colorRemainProgressProperty.get()), percentage + gradientFeatherProperty.get(), formatColor(colorRemainProgressProperty.get())));
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
            timeLabel.setText(start.format(start.getSecond() == 0 ? PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS_NO_SECONDS : FORMAT_24_HOURS_NO_SECONDS : PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS : FORMAT_24_HOURS) +
                    " → " +
                    end.format(end.getSecond() == 0 ? PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS_NO_SECONDS : FORMAT_24_HOURS_NO_SECONDS : PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS : FORMAT_24_HOURS));
        } else {
            countLabel.setText(date.format(DateTimeFormatter.ofPattern("dd MMM")));
            timeLabel.setText(date.format(DateTimeFormatter.ofPattern("dd MMM")) + ": " + start.format(start.getSecond() == 0 ? PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS_NO_SECONDS : FORMAT_24_HOURS_NO_SECONDS : PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS : FORMAT_24_HOURS) +
                    " → " +
                    end.format(end.getSecond() == 0 ? PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS_NO_SECONDS : FORMAT_24_HOURS_NO_SECONDS : PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS : FORMAT_24_HOURS));
            getStyleClass().removeAll("started", "ended");
        }
    }

    public ExamHolder reset() {
        this.exam = null;
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
        controller.showAddExamStage(exam);
        controller.exams.remove(exam);
    }

    public void onDelete(ActionEvent e) {
        controller.exams.remove(exam);
    }

    public ExamHolder setExam(Exam exam) {
        this.exam = exam;
        nameLabel.setText(exam.getName());
        date = LocalDate.parse(exam.getDate());
        start = LocalTime.parse(exam.getStart());
        end = LocalTime.parse(exam.getEnd());
        if (visibleProperty().isBound()) visibleProperty().unbind();
        visibleProperty().bind(Bindings.createBooleanBinding(() -> showExamsFromOtherDaysProperty.get() || date.equals(LocalDate.now()), showExamsFromOtherDaysProperty));
        return this;
    }
}
