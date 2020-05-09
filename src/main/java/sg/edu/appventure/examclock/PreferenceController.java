package sg.edu.appventure.examclock;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Setting;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.google.zxing.WriterException;
import com.jfoenix.controls.JFXButton;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sg.edu.appventure.examclock.connection.Encryption;
import sg.edu.appventure.examclock.connection.WebServer;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;

public class PreferenceController {
    private final HashMap<String, byte[]> keys;
    private PreferencesFx preferencesFx;
    private final MainController controller;

    public static final SimpleIntegerProperty fontScaleProperty = new SimpleIntegerProperty(12);
    public static final SimpleBooleanProperty nightMode = new SimpleBooleanProperty(true);
    public static final SimpleBooleanProperty lanEnabledProperty = new SimpleBooleanProperty(false);
    public static final SimpleBooleanProperty controlPanelEnabledProperty = new SimpleBooleanProperty(false);
    public static final SimpleIntegerProperty tcpPortProperty = new SimpleIntegerProperty(12345);
    public static final SimpleIntegerProperty udpPortProperty = new SimpleIntegerProperty(12346);
    public static final SimpleIntegerProperty panelPortProperty = new SimpleIntegerProperty(8080);
    public static final SimpleStringProperty panelAddressProperty = new SimpleStringProperty("loading...");
    public static final SimpleStringProperty lanNameProperty = new SimpleStringProperty("Exam Clock");

    public static final SimpleBooleanProperty allowAddingProperty = new SimpleBooleanProperty(true);
    public static final SimpleBooleanProperty allowEditProperty = new SimpleBooleanProperty(true);
    public static final SimpleBooleanProperty allowDeleteProperty = new SimpleBooleanProperty(true);
    public static final SimpleBooleanProperty allowToiletProperty = new SimpleBooleanProperty(true);

    public static final SimpleObjectProperty<Color> secondHandColorProperty = new SimpleObjectProperty<>(Color.RED);
    public static final SimpleBooleanProperty analogueShadowProperty = new SimpleBooleanProperty(false);

    public static final SimpleBooleanProperty digitalAboveAnalogProperty = new SimpleBooleanProperty(false);
    public static final SimpleBooleanProperty digitalBackgroundProperty = new SimpleBooleanProperty(true);
    public static final SimpleBooleanProperty digitalClockEffectsProperty = new SimpleBooleanProperty(false);
    public static final SimpleObjectProperty<Color> digitalClockDigitColorProperty = new SimpleObjectProperty<>(Color.DODGERBLUE.brighter());
    public static final SimpleObjectProperty<Color> digitalClockDigitBorderColorProperty = new SimpleObjectProperty<>(Color.DODGERBLUE.brighter());
    public static final SimpleObjectProperty<Color> digitalClockBackgroundColorProperty = new SimpleObjectProperty<>(new Color(0, 0, 0, .5));

    private final Server server;
    private WebServer webServer;


    public PreferenceController(MainController controller) {
        this.controller = controller;
        server = new Server();
        server.getKryo().register(byte[].class);
        keys = new HashMap<>();
        webServer = new WebServer(controller, keys, controller.exams, panelPortProperty.get());
    }

