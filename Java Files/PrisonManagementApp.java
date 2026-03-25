package com.prison;

import com.prison.util.Database;
import com.prison.util.AutoReleaseService;
import com.prison.util.WindowManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class PrisonManagementApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize database
        Database.getInstance();
        AutoReleaseService.start();
        
        // Load Login page instead of Main Menu
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();

        WindowManager.configureStage(primaryStage, root, "Login - Prison Management System");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}