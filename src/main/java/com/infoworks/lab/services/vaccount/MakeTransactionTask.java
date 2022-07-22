package com.infoworks.lab.services.vaccount;

import com.infoworks.lab.domain.models.Transaction;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.services.ledger.LedgerBook;
import com.itsoul.lab.generalledger.entities.Money;
import org.springframework.http.HttpStatus;

public class MakeTransactionTask extends LedgerTask {

    private Transaction transaction;

    public MakeTransactionTask(LedgerBook book) {super(book);}

    public Transaction getTransaction() {
        if (transaction == null && (getMessage() instanceof Transaction)){
            transaction = (Transaction) getMessage();
        }
        return transaction;
    }

    public MakeTransactionTask(LedgerBook book, Transaction message) {
        super(book, message);
    }

    @Override
    public Message execute(Message message) throws RuntimeException {

        Response response = new Response()
                .setError("Not Implemented")
                .setStatus(HttpStatus.BAD_REQUEST.value());
        //Internal-Input-Validation:
        if(getTransaction().getFrom() == null || getTransaction().getFrom().isEmpty()){
            getTransaction().setFrom(LedgerBook.getACNo(getTransaction().getUsername(), getTransaction().getPrefix()));
        }
        if (getTransaction().getRef() == null || getTransaction().getRef().isEmpty()
                || getTransaction().getRef().length() > 20){
            getTransaction().setRef(LedgerBook.generateTransactionRef(getTransaction().getUsername()));
        }
        if (getTransaction().getType() == null || getTransaction().getType().isEmpty()
                || getTransaction().getType().length() > 20){
            if (getTransaction().getType() != null)
                getTransaction().setType(getTransaction().getType().substring(0, 19));
            else
                getTransaction().setType("transaction");
        }
        //
        try {
            Money money = getLedgerBook().makeTransactions(
                    getTransaction().getType()
                    , getTransaction().getRef()
                    , getTransaction().getFrom()
                    , getTransaction().getAmount()
                    , getTransaction().getTo()
            );

            response.setPayload(String.format(
                    "{\"title\":\"%s\",\"balance\":\"%s\",\"currency\":\"%s\"}"
                    , getTransaction().getFrom()
                    ,money.getAmount().toPlainString()
                    ,money.getCurrency().getDisplayName())
            );

            return response.setStatus(HttpStatus.OK.value())
                    .setError(null)
                    .setMessage("Transaction Successfully Completed.");
            //
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Message abort(Message message) throws RuntimeException {
        return new Response()
                .setError("Not Implemented")
                .setStatus(HttpStatus.BAD_REQUEST.value())
                .setMessage(message != null ? message.toString() : "Unknown Error Happened @ MakeTransactionTask");
    }
}
