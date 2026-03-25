package com.prison.model;

public class RecentActivity {
    private final String timestamp;
    private final String action;
    private final String details;

    public RecentActivity(String timestamp, String action, String details) {
        this.timestamp = timestamp;
        this.action = action;
        this.details = details;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }
}