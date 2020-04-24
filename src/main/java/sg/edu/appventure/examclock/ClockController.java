package sg.edu.appventure.examclock;

import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ClockController implements Runnable {
    private final Group parent;
    private final Group hour;
    private final Group minute;
    private final Group second;
    private final Calendar calendar;
    private final Timer timer;
    private TimerTask timerTask;

    private final Circle clockFace;
    private final Text[] clockLabels;
    private final Line[] clockLines;

    public ClockController(Group parent, Circle clockFace, Group hour, Group minute, Group second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.clockFace = clockFace;
        this.parent = parent;
        clockLabels = new Text[12];
        clockLines = new Line[60];
        timer = new Timer();
        calendar = Calendar.getInstance();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                ClockController.this.run();
            }
        };
        updateClockLabels(parent, 200);
    }

    private void updateClockLabels(Group parent, double radius) {
        clockFace.setRadius(radius);
        for (int i = 0; i < 60; i++) {
            if (i % 5 == 0) {
                Text text;
                if (clockLabels[i / 5] == null) {
                    parent.getChildren().add(clockLabels[i / 5] = text = new Text());
                    text.setText(String.valueOf((i == 0 ? 60 : i) / 5));
                    text.getStyleClass().add("clock-element");
                } else text = clockLabels[i / 5];
                text.setFont(Font.font(radius * 0.09));
                text.setX(radius * 0.8 * Math.sin(i * Math.PI / 30));
                text.setY(-radius * 0.8 * Math.cos(i * Math.PI / 30));
                text.setX(text.getX() - text.getLayoutBounds().getWidth() / 2);
                text.setY(text.getY() + text.getLayoutBounds().getHeight() / 4);
            }
            Line line;
            if (clockLines[i] == null) {
                parent.getChildren().add(line = clockLines[i] = new Line());
                line.getStyleClass().add("clock-element");
            } else line = clockLines[i];
            line.setEndX(radius * Math.sin(i * Math.PI / 30));
            line.setEndY(radius * Math.cos(i * Math.PI / 30));

            double start = i % 15 == 0 ? radius * 0.875 : i % 5 == 0 ? radius * 0.925 : radius * 0.95;
            line.setStartX(start * Math.sin(i * Math.PI / 30));
            line.setStartY(start * Math.cos(i * Math.PI / 30));
        }
        second.setScaleX(radius / 200);
        second.setScaleY(radius / 200);
        minute.setScaleX(radius / 200);
        minute.setScaleY(radius / 200);
        hour.setScaleX(radius / 200);
        hour.setScaleY(radius / 200);
    }

    public void start() {
        if (timerTask != null) timerTask.cancel();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                ClockController.this.run();
            }
        };
        timer.scheduleAtFixedRate(timerTask, 1000, 16);
    }

    public void stop() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public void run() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        int millis = calendar.get(Calendar.MILLISECOND);
        second.setRotate(360d * (seconds + interpolate(millis / 1000d)) / 60);
        minute.setRotate(360d * (minutes + seconds / 60d) / 60);
        hour.setRotate(360d * (hours + minutes / 60d) / 12);
        parent.layout();
    }

    private static final double min = Math.pow(2, -10);
    private static final double scale = 1 / (1 - min);

    public static double interpolate(double value) {
        if (value <= 0.5f) return ((float) Math.pow(2, 10 * (value * 2 - 1)) - min) * scale / 2;
        return (2 - ((float) Math.pow(2, -10 * (value * 2 - 1)) - min) * scale) / 2;
    }

    public void resize(double width, double height) {
        double radius = Math.min(width / 2 - 125.5, height / 2);
        updateClockLabels(parent, radius);
        parent.setLayoutX(width / 2 - 125.5);
        parent.setLayoutY(height / 2);
    }
}
