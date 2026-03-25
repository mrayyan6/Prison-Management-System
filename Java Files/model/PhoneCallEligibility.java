package com.prison.model;

public class PhoneCallEligibility {
    private final boolean freeCallAvailable;
    private final boolean paidCallAvailable;
    private final boolean maxWeeklyCallsReached;

    public PhoneCallEligibility(boolean freeCallAvailable, boolean paidCallAvailable) {
        this.freeCallAvailable = freeCallAvailable;
        this.paidCallAvailable = paidCallAvailable;
        this.maxWeeklyCallsReached = !freeCallAvailable && !paidCallAvailable;
    }

    public boolean isFreeCallAvailable() {
        return freeCallAvailable;
    }

    public boolean isPaidCallAvailable() {
        return paidCallAvailable;
    }

    public boolean isMaxWeeklyCallsReached() {
        return maxWeeklyCallsReached;
    }
}