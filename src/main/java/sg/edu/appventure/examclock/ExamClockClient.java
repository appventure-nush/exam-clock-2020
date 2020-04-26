package sg.edu.appventure.examclock;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ExamClockClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml_client.fxml")));
        scene.getStylesheets().addAll("/main.css", "/theme.css", "/theme.dark.css");
        primaryStage.setTitle("Exam Clock Controller");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> System.exit(0));
        primaryStage.show();
    }
}
