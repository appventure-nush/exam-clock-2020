package sg.edu.appventure.examclock.connection;

import com.nimbusds.jose.util.JSONObjectUtils;
import fi.iki.elonen.NanoHTTPD;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import net.minidev.json.JSONObject;
import sg.edu.appventure.examclock.MainController;
import sg.edu.appventure.examclock.model.Exam;
import sg.edu.appventure.examclock.model.Key;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;

public class WebServer extends NanoHTTPD {
    private static final String MIME_JSON = "application/json";
    private final ArrayList<String> verified_keys;
    private final MainController controller;
    private final ObservableList<Key> keys;
    private final ObservableList<Exam> exams;

    public WebServer(MainController controller, ObservableList<Exam> exams, int port) {
        super(port);
        this.controller = controller;
        this.keys = controller.keys;
        this.exams = exams;
        verified_keys = new ArrayList<>();
//        try {
//            makeSecure(NanoHTTPD.makeSSLSocketFactory("/keystore.jks", "password".toCharArray()), null);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static Response newJSONResponse(Response.IStatus status, JSONObject object) {
        return newFixedLengthResponse(status, MIME_JSON, object.toJSONString());
    }

    public static Response newJSONResponse(byte[] key, JSONObject object) {
        return newJSONResponse(key, Response.Status.OK, object);
    }

    public static Response newErrorJSONResponse(byte[] key, Response.IStatus status, String error) {
        JSONObject response = new JSONObject();
        response.put("error", error);
        return newJSONResponse(key, status, response);
    }

    public static Response newJSONResponse(byte[] key, Response.IStatus status, JSONObject object) {
        return newFixedLengthResponse(status, MIME_PLAINTEXT, new String(Base64.encode(Objects.requireNonNull(
                Encryption.encrypt(key, object.toJSONString().getBytes())
        ))));
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
            FilteredList<Key> filteredKeys = keys.filtered(key -> key.id.equals(keyID));
            if (filteredKeys.size() == 0) {
                JSONObject response = new JSONObject();
                response.put("error", "who-are-you");
                return newJSONResponse(Response.Status.UNAUTHORIZED, response);
            } else {
                Key key = filteredKeys.get(0);
                String encrypted = session.getParms().get("encrypted");
                if (!verified_keys.contains(keyID)) { // unverified
                    if (encrypted == null) {
                        JSONObject response = new JSONObject();
                        response.put("error", "not-verified");
                        return newJSONResponse(Response.Status.FORBIDDEN, response);
                    }
                    byte[] keyBytes = key.key;
                    byte[] decrypt = Encryption.decrypt(keyBytes, Base64.decode(encrypted.toCharArray()));
                    if (decrypt != null && keyID.equals(new String(decrypt))) {
                        verified_keys.add(keyID);
                        System.out.println(keyID + " has been VERIFIED");
                        JSONObject response = new JSONObject();
                        response.put("verified", true);
                        return newJSONResponse(keyBytes, response);
                    } else {
                        JSONObject response = new JSONObject();
                        response.put("error", "keyBytes-do-not-match");
                        return newJSONResponse(Response.Status.FORBIDDEN, response);
                    }
                } else {
                    byte[] keyBytes = key.key;
                    byte[] decrypt = Encryption.decrypt(keyBytes, Base64.decode(encrypted.toCharArray()));
                    if (decrypt != null) {
                        JSONObject request = Encryption.toJSONObject(decrypt);
                        if (request != null) {
                            System.out.println(keyID + ": " + request.toJSONString());
                            String method = request.getAsString("method");
                            switch (method) {
                                case "get_exams": {
                                    JSONObject response = new JSONObject();
                                    response.put("exams", new ArrayList<>(exams));
                                    return newJSONResponse(keyBytes, response);
                                }
                                case "add_exam": {
                                    try {
                                        if (key.type != Key.KeyType.ADMIN)
                                            return newErrorJSONResponse(keyBytes, Response.Status.BAD_REQUEST, "no-permission");
                                        JSONObject exam = JSONObjectUtils.getJSONObject(request, "exam");
                                        Exam newExam = new Exam(exam.getAsString("name"), LocalDate.parse(exam.getAsString("examDate")), LocalTime.parse(exam.getAsString("startTime")), LocalTime.parse(exam.getAsString("endTime")));
                                        Platform.runLater(() -> exams.add(newExam));
                                        JSONObject response = new JSONObject();
                                        ArrayList<Exam> temp = new ArrayList<>(exams);
                                        temp.add(newExam);
                                        response.put("exams", temp);
                                        return newJSONResponse(keyBytes, response);
                                    } catch (Exception e) {
                                        return newErrorJSONResponse(keyBytes, Response.Status.BAD_REQUEST, "invalid-data: " + e.getMessage());
                                    }
                                }
                                case "delete": {
                                    if (key.type != Key.KeyType.ADMIN)
                                        return newErrorJSONResponse(keyBytes, Response.Status.BAD_REQUEST, "no-permission");
                                    String id = request.getAsString("exam_id");
                                    Platform.runLater(() -> exams.removeIf(e -> e.getID().equals(id)));
                                    ArrayList<Exam> temp = new ArrayList<>(exams);
                                    temp.removeIf(e -> e.getID().equals(id));
                                    JSONObject response = new JSONObject();
                                    response.put("exams", temp);
                                    return newJSONResponse(keyBytes, response);
                                }

                                case "start_all": {
                                    if (key.type != Key.KeyType.ADMIN)
                                        return newErrorJSONResponse(keyBytes, Response.Status.BAD_REQUEST, "no-permission");
                                    controller.startAllExams();
                                    JSONObject response = new JSONObject();
                                    response.put("exams", exams);
                                    return newJSONResponse(keyBytes, response);
                                }
                                case "stop_all": {
                                    if (key.type != Key.KeyType.ADMIN)
                                        return newErrorJSONResponse(keyBytes, Response.Status.BAD_REQUEST, "no-permission");
                                    controller.stopAllExams();
                                    JSONObject response = new JSONObject();
                                    response.put("exams", exams);
                                    return newJSONResponse(keyBytes, response);
                                }
                                case "toilet": {
                                    if (key.type != Key.KeyType.ADMIN && key.type != Key.KeyType.TOILET)
                                        return newErrorJSONResponse(keyBytes, Response.Status.BAD_REQUEST, "no-permission");
                                    Platform.runLater(() -> controller.toiletOccupied.set(!controller.toiletOccupied.get()));
                                    JSONObject response = new JSONObject();
                                    response.put("occupied", !controller.toiletOccupied.get());
                                    return newJSONResponse(keyBytes, response);
                                }
                            }
                            return newErrorJSONResponse(keyBytes, Response.Status.BAD_REQUEST, "unsupported-method");
                        } else if (keyID.equals(new String(decrypt))) {
                            return newErrorJSONResponse(keyBytes, Response.Status.BAD_REQUEST, "already-verified");
                        } else {
                            return newErrorJSONResponse(keyBytes, Response.Status.BAD_REQUEST, "only-json-allowed");
                        }
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
}
