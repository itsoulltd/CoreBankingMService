package com.infoworks.components.presenters.Payments.tasks;

import com.infoworks.tasks.ExecutableTask;
import com.infoworks.components.presenters.Payments.parser.VAccountResponseParser;
import com.infoworks.domain.repositories.VAccountRepository;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;

public class CreateAccountTask extends ExecutableTask<Message, Response> {

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
