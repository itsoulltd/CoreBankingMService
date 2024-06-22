package com.infoworks.lab.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class MakeTransaction extends Transaction {
    @JsonIgnore private String type = "transfer";

    @NotNull(message = "to must not be null. Represent account_ref column of account table." +
            " Format: prefix@<username/account> e.g CASH@Master, REVENUE@Master, CASH@user-name, bKash@user-name, NAGAD@user-name")
    @Length(max = 20, min = 1, message = "to has to be 1<=length<=20")
    @Pattern(regexp = ".*@.*", message = "pattern of 'to' must be as follow: prefix@<username/account> e.g CASH@Master, REVENUE@Master, CASH@user-name, bKash@user-name, NAGAD@user-name")
    private String to;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getTo() {
        return to;
    }

    @Override
    public void setTo(String to) {
        this.to = to;
    }
}
