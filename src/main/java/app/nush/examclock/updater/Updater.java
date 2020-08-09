package app.nush.examclock.updater;

import app.nush.examclock.ExamClock;
import app.nush.examclock.Version;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

/**
 * Automatic updater
 * Check if a new version has been released
 */
public class Updater {
    public static final Gson gson = new Gson();
    public static final OkHttpClient client = new OkHttpClient();

    /**
     * Async update.
     */
    public static void asyncUpdate() {
        new Thread(Updater::checkUpdates).start();
    }

    /**
     * Check updates.
     */
    public static void checkUpdates() {
        Request request = new Request.Builder()
                .url("https://api.github.com/repos/appventure-nush/exam-clock-2020/releases")
                .build();
        try {
            Response response = client.newCall(request).execute();
            Release[] releases = gson.fromJson(new InputStreamReader(Objects.requireNonNull(response.body()).byteStream()), Release[].class);
            Arrays.sort(releases, Comparator.comparing(r -> Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(r.published_at)))));
            Release release = releases[releases.length - 1];
            int i = release.tag_name.substring(1).compareTo(Version.getVersion());
            if (i > 0) {
                System.out.println("A new version found!");
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, release.body + "\n\nUpdate?", ButtonType.NO, ButtonType.YES);
                    alert.setTitle("New Version");
                    alert.setHeaderText(release.name + " (" + release.tag_name + ")");
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.YES)
                        ExamClock.getInstance().getHostServices().showDocument(release.assets[0].url);
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

//    public static void replaceCurrentJar() {
//        try {
//            File currentJar = new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI());
//            // do stuff idk
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//    }

    public static class Release {
        public String url,
                assets_url,
                upload_url,
                html_url;
        public int id;
        public String node_id,
                tag_name,
                target_commitish,
                name;
        public boolean draft;
        public User author;
        public boolean prerelease;
        public String created_at,
                published_at;
        public Asset[] assets;
        public String tarball_url,
                zipball_url,
                body;
    }

    public static class User {
        public String login;
        public int id;
        public String node_id,
                avatar_url,
                gravatar_id,
                url,
                html_url,
                followers_url,
                following_url,
                gists_url,
                starred_url,
                subscriptions_url,
                organizations_url,
                repos_url,
                events_url,
                received_events_url, type;
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
