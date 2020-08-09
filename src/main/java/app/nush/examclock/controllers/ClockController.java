package app.nush.examclock.controllers;

import app.nush.examclock.display.DigitalClock;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Calendar;

/**
 * The Clock controller.
 */
public class ClockController {
    /**
     * Some parameters for the interpolation function
     */
    public static final SimpleDoubleProperty speed = new SimpleDoubleProperty(20);
    private static final SimpleDoubleProperty min = new SimpleDoubleProperty(Math.pow(2, -speed.get()));
    private static final SimpleDoubleProperty scale = new SimpleDoubleProperty(1 / (1 - min.get()));

    static {
        scale.bind(Bindings.divide(1, Bindings.subtract(1, min)));
        min.bind(Bindings.createDoubleBinding(() -> Math.pow(2, -speed.get()), speed));
    }

    private final Group parent;
    private final Group clockFace;
    private final Group hour;
    private final Group minute;
    private final Group second;
    private final Calendar calendar;
    private final DigitalClock digitalClock;
    /**
     * Second displayed in the last frame, used to reduce unneeded refresh attempts
     */
    private int lastFrameSeconds = 0;

    /**
     * Instantiates a new Clock controller.
     *
     * @param parent     the parent
     * @param clockFace  the clock face
     * @param hour       the hour
     * @param minute     the minute
     * @param second     the second
     * @param hourHand   the hour hand
     * @param minuteHand the minute hand
     * @param secondHand the second hand
     */
    public ClockController(Group parent, Group clockFace, Group hour, Group minute, Group second, Polygon hourHand, Polygon minuteHand, Polygon secondHand) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.parent = parent;
        this.clockFace = clockFace;
        calendar = Calendar.getInstance();
        digitalClock = new DigitalClock();
        digitalClock.setLayoutX(-digitalClock.width / 2);
        digitalClock.setLayoutY(60);
        parent.getChildren().add(1, digitalClock);
        createClockLabels();

        PreferenceController.digitalAboveAnalogProperty.addListener((observable, oldValue, newValue) -> {
            parent.getChildren().remove(digitalClock);
            if (newValue) parent.getChildren().add(digitalClock);
            else parent.getChildren().add(1, digitalClock);
        });
        secondHand.fillProperty().bind(PreferenceController.secondHandColorProperty);
        DropShadow dropShadow = new DropShadow(4, Color.BLACK);
        InnerShadow innerShadow = new InnerShadow();
        PreferenceController.analogueShadowProperty.addListener((observable, oldValue, newValue) -> {
            clockFace.setEffect(newValue ? innerShadow : null);
            hourHand.setEffect(newValue ? dropShadow : null);
            minuteHand.setEffect(newValue ? dropShadow : null);
            secondHand.setEffect(newValue ? dropShadow : null);
        });
    }

    /**
     * Interpolate double.
     * This function accepts any double value interpolate: R => R
     * Steep acceleration and deceleration of the decimal component
     * Whole number component should be kept unchanged
     * <p>
     * Interpolates R => [0,1]
     * When speed approach 0, this function gets smoother and smoother
     * When speed gets larger, this function behaves like a step function at x = 0.5
     * _|‾
     * When speed is 0, result is not well defined so return original value
     * </p>
     *
     * @param value the value to interpolate
     * @return the interpolated value
     */
    public static double interpolate(double value) {
        if (speed.get() == 0) return value;
        if (value > 1) return Math.floor(value) + interpolate(value % 1);
        if (value < 0) return Math.ceil(value) - interpolate(-(value % 1));
        if (value <= 0.5f) return (Math.pow(2, speed.get() * (value * 2 - 1)) - min.get()) * scale.get() / 2;
        return 1 - (Math.pow(2, -speed.get() * (value * 2 - 1)) - min.get()) * scale.get() / 2;
    }

    private void createClockLabels() {
        for (int i = 0; i < 60; i++) {
            if (i % 5 == 0) {
                Text text = new Text();
                text.setText(String.valueOf((i == 0 ? 60 : i) / 5));
                text.getStyleClass().add("clock-element");
                text.setFont(Font.font(i % 15 == 0 ? 24 : 16));
                text.setX(155 * Math.sin(i * Math.PI / 30));
                text.setY(-155 * Math.cos(i * Math.PI / 30));
                text.setX(text.getX() - text.getLayoutBounds().getWidth() / 2);
                text.setY(text.getY() + text.getLayoutBounds().getHeight() / 4);
                clockFace.getChildren().add(text);
            }
            Line line = new Line();
            line.getStyleClass().add("clock-element");
            line.setEndX(200 * Math.sin(i * Math.PI / 30));
            line.setEndY(200 * Math.cos(i * Math.PI / 30));
            line.setStrokeWidth(i % 15 == 0 ? 3 : i % 5 == 0 ? 2 : 1);

            double start = i % 15 == 0 ? 175.0 : i % 5 == 0 ? 185.0 : 190.0;
            line.setStartX(start * Math.sin(i * Math.PI / 30));
            line.setStartY(start * Math.cos(i * Math.PI / 30));
            clockFace.getChildren().add(line);
        }
    }

    /**
     * Refresh clock
     * The calculations are questionable but they work so I guess its fine
     */
    public void refresh() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        int millis = calendar.get(Calendar.MILLISECOND);
        second.setRotate(360d * ((interpolate(seconds + millis / 1000d - .5) / 60 + 1) % 1)); // the % 1 here for angle out of 360 degrees
        minute.setRotate(6d * (minutes + seconds / 60d + millis / 60000d));
        hour.setRotate(30d * (hours + minutes / 60d + seconds / 3600d));
        if (lastFrameSeconds != seconds) {
            lastFrameSeconds = seconds;
            digitalClock.refreshClocks(hours, minutes, seconds);
        }
    }

    /**
     * Resize.
     *
     * @param width  the width
     * @param height the height
     */
    public void resize(double width, double height) {
        double radius = 0.95 * Math.min(width / 2, height / 2);
        parent.setScaleX(radius / 200);
        parent.setScaleY(radius / 200);
        parent.setLayoutX(width / 2);
        parent.setLayoutY(height / 2);
    }
}
