package sg.edu.appventure.examclock;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import sg.edu.appventure.examclock.connection.WebServer;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class PreferenceController {
    public static final SimpleIntegerProperty fontScaleProperty = new SimpleIntegerProperty(20);
    public static final SimpleBooleanProperty nightMode = new SimpleBooleanProperty(true);
    public static final SimpleBooleanProperty showToiletProperty = new SimpleBooleanProperty(true);
    public static final SimpleBooleanProperty controlPanelEnabledProperty = new SimpleBooleanProperty(true);
    public static final SimpleIntegerProperty panelPortProperty = new SimpleIntegerProperty(8080);
    public static final SimpleStringProperty panelAddressProperty = new SimpleStringProperty("loading...");
    public static final SimpleStringProperty lanNameProperty = new SimpleStringProperty("Exam Clock");
    public static final SimpleObjectProperty<Color> secondHandColorProperty = new SimpleObjectProperty<>(Color.RED);
    public static final SimpleBooleanProperty analogueShadowProperty = new SimpleBooleanProperty(false);
    public static final SimpleBooleanProperty use12HourFormatProperty = new SimpleBooleanProperty(false);
    public static final SimpleBooleanProperty digitalAboveAnalogProperty = new SimpleBooleanProperty(false);
    public static final SimpleBooleanProperty digitalBackgroundProperty = new SimpleBooleanProperty(true);
    public static final SimpleBooleanProperty digitalClockEffectsProperty = new SimpleBooleanProperty(false);
    public static final SimpleObjectProperty<Color> digitalClockDigitColorProperty = new SimpleObjectProperty<>(Color.DODGERBLUE.brighter());
    public static final SimpleObjectProperty<Color> digitalClockDigitBorderColorProperty = new SimpleObjectProperty<>(Color.DODGERBLUE.brighter());
    public static final SimpleObjectProperty<Color> digitalClockBackgroundColorProperty = new SimpleObjectProperty<>(new Color(0, 0, 0, .5));
    private final MainController controller;
    private PreferencesFx preferencesFx;
    private WebServer webServer;

    public PreferenceController(MainController controller) {
        this.controller = controller;
        webServer = new WebServer(controller, controller.exams, panelPortProperty.get());
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

    public static boolean available(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException ignored) {
        } finally {
            if (ds != null) ds.close();
            if (ss != null) try {
                ss.close();
            } catch (IOException ignored) {
            }
        }
        return false;
    }

    public void initPreferences() {
        attachListener();
        panelAddressProperty.set("http://" + getAddress() + ":" + panelPortProperty.get());
        panelPortProperty.addListener((observable, oldValue, newValue) -> panelAddressProperty.set("http://" + getAddress() + ":" + panelPortProperty.get()));
        preferencesFx = PreferencesFx.of(ExamClock.class,
                Category.of("Display",
                        com.dlsc.preferencesfx.model.Group.of("General",
                                Setting.of("Night Mode", nightMode),
                                Setting.of("Font Size", fontScaleProperty, 8, 40),
                                Setting.of("Use 12 Hour Format", use12HourFormatProperty),
                                Setting.of("Show Toilet", showToiletProperty)
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
                Category.of("Web Panel",
                        com.dlsc.preferencesfx.model.Group.of(
                                Setting.of("Web Control Panel", controlPanelEnabledProperty)
                        ),
                        com.dlsc.preferencesfx.model.Group.of("Lan",
                                Setting.of("Display Name", lanNameProperty),
                                Setting.of("Port", panelPortProperty),
                                Setting.of("Panel IP", panelAddressProperty)
                        )
                )
        );
        preferencesFx.getView().setPrefSize(600, 400);
        preferencesFx.getView().getScene().getStylesheets().addAll("/theme.css", nightMode.get() ? "/theme.dark.css" : "/theme.light.css");
        preferencesFx.buttonsVisibility(true);
    }

    public void attachListener() {
        try {
            webServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        nightMode.addListener((observable, oldValue, newValue) -> {
            controller.stage.getScene().getStylesheets().removeAll("/theme.dark.css", "/theme.light.css");
            controller.stage.getScene().getStylesheets().add(newValue ? "/theme.dark.css" : "/theme.light.css");
            preferencesFx.getView().getScene().getStylesheets().removeAll("/theme.dark.css", "/theme.light.css");
            preferencesFx.getView().getScene().getStylesheets().addAll("/theme.css", nightMode.get() ? "/theme.dark.css" : "/theme.light.css");
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
        panelPortProperty.addListener((observable, oldValue, newValue) -> {
            webServer.stop();
            webServer = new WebServer(controller, controller.exams, (Integer) newValue);
            if (controlPanelEnabledProperty.get()) try {
                webServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void show(boolean modal) {
        preferencesFx.show(modal);
    }

    public void onClose(WindowEvent event) {
        webServer.stop();
    }
}
