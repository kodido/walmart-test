package com.walmart.seatschallenge.solution.api.model;

public class NewReservationPayload {

    private int holdId;

    private String customerEmail;

    public int getHoldId() {
        return holdId;
    }

    public void setHoldId(int holdId) {
        this.holdId = holdId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

}
