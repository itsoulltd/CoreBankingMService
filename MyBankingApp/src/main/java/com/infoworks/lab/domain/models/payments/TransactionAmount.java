package com.infoworks.lab.domain.models.payments;

import com.infoworks.lab.rest.validation.MoneyFormat.Money;

import javax.validation.constraints.NotBlank;

public class TransactionAmount {

    @NotBlank(message = "Money should not be null or empty")
    @Money(message = "E.g. 0.00 or 1.99 or 110.09 etc")
    private String money;

    public TransactionAmount(String money) {
        this.money = money;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }
}
