package com.prison.model;

import java.time.LocalDate;

public class Inmate {
    private int inmateId;
    private String name;
    private int age;
    private String gender;
    private String crime;
    private LocalDate admissionDate;
    private LocalDate releaseDate;
    private String status;
    private String cellNumber;
    private String behaviorRecord;

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCellNumber() { return cellNumber; }
    public void setCellNumber(String cellNumber) { this.cellNumber = cellNumber; }

    public String getBehaviorRecord() { return behaviorRecord; }
    public void setBehaviorRecord(String behaviorRecord) { this.behaviorRecord = behaviorRecord; }

    @Override
    public String toString() {
        return "ID: " + inmateId + " - " + name + " (" + status + ")";
    }
}
