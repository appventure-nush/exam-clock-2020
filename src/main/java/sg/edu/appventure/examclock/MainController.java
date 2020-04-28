package sg.edu.appventure.examclock;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import sg.edu.appventure.examclock.addexam.AddExamController;
import sg.edu.appventure.examclock.display.ClockController;
import sg.edu.appventure.examclock.model.Exam;

import java.io.IOException;

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
    private JFXNodesList examList;

    public ObservableList<Exam> exams;

    private ClockController clockController;
    private PreferenceController preferenceController;
    public Stage stage;
    private WindowButtons windowButtons;

    @FXML
    public void initialize() {
        System.out.println("initialize");
        exams = FXCollections.observableArrayList();
        clockController = new ClockController(clockPane, clockFace, hourGroup, minuteGroup, secondGroup, hourHand, minuteHand, secondHand);
        clockController.play();
        preferenceController = new PreferenceController(this);
        preferenceController.initPreferences();
        rightPane.getChildren().add(0, windowButtons = new WindowButtons());
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
            scene.getStylesheets().add("/main.css");
            scene.getStylesheets().add("/theme.css");
            scene.getStylesheets().add(PreferenceController.nightMode.get() ? "theme.dark.css" : "/theme.light.css");
            scene.getStylesheets().add(PreferenceController.nightMode.get() ? "/picker.dark.css" : "/picker.light.css");
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

    public void setStage(Stage stage) {
        this.stage = stage;
        windowButtons.setStage(stage);
    }

    public void onClose(WindowEvent event) {
        clockController.onClose(event);
        preferenceController.onClose(event);
    }

    static class WindowButtons extends ToolBar {

        private Stage stage;
        private double xOffset;
        private double yOffset;

        public WindowButtons() {
            JFXButton close = new JFXButton("", new Glyph("FontAwesome", FontAwesome.Glyph.CLOSE));
            close.setDisableVisualFocus(true);
            close.setOnMouseClicked(event -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));
            JFXButton max = new JFXButton("", new Glyph("FontAwesome", FontAwesome.Glyph.SQUARE_ALT));
            max.setDisableVisualFocus(true);
            max.setOnMouseClicked(event -> {
                stage.setFullScreen(true);
                stage.setMaximized(true);
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

        public void setStage(Stage stage) {
            this.stage = stage;
        }
    }
}
