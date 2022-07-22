package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.jjwt.JWTPayload;
import com.infoworks.lab.jjwt.TokenValidator;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.services.ledger.LedgerBook;
import com.itsoul.lab.generalledger.entities.Money;
import com.itsoul.lab.generalledger.entities.TransferRequest;
import com.itsoul.lab.ledgerbook.accounting.head.ChartOfAccounts;
import com.itsoul.lab.ledgerbook.accounting.head.Ledger;
import com.itsoul.lab.ledgerbook.connector.SourceConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class AccountControllerV1 {

    @Autowired
    @Qualifier("LedgerBookUsername")
    private String accountOwner;

    @Autowired @Qualifier("LedgerBookPassword")
    private String password;

    @Autowired @Qualifier("LedgerBookCurrency")
    private String currency;

    @Autowired @Qualifier("TenantID")
    private String tenantID;

    @Autowired @Qualifier("SQLConnector")
    private SourceConnector connector;

    public SourceConnector getConnector() {
        return connector;
    }

    @PostMapping("/create/company/master/{prefix}/{amount}")
    public ResponseEntity<Response> createMAccount(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
            , @PathVariable("prefix") String prefix
            , @PathVariable("amount") String amount){
        //
        return createVAccount(token, prefix, amount);
    }

    @PostMapping("/create/{prefix}/{amount}")
    public ResponseEntity<Response> createVAccount(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
            , @PathVariable("prefix") String prefix
            , @PathVariable("amount") String amount){
        //
        Response response = new Response()
                .setError("Not Implemented")
                .setStatus(HttpStatus.NOT_IMPLEMENTED.value());
        //
        LedgerBook ledgerBook = new LedgerBook(getConnector(), accountOwner, password, tenantID, currency);
        //
        String username = TokenValidator.parsePayload(token, JWTPayload.class).getIss();
        String ac_title = ledgerBook.getACTitle(username);
        String cash_account = ledgerBook.getACNo(username, prefix);
        //Event A: Create vAccountSummary entity with balance = 1000.0;
        //Event B: ChartOfAccount for the username:
        //
        try{
            Money money = ledgerBook.createAccount(prefix, username, amount);
            response.setPayload(String.format("{\"title\":\"%s\",\"no\":\"%s\",\"balance\":\"%s\",\"currency\":\"%s\"}"
                    , ac_title
                    , cash_account
                    ,money.getAmount().toPlainString()
                    ,money.getCurrency().getDisplayName()));
            response.setStatus(HttpStatus.OK.value());
            response.setError(null);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            response.setError(e.getMessage());
        }
        //
        return ResponseEntity.unprocessableEntity().body(response);
    }

    @PostMapping("/make/transaction/{from}/{amount}/{to}/{type}/{ref}")
    public ResponseEntity<Response> makeTransactions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
            , @PathVariable("type") String type
            , @PathVariable("ref") String ref
            , @PathVariable("from") String from
            , @PathVariable("amount") String amount
            , @PathVariable("to") String to){
        //
        Response response = new Response()
                .setError("Not Implemented")
                .setStatus(HttpStatus.NOT_IMPLEMENTED.value());
        //
        LedgerBook ledgerBook = new LedgerBook(getConnector(), accountOwner, password, tenantID, currency);
        //
        String username = TokenValidator.parsePayload(token, JWTPayload.class).getIss();
        if(from == null || from.isEmpty()){
            from = ledgerBook.getACNo(username, "CASH");
        }
        //
        try {
            //
            Money money = ledgerBook.makeTransactions(type, ref, from, amount, to);
            response.setPayload(String.format("{\"title\":\"%s\",\"balance\":\"%s\",\"currency\":\"%s\"}"
                    , from
                    ,money.getAmount().toPlainString()
                    ,money.getCurrency().getDisplayName()));
            response.setStatus(HttpStatus.OK.value());
            response.setError(null);
            return ResponseEntity.ok(response);
            //
        }catch (Exception e){
            response.setError(e.getMessage());
        }
        //
        return ResponseEntity.unprocessableEntity().body(response);
    }

    @PostMapping("/check/balance/{prefix}")
    public ResponseEntity<Response> checkAccountBalance(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
            ,@PathVariable("prefix") String prefix){
        //
        Response response = new Response()
                .setError("Not Implemented")
                .setStatus(HttpStatus.NOT_IMPLEMENTED.value());
        //
        LedgerBook ledgerBook = new LedgerBook(getConnector(), accountOwner, password, tenantID, currency);
        //
        String username = TokenValidator.parsePayload(token, JWTPayload.class).getIss();
        String cash_account = ledgerBook.getACNo(username, prefix);
        //
        try{
            //
            Money money = ledgerBook.readBalance(prefix, username);
            response.setPayload(String.format("{\"title\":\"%s\",\"balance\":\"%s\",\"currency\":\"%s\"}"
                    , cash_account
                    ,money.getAmount().toPlainString()
                    ,money.getCurrency().getDisplayName()));
            response.setStatus(HttpStatus.OK.value());
            response.setError(null);
            return ResponseEntity.ok(response);
            //
        }catch (Exception e){
            response.setError(e.getMessage());
        }
        //
        return ResponseEntity.unprocessableEntity().body(response);
    }

    @PostMapping("/check/account/{prefix}")
    public ResponseEntity<Response> checkAccountExist(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
            ,@PathVariable("prefix") String prefix){
        Response response = new Response()
                .setError("Not Implemented")
                .setStatus(HttpStatus.NOT_IMPLEMENTED.value());
        //
        LedgerBook ledgerBook = new LedgerBook(getConnector(), accountOwner, password, tenantID, currency);
        //
        String username = TokenValidator.parsePayload(token, JWTPayload.class).getIss();
        String cash_account = ledgerBook.getACNo(username, prefix);
        //
        try{
            //
            boolean exist = ledgerBook.isAccountExist(prefix, username);
            if (exist){
                response.setPayload(String.format("{\"username\":\"%s\",\"title\":\"%s\",\"exist\":\"%s\"}"
                        , username
                        , cash_account, exist));
                response.setStatus(HttpStatus.OK.value());
                response.setError(null);
            }else{
                response.setStatus(HttpStatus.OK.value());
                response.setError(String.format("{\"error\":\"Not an account!\",\"exist\":\"%s\"}", exist));
            }
            return ResponseEntity.ok(response);
            //
        }catch (Exception e){
            response.setError(e.getMessage());
        }
        //
        return ResponseEntity.unprocessableEntity().body(response);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostMapping("/create/duel")
    public ResponseEntity<Response> createVTestAccount(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token){
        //
        Response response = new Response()
                .setError("Not Implemented")
                .setStatus(HttpStatus.NOT_IMPLEMENTED.value());
        //
        LedgerBook ledgerBook = new LedgerBook(getConnector(), accountOwner, password, tenantID, currency);
        //
        String username = TokenValidator.parsePayload(token, JWTPayload.class).getIss();
        String ac_title = ledgerBook.getName(username) + "-Account";
        String cash_account = ledgerBook.getName(username) + "_CASH";
        String rev_account = ledgerBook.getName(username) + "_REVENUE";
        String currency = "BDT"; // "$" OR "EUR"
        //Event A: Create vAccountSummary entity with balance = 1000.0;
        //Event B: ChartOfAccount for the username:
        //
        try{
            ChartOfAccounts chartOfAccounts = new ChartOfAccounts.ChartOfAccountsBuilder()
                    .create(cash_account, "1000.00", currency)
                    .create(rev_account, "0.00", currency)
                    .build();
            //
            String secret = username;//FOR TESTING:
            //
            Ledger book = new Ledger.LedgerBuilder(chartOfAccounts)
                    .name(ac_title)
                    .connector(getConnector())
                    .client(username, tenantID)
                    .secret(secret)
                    .build();
            //
            Money money = book.getAccountBalance(cash_account);
            Money revenue = book.getAccountBalance(rev_account);
            book.close();
            //
            response.setPayload(String.format("{\"balance\":\"%s\",\"revenue\":\"%s\",\"currency\":\"%s\"}"
                    ,money.getAmount().toPlainString()
                    ,revenue.getAmount().toPlainString()
                    ,money.getCurrency().getDisplayName()));
            response.setStatus(HttpStatus.OK.value());
            response.setError(null);
            return ResponseEntity.ok(response);
            //
            //
        }catch (Exception e){
            response.setError(e.getMessage());
        }
        //
        return ResponseEntity.unprocessableEntity().body(response);
    }


    @PostMapping("/make/duel/transaction/{action}/{ref}")
    public ResponseEntity<Response> makeTransactions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
            , @PathVariable("action") String action
            , @PathVariable("ref") String ref){
        //
        LedgerBook ledgerBook = new LedgerBook(getConnector(), accountOwner, password, tenantID, currency);
        //
        Response response = new Response()
                .setError("Not Implemented")
                .setStatus(HttpStatus.NOT_IMPLEMENTED.value());
        //
        String username = TokenValidator.parsePayload(token, JWTPayload.class).getIss();
        String ac_title = ledgerBook.getName(username) + "-Account";
        String cash_account = ledgerBook.getName(username) + "_CASH";
        String rev_account = ledgerBook.getName(username) + "_REVENUE";
        String currency = "BDT"; // "$" OR "EUR"
        //actionType: Deposit/Redeem/CompleteDelivery
        //
        try {
            //
            ChartOfAccounts chartOfAccounts = new ChartOfAccounts.ChartOfAccountsBuilder()
                    .retrive(cash_account)
                    .retrive(rev_account)
                    .build();
            //
            String secret = username;//FOR TESTING:
            //
            Ledger book = new Ledger.LedgerBuilder(chartOfAccounts)
                    .name(ac_title)
                    .connector(getConnector())
                    .secret(secret)
                    .client(username, tenantID)
                    .build();

            //Transfer request:
            TransferRequest transferRequest1 = book.createTransferRequest()
                    .reference(ledgerBook.validateTransactionRef(ref))
                    .type(action)
                    .account(cash_account).debit("5.00", currency)
                    .account(rev_account).credit("5.00", currency)
                    .build();

            book.commit(transferRequest1);
            //At the end close the ledger book:
            Money money = book.getAccountBalance(cash_account);
            Money revenue = book.getAccountBalance(rev_account);
            book.close();
            //
            response.setPayload(String.format("{\"balance\":\"%s\",\"revenue\":\"%s\",\"currency\":\"%s\"}"
                    ,money.getAmount().toPlainString()
                    ,revenue.getAmount().toPlainString()
                    ,money.getCurrency().getDisplayName()));
            response.setStatus(HttpStatus.OK.value());
            response.setError(null);
            return ResponseEntity.ok(response);
            //
        }catch (Exception e){
            response.setError(e.getMessage());
        }
        //
        return ResponseEntity.unprocessableEntity().body(response);
    }

}
