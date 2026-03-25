package com.prison.controller;

import com.prison.model.Inmate;
import com.prison.model.WorkAssignment;
import com.prison.util.Database;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;

public class MonitorAttendanceController {
    
    @FXML private ComboBox<Inmate> inmateCombo;
    @FXML private ComboBox<WorkAssignment> workAssignmentCombo;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea attendanceReportArea;
    @FXML private Label statusLabel;
    
    private Database database = Database.getInstance();
    
    @FXML
    public void initialize() {
        inmateCombo.setItems(FXCollections.observableArrayList(database.getAllInmates()));
        workAssignmentCombo.setItems(FXCollections.observableArrayList(database.getAllWorkAssignments()));
        statusCombo.getItems().addAll("Present", "Absent", "Late", "Excused");
        datePicker.setValue(LocalDate.now());
        
        inmateCombo.setOnAction(e -> updateWorkAssignments());
    }
    
    private void updateWorkAssignments() {
        Inmate selected = inmateCombo.getValue();
        if (selected != null) {
            workAssignmentCombo.getItems().clear();
            database.getAllWorkAssignments().stream()
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
        StringBuilder report = new StringBuilder();
        report.append("=== ATTENDANCE REPORT ===\n\n");
        report.append(String.format("Date: %s\n\n", LocalDate.now()));
        
        for (Inmate inmate : database.getAllInmates()) {
            report.append(String.format("Inmate: %s (ID: %d)\n", inmate.getName(), inmate.getInmateId()));
            
            boolean hasAssignments = false;
            for (WorkAssignment wa : database.getAllWorkAssignments()) {
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
        
        attendanceReportArea.setText(report.toString());
    }
    
    @FXML
    private void goBack() {
        Stage stage = (Stage) inmateCombo.getScene().getWindow();
        stage.close();
    }
}
