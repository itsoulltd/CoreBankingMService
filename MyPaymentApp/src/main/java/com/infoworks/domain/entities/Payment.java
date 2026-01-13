package com.infoworks.domain.entities;

import com.infoworks.domain.validation.constraint.CurrencyCode.IsValidCurrencyCode;
import com.infoworks.domain.validation.constraint.MoneyFormat.Money;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class Payment extends Persistable<Long, Long> {

    @Money(message = "amount has to be 0.00 or any combination with at least 2 digit after precision. e.g. 1002001.00 or 1200933.97 etc")
    private String amount = "0.00";

    @IsValidCurrencyCode(message = "currency is invalid. e.g. BDT, USD, EUR etc")
    private String currency;

    @Size(min = 1, max = 20, message = "fromAccount has to be 1<=length<=20. e.g. CASH@<Account-Name>")
    @Pattern(regexp = ".*@.*", message = "pattern of 'fromAccount' must be as follow: prefix@<username/account> e.g CASH@Master, REVENUE@Master, CASH@user-name, bKash@user-name, NAGAD@user-name")
    private String fromAccount;

    @Size(min = 1, max = 20, message = "toAccount has to be 1<=length<=20. e.g. CASH@<Account-Name>")
    @Pattern(regexp = ".*@.*", message = "pattern of 'toAccount' must be as follow: prefix@<username/account> e.g CASH@Master, REVENUE@Master, CASH@user-name, bKash@user-name, NAGAD@user-name")
    private String toAccount;

    public Payment() {}

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public boolean isAmountGreaterThan(BigDecimal val) {
        if (val == null) val = new BigDecimal("0.00");
        return new BigDecimal(getAmount()).compareTo(val) > 0;
    }

    public boolean isAmountGreaterThanZero() {
        return isAmountGreaterThan(new BigDecimal("0.00"));
    }

    public boolean isAmountZero() {
        return new BigDecimal(getAmount()).compareTo(new BigDecimal("0.00")) == 0;
    }

    public boolean isAmountLessThan(BigDecimal val) {
        if (val == null) val = new BigDecimal("0.00");
        return new BigDecimal(getAmount()).compareTo(val) < 0;
    }

    public boolean isAmountLessThanZero() {
        return isAmountLessThan(new BigDecimal("0.00"));
    }
}
