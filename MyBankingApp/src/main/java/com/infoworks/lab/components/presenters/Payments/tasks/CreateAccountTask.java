package com.infoworks.lab.components.presenters.Payments.tasks;

import com.infoworks.lab.beans.tasks.rest.client.base.BaseRequest;
import com.infoworks.lab.components.presenters.Payments.parser.VAccountResponseParser;
import com.infoworks.lab.domain.repository.VAccountRepository;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;

public class CreateAccountTask extends BaseRequest<Message, Response> {

    private VAccountRepository repository;
    private String accountName;
    private String accountPrefix;
    private String accountType;
    private String currency;
    private String amount;

    public CreateAccountTask(String accountName
            , String accountPrefix
            , String accountType
            , String currency
            , String amount) {
        this.accountName = accountName;
        this.accountPrefix = accountPrefix;
        this.accountType = accountType;
        this.currency = currency;
        this.amount = amount;
    }

    public VAccountRepository getRepository() {
        if (this.repository == null) {
            this.repository = new VAccountRepository();
        }
        return repository;
    }

    public void setRepository(VAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        Response response = getRepository().accountExist(accountPrefix, accountName);
        boolean isExist = VAccountResponseParser.isExist(response);
        if (!isExist) {
            Response newCreated = getRepository().createAccount(accountPrefix
                    , accountName
                    , currency
                    , amount
                    , accountType);
            return newCreated;
        }
        return response;
    }
}
