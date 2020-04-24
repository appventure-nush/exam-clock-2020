package sg.edu.appventure.examclock;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class MainController {
    @FXML
    private Group clockPane;
    @FXML
    private Circle clockFace;
    @FXML
    private Circle clockFace1;
    @FXML
    private Circle clockFace2;
    @FXML
    private Circle clockFace3;
    @FXML
    private Group hourGroup;
    @FXML
    private Line hourHand;
    @FXML
    private Group minuteGroup;
    @FXML
    private Line minuteHand;
    @FXML
    private Group secondGroup;
    @FXML
    private Line secondHand;
    @FXML
    private Button addBtn;
    @FXML
    private Button startBtn;
    @FXML
    private Button stopBtn;
    @FXML
    private Button settingBtn;

    @FXML
    public void initialize() {
        System.out.println("initialize");
        ClockController clockController = new ClockController(clockPane, hourGroup, minuteGroup, secondGroup);
        clockController.start();
    }
}
