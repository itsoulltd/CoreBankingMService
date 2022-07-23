package com.infoworks.lab.services.ledger;

import com.itsoul.lab.generalledger.entities.Money;
import com.itsoul.lab.generalledger.entities.Transaction;
import com.itsoul.lab.generalledger.entities.TransferRequest;
import com.itsoul.lab.ledgerbook.accounting.head.ChartOfAccounts;
import com.itsoul.lab.ledgerbook.accounting.head.Ledger;
import com.itsoul.lab.ledgerbook.connector.SourceConnector;

import java.util.List;
import java.util.UUID;

/**
 * This a thread-safe bean:
 */
public class LedgerBook {

    private SourceConnector connector;
    private String owner;
    private String password;
    private String tenantID;
    private String currency;

    public LedgerBook(SourceConnector connector, String owner, String password, String tenantID, String currency) {
        this.connector = connector;
        this.owner = owner;
        this.password = password;
        this.tenantID = tenantID;
        this.currency = currency;
    }

    public Money createAccount(String prefix, String username, String deposit)
            throws RuntimeException{
        //
        String cash_account = getACNo(username, prefix);
        //
        ChartOfAccounts chartOfAccounts = new ChartOfAccounts.ChartOfAccountsBuilder()
                .create(cash_account, deposit, currency)
                .build();
        //
        Money money = null;
        Ledger book = null;
        try {
            book = getLedger(chartOfAccounts);
            money = Money.toMoney(deposit, currency);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (book != null)
                book.close();
        }
        return money;
    }

    public String validateTransactionRef(String ref){
        if (ref.length() > 20) ref = ref.substring(0, 19);
        return ref;
    }

    public Money readBalance(String prefix, String username) {
        //
        String cash_account = getACNo(username, prefix);
        ChartOfAccounts chartOfAccounts = new ChartOfAccounts.ChartOfAccountsBuilder()
                .retrive(cash_account)
                .build();
        //
        Money money = null;
        Ledger book = null;
        try {
            book = getLedger(chartOfAccounts);
            money = book.getAccountBalance(cash_account);
        } catch (Exception e) {
            money = Money.NULL_MONEY;
        } finally {
            if (book != null)
                book.close();
        }
        return money;
    }

    public boolean isAccountExist(String prefix, String username) {
        //
        String cash_account = getACNo(username, prefix);
        ChartOfAccounts chartOfAccounts = new ChartOfAccounts.ChartOfAccountsBuilder()
                .retrive(cash_account)
                .build();
        Ledger book = null;
        boolean isExist = false;
        try{
            book = getLedger(chartOfAccounts);
            isExist = book.getAccountBalance(cash_account) != null;
        }catch (Exception e) {} finally {
            if (book != null)
                book.close();
        }
        return isExist;
    }

    public Money makeTransactions(String type
            , String ref
            , String from
            , String amount
            , String to) throws RuntimeException{
        //
        Ledger book = null;
        try {
            //
            ChartOfAccounts chartOfAccounts = new ChartOfAccounts.ChartOfAccountsBuilder()
                    .retrive(from)
                    .retrive(to)
                    .build();
            //
            book = getLedger(chartOfAccounts);

            //Transfer request:
            TransferRequest transferRequest1 = book.createTransferRequest()
                    .reference(validateTransactionRef(ref))
                    .type(type)
                    .account(from).debit(amount, currency)
                    .account(to).credit(amount, currency)
                    .build();

            book.commit(transferRequest1);
            //At the end close the ledger book:
            Money money = book.getAccountBalance(from);
            return money;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }finally {
            if (book != null)
                book.close();
        }
    }

    private Ledger getLedger(ChartOfAccounts chartOfAccounts){
        Ledger book = new Ledger.LedgerBuilder(chartOfAccounts)
                .name("General Ledger")
                .connector(getConnector())
                .client(owner, tenantID)
                .secret(password)
                .skipLogPrinting(true)
                .build();
        return book;
    }

    public SourceConnector getConnector() {
        return connector;
    }

    public static String getName(String username){
        String name = username.split("@")[0];
        if(name.length() > 14) name = name.substring(0, 13); //14-char as Name: +8801712645571 or any unique-name length of 14 char
        return name;
    }

    public static String getACTitle(String username){
        String name = username.split("@")[0];
        if (name.length() > 20) name = name.substring(0,19);
        return name;
    }

    public static String getACNo(String username, String prefix){
        String name = getName(username);
        int nameLength = name.length();
        int howShort = Math.abs(nameLength - 14);
        if((howShort + 5) < prefix.length()) prefix = prefix.substring(0, (howShort + 5)); //greater then 5 char
        return prefix + "@" + name;
    }

    public static String parseUsername(String accountNo){
        try {
            String name = accountNo.split("@")[1];
            return name;
        } catch (Exception e) {}
        return "";
    }

    public static String parsePrefix(String accountNo){
        String prefix = accountNo.split("@")[0];
        return prefix;
    }

    public static String generateTransactionRef(String username) {
        return UUID.randomUUID().toString().substring(0, 18);
    }

    public List<Transaction> findTransactions(String prefix, String username) {
        String cash_account = getACNo(username, prefix);
        ChartOfAccounts chartOfAccounts = new ChartOfAccounts.ChartOfAccountsBuilder()
                .retrive(cash_account)
                .build();
        Ledger book = getLedger(chartOfAccounts);
        List<Transaction> cashAccountTransactionList = book.findTransactions(cash_account);
        return cashAccountTransactionList;
    }
}
