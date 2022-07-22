package com.infoworks.lab.services.vaccount;

import com.infoworks.lab.domain.models.CreateAccount;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.services.ledger.LedgerBook;
import com.it.soul.lab.sql.query.models.Property;
import com.itsoul.lab.generalledger.entities.Money;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.math.BigDecimal;

public class BalanceEnquireTask extends CheckBalanceTask{

    public BalanceEnquireTask(LedgerBook book) {super(book);}

    public BalanceEnquireTask(LedgerBook book, String username, String prefix) {
        this(book, username, prefix, "0.00");
    }

    public BalanceEnquireTask(LedgerBook book, String username, String prefix, String enquireAmount) {
        super(book, new Property("username", username), new Property("prefix", prefix), new Property("amount", enquireAmount));
        this.enquireAmount = enquireAmount;
    }

    private String enquireAmount;
    protected String getEnquireAmount(){
        if (enquireAmount == null || enquireAmount.isEmpty()){
            try {
                CreateAccount account = Message.unmarshal(CreateAccount.class, getMessage().getPayload());
                enquireAmount = account.getAmount();
            } catch (IOException e) {}
        }
        return enquireAmount;
    }

    @Override
    public Message execute(Message message) throws RuntimeException {
        Response response = new Response().setStatus(HttpStatus.BAD_REQUEST.value());
        try {
            Money money = getLedgerBook().readBalance(getPrefix(), getUsername());
            BigDecimal available = money.getAmount();
            BigDecimal enquire = new BigDecimal(getEnquireAmount());
            if (available.compareTo(enquire) >= 0) // if-available-is-greater-then-or-equal-to-enquire return 1 or 0
                response.setStatus(HttpStatus.OK.value());
            else // if-available-is-less-then-enquire return -1
                response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setPayload(String.format("{\"balance\":\"%s\"}",money.getAmount().toPlainString()));
        } catch (Exception e) {
            response.setError(e.getMessage());
        }
        return response;
    }

    @Override
    public Message abort(Message message) throws RuntimeException {
        return new Response()
                .setError("Not Implemented")
                .setStatus(HttpStatus.BAD_REQUEST.value());
    }
}
