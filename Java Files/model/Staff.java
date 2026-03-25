package com.prison.model;

public class Staff {
    private int staffId;
    private String name;
    private String role;
    private String shift;
    private String contactInfo;
    private String username;
    private String password;

    public Staff() {}

    public Staff(int staffId, String name, String role, String shift, String contactInfo) {
        this.staffId = staffId;
        this.name = name;
        this.role = role;
        this.shift = shift;
        this.contactInfo = contactInfo;
    }

    // Getters and Setters
    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "ID: " + staffId + " - " + name + " (" + role + ")";
    }
}
