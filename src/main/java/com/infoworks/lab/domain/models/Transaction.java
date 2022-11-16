package com.infoworks.lab.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.Length;

public class Transaction extends CreateAccount {
    @JsonIgnore private String accountType;

    //@NotNull(message = "type must not be null. Any string less-then 20 char. e.g. transaction, purchase, transfer etc")
    //@Length(max = 20, min = 1, message = "type has to be 1<=length<=20")
    private String type;

    /**
     * If null then, we will generate this.
     */
    @JsonIgnore
    @Length(max = 20, min = 1, message = "ref has to be 1<=length<=20")
    private String ref;

    /**
     * from could be null, because we can reconstruct it from token's embedded userID/issuer/
     */
    @JsonIgnore
    @Length(max = 20, min = 1, message = "from has to be 1<=length<=20")
    private String from;

    private String to;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String getAccountType() {
        return accountType;
    }

    @Override
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
