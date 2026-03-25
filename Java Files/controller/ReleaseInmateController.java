package com.prison.controller;

import com.prison.model.Inmate;
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
import java.time.LocalDate;
import java.util.ArrayList;

public class ReleaseInmateController {
    
    @FXML private TableView<Inmate> eligibleInmatesTable;
    @FXML private TableColumn<Inmate, Integer> inmateIdColumn;
    @FXML private TableColumn<Inmate, String> nameColumn;
    @FXML private TableColumn<Inmate, String> crimeColumn;
    @FXML private TableColumn<Inmate, LocalDate> admissionColumn;
    @FXML private TableColumn<Inmate, LocalDate> releaseColumn;
    @FXML private TableColumn<Inmate, String> cellColumn;
    
    @FXML private TextArea releaseNotesArea;
    @FXML private TextField releasedByField;
    @FXML private Label statusLabel;
    
    private Database database = Database.getInstance();
    private Inmate selectedInmate = null;
    
    @FXML
    public void initialize() {
        // Initialize table columns
        inmateIdColumn.setCellValueFactory(new PropertyValueFactory<>("inmateId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        crimeColumn.setCellValueFactory(new PropertyValueFactory<>("crime"));
        admissionColumn.setCellValueFactory(new PropertyValueFactory<>("admissionDate"));
        releaseColumn.setCellValueFactory(new PropertyValueFactory<>("releaseDate"));
        cellColumn.setCellValueFactory(new PropertyValueFactory<>("cellNumber"));

        UiPerformanceUtil.optimizeTableScrolling(eligibleInmatesTable);
        UiPerformanceUtil.enableBufferedRendering(eligibleInmatesTable);
        
        loadEligibleInmates();
        
        // Add selection listener
        eligibleInmatesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedInmate = newVal;
        });
    }
    
    private void loadEligibleInmates() {
        BackgroundLoader.loadAsync(
            database::getAllActiveInmates,
            inmates -> eligibleInmatesTable.setItems(FXCollections.observableArrayList(inmates)),
            error -> {
                statusLabel.setText("Failed to load eligible inmates: " + error.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        );
    }
    
    @FXML
    private void refreshList() {
        loadEligibleInmates();
        statusLabel.setText("List refreshed");
        statusLabel.setStyle("-fx-text-fill: green;");
    }
    
    @FXML
    private void releaseInmate() {
        if (selectedInmate == null) {
            statusLabel.setText("Please select an inmate to release!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        if (selectedInmate != null && "released".equalsIgnoreCase(selectedInmate.getStatus()))
        {
            statusLabel.setText("This inmate has been released.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }            

        String releasedBy = releasedByField.getText();
        String notes = releaseNotesArea.getText();
        
        if (releasedBy.isEmpty()) {
            statusLabel.setText("Please enter who authorized the release!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        ArrayList<String> adminNames = database.getStaffNamesByRole("Administrator","Admin");
        if (!adminNames.contains(releasedBy)) {
            statusLabel.setText("Only an Administrator can authorize the release!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }



        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Release");
        confirmAlert.setHeaderText("Release Inmate");
        confirmAlert.setContentText("Are you sure you want to release " + selectedInmate.getName() + "?");
        
        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            boolean archived = database.archiveAndReleaseInmate(selectedInmate, releasedBy, notes);
            if (!archived) {
                statusLabel.setText("Failed to archive and release inmate.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Release Successful");
            successAlert.setHeaderText("Inmate Released");
            successAlert.setContentText(String.format(
                "Inmate: %s (ID: %d)\n" +
                "Released By: %s\n" +
                "Release Date: %s\n" +
                "Notes: %s",
                selectedInmate.getName(),
                selectedInmate.getInmateId(),
                releasedBy,
                LocalDate.now(),
                notes.isEmpty() ? "None" : notes
            ));
            successAlert.showAndWait();
            
            statusLabel.setText("Inmate released successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");

            ActivityLogService.log("Manual Release", "Inmate " + selectedInmate.getName() + " Released Early by Warden");

            SystemUpdateBus.publish();
            
            loadEligibleInmates();
            releaseNotesArea.clear();
            releasedByField.clear();
        }
    }
    
    @FXML
    private void goBack() {
        Stage stage = (Stage) eligibleInmatesTable.getScene().getWindow();
        WindowManager.showDashboardForCurrentUser(stage, getClass());
    }
}
