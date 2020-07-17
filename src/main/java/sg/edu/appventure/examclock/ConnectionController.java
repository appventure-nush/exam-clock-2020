package sg.edu.appventure.examclock;

import com.google.zxing.WriterException;
import javafx.animation.TranslateTransition;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import sg.edu.appventure.examclock.connection.Base64;
import sg.edu.appventure.examclock.connection.ClientSocket;
import sg.edu.appventure.examclock.connection.Encryption;
import sg.edu.appventure.examclock.model.Key;

public class ConnectionController {
    @FXML
    private ToggleGroup keyTypeGroup;
    @FXML
    private RadioButton typeAdmin;
    @FXML
    private RadioButton typeToilet;
    @FXML
    private RadioButton typeRead;
    @FXML
    private Button createKey;
    @FXML
    private ListView<Key> keys;
    @FXML
    private VBox infoPane;
    @FXML
    private Label keyID;
    @FXML
    private Label keyType;
    @FXML
    private TextArea keyRaw;
    @FXML
    private PasswordField masterPassword;
    @FXML
    private TextField addressDisplay;
    @FXML
    private Label clockIDLabel;
    private MainController mainController;
    private Stage qrCodeStage;
    private ImageView qrCodeImage;

    private String selectedID;
    private boolean shown = false;

    private ClientSocket socket;

    @FXML
    public void initialize() {
        infoPane.setTranslateX(-infoPane.getWidth());
        infoPane.translateXProperty().bind(infoPane.widthProperty());
        keys.setCellFactory(param -> {
            Label uuid = new Label();
            uuid.setFont(Font.font("monospace"));
            Label type = new Label();
            type.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(type, Priority.ALWAYS);
            Button open = new Button("Details");
            open.setUnderline(true);
            HBox hbox = new HBox(uuid, type, open);
            hbox.setAlignment(Pos.CENTER);
            hbox.setSpacing(10);
            ListCell<Key> cell = new ListCell<>();
            cell.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    uuid.setText(newItem.id);
                    type.setText(newItem.type.toString());
                    open.setOnAction(e -> showInfo(newItem));
                }
            });
            cell.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                if (isEmpty) cell.setGraphic(null);
                else cell.setGraphic(hbox);
            });
            cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            return cell;
        });
        qrCodeStage = new Stage();
        qrCodeStage.initModality(Modality.APPLICATION_MODAL);
        qrCodeStage.setScene(new Scene(new StackPane(qrCodeImage = new ImageView() {{
            setFitHeight(512);
            setFitWidth(512);
        }})));
        addressDisplay.setText("http://" + PreferenceController.getAddress() + ":" + PreferenceController.panelPortProperty.get());
        addressDisplay.focusedProperty().addListener((observable, oldValue, newValue) -> addressDisplay.setText("http://" + PreferenceController.getAddress() + ":" + PreferenceController.panelPortProperty.get()));
        clockIDLabel.setText(PreferenceController.clockID);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        mainController.connectionController = this;
        socket = new ClientSocket(mainController);
        keys.setItems(mainController.keys);
        masterPassword.setText(mainController.preferences.get("password", "password"));
        mainController.simpleKey.key = Encryption.createKeyFromPassword(mainController.preferences.get("password", "password"));
    }

    public void showInfo(Key key) {
        if (shown) return;
        shown = true;
        try {
            qrCodeImage.setImage(SwingFXUtils.toFXImage(Encryption.generateQRCode(key, PreferenceController.getAddress() + ":" + PreferenceController.panelPortProperty.get()), null));
        } catch (WriterException e) {
            e.printStackTrace();
        }
        selectedID = key.id;
        keyID.setText(key.id);
        keyType.setText(key.type.toString());
        keyRaw.setText(new String(Base64.encode(key.key)));
        infoPane.translateXProperty().unbind();
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500), infoPane);
        translateTransition.setFromX(infoPane.getWidth());
        translateTransition.setToX(0);
        translateTransition.playFromStart();
    }

    public void create(ActionEvent actionEvent) {
        Key key = new Key(typeAdmin.isSelected() ? Key.KeyType.ADMIN : typeToilet.isSelected() ? Key.KeyType.TOILET : Key.KeyType.READ_ONLY);
        mainController.keys.add(key);
    }

    public void delete(ActionEvent actionEvent) {
        mainController.keys.removeIf(key -> key.id.equals(selectedID));
        cancel(null);
    }

    public void cancel(ActionEvent actionEvent) {
        if (!shown) return;
        shown = false;
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500), infoPane);
        translateTransition.setFromX(0);
        translateTransition.setToX(infoPane.getWidth());
        translateTransition.setOnFinished(e -> infoPane.translateXProperty().bind(infoPane.widthProperty().multiply(1)));
        translateTransition.playFromStart();
    }

    public void changePassword(ActionEvent actionEvent) {
        String password = masterPassword.getText();
        mainController.preferences.put("password", password);
        mainController.simpleKey.key = Encryption.createKeyFromPassword(password);
    }

    public void showQRCode(ActionEvent actionEvent) {
        qrCodeStage.show();
    }

    public void onClose(WindowEvent event) {
        socket.close();
    }

    public void connect(ActionEvent actionEvent) {
        socket.getSocket().close();
        socket.getSocket().open();
    }

    public void resend(ActionEvent actionEvent) {
        socket.resend();
    }
}
