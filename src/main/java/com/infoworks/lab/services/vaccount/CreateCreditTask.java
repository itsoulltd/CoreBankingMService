package com.infoworks.lab.services.vaccount;

import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.services.ledger.LedgerBook;

public class CreateCreditTask extends LedgerTask {

    public CreateCreditTask(LedgerBook book) {super(book);}

    @Override
    public Message execute(Message message) throws RuntimeException {
        return null;
    }

    @Override
    public Message abort(Message message) throws RuntimeException {
        return null;
    }
}
