package com.prison.model;

public class InmateFinancialRecord {
    private int inmateId;
    private String inmateName;
    private double balance;

    public InmateFinancialRecord() {
    }

    public InmateFinancialRecord(int inmateId, String inmateName, double balance) {
        this.inmateId = inmateId;
        this.inmateName = inmateName;
        this.balance = balance;
    }

    public int getInmateId() {
        return inmateId;
    }

    public void setInmateId(int inmateId) {
        this.inmateId = inmateId;
    }

    public String getInmateName() {
        return inmateName;
    }

    public void setInmateName(String inmateName) {
        this.inmateName = inmateName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}