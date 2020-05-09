package sg.edu.appventure.examclock.model;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXNodesList;
import com.jfoenix.transitions.hamburger.HamburgerBasicCloseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import sg.edu.appventure.examclock.MainController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ExamHolder extends HBox {

    private static final PseudoClass STARTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("started");

    private final Label examCode;
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
    private final JFXNodesList list;
    private final BooleanProperty started;

    public ExamHolder(MainController controller) {
        this.controller = controller;
        VBox infoPane = new VBox();
        infoPane.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(infoPane, Priority.ALWAYS);

        examCode = new Label();
        examCode.getStyleClass().add("exam-code");
        examName = new Label();
        examName.getStyleClass().add("exam-name");
        examName.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(examName, Priority.ALWAYS);
        list = new JFXNodesList();
        list.setSpacing(10);

        {
            JFXHamburger hamburger = new JFXHamburger();
            HamburgerBasicCloseTransition animation = new HamburgerBasicCloseTransition(hamburger);
            hamburger.setAnimation(animation);
            hamburger.setMaxWidth(14);
            hamburger.setMaxHeight(12);
            animation.setRate(-1);

            JFXButton menuButton = new JFXButton();
            menuButton.setFont(Font.font(14));
            menuButton.setGraphic(hamburger);
            menuButton.setDisableVisualFocus(true);
            menuButton.getStyleClass().addAll("animated-option-button", "animated-option-sub-button");
            menuButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                animation.setRate(list.isExpanded() ? 1 : -1);
                animation.play();
            });
            list.addAnimatedNode(menuButton);

            JFXButton deleteButton = new JFXButton();
            deleteButton.setFont(Font.font(14));
            deleteButton.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
            deleteButton.getStyleClass().addAll("primary-raised", "animated-option-button");
            list.addAnimatedNode(deleteButton);
            deleteButton.setOnAction(e -> controller.exams.remove(exam));

            JFXButton editButton = new JFXButton();
            editButton.setFont(Font.font(14));
            editButton.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.EDIT));
            editButton.getStyleClass().addAll("primary-raised", "animated-option-button");
            list.addAnimatedNode(editButton);
            list.setRotate(90);
        }

        infoPane.getChildren().add(new HBox(examCode, examName, list) {{
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

        infoPane.getChildren().add(new HBox(examDate, examStartTime, new Label("", new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT)), examEndTime));
        getChildren().add(infoPane);

        timeLeft = new Label();
        timeLeft.getStyleClass().addAll("time-left", "elevated");
        timeLeft.setMaxHeight(Double.MAX_VALUE);
        getChildren().add(timeLeft);

        getStyleClass().add("exam-holder");
        setMaxWidth(Double.MAX_VALUE);
        setSpacing(5);
        setPadding(new Insets(2, 4, 2, 4));
        setBorder(new Border(new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        started = new BooleanPropertyBase(false) {
            public void invalidated() {
                pseudoClassStateChanged(STARTED_PSEUDO_CLASS, get());
            }

            public Object getBean() {
                return ExamHolder.this;
            }

            public String getName() {
                return "started";
            }
        };
    }

    public ExamHolder(MainController controller, Exam exam) {
        this(controller);
        setExam(exam);
    }

    public ExamHolder setExam(Exam exam) {
        this.exam = exam;
        examCode.setText(exam.getCode());
        examName.setText(exam.getName());
        examDate.setText(exam.getDate().equals(LocalDate.now().toString()) ? "" : exam.getDate());
        examStartTime.setText(exam.getStartTime());
        examEndTime.setText(exam.getEndTime());
        date = LocalDate.parse(exam.getDate());
        start = LocalTime.parse(exam.getStartTime());
        end = LocalTime.parse(exam.getEndTime());
        return this;
    }

    public void update(LocalDate today, LocalTime now) {
        if (today.isEqual(date)) {
            if (now.isBefore(start)) {
                timeLeft.setText(String.format("%02d", ChronoUnit.HOURS.between(start, end)) + ":" + String.format("%02d", ChronoUnit.MINUTES.between(start, end) % 60) + ":" + String.format("%02d", ChronoUnit.SECONDS.between(start, end) % 60));
                started.set(false);
            } else if (now.isAfter(end)) {
                timeLeft.setText("00:00:00");
                started.set(false);
            } else {
                timeLeft.setText(String.format("%02d", ChronoUnit.HOURS.between(now, end)) + ":" + String.format("%02d", ChronoUnit.MINUTES.between(now, end) % 60) + ":" + String.format("%02d", ChronoUnit.SECONDS.between(now, end) % 60));
                started.set(true);
            }
        } else {
            timeLeft.setText(date.format(DateTimeFormatter.ofPattern("dd MMM")));
            started.set(false);
        }
    }

    public Exam getExam() {
        return exam;
    }

    public ExamHolder reset() {
        list.animateList(false);
        return this;
    }
}
