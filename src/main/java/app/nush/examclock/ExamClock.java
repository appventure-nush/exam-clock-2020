package app.nush.examclock;

import app.nush.examclock.connection.ClientSocket;
import app.nush.examclock.controllers.MainController;
import app.nush.examclock.controllers.PreferenceController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Exam Clock, a clock meant to be used in a exam
 * <p>
 * <sub>Made with love and care</sub>
 *
 * @author Zhao Yun
 * @see <a href="https://www.nushigh.edu.sg/">NUS High School</a>
 * @see <a href="https://nush.app/">Appventure Website</a>
 * @see <a href="https://github.com/appventure-nush/exam-clock-2020">Github Repo</a>
 */
public class ExamClock extends Application {
    public static Preferences preferences;
    private static ExamClock instance;
    private MainController controller;

    public static void main(String[] args) {
        launch(args);
    }

    public static ExamClock getInstance() {
        return instance;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
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
            primaryStage.titleProperty().bind(Bindings.concat("Exam Clock " + Version.getVersion() + " : ", ClientSocket.connectivityStateProperty));

            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("Closing Down...");
                controller.onClose(event);
                System.exit(0);
            });
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
                alert.setTitle("Error");
                alert.setHeaderText("Exam clock is unable to start!");
                alert.showAndWait();
            });
        }
    }
}
