package com.infoworks.lab.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infoworks.lab.domain.validation.constraint.AccountType.IsValidAccountType;
import com.infoworks.lab.domain.validation.constraint.CurrencyCode.IsValidCurrencyCode;
import com.infoworks.lab.domain.validation.constraint.MoneyFormat.Money;
import com.infoworks.lab.rest.models.Response;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreateAccount extends Response {

    @NotNull(message = "prefix must not be null or empty! e.g CASH, REVENUE, bKash, NAGAD etc")
    @Length(max = 6, min = 1, message = "type has to be 1<=length<=6")
    private String prefix;

    @NotNull(message = "amount has to be not null, you may pass default zero amount. e.g. 0.00 ")
    @Money(message = "amount has to be 0.00 or any combination with at least 2 digit after precision. e.g. 1002001.00 or 1200933.97 etc")
    private String amount;

    @NotNull(message = "currency should not be null. e.g. BDT, USD, EUR etc")
    @IsValidCurrencyCode(message = "currency is invalid. e.g. BDT, USD, EUR etc")
    private String currency;

    @NotNull(message = "accountType must not be null or empty!")
    @IsValidAccountType(message = "accountType = MASTER or USER")
    private String accountType;

    @JsonIgnore
    private String username;

    public String getPrefix() {
        return prefix;
    }

    public CreateAccount setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public CreateAccount setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getAmount() {
        return amount;
    }

    public CreateAccount setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public BigDecimal convertAmount(){
        if (isAmountValid())
            return new BigDecimal(getAmount());
        return new BigDecimal("0.00");
    }

    @JsonIgnore
    public boolean isAmountValid() {
        if (getAmount() == null) return false;
        try {
            BigDecimal decimal = new BigDecimal(getAmount());
            setAmount(decimal.toPlainString());
            return true;
        } catch (Exception e) {}
        return false;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
