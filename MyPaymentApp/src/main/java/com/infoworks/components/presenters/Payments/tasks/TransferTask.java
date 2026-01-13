package com.infoworks.components.presenters.Payments.tasks;

import com.infoworks.components.presenters.Payments.parser.VAccountResponseParser;
import com.infoworks.domain.repositories.VAccountRepository;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.tasks.ExecutableTask;

public class TransferTask extends ExecutableTask<Message, Response> {

    private VAccountRepository repository;
    private String accountName;
    private String accountPrefix;
    private String senderAcTitle;
    private String receiverAcTitle;
    private String currency;
    private String amount;
    private boolean shouldReverseIfExecuted = false;

    public TransferTask(String accountPrefix
            , String accountName
            , String receiverAcTitle
            , String currency
            , String amount) {
        this.accountName = accountName;
        this.accountPrefix = accountPrefix;
        this.senderAcTitle = accountPrefix + "@" + accountName;
        this.receiverAcTitle = receiverAcTitle;
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

    private String parseAccountName(String acTitle) {
        String[] parsed = acTitle.split("@");
        return parsed.length > 1 ? parsed[1] : null;
    }

    private String parseAccountPrefix(String acTitle) {
        String[] parsed = acTitle.split("@");
        return parsed.length > 0 ? parsed[0] : null;
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        //Check account existence:
        Response response = getRepository().accountExist(accountPrefix, accountName);
        boolean isSenderExist = VAccountResponseParser.isExist(response);
        //
        response = getRepository().accountExist(parseAccountPrefix(receiverAcTitle), parseAccountName(receiverAcTitle));
        boolean isReceiverExist = VAccountResponseParser.isExist(response);
        //Make Transfer:
        try {
            if (isSenderExist && isReceiverExist) {
                response = getRepository().makeTransaction(accountPrefix, accountName, currency, amount, receiverAcTitle);
                if (response.getStatus() == 200) {
                    shouldReverseIfExecuted = true;
                    return response;
                } else {
                    throw new Exception(senderAcTitle + " : " + amount + " (" + currency + ") Transfer Failed!");
                }
            } else {
                String accountNotFound = isSenderExist == false ? senderAcTitle : receiverAcTitle;
                throw new Exception(accountNotFound + " : " + "Not Found!");
            }
        } catch (Exception ex) {
            shouldReverseIfExecuted = false;
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Response abort(Message message) throws RuntimeException {
        if (shouldReverseIfExecuted) {
            Response response = getRepository().accountExist(accountPrefix, accountName);
            boolean isSenderExist = VAccountResponseParser.isExist(response);
            //
            response = getRepository().accountExist(parseAccountPrefix(receiverAcTitle), parseAccountName(receiverAcTitle));
            boolean isReceiverExist = VAccountResponseParser.isExist(response);
            //Make Reverse Transfer:
            if (isSenderExist && isReceiverExist) {
                String receiverPrefix = parseAccountPrefix(receiverAcTitle);
                String receiverName = parseAccountName(receiverAcTitle);
                response = getRepository().makeTransaction(receiverPrefix, receiverName, currency, amount, senderAcTitle);
                if (response.getStatus() == 200) {
                    return response;
                } else {
                    return response.setStatus(500)
                            .setError(receiverAcTitle + " : " + amount + " (" + currency + ") Reverse Transfer Failed!");
                }
            } else {
                String accountNotFound = isSenderExist == false ? senderAcTitle : receiverAcTitle;
                return response.setStatus(404).setError(accountNotFound + " : " + "Not Found!");
            }
        }
        return super.abort(message);
    }
}
