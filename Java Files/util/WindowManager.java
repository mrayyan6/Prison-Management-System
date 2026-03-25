package com.prison.util;

import com.prison.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class WindowManager {
    public static final int APP_WIDTH = 1024;
    public static final int APP_HEIGHT = 768;

    private WindowManager() {
    }

    public static void configureStage(Stage stage, Parent root, String title) {
        stage.setScene(new Scene(root, APP_WIDTH, APP_HEIGHT));
        stage.setTitle(title);
        stage.setResizable(false);
        stage.setMinWidth(APP_WIDTH);
        stage.setMaxWidth(APP_WIDTH);
        stage.setMinHeight(APP_HEIGHT);
        stage.setMaxHeight(APP_HEIGHT);
        stage.setMaximized(false);
        stage.setFullScreen(false);
        stage.centerOnScreen();
    }

    public static Stage switchScene(Stage stage, Class<?> context, String fxmlPath, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(context.getResource(fxmlPath));
        Parent root = loader.load();

        stage.close();
        Stage nextStage = new Stage();
        configureStage(nextStage, root, title);
        nextStage.show();
        return nextStage;
    }

    public static void showDashboardForCurrentUser(Stage stage, Class<?> context) {
        User user = SessionManager.getCurrentUser();
        String role = user == null || user.getRole() == null ? "" : user.getRole().trim();

        String fxml = role.equalsIgnoreCase("Visitor") ? "/fxml/VisitorDashboard.fxml" : "/fxml/MainMenu.fxml";
        String title = role.equalsIgnoreCase("Visitor") ? "Visitor Dashboard - Prison Management System" : "Main Menu - Prison Management System";

        try {
            switchScene(stage, context, fxml, title);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load dashboard", e);
        }
    }
}