package com.prison.controller;

import com.prison.model.Inmate;
import com.prison.model.WorkAssignment;
import com.prison.util.BackgroundLoader;
import com.prison.util.Database;
import com.prison.util.WindowManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MonitorAttendanceController {
    
    @FXML private ComboBox<Inmate> inmateCombo;
    @FXML private ComboBox<WorkAssignment> workAssignmentCombo;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea attendanceReportArea;
    @FXML private Label statusLabel;
    
    private Database database = Database.getInstance();
    private List<WorkAssignment> allAssignments = new ArrayList<>();
    
    @FXML
    public void initialize() {
        statusCombo.getItems().addAll("Present", "Absent", "Late", "Excused");
        datePicker.setValue(LocalDate.now());

        loadInitialData();
        
        inmateCombo.setOnAction(e -> updateWorkAssignments());
    }

    private void loadInitialData() {
        BackgroundLoader.loadAsync(
            database::getAllInmates,
            inmates -> inmateCombo.setItems(FXCollections.observableArrayList(inmates)),
            error -> {
                statusLabel.setText("Failed to load inmate names: " + error.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        );

        BackgroundLoader.loadAsync(
            database::getAllWorkAssignments,
            assignments -> {
                allAssignments = assignments;
                workAssignmentCombo.setItems(FXCollections.observableArrayList(assignments));
            },
            error -> {
                statusLabel.setText("Failed to load assignments: " + error.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        );
    }
    
    private void updateWorkAssignments() {
        Inmate selected = inmateCombo.getValue();
        if (selected != null) {
            workAssignmentCombo.getItems().clear();
            allAssignments.stream()
                .filter(wa -> wa.getInmateId() == selected.getInmateId())
                .forEach(wa -> workAssignmentCombo.getItems().add(wa));
        }
    }
    
    @FXML
    private void recordAttendance() {
        try {
            Inmate selectedInmate = inmateCombo.getValue();
            WorkAssignment assignment = workAssignmentCombo.getValue();
            LocalDate date = datePicker.getValue();
            String status = statusCombo.getValue();
            
            if (selectedInmate == null || assignment == null || status == null) {
                statusLabel.setText("Please fill all fields!");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            if (selectedInmate != null && "released".equalsIgnoreCase(selectedInmate.getStatus()))
            {
                statusLabel.setText("This inmate has been released and cannot have attendance recorded.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }            

            
            // Update work assignment status
            assignment.setStatus(status);
            database.updateWorkAssignment(assignment);
            
            statusLabel.setText("Attendance recorded successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");
            
            viewReport();
            
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    private void viewReport() {
        BackgroundLoader.loadAsync(
            () -> {
                List<Inmate> inmates = database.getAllInmates();
                List<WorkAssignment> assignments = database.getAllWorkAssignments();

                StringBuilder report = new StringBuilder();
                report.append("=== ATTENDANCE REPORT ===\n\n");
                report.append(String.format("Date: %s\n\n", LocalDate.now()));

                for (Inmate inmate : inmates) {
                    report.append(String.format("Inmate: %s (ID: %d)\n", inmate.getName(), inmate.getInmateId()));

                    boolean hasAssignments = false;
                    for (WorkAssignment wa : assignments) {
                        if (wa.getInmateId() == inmate.getInmateId()) {
                            hasAssignments = true;
                            report.append(String.format("  - %s: %s\n", wa.getWorkType(), wa.getStatus()));
                        }
                    }

                    if (!hasAssignments) {
                        report.append("  - No work assignments\n");
                    }
                    report.append("\n");
                }

                return report.toString();
            },
            report -> attendanceReportArea.setText(report),
            error -> {
                statusLabel.setText("Failed to build attendance report: " + error.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        );
    }
    
    @FXML
    private void goBack() {
        Stage stage = (Stage) inmateCombo.getScene().getWindow();
        WindowManager.showDashboardForCurrentUser(stage, getClass());
    }
}
