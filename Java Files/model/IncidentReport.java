package com.prison.model;

import java.time.LocalDateTime;

public class IncidentReport {
    private int reportId;
    private int inmateId;
    private LocalDateTime incidentDate;
    private String incidentType;
    private String description;
    private String reportedBy;
    private String actionTaken;

    public IncidentReport() {}

    public IncidentReport(int reportId, int inmateId, LocalDateTime incidentDate, 
                         String incidentType, String description, String reportedBy) {
        this.reportId = reportId;
        this.inmateId = inmateId;
        this.incidentDate = incidentDate;
        this.incidentType = incidentType;
        this.description = description;
        this.reportedBy = reportedBy;
    }

    // Getters and Setters
    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getInmateId() { return inmateId; }
    public void setInmateId(int inmateId) { this.inmateId = inmateId; }

    public LocalDateTime getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDateTime incidentDate) { this.incidentDate = incidentDate; }

    public String getIncidentType() { return incidentType; }
    public void setIncidentType(String incidentType) { this.incidentType = incidentType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }

    @Override
    public String toString() {
        return "Report ID: " + reportId + " - " + incidentType + " - " + incidentDate;
    }
}
