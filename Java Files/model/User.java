package com.prison.model;

import java.time.LocalDateTime;

public interface User {

    int getUserId();
    void setUserId(int userId);

    String getUsername();
    void setUsername(String username);

    String getPassword();
    void setPassword(String password);

    String getEmail();
    void setEmail(String email);

    String getFullName();
    void setFullName(String fullName);

    String getRole();
    void setRole(String role);

    LocalDateTime getCreatedAt();
    void setCreatedAt(LocalDateTime time);

    String getStatus();
    void setStatus(String status);

    default void showPermissions() {
        System.out.println("Generic user — no specific permissions defined.");
    }

}



// class Visitor implements User {

//     private int userId;
//     private String username;
//     private String password;
//     private String email;
//     private String fullName;
//     private String role = "VISITOR";
//     private LocalDateTime createdAt;
//     private String status;

//     private int inmateNumber; 

//     public Visitor() {}

//     public Visitor(int userId, String username, String password, String email,
//                    String fullName, int inmateNumber, LocalDateTime createdAt, String status) {
//         this.userId = userId;
//         this.username = username;
//         this.password = password;
//         this.email = email;
//         this.fullName = fullName;
//         this.inmateNumber = inmateNumber;
//         this.createdAt = createdAt;
//         this.status = status;
//     }

//     public int getInmateNumber() { return inmateNumber; }
//     public void setInmateNumber(int inmateNumber) { this.inmateNumber = inmateNumber; }

//     @Override
//     public void showPermissions() {
//         System.out.println("VISITOR: Can only visit inmate #" + inmateNumber);
//     }

//     public int getUserId() { return userId; }
//     public void setUserId(int id) { this.userId = id; }

//     public String getUsername() { return username; }
//     public void setUsername(String u) { this.username = u; }

//     public String getPassword() { return password; }
//     public void setPassword(String p) { this.password = p; }

//     public String getEmail() { return email; }
//     public void setEmail(String e) { this.email = e; }

//     public String getFullName() { return fullName; }
//     public void setFullName(String n) { this.fullName = n; }

//     public String getRole() { return role; }
//     public void setRole(String role) { this.role = role; }

//     public LocalDateTime getCreatedAt() { return createdAt; }
//     public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }

//     public String getStatus() { return status; }
//     public void setStatus(String s) { this.status = s; }

//     @Override
//     public String toString() {
//         return "Visitor{" +
//                 "userId=" + userId +
//                 ", username='" + username + '\'' +
//                 ", visiting inmate=" + inmateNumber +
//                 '}';
//     }
// }

// class Warden implements User {

//     private int userId;
//     private String username;
//     private String password;
//     private String email;
//     private String fullName;
//     private String role = "WARDEN";
//     private LocalDateTime createdAt;
//     private String status;

//     public void requestParole(int inmateNumber) {
//         System.out.println("WARDEN: Requesting parole hearing for inmate #" + inmateNumber + " (admin approval needed).");
//     }

//     public Warden() {}

//     public Warden(int userId, String username, String password, String email,
//                   String fullName, LocalDateTime createdAt, String status) {
//         this.userId = userId;
//         this.username = username;
//         this.password = password;
//         this.email = email;
//         this.fullName = fullName;
//         this.createdAt = createdAt;
//         this.status = status;
//     }

//     @Override
//     public void showPermissions() {
//         System.out.println("WARDEN: Can request parole hearings only.");
//     }
//     public String getUsername() { return username; }
//     public void setUsername(String u) { this.username = u; }

//     public String getPassword() { return password; }
//     public void setPassword(String p) { this.password = p; }

//     public String getEmail() { return email; }
//     public void setEmail(String e) { this.email = e; }

//     public String getFullName() { return fullName; }
//     public void setFullName(String n) { this.fullName = n; }

//     public String getRole() { return role; }
//     public void setRole(String role) { this.role = role; }

//     public LocalDateTime getCreatedAt() { return createdAt; }
//     public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }

//     public String getStatus() { return status; }
//     public void setStatus(String s) { this.status = s; }

//     @Override
//     public String toString() {
//         return "Warden{" +
//                 "userId=" + userId +
//                 ", username='" + username + '\'' +
//                 '}';
//     }
// }
