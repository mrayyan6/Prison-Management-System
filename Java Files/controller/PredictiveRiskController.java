package com.prison.controller;

import com.prison.model.CourtSchedule;
import com.prison.model.Inmate;
import com.prison.util.ActivityLogService;
import com.prison.util.BackgroundLoader;
import com.prison.util.Database;
import com.prison.util.SystemUpdateBus;
import com.prison.util.UiPerformanceUtil;
import com.prison.util.WindowManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class PredictiveRiskController {

    @FXML private ComboBox<Inmate> inmateCombo;
    @FXML private DatePicker courtDatePicker;
    @FXML private TextField courtTimeField;
    @FXML private TextField scheduledByField;
    @FXML private Label statusLabel;

    @FXML private TableView<CourtSchedule> courtScheduleTable;
    @FXML private TableColumn<CourtSchedule, Integer> scheduleIdColumn;
    @FXML private TableColumn<CourtSchedule, Integer> inmateIdColumn;
    @FXML private TableColumn<CourtSchedule, LocalDateTime> courtTimeColumn;
    @FXML private TableColumn<CourtSchedule, String> scheduledByColumn;

    private final Database database = Database.getInstance();

    @FXML
    public void initialize() {
        scheduleIdColumn.setCellValueFactory(new PropertyValueFactory<>("scheduleId"));
        inmateIdColumn.setCellValueFactory(new PropertyValueFactory<>("inmateId"));
        courtTimeColumn.setCellValueFactory(new PropertyValueFactory<>("courtTime"));
        scheduledByColumn.setCellValueFactory(new PropertyValueFactory<>("scheduledBy"));

        courtDatePicker.setValue(LocalDate.now());
        UiPerformanceUtil.optimizeTableScrolling(courtScheduleTable);

        loadInmates();
        loadSchedules();
    }

    @FXML
    private void scheduleCourtCase() {
        Inmate inmate = inmateCombo.getValue();
        LocalDate date = courtDatePicker.getValue();
        String timeText = courtTimeField.getText().trim();
        String scheduledBy = scheduledByField.getText().trim();

        if (inmate == null || date == null || timeText.isEmpty() || scheduledBy.isEmpty()) {
            showError("Fill inmate, date, time, and scheduler.");
            return;
        }

        LocalTime time;
        try {
            time = LocalTime.parse(timeText);
        } catch (Exception e) {
            showError("Use time format HH:mm, e.g. 12:00");
            return;
        }

        LocalDateTime target = LocalDateTime.of(date, time);

        if (database.hasCourtScheduleConflict(target, inmate.getInmateId())) {
            showError("High Risk: Personnel Shortage. 1-hour court buffer conflict detected.");
            return;
        }

        boolean scheduled = database.scheduleCourtCase(inmate.getInmateId(), target, scheduledBy);
        if (!scheduled) {
            showError("Unable to schedule court case.");
            return;
        }

        ActivityLogService.log("Predictive Risk", "Court case scheduled for inmate #" + inmate.getInmateId() + " at " + target);
        SystemUpdateBus.publish();
        statusLabel.setText("Court case scheduled successfully.");
        statusLabel.setStyle("-fx-text-fill: #22c55e;");
        loadSchedules();
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) inmateCombo.getScene().getWindow();
        WindowManager.showDashboardForCurrentUser(stage, getClass());
    }

    private void loadInmates() {
        BackgroundLoader.loadAsync(
            database::getAllInmates,
            inmates -> inmateCombo.setItems(FXCollections.observableArrayList(inmates)),
            error -> showError("Failed to load inmates: " + error.getMessage())
        );
    }

    private void loadSchedules() {
        BackgroundLoader.loadAsync(
            database::getCourtSchedules,
            schedules -> courtScheduleTable.setItems(FXCollections.observableArrayList(schedules)),
            error -> showError("Failed to load schedules: " + error.getMessage())
        );
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #ef4444;");
    }
}