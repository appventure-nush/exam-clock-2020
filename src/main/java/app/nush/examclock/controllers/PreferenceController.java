package app.nush.examclock.controllers;

import app.nush.examclock.ExamClock;
import app.nush.examclock.display.DigitalClock;
import app.nush.examclock.display.ExamHolder;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * The type Preference controller.
 */
public class PreferenceController {

    /**
     * font scale of the app, some parts are not affected
     */
    public static final SimpleIntegerProperty fontScaleProperty = new SimpleIntegerProperty(20);
    /**
     * scale of the toilet buttons, scale 1.0 is 150px in height
     */
    public static final SimpleDoubleProperty toiletScaleProperty = new SimpleDoubleProperty(1);
    /**
     * if toilet buttons are even shown
     */
    public static final SimpleBooleanProperty showToiletProperty = new SimpleBooleanProperty(true);
    /**
     * global night mode
     */
    public static final SimpleBooleanProperty nightMode = new SimpleBooleanProperty(true);

    /**
     * if this is set to false, all incoming requests to take control will be answered automatically with a "no"
     */
    public static final SimpleBooleanProperty openToRequestsProperty = new SimpleBooleanProperty(true);
    /**
     * the name of this clock, visible to controllers
     */
    public static final SimpleStringProperty nameProperty = new SimpleStringProperty("Exam Clock");

    /**
     * color of the second hand (big red arrow of death)
     */
    public static final SimpleObjectProperty<Color> secondHandColorProperty = new SimpleObjectProperty<>(Color.RED);
    /**
     * if shadows are enabled for the analogue clock, looks cool ngl, but may be unnecessary or laggy
     */
    public static final SimpleBooleanProperty analogueShadowProperty = new SimpleBooleanProperty(false);
    /**
     * 12 hour format used instead of the default 24 hours format, should be global
     */
    public static final SimpleBooleanProperty use12HourFormatProperty = new SimpleBooleanProperty(false);
    /**
     * whether digital clock should be above the analogue clock
     * less realistic but more visible
     */
    public static final SimpleBooleanProperty digitalAboveAnalogProperty = new SimpleBooleanProperty(false);
    /**
     * The clockID.
     */
    public static String clockID;
    private final MainController controller;
    private PreferencesFx preferencesFx;

    /**
     * Instantiates a new Preference controller.
     *
     * @param controller the controller
     */
    public PreferenceController(MainController controller) {
        this.controller = controller;
    }

    /**
     * Init preferences.
     */
    public void initPreferences() {
        attachListeners();
        preferencesFx = PreferencesFx.of(ExamClock.class,
                Category.of("Display",
                        Group.of("General",
                                Setting.of("Night Mode", nightMode),
                                Setting.of("Font Size", fontScaleProperty, 8, 40),
                                Setting.of("Use 12 Hour Format", use12HourFormatProperty),
                                Setting.of("Show Toilet", showToiletProperty),
                                Setting.of("Toilet Scale", toiletScaleProperty, 0.1, 3, 2)
                        )
                ).expand().subCategories(
                        Category.of("Exams",
                                Group.of(
                                        Setting.of("Show Exams", ExamHolder.showExamsProperty),
                                        Setting.of("Orientation", ExamHolder.displayOrientationList, ExamHolder.displayOrientationProperty)
                                ), Group.of("Exam Holder",
                                        Setting.of("Progress Feather", ExamHolder.gradientFeatherProperty, 0, 5, 1),
                                        Setting.of("Countdown", ExamHolder.showCountDownForExamProperty),
                                        Setting.of("Simplified Countdown", ExamHolder.useSimplifiedCountdownForExamProperty)
                                )
                        ),
                        Category.of("Analogue Clock", Group.of(
                                Setting.of("Interpolation", ClockController.speed),
                                Setting.of("Second Hand", secondHandColorProperty),
                                Setting.of("Shadows", analogueShadowProperty)
                        )),
                        Category.of("Digital Clock",
                                Group.of(
                                        Setting.of("Above Analogue", digitalAboveAnalogProperty),
                                        Setting.of("Show Background", DigitalClock.digitalBackgroundProperty),
                                        Setting.of("Background", DigitalClock.digitalClockBackgroundColorProperty),
                                        Setting.of("Shadows", DigitalClock.digitalClockShadowEffectsProperty)
                                ),
                                Group.of("Digits",
                                        Setting.of("Digit Color", DigitalClock.digitalClockDigitColorProperty),
                                        Setting.of("Border Color", DigitalClock.digitalClockDigitBorderColorProperty),
                                        Setting.of("Border Width", DigitalClock.digitalClockDigitBorderWidthProperty, 0, 2, 2)
                                )
                        )
                ),
                Category.of("Web Panel",
                        Group.of(
                                Setting.of("Display Name", nameProperty)
                        ),
                        Group.of("Central Server",
                                Setting.of("Open to requests", openToRequestsProperty)
                        )
                )
        );
        preferencesFx.getView().getScene().getStylesheets().addAll("/theme.css", nightMode.get() ? "/theme.dark.css" : "/theme.light.css");
        preferencesFx.getView().getStyleClass().add("preference");
        Stage window = (Stage) preferencesFx.getView().getScene().getWindow();
        window.setMaxHeight(500);
        window.setWidth(600);
        window.setHeight(500);
        preferencesFx.persistWindowState(true);
    }

    /**
     * Attach listeners
     */
    public void attachListeners() {
        nightMode.addListener((observable, oldValue, newValue) -> {
            ExamClock.getStage().getScene().getStylesheets().removeAll("/theme.dark.css", "/theme.light.css");
            ExamClock.getStage().getScene().getStylesheets().add(newValue ? "/theme.dark.css" : "/theme.light.css");
            preferencesFx.getView().getScene().getStylesheets().removeAll("/theme.dark.css", "/theme.light.css");
            preferencesFx.getView().getScene().getStylesheets().addAll("/theme.css", nightMode.get() ? "/theme.dark.css" : "/theme.light.css");
        });
    }

    /**
     * Show.
     *
     * @param modal the modal
     */
    public void show(boolean modal) {
        preferencesFx.show(modal);
    }

}
