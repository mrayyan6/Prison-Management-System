package com.prison.controller;

import com.prison.model.Staff;
import com.prison.util.Database;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ManageStaffController {
    
    @FXML private TableView<Staff> staffTable;
    @FXML private TableColumn<Staff, Integer> staffIdColumn;
    @FXML private TableColumn<Staff, String> nameColumn;
    @FXML private TableColumn<Staff, String> roleColumn;
    @FXML private TableColumn<Staff, String> shiftColumn;
    @FXML private TableColumn<Staff, String> contactColumn;
    
    @FXML private VBox staffForm;
    @FXML private TextField staffNameField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private ComboBox<String> shiftCombo;
    @FXML private TextField contactField;
    @FXML private Label statusLabel;
    
    private Database database = Database.getInstance();
    private Staff selectedStaff = null;
    private boolean isEditMode = false;
    
    @FXML
    public void initialize() {
        // Initialize table columns
        staffIdColumn.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        shiftColumn.setCellValueFactory(new PropertyValueFactory<>("shift"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactInfo"));
        
        // Initialize combo boxes
        roleCombo.getItems().addAll("Administrator", "Guard", "Medical Staff", "Warden", "Support Staff");
        shiftCombo.getItems().addAll("Day", "Night", "Rotating");
        
        // Hide form initially
        staffForm.setVisible(false);
        
        // Load data
        loadStaffData();
        
        // Add selection listener
        staffTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedStaff = newVal;
        });
    }
    
    private void loadStaffData() {
        staffTable.setItems(FXCollections.observableArrayList(database.getAllStaff()));
    }
    
    @FXML
    private void showAddStaffForm() {
        isEditMode = false;
        staffForm.setVisible(true);
        clearForm();
        statusLabel.setText("");
    }
    
    @FXML
    private void updateStaff() {
        if (selectedStaff == null) {
            statusLabel.setText("Please select a staff member to update!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        isEditMode = true;
        staffForm.setVisible(true);
        
        // Populate form with selected staff data
        staffNameField.setText(selectedStaff.getName());
        roleCombo.setValue(selectedStaff.getRole());
        shiftCombo.setValue(selectedStaff.getShift());
        contactField.setText(selectedStaff.getContactInfo());
        
        statusLabel.setText("");
    }
    
    @FXML
    private void deleteStaff() {
        if (selectedStaff == null) {
            statusLabel.setText("Please select a staff member to delete!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Staff Member");
        alert.setContentText("Are you sure you want to delete " + selectedStaff.getName() + "?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            database.deleteStaff(selectedStaff.getStaffId());
            loadStaffData();
            statusLabel.setText("Staff member deleted successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");
        }
    }
    
    @FXML
    private void saveStaff() {
        try {
            String name = staffNameField.getText();
            String role = roleCombo.getValue();
            String shift = shiftCombo.getValue();
            String contact = contactField.getText();
            
            if (name.isEmpty() || role == null || shift == null || contact.isEmpty()) {
                statusLabel.setText("Please fill all fields!");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (isEditMode && selectedStaff != null) {
                selectedStaff.setName(name);
                selectedStaff.setRole(role);
                selectedStaff.setShift(shift);
                selectedStaff.setContactInfo(contact);
                database.updateStaff(selectedStaff);
                statusLabel.setText("Staff updated successfully!");
            } else {
                Staff staff = new Staff(0, name, role, shift, contact);
                database.addStaff(staff);
                statusLabel.setText("Staff added successfully!");
            }
            
            statusLabel.setStyle("-fx-text-fill: green;");
            loadStaffData();
            cancelForm();
            
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    private void cancelForm() {
        staffForm.setVisible(false);
        clearForm();
    }
    
    private void clearForm() {
        staffNameField.clear();
        roleCombo.setValue(null);
        shiftCombo.setValue(null);
        contactField.clear();
    }
    
    @FXML
    private void goBack() {
        Stage stage = (Stage) staffTable.getScene().getWindow();
        stage.close();
    }
}
