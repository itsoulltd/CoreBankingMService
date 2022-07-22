package com.infoworks.lab.services.vaccount;

import com.infoworks.lab.beans.tasks.definition.TaskStack;
import com.infoworks.lab.domain.models.MakeTransaction;
import com.infoworks.lab.domain.models.MakeTransactionWithCharge;
import com.infoworks.lab.domain.types.AccountPrefix;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.services.ledger.LedgerBook;
import com.it.soul.lab.sql.entity.Ignore;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.logging.Logger;

public class MakeTransactionOnHandoverTask extends MakeTransactionTask {

    public MakeTransactionOnHandoverTask(LedgerBook book) {super(book);}

    public MakeTransactionOnHandoverTask(LedgerBook book, MakeTransactionWithCharge message) {
        super(book, message);
    }

    @Ignore
    private static final Logger LOG = Logger.getLogger("MakeTransactionOnHandoverTask");

    @Override
    public Message execute(Message message) throws RuntimeException {
        Response response = new Response()
                .setError("Bad Request")
                .setStatus(HttpStatus.BAD_REQUEST.value());
        if (getTransaction() == null || !(getTransaction() instanceof MakeTransactionWithCharge)){
            return response;
        }
        try {
            //
            MakeTransactionWithCharge request = (MakeTransactionWithCharge) getTransaction();
            LOG.info("MakeTransactionOnHandoverTask: " + request.toString());
            BigDecimal serviceCharge = new BigDecimal(request.getServiceCharge());
            BigDecimal merchantBaseCharge = new BigDecimal(request.getBaseCharge());// is the given value by the merchant.
            BigDecimal requestedAmount = new BigDecimal(request.getAmount()); //is the given value by merchant to collect by rider.
            //First: CASH@Rider (dr) & CASH@Master (cr) transaction with the requestedMoney:
            MakeTransaction firstTransaction = new MakeTransaction();
            firstTransaction.setFrom(request.getFrom()); //From: Rider-Cash-Account
            firstTransaction.setTo(LedgerBook.getACNo("Master", AccountPrefix.CASH.name())); //Master-Cash-Account
            //
            BigDecimal totalCollectable = requestedAmount.add(merchantBaseCharge);
            firstTransaction.setAmount(totalCollectable.toPlainString());
            firstTransaction.setCurrency(request.getCurrency());
            firstTransaction.setType(request.getType().length() > 20 ? "hand-over-delivery" : request.getType());
            MakeTransactionTask first = new MakeTransactionTask(getLedgerBook(), firstTransaction);
            //
            //Second: CASH@Master (dr) & CASH@Merchant (cr) transaction
            // after deducting the serviceChange for Merchant from the sum of the total sum:
            MakeTransaction secTransaction = new MakeTransaction();
            secTransaction.setFrom(LedgerBook.getACNo("Master", AccountPrefix.CASH.name())); //Master-Cash-Account
            secTransaction.setTo(request.getTo()); //To: Merchant-Cash-Account
            //
            BigDecimal deductedAmount = totalCollectable.subtract(serviceCharge);
            secTransaction.setAmount(deductedAmount.toPlainString());
            secTransaction.setCurrency(request.getCurrency());
            secTransaction.setType(request.getType().length() > 20 ? "hand-over-delivery" : request.getType());
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
                        .setMessage("HandOver Transaction Successfully Completed.")
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
                .setMessage(message != null ? message.toString() : "Unknown Error Happened @ MakeTransactionOnHandoverTask");
    }
}
