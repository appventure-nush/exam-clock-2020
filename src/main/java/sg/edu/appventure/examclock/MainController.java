package sg.edu.appventure.examclock;

import com.jfoenix.controls.JFXButton;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import sg.edu.appventure.examclock.addexam.AddExamController;
import sg.edu.appventure.examclock.display.ClockController;
import sg.edu.appventure.examclock.model.Exam;
import sg.edu.appventure.examclock.model.ExamHolder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Stack;

public class MainController {
    @FXML
    private SplitPane root;
    @FXML
    private Pane clockRoot;
    @FXML
    private Group clockPane;
    @FXML
    private Group clockFace;
    @FXML
    private Circle clockDisc;
    @FXML
    private Group hourGroup;
    @FXML
    private Polygon hourHand;
    @FXML
    private Group minuteGroup;
    @FXML
    private Polygon minuteHand;
    @FXML
    private Group secondGroup;
    @FXML
    private Polygon secondHand;
    @FXML
    private VBox rightPane;
    @FXML
    private JFXButton addBtn;
    @FXML
    private JFXButton startBtn;
    @FXML
    private JFXButton stopBtn;
    @FXML
    private JFXButton settingBtn;
    @FXML
    private VBox examList;

    public ObservableList<Exam> exams;

    private ClockController clockController;
    private PreferenceController preferenceController;
    public Stage stage;
    public Stage fullScreenStage;
    public Stage addExamStage;
    private WindowButtons windowButtons;

    private Stack<ExamHolder> holderPool;

    private Timeline timeline;

    @FXML
    public void initialize() {
        System.out.println("initialize");
        exams = FXCollections.observableArrayList();
        exams.addListener((ListChangeListener<Exam>) c -> {
            while (c.next()) {
                List<? extends Exam> removed = c.getRemoved();
                examList.getChildren().removeIf(node -> {
                    ExamHolder examHolder = (ExamHolder) node;
                    boolean contains = removed.contains(examHolder.getExam());
                    if (contains) holderPool.push(examHolder.reset());
                    return contains;
                });
                for (int i = c.getFrom(); i < c.getTo(); i++) {
                    if (holderPool.empty()) examList.getChildren().add(i, new ExamHolder(this, c.getList().get(i)));
                    else examList.getChildren().add(i, holderPool.pop().setExam(c.getList().get(i)));
                }
            }
        });
        holderPool = new Stack<>();
        clockController = new ClockController(clockPane, clockFace, hourGroup, minuteGroup, secondGroup, hourHand, minuteHand, secondHand);
        preferenceController = new PreferenceController(this);
        PreferenceController.fontScaleProperty.addListener((observable, oldValue, newValue) -> root.setStyle("-fx-font-size: " + newValue + "px;"));
        preferenceController.initPreferences();
        rightPane.getChildren().add(0, windowButtons = new WindowButtons());

        clockRoot.widthProperty().addListener((observable, oldValue, newValue) -> resize(clockRoot.getWidth(), clockRoot.getHeight()));
        clockRoot.heightProperty().addListener((observable, oldValue, newValue) -> resize(clockRoot.getWidth(), clockRoot.getHeight()));
        try {
            initAddStage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fullScreenStage = new Stage();
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(16), event1 -> refresh()));

        debug:
        addCallback(new Exam("MA2020", "Math", LocalDate.now(), LocalTime.now().plusHours(0), LocalTime.now().plusHours(2)));

