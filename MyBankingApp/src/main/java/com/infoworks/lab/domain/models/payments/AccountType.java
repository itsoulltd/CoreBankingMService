package com.infoworks.lab.domain.models.payments;

public enum AccountType {
    MASTER("Master"),
    USER("User"),
    CURRENT("Current"),
    SAVING("Saving"),
    PAYABLE("Payable"),
    RECEIVABLE("Receivable");

    private String value;

    AccountType(String value) {
        this.value = value;
    }

    public String value(){
        return value;
    }
}
