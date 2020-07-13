package sg.edu.appventure.examclock.connection;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.minidev.json.JSONObject;
import okhttp3.OkHttpClient;
import sg.edu.appventure.examclock.MainController;

import java.net.URISyntaxException;

public class ClientSocket {
    private final Socket socket;
    private final MainController controller;

    public ClientSocket(MainController controller) throws URISyntaxException {
        this.controller = controller;
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
        IO.setDefaultOkHttpCallFactory(okHttpClient);
        IO.Options opts = new IO.Options();
        opts.callFactory = okHttpClient;
        opts.webSocketFactory = okHttpClient;
        socket = IO.socket("http://localhost", opts);
        socket.on(Socket.EVENT_CONNECT, args -> {
            JSONObject obj = new JSONObject();
            obj.put("hello", "server");
            obj.put("binary", new byte[42]);
            socket.emit("new_clock_connected", obj.toJSONString());
        }).on("event", args -> {
        }).on(Socket.EVENT_DISCONNECT, args -> {
        });
    }
}
