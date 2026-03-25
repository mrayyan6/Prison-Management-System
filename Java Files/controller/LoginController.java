package com.prison.controller;

import com.prison.model.*;
import com.prison.util.Database;
import com.prison.util.SessionManager;
import com.prison.util.WindowManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label errorLabel;
    
    private Database db = Database.getInstance();
    
    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please enter both username and password!");
                return;
            }
            
            User user = db.authenticateUser(username, password);
            
            if (user != null) {
                if ("Active".equals(user.getStatus())) 
                {
                    // Login successful
                    System.out.println("Login successful: " + user.getFullName());
                    SessionManager.setCurrentUser(user);
                    openDashboard(event, user);
                } else {
                    errorLabel.setText("Your account is inactive. Contact administrator.");
                }
            } else {
                errorLabel.setText("Invalid username or password!");
            }
        } catch (Exception e) {
            System.err.println("Error in handleLogin: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("Error: " + e.getMessage());
        }
    }
    
    @FXML
    public void openSignup(ActionEvent event) {
        try {
            System.out.println("Opening signup page...");
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            WindowManager.switchScene(stage, getClass(), "/fxml/Signup.fxml", "Sign Up - Prison Management System");
        } catch (Exception e) {
            System.err.println("Error loading signup page: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("Error loading signup page: " + e.getMessage());
        }
    }
    
    private void openDashboard(ActionEvent event, User user) {
        String role = user.getRole() == null ? "" : user.getRole().trim();
        if (role.equalsIgnoreCase("Visitor")) {
            openScene(event, "/fxml/VisitorDashboard.fxml", "Visitor Dashboard - Prison Management System");
        } else {
            openScene(event, "/fxml/MainMenu.fxml", "Main Menu - Prison Management System");
        }
    }

    private void openScene(ActionEvent event, String resourcePath, String title) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            WindowManager.switchScene(stage, getClass(), resourcePath, title);
        } catch (Exception e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("Error loading dashboard: " + e.getMessage());
        }
    }
}
