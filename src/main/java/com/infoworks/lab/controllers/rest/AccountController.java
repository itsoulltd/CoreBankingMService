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
import com.itsoul.lab.generalledger.entities.TransactionLeg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        String tokenIss = TokenValidator.parsePayload(token, JWTPayload.class).getIss();
        if (!transaction.getUsername().equalsIgnoreCase(tokenIss)){
            response.setError("Unauthorized Access By: " + transaction.getUsername());
            return ResponseEntity.unprocessableEntity().body(response);
        }
        if(transaction.getType() == null || transaction.getType().isEmpty())
            transaction.setType("transfer");
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
        transaction.setType("deposit");
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
            ,@Valid @RequestBody MakeWithdrawal transaction){
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
        transaction.setType("withdrawal");
        //Now Make The Transaction-Flow
        TaskStack stack = TaskStack.createSync(true);
        stack.push(new MakeTransactionTask(ledgerBook, transaction));
        stack.commit(false, (message, state) -> {
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

    @GetMapping("/recent/transactions")
    public ResponseEntity<List<Map>> searchAll(@RequestHeader(HttpHeaders.AUTHORIZATION) String token
            , @RequestParam("username") String username
            , @RequestParam("prefix") String prefix) {
        //
        final String matcher = LedgerBook.getACNo(username, prefix);
        List<Transaction> cashAccountTransactionList = ledgerBook.findTransactions(prefix, username);
        int toIndex = cashAccountTransactionList.size() > 10
                ? 10
                : (cashAccountTransactionList.size() - 1);
        List<Map> data = cashAccountTransactionList.subList(0, toIndex)
                .stream()
                .map(transaction -> convertTransactionIntoMap(transaction, matcher))
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(data);
    }

    private Map<String, String> convertTransactionIntoMap(Transaction transaction, String matcher) {
        Map<String, String> info = new HashMap<>();
        info.put("transaction-ref", transaction.getTransactionRef());
        Optional<TransactionLeg> tLeg = transaction.getLegs().stream()
                .filter(leg -> leg.getAccountRef().equalsIgnoreCase(matcher))
                .findFirst();
        if (tLeg.isPresent()){
            info.put("amount", tLeg.get().getAmount().getAmount().toPlainString());
            info.put("curr", tLeg.get().getAmount().getCurrency().getDisplayName());
            info.put("balance", tLeg.get().getBalance().toPlainString());
        }
        info.put("transaction-type", transaction.getTransactionType());
        /*info.put("transaction-date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(transaction.getTransactionDate()));*/
        info.put("transaction-date", transaction.getTransactionDate().getTime() + "");
        return info;
    }

    @PostMapping("/search/transactions")
    public ResponseEntity<List<Map<String, Object>>> search(@RequestHeader(HttpHeaders.AUTHORIZATION) String token
            , @RequestParam("username") String username
            , @RequestParam("prefix") String prefix
            , @RequestBody TransHistoryQuery query) {
        //
        List<Map<String, Object>> data = ledgerBook.findTransactions(prefix, username, query);
        return ResponseEntity.ok().body(data);
    }

}
