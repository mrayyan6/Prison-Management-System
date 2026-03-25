package com.prison.controller;

import com.prison.model.Inmate;
import com.prison.model.MovementRecord;
import com.prison.util.Database;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.time.LocalDateTime;

public class RecordMovementController {
    
    @FXML private ComboBox<Inmate> inmateCombo;
    @FXML private TextField fromLocationField;
    @FXML private TextField toLocationField;
    @FXML private ComboBox<String> reasonCombo;
    @FXML private TextField authorizedByField;
    
    @FXML private TableView<MovementRecord> movementsTable;
    @FXML private TableColumn<MovementRecord, Integer> movementIdColumn;
    @FXML private TableColumn<MovementRecord, Integer> inmateIdColumn;
    @FXML private TableColumn<MovementRecord, String> fromColumn;
    @FXML private TableColumn<MovementRecord, String> toColumn;
    @FXML private TableColumn<MovementRecord, LocalDateTime> timeColumn;
    @FXML private TableColumn<MovementRecord, String> reasonColumn;
    
    @FXML private Label statusLabel;
    
    private Database database = Database.getInstance();
    
    @FXML
    public void initialize() {
        inmateCombo.setItems(FXCollections.observableArrayList(database.getAllInmates()));
        reasonCombo.getItems().addAll("Cell Transfer", "Medical Visit", "Court Appearance", 
                                     "Visitation", "Work Assignment", "Recreation", 
                                     "Meal Time", "Other");
        
        // Initialize table columns
        movementIdColumn.setCellValueFactory(new PropertyValueFactory<>("movementId"));
        inmateIdColumn.setCellValueFactory(new PropertyValueFactory<>("inmateId"));
        fromColumn.setCellValueFactory(new PropertyValueFactory<>("fromLocation"));
        toColumn.setCellValueFactory(new PropertyValueFactory<>("toLocation"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("movementTime"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        
        loadMovements();
        
        // Auto-populate from location based on selected inmate's current cell
        inmateCombo.setOnAction(e -> {
            Inmate selected = inmateCombo.getValue();
            if (selected != null) {
                fromLocationField.setText(selected.getCellNumber());
            }
        });
    }
    
    private void loadMovements() {
        movementsTable.setItems(FXCollections.observableArrayList(database.getAllMovementRecords()));
    }
    
    @FXML
    private void recordMovement() {
        try {
            Inmate selectedInmate = inmateCombo.getValue();
            String fromLocation = fromLocationField.getText();
            String toLocation = toLocationField.getText();
            String reason = reasonCombo.getValue();
            String authorizedBy = authorizedByField.getText();
            
            if (selectedInmate == null || fromLocation.isEmpty() || toLocation.isEmpty() || 
                reason == null || authorizedBy.isEmpty()) {
                statusLabel.setText("Please fill all fields!");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            if (selectedInmate != null && "released".equalsIgnoreCase(selectedInmate.getStatus()))
            {
                statusLabel.setText("This inmate has been released and cannot be moved.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }            
            MovementRecord movement = new MovementRecord(0, selectedInmate.getInmateId(), 
                                                        fromLocation, toLocation, 
                                                        LocalDateTime.now(), reason, authorizedBy);
            database.addMovementRecord(movement);
            
            // Update inmate's cell number if it's a cell transfer
            if ("Cell Transfer".equals(reason)) {
                selectedInmate.setCellNumber(toLocation);
                database.updateInmate(selectedInmate);
            }
            
            statusLabel.setText("Movement recorded successfully! Movement ID: " + movement.getMovementId());
            statusLabel.setStyle("-fx-text-fill: green;");
            
            loadMovements();
            clearFields();
            
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    private void clearFields() {
        inmateCombo.setValue(null);
        fromLocationField.clear();
        toLocationField.clear();
        reasonCombo.setValue(null);
        authorizedByField.clear();
    }
    
    @FXML
    private void goBack() {
        Stage stage = (Stage) inmateCombo.getScene().getWindow();
        stage.close();
    }
}
