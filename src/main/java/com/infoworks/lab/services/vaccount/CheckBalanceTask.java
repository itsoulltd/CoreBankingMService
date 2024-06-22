package com.infoworks.lab.services.vaccount;

import com.infoworks.lab.domain.models.CreateAccount;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.services.ledger.LedgerBook;
import com.it.soul.lab.sql.query.models.Property;
import com.itsoul.lab.generalledger.entities.Money;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class CheckBalanceTask extends LedgerTask {

    public CheckBalanceTask(LedgerBook book) {super(book);}

    private String username;
    private String prefix;

    public String getUsername() {
        if (username == null){
            try {
                CreateAccount account = Message.unmarshal(CreateAccount.class, getMessage().getPayload());
                username = account.getUsername();
            } catch (IOException e) {}
        }
        return username;
    }

    public String getPrefix() {
        if (prefix == null){
            try {
                CreateAccount account = Message.unmarshal(CreateAccount.class, getMessage().getPayload());
                prefix = account.getPrefix();
            } catch (IOException e) {}
        }
        return prefix;
    }

    public CheckBalanceTask(LedgerBook book, String username, String prefix) {
        this(book, new Property("username", username), new Property("prefix", prefix));
        this.username = username;
        this.prefix = prefix;
    }

    public CheckBalanceTask(LedgerBook book, Property... properties) {
        super(book, properties);
    }

    @Override
    public Message execute(Message message) throws RuntimeException {
        Response response = new Response()
                .setError("Not Implemented")
                .setStatus(HttpStatus.BAD_REQUEST.value());
        try{
            //
            String cash_account = LedgerBook.getACNo(getUsername(), getPrefix());
            Money money = getLedgerBook().readBalance(getPrefix(), getUsername());
            response.setPayload(
                    String.format("{\"title\":\"%s\",\"balance\":\"%s\",\"currency\":\"%s\",\"currencyDisplayName\":\"%s\"}"
                    , cash_account
                    , money.getAmount().toPlainString()
                    , money.getCurrency().getCurrencyCode()
                    , money.getCurrency().getDisplayName()));
            //
            return response.setError(null)
                    .setStatus(HttpStatus.OK.value());
            //
        }catch (Exception e){
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
