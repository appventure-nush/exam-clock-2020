package app.nush.examclock.addexam;

import app.nush.examclock.MainController;
import app.nush.examclock.model.Exam;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import tornadofx.control.Form;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class AddExamController {
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy MMM dd");
    public static final DateTimeFormatter[] timeFormatters = {
            DateTimeFormatter.ofPattern("hh:mma"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("h:mma"),
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("hha"),
            DateTimeFormatter.ofPattern("ha")
    };

    private static LocalTime parseTime(String time, int index) {
        if (index >= timeFormatters.length) throw new DateTimeParseException("No match found", time, 0);
        try {
            return LocalTime.parse(time.replace(" ", ""), timeFormatters[index]);
        } catch (DateTimeParseException e) {
            return parseTime(time, index + 1);
        }
    }

    public TextField name_input;
    public DatePicker date_input;
    public Form form;

    public Spinner<Integer> duration_hours;
    public Spinner<Integer> duration_minutes;
    private MainController mainController;
    public TextField start_time_input;
    public TextField end_time_input;

    @FXML
    public void initialize() {
        date_input.setConverter(new StringConverter<LocalDate>() {
            public String toString(LocalDate localDate) {
                if (localDate == null) return "";
                return dateFormatter.format(localDate);
            }

            public LocalDate fromString(String dateString) {
                if (dateString == null || dateString.trim().isEmpty()) return null;
                return LocalDate.parse(dateString, dateFormatter);
            }
        });
        date_input.setValue(LocalDate.now());

        start_time_input.textProperty().addListener((observable, newv, oldv) -> {
            try {
                LocalTime parsed = parseTime(start_time_input.getText().toLowerCase(), 0);
                start_time_input.setUserData(parsed);
                end_time_input.setText(timeFormatters[0].format(parsed.plusHours(duration_hours.getValue()).plusMinutes(duration_minutes.getValue())));
            } catch (DateTimeParseException e) {
//                if (start_time_input.getUserData() != null)
//                    start_time_input.setText(timeFormatter.format((LocalTime) start_time_input.getUserData()));
            }
        });
        end_time_input.textProperty().addListener((observable, newv, oldv) -> {
            try {
                LocalTime parsed = parseTime(end_time_input.getText().toLowerCase(), 0);
                end_time_input.setUserData(parsed);
                LocalTime start = (LocalTime) start_time_input.getUserData();
                int minutes = (int) start.until(parsed, ChronoUnit.MINUTES);
                duration_hours.getValueFactory().setValue(minutes / 60);
                duration_minutes.getValueFactory().setValue(minutes % 60);
            } catch (DateTimeParseException e) {
//                if (end_time_input.getUserData() != null)
//                    end_time_input.setText(timeFormatter.format((LocalTime) end_time_input.getUserData()));
            }
        });
        duration_hours.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (start_time_input.getUserData() == null) return;
            end_time_input.setText(timeFormatters[0].format(((LocalTime) start_time_input.getUserData()).plusHours(duration_hours.getValue()).plusMinutes(duration_minutes.getValue())));
        });
        duration_minutes.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (start_time_input.getUserData() == null) return;
            end_time_input.setText(timeFormatters[0].format(((LocalTime) start_time_input.getUserData()).plusHours(duration_hours.getValue()).plusMinutes(duration_minutes.getValue())));
        });
    }

    @FXML
    public void add(ActionEvent event) {
        try {
            Exam exam = new Exam(name_input.getText(), date_input.getValue(), parseTime(start_time_input.getText().toLowerCase(), 0), parseTime(end_time_input.getText().toLowerCase(), 0));
            mainController.addCallback(exam);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void cancel(ActionEvent event) {
        mainController.addExamStage.hide();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
