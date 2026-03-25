package com.prison.model;

import java.time.LocalDate;

public class WorkAssignment {
    private int assignmentId;
    private int inmateId;
    private String workType;
    private LocalDate assignedDate;
    private String status;
    private String supervisorName;

    public WorkAssignment() {}

    public WorkAssignment(int assignmentId, int inmateId, String workType, 
                         LocalDate assignedDate, String status, String supervisorName) {
        this.assignmentId = assignmentId;
        this.inmateId = inmateId;
        this.workType = workType;
        this.assignedDate = assignedDate;
        this.status = status;
        this.supervisorName = supervisorName;
    }

    // Getters and Setters
    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public int getInmateId() { return inmateId; }
    public void setInmateId(int inmateId) { this.inmateId = inmateId; }

    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }

    public LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }

    @Override
    public String toString() {
        return "Assignment ID: " + assignmentId + " - Inmate: " + inmateId + " - " + workType;
    }
}
