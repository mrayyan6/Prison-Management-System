package com.prison.model;

public class Resources {
    private int resourceId;
    private String resourceName;
    private String category;
    private int quantity;
    private String location;
    private String status;

    public Resources() {}

    public Resources(int resourceId, String resourceName, String category, 
                    int quantity, String location, String status) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.category = category;
        this.quantity = quantity;
        this.location = location;
        this.status = status;
    }

    // Getters and Setters
    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "ID: " + resourceId + " - " + resourceName + " (Qty: " + quantity + ")";
    }
}
