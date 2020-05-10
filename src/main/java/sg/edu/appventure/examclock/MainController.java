package sg.edu.appventure.examclock;

import com.jfoenix.controls.JFXButton;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import sg.edu.appventure.examclock.addexam.AddExamController;
import sg.edu.appventure.examclock.display.ClockController;
import sg.edu.appventure.examclock.model.Exam;
import sg.edu.appventure.examclock.model.ExamHolder;
import sg.edu.appventure.examclock.model.Key;

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
    @FXML
    private ImageView toiletIcon;
    @FXML
    private StackPane toiletIconParent;

    public ObservableList<Exam> exams;
    public SimpleBooleanProperty toiletOccupied = new SimpleBooleanProperty(false);

    private ClockController clockController;
    private PreferenceController preferenceController;
    public Stage stage;
    public Stage addExamStage;
    private Stage connectStage;

    private Stack<ExamHolder> holderPool;

    private Timeline timeline;

    public ObservableList<Key> keys;

    @FXML
    public void initialize() {
        System.out.println("initialize");
        exams = FXCollections.observableArrayList();
        keys = FXCollections.observableArrayList();
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

        clockRoot.widthProperty().addListener((observable, oldValue, newValue) -> resize(clockRoot.getWidth(), clockRoot.getHeight()));
        clockRoot.heightProperty().addListener((observable, oldValue, newValue) -> resize(clockRoot.getWidth(), clockRoot.getHeight()));

        toiletOccupied.addListener((observable, oldValue, newValue) -> {
            if (newValue) toiletIconParent.getStyleClass().add("occupied");
            else toiletIconParent.getStyleClass().remove("occupied");
        });
        toiletIcon.setOnMouseClicked(e -> toiletOccupied.set(!toiletOccupied.get()));
        Blend blendEffect = new Blend(BlendMode.DIFFERENCE);
        ColorInput input = new ColorInput();
        blendEffect.setTopInput(input);
        toiletIcon.setEffect(blendEffect);
        PreferenceController.nightMode.addListener((observable, oldValue, newValue) -> {
            if (newValue) toiletIcon.setEffect(blendEffect);
            else toiletIcon.setEffect(null);
        });

        try {
            initAddExamStage();
            initConnectionStage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(16), event1 -> refresh()));

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

    private void initAddExamStage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml_add_exam.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        AddExamController addExamController = fxmlLoader.getController();
        addExamController.setMainController(this);
        addExamStage = new Stage();
        scene.getStylesheets().add("/main.css");
        scene.getStylesheets().add("/theme.css");
        scene.getStylesheets().add(PreferenceController.nightMode.get() ? "theme.dark.css" : "/theme.light.css");
        scene.getStylesheets().add(PreferenceController.nightMode.get() ? "/picker.dark.css" : "/picker.light.css");
        addExamStage.setTitle("Add Exam");
        addExamStage.initModality(Modality.APPLICATION_MODAL);
        addExamStage.setScene(scene);
    }

    private void initConnectionStage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml_connect.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ConnectionController connectionController = fxmlLoader.getController();
        connectionController.setMainController(this);
        connectStage = new Stage();
        scene.getStylesheets().add("/main.css");
        scene.getStylesheets().add("/theme.css");
        scene.getStylesheets().add(PreferenceController.nightMode.get() ? "theme.dark.css" : "/theme.light.css");
        scene.getStylesheets().add(PreferenceController.nightMode.get() ? "/picker.dark.css" : "/picker.light.css");
        connectStage.setTitle("Connection");
        connectStage.setResizable(false);
        connectStage.initModality(Modality.APPLICATION_MODAL);
        connectStage.setScene(scene);
    }

    public void addCallback(Exam exam) {
        exams.add(exam);
        addExamStage.hide();
    }

    public void startAllExams() {
        exams.forEach(exam -> {
            long seconds = ChronoUnit.SECONDS.between(exam.getStartTimeObj(), exam.getEndTimeObj());
            LocalTime newStartTime = LocalTime.now().withNano(0).plusSeconds(1);
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

    @FXML
    public void showSettings(ActionEvent event) {
        preferenceController.show(true);
    }

    @FXML
    public void showConnection(ActionEvent event) {
        connectStage.show();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void onClose(WindowEvent event) {
        stop();
        preferenceController.onClose(event);
    }

    public void play() {
        timeline.stop();
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }
}
