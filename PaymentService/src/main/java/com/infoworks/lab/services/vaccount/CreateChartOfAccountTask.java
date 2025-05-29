package com.infoworks.lab.services.vaccount;

import com.infoworks.lab.domain.models.ChartOfAccountResponse;
import com.infoworks.lab.domain.models.CreateAccount;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.services.ledger.LedgerBook;
import org.springframework.http.HttpStatus;

public class CreateChartOfAccountTask extends LedgerTask {

    public CreateChartOfAccountTask(LedgerBook book) {super(book);}

    public CreateChartOfAccountTask(LedgerBook book, CreateAccount message) {
        super(book, message);
    }

    @Override
    public Message execute(Message message) throws RuntimeException {
        //
        Response response = new Response();
        if (getMessage() == null || !(getMessage() instanceof CreateAccount))
            return response
                    .setError("Message is null OR not the required type!")
                    .setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        //
        CreateAccount createAccount = (CreateAccount) getMessage();
        String possibleACNo = LedgerBook.getACNo(createAccount.getUsername(), createAccount.getPrefix());
        //
        if (getLedgerBook().isAccountExist(createAccount.getPrefix(), createAccount.getUsername())){
            throw new RuntimeException(possibleACNo + " account already exist!");
        }
        //
        getLedgerBook().createAccount(createAccount.getPrefix()
                , createAccount.getUsername()
                , createAccount.getAmount());
        //
        return new ChartOfAccountResponse()
                .setAccountTitle(possibleACNo)
                .setStatus(HttpStatus.CREATED.value())
                .setMessage(possibleACNo + " account creation successful!");
    }

    @Override
    public Message abort(Message message) throws RuntimeException {
        Response response = new Response();
        if (getMessage() == null) return response.setError("Message is null!!!")
                .setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        //
        //FIXME: LedgerBook API Doesn't have Delete a account function:
        //Sorry to say: future implementation:
        //
        response.setStatus(HttpStatus.BAD_REQUEST.value())
                .setMessage((message != null) ? message.getPayload() : "Unknown Reason @ CreateChartOfAccountTask");
        return response;
    }
}
