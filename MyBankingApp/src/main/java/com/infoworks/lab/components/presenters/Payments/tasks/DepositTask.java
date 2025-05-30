package com.infoworks.lab.components.presenters.Payments.tasks;

import com.infoworks.lab.beans.tasks.rest.client.base.BaseRequest;
import com.infoworks.lab.components.presenters.Payments.parser.VAccountResponseParser;
import com.infoworks.lab.domain.repository.VAccountRepository;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;

public class DepositTask extends BaseRequest<Message, Response> {

    private VAccountRepository repository;
    private String accountName;
    private String accountPrefix;
    private String accountTitle;
    private String currency;
    private String amount;
    private boolean shouldReverseIfExecuted = false;

    public DepositTask(String accountName
            , String accountPrefix
            , String currency
            , String amount) {
        this.accountName = accountName;
        this.accountPrefix = accountPrefix;
        this.currency = currency;
        this.amount = amount;
        this.accountTitle = accountPrefix + "@" + accountName;
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
        //Check account existence:
        Response response = getRepository().accountExist(accountPrefix, accountName);
        boolean isExist = VAccountResponseParser.isExist(response);
        //Make Deposit:
        try {
            if (isExist) {
                response = repository.makeDeposit(accountName, currency, amount);
                if (response.getStatus() == 200) {
                    this.shouldReverseIfExecuted = true;
                    return response;
                } else {
                    throw new Exception(accountTitle + " : " + amount + " (" + currency + ") Deposit Failed!");
                }
            } else {
                throw new Exception(accountTitle + " : " + "Not Found!");
            }
        } catch (Exception ex) {
            this.shouldReverseIfExecuted = false;
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Response abort(Message message) throws RuntimeException {
        if (shouldReverseIfExecuted) {
            //Check account existence:
            Response response = getRepository().accountExist(accountPrefix, accountName);
            boolean isExist = VAccountResponseParser.isExist(response);
            //Reverse Deposit or Make Withdrawal:
            if (isExist) {
                response = repository.makeWithdrawal(accountName, currency, amount);
                if (response.getStatus() == 200) {
                    return response;
                } else {
                    return response.setStatus(500)
                            .setError(accountTitle + " : " + amount + " (" + currency + ") Withdrawal Failed!");
                }
            } else {
                return response.setStatus(404).setError(accountTitle + " : " + "Not Found!");
            }
        }
        return super.abort(message);
    }
}
