package sg.edu.appventure.examclock;

import com.google.zxing.WriterException;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXRadioButton;
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
import javafx.util.Duration;
import sg.edu.appventure.examclock.connection.Base64;
import sg.edu.appventure.examclock.connection.Encryption;
import sg.edu.appventure.examclock.model.Key;

public class ConnectionController {
    @FXML
    private ToggleGroup keyTypeGroup;
    @FXML
    private JFXRadioButton typeAdmin;
    @FXML
    private JFXRadioButton typeToilet;
    @FXML
    private JFXRadioButton typeRead;
    @FXML
    private JFXButton createKey;
    @FXML
    private JFXListView<Key> keys;
    @FXML
    private VBox infoPane;
    @FXML
    private Label keyID;
    @FXML
    private Label keyType;
    @FXML
    private TextArea keyRaw;
    private MainController mainController;
    private Stage qrCodeStage;
    private ImageView qrCodeImage;

    private String selectedID;
    private boolean shown = false;

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
            JFXButton open = new JFXButton("Details");
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
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        keys.setItems(mainController.keys);
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

    public void showQRCode(ActionEvent actionEvent) {
        qrCodeStage.show();
    }
}
