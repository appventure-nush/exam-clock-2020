package sg.edu.appventure.examclock;

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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import sg.edu.appventure.examclock.addexam.AddExamController;
import sg.edu.appventure.examclock.display.ClockController;
import sg.edu.appventure.examclock.model.Exam;
import sg.edu.appventure.examclock.model.ExamHolder;
import sg.edu.appventure.examclock.model.Key;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class MainController {
    public ObservableList<Exam> exams;
    public SimpleBooleanProperty toiletOccupied = new SimpleBooleanProperty(false);
    public Stage stage;
    public Stage addExamStage;
    public ObservableList<Key> keys;
    public final Key simpleKey;
    public ExamHolder selectedExamHolder;
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
    private VBox examList;
    @FXML
    private ImageView toiletIcon;
    @FXML
    private StackPane toiletIconParent;
    private ClockController clockController;
    private PreferenceController preferenceController;
    private Stage connectStage;
    private Stack<ExamHolder> holderPool;
    private Timeline timeline;
    private AddExamController addExamController;

    public Preferences preferences;
    private FileChooser fileChooser;
    @FXML
    private MenuBar menuBar;

    public MainController() {
        this.simpleKey = new Key(Key.KeyType.TOILET);
    }

    private static String generateClockID(int length) {
        char[] set = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ0123456789".toCharArray();
        char[] res = new char[length];
        for (int i = 0; i < length; i++) res[i] = set[(int) (set.length * Math.random())];
        return new String(res);
    }

    @FXML
    public void initialize() {
        System.out.println("initialize");
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac")) menuBar.useSystemMenuBarProperty().set(true);
        exams = FXCollections.observableArrayList();
        keys = FXCollections.observableArrayList();
        preferences = Preferences.userNodeForPackage(MainController.class);
        holderPool = new Stack<>();
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
                    else {
                        ExamHolder element = holderPool.pop().setExam(c.getList().get(i));
                        examList.getChildren().add(i, element);
                        element.reset();
                    }
                }
            }
        });
        keys.addListener((ListChangeListener<Key>) c -> {
            JSONArray array = keys.stream().map(Key::toJsonObject).collect(Collectors.toCollection(JSONArray::new));
            preferences.put("keys", array.toJSONString());
        });
        String examsStr = preferences.get("exams", null);
        if (examsStr != null) {
            JSONArray root = (JSONArray) JSONValue.parse(examsStr);
            for (Object o : root) {
                JSONObject exam = (JSONObject) o;
                exams.add(new Exam(exam.getAsString("id"), exam.getAsString("name"), LocalDate.parse(exam.getAsString("examDate")), LocalTime.parse(exam.getAsString("startTime")), LocalTime.parse(exam.getAsString("endTime"))));
            }
        }
        String keyStr = preferences.get("keys", null);
        if (keyStr != null) {
            JSONArray root = (JSONArray) JSONValue.parse(keyStr);
            keys.addAll(root.stream().map(o -> Key.fromJsonObject((JSONObject) o)).collect(Collectors.toCollection(ArrayList::new)));
        }
        PreferenceController.clockID = preferences.get("clockID", generateClockID(7));
        clockController = new ClockController(clockPane, clockFace, hourGroup, minuteGroup, secondGroup, hourHand, minuteHand, secondHand);
        preferenceController = new PreferenceController(this);
        root.setStyle("-fx-font-size: " + PreferenceController.fontScaleProperty.get() + "px;");
        PreferenceController.fontScaleProperty.addListener((observable, oldValue, newValue) -> root.setStyle("-fx-font-size: " + newValue + "px;"));
        preferenceController.initPreferences();

        clockRoot.widthProperty().addListener((observable, oldValue, newValue) -> resize(clockRoot.getWidth(), clockRoot.getHeight()));
        clockRoot.heightProperty().addListener((observable, oldValue, newValue) -> resize(clockRoot.getWidth(), clockRoot.getHeight()));

        toiletOccupied.addListener((observable, oldValue, newValue) -> {
            if (newValue) toiletIconParent.getStyleClass().add("occupied");
            else toiletIconParent.getStyleClass().remove("occupied");
        });
        toiletIcon.setOnMouseClicked(e -> toiletOccupied.set(!toiletOccupied.get()));
        toiletIconParent.visibleProperty().bind(PreferenceController.showToiletProperty);

        try {
            initAddExamStage();
            initConnectionStage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(16), event1 -> refresh()));
        fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
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

    public void showAddExamStage(Exam exam) {
        addExamController.name_input.setText(exam.name);
        addExamController.date_input.setValue(LocalDate.parse(exam.examDate));
        addExamController.start_time_input.setText(AddExamController.timeFormatter.format(LocalTime.parse(exam.startTime)));
        addExamController.end_time_input.setText(AddExamController.timeFormatter.format(LocalTime.parse(exam.endTime)));
        addExamStage.show();
    }

    private void initAddExamStage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml_add_exam.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        addExamController = fxmlLoader.getController();
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

    @FXML
    public void addExamClicked(ActionEvent event) {
        addExamStage.show();
    }

    @FXML
    public void editExamClicked(ActionEvent event) {
        if (selectedExamHolder != null) selectedExamHolder.edit();
    }

    @FXML
    public void deleteExamClicked(ActionEvent event) {
        if (selectedExamHolder != null) selectedExamHolder.delete();
    }

    @FXML
    public void startSelectedExams(ActionEvent event) {
        if (selectedExamHolder != null) {
            LocalDate newDate = LocalDate.now();
            LocalTime newStartTime = LocalTime.now().withNano(0).plusSeconds(1);
            Exam exam = selectedExamHolder.getExam();
            long seconds = ChronoUnit.SECONDS.between(exam.getStartTimeObj(), exam.getEndTimeObj());
            exam.examDate = newDate.toString();
            exam.startTime = newStartTime.toString();
            exam.endTime = newStartTime.plusSeconds(seconds).toString();
            selectedExamHolder.setExam(selectedExamHolder.getExam());
        }
    }

    @FXML
    public void stopSelectedExams(ActionEvent event) {
        if (selectedExamHolder != null) {
            LocalDate newDate = LocalDate.now();
            LocalTime newEndTime = LocalTime.now().withNano(0);
            Exam exam = selectedExamHolder.getExam();
            long seconds = ChronoUnit.SECONDS.between(exam.getStartTimeObj(), exam.getEndTimeObj());
            exam.examDate = newDate.toString();
            exam.startTime = newEndTime.minusSeconds(seconds).toString();
            exam.endTime = newEndTime.toString();
            selectedExamHolder.setExam(selectedExamHolder.getExam());
        }
    }

    @FXML
    public void startAllExams(ActionEvent event) {
        LocalDate newDate = LocalDate.now();
        LocalTime newStartTime = LocalTime.now().withNano(0).plusSeconds(1);
        exams.forEach(exam -> {
            long seconds = ChronoUnit.SECONDS.between(exam.getStartTimeObj(), exam.getEndTimeObj());
            exam.examDate = newDate.toString();
            exam.startTime = newStartTime.toString();
            exam.endTime = newStartTime.plusSeconds(seconds).toString();
        });
        examList.getChildren().forEach(node -> Platform.runLater(() -> ((ExamHolder) node).setExam(((ExamHolder) node).getExam())));
    }

    @FXML
    public void stopAllExams(ActionEvent event) {
        LocalDate newDate = LocalDate.now();
        LocalTime newEndTime = LocalTime.now().withNano(0);
        exams.forEach(exam -> {
            long seconds = ChronoUnit.SECONDS.between(exam.getStartTimeObj(), exam.getEndTimeObj());
            exam.examDate = newDate.toString();
            exam.startTime = newEndTime.minusSeconds(seconds).toString();
            exam.endTime = newEndTime.toString();
        });
        examList.getChildren().forEach(node -> ((ExamHolder) node).setExam(((ExamHolder) node).getExam()));
    }


    @FXML
    public void reset(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure? This will remove everything!", ButtonType.NO, ButtonType.YES);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) exams.clear();
    }

    @FXML
    public void save(ActionEvent event) {
        JSONArray array = new JSONArray();
        array.addAll(exams);
        preferences.put("exams", array.toJSONString());
    }

    @FXML
    public void load(ActionEvent event) {
        String examsStr = preferences.get("exams", null);
        if (examsStr != null) {
            JSONArray root = (JSONArray) JSONValue.parse(examsStr);
            for (Object o : root) {
                JSONObject exam = (JSONObject) o;
                exams.add(new Exam(exam.getAsString("id"), exam.getAsString("name"), LocalDate.parse(exam.getAsString("examDate")), LocalTime.parse(exam.getAsString("startTime")), LocalTime.parse(exam.getAsString("endTime"))));
            }
        }
    }

    @FXML
    public void showSettings(ActionEvent event) {
        preferenceController.show(true);
    }

    @FXML
    public void showConnection(ActionEvent event) {
        connectStage.show();
    }

    @FXML
    public void importExams(ActionEvent actionEvent) {
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) return;
        try {
            String str = new String(Files.readAllBytes(Paths.get(file.toURI())));
            System.out.println("Read from File\n" + str);
            JSONArray root = (JSONArray) JSONValue.parse(str);
            exams.clear();
            for (Object o : root) {
                JSONObject exam = (JSONObject) o;
                exams.add(new Exam(exam.getAsString("id"), exam.getAsString("name"), LocalDate.parse(exam.getAsString("examDate")), LocalTime.parse(exam.getAsString("startTime")), LocalTime.parse(exam.getAsString("endTime"))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void exportExams(ActionEvent actionEvent) {
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;
        try {
            Files.write(Paths.get(file.toURI()), JSONArray.toJSONString(exams).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void about(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            Group logo = FXMLLoader.load(getClass().getResource("/logo_light.fxml"));

            Label title = new Label("Exam Clock", new Label(Version.getVersion()) {{
                setFont(Font.font("monospaced", 14));
            }});
            title.setContentDisplay(ContentDisplay.BOTTOM);
            title.setFont(Font.font(30));
            VBox vbox = new VBox(title,
                    new Hyperlink("GitHub Repo") {{
                        setOnAction(e -> {
                            if (Desktop.isDesktopSupported()) {
                                try {
                                    Desktop.getDesktop().browse(new URI("https://github.com/appventure-nush/exam-clock-2020"));
                                } catch (IOException | URISyntaxException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        });
                    }},
                    new TitledPane("Created by",
                            new Label("Zhao Yun (h1710169)\nWang HengYue (h1710149)\nJamie Pang (h1510104)")
                    ) {{
                        setCollapsible(false);
                    }},
                    new TitledPane("Originally by",
                            new Label("Leong Yu Siang")
                    ) {{
                        setCollapsible(false);
                    }},
                    logo);
            vbox.setPadding(new Insets(10));
            vbox.setSpacing(10);
            vbox.setAlignment(Pos.CENTER);
            Scene scene = new Scene(vbox);
            stage.setScene(scene);
            stage.setWidth(200);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
