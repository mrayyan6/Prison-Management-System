package com.prison.controller;

import com.prison.model.Inmate;
import com.prison.model.WorkAssignment;
import com.prison.util.BackgroundLoader;
import com.prison.util.Database;
import com.prison.util.UiPerformanceUtil;
import com.prison.util.WindowManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.time.LocalDate;

public class AssignWorkController {
    
    @FXML private ComboBox<Inmate> inmateCombo;
    @FXML private ComboBox<String> workTypeCombo;
    @FXML private DatePicker assignedDatePicker;
    @FXML private TextField supervisorField;
    
    @FXML private TableView<WorkAssignment> assignmentsTable;
    @FXML private TableColumn<WorkAssignment, Integer> assignmentIdColumn;
    @FXML private TableColumn<WorkAssignment, Integer> inmateIdColumn;
    @FXML private TableColumn<WorkAssignment, String> workTypeColumn;
    @FXML private TableColumn<WorkAssignment, LocalDate> dateColumn;
    @FXML private TableColumn<WorkAssignment, String> supervisorColumn;
    @FXML private TableColumn<WorkAssignment, String> statusColumn;
    
    @FXML private Label statusLabel;
    
    private Database database = Database.getInstance();
    
    @FXML
    public void initialize() {
        workTypeCombo.getItems().addAll("Kitchen", "Laundry", "Maintenance", "Cleaning", 
                                       "Library", "Garden", "Workshop", "Office Work");
        assignedDatePicker.setValue(LocalDate.now());
        
        // Initialize table columns
        assignmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("assignmentId"));
        inmateIdColumn.setCellValueFactory(new PropertyValueFactory<>("inmateId"));
        workTypeColumn.setCellValueFactory(new PropertyValueFactory<>("workType"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("assignedDate"));
        supervisorColumn.setCellValueFactory(new PropertyValueFactory<>("supervisorName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        UiPerformanceUtil.optimizeTableScrolling(assignmentsTable);
        UiPerformanceUtil.enableBufferedRendering(inmateCombo, assignmentsTable);

        loadInmates();
        loadAssignments();
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
    
    private void loadAssignments() {
        BackgroundLoader.loadAsync(
            database::getAllWorkAssignments,
            assignments -> assignmentsTable.setItems(FXCollections.observableArrayList(assignments)),
            error -> {
                statusLabel.setText("Failed to load assignments: " + error.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        );
    }
    
    @FXML
    private void assignWork() {
        try {
            Inmate selectedInmate = inmateCombo.getValue();
            String workType = workTypeCombo.getValue();
            LocalDate assignedDate = assignedDatePicker.getValue();
            String supervisor = supervisorField.getText();
            
            if (selectedInmate == null || workType == null || supervisor.isEmpty()) {
                statusLabel.setText("Please fill all fields!");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (selectedInmate != null && "released".equalsIgnoreCase(selectedInmate.getStatus()))
            {
                statusLabel.setText("This inmate has been released and cannot be assigned work.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }            

            
            WorkAssignment assignment = new WorkAssignment(0, selectedInmate.getInmateId(), 
                                                          workType, assignedDate, "Active", supervisor);
            database.addWorkAssignment(assignment);
            
            statusLabel.setText("Work assigned successfully! Assignment ID: " + assignment.getAssignmentId());
            statusLabel.setStyle("-fx-text-fill: green;");
            
            loadAssignments();
            clearFields();
            
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    private void clearFields() {
        inmateCombo.setValue(null);
        workTypeCombo.setValue(null);
        assignedDatePicker.setValue(LocalDate.now());
        supervisorField.clear();
    }
    
    @FXML
    private void goBack() {
        Stage stage = (Stage) inmateCombo.getScene().getWindow();
        WindowManager.showDashboardForCurrentUser(stage, getClass());
    }
}
