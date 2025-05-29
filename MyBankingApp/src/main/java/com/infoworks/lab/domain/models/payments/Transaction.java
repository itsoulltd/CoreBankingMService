package com.infoworks.lab.domain.models.payments;

import com.infoworks.lab.rest.models.events.Event;

import java.time.LocalDate;
import java.util.Objects;

/**
 * {transaction_date=2024-08-24
 * , amount=4.9
 * , account_ref=CASH@rider_2runner
 * , balance=375.0
 * , currency=BDT
 * , transaction_type=transfer
 * , transaction_ref=230b5127-273c-40e8}
 */
public class Transaction extends Event {

    private Double amount;
    private Double balance;
    private String account_ref;
    private String currency;
    private String transaction_ref;
    private String transaction_type;
    private String transaction_date;

    public Transaction() {}

    public String getAccount_ref() {
        return account_ref;
    }

    public void setAccount_ref(String account_ref) {
        this.account_ref = account_ref;
    }

    public String getAccountName() {
        if (getAccount_ref() == null || getAccount_ref().isEmpty()) return null;
        String[] parsed = getAccount_ref().split("@");
        return parsed.length > 1 ? parsed[1] : null;
    }

    public String getAccountPrefix() {
        if (getAccount_ref() == null || getAccount_ref().isEmpty()) return null;
        String[] parsed = getAccount_ref().split("@");
        return parsed.length > 0 ? parsed[0] : null;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTransaction_ref() {
        return transaction_ref;
    }

    public void setTransaction_ref(String transaction_ref) {
        this.transaction_ref = transaction_ref;
    }

    public String getTransaction_type() {
        return transaction_type;
    }

    public void setTransaction_type(String transaction_type) {
        this.transaction_type = transaction_type;
    }

    public String getTransaction_date() {
        return transaction_date;
    }

    public void setTransaction_date(String transaction_date) {
        this.transaction_date = transaction_date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Transaction that = (Transaction) o;
        return transaction_ref.equals(that.transaction_ref);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), transaction_ref);
    }

    public LocalDate getTransactionLocalDate() {
        if (transaction_date != null && !transaction_date.isEmpty()) {
            return LocalDate.parse(transaction_date);
        }
        return LocalDate.now();
    }
}