        startBtn.setOnAction(e -> startAllExams());
        stopBtn.setOnAction(e -> stopAllExams());
        play();
    }

    public void refresh() {
        clockController.refresh();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        examList.getChildren().forEach(node -> {
            ExamHolder examHolder = (ExamHolder) node;
            examHolder.update(today, now);
        });
    }

    public void resize(double width, double height) {
        clockController.resize(width, height);
    }

    @FXML
    public void addExamClicked(ActionEvent event) {
        addExamStage.show();
    }

    private void initAddStage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml_add_exam.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        AddExamController addExamController = fxmlLoader.getController();
        addExamController.setMainController(this);
        addExamStage = new Stage();
        scene.getStylesheets().add("/main.css");
        scene.getStylesheets().add("/theme.css");
        scene.getStylesheets().add(PreferenceController.nightMode.get() ? "theme.dark.css" : "/theme.light.css");
        scene.getStylesheets().add(PreferenceController.nightMode.get() ? "/picker.dark.css" : "/picker.light.css");
        addExamStage.setTitle("Exam Clock");
        addExamStage.initModality(Modality.APPLICATION_MODAL);
        addExamStage.setScene(scene);
    }

    public void addCallback(Exam exam) {
        exams.add(exam);
        addExamStage.hide();
    }

    @FXML
    public void showSettings(ActionEvent event) {
        preferenceController.show(true);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        windowButtons.setStage(stage, fullScreenStage);
    }

    public void onClose(WindowEvent event) {
        timeline.stop();
        preferenceController.onClose(event);
    }

    public void startAllExams() {
        exams.forEach(exam -> {
            long seconds = ChronoUnit.SECONDS.between(exam.getStartTimeObj(), exam.getEndTimeObj());
            LocalTime newStartTime = LocalTime.now().withNano(0);
            exam.startTime = newStartTime.toString();
            exam.endTime = newStartTime.plusSeconds(seconds).toString();
        });
        examList.getChildren().forEach(node -> Platform.runLater(() -> ((ExamHolder) node).setExam(((ExamHolder) node).getExam())));
    }

    public void stopAllExams() {
        exams.forEach(exam -> {
            long seconds = ChronoUnit.SECONDS.between(exam.getStartTimeObj(), exam.getEndTimeObj());
            LocalTime newEndTime = LocalTime.now().withNano(0);
            exam.startTime = newEndTime.minusSeconds(seconds).toString();
            exam.endTime = newEndTime.toString();
        });
        examList.getChildren().forEach(node -> ((ExamHolder) node).setExam(((ExamHolder) node).getExam()));
    }

    static class WindowButtons extends ToolBar {

        private Stage stage;
        private Stage fullscreen;
        private double xOffset;
        private double yOffset;

        boolean isFullscreen = false;

        public WindowButtons() {
            getStyleClass().add("window-buttons");
            JFXButton close = new JFXButton("", new Glyph("FontAwesome", FontAwesome.Glyph.CLOSE));
            close.setDisableVisualFocus(true);
            close.setOnMouseClicked(event -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));
            JFXButton max = new JFXButton("", new Glyph("FontAwesome", FontAwesome.Glyph.SQUARE_ALT));
            max.setDisableVisualFocus(true);
            max.setOnMouseClicked(event -> {
                isFullscreen = !isFullscreen;
                if (isFullscreen) {
                    stage.hide();
                    fullscreen.setScene(stage.getScene());
                    fullscreen.setFullScreen(true);
                    fullscreen.show();
                } else {
                    fullscreen.setFullScreen(false);
                    fullscreen.hide();
                    stage.setScene(fullscreen.getScene());
                    stage.show();
                }
            });
            JFXButton min = new JFXButton("", new Glyph("FontAwesome", FontAwesome.Glyph.MINUS));
            min.setDisableVisualFocus(true);
            min.setOnMouseClicked(event -> stage.setIconified(true));
            setOnMousePressed(event -> {
                xOffset = stage.getX() - event.getScreenX();
                yOffset = stage.getY() - event.getScreenY();
            });
            setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() + xOffset);
                stage.setY(event.getScreenY() + yOffset);
            });
            Region region = new Region();
            region.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(region, Priority.ALWAYS);
            getItems().addAll(region, min, max, close);
            setPrefHeight(24);
            setMaxHeight(24);
            setHeight(24);
            setBackground(new Background(new BackgroundFill(Color.RED, new CornerRadii(1), null)));
        }

        public void setStage(Stage stage, Stage fullscreen) {
            this.stage = stage;
            this.fullscreen = fullscreen;
        }
    }

    public void play() {
        timeline.stop();
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }
}
