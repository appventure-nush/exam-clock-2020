package sg.edu.appventure.examclock.addexam;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTimePicker;
import com.jfoenix.validation.RequiredFieldValidator;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.util.StringConverter;
import sg.edu.appventure.examclock.MainController;
import sg.edu.appventure.examclock.model.Exam;
import tornadofx.control.Form;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class AddExamController {
    public JFXTextField name_input;
    public JFXDatePicker date_input;
    public JFXTimePicker start_time_input;
    public JFXTimePicker end_time_input;
    public Form form;

    public Spinner<Integer> duration_hours;
    public Spinner<Integer> duration_minutes;
    private MainController mainController;

    @FXML
    public void initialize() {
        name_input.setValidators(new RequiredFieldValidator("Required"));
        date_input.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy MMM dd");

            public String toString(LocalDate localDate) {
                if (localDate == null) return "";
                return dateTimeFormatter.format(localDate);
            }

            public LocalDate fromString(String dateString) {
                if (dateString == null || dateString.trim().isEmpty()) return null;
                return LocalDate.parse(dateString, dateTimeFormatter);
            }
        });
        date_input.setValue(LocalDate.now());
        ValidatorBase dateValidator = new ValidatorBase("Date not valid") {
            @Override
            protected void eval() {
                hasErrors.set(date_input.getValue().isBefore(LocalDate.now()));
            }
        };
        date_input.setValidators(new RequiredFieldValidator("Required"), dateValidator);
        ValidatorBase endTimeValidator = new ValidatorBase("End must be after startTime") {
            @Override
            protected void eval() {
                hasErrors.set(start_time_input.getValue().isAfter(end_time_input.getValue()));
            }
        };
        start_time_input.setValidators(new RequiredFieldValidator("Required"));
        end_time_input.setValidators(new RequiredFieldValidator("Required"), endTimeValidator);

        start_time_input.valueProperty().addListener((observable, oldValue, newValue) -> end_time_input.setValue(newValue.plusHours(duration_hours.getValue()).plusMinutes(duration_minutes.getValue())));
        end_time_input.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (start_time_input.getValue() == null) return;
            int minutes = (int) start_time_input.getValue().until(newValue, ChronoUnit.MINUTES);
            duration_hours.getValueFactory().setValue(minutes / 60);
            duration_minutes.getValueFactory().setValue(minutes % 60);
        });
        duration_hours.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (start_time_input.getValue() == null) return;
            end_time_input.setValue(start_time_input.getValue().plusHours(duration_hours.getValue()).plusMinutes(duration_minutes.getValue()));
        });
        duration_minutes.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (start_time_input.getValue() == null) return;
            end_time_input.setValue(start_time_input.getValue().plusHours(duration_hours.getValue()).plusMinutes(duration_minutes.getValue()));
        });
    }

    @FXML
    public void add(ActionEvent event) {
        if (name_input.validate()
                && date_input.validate()
                && start_time_input.validate()
                && end_time_input.validate()) {
            Exam exam = new Exam(name_input.getText(), date_input.getValue(), start_time_input.getValue(), end_time_input.getValue());
            mainController.addCallback(exam);
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
