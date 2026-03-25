package com.prison.model;
import java.time.LocalDateTime;
import com.prison.model.User;


 public class Admin implements User {

    private int userId;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String role = "ADMIN";
    private LocalDateTime createdAt;
    private String status;

    public Admin() {}

    public Admin(int userId, String username, String password, String email,
                 String fullName, LocalDateTime createdAt, String status) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.createdAt = createdAt;
        this.status = status;
    }

    @Override
    public void showPermissions() {
        System.out.println("ADMIN: Full system control — manage inmates, wardens, visitors, parole, etc.");
    }

    public int getUserId() { return userId; }
    public void setUserId(int id) { this.userId = id; }

    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }

    public String getPassword() { return password; }
    public void setPassword(String p) { this.password = p; }

    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }

    public String getFullName() { return fullName; }
    public void setFullName(String n) { this.fullName = n; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }

    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    
}

