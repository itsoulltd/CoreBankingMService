package com.infoworks.components.presenters.Payments.view.models;

public enum TransactionType {

    Any("any")
    , Deposit("deposit")
    , Transfer("transfer")
    , Withdrawal("withdrawal");

    private String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
