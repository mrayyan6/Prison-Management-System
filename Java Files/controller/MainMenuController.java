package com.prison.controller;

import com.prison.model.RecentActivity;
import com.prison.model.User;
import com.prison.util.BackgroundLoader;
import com.prison.util.Database;
import com.prison.util.SessionManager;
import com.prison.util.SystemUpdateBus;
import com.prison.util.UiPerformanceUtil;
import com.prison.util.WindowManager;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class MainMenuController {

    @FXML private VBox sidebar;
    @FXML private Label welcomeLabel;

    @FXML private Button registerInmateBtn;
    @FXML private Button manageStaffBtn;
    @FXML private Button assignWorkBtn;
    @FXML private Button manageResourcesBtn;
    @FXML private Button medicalCheckupBtn;
    @FXML private Button monitorAttendanceBtn;
    @FXML private Button incidentReportBtn;
    @FXML private Button recordMovementBtn;
    @FXML private Button releaseInmateBtn;
    @FXML private Button commissaryBtn;
    @FXML private Button predictiveRiskBtn;

    @FXML private Button financialServicesBtn;
    @FXML private Button publicIncidentReportsBtn;

    @FXML private Label inmateCountValue;
    @FXML private Label alertsValue;
    @FXML private Label courtPendingValue;
    @FXML private Label capacityValue;

    @FXML private TableView<RecentActivity> recentActivityTable;
    @FXML private TableColumn<RecentActivity, String> activityTimeColumn;
    @FXML private TableColumn<RecentActivity, String> activityActionColumn;
    @FXML private TableColumn<RecentActivity, String> activityDetailsColumn;

    private final Database database = Database.getInstance();
    private final ChangeListener<Number> systemUpdateListener = (obs, oldVal, newVal) -> loadDashboardData();

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName() + " (" + user.getRole() + ")");
        } else {
            welcomeLabel.setText("Welcome");
        }

        applyRoleBasedNavigation();
        UiPerformanceUtil.enableBufferedRendering(sidebar);
        setupActivityTable();
        loadDashboardData();
        SystemUpdateBus.versionProperty().addListener(systemUpdateListener);
    }

    private void setupActivityTable() {
        activityTimeColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        activityActionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        activityDetailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        UiPerformanceUtil.optimizeTableScrolling(recentActivityTable);
    }

    private void loadDashboardData() {
        BackgroundLoader.loadAsync(
            () -> {
                DashboardSnapshot snapshot = new DashboardSnapshot();
                snapshot.inmateCount = database.getActiveInmateCount();
                snapshot.alerts = database.getAlertsCount();
                snapshot.pendingCourtCases = database.getPendingCourtCasesCount();
                snapshot.capacity = database.getCapacitySnapshot();
                snapshot.activities = database.getRecentActivities(10);
                return snapshot;
            },
            snapshot -> {
                inmateCountValue.setText(String.valueOf(snapshot.inmateCount));
                alertsValue.setText(String.valueOf(snapshot.alerts));
                courtPendingValue.setText(String.valueOf(snapshot.pendingCourtCases));
                capacityValue.setText(snapshot.capacity);
                recentActivityTable.getItems().setAll(snapshot.activities);
            },
            error -> {
                inmateCountValue.setText("Error");
                alertsValue.setText("Error");
                courtPendingValue.setText("Error");
                capacityValue.setText("Error");
            }
        );
    }

    private void applyRoleBasedNavigation() {
        User user = SessionManager.getCurrentUser();
        String role = user == null || user.getRole() == null ? "" : user.getRole().trim();

        boolean isAdmin = role.equalsIgnoreCase("Administrator") || role.equalsIgnoreCase("Admin");
        boolean isWarden = role.equalsIgnoreCase("Warden");
        boolean isVisitor = role.equalsIgnoreCase("Visitor");

        // Admin modules
        setVisibleManaged(registerInmateBtn, isAdmin);
        setVisibleManaged(manageStaffBtn, isAdmin);
        setVisibleManaged(manageResourcesBtn, isAdmin);
        setVisibleManaged(commissaryBtn, isAdmin);
        setVisibleManaged(monitorAttendanceBtn, isAdmin);
        setVisibleManaged(assignWorkBtn, isAdmin);
        setVisibleManaged(medicalCheckupBtn, isAdmin);
        setVisibleManaged(recordMovementBtn, isAdmin);


        // Warden modules
        setVisibleManaged(predictiveRiskBtn, isWarden);
        setVisibleManaged(releaseInmateBtn, isWarden);
        setVisibleManaged(incidentReportBtn, isWarden);

        // Visitor modules
        setVisibleManaged(financialServicesBtn, isVisitor);
        setVisibleManaged(publicIncidentReportsBtn, isVisitor);

        // Hidden from strict role mapping
    }

    private void setVisibleManaged(Button button, boolean visible) {
        if (button == null) {
            return;
        }
        button.setVisible(visible);
        button.setManaged(visible);
    }
    
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
    private void openCommissary() {
        loadScene("Commissary.fxml", "Financial & Commissary Management");
    }

    @FXML
    private void openPredictiveRisk() {
        loadScene("PredictiveRisk.fxml", "Predictive Risk Analysis");
    }

    @FXML
    private void openFinancialServices() {
        loadScene("FinancialServices.fxml", "Financial Services");
    }

    @FXML
    private void openVisitorIncidentReports() {
        loadScene("VisitorIncidentReports.fxml", "Public Incident Reports");
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
    private void handleLogout() {
        try {
            SessionManager.clear();
            SystemUpdateBus.versionProperty().removeListener(systemUpdateListener);
            Stage stage = (Stage) sidebar.getScene().getWindow();
            WindowManager.switchScene(stage, getClass(), "/fxml/Login.fxml", "Login - Prison Management System");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshDashboard() {
        loadDashboardData();
    }
    
    private void loadScene(String fxmlFile, String title) {
        try {
            Stage stage = (Stage) sidebar.getScene().getWindow();
            WindowManager.switchScene(stage, getClass(), "/fxml/" + fxmlFile, title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class DashboardSnapshot {
        int inmateCount;
        int alerts;
        int pendingCourtCases;
        String capacity;
        java.util.List<RecentActivity> activities;
    }
}