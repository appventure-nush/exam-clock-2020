package sg.edu.appventure.examclock;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ExamClock extends Application {

    private MainController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_main.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        Scene scene = new Scene(root);
        scene.widthProperty().addListener((observable, oldValue, newValue) -> controller.resize((Double) newValue, scene.getHeight()));
        scene.heightProperty().addListener((observable, oldValue, newValue) -> controller.resize(scene.getWidth(), (Double) newValue));
        scene.getStylesheets().add("/main.dark.css");
        primaryStage.setTitle("Exam Clock");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> System.exit(0));
        primaryStage.show();
    }
}
