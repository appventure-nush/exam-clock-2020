package app.nush.examclock;

import app.nush.examclock.addexam.AddExamController;
import app.nush.examclock.display.ClockController;
import app.nush.examclock.model.Exam;
import app.nush.examclock.model.ExamHolder;
import app.nush.examclock.updater.Updater;
import com.google.gson.Gson;
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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Stack;
import java.util.prefs.Preferences;

public class MainController {
    public static final Gson gson = new Gson();
    public ObservableList<Exam> exams;
    public SimpleBooleanProperty toiletFemaleOccupied = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty toiletMaleOccupied = new SimpleBooleanProperty(false);
    public Stage stage;
    public Stage addExamStage;
    //    public ObservableList<Key> keys;
//    public final Key simpleKey;
    public ExamHolder selectedExamHolder;
    @FXML
    private SplitPane root;
    @FXML
    private StackPane clockRoot;
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
    private HBox toiletIconParent;
    @FXML
    private ImageView toiletMale;
    @FXML
    private ImageView toiletFemale;
    private ClockController clockController;
    private PreferenceController preferenceController;
    public ConnectionController connectionController;
    private Stage connectStage;
    private Stack<ExamHolder> holderPool;
    private Timeline timeline;
    private AddExamController addExamController;

    public Preferences preferences;
    private FileChooser fileChooser;
    @FXML
    private MenuBar menuBar;

    public MainController() {
//        this.simpleKey = new Key(Key.KeyType.TOILET);
    }

    private static String generateClockID() {
        char[] set = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ0123456789".toCharArray();
        char[] res = new char[7];
        for (int i = 0; i < 7; i++) res[i] = set[(int) (set.length * Math.random())];
        return new String(res);
    }

    public void regenClockID() {
        PreferenceController.clockID = generateClockID();
    }

    @FXML
    public void initialize() {
        System.out.println("initialize");
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac")) menuBar.useSystemMenuBarProperty().set(true);
        exams = FXCollections.observableArrayList();
//        keys = FXCollections.observableArrayList();
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
//        keys.addListener((ListChangeListener<Key>) c -> {
//            JSONArray array = keys.stream().map(Key::toJsonObject).collect(Collectors.toCollection(JSONArray::new));
//            preferences.put("keys", array.toJSONString());
//        });
        load(null);
//        String keyStr = preferences.get("keys", null);
//        if (keyStr != null) {
//            JSONArray root = (JSONArray) JSONValue.parse(keyStr);
//            keys.addAll(root.stream().map(o -> Key.fromJsonObject((JSONObject) o)).collect(Collectors.toCollection(ArrayList::new)));
//        }
        PreferenceController.clockID = preferences.get("clockID", generateClockID());
        preferences.put("clockID", PreferenceController.clockID);
        clockController = new ClockController(clockPane, clockFace, hourGroup, minuteGroup, secondGroup, hourHand, minuteHand, secondHand);
        preferenceController = new PreferenceController(this);
        root.setStyle("-fx-font-size: " + PreferenceController.fontScaleProperty.get() + "px;");
        PreferenceController.fontScaleProperty.addListener((observable, oldValue, newValue) -> root.setStyle("-fx-font-size: " + newValue + "px;"));
        examList.visibleProperty().bind(ExamHolder.showExamsProperty);
        root.orientationProperty().bind(ExamHolder.displayOrientationProperty);
        preferenceController.initPreferences();

        clockRoot.widthProperty().addListener((observable, oldValue, newValue) -> resize(clockRoot.getWidth(), clockRoot.getHeight()));
        clockRoot.heightProperty().addListener((observable, oldValue, newValue) -> resize(clockRoot.getWidth(), clockRoot.getHeight()));
        rightPane.visibleProperty().bind(ExamHolder.showExamsProperty);
        ExamHolder.showExamsProperty.addListener(((observable, oldValue, newValue) -> root.setDividerPositions(newValue ? 0.5 : 1)));

        ColorAdjust redEffect = new ColorAdjust();
        redEffect.setBrightness(0.5);
        redEffect.setSaturation(1);
        redEffect.setHue(0);
        ColorAdjust greenEffect = new ColorAdjust();
        greenEffect.setBrightness(0.5);
        greenEffect.setSaturation(1);
        greenEffect.setHue(0.5);
        toiletMale.setEffect(greenEffect);
        toiletFemale.setEffect(greenEffect);
        toiletFemaleOccupied.addListener((observable, oldValue, newValue) -> {
            if (newValue) toiletFemale.setEffect(redEffect);
            else toiletFemale.setEffect(greenEffect);
        });
        toiletMaleOccupied.addListener((observable, oldValue, newValue) -> {
            if (newValue) toiletMale.setEffect(redEffect);
            else toiletMale.setEffect(greenEffect);
        });
        toiletMale.setOnMouseClicked(e -> toiletMaleOccupied.set(!toiletMaleOccupied.get()));
        toiletFemale.setOnMouseClicked(e -> toiletFemaleOccupied.set(!toiletFemaleOccupied.get()));
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

        Updater.asyncUpdate();
    }

