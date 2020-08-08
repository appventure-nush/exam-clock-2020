package app.nush.examclock;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.prefs.Preferences;

public class ExamClock extends Application {
    public static Preferences preferences;
    private MainController controller;
    private static ExamClock instance;

    public static void main(String[] args) {
        launch(args);
    }

    public static ExamClock getInstance() {
        return instance;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        instance = this;
        preferences = Preferences.userNodeForPackage(ExamClock.class);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_main.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setStage(primaryStage);
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/main.css");
        scene.getStylesheets().add("/theme.css");
        scene.getStylesheets().add(PreferenceController.nightMode.get() ? "/theme.dark.css" : "/theme.light.css");
        primaryStage.titleProperty().bind(Bindings.concat("Exam Clock " + Version.getVersion() + " : ", PreferenceController.connectivityStateProperty));

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Closing Down...");
            controller.onClose(event);
            System.exit(0);
        });
        primaryStage.show();
    }
}
