package sg.edu.appventure.examclock;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sg.edu.appventure.examclock.display.ResizeHelper;

import java.io.IOException;
import java.util.prefs.Preferences;

public class ExamClock extends Application {
    public static Preferences preferences;
    private MainController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        preferences = Preferences.userNodeForPackage(ExamClock.class);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_main.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setStage(primaryStage);
        Scene scene = new Scene(root);
        scene.widthProperty().addListener((observable, oldValue, newValue) -> controller.resize((Double) newValue, scene.getHeight()));
        scene.heightProperty().addListener((observable, oldValue, newValue) -> controller.resize(scene.getWidth(), (Double) newValue));
        scene.getStylesheets().add("/main.css");
        scene.getStylesheets().add("/theme.css");
        scene.getStylesheets().add(PreferenceController.nightMode.get() ? "theme.dark.css" : "/theme.light.css");
        primaryStage.setTitle("Exam Clock");
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Closing Down...");
            controller.onClose(event);
            System.exit(0);
        });
        ResizeHelper.addResizeListener(primaryStage);
        primaryStage.show();
    }
}
