package sg.edu.appventure.examclock;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

public class MainController {
    @FXML
    private BorderPane root;
    @FXML
    private Pane clockRoot;
    @FXML
    private Group clockPane;
    @FXML
    private Circle clockFace;
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

    private ClockController clockController;

    @FXML
    public void initialize() {
        System.out.println("initialize");
        clockController = new ClockController(clockPane, hourGroup, minuteGroup, secondGroup);
        clockController.play();
    }

    public void resize(double width, double height) {
        clockController.resize(width, height);
    }
}
