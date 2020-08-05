package sg.edu.appventure.examclock.updater;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sg.edu.appventure.examclock.Version;

import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class Updater {
    public static final Gson gson = new Gson();
    public static final OkHttpClient client = new OkHttpClient();

    public static void checkUpdates() {
        Request request = new Request.Builder()
                .url("https://api.github.com/repos/appventure-nush/exam-clock-2020/releases")
                .build();
        try {
            Response response = client.newCall(request).execute();
            Release[] releases = gson.fromJson(new InputStreamReader(Objects.requireNonNull(response.body()).byteStream()), Release[].class);
            Arrays.sort(releases, Comparator.comparing(r -> DatatypeConverter.parseDateTime(r.published_at)));
            Release release = releases[releases.length - 1];
            int i = release.tag_name.substring(1).compareTo(Version.getVersion());
            if (i > 0) {
                System.out.println("A new version found!");
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, release.name + " (" + release.tag_name + ")\n" + release.body + "\n\nUpdate?", ButtonType.NO, ButtonType.YES);
                    alert.setTitle("New Version");
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.YES && Desktop.isDesktopSupported()) try {
                        Desktop.getDesktop().browse(new URI(release.assets[0].browser_download_url));
                    } catch (IOException | URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                });
            } else if (i == 0) {
                System.out.println("Latest version in use!");
            } else {
                System.out.println("Alpha version in use!");
            }
        } catch (IOException | NullPointerException e) {
            System.out.println("Update check failed! " + e.getMessage());
        }
    }

    public static class Release {
        public String url, assets_url, upload_url, html_url;
        public int id;
        public String node_id, tag_name, target_commitish, name;
        public boolean draft;
        public User author;
        public boolean prerelease;
        public String created_at, published_at;
        public Asset[] assets;
        public String tarball_url, zipball_url, body;
    }

    public static class User {
        public String login;
        public int id;
        public String node_id, avatar_url, gravatar_id, url, html_url, followers_url, following_url, gists_url,
                starred_url, subscriptions_url, organizations_url, repos_url, events_url, received_events_url, type;
        public boolean site_admin;
    }

    public static class Asset {
        public String url;
        public int id;
        public String node_id;
        public String name;
        public Object label;
        public User uploader;
        public String content_type;
        public String state;
        public int size;
        public int download_count;
        public String created_at;
        public String updated_at;
        public String browser_download_url;
    }
}