    public void initPreferences() {
        attachListener();
        panelAddressProperty.set("http://" + getAddress() + ":" + panelPortProperty.get());
        panelPortProperty.addListener((observable, oldValue, newValue) -> panelAddressProperty.set("http://" + getAddress() + ":" + panelPortProperty.get()));
        preferencesFx = PreferencesFx.of(ExamClock.class,
                Category.of("Display",
                        com.dlsc.preferencesfx.model.Group.of("General",
                                Setting.of("Night Mode", nightMode),
                                Setting.of("Font Scale", fontScaleProperty, 8, 40)
                        ),
                        com.dlsc.preferencesfx.model.Group.of("Analogue Clock",
                                Setting.of("Second Hand", secondHandColorProperty),
                                Setting.of("Shadow effect", analogueShadowProperty)
                        ),
                        com.dlsc.preferencesfx.model.Group.of("Digital Clock",
                                Setting.of("Show above analogue", digitalAboveAnalogProperty),
                                Setting.of("Show background", digitalBackgroundProperty),
                                Setting.of("Shadow effect", digitalClockEffectsProperty),
                                Setting.of("Digit Fill", digitalClockDigitColorProperty),
                                Setting.of("Border Color", digitalClockDigitBorderColorProperty),
                                Setting.of("Background", digitalClockBackgroundColorProperty)
                        )
                ),
                Category.of("Connection",
                        com.dlsc.preferencesfx.model.Group.of(
                                Setting.of("Open to LAN", lanEnabledProperty),
                                Setting.of("Web Control Panel", controlPanelEnabledProperty)
                        ),
                        com.dlsc.preferencesfx.model.Group.of("Lan Display",
                                Setting.of("Name", lanNameProperty),
                                Setting.of("Port (TCP)", tcpPortProperty),
                                Setting.of("Port (UDP)", udpPortProperty),
                                Setting.of("Port (Panel)", panelPortProperty),
                                Setting.of("Panel IP", panelAddressProperty),
                                Setting.of(new JFXButton("QR Code for Key") {{
                                    getStyleClass().add("primary-raised");
                                    setOnAction(e -> {
                                        final Stage dialog = new Stage();
                                        dialog.initModality(Modality.APPLICATION_MODAL);
                                        dialog.initOwner(preferencesFx.getView().getScene().getWindow());
                                        dialog.setScene(new Scene(new StackPane(new ImageView() {{
                                            try {
                                                byte[] key = Encryption.createKey();
                                                String id = UUID.randomUUID().toString();
                                                keys.put(id, key);
                                                setImage(SwingFXUtils.toFXImage(Encryption.generateQRCode(id, key, getAddress() + ":" + panelPortProperty.get()), null));
                                                setFitHeight(512);
                                                setFitWidth(512);
                                            } catch (WriterException e) {
                                                e.printStackTrace();
                                            }
                                        }})));
                                        dialog.show();
                                    });
                                }})
                        ),
                        com.dlsc.preferencesfx.model.Group.of("Lan Permission",
                                Setting.of("Add", allowAddingProperty),
                                Setting.of("Edit", allowEditProperty),
                                Setting.of("Delete", allowDeleteProperty),
                                Setting.of("Toilet", allowToiletProperty)
                        )
                )
        );
        preferencesFx.getView().getScene().getStylesheets().addAll("/theme.css", nightMode.get() ? "/theme.dark.css" : "/theme.light.css");
        preferencesFx.buttonsVisibility(true);
    }

    public void attachListener() {
        server.addListener(new Listener() {
            public void connected(Connection connection) {
                System.out.println("Received connection " + connection.toString());
            }

            public void disconnected(Connection connection) {
                System.out.println("Disconnected " + connection.getEndPoint());
            }

            public void received(Connection connection, Object object) {
                if (object instanceof byte[]) {
                    System.out.println("Connection");
                }
            }
        });

        nightMode.addListener((observable, oldValue, newValue) -> {
            controller.stage.getScene().getStylesheets().removeAll("/theme.dark.css", "/theme.light.css");
            controller.stage.getScene().getStylesheets().add(newValue ? "/theme.dark.css" : "/theme.light.css");
            preferencesFx.getView().getScene().getStylesheets().removeAll("/theme.dark.css", "/theme.light.css");
            preferencesFx.getView().getScene().getStylesheets().addAll("/theme.css", nightMode.get() ? "/theme.dark.css" : "/theme.light.css");
        });

        lanEnabledProperty.addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue) {
                    server.bind(tcpPortProperty.get(), udpPortProperty.get());
                    server.start();
                } else server.stop();
                System.out.println("Server is " + (newValue ? "" : "not ") + "running!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        controlPanelEnabledProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) try {
                webServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            else webServer.stop();
            System.out.println("Web Server is " + (newValue ? "" : "not ") + "running!");
        });
        tcpPortProperty.addListener((observable, oldValue, newValue) -> {
            try {
                if (lanEnabledProperty.get()) server.bind(tcpPortProperty.get(), udpPortProperty.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        udpPortProperty.addListener((observable, oldValue, newValue) -> {
            try {
                if (lanEnabledProperty.get()) server.bind(tcpPortProperty.get(), udpPortProperty.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        panelPortProperty.addListener((observable, oldValue, newValue) -> {
            webServer.stop();
            webServer = new WebServer(controller, keys, controller.exams, (Integer) newValue);
            if (controlPanelEnabledProperty.get()) try {
                webServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        attachDisplayListeners();
    }

    private void attachDisplayListeners() {
        // digitalAboveAnalogProperty done else where
    }

    public void show(boolean modal) {
        preferencesFx.show(modal);
    }

    public static String getAddress() {
        try {
            for (final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
                final NetworkInterface cur = interfaces.nextElement();
                if (cur.isLoopback()) continue;
                for (final InterfaceAddress addr : cur.getInterfaceAddresses()) {
                    final InetAddress inet_addr = addr.getAddress();
                    if (!(inet_addr instanceof Inet4Address)) continue;
                    return inet_addr.getHostAddress();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onClose(WindowEvent event) {
        server.stop();
        webServer.stop();
        try {
            server.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
