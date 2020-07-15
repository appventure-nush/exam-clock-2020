package sg.edu.appventure.examclock.connection;

import io.socket.client.IO;
import io.socket.client.Socket;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import net.minidev.json.JSONObject;
import okhttp3.OkHttpClient;
import sg.edu.appventure.examclock.MainController;
import sg.edu.appventure.examclock.PreferenceController;
import sg.edu.appventure.examclock.addexam.AddExamController;
import sg.edu.appventure.examclock.model.Exam;

import java.net.URISyntaxException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;

public class ClientSocket {
    private Socket socket;
    private final MainController controller;

    public ClientSocket(MainController controller) {
        this.controller = controller;
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
        IO.setDefaultOkHttpCallFactory(okHttpClient);
        IO.Options opts = new IO.Options();
        opts.callFactory = okHttpClient;
        opts.webSocketFactory = okHttpClient;
        try {
            socket = IO.socket("https://exam-clock-nush.tk", opts);
//            socket = IO.socket("http://localhost:3000", opts);
            socket.on(Socket.EVENT_CONNECT, args -> {
                System.out.println("Connected to Server!");
                sendHello();
            }).on(Socket.EVENT_DISCONNECT, args -> {
                System.out.println("Disconnected from Server!");
            });
            socket.on("clock_id_clash", this::onClockIDClash);
            socket.on("new_exam", this::onNewExam);
            socket.on("delete_exam", this::onDeleteExam);
            socket.on("request", this::onRequest);
            // TODO: add event "request"
            // TODO: send event "request_callback" back to server
            socket.open();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void onRequest(Object[] objects) {
        String socketID = String.valueOf(objects[0]);
        String nick = String.valueOf(objects[1]);
        System.out.println("Received request from " + socketID + " (" + nick + ") to take control");
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "A new controller \"" + nick + "\" wants to connect, confirm?", ButtonType.OK, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.OK) socket.emit("request_callback", socketID, "accepted");
            else socket.emit("request_callback", socketID, "rejected");
            System.out.println("Responded to " + socketID + " (" + nick + ") with " + (alert.getResult() == ButtonType.OK ? "accepted" : "rejected"));
        });
    }

    private void onNewExam(Object... objects) {
        try {
            controller.exams.add(new Exam(
                    String.valueOf(objects[1]),
                    LocalDate.parse(String.valueOf(objects[2]), AddExamController.dateFormatter),
                    LocalTime.parse(String.valueOf(objects[3]), AddExamController.timeFormatter),
                    LocalTime.parse(String.valueOf(objects[4]), AddExamController.timeFormatter)));
        } catch (DateTimeException e) {
            socket.emit("clock_error", objects[0], "date_time_invalid");
        } catch (RuntimeException e) {
            socket.emit("clock_error", objects[0], e.getMessage());
        }
    }

    private void onDeleteExam(Object... objects) {
        String id = String.valueOf(objects[1]);
        for (int i = 0; i < controller.exams.size(); i++) {
            if (controller.exams.get(i).id.equals(id)) {
                controller.exams.remove(i);
                return;
            }
        }
        socket.emit("clock_error", objects[0], "exam_not_found");
    }

    private void sendHello() {
        JSONObject obj = new JSONObject();
        obj.put("clockID", PreferenceController.clockID);
        obj.put("clockName", PreferenceController.lanNameProperty.get());
        socket.emit("clock_connected", obj.toJSONString());
    }

    public void onClockIDClash(Object... args) {
        System.out.println("So somehow there's a clash of clock id");
        controller.regenClockID();
    }

    public void close() {
        socket.close();
    }
}
