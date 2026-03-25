package com.prison.controller;

import com.prison.model.InmateFinancialRecord;
import com.prison.util.BalanceUpdateBus;
import com.prison.util.BackgroundLoader;
import com.prison.util.Database;
import com.prison.util.UiPerformanceUtil;
import com.prison.util.WindowManager;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class FinancialServicesController {

    @FXML private ScrollPane balancesScrollPane;
    @FXML private TableView<InmateFinancialRecord> balancesTable;
    @FXML private TableColumn<InmateFinancialRecord, Integer> inmateIdColumn;
    @FXML private TableColumn<InmateFinancialRecord, String> inmateNameColumn;
    @FXML private TableColumn<InmateFinancialRecord, Double> balanceColumn;

    @FXML private TextField inmateIdSearchField;
    @FXML private TextField depositAmountField;
    @FXML private Label selectedInmateLabel;
    @FXML private Label currentBalanceLabel;
    @FXML private Label statusLabel;

    private final Database database = Database.getInstance();
    private InmateFinancialRecord selectedRecord;

    private final ChangeListener<Number> balanceListener = (obs, oldVal, newVal) -> loadBalances();

    @FXML
    public void initialize() {
        inmateIdColumn.setCellValueFactory(new PropertyValueFactory<>("inmateId"));
        inmateNameColumn.setCellValueFactory(new PropertyValueFactory<>("inmateName"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));

        UiPerformanceUtil.optimizeScrollPane(balancesScrollPane);
        UiPerformanceUtil.optimizeTableScrolling(balancesTable);

        balancesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedRecord = newValue;
            updateSelectedInmateDetails();
        });

        BalanceUpdateBus.versionProperty().addListener(balanceListener);
        loadBalances();
    }

    @FXML
    private void depositFunds() {
        if (selectedRecord == null) {
            showError("Select an inmate account first.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(depositAmountField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Enter a valid deposit amount.");
            return;
        }

        if (amount <= 0) {
            showError("Deposit amount must be greater than zero.");
            return;
        }

        statusLabel.setText("Processing deposit...");
        statusLabel.setStyle("-fx-text-fill: #f59e0b;");

        final double depositAmount = amount;
        BackgroundLoader.loadAsync(
            () -> database.depositToInmateAccount(selectedRecord.getInmateId(), depositAmount),
            success -> {
                if (success) {
                    statusLabel.setText(String.format("Deposit successful: %.2f", depositAmount));
                    statusLabel.setStyle("-fx-text-fill: #22c55e;");
                    depositAmountField.clear();
                    BalanceUpdateBus.publish();
                    loadBalances();
                } else {
                    showError("Deposit failed.");
                }
            },
            error -> showError("Deposit failed: " + error.getMessage())
        );
    }

    @FXML
    private void refreshBalances() {
        loadBalances();
    }

    @FXML
    private void searchInmateById() {
        String idText = inmateIdSearchField.getText().trim();
        int inmateId;

        try {
            inmateId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showError("Enter a valid inmate ID.");
            return;
        }

        InmateFinancialRecord record = balancesTable.getItems().stream()
            .filter(item -> item.getInmateId() == inmateId)
            .findFirst()
            .orElse(null);

        if (record == null) {
            showError("No inmate found with ID " + inmateId + ".");
            return;
        }

        balancesTable.getSelectionModel().select(record);
        balancesTable.scrollTo(record);
        selectedRecord = record;
        updateSelectedInmateDetails();
        statusLabel.setText("Inmate found.");
        statusLabel.setStyle("-fx-text-fill: #22c55e;");
    }

    @FXML
    private void goBack() {
        BalanceUpdateBus.versionProperty().removeListener(balanceListener);
        Stage stage = (Stage) balancesTable.getScene().getWindow();
        WindowManager.showDashboardForCurrentUser(stage, getClass());
    }

    private void loadBalances() {
        statusLabel.setText("Loading inmate balances...");
        statusLabel.setStyle("-fx-text-fill: #cbd5e1;");

        BackgroundLoader.loadAsync(
            database::getAllInmateFinancialRecords,
            records -> {
                balancesTable.setItems(FXCollections.observableArrayList(records));
                if (selectedRecord != null) {
                    selectedRecord = records.stream()
                        .filter(record -> record.getInmateId() == selectedRecord.getInmateId())
                        .findFirst()
                        .orElse(null);

                    if (selectedRecord != null) {
                        balancesTable.getSelectionModel().select(selectedRecord);
                    }
                }
                updateSelectedInmateDetails();
                statusLabel.setText("Balances loaded.");
                statusLabel.setStyle("-fx-text-fill: #22c55e;");
            },
            error -> showError("Failed to load balances: " + error.getMessage())
        );
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #ef4444;");
    }

    private void updateSelectedInmateDetails() {
        if (selectedRecord == null) {
            selectedInmateLabel.setText("Selected Inmate: None");
            currentBalanceLabel.setText("Current Balance: --");
            return;
        }

        selectedInmateLabel.setText("Selected Inmate: " + selectedRecord.getInmateName() + " (ID " + selectedRecord.getInmateId() + ")");
        currentBalanceLabel.setText(String.format("Current Balance: %.2f", selectedRecord.getBalance()));
    }
}