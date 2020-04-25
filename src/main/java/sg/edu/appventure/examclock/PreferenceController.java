package sg.edu.appventure.examclock;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

public class PreferenceController {
    private final BooleanProperty nightMode;
    private PreferencesFx preferencesFx;
    private final MainController controller;

    public PreferenceController(MainController controller) {
        this.controller = controller;
        nightMode = new SimpleBooleanProperty(true);
    }

    public void initPreferences() {
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
                                Setting.of("Open to LAN", new SimpleBooleanProperty(true))
                        ),
                        com.dlsc.preferencesfx.model.Group.of("Lan Display",
                                Setting.of("Name", new SimpleStringProperty("Exam Clock")),
                                Setting.of("Port", new SimpleIntegerProperty(12345))
                        ),
                        com.dlsc.preferencesfx.model.Group.of("Lan Permission",
                                Setting.of("Add", new SimpleBooleanProperty(true)),
                                Setting.of("Edit", new SimpleBooleanProperty(true)),
                                Setting.of("Delete", new SimpleBooleanProperty(true)),
                                Setting.of("Toilet", new SimpleBooleanProperty(true))
                        )
                )
        );
        preferencesFx.saveSettings();
        preferencesFx.getView().addStylesheetFiles("/main.dark.css");
    }

    public void apply() {
        // TODO: Apply the properties to controls
    }

    public void show(boolean modal) {
        preferencesFx.show(modal);
    }
}
