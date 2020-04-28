package sg.edu.appventure.examclock.connection;

import fi.iki.elonen.NanoHTTPD;
import javafx.collections.ObservableList;
import net.minidev.json.JSONObject;
import sg.edu.appventure.examclock.model.Exam;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class WebServer extends NanoHTTPD {
    private static final String MIME_JSON = "application/json";
    private final ArrayList<String> verified_keys;
    private final HashMap<String, byte[]> keys;
    private final ObservableList<Exam> exams;

    public WebServer(HashMap<String, byte[]> keys, ObservableList<Exam> exams, int port) {
        super(port);
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
                byte[] decrypt = Encryption.decrypt(keys.get(keyID), Base64.decode(encrypted.toCharArray()));
                if (decrypt != null && keyID.equals(new String(decrypt))) {
                    verified_keys.add(keyID);
                    System.out.println(keyID + " has been VERIFIED");
                    JSONObject response = new JSONObject();
                    response.put("verified", true);
                    return newJSONResponse(response);
                } else {
                    JSONObject response = new JSONObject();
                    response.put("error", "key-do-not-match");
                    return newJSONResponse(Response.Status.FORBIDDEN, response);
                }
            } else {
                String encrypted = session.getParms().get("encrypted");
                byte[] decrypt = Encryption.decrypt(keys.get(keyID), Base64.decode(encrypted.toCharArray()));
                if (decrypt != null) {
                    JSONObject request = Encryption.toJSONObject(decrypt);
                    if (request != null) {
                        System.out.println(keyID + ": " + request.toJSONString());
                        request.getAsString("method");
                        JSONObject response = new JSONObject();
                        return newJSONResponse(response);
                    } else {
                        JSONObject response = new JSONObject();
                        response.put("error", "only-json-allowed");
                        return newJSONResponse(Response.Status.BAD_REQUEST, response);
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

    public static Response newJSONResponse(JSONObject object) {
        return newFixedLengthResponse(Response.Status.OK, MIME_JSON, object.toJSONString());
    }

    public static Response newJSONResponse(Response.IStatus status, JSONObject object) {
        return newFixedLengthResponse(status, MIME_JSON, object.toJSONString());
    }
}
