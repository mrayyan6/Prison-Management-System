package com.prison.controller;

import com.prison.model.User;
import com.prison.model.Admin;
import com.prison.util.Database;
import com.prison.util.WindowManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.time.LocalDateTime;

public class SignupController {
    
    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private ComboBox<String> roleCombo;
    
    @FXML
    private Label errorLabel;
    
    private Database db = Database.getInstance();
    
    @FXML
    public void initialize() {
        // Populate role dropdown
        if (roleCombo != null) {
            roleCombo.getItems().clear();
            roleCombo.getItems().addAll("Administrator", "Warden", "Visitor");
        }
    }
    
    @FXML
    public void handleSignup(ActionEvent event) {
        try {
            String fullName = fullNameField.getText().trim();
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String role = roleCombo.getValue();
            
            // Validation
            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || 
                password.isEmpty() || confirmPassword.isEmpty() || role == null) {
                errorLabel.setText("All fields are required!");
                return;
            }
            
            if (username.length() < 4) {
                errorLabel.setText("Username must be at least 4 characters!");
                return;
            }
            
            if (password.length() < 6) {
                errorLabel.setText("Password must be at least 6 characters!");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                errorLabel.setText("Passwords do not match!");
                return;
            }
            
            if (!email.contains("@") || !email.contains(".")) {
                errorLabel.setText("Invalid email address!");
                return;
            }
            
            // Check if username already exists
            if (db.usernameExists(username)) {
                errorLabel.setText("Username already exists! Choose another.");
                return;
            }
            
            // Check if email already exists
            if (db.emailExists(email)) {
                errorLabel.setText("Email already registered!");
                return;
            }
            
            // Create new user
            User newUser = new Admin(0, username, password, email, fullName, LocalDateTime.now(), "Active");
            newUser.setRole(role);
            
            boolean success = db.addUser(newUser);
            
            if (success) {
                System.out.println("User registered successfully: " + username);
                showSuccessAndRedirect(event);
            } else {
                errorLabel.setText("Registration failed! Please try again.");
            }
        } catch (Exception e) {
            System.err.println("Error in handleSignup: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("Error: " + e.getMessage());
        }
    }
    
    private void showSuccessAndRedirect(ActionEvent event) {
        try {
            // Show success message
            errorLabel.setStyle("-fx-text-fill: #44ff44; -fx-font-size: 12px;");
            errorLabel.setText("✅ Registration successful! Redirecting to login...");
            
            // Wait for 1.5 seconds then redirect
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> openLogin(event));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            
        } catch (Exception e) {
            System.err.println("Error in showSuccessAndRedirect: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void openLogin(ActionEvent event) {
        try {
            System.out.println("Opening login page...");
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            WindowManager.switchScene(stage, getClass(), "/fxml/Login.fxml", "Login - Prison Management System");
        } catch (Exception e) {
            System.err.println("Error loading login page: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("Error loading login page: " + e.getMessage());
        }
    }
}