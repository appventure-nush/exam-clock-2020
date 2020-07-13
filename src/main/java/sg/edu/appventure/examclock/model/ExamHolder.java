package sg.edu.appventure.examclock.model;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import sg.edu.appventure.examclock.MainController;
import sg.edu.appventure.examclock.PreferenceController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ExamHolder extends HBox {
    private static final Image menu_night = new Image(ExamHolder.class.getResource("/menu_night.png").toExternalForm(), 20, 20, true, true);
    private static final Image menu_day = new Image(ExamHolder.class.getResource("/menu_day.png").toExternalForm(), 20, 20, true, true);
    private static final DateTimeFormatter FORMAT_12_HOURS = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter FORMAT_24_HOURS = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FORMAT_12_HOURS_NO_SECONDS = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter FORMAT_24_HOURS_NO_SECONDS = DateTimeFormatter.ofPattern("HH:mm");
    private final Label examName;
    private final Label examDate;
    private final Label examStartTime;
    private final Label examEndTime;
    private final Label timeLeft;
    private LocalDate date;
    private LocalTime start;
    private LocalTime end;
    private Exam exam;
    private final MainController controller;

    public ExamHolder(MainController controller) {
        this.controller = controller;
        VBox infoPane = new VBox();
        infoPane.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(infoPane, Priority.SOMETIMES);
        examName = new Label();
        examName.getStyleClass().add("exam-name");
        examName.setMaxWidth(Double.MAX_VALUE);
        examName.setWrapText(true);
        HBox.setHgrow(examName, Priority.ALWAYS);
        MenuButton menu = new MenuButton();
        menu.setGraphic(new ImageView(PreferenceController.nightMode.get() ? menu_night : menu_day));
        menu.getItems().addAll(new MenuItem("Really"), new MenuItem("Do not"));

        MenuItem deleteButton = new MenuItem("Delete");
        deleteButton.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        menu.getItems().add(deleteButton);
        deleteButton.setOnAction(e -> delete());

        MenuItem editButton = new MenuItem("Edit");
        editButton.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.EDIT));
        menu.getItems().add(editButton);
        editButton.setOnAction(e -> edit());
        infoPane.getChildren().add(new HBox(examName, menu) {{
            setSpacing(5);
        }});

        examDate = new Label();
        examStartTime = new Label();
        examStartTime.setAlignment(Pos.CENTER);
        examStartTime.setMaxWidth(Double.MAX_VALUE);
        examEndTime = new Label();
        examEndTime.setAlignment(Pos.CENTER);
        examEndTime.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(examStartTime, Priority.ALWAYS);
        HBox.setHgrow(examEndTime, Priority.ALWAYS);

        infoPane.setSpacing(5);
        infoPane.getChildren().add(new HBox(examDate, examStartTime, new Label("", new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT)), examEndTime));
        getChildren().add(infoPane);

        timeLeft = new Label();
        timeLeft.getStyleClass().addAll("time-left", "elevated");
        timeLeft.setMaxHeight(Double.MAX_VALUE);
        timeLeft.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        getChildren().add(timeLeft);

        getStyleClass().add("exam-holder");
        setMaxWidth(Double.MAX_VALUE);
        setSpacing(5);
        setPadding(new Insets(2, 4, 2, 4));
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
                timeLeft.setText(String.format("%02d", ChronoUnit.HOURS.between(start, end)) + ":" + String.format("%02d", ChronoUnit.MINUTES.between(start, end) % 60) + ":" + String.format("%02d", ChronoUnit.SECONDS.between(start, end) % 60));
                getStyleClass().removeAll("started", "ended");
            } else if (now.isAfter(end)) {
                timeLeft.setText("00:00:00");
                getStyleClass().removeAll("started", "ended");
                getStyleClass().add("ended");
            } else {
                timeLeft.setText(String.format("%02d", ChronoUnit.HOURS.between(now, end)) + ":" + String.format("%02d", ChronoUnit.MINUTES.between(now, end) % 60) + ":" + String.format("%02d", ChronoUnit.SECONDS.between(now, end) % 60));
                getStyleClass().removeAll("started", "ended");
                getStyleClass().add("started");
            }
        } else {
            timeLeft.setText(date.format(DateTimeFormatter.ofPattern("dd MMM")));
            getStyleClass().removeAll("started", "ended");
        }
        if (start.getSecond() == 0)
            examStartTime.setText(start.format(PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS_NO_SECONDS : FORMAT_24_HOURS_NO_SECONDS));
        else
            examStartTime.setText(start.format(PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS : FORMAT_24_HOURS));
        if (end.getSecond() == 0)
            examEndTime.setText(end.format(PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS_NO_SECONDS : FORMAT_24_HOURS_NO_SECONDS));
        else
            examEndTime.setText(end.format(PreferenceController.use12HourFormatProperty.get() ? FORMAT_12_HOURS : FORMAT_24_HOURS));
    }

    public Exam getExam() {
        return exam;
    }

    public ExamHolder setExam(Exam exam) {
        this.exam = exam;
        examName.setText(exam.getName());
        examDate.setText(exam.getDate().equals(LocalDate.now().toString()) ? "" : exam.getDate());
        date = LocalDate.parse(exam.getDate());
        start = LocalTime.parse(exam.getStartTime());
        end = LocalTime.parse(exam.getEndTime());
        return this;
    }

    public ExamHolder reset() {
        if (controller.selectedExamHolder == this) {
            controller.selectedExamHolder = null;
            getStyleClass().remove("selected");
        }
        return this;
    }

    public void edit() {
        controller.exams.remove(exam);
        controller.showAddExamStage(exam);
    }

    public void delete() {
        controller.exams.remove(exam);
    }
}
