package com.prison.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

public class MainMenuController {
    
    @FXML
    private void openRegisterInmate() {
        loadScene("RegisterInmate.fxml", "Register New Inmate");
    }
    
    @FXML
    private void openManageStaff() {
        loadScene("ManageStaff.fxml", "Manage Staff Records");
    }
    
    @FXML
    private void openAssignWork() {
        loadScene("AssignWork.fxml", "Assign Work to Inmate");
    }
    
    @FXML
    private void openManageResources() {
        loadScene("ManageResources.fxml", "Manage Resources");
    }
    
    @FXML
    private void openMedicalCheckup() {
        loadScene("MedicalCheckup.fxml", "Medical Checkup");
    }
    
    @FXML
    private void openMonitorAttendance() {
        loadScene("MonitorAttendance.fxml", "Monitor Attendance");
    }
    
    @FXML
    private void openIncidentReport() {
        loadScene("IncidentReport.fxml", "Record Incident Report");
    }
    
    @FXML
    private void openRecordMovement() {
        loadScene("RecordMovement.fxml", "Record Movement");
    }
    
    @FXML
    private void openReleaseInmate() {
        loadScene("ReleaseInmate.fxml", "Release Inmate");
    }
    
    @FXML
    private void exitApplication() {
        System.exit(0);
    }
    
    // Hover effects for cards
    @FXML
    private void onCardHover(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        
        // Scale up animation
        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
        st.setToX(1.05);
        st.setToY(1.05);
        st.play();
        
        // Enhanced shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(20);
        shadow.setSpread(0.4);
        card.setEffect(shadow);
    }
    
    @FXML
    private void onCardExit(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        
        // Scale back animation
        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
        
        // Normal shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0));
        shadow.setRadius(10);
        shadow.setSpread(0.2);
        card.setEffect(shadow);
    }
    
    private void loadScene(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}