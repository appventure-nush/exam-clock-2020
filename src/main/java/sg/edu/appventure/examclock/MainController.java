package sg.edu.appventure.examclock;

import com.jfoenix.controls.JFXNodesList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sg.edu.appventure.examclock.addexam.AddExamController;
import sg.edu.appventure.examclock.display.ClockController;
import sg.edu.appventure.examclock.model.Exam;

import java.io.IOException;
import java.util.ArrayList;

public class MainController {
    @FXML
    private BorderPane root;
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
    private Button addBtn;
    @FXML
    private Button startBtn;
    @FXML
    private Button stopBtn;
    @FXML
    private Button settingBtn;
    @FXML
    private JFXNodesList examList;

    private ArrayList<Exam> exams;

    private ClockController clockController;
    private PreferenceController preferenceController;

    @FXML
    public void initialize() {
        System.out.println("initialize");
        exams = new ArrayList<>();
        clockController = new ClockController(clockPane, clockFace, hourGroup, minuteGroup, secondGroup);
        clockController.play();
        preferenceController = new PreferenceController(this);
        preferenceController.initPreferences();
    }

    public void resize(double width, double height) {
        clockController.resize(width, height);
    }

    @FXML
    public void add(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml_add_exam.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            ((AddExamController) fxmlLoader.getController()).setMainController(this);
            Stage stage = new Stage();
            scene.getStylesheets().add("/main.dark.css");
            scene.getStylesheets().add("/picker.dark.css");
            stage.setTitle("Exam Clock");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.show();
            stage.setMinWidth(stage.getWidth());
            stage.setMinHeight(stage.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCallback(Exam exam) {

    }

    @FXML
    public void showSettings(ActionEvent event) {
        preferenceController.show(true);
    }
}
