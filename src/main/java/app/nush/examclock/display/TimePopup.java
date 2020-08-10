package app.nush.examclock.display;

import app.nush.examclock.model.CustomBinding;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.layout.HBox;
import tornadofx.control.ListItem;
import tornadofx.control.ListMenu;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TimePopup extends PopupControl {

    public TimePopup(TimePicker picker) {
        HBox root = new HBox();
        root.setPrefHeight(300);
        ListMenu hourMenu = new ListMenu();
        List<ListItem> hourMenuItems = IntStream.range(0, 24).mapToObj(i -> new ListItem(String.valueOf(i))).collect(Collectors.toList());
        hourMenu.getChildren().addAll(hourMenuItems);

        ListMenu minuteMenu = new ListMenu();
        List<ListItem> minuteMenuItems = IntStream.range(0, 60).mapToObj(i -> new ListItem(String.valueOf(i))).collect(Collectors.toList());
        minuteMenu.getChildren().addAll(minuteMenuItems);

        CustomBinding.bindBidirectional(hourMenu.activeProperty(), picker.hour, listItem -> Integer.parseInt(listItem.getText()), hour -> hourMenuItems.get((Integer) hour));
        CustomBinding.bindBidirectional(minuteMenu.activeProperty(), picker.minute, listItem -> Integer.parseInt(listItem.getText()), minute -> minuteMenuItems.get((Integer) minute));
        root.getChildren().addAll(new ScrollPane(hourMenu) {{
            setVbarPolicy(ScrollBarPolicy.ALWAYS);
            setHbarPolicy(ScrollBarPolicy.NEVER);
        }}, new ScrollPane(minuteMenu) {{
            setVbarPolicy(ScrollBarPolicy.ALWAYS);
            setHbarPolicy(ScrollBarPolicy.NEVER);
        }});

        setSkin(new Skin<TimePopup>() {
            public TimePopup getSkinnable() {
                return TimePopup.this;
            }

            public Node getNode() {
                return root;
            }

            public void dispose() {
            }
        });
        setHideOnEscape(true);
        setAutoFix(true);
        setAutoHide(true);
    }
}
