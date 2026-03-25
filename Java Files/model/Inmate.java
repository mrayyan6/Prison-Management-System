package com.prison.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Inmate {
    public static final double DEFAULT_STARTING_BALANCE = 500.0;

    private int inmateId;
    private String name;
    private int age;
    private String gender;
    private String crime;
    private LocalDate admissionDate;
    private LocalDate releaseDate;
    private LocalDate courtDate;
    private String status;
    private String cellNumber;
    private String behaviorRecord;
    private double balance = DEFAULT_STARTING_BALANCE;
    private LocalDateTime lastPhoneCallDate;
    private LocalDateTime lastFreePhoneCallDate;
    private LocalDateTime lastPaidPhoneCallDate;

    public Inmate() {}

    public Inmate(int inmateId, String name, int age, String gender, String crime, 
                  LocalDate admissionDate, LocalDate releaseDate, String status, String cellNumber) {
        this.inmateId = inmateId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.crime = crime;
        this.admissionDate = admissionDate;
        this.releaseDate = releaseDate;
        this.status = status;
        this.cellNumber = cellNumber;
        this.balance = DEFAULT_STARTING_BALANCE;
    }

    public Inmate(int inmateId, String name, int age, String gender, String crime,
                  LocalDate admissionDate, LocalDate releaseDate, String status, String cellNumber,
                  double balance, LocalDateTime lastPhoneCallDate,
                  LocalDateTime lastFreePhoneCallDate, LocalDateTime lastPaidPhoneCallDate) {
        this.inmateId = inmateId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.crime = crime;
        this.admissionDate = admissionDate;
        this.releaseDate = releaseDate;
        this.status = status;
        this.cellNumber = cellNumber;
        this.balance = balance;
        this.lastPhoneCallDate = lastPhoneCallDate;
        this.lastFreePhoneCallDate = lastFreePhoneCallDate;
        this.lastPaidPhoneCallDate = lastPaidPhoneCallDate;
    }

    // Getters and Setters
    public int getInmateId() { return inmateId; }
    public void setInmateId(int inmateId) { this.inmateId = inmateId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getCrime() { return crime; }
    public void setCrime(String crime) { this.crime = crime; }

    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public LocalDate getCourtDate() { return courtDate; }
    public void setCourtDate(LocalDate courtDate) { this.courtDate = courtDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCellNumber() { return cellNumber; }
    public void setCellNumber(String cellNumber) { this.cellNumber = cellNumber; }

    public String getBehaviorRecord() { return behaviorRecord; }
    public void setBehaviorRecord(String behaviorRecord) { this.behaviorRecord = behaviorRecord; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public LocalDateTime getLastPhoneCallDate() { return lastPhoneCallDate; }
    public void setLastPhoneCallDate(LocalDateTime lastPhoneCallDate) { this.lastPhoneCallDate = lastPhoneCallDate; }

    public LocalDateTime getLastFreePhoneCallDate() { return lastFreePhoneCallDate; }
    public void setLastFreePhoneCallDate(LocalDateTime lastFreePhoneCallDate) { this.lastFreePhoneCallDate = lastFreePhoneCallDate; }

    public LocalDateTime getLastPaidPhoneCallDate() { return lastPaidPhoneCallDate; }
    public void setLastPaidPhoneCallDate(LocalDateTime lastPaidPhoneCallDate) { this.lastPaidPhoneCallDate = lastPaidPhoneCallDate; }

    @Override
    public String toString() {
        return "ID: " + inmateId + " - " + name + " (" + status + ") - $" + String.format("%.2f", balance);
    }
}
