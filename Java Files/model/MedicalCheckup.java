package com.prison.model;

import java.time.LocalDate;

public class MedicalCheckup {
    private int checkupId;
    private int inmateId;
    private LocalDate checkupDate;
    private String diagnosis;
    private String treatment;
    private String doctorName;
    private String notes;

    public MedicalCheckup() {}

    public MedicalCheckup(int checkupId, int inmateId, LocalDate checkupDate, 
                         String diagnosis, String treatment, String doctorName) {
        this.checkupId = checkupId;
        this.inmateId = inmateId;
        this.checkupDate = checkupDate;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.doctorName = doctorName;
    }

    // Getters and Setters
    public int getCheckupId() { return checkupId; }
    public void setCheckupId(int checkupId) { this.checkupId = checkupId; }

    public int getInmateId() { return inmateId; }
    public void setInmateId(int inmateId) { this.inmateId = inmateId; }

    public LocalDate getCheckupDate() { return checkupDate; }
    public void setCheckupDate(LocalDate checkupDate) { this.checkupDate = checkupDate; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "Checkup ID: " + checkupId + " - Inmate: " + inmateId + " - " + checkupDate;
    }
}
