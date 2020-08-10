package app.nush.examclock.display;

import app.nush.examclock.model.CustomBinding;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.control.TextField;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static app.nush.examclock.controllers.AddExamController.parseTime;

public class TimePicker extends TextField {
    public static final DateTimeFormatter defaultFormatter = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("hh:mm a")
            .toFormatter();

    private final TimePopup popup;
    public SimpleObjectProperty<LocalTime> timeProperty;
    public SimpleIntegerProperty hour;
    public SimpleIntegerProperty minute;

    public TimePicker() {
        timeProperty = new SimpleObjectProperty<>();
        hour = new SimpleIntegerProperty(0);
        minute = new SimpleIntegerProperty(0);

        popup = new TimePopup(this);

        CustomBinding.bindBidirectional(timeProperty, hour, LocalTime::getHour, hour -> LocalTime.of(hour.intValue(), minute.get()));
        CustomBinding.bindBidirectional(timeProperty, minute, LocalTime::getMinute, minute -> LocalTime.of(hour.get(), minute.intValue()));
        CustomBinding.bindBidirectional(timeProperty, textProperty(), localTime -> localTime.format(defaultFormatter), text -> {
            try {
                return parseTime(text, 0);
            } catch (Exception ignored) {
                return timeProperty.get();
            }
        });

        setPromptText("00:00 am");
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue && newValue) { //gain focus
                Bounds bounds = localToScreen(getBoundsInLocal());
                popup.show(TimePicker.this, bounds.getMinX(), bounds.getMaxY());
            }
        });
    }
}
