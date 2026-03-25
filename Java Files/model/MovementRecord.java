package com.prison.model;

import java.time.LocalDateTime;

public class MovementRecord {
    private int movementId;
    private int inmateId;
    private String fromLocation;
    private String toLocation;
    private LocalDateTime movementTime;
    private String reason;
    private String authorizedBy;

    public MovementRecord() {}

    public MovementRecord(int movementId, int inmateId, String fromLocation, 
                         String toLocation, LocalDateTime movementTime, String reason, String authorizedBy) {
        this.movementId = movementId;
        this.inmateId = inmateId;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.movementTime = movementTime;
        this.reason = reason;
        this.authorizedBy = authorizedBy;
    }

    // Getters and Setters
    public int getMovementId() { return movementId; }
    public void setMovementId(int movementId) { this.movementId = movementId; }

    public int getInmateId() { return inmateId; }
    public void setInmateId(int inmateId) { this.inmateId = inmateId; }

    public String getFromLocation() { return fromLocation; }
    public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }

    public String getToLocation() { return toLocation; }
    public void setToLocation(String toLocation) { this.toLocation = toLocation; }

    public LocalDateTime getMovementTime() { return movementTime; }
    public void setMovementTime(LocalDateTime movementTime) { this.movementTime = movementTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getAuthorizedBy() { return authorizedBy; }
    public void setAuthorizedBy(String authorizedBy) { this.authorizedBy = authorizedBy; }

    @Override
    public String toString() {
        return "Movement ID: " + movementId + " - Inmate: " + inmateId + " - " + fromLocation + " -> " + toLocation;
    }
}
