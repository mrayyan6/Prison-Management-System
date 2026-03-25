package com.prison.controller;

import com.prison.model.IncidentReport;
import com.prison.model.Inmate;
import com.prison.util.BackgroundLoader;
import com.prison.util.Database;
import com.prison.util.UiPerformanceUtil;
import com.prison.util.WindowManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VisitorIncidentReportsController {

    @FXML private ComboBox<Inmate> inmateFilterCombo;
    @FXML private ScrollPane reportsScrollPane;

    @FXML private TableView<IncidentReport> reportsTable;
    @FXML private TableColumn<IncidentReport, Integer> reportIdColumn;
    @FXML private TableColumn<IncidentReport, Integer> inmateIdColumn;
    @FXML private TableColumn<IncidentReport, String> typeColumn;
    @FXML private TableColumn<IncidentReport, LocalDateTime> dateColumn;
    @FXML private TableColumn<IncidentReport, String> reportedByColumn;
    @FXML private TableColumn<IncidentReport, String> releaseDateColumn;

    @FXML private Label statusLabel;

    private final Database database = Database.getInstance();
    private List<IncidentReport> allReports = new ArrayList<>();

    @FXML
    public void initialize() {
        reportIdColumn.setCellValueFactory(new PropertyValueFactory<>("reportId"));
        inmateIdColumn.setCellValueFactory(new PropertyValueFactory<>("inmateId"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("incidentType"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("incidentDate"));
        reportedByColumn.setCellValueFactory(new PropertyValueFactory<>("reportedBy"));
        releaseDateColumn.setCellValueFactory(new PropertyValueFactory<>("inmateReleaseDate"));

        UiPerformanceUtil.optimizeScrollPane(reportsScrollPane);
        UiPerformanceUtil.optimizeTableScrolling(reportsTable);

        inmateFilterCombo.setOnAction(event -> applyFilter());
        loadData();
    }

    @FXML
    private void clearFilter() {
        inmateFilterCombo.setValue(null);
        applyFilter();
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) reportsTable.getScene().getWindow();
        WindowManager.showDashboardForCurrentUser(stage, getClass());
    }

    private void loadData() {
        statusLabel.setText("Loading public safety records...");
        statusLabel.setStyle("-fx-text-fill: #cbd5e1;");

        BackgroundLoader.loadAsync(
            database::getAllInmates,
            inmates -> inmateFilterCombo.setItems(FXCollections.observableArrayList(inmates)),
            error -> showError("Failed to load inmate list: " + error.getMessage())
        );

        BackgroundLoader.loadAsync(
            database::getAllIncidentReports,
            reports -> {
                allReports = reports;
                applyFilter();
                statusLabel.setText("Public safety records loaded.");
                statusLabel.setStyle("-fx-text-fill: #22c55e;");
            },
            error -> showError("Failed to load reports: " + error.getMessage())
        );
    }

    private void applyFilter() {
        Inmate selected = inmateFilterCombo.getValue();
        if (selected == null) {
            reportsTable.setItems(FXCollections.observableArrayList(allReports));
            return;
        }

        List<IncidentReport> filtered = allReports.stream()
            .filter(report -> report.getInmateId() == selected.getInmateId())
            .collect(Collectors.toList());

        reportsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #ef4444;");
    }
}