package sg.edu.appventure.examclock.display;

import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import sg.edu.appventure.examclock.PreferenceController;

import java.util.Calendar;

public class ClockController {
    private final Group parent;
    private final Group clockFace;
    private final Group hour;
    private final Group minute;
    private final Group second;
    private final Calendar calendar;
    private final DigitalClock digitalClock;

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

    int lastSecond = 0;

    public void refresh() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        int millis = calendar.get(Calendar.MILLISECOND);
        second.setRotate(360d * ((interpolate(seconds + millis / 1000d - .5) / 60 + 1) % 1));
        minute.setRotate(360d * (minutes + seconds / 60d + millis / 60000d) / 60);
        hour.setRotate(360d * (hours + minutes / 60d + seconds / 3600d) / 12);
        if (lastSecond != seconds) {
            lastSecond = seconds;
            digitalClock.refreshClocks(hours, minutes, seconds);
        }
    }

    private static final double speed = 20;
    private static final double min = Math.pow(2, -speed);
    private static final double scale = 1 / (1 - min);

    public static double interpolate(double value) {
        if (value > 1) return Math.floor(value) + interpolate(value % 1);
        if (value < 0) return Math.ceil(value) - interpolate(-(value % 1));
        if (value <= 0.5f) return ((float) Math.pow(2, speed * (value * 2 - 1)) - min) * scale / 2;
        return (2 - ((float) Math.pow(2, -speed * (value * 2 - 1)) - min) * scale) / 2;
    }

    public void resize(double width, double height) {
        double radius = 0.95 * Math.min(width / 2, height / 2);
        parent.setScaleX(radius / 200);
        parent.setScaleY(radius / 200);
        parent.setLayoutX(width / 2);
        parent.setLayoutY(height / 2);
    }
}
