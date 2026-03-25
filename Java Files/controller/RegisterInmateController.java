package com.prison.controller;

import com.prison.model.Inmate;
import com.prison.util.Database;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;

public class RegisterInmateController {
    
    @FXML private TextField nameField;
    @FXML private TextField ageField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField crimeField;
    @FXML private DatePicker admissionDatePicker;
    @FXML private DatePicker releaseDatePicker;
    @FXML private TextField cellNumberField;
    @FXML private Label statusLabel;
    
    private Database database = Database.getInstance();
    
    @FXML
    public void initialize() {
        genderCombo.getItems().addAll("Male", "Female", "Other");
        admissionDatePicker.setValue(LocalDate.now());
    }
    
    @FXML
    private void registerInmate() {
        try {
            String name = nameField.getText();
            int age = Integer.parseInt(ageField.getText());
            String gender = genderCombo.getValue();
            String crime = crimeField.getText();
            LocalDate admissionDate = admissionDatePicker.getValue();
            LocalDate releaseDate = releaseDatePicker.getValue();
            String cellNumber = cellNumberField.getText();
            
            if (name.isEmpty() || gender == null || crime.isEmpty() || cellNumber.isEmpty()) {
                statusLabel.setText("Please fill all fields!");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            Inmate inmate = new Inmate(0, name, age, gender, crime, admissionDate, 
                                      releaseDate, "Active", cellNumber);
            database.addInmate(inmate);
            
            statusLabel.setText("Inmate registered successfully! ID: " + inmate.getInmateId());
            statusLabel.setStyle("-fx-text-fill: green;");
            
            clearFields();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid age format!");
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    private void clearFields() {
        nameField.clear();
        ageField.clear();
        genderCombo.setValue(null);
        crimeField.clear();
        admissionDatePicker.setValue(LocalDate.now());
        releaseDatePicker.setValue(null);
        cellNumberField.clear();
    }
    
    @FXML
    private void goBack() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
