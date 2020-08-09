package app.nush.examclock.controllers;

import app.nush.examclock.connection.ClientSocket;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.WindowEvent;

/**
 * The type Connection controller.
 */
public class ConnectionController {
    @FXML
    private Label clockIDLabel;
    private MainController mainController;

    private ClientSocket socket;

    /**
     * Initialize.
     */
    @FXML
    public void initialize() {
        clockIDLabel.setText(PreferenceController.clockID);
    }

    /**
     * On close.
     *
     * @param event the event
     */
    @FXML
    public void onClose(WindowEvent event) {
        socket.getSocket().close();
    }

    /**
     * Reconnects to server
     *
     * @param actionEvent the action event
     */
    @FXML
    public void reconnect(ActionEvent actionEvent) {
        socket.getSocket().close();
        socket.getSocket().open();
    }

    /**
     * Update server side exam cache
     *
     * @param actionEvent the action event
     */
    @FXML
    public void forceExamUpdate(ActionEvent actionEvent) {
        socket.forceExamUpdate();
    }

    /**
     * Sets main controller.
     *
     * @param mainController the main controller
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        mainController.setConnectionController(this);
        socket = new ClientSocket(mainController);
    }
}
