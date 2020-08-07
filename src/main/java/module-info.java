module main {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.prefs;
    requires preferencesfx.core;
    requires com.google.gson;
    requires okhttp;
    requires socket.io.client;
    requires engine.io.client;
    exports app.nush.examclock;
    exports app.nush.examclock.res;
    exports app.nush.examclock.addexam;
    exports app.nush.examclock.connection;
    exports app.nush.examclock.display;
    exports app.nush.examclock.model;
    exports app.nush.examclock.updater;
}