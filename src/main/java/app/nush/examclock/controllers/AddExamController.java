package app.nush.examclock.controllers;

import app.nush.examclock.display.TimePicker;
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
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * The controller for add exam prompt
 */
public class AddExamController {
    /**
     * The constant dateFormatter used for date picker (user friendly, shows jan feb etc)
     */
    public static final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("yyyy MMM dd").toFormatter();
    /**
     * The time formatters that will be supported by start and end fields
     */
    public static final DateTimeFormatter[] timeFormatters = {
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("hh:mma").toFormatter(),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("HH:mm").toFormatter(),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("h:mma").toFormatter(),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("H:mm").toFormatter(),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("hha").toFormatter(),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ha").toFormatter()
    };

    public Form form;
    public TextField name_input;
    public DatePicker date_input;
    public Spinner<Integer> duration_hours;
    public Spinner<Integer> duration_minutes;
    public TimePicker start_time_input;
    public TimePicker end_time_input;

    private MainController mainController;

    public static LocalTime parseTime(String time, int index) {
        if (index >= timeFormatters.length) throw new DateTimeParseException("No match found", time, 0);
        try {
            return LocalTime.parse(time.replace(" ", ""), timeFormatters[index]);
        } catch (DateTimeParseException e) {
            return parseTime(time, index + 1);
        }
    }

    /**
     * Initialize.
     */
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
        start_time_input.timeProperty.addListener((observable, oldValue, newValue) -> end_time_input.timeProperty.set(newValue.plusHours(duration_hours.getValue()).plusMinutes(duration_minutes.getValue())));
        end_time_input.timeProperty.addListener((observable, oldValue, newValue) -> {
            try {
                int minutes = (int) start_time_input.timeProperty.get().until(newValue, ChronoUnit.MINUTES);
                duration_hours.getValueFactory().setValue(minutes / 60);
                duration_minutes.getValueFactory().setValue(minutes % 60);
            } catch (DateTimeParseException e) {
//                if (end_time_input.getUserData() != null)
//                    end_time_input.setText(timeFormatter.format((LocalTime) end_time_input.getUserData()));
            }
        });
        duration_hours.valueProperty().addListener((observable, oldValue, newValue) -> end_time_input.timeProperty.set(start_time_input.timeProperty.get().plusHours(duration_hours.getValue()).plusMinutes(duration_minutes.getValue())));
        duration_minutes.valueProperty().addListener((observable, oldValue, newValue) -> end_time_input.timeProperty.set(start_time_input.timeProperty.get().plusHours(duration_hours.getValue()).plusMinutes(duration_minutes.getValue())));
    }

    /**
     * Add exam
     *
     * @param event the event
     */
    @FXML
    public void addExam(ActionEvent event) {
        try {
            Exam exam = new Exam(name_input.getText(), date_input.getValue(), parseTime(start_time_input.getText(), 0), parseTime(end_time_input.getText(), 0));
            mainController.addExam(exam);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Cancel adding exam
     *
     * @param event the event
     */
    @FXML
    public void cancel(ActionEvent event) {
        mainController.addExamStage.hide();
    }

    /**
     * Sets main controller.
     *
     * @param mainController the main controller
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
