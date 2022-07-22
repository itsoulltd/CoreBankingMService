package com.infoworks.lab.services.vaccount;

import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.services.ledger.LedgerBook;

public class UpdateCreditTask extends LedgerTask {

    public UpdateCreditTask(LedgerBook book) {super(book);}

    public UpdateCreditTask(LedgerBook book, Message message) {
        super(book, message);
    }

    @Override
    public Message execute(Message message) throws RuntimeException {
        return null;
    }

    @Override
    public Message abort(Message message) throws RuntimeException {
        return null;
    }
}
