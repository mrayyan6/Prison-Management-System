package com.prison.controller;

import com.prison.model.Inmate;
import com.prison.model.User;
import com.prison.util.BalanceUpdateBus;
import com.prison.util.BackgroundLoader;
import com.prison.util.Database;
import com.prison.util.SessionManager;
import com.prison.util.UiPerformanceUtil;
import com.prison.util.WindowManager;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VisitorDashboardController {

    @FXML private VBox dashboardRoot;
    @FXML private Label welcomeLabel;
    @FXML private TextField inmateIdField;
    @FXML private TextField depositAmountField;
    @FXML private Label foundInmateLabel;
    @FXML private Label foundBalanceLabel;
    @FXML private Label depositStatusLabel;

    private final Database database = Database.getInstance();
    private Inmate foundInmate;
    private final ChangeListener<Number> balanceListener = (obs, oldVal, newVal) -> refreshFoundInmateBalance();

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        String name = user == null ? "Visitor" : user.getFullName();
        welcomeLabel.setText("Welcome, " + name + ". Choose one of your visitor services.");
        UiPerformanceUtil.enableBufferedRendering(dashboardRoot);
        BalanceUpdateBus.versionProperty().addListener(balanceListener);
    }

    @FXML
    private void openFinancialServices() {
        openWindow("/fxml/FinancialServices.fxml", "Financial Services");
    }

    @FXML
    private void openIncidentReports() {
        openWindow("/fxml/VisitorIncidentReports.fxml", "Public Incident Reports");
    }

    @FXML
    private void logout() {
        try {
            BalanceUpdateBus.versionProperty().removeListener(balanceListener);
            SessionManager.clear();
            Stage current = (Stage) dashboardRoot.getScene().getWindow();
            WindowManager.switchScene(current, getClass(), "/fxml/Login.fxml", "Login - Prison Management System");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openWindow(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) dashboardRoot.getScene().getWindow();
            WindowManager.switchScene(stage, getClass(), fxmlPath, title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void findInmate() {
        String idText = inmateIdField.getText().trim();
        int inmateId;

        try {
            inmateId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showDepositError("Enter a valid inmate ID.");
            return;
        }

        depositStatusLabel.setText("Searching inmate...");
        depositStatusLabel.setStyle("-fx-text-fill: #f59e0b;");

        BackgroundLoader.loadAsync(
            () -> database.getInmate(inmateId),
            inmate -> {
                if (inmate == null) {
                    foundInmate = null;
                    foundInmateLabel.setText("Inmate: Not found");
                    foundBalanceLabel.setText("Balance: --");
                    showDepositError("No inmate found with ID " + inmateId + ".");
                    return;
                }

                foundInmate = inmate;
                foundInmateLabel.setText("Inmate: " + inmate.getName() + " (ID " + inmate.getInmateId() + ")");
                foundBalanceLabel.setText(String.format("Balance: %.2f", inmate.getBalance()));
                depositStatusLabel.setText("Inmate located. Ready to deposit.");
                depositStatusLabel.setStyle("-fx-text-fill: #22c55e;");
            },
            error -> showDepositError("Lookup failed: " + error.getMessage())
        );
    }

    @FXML
    private void depositFunds() {
        if (foundInmate == null) {
            showDepositError("Search and select an inmate first.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(depositAmountField.getText().trim());
        } catch (NumberFormatException e) {
            showDepositError("Enter a valid deposit amount.");
            return;
        }

        if (amount <= 0) {
            showDepositError("Deposit amount must be greater than zero.");
            return;
        }

        depositStatusLabel.setText("Processing secure deposit...");
        depositStatusLabel.setStyle("-fx-text-fill: #f59e0b;");

        final double depositAmount = amount;
        BackgroundLoader.loadAsync(
            () -> database.depositToInmateAccount(foundInmate.getInmateId(), depositAmount),
            success -> {
                if (!success) {
                    showDepositError("Deposit failed.");
                    return;
                }

                depositAmountField.clear();
                depositStatusLabel.setText(String.format("Deposit successful: %.2f", depositAmount));
                depositStatusLabel.setStyle("-fx-text-fill: #22c55e;");
                BalanceUpdateBus.publish();
                refreshFoundInmateBalance();
            },
            error -> showDepositError("Deposit failed: " + error.getMessage())
        );
    }

    private void refreshFoundInmateBalance() {
        if (foundInmate == null) {
            return;
        }

        BackgroundLoader.loadAsync(
            () -> database.getInmate(foundInmate.getInmateId()),
            inmate -> {
                if (inmate == null) {
                    return;
                }
                foundInmate = inmate;
                foundBalanceLabel.setText(String.format("Balance: %.2f", inmate.getBalance()));
            },
            error -> showDepositError("Refresh failed: " + error.getMessage())
        );
    }

    private void showDepositError(String message) {
        depositStatusLabel.setText(message);
        depositStatusLabel.setStyle("-fx-text-fill: #ef4444;");
    }
}