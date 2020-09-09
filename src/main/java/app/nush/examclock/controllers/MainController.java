package app.nush.examclock.controllers;

import app.nush.examclock.ExamClock;
import app.nush.examclock.Version;
import app.nush.examclock.display.ExamHolder;
import app.nush.examclock.model.Exam;
import app.nush.examclock.updater.Updater;
import com.google.gson.Gson;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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

/**
 * The type Main controller.
 */
public class MainController {
    public static final Gson gson = new Gson();
    private static final ColorAdjust redEffect = new ColorAdjust(0, 1, 0.5, 0);
    private static final ColorAdjust greenEffect = new ColorAdjust(0.5, 1, 0.5, 0);
    /**
     * Exams, observable so changes are reflected across entire program
     */
    public ObservableList<Exam> exams;
    /**
     * if female toilet is occupied.
     */
    public SimpleBooleanProperty toiletFemaleOccupied = new SimpleBooleanProperty(false);
    /**
     * if male toilet is occupied.
     */
    public SimpleBooleanProperty toiletMaleOccupied = new SimpleBooleanProperty(false);
    /**
     * The Add exam stage.
     */
    public Stage addExamStage;
    @FXML
    private SplitPane root;
    @FXML
    private StackPane clockRoot;
    @FXML
    private Group clockPane;
    @FXML
    private Group clockFace;
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
    /**
     * The Selected exam holder.
     */
    public ExamHolder selectedExamHolder;
    private PreferenceController preferenceController;
    /**
     * The Preferences.
     */
    public Preferences preferences;

    private Stage connectStage;
    /**
     * Link to instances of other controllers
     */
    private ClockController clockController;
    private Timeline timeline;
    private AddExamController addExamController;
    private FileChooser fileChooser;
    private ConnectionController connectionController;
    private Stack<ExamHolder> examHolderPool;

    /**
     * Instantiates a new Main controller.
     */
    public MainController() {
    }

    private static String generateClockID() {
        char[] set = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ0123456789".toCharArray(); // stripped of  I l and O
        char[] res = new char[7];
        for (int i = 0; i < 7; i++) res[i] = set[(int) (set.length * Math.random())];
        return new String(res);
    }

    /**
     * Regen clock id.
     */
    public void regenClockID() {
        PreferenceController.clockID = generateClockID();
    }

    /**
     * Initialize.
     */
    @FXML
    public void initialize() throws IOException {
        System.out.println("initialize");
        exams = FXCollections.observableArrayList();
        exams.addListener((ListChangeListener<Exam>) c -> {
            while (c.next()) {
                List<? extends Exam> removed = c.getRemoved();
                examList.getChildren().removeIf(node -> {
                    ExamHolder examHolder = (ExamHolder) node;
                    boolean contains = removed.contains(examHolder.getExam());
                    if (contains) examHolderPool.push(examHolder.reset());
                    return contains;
                });
                for (int i = c.getFrom(); i < c.getTo(); i++)
                    examList.getChildren().add(i, examHolderPool.empty() ? new ExamHolder(this, c.getList().get(i)) : examHolderPool.pop().setExam(c.getList().get(i)));
            }
            if (ExamHolder.autoSaveProperty.get()) saveExams(null);
        });
        examHolderPool = new Stack<>();
        preferences = Preferences.userNodeForPackage(MainController.class);
        preferences.put("clockID", PreferenceController.clockID = preferences.get("clockID", generateClockID())); // set if unset

        clockController = new ClockController(clockPane, clockFace, hourGroup, minuteGroup, secondGroup, hourHand, minuteHand, secondHand);
        preferenceController = new PreferenceController(this);

        root.styleProperty().bind(Bindings.concat("-fx-font-size: ", PreferenceController.fontScaleProperty, "px;"));
        root.orientationProperty().bind(ExamHolder.displayOrientationProperty);
        clockRoot.widthProperty().addListener((observable, oldValue, newValue) -> clockController.resize(clockRoot.getWidth(), clockRoot.getHeight()));
        clockRoot.heightProperty().addListener((observable, oldValue, newValue) -> clockController.resize(clockRoot.getWidth(), clockRoot.getHeight()));
        ExamHolder.showExamsProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) root.getItems().add(1, examList);
            else root.getItems().remove(1);
        });

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
        toiletMale.fitHeightProperty().bind(PreferenceController.toiletScaleProperty.multiply(150));
        toiletFemale.fitHeightProperty().bind(PreferenceController.toiletScaleProperty.multiply(150));

        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));

        initAddExamStage();
        initConnectionStage();
        PreferenceController.nightMode.addListener((observable, oldValue, newValue) -> {
            addExamStage.getScene().getStylesheets().removeAll("/theme.dark.css", "/theme.light.css");
            addExamStage.getScene().getStylesheets().add(newValue ? "/theme.dark.css" : "/theme.light.css");
            connectStage.getScene().getStylesheets().removeAll("/theme.dark.css", "/theme.light.css");
            connectStage.getScene().getStylesheets().add(newValue ? "/theme.dark.css" : "/theme.light.css");
        });

        preferenceController.initPreferences(); // load preferences after adding listeners
        loadExams(null); // load exams from disk
        Updater.asyncUpdate(); // check updates

        // Start main render loop
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(16), e -> refresh()));
        play();
    }

    /**
     * Gets exam holder by exam
     *
     * @param exam the exam
     * @return the exam holder
     */
    public ExamHolder getExamHolder(Exam exam) {
        for (Node child : examList.getChildren()) {
            ExamHolder holder = (ExamHolder) child;
            if (holder.getExam().id.equals(exam.id)) {
                return holder;
            }
        }
        return null;
    }

    /**
     * Refresh display
     */
    public void refresh() {
        clockController.refresh();
        if (!ExamHolder.showExamsProperty.get()) return;
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        for (Node node : examList.getChildren()) {
            ExamHolder examHolder = (ExamHolder) node;
            examHolder.update(today, now);
        }
    }

    /**
     * Show add exam stage.
     *
     * @param exam the exam
     */
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

    public void addExam(Exam exam) {
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
        if (alert.getResult() == ButtonType.YES) {
            exams.clear();
            saveExams(null);
        }
    }

    @FXML
    public void saveExams(ActionEvent event) {
        preferences.put("exams", gson.toJson(exams));
    }

    @FXML
    public void loadExams(ActionEvent event) {
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
    public void importExams(ActionEvent event) {
        File file = fileChooser.showOpenDialog(ExamClock.getStage());
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
    public void exportExams(ActionEvent event) {
        File file = fileChooser.showSaveDialog(ExamClock.getStage());
        if (file == null) return;
        try {
            Files.write(Paths.get(file.toURI()), gson.toJson(exams).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void about(ActionEvent event) {
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

    @FXML
    public void help(ActionEvent event) {
        ExamClock.getInstance().getHostServices().showDocument("https://github.com/appventure-nush/exam-clock-2020/blob/master/README.md");
    }

    public void onClose(WindowEvent event) {
        stop();
        connectionController.onClose(event);
    }

    /**
     * Play clock animation, updates etc
     */
    public void play() {
        timeline.stop();
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }

    public void setConnectionController(ConnectionController connectionController) {
        this.connectionController = connectionController;
    }
}
