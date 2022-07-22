package com.infoworks.lab.services.vaccount;

import com.infoworks.lab.services.ledger.LedgerBook;
import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;
import org.springframework.http.HttpStatus;

public class CheckVAccountExistTask extends CheckBalanceTask {

    public CheckVAccountExistTask(LedgerBook book) {super(book);}

    public CheckVAccountExistTask(LedgerBook book, String username, String prefix) {
        super(book, username, prefix);
    }

    @Override
    public Message execute(Message message) throws RuntimeException {
        Response response = new Response()
                .setError("Not Implemented")
                .setStatus(HttpStatus.BAD_REQUEST.value());
        try{
            //
            String cash_account = LedgerBook.getACNo(getUsername(), getPrefix());
            boolean exist = getLedgerBook().isAccountExist(getPrefix(), getUsername());
            if (exist){
                response.setPayload(String.format("{\"username\":\"%s\",\"title\":\"%s\",\"exist\":\"%s\"}"
                        , getUsername()
                        , cash_account, exist));
                //
                response.setMessage(null);
            }else{
                response.setMessage(String.format("{\"error\":\"Not an account!\",\"exist\":\"%s\"}", exist));
            }
            return response
                    .setStatus(HttpStatus.OK.value())
                    .setError(null);
            //
        }catch (Exception e){
            response.setError(e.getMessage());
        }
        return response;
    }

}
