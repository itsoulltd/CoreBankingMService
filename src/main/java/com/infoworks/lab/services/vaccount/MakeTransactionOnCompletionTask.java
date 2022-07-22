package com.infoworks.lab.services.vaccount;

import com.infoworks.lab.beans.tasks.definition.TaskStack;
import com.infoworks.lab.domain.models.MakeTransaction;
import com.infoworks.lab.domain.models.MakeTransactionWithCharge;
import com.infoworks.lab.domain.types.AccountPrefix;
import com.infoworks.lab.services.ledger.LedgerBook;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;
import com.it.soul.lab.sql.entity.Ignore;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.logging.Logger;

public class MakeTransactionOnCompletionTask extends MakeTransactionTask {

    public MakeTransactionOnCompletionTask(LedgerBook book) {super(book);}

    public MakeTransactionOnCompletionTask(LedgerBook book, MakeTransactionWithCharge message) {
        super(book, message);
    }

    @Ignore
    private static final Logger LOG = Logger.getLogger("MakeTransactionOnCompletionTask");

    @Override
    public Message execute(Message message) throws RuntimeException {
        Response response = new Response()
                .setError("Bad Request")
                .setStatus(HttpStatus.BAD_REQUEST.value());
        if (getTransaction() == null || !(getTransaction() instanceof MakeTransactionWithCharge)){
            return response;
        }
        try {
            MakeTransactionWithCharge request = (MakeTransactionWithCharge) getTransaction();
            LOG.info("MakeTransactionOnCompletionTask: " + request.toString());
            BigDecimal serviceCharge = new BigDecimal(request.getServiceCharge());
            BigDecimal riderBaseCharge = new BigDecimal(request.getBaseCharge());// is the given value by the Rider/Agent.
            //First: CASH@Master (dr) & CASH@Rider (cr) transaction with the baseCharge for Rider/Agent:
            MakeTransaction firstTransaction = new MakeTransaction();
            firstTransaction.setFrom(LedgerBook.getACNo("Master", AccountPrefix.CASH.name())); //From: Master-Cash-Account
            firstTransaction.setTo(LedgerBook.getACNo(request.getUsername(), request.getPrefix())); //To: Rider-Cash-Account
            //
            firstTransaction.setAmount(riderBaseCharge.toPlainString());
            firstTransaction.setCurrency(request.getCurrency());
            firstTransaction.setType(request.getType().length() > 20 ? "delivery-complete" : request.getType());
            MakeTransactionTask first = new MakeTransactionTask(getLedgerBook(), firstTransaction);
            //
            //Second: CASH@Master (dr) & CASH@Revenue (cr) transaction with the serviceCharge
            MakeTransaction secTransaction = new MakeTransaction();
            secTransaction.setFrom(LedgerBook.getACNo("Master", AccountPrefix.CASH.name())); //From: Master-Cash-Account
            secTransaction.setTo(LedgerBook.getACNo("Master", AccountPrefix.REVENUE.name())); //To: Master-Revenue-Account
            //
            BigDecimal revenue = serviceCharge.subtract(riderBaseCharge);
            secTransaction.setAmount(revenue.toPlainString());
            secTransaction.setCurrency(request.getCurrency());
            secTransaction.setType(request.getType().length() > 20 ? "delivery-complete" : request.getType());
            MakeTransactionTask second = new MakeTransactionTask(getLedgerBook(), secTransaction);
            //
            //////////////////////////
            ////---DO THE TRANSACTION:
            //////////////////////////
            TaskStack stack = TaskStack.createSync(true);
            stack.push(second);
            stack.push(first);
            stack.commit(false, (message1, state) -> {
                response.setStatus(HttpStatus.OK.value())
                        .setError(null)
                        .setMessage("Delivery-Completion Transaction Successfully Completed.")
                        .setPayload((message1 != null) ? message1.getPayload() : "");
            });
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return response;
    }

    @Override
    public Message abort(Message message) throws RuntimeException {
        return new Response()
                .setError("Not Implemented")
                .setStatus(HttpStatus.BAD_REQUEST.value())
                .setMessage(message != null ? message.toString() : "Unknown Error Happened @ MakeTransactionOnCompletionTask");
    }
}
