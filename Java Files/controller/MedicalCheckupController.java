package com.prison.controller;

import com.prison.model.Inmate;
import com.prison.model.MedicalCheckup;
import com.prison.util.Database;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.time.LocalDate;

public class MedicalCheckupController {
    
    @FXML private ComboBox<Inmate> inmateCombo;
    @FXML private DatePicker checkupDatePicker;
    @FXML private TextField diagnosisField;
    @FXML private TextField treatmentField;
    @FXML private TextField doctorNameField;
    @FXML private TextArea notesArea;
    
    @FXML private TableView<MedicalCheckup> checkupsTable;
    @FXML private TableColumn<MedicalCheckup, Integer> checkupIdColumn;
    @FXML private TableColumn<MedicalCheckup, Integer> inmateIdColumn;
    @FXML private TableColumn<MedicalCheckup, LocalDate> dateColumn;
    @FXML private TableColumn<MedicalCheckup, String> diagnosisColumn;
    @FXML private TableColumn<MedicalCheckup, String> treatmentColumn;
    @FXML private TableColumn<MedicalCheckup, String> doctorColumn;
    
    @FXML private Label statusLabel;
    
    private Database database = Database.getInstance();
    
    @FXML
    public void initialize() {
        inmateCombo.setItems(FXCollections.observableArrayList(database.getAllInmates()));
        checkupDatePicker.setValue(LocalDate.now());
        
        // Initialize table columns
        checkupIdColumn.setCellValueFactory(new PropertyValueFactory<>("checkupId"));
        inmateIdColumn.setCellValueFactory(new PropertyValueFactory<>("inmateId"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("checkupDate"));
        diagnosisColumn.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        treatmentColumn.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        doctorColumn.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        
        loadCheckups();
    }
    
    private void loadCheckups() {
        checkupsTable.setItems(FXCollections.observableArrayList(database.getAllMedicalCheckups()));
    }
    
    @FXML
    private void saveCheckup() {
        try {
            Inmate selectedInmate = inmateCombo.getValue();
            LocalDate checkupDate = checkupDatePicker.getValue();
            String diagnosis = diagnosisField.getText();
            String treatment = treatmentField.getText();
            String doctorName = doctorNameField.getText();
            String notes = notesArea.getText();
            
            if (selectedInmate == null || diagnosis.isEmpty() || treatment.isEmpty() || doctorName.isEmpty()) {
                statusLabel.setText("Please fill all required fields!");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (selectedInmate != null && "released".equalsIgnoreCase(selectedInmate.getStatus()))
            {
                statusLabel.setText("This inmate has been released and cannot have a medical checkup recorded.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }            

            MedicalCheckup checkup = new MedicalCheckup(0, selectedInmate.getInmateId(), 
                                                       checkupDate, diagnosis, treatment, doctorName);
            checkup.setNotes(notes);
            database.addMedicalCheckup(checkup);
            
            statusLabel.setText("Medical checkup saved successfully! Checkup ID: " + checkup.getCheckupId());
            statusLabel.setStyle("-fx-text-fill: green;");
            
            loadCheckups();
            clearFields();
            
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    private void clearFields() {
        inmateCombo.setValue(null);
        checkupDatePicker.setValue(LocalDate.now());
        diagnosisField.clear();
        treatmentField.clear();
        doctorNameField.clear();
        notesArea.clear();
    }
    
    @FXML
    private void goBack() {
        Stage stage = (Stage) inmateCombo.getScene().getWindow();
        stage.close();
    }
}
