package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.jjwt.JWTHeader;
import com.infoworks.lab.jjwt.JWTPayload;
import com.infoworks.lab.jwtoken.definition.TokenProvider;
import com.infoworks.lab.jwtoken.services.JWTokenProvider;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.rest.models.SearchQuery;
import com.infoworks.lab.services.VAccountResponseParser;
import com.infoworks.lab.services.VAccountService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AccountControllerTest {

    String tokenPayload = "{\"password\": \"1234\"\n" +
            ", \"role\": \"User\"\n" +
            ", \"username\": \"giko\"}";

    String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
            ".eyJwYXNzd29yZCI6IjEyMzQiLCJyb2xlIjoiVXNlciIsInVzZXJuYW1lIjoiZ2lrbyJ9" +
            ".WGJX9x7Wr0rz59poidg4cqIYLV5mtEGz6S4peagApEI";

    private String generateJWToken(String username, String password, String role) {
        JWTHeader header = new JWTHeader().setAlg("HS256").setTyp("JWT");
        JWTPayload payload = new JWTPayload()
                .setIss(username)
                .addData("password", password)
                .addData("role",role)
                .addData("username",username);
        TokenProvider provider = new JWTokenProvider(UUID.randomUUID().toString())
                .setPayload(payload).setHeader(header);
        String token = provider.generateToken(TokenProvider.defaultTokenTimeToLive());
        return token;
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void createVAccount() {
        jwtToken = generateJWToken("giko", "1234", "User");
        System.out.println(jwtToken);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8086/api/account/v2")
                .build();
        VAccountService service = new VAccountService(restTemplate, jwtToken);
        //
        Response response = service.accountExist("CASH", "giko");
        boolean isExist = VAccountResponseParser.isExist(response);
        System.out.println("Exist: " + isExist);
        if (!isExist) {
            Response newCreated = service.createAccount("CASH"
                    , "giko"
                    , "BDT"
                    , "3000.00"
                    , "USER");
            System.out.println("New Account Create Status: " + newCreated.getStatus()
                    + "; Message: " + newCreated.getMessage());
        }
        //
        response = service.accountExist("CASH", "mike");
        isExist = VAccountResponseParser.isExist(response);
        System.out.println("Exist: " + isExist);
        if (!isExist) {
            Response newCreated = service.createAccount("CASH"
                    , "mike"
                    , "BDT"
                    , "2300.00"
                    , "USER");
            System.out.println("New Account Create Status: " + newCreated.getStatus()
                    + "; Message: " + newCreated.getMessage());
        }
    }

    @Test
    public void makeDeposit() {
        jwtToken = generateJWToken("giko", "1234", "User");
        System.out.println(jwtToken);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8086/api/account/v2")
                .build();
        //
        VAccountService service = new VAccountService(restTemplate, jwtToken);
        //Check isExist?
        Response response = service.accountExist("CASH", "giko");
        boolean isExist = VAccountResponseParser.isExist(response);
        //Fetch current-balance:
        Response balanceRes = service.accountBalance("CASH", "giko");
        BigDecimal amount = VAccountResponseParser.getAmount(balanceRes);
        boolean isLessThan = amount.compareTo(new BigDecimal("6000.00")) == -1;
        if (isExist && isLessThan) {
            Response updated = service.makeDeposit("giko"
                    , "BDT"
                    , "102.00");
            System.out.println("Deposit Status: " + updated.getStatus()
                    + "; Message: " + updated.getMessage());
        }
    }

    @Test
    public void makeTransactions() {
        jwtToken = generateJWToken("giko", "1234", "User");
        System.out.println(jwtToken);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8086/api/account/v2")
                .build();
        VAccountService service = new VAccountService(restTemplate, jwtToken);
        //
        Response response = service.accountExist("CASH", "giko");
        boolean isGikoExist = VAccountResponseParser.isExist(response);
        System.out.println("giko Exist: " + isGikoExist);
        //
        response = service.accountExist("CASH", "mike");
        boolean isMikeExist = VAccountResponseParser.isExist(response);
        System.out.println("mike Exist: " + isMikeExist);
        //
        if (isGikoExist && isMikeExist) {
            Response makeTrans = service.makeTransaction("CASH"
                    , "giko"
                    , "BDT"
                    , "78.00"
                    , "CASH@mike");
            System.out.println("Send Money Status: " + makeTrans.getStatus()
                    + "; Message: " + makeTrans.getMessage());
        }
    }

    @Test
    public void makeWithdrawal() {
        jwtToken = generateJWToken("giko", "1234", "User");
        System.out.println(jwtToken);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8086/api/account/v2")
                .build();
        //
        VAccountService service = new VAccountService(restTemplate, jwtToken);
        //Check isExist?
        Response response = service.accountExist("CASH", "giko");
        boolean isExist = VAccountResponseParser.isExist(response);
        //Fetch current-balance:
        Response balanceRes = service.accountBalance("CASH", "giko");
        BigDecimal amount = VAccountResponseParser.getAmount(balanceRes);
        boolean isLessThan = amount.compareTo(new BigDecimal("6000.00")) == -1;
        if (isExist && isLessThan) {
            Response updated = service.makeWithdrawal("giko"
                    , "BDT"
                    , "300.00");
            System.out.println("Withdrawal Status: " + updated.getStatus()
                    + "; Message: " + updated.getMessage());
        }
    }

    @Test
    public void negativeWithdrawal() {
        jwtToken = generateJWToken("nikku", "1234", "User");
        System.out.println(jwtToken);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8086/api/account/v2")
                .build();
        //
        VAccountService service = new VAccountService(restTemplate, jwtToken);
        //Check isExist?
        Response response = service.accountExist("CASH", "nikku");
        boolean isExist = VAccountResponseParser.isExist(response);
        //
        if (!isExist) {
            Response newCreated = service.createAccount("CASH"
                    , "nikku"
                    , "BDT"
                    , "100.00"
                    , "USER");
            isExist = newCreated != null ? newCreated.getStatus() == 200 : false;
            System.out.println("New Account Create Status: " + newCreated.getStatus()
                    + "; Message: " + newCreated.getMessage());
        }
        //
        if (isExist) {
            Response updated = service.makeWithdrawal("nikku"
                    , "BDT"
                    , "300.00");
            System.out.println("Withdrawal Status: " + updated.getStatus()
                    + "; Message: " + updated.getMessage());
        }
    }

    @Test
    public void checkAccountBalance() {
        jwtToken = generateJWToken("Master", "1234", "User");
        System.out.println(jwtToken);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8086/api/account/v2")
                .build();
        //
        VAccountService service = new VAccountService(restTemplate, jwtToken);
        Response response = service.accountBalance("CASH", "Master");
        Assert.assertTrue(response.getStatus() == 200);
        System.out.println(response.getPayload());
        BigDecimal amount = VAccountResponseParser.getAmount(response);
        System.out.println("Balance: " + amount.toString());
    }

    @Test
    public void checkAccountExist() {
        jwtToken = generateJWToken("Master", "1234", "User");
        System.out.println(jwtToken);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8086/api/account/v2")
                .build();
        //
        VAccountService service = new VAccountService(restTemplate, jwtToken);
        Response response = service.accountExist("CASH", "Master");
        Assert.assertTrue(response.getStatus() == 200);
        System.out.println(response.getPayload());
        boolean isExist = VAccountResponseParser.isExist(response);
        System.out.println("Exist: " + isExist);
    }

    @Test
    public void recentTrans() {
        jwtToken = generateJWToken("Master", "1234", "User");
        System.out.println(jwtToken);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8086/api/account/v2")
                .build();
        //
        try {
            VAccountService service = new VAccountService(restTemplate, jwtToken);
            List<Map> response = service.recentTransactions("CASH", "Master");
            Assert.assertTrue(response.size() >= 0);
            response.forEach(trans -> System.out.println(trans));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void searchTrans() {
        jwtToken = generateJWToken("Master", "1234", "User");
        System.out.println(jwtToken);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8086/api/account/v2")
                .build();
        //
        SearchQuery query = new SearchQuery();
        query.setSize(10);
        query.setPage(0);
        query.add("type").isEqualTo("deposit")
                .and("from").isGreaterThenOrEqual("2022-07-23");
                //.and("till").isEqualTo("2022-08-20");
                //.and("to").isLessThen("2023-04-18");
        try {
            VAccountService service = new VAccountService(restTemplate, jwtToken);
            List<Map> response = service.searchTransactions("CASH", "Master", query);
            Assert.assertTrue(response.size() >= 0);
            response.forEach(trans -> System.out.println(trans));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void createAccountWithZeroBalance() {
        //Create a account with zero balance and try to make a withdraw:
        String prefix = "CASH";
        String username = "crt_ac_w_zero_bal";
        jwtToken = generateJWToken(username, "1234", "User");
        System.out.println(jwtToken);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8086/api/account/v2")
                .build();
        VAccountService service = new VAccountService(restTemplate, jwtToken);
        //
        boolean isExist = VAccountResponseParser.isExist(service.accountExist(prefix, username));
        if (!isExist) {
            Response response = service.createAccount(prefix, username, "BDT", "0.00", "USER");
            System.out.println(response.getMessage());
        }
    }

    @Test(expected = HttpClientErrorException.class)
    public void createAccountWithNegativeBalance() {
        //Create a account with zero balance and try to make a withdraw:
        String prefix = "CASH";
        String username = "crt_ac_w_neg_bal";
        jwtToken = generateJWToken(username, "1234", "User");
        System.out.println(jwtToken);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8086/api/account/v2")
                .build();
        VAccountService service = new VAccountService(restTemplate, jwtToken);
        //
        boolean isExist = VAccountResponseParser.isExist(service.accountExist(prefix, username));
        if (!isExist) {
            try {
                Response response = service.createAccount(prefix, username, "BDT", "-1.00", "USER");
                System.out.println(response.getMessage());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw e;
            }
        }
    }

    @Test
    public void makeTransactionTestWithBothZeroBalance() {
        //Create 2 accounts: one for company and another for a rider
        //Rider goto customer home and deliver the package.
        String prefix = "CASH";
        String receiver = "rider_receiver";
        String sender = "rider_sender";
        jwtToken = generateJWToken(sender, "1234", "User");
        System.out.println(jwtToken);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8086/api/account/v2")
                .build();
        VAccountService service = new VAccountService(restTemplate, jwtToken);
        //
        boolean isSenderExist = VAccountResponseParser.isExist(service.accountExist(prefix, sender));
        if (!isSenderExist) {
            Response response = service.createAccount(prefix, sender, "BDT", "0.00", "USER");
            isSenderExist = response.getStatus() == 200 ? true : false;
            System.out.println(response.getMessage());
        }
        boolean isReceiverExist = VAccountResponseParser.isExist(service.accountExist(prefix, receiver));
        if (!isReceiverExist) {
            Response response = service.createAccount(prefix, receiver, "BDT", "0.00", "USER");
            isReceiverExist = response.getStatus() == 200 ? true : false;
            System.out.println(response.getMessage());
        }
        //
        if (isSenderExist && isReceiverExist) {
            String to = prefix + "@" + receiver;
            Response response = service.makeTransaction(prefix, sender, "BDT", "1000.00", to);
            System.out.println(response.getMessage());
        }
    }

    @Test(expected = HttpClientErrorException.class)
    public void invalidReceiverUsernameTest() {
        //to: prefix@user-name/account-name validation test:
        String prefix = "CASH";
        String receiver = "rider_receiver";
        String sender = "rider_sender";
        jwtToken = generateJWToken(sender, "1234", "User");
        System.out.println(jwtToken);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8086/api/account/v2")
                .build();
        VAccountService service = new VAccountService(restTemplate, jwtToken);
        //
        boolean isSenderExist = VAccountResponseParser.isExist(service.accountExist(prefix, sender));
        if (!isSenderExist) {
            Response response = service.createAccount(prefix, sender, "BDT", "0.00", "USER");
            isSenderExist = response.getStatus() == 200 ? true : false;
            System.out.println(response.getMessage());
        }
        boolean isReceiverExist = VAccountResponseParser.isExist(service.accountExist(prefix, receiver));
        if (!isReceiverExist) {
            Response response = service.createAccount(prefix, receiver, "BDT", "0.00", "USER");
            isReceiverExist = response.getStatus() == 200 ? true : false;
            System.out.println(response.getMessage());
        }
        //
        if (isSenderExist && isReceiverExist) {
            //Invalid receiver:
            String to = receiver; //prefix + "@" + receiver;
            try {
                Response response = service.makeTransaction(prefix, sender, "BDT", "1000.00", to);
                System.out.println(response.getMessage());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw e;
            }
        }
    }

    @Test
    public void makeTransactionTestFromSumBalanceToZeroBalance() {
        //Create 2 accounts: one for company and another for a rider
        //Rider goto customer home and deliver the package.
    }

    @Test
    public void makeWithdrawalFromZeroBalance() {
        //Create a account with zero balance and try to make a withdraw:
        String prefix = "CASH";
        String username = "wdth_zero_bal";
        jwtToken = generateJWToken(username, "1234", "User");
        System.out.println(jwtToken);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8086/api/account/v2")
                .build();
        VAccountService service = new VAccountService(restTemplate, jwtToken);
        //
        boolean isExist = VAccountResponseParser.isExist(service.accountExist(prefix, username));
        if (!isExist) {
            Response response = service.createAccount(prefix, username, "BDT", "0.00", "USER");
            isExist = (response != null && response.getStatus() == 200) ? true : false;
        }
        if (isExist) {
            Response response = service.makeWithdrawal(username, "BDT", "1.00");
            System.out.println(response.getMessage());
        }
    }

    @Test(expected = HttpClientErrorException.class)
    public void makeDepositWithNegativeBalance() {
        //Create a account with zero balance and try to make a withdraw:
        String prefix = "CASH";
        String username = "dep_neg_bal";
        jwtToken = generateJWToken(username, "1234", "User");
        System.out.println(jwtToken);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8086/api/account/v2")
                .build();
        VAccountService service = new VAccountService(restTemplate, jwtToken);
        //
        boolean isExist = VAccountResponseParser.isExist(service.accountExist(prefix, username));
        if (!isExist) {
            Response response = service.createAccount(prefix, username, "BDT", "0.00", "USER");
            isExist = (response != null && response.getStatus() == 200) ? true : false;
        }
        if (isExist) {
            try {
                Response response = service.makeDeposit(username, "BDT", "-1.00");
                System.out.println(response.getMessage());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw e;
            }
        }
    }

}