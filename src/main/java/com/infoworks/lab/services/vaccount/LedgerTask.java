package com.infoworks.lab.services.vaccount;

import com.infoworks.lab.beans.tasks.nuts.AbstractTask;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.services.ledger.LedgerBook;
import com.it.soul.lab.sql.query.models.Property;

import java.util.function.Function;

public abstract class LedgerTask extends AbstractTask {

    public LedgerTask(LedgerBook ledgerBook) {
        this(ledgerBook, new Message());
    }

    public LedgerTask(LedgerBook ledgerBook, Message message) {
        this(ledgerBook, message, null);
    }

    public LedgerTask(LedgerBook ledgerBook, Message message, Function<Message, Message> converter) {
        super(message, converter);
        this.ledgerBook = ledgerBook;
    }

    public LedgerTask(LedgerBook ledgerBook, Property... properties) {
        this(ledgerBook, properties, null);
    }

    public LedgerTask(LedgerBook ledgerBook, Property[] properties, Function<Message, Message> converter) {
        super(properties, converter);
        this.ledgerBook = ledgerBook;
    }

    private LedgerBook ledgerBook;
    protected LedgerBook getLedgerBook(){
        return ledgerBook;
    }

    private String appID;
    protected String getAppID(){
        if (appID == null){
            appID = System.getenv("com.itsoul.lab.api.appid");
        }
        return appID;
    }
    public void setAppID(String appID) {
        this.appID = appID;
    }

    private String publicDns;
    protected String getPublicDns(){
        if (publicDns == null){
            publicDns = System.getenv("com.itsoul.lab.api.public.dns");
        }
        return publicDns;
    }
    public void setPublicDns(String publicDns) {
        this.publicDns = publicDns;
    }

    private String ledgerBookUser;
    protected String getLedgerBookUser(){
        if (ledgerBookUser == null){
            ledgerBookUser = System.getenv("app.ledger.book.username");
        }
        return ledgerBookUser;
    }
    public void setLedgerBookUser(String ledgerBookUser) {
        this.ledgerBookUser = ledgerBookUser;
    }

    private String ledgerBookPassword;
    protected String getLedgerBookPassword(){
        if (ledgerBookPassword == null){
            ledgerBookPassword = System.getenv("app.ledger.book.password");
        }
        return ledgerBookPassword;
    }
    public void setLedgerBookPassword(String ledgerBookPassword) {
        this.ledgerBookPassword = ledgerBookPassword;
    }

    private String currency;
    protected String getCurrency(){
        if (currency == null){
            currency = System.getenv("app.ledger.book.currency");
        }
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }

}
