package sg.edu.appventure.examclock;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Setting;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import sg.edu.appventure.examclock.connection.WebServer;
import sg.edu.appventure.examclock.model.Exam;
import sg.edu.appventure.examclock.model.ExamResponse;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class PreferenceController {
    private PreferencesFx preferencesFx;
    private final MainController controller;

    public static final SimpleBooleanProperty nightMode = new SimpleBooleanProperty(true);
    public static final SimpleBooleanProperty lanEnabledProperty = new SimpleBooleanProperty(false);
    public static final SimpleIntegerProperty tcpPortProperty = new SimpleIntegerProperty(12345);
    public static final SimpleIntegerProperty udpPortProperty = new SimpleIntegerProperty(12346);
    public static final SimpleStringProperty lanNameProperty = new SimpleStringProperty("Exam Clock");

    public static final SimpleBooleanProperty allowAddingProperty = new SimpleBooleanProperty(true);
    public static final SimpleBooleanProperty allowEditProperty = new SimpleBooleanProperty(true);
    public static final SimpleBooleanProperty allowDeleteProperty = new SimpleBooleanProperty(true);
    public static final SimpleBooleanProperty allowToiletProperty = new SimpleBooleanProperty(true);

    private final Server server;
    private final WebServer webServer;

    public PreferenceController(MainController controller) {
        this.controller = controller;
        server = new Server();
        server.getKryo().register(Exam.class);
        server.getKryo().register(ExamResponse.class);

        webServer = new WebServer(8080);
    }

    public void initPreferences() {
        attachListener();
        preferencesFx = PreferencesFx.of(ExamClock.class,
                Category.of("Display",
                        com.dlsc.preferencesfx.model.Group.of("General",
                                Setting.of("Night Mode", nightMode),
                                Setting.of("Font Size", new SimpleIntegerProperty(12), 6, 36),
                                Setting.of("Scaling", new SimpleDoubleProperty(1))
                        ),
                        com.dlsc.preferencesfx.model.Group.of("Colors",
                                Setting.of("Second Hand", new SimpleObjectProperty<>(Color.RED)),
                                Setting.of("Minute Hand", new SimpleObjectProperty<>(Color.WHITE)),
                                Setting.of("Hour Hand", new SimpleObjectProperty<>(Color.WHITE)),
                                Setting.of("Clock Face", new SimpleObjectProperty<>(Color.WHITE))
                        ),
                        com.dlsc.preferencesfx.model.Group.of("Others",
                                Setting.of("Show digital clock above analogue", new SimpleBooleanProperty(false))
                        )
                ),
                Category.of("Connection",
                        com.dlsc.preferencesfx.model.Group.of(
                                Setting.of("Open to LAN", lanEnabledProperty)
                        ),
                        com.dlsc.preferencesfx.model.Group.of("Lan Display",
                                Setting.of("Name", lanNameProperty),
                                Setting.of("Port (TCP)", tcpPortProperty),
                                Setting.of("Port (UDP)", udpPortProperty),
                                Setting.of(new Label("Your controller can find this device via UDP port")),
                                Setting.of(new HBox(new Label("Control Panel IP") {{
                                    setMaxHeight(Double.POSITIVE_INFINITY);
                                    setPrefWidth(95);
                                    setAlignment(Pos.CENTER);
                                }}, new TextField(getAddress() + ":8080") {{
                                    setEditable(false);
                                }}))
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
                if (object instanceof Exam) {
                    Exam exam = (Exam) object;
                    System.out.println("Exam Received: " + exam.getName());
                    ExamResponse response = new ExamResponse("Yes I got it, the exam code was " + exam.getCode());
                    connection.sendTCP(response);
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
                    server.start();
                    webServer.start();
                    server.bind(tcpPortProperty.get(), udpPortProperty.get());
                } else {
                    server.stop();
                    webServer.stop();
                }
                System.out.println("Server is " + (newValue ? "" : "not ") + "running!");
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    }

    public void apply() {
        // TODO: Apply the properties to controls
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
