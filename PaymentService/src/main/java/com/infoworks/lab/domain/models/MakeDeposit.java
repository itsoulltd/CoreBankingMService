package com.infoworks.lab.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MakeDeposit extends Transaction {
    @JsonIgnore private String prefix;
    @JsonIgnore private String to;
    @JsonIgnore private String type = "deposit";
    @JsonIgnore private String from;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
