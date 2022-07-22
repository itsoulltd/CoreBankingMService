package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.beans.tasks.definition.TaskStack;
import com.infoworks.lab.domain.models.CreateAccount;
import com.infoworks.lab.domain.models.MakeTransaction;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.services.ledger.LedgerBook;
import com.infoworks.lab.services.vaccount.CheckBalanceTask;
import com.infoworks.lab.services.vaccount.CheckVAccountExistTask;
import com.infoworks.lab.services.vaccount.CreateChartOfAccountTask;
import com.infoworks.lab.services.vaccount.MakeTransactionTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/account/v1")
public class AccountController {

    private static Logger LOG = LoggerFactory.getLogger("AccountController");
    private LedgerBook ledgerBook;

    public AccountController(@Qualifier("GeneralLedger") LedgerBook ledgerBook) {
        this.ledgerBook = ledgerBook;
    }

    @PostMapping("/new/account")
    public ResponseEntity<Response> createVAccount(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
            ,@Valid @RequestBody CreateAccount createAccount){
        //
        Response response = new Response()
                .setError("Error!")
                .setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        //
        if (createAccount.isAmountValid() == false){
            response.setError("Invalid Amount Value: " + createAccount.getAmount());
            return ResponseEntity.unprocessableEntity().body(response);
        }
        //
        if (createAccount.getUsername() == null || createAccount.getUsername().isEmpty()){
            response.setError("Invalid Username.");
            return ResponseEntity.unprocessableEntity().body(response);
        }
        //
        TaskStack stack = TaskStack.createSync(true);
        stack.push(new CreateChartOfAccountTask(ledgerBook, createAccount)); //Event:A
        stack.commit(false, (message, state) -> {
            //
            if (message != null && (message instanceof  Response)){
                int statusCode = ((Response) message).getStatus();
                response.setError(null)
                        .setStatus(statusCode)
                        .setPayload(message.toString());
            }
        });
        //
        return ResponseEntity.unprocessableEntity().body(response);
    }

    @PostMapping("/make/transaction")
    public ResponseEntity<Response> makeTransactions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
            ,@Valid @RequestBody MakeTransaction transaction){
        //
        Response response = new Response()
                .setMessage("Successfully Enqueued for Processing.")
                .setStatus(HttpStatus.OK.value());
        //
        if (transaction.getUsername() == null || transaction.getUsername().isEmpty()){
            response.setError("Invalid Username.");
            return ResponseEntity.unprocessableEntity().body(response);
        }
        //Now Make The Transaction-Flow
        TaskStack stack = TaskStack.createSync(true);
        stack.push(new MakeTransactionTask(ledgerBook, transaction));
        stack.commit(false, (message, state) -> {
            if (state == TaskStack.State.Finished){
                response.setMessage("Transaction Has Successfully Executed!");
            }else{
                response.setMessage("Transaction Has Failed To Execute!");
            }
        });
        //
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/balance")
    public ResponseEntity<Response> checkAccountBalance(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
            ,@RequestParam("username") String username
            ,@RequestParam("prefix") String prefix){
        //
        Response response = new Response()
                .setError("Not Implemented")
                .setStatus(HttpStatus.NOT_IMPLEMENTED.value());

        TaskStack stack = TaskStack.createSync(true);
        stack.push(new CheckBalanceTask(ledgerBook, username, prefix));
        stack.commit(false, (message, state) -> {
            if (message != null && message instanceof Response)
                response.setError(null)
                        .setStatus(((Response)message).getStatus())
                        .setPayload(message.getPayload());
        });

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/exist")
    public ResponseEntity<Response> checkAccountExist(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
            ,@RequestParam("username") String username
            ,@RequestParam("prefix") String prefix){
        //
        Response response = new Response()
                .setError("Not Implemented")
                .setStatus(HttpStatus.NOT_IMPLEMENTED.value());

        TaskStack stack = TaskStack.createSync(true);
        stack.push(new CheckVAccountExistTask(ledgerBook, username, prefix));
        stack.commit(false, (message, state) -> {
            if (message != null && message instanceof Response)
                response.setError(null)
                        .setStatus(((Response)message).getStatus())
                        .setPayload(message.getPayload());
        });

        return ResponseEntity.ok().body(response);
    }

}