    public ExamHolder getExamHolder(Exam exam) {
        for (Node child : examList.getChildren()) {
            ExamHolder holder = (ExamHolder) child;
            if (holder.getExam().id.equals(exam.id)) {
                return holder;
            }
        }
        return null;
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
        addExamController.date_input.setValue(LocalDate.parse(exam.date));
        addExamController.start_time_input.setText(AddExamController.timeFormatters[0].format(LocalTime.parse(exam.start)));
        addExamController.end_time_input.setText(AddExamController.timeFormatters[0].format(LocalTime.parse(exam.end)));
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
        scene.getStylesheets().add(PreferenceController.nightMode.get() ? "/theme.dark.css" : "/theme.light.css");
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
        scene.getStylesheets().add(PreferenceController.nightMode.get() ? "/theme.dark.css" : "/theme.light.css");
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
        if (selectedExamHolder != null) selectedExamHolder.onEdit(event);
    }

    @FXML
    public void deleteExamClicked(ActionEvent event) {
        if (selectedExamHolder != null) selectedExamHolder.onDelete(event);
    }

    @FXML
    public void startSelectedExams(ActionEvent event) {
        if (selectedExamHolder != null) {
            LocalDate newDate = LocalDate.now();
            LocalTime newStartTime = LocalTime.now().withNano(0).plusSeconds(1);
            Exam exam = selectedExamHolder.getExam();
            long seconds = ChronoUnit.SECONDS.between(exam.getStartTimeObj(), exam.getEndTimeObj());
            exam.date = newDate.toString();
            exam.start = newStartTime.toString();
            exam.end = newStartTime.plusSeconds(seconds).toString();
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
            exam.date = newDate.toString();
            exam.start = newEndTime.minusSeconds(seconds).toString();
            exam.end = newEndTime.toString();
            selectedExamHolder.setExam(selectedExamHolder.getExam());
        }
    }

    @FXML
    public void startAllExams(ActionEvent event) {
        LocalDate newDate = LocalDate.now();
        LocalTime newStartTime = LocalTime.now().withNano(0).plusSeconds(1);
        exams.forEach(exam -> {
            long seconds = ChronoUnit.SECONDS.between(exam.getStartTimeObj(), exam.getEndTimeObj());
            exam.date = newDate.toString();
            exam.start = newStartTime.toString();
            exam.end = newStartTime.plusSeconds(seconds).toString();
        });
        examList.getChildren().forEach(node -> Platform.runLater(() -> ((ExamHolder) node).setExam(((ExamHolder) node).getExam())));
    }

    @FXML
    public void stopAllExams(ActionEvent event) {
        LocalDate newDate = LocalDate.now();
        LocalTime newEndTime = LocalTime.now().withNano(0);
        exams.forEach(exam -> {
            long seconds = ChronoUnit.SECONDS.between(exam.getStartTimeObj(), exam.getEndTimeObj());
            exam.date = newDate.toString();
            exam.start = newEndTime.minusSeconds(seconds).toString();
            exam.end = newEndTime.toString();
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
        preferences.put("exams", gson.toJson(exams));
    }

    @FXML
    public void load(ActionEvent event) {
        String examsStr = preferences.get("exams", null);
        if (examsStr != null) try {
            exams.addAll(gson.fromJson(examsStr, Exam[].class));
        } catch (Exception e) {
            System.out.println("Version incompatibility, skipped exam!");
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
            exams.clear();
            try {
                exams.addAll(gson.fromJson(str, Exam[].class));
            } catch (Exception e) {
                System.out.println("Version incompatibility, skipped exam!");
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
            Files.write(Paths.get(file.toURI()), gson.toJson(exams).getBytes());
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
                        setOnAction(e -> ExamClock.getInstance().getHostServices().showDocument("https://github.com/appventure-nush/exam-clock-2020"));
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
        connectionController.onClose(event);
    }

    public void play() {
        timeline.stop();
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }
}
