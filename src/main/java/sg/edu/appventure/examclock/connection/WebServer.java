package sg.edu.appventure.examclock.connection;

import com.nimbusds.jose.util.JSONObjectUtils;
import fi.iki.elonen.NanoHTTPD;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import net.minidev.json.JSONObject;
import sg.edu.appventure.examclock.MainController;
import sg.edu.appventure.examclock.model.Exam;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class WebServer extends NanoHTTPD {
    private static final String MIME_JSON = "application/json";
    private final ArrayList<String> verified_keys;
    private final MainController controller;
    private final HashMap<String, byte[]> keys;
    private final ObservableList<Exam> exams;

    public WebServer(MainController controller, HashMap<String, byte[]> keys, ObservableList<Exam> exams, int port) {
        super(port);
        this.controller = controller;
        this.keys = keys;
        this.exams = exams;
        verified_keys = new ArrayList<>();
    }

    @Override
    public Response serve(IHTTPSession session) {
        Response response = serveProxy(session);
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    public Response serveProxy(IHTTPSession session) {
        try {
            session.parseBody(session.getParms());
        } catch (IOException | ResponseException e) {
            e.printStackTrace();
        }
        String keyID = session.getParms().get("keyID");
        if (keyID == null) return serve(session.getUri());
        else {
            if (!keys.containsKey(keyID)) {
                JSONObject response = new JSONObject();
                response.put("error", "who-are-you");
                return newJSONResponse(Response.Status.UNAUTHORIZED, response);
            } else if (!verified_keys.contains(keyID)) { // unverified
                String encrypted = session.getParms().get("encrypted");
                if (encrypted == null) {
                    JSONObject response = new JSONObject();
                    response.put("error", "not-verified");
                    return newJSONResponse(Response.Status.FORBIDDEN, response);
                }
                byte[] key = keys.get(keyID);
                byte[] decrypt = Encryption.decrypt(key, Base64.decode(encrypted.toCharArray()));
                if (decrypt != null && keyID.equals(new String(decrypt))) {
                    verified_keys.add(keyID);
                    System.out.println(keyID + " has been VERIFIED");
                    JSONObject response = new JSONObject();
                    response.put("verified", true);
                    return newJSONResponse(key, response);
                } else {
                    JSONObject response = new JSONObject();
                    response.put("error", "key-do-not-match");
                    return newJSONResponse(Response.Status.FORBIDDEN, response);
                }
            } else {
                String encrypted = session.getParms().get("encrypted");
                byte[] key = keys.get(keyID);
                byte[] decrypt = Encryption.decrypt(key, Base64.decode(encrypted.toCharArray()));
                if (decrypt != null) {
                    JSONObject request = Encryption.toJSONObject(decrypt);
                    if (request != null) {
                        System.out.println(keyID + ": " + request.toJSONString());
                        String method = request.getAsString("method");
                        switch (method) {
                            case "get_exams": {
                                JSONObject response = new JSONObject();
                                response.put("exams", new ArrayList<>(exams));
                                return newJSONResponse(key, response);
                            }
                            case "add_exam": {
                                try {
                                    JSONObject exam = JSONObjectUtils.getJSONObject(request, "exam");
                                    Exam newExam = new Exam(exam.getAsString("code"), exam.getAsString("name"), LocalDate.parse(exam.getAsString("examDate")), LocalTime.parse(exam.getAsString("startTime")), LocalTime.parse(exam.getAsString("endTime")));
                                    if (exams.filtered(e -> e.getCode().equals(newExam.getCode())).size() > 0) {
                                        JSONObject response = new JSONObject();
                                        response.put("error", "duplicated exam");
                                        return newJSONResponse(key, Response.Status.BAD_REQUEST, response);
                                    }
                                    Platform.runLater(() -> exams.add(newExam));
                                    JSONObject response = new JSONObject();
                                    ArrayList<Exam> temp = new ArrayList<>(exams);
                                    temp.add(newExam);
                                    response.put("exams", temp);
                                    return newJSONResponse(key, response);
                                } catch (Exception e) {
                                    JSONObject response = new JSONObject();
                                    response.put("error", "invalid-data: " + e.getMessage());
                                    return newJSONResponse(key, Response.Status.BAD_REQUEST, response);
                                }
                            }
                            case "delete": {
                                String code = request.getAsString("module_code");
                                Platform.runLater(() -> exams.removeIf(e -> e.getCode().equals(code)));
                                ArrayList<Exam> temp = new ArrayList<>(exams);
                                temp.removeIf(e -> e.getCode().equals(code));
                                JSONObject response = new JSONObject();
                                response.put("exams", temp);
                                return newJSONResponse(key, response);
                            }

                            case "start_all": {
                                controller.startAllExams();
                                JSONObject response = new JSONObject();
                                response.put("exams", exams);
                                return newJSONResponse(key, response);
                            }
                            case "stop_all": {
                                controller.stopAllExams();
                                JSONObject response = new JSONObject();
                                response.put("exams", exams);
                                return newJSONResponse(key, response);
                            }
                            case "toilet": {
                                Platform.runLater(() -> controller.toiletOccupied.set(!controller.toiletOccupied.get()));
                                JSONObject response = new JSONObject();
                                response.put("occupied", !controller.toiletOccupied.get());
                                return newJSONResponse(key, response);
                            }
                        }
                        JSONObject response = new JSONObject();
                        response.put("error", "Unsupported method");
                        return newJSONResponse(key, response);
                    } else if (keyID.equals(new String(decrypt))) {
                        JSONObject response = new JSONObject();
                        response.put("error", "already-verified");
                        return newJSONResponse(key, Response.Status.BAD_REQUEST, response);
                    } else {
                        JSONObject response = new JSONObject();
                        response.put("error", "only-json-allowed");
                        return newJSONResponse(key, Response.Status.BAD_REQUEST, response);
                    }
                }
            }
        }
        return newChunkedResponse(Response.Status.NOT_FOUND, MIME_HTML, getClass().getResourceAsStream("/web/404.html"));
    }

    public Response serve(String uri) {
        if (uri.isEmpty()) uri = "/";
        if (uri.endsWith("/")) uri = uri + "index.html";
        InputStream stream = getClass().getResourceAsStream("/web" + uri);
        if (stream != null) return newChunkedResponse(Response.Status.OK, getMimeTypeForFile(uri), stream);
        else
            return newChunkedResponse(Response.Status.NOT_FOUND, MIME_HTML, getClass().getResourceAsStream("/web/404.html"));
    }

    public static Response newJSONResponse(Response.IStatus status, JSONObject object) {
        return newFixedLengthResponse(status, MIME_JSON, object.toJSONString());
    }

    public static Response newJSONResponse(byte[] key, JSONObject object) {
        return newJSONResponse(key, Response.Status.OK, object);
    }

    public static Response newJSONResponse(byte[] key, Response.IStatus status, JSONObject object) {
        return newFixedLengthResponse(status, MIME_PLAINTEXT, new String(Base64.encode(Objects.requireNonNull(
                Encryption.encrypt(key, object.toJSONString().getBytes())
        ))));
    }
}
