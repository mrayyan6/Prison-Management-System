package com.prison.controller;

import com.prison.model.Inmate;
import com.prison.model.IncidentReport;
import com.prison.util.ActivityLogService;
import com.prison.util.BackgroundLoader;
import com.prison.util.Database;
import com.prison.util.SystemUpdateBus;
import com.prison.util.UiPerformanceUtil;
import com.prison.util.WindowManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.time.LocalDateTime;

public class IncidentReportController {
    
    @FXML private ComboBox<Inmate> inmateCombo;
    @FXML private ComboBox<String> incidentTypeCombo;
    @FXML private TextField reportedByField;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea actionTakenArea;
    
    @FXML private TableView<IncidentReport> reportsTable;
    @FXML private TableColumn<IncidentReport, Integer> reportIdColumn;
    @FXML private TableColumn<IncidentReport, Integer> inmateIdColumn;
    @FXML private TableColumn<IncidentReport, String> typeColumn;
    @FXML private TableColumn<IncidentReport, LocalDateTime> dateColumn;
    @FXML private TableColumn<IncidentReport, String> reportedByColumn;
    
    @FXML private Label statusLabel;
    
    private Database database = Database.getInstance();
    
    @FXML
    public void initialize() {
        incidentTypeCombo.getItems().addAll("Fight", "Misconduct", "Rule Violation", 
                                           "Theft", "Damage to Property", "Medical Emergency", 
                                           "Security Breach", "Other");
        
        // Initialize table columns
        reportIdColumn.setCellValueFactory(new PropertyValueFactory<>("reportId"));
        inmateIdColumn.setCellValueFactory(new PropertyValueFactory<>("inmateId"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("incidentType"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("incidentDate"));
        reportedByColumn.setCellValueFactory(new PropertyValueFactory<>("reportedBy"));

        UiPerformanceUtil.optimizeTableScrolling(reportsTable);
        UiPerformanceUtil.enableBufferedRendering(inmateCombo, reportsTable);

        loadInmates();
        loadReports();
    }

    private void loadInmates() {
        BackgroundLoader.loadAsync(
            database::getAllInmates,
            inmates -> inmateCombo.setItems(FXCollections.observableArrayList(inmates)),
            error -> {
                statusLabel.setText("Failed to load inmate names: " + error.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        );
    }
    
    private void loadReports() {
        BackgroundLoader.loadAsync(
            database::getAllIncidentReports,
            reports -> reportsTable.setItems(FXCollections.observableArrayList(reports)),
            error -> {
                statusLabel.setText("Failed to load reports: " + error.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        );
    }
    
    @FXML
    private void saveReport() {
        try {
            Inmate selectedInmate = inmateCombo.getValue();
            String incidentType = incidentTypeCombo.getValue();
            String reportedBy = reportedByField.getText();
            String description = descriptionArea.getText();
            String actionTaken = actionTakenArea.getText();
            
            if (selectedInmate == null || incidentType == null || reportedBy.isEmpty() || description.isEmpty()) {
                statusLabel.setText("Please fill all required fields!");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (selectedInmate != null && "released".equalsIgnoreCase(selectedInmate.getStatus()))
            {
                statusLabel.setText("This inmate has been released and cannot have an incident reported.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }            

            IncidentReport report = new IncidentReport(0, selectedInmate.getInmateId(), 
                                                       LocalDateTime.now(), incidentType, 
                                                       description, reportedBy);
            report.setActionTaken(actionTaken);
            database.addIncidentReport(report);
            ActivityLogService.log("Incident Report", "Inmate " + selectedInmate.getName() + " incident reported: " + incidentType);
            
            statusLabel.setText("Incident report saved successfully! Report ID: " + report.getReportId());
            statusLabel.setStyle("-fx-text-fill: green;");
            
            SystemUpdateBus.publish();
            loadReports();
            clearFields();
            
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    private void clearFields() {
        inmateCombo.setValue(null);
        incidentTypeCombo.setValue(null);
        reportedByField.clear();
        descriptionArea.clear();
        actionTakenArea.clear();
    }
    
    @FXML
    private void goBack() {
        Stage stage = (Stage) inmateCombo.getScene().getWindow();
        WindowManager.showDashboardForCurrentUser(stage, getClass());
    }
}
