package com.infoworks.lab.domain.models;

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
}
