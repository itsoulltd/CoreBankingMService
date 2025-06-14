package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.beans.tasks.definition.TaskStack;
import com.infoworks.lab.domain.models.*;
import com.infoworks.lab.domain.types.AccountType;
import com.infoworks.lab.jjwt.JWTPayload;
import com.infoworks.lab.jjwt.TokenValidator;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.services.ledger.LedgerBook;
import com.infoworks.lab.services.vaccount.CheckBalanceTask;
import com.infoworks.lab.services.vaccount.CheckVAccountExistTask;
import com.infoworks.lab.services.vaccount.CreateChartOfAccountTask;
import com.infoworks.lab.services.vaccount.MakeTransactionTask;
import com.itsoul.lab.generalledger.entities.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/account/v1")
public class AccountController {

    private static Logger LOG = LoggerFactory.getLogger("AccountController");

    @Resource(name = "GeneralLedger")
    private LedgerBook ledgerBook;

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
        MakeDeposit deposit = new MakeDeposit();
        deposit.unmarshallingFromMap(createAccount.marshallingToMap(true), true);
        deposit.setPrefix("CASH");
        deposit.setFrom("CASH@" + AccountType.MASTER.value());
        deposit.setType("deposit");
        deposit.setTo(LedgerBook.getACNo(createAccount.getUsername(), createAccount.getPrefix()));
        //Update create account amount with 0.00:
        createAccount.setAmount("0.00");
        //
        TaskStack stack = TaskStack.createSync(true);
        stack.push(new CreateChartOfAccountTask(ledgerBook, createAccount)); //Event:A
        stack.push(new MakeTransactionTask(ledgerBook, deposit));            //Event:B
        stack.commit(true, (message, state) -> {
            if (message != null && (message instanceof  Response)){
                int statusCode = ((Response) message).getStatus();
                response.setError(null).setStatus(statusCode);
            }
            if (state == TaskStack.State.Finished){
                response.setMessage("Account-Opening Has Been Successfully Executed!");
            }else{
                response.setMessage("Account-Opening Has Failed To Execute!");
            }
        });
        //
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/make/transaction")
    public ResponseEntity<Response> makeTransactions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
            , @Valid @RequestBody MakeTransaction transaction){
        //
        Response response = new Response()
                .setMessage("Successfully Enqueued for Processing.")
                .setStatus(HttpStatus.OK.value());
        //
        if (transaction.getUsername() == null || transaction.getUsername().isEmpty()){
            response.setError("Invalid Username.");
            return ResponseEntity.unprocessableEntity().body(response);
        }
        String tokenIss = TokenValidator.parsePayload(token, JWTPayload.class).getIss();
        if (!transaction.getUsername().equalsIgnoreCase(tokenIss)){
            response.setError("Unauthorized Access By: " + transaction.getUsername());
            return ResponseEntity.unprocessableEntity().body(response);
        }
        //Now Make The Transaction-Flow
        TaskStack stack = TaskStack.createSync(true);
        stack.push(new CheckBalanceTask(ledgerBook, transaction.getUsername(), transaction.getPrefix()));
        stack.push(new MakeTransactionTask(ledgerBook, transaction));
        stack.commit(true, (message, state) -> {
            if (state == TaskStack.State.Finished){
                response.setMessage("Transaction Has Successfully Executed!");
            }else{
                response.setMessage("Transaction Has Failed To Execute!");
            }
        });
        //
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/make/deposit")
    public ResponseEntity<Response> makeDeposit(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
            ,@Valid @RequestBody MakeDeposit transaction){
        //
        Response response = new Response()
                .setMessage("Successfully Enqueued for Processing.")
                .setStatus(HttpStatus.OK.value());
        //
        if (transaction.getUsername() == null || transaction.getUsername().isEmpty()){
            response.setError("Invalid Username.");
            return ResponseEntity.unprocessableEntity().body(response);
        }
        String tokenIss = TokenValidator.parsePayload(token, JWTPayload.class).getIss();
        if (!transaction.getUsername().equalsIgnoreCase(tokenIss)){
            response.setError("Unauthorized Access By: " + transaction.getUsername());
            return ResponseEntity.unprocessableEntity().body(response);
        }
        transaction.setPrefix("CASH");
        transaction.setFrom("CASH@" + AccountType.MASTER.value());
        transaction.setTo(LedgerBook.getACNo(transaction.getUsername(), transaction.getPrefix()));
        //Now Make The Transaction-Flow
        TaskStack stack = TaskStack.createSync(true);
        stack.push(new MakeTransactionTask(ledgerBook, transaction));
        stack.commit(false, (message, state) -> {
            if (state == TaskStack.State.Finished){
                response.setMessage("Deposit Has Successfully Executed!");
            }else{
                response.setMessage("Deposit Has Failed To Execute!");
            }
        });
        //
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/make/withdrawal")
    public ResponseEntity<Response> makeWithdrawal(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
            , @Valid @RequestBody MakeWithdrawal transaction){
        //
        Response response = new Response()
                .setMessage("Successfully Enqueued for Processing.")
                .setStatus(HttpStatus.OK.value());
        //
        if (transaction.getUsername() == null || transaction.getUsername().isEmpty()){
            response.setError("Invalid Username.");
            return ResponseEntity.unprocessableEntity().body(response);
        }
        String tokenIss = TokenValidator.parsePayload(token, JWTPayload.class).getIss();
        if (!transaction.getUsername().equalsIgnoreCase(tokenIss)){
            response.setError("Unauthorized Access By: " + transaction.getUsername());
            return ResponseEntity.unprocessableEntity().body(response);
        }
        transaction.setPrefix("CASH");
        transaction.setTo("CASH@" + AccountType.MASTER.value());
        //Now Make The Transaction-Flow
        TaskStack stack = TaskStack.createSync(true);
        stack.push(new CheckBalanceTask(ledgerBook, transaction.getUsername(), transaction.getPrefix()));
        stack.push(new MakeTransactionTask(ledgerBook, transaction));
        stack.commit(true, (message, state) -> {
            if (state == TaskStack.State.Finished){
                response.setMessage("Withdrawal Has Successfully Executed!");
            }else{
                response.setMessage("Withdrawal Has Failed To Execute!");
            }
        });
        //
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/balance")
    public ResponseEntity<Response> checkAccountBalance(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
            , @RequestParam("username") String username
            , @RequestParam(name = "prefix", required = false, defaultValue = "CASH") String prefix){
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
            , @RequestParam("username") String username
            , @RequestParam(name = "prefix", required = false, defaultValue = "CASH") String prefix){
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

    @GetMapping("/recent/transactions")
    public ResponseEntity<List<Map>> searchAll(@RequestHeader(HttpHeaders.AUTHORIZATION) String token
            , @RequestParam("username") String username
            , @RequestParam(name = "prefix", required = false, defaultValue = "CASH") String prefix) {
        //
        final String matcher = LedgerBook.getACNo(username, prefix);
        List<Transaction> cashAccountTransactionList = ledgerBook.findTransactions(prefix, username);
        if (cashAccountTransactionList.isEmpty())
            return ResponseEntity.notFound().build();
        //
        int toIndex = cashAccountTransactionList.size() > 10
                ? 10
                : (cashAccountTransactionList.size() - 1);
        List<Map> data = cashAccountTransactionList.subList(0, toIndex)
                .stream()
                .map(transaction -> ledgerBook.convertTransaction(transaction, matcher))
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(data);
    }

    @PostMapping("/search/transactions")
    public ResponseEntity<List<Map<String, Object>>> search(@RequestHeader(HttpHeaders.AUTHORIZATION) String token
            , @RequestParam("username") String username
            , @RequestParam(name = "prefix", required = false, defaultValue = "CASH") String prefix
            , @RequestBody TransactionSearchQuery query) {
        //
        List<Map<String, Object>> data = ledgerBook.findTransactions(prefix, username, query);
        return ResponseEntity.ok().body(data);
    }

}
