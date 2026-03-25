package com.prison.controller;

import com.prison.model.Resources;
import com.prison.util.Database;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ManageResourcesController {
    
    @FXML private TextField resourceNameField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField quantityField;
    @FXML private TextField locationField;
    
    @FXML private TableView<Resources> resourcesTable;
    @FXML private TableColumn<Resources, Integer> resourceIdColumn;
    @FXML private TableColumn<Resources, String> nameColumn;
    @FXML private TableColumn<Resources, String> categoryColumn;
    @FXML private TableColumn<Resources, Integer> quantityColumn;
    @FXML private TableColumn<Resources, String> locationColumn;
    @FXML private TableColumn<Resources, String> statusColumn;
    
    @FXML private Label statusLabel;
    
    private Database database = Database.getInstance();
    private Resources selectedResource = null;
    
    @FXML
    public void initialize() {
        // Initialize combo box
        categoryCombo.getItems().addAll("Healthcare", "Food", "Security", "Maintenance", 
                                       "Education", "Recreation", "Clothing", "Other");
        
        // Initialize table columns
        resourceIdColumn.setCellValueFactory(new PropertyValueFactory<>("resourceId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("resourceName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        loadResources();
        
        // Add selection listener
        resourcesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedResource = newVal;
                populateFields(newVal);
            }
        });
    }
    
    private void loadResources() {
        resourcesTable.setItems(FXCollections.observableArrayList(database.getAllResources()));
    }
    
    private void populateFields(Resources resource) {
        resourceNameField.setText(resource.getResourceName());
        categoryCombo.setValue(resource.getCategory());
        quantityField.setText(String.valueOf(resource.getQuantity()));
        locationField.setText(resource.getLocation());
    }
    
    @FXML
    private void addResource() {
        try {
            String name = resourceNameField.getText();
            String category = categoryCombo.getValue();
            int quantity = Integer.parseInt(quantityField.getText());
            String location = locationField.getText();
            
            if (name.isEmpty() || category == null || location.isEmpty()) {
                statusLabel.setText("Please fill all fields!");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            Resources resource = new Resources(0, name, category, quantity, location, "Available");
            database.addResource(resource);
            
            statusLabel.setText("Resource added successfully! ID: " + resource.getResourceId());
            statusLabel.setStyle("-fx-text-fill: green;");
            
            loadResources();
            clearFields();
            
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid quantity format!");
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    private void updateResource() {
        if (selectedResource == null) {
            statusLabel.setText("Please select a resource to update!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        try {
            String name = resourceNameField.getText();
            String category = categoryCombo.getValue();
            int quantity = Integer.parseInt(quantityField.getText());
            String location = locationField.getText();
            
            if (name.isEmpty() || category == null || location.isEmpty()) {
                statusLabel.setText("Please fill all fields!");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            selectedResource.setResourceName(name);
            selectedResource.setCategory(category);
            selectedResource.setQuantity(quantity);
            selectedResource.setLocation(location);
            
            database.updateResource(selectedResource);
            
            statusLabel.setText("Resource updated successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");
            
            loadResources();
            clearFields();
            
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid quantity format!");
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    private void clearFields() {
        resourceNameField.clear();
        categoryCombo.setValue(null);
        quantityField.clear();
        locationField.clear();
        selectedResource = null;
    }
    
    @FXML
    private void goBack() {
        Stage stage = (Stage) resourceNameField.getScene().getWindow();
        stage.close();
    }
}
