package com.infoworks.lab.services.vaccount;

import com.infoworks.lab.domain.models.Transaction;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.services.ledger.LedgerBook;
import com.itsoul.lab.generalledger.entities.Money;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

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
            checkInsufficientBalance(getTransaction(), message);

            Money money = getLedgerBook().makeTransactions(
                    getTransaction().getType()
                    , getTransaction().getRef()
                    , getTransaction().getFrom()
                    , getTransaction().getAmount()
                    , getTransaction().getTo()
            );

            response.setPayload(String.format(
                    "{\"title\":\"%s\",\"balance\":\"%s\",\"currency\":\"%s\",\"currencyDisplayName\":\"%s\"}"
                    , getTransaction().getFrom()
                    , money.getAmount().toPlainString()
                    , money.getCurrency().getCurrencyCode()
                    , money.getCurrency().getDisplayName())
            );

            return response.setStatus(HttpStatus.OK.value())
                    .setError(null)
                    .setMessage("Transaction Successfully Completed.");
            //
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private void checkInsufficientBalance(Transaction transaction, Message message) throws RuntimeException {
        if (transaction == null || transaction.getAmount() == null || transaction.getAmount().isEmpty()) return;
        if (message == null || message.getPayload() == null || message.getPayload().isEmpty()) return;
        try {
            String amount = transaction.getAmount();
            Map<String, Object> payload = Message.unmarshal(Map.class, message.getPayload());
            if (payload.get("balance") == null || payload.get("balance").toString().isEmpty()) return;
            String balance = payload.get("balance").toString();
            //If amount is less-then balance, then throw Insufficient Balance Exception.
            boolean insufficientBalance = new BigDecimal(balance).compareTo(new BigDecimal(amount)) == -1;
            if (insufficientBalance)
                throw new RuntimeException("Insufficient Balance!");
        } catch (IOException e) {}
    }

    @Override
    public Message abort(Message message) throws RuntimeException {
        return new Response()
                .setError("Not Implemented")
                .setStatus(HttpStatus.BAD_REQUEST.value())
                .setMessage(message != null ? message.toString() : "Unknown Error Happened @ MakeTransactionTask");
    }
}
