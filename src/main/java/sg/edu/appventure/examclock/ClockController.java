package sg.edu.appventure.examclock;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Calendar;

public class ClockController {
    private final Group parent;
    private final Group hour;
    private final Group minute;
    private final Group second;
    private final Calendar calendar;

    private final Text[] clockLabels;
    private final Line[] clockLines;
    private Timeline timeline;
    private final DigitalClock digitalClock;

    public ClockController(Group parent, Group hour, Group minute, Group second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.parent = parent;
        clockLabels = new Text[12];
        clockLines = new Line[60];
        calendar = Calendar.getInstance();
        digitalClock = new DigitalClock(Color.WHITE, Color.TRANSPARENT, Color.LIGHTGREY);
        digitalClock.setLayoutX(-digitalClock.width / 2);
        digitalClock.setLayoutY(60);
        parent.getChildren().add(digitalClock);
        updateClockLabels(parent, 200);
    }

    private void updateClockLabels(Group parent, double radius) {
        for (int i = 0; i < 60; i++) {
            if (i % 5 == 0) {
                Text text;
                if (clockLabels[i / 5] == null) {
                    parent.getChildren().add(clockLabels[i / 5] = text = new Text());
                    text.setText(String.valueOf((i == 0 ? 60 : i) / 5));
                    text.getStyleClass().add("clock-element");
                } else text = clockLabels[i / 5];
                text.setFont(Font.font(18));
                text.setX(160 * Math.sin(i * Math.PI / 30));
                text.setY(-160 * Math.cos(i * Math.PI / 30));
                text.setX(text.getX() - text.getLayoutBounds().getWidth() / 2);
                text.setY(text.getY() + text.getLayoutBounds().getHeight() / 4);
            }
            Line line;
            if (clockLines[i] == null) {
                parent.getChildren().add(line = clockLines[i] = new Line());
                line.getStyleClass().add("clock-element");
            } else line = clockLines[i];
            line.setEndX(200 * Math.sin(i * Math.PI / 30));
            line.setEndY(200 * Math.cos(i * Math.PI / 30));

            double start = i % 15 == 0 ? 175.0 : i % 5 == 0 ? 185.0 : 190.0;
            line.setStartX(start * Math.sin(i * Math.PI / 30));
            line.setStartY(start * Math.cos(i * Math.PI / 30));
        }
        parent.setScaleX(radius / 200);
        parent.setScaleY(radius / 200);
    }

    public void play() {
        if (timeline != null) timeline.stop();
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(16), event1 -> refresh()));
        timeline.play();
    }

    public void stop() {
        if (timeline != null) timeline.stop();
    }

    int lastSecond = 0;

    public void refresh() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        int millis = calendar.get(Calendar.MILLISECOND);
        second.setRotate(360d * (seconds + interpolate(millis / 1000d)) / 60);
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
        if (value <= 0.5f) return ((float) Math.pow(2, speed * (value * 2 - 1)) - min) * scale / 2;
        return (2 - ((float) Math.pow(2, -speed * (value * 2 - 1)) - min) * scale) / 2;
    }

    public void resize(double width, double height) {
        double radius = 0.95 * Math.min(width / 2 - 125.5, height / 2);
        updateClockLabels(parent, radius);
        parent.setLayoutX(width / 2 - 125.5);
        parent.setLayoutY(height / 2);
    }
}
