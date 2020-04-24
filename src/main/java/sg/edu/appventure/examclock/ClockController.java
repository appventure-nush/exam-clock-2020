package sg.edu.appventure.examclock;

import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ClockController implements Runnable {
    private final Group hour;
    private final Group minute;
    private final Group second;
    private final Calendar calendar;
    private final Timer timer;
    private TimerTask timerTask;

    public ClockController(Group parent, Group hour, Group minute, Group second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        timer = new Timer();
        calendar = Calendar.getInstance();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                ClockController.this.run();
            }
        };
        timer.scheduleAtFixedRate(timerTask, 1000, 500);
        createClockLabels(parent);
    }

    private void createClockLabels(Group parent) {
        for (int i = 0; i < 60; i++) {
            if (i % 5 == 0) {
                Text text = new Text();
                text.setFont(Font.font(18));
                text.setText(String.valueOf((i == 0 ? 60 : i) / 5));
                text.setX(160 * Math.sin(i * Math.PI / 30));
                text.setY(-160 * Math.cos(i * Math.PI / 30));
                text.setX(text.getX() - text.getLayoutBounds().getWidth() / 2);
                text.setY(text.getY() + text.getLayoutBounds().getHeight() / 4);
                parent.getChildren().add(text);
            }
            Line line = new Line();
            line.setEndX(200 * Math.sin(i * Math.PI / 30));
            line.setEndY(200 * Math.cos(i * Math.PI / 30));

            int start = i % 15 == 0 ? 175 : i % 5 == 0 ? 185 : 190;
            line.setStartX(start * Math.sin(i * Math.PI / 30));
            line.setStartY(start * Math.cos(i * Math.PI / 30));

            parent.getChildren().add(line);
        }
    }

    public void start() {
        if (timerTask != null) timerTask.cancel();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                ClockController.this.run();
            }
        };
        timer.scheduleAtFixedRate(timerTask, 1000, 500);
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
        second.setRotate(360d * seconds / 60);
        minute.setRotate(360d * minutes / 60);
        hour.setRotate(360d * hours / 12);
    }
}
