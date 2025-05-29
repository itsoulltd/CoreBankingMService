package com.infoworks.lab.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infoworks.lab.rest.models.Response;

public class ChartOfAccountResponse extends Response {

    private String accountRef;

    public String getAccountRef() {
        return accountRef;
    }

    public Response setAccountTitle(String accountTitle) {
        this.accountRef = accountTitle;
        return this;
    }

    @JsonIgnore private String payload;
    @JsonIgnore private Integer status = 200;
    @JsonIgnore private String error;
    @JsonIgnore private String message;
}
