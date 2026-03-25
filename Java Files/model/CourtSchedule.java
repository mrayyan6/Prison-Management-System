package com.prison.model;

import java.time.LocalDateTime;

public class CourtSchedule {
    private int scheduleId;
    private int inmateId;
    private LocalDateTime courtTime;
    private String scheduledBy;

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getInmateId() {
        return inmateId;
    }

    public void setInmateId(int inmateId) {
        this.inmateId = inmateId;
    }

    public LocalDateTime getCourtTime() {
        return courtTime;
    }

    public void setCourtTime(LocalDateTime courtTime) {
        this.courtTime = courtTime;
    }

    public String getScheduledBy() {
        return scheduledBy;
    }

    public void setScheduledBy(String scheduledBy) {
        this.scheduledBy = scheduledBy;
    }
}