package com.infoworks.lab.services;

import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.rest.models.SearchQuery;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VAccountServiceV2 {

    private RestTemplate template;
    private String token;

    public VAccountServiceV2(RestTemplate template) {
        this.template = template;
    }

    public VAccountServiceV2(RestTemplate template, String token) {
        this(template);
        this.token = token;
    }

    public void setTemplate(RestTemplate template) {
        this.template = template;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token == null ? "" : token;
    }

    protected HttpHeaders createHeaderFrom(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (token.startsWith("Bearer")){
            httpHeaders.set(HttpHeaders.AUTHORIZATION, token);
        } else {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return httpHeaders;
    }

    /**
     * AccountExist
     * @param prefix e.g. 'CASH', 'REVENUE'
     * @param username e.g. user-name that need to be remember.
     * @return
     * @throws RuntimeException
     */
    public Response accountExist(String prefix, String username) throws RuntimeException {
        HttpHeaders headers = createHeaderFrom(getToken());
        Map body = new HashMap();
        HttpEntity<Map> entity = new HttpEntity<>(body, headers);
        //https://localhost/api/account/v2/exist?prefix={prefix}&username={username}
        StringBuilder rootUri = new StringBuilder(((RootUriTemplateHandler)template.getUriTemplateHandler()).getRootUri());
        rootUri.append("/exist?prefix={prefix}&username={username}");
        ResponseEntity<Response> rs = template.exchange(rootUri.toString()
                , HttpMethod.GET
                , entity
                , Response.class
                , prefix, username);
        return rs.getBody();
    }

    /**
     * AccountBalance
     * @param prefix e.g. 'CASH', 'REVENUE'
     * @param username e.g. user-name that need to be remember.
     * @return
     * @throws RuntimeException
     */
    public Response accountBalance(String prefix, String username) throws RuntimeException {
        HttpHeaders headers = createHeaderFrom(getToken());
        Map body = new HashMap();
        HttpEntity<Map> entity = new HttpEntity<>(body, headers);
        //https://localhost/api/account/v2/balance?prefix={prefix}&username={username}
        StringBuilder rootUri = new StringBuilder(((RootUriTemplateHandler)template.getUriTemplateHandler()).getRootUri());
        rootUri.append("/balance?prefix={prefix}&username={username}");
        ResponseEntity<Response> rs = template.exchange(rootUri.toString()
                , HttpMethod.GET
                , entity
                , Response.class
                , prefix, username);
        return rs.getBody();
    }

    /**
     * RecentTransactions
     * @param prefix e.g. 'CASH', 'REVENUE'
     * @param username e.g. user-name that need to be remember.
     * @return
     * @throws RuntimeException
     */
    public List<Map> recentTransactions(String prefix, String username) throws RuntimeException {
        HttpHeaders headers = createHeaderFrom(getToken());
        Map body = new HashMap();
        HttpEntity<Map> entity = new HttpEntity<>(body, headers);
        //https://localhost/api/account/v2/recent/transactions?prefix={prefix}&username={username}
        StringBuilder rootUri = new StringBuilder(((RootUriTemplateHandler)template.getUriTemplateHandler()).getRootUri());
        rootUri.append("/recent/transactions?prefix={prefix}&username={username}");
        ResponseEntity<List<Map>> rs = template.exchange(rootUri.toString()
                , HttpMethod.GET
                , entity
                , new ParameterizedTypeReference<List<Map>>() {}
                , prefix, username);
        return rs.getBody();
    }

    /**
     * SearchTransactions
     * @param prefix e.g. 'CASH', 'REVENUE'
     * @param username e.g. user-name that need to be remember.
     * @param query
     * @return
     * @throws RuntimeException
     */
    public List<Map> searchTransactions(String prefix, String username, SearchQuery query) throws RuntimeException {
        HttpHeaders headers = createHeaderFrom(getToken());
        HttpEntity<SearchQuery> entity = new HttpEntity<>(query, headers);
        //https://localhost/api/account/v2/search/transactions?prefix={prefix}&username={username}
        StringBuilder rootUri = new StringBuilder(((RootUriTemplateHandler)template.getUriTemplateHandler()).getRootUri());
        rootUri.append("/search/transactions?prefix={prefix}&username={username}");
        ResponseEntity<List<Map>> rs = template.exchange(rootUri.toString()
                , HttpMethod.POST
                , entity
                , new ParameterizedTypeReference<List<Map>>() {}
                , prefix, username);
        return rs.getBody();
    }

    /**
     * CreateAccount
     * @param prefix e.g. 'CASH', 'REVENUE'
     * @param username e.g. user-name that need to be remember.
     * @param currency e.g. BDT, USD, EUR etc
     * @param amount e.g. 1.00 or 12.09 or 0.00 or Any sum with at least 2 digit after precision.
     * @param accountType e.g. MASTER or USER. Most of the time 'USER'
     * @return
     * @throws RuntimeException
     */
    public Response createAccount(String prefix
            , String username
            , String currency
            , String amount
            , String accountType) throws RuntimeException {
        //
        HttpHeaders headers = createHeaderFrom(getToken());
        Map body = new HashMap();
        body.put("prefix", prefix);
        body.put("username", username);
        body.put("currency", currency);
        body.put("amount", amount);
        body.put("accountType", accountType);
        HttpEntity<Map> entity = new HttpEntity<>(body, headers);
        //https://localhost/api/account/v2/new/account
        StringBuilder rootUri = new StringBuilder(((RootUriTemplateHandler)template.getUriTemplateHandler()).getRootUri());
        rootUri.append("/new/account");
        ResponseEntity<Response> rs = template.exchange(rootUri.toString()
                , HttpMethod.POST
                , entity
                , Response.class);
        return rs.getBody();
    }

    /**
     * MakeTransaction
     * @param prefix e.g. 'CASH', 'REVENUE'
     * @param username e.g. user-name that need to be remember.
     * @param currency e.g. BDT, USD, EUR etc
     * @param amount e.g. 1.00 or 12.09 or 0.00 or Any sum with at least 2 digit after precision.
     * @param to pattern: prefix@username. e.g. CASH@sohana, CASH@Master, REVENUE@Master etc
     * @return
     * @throws RuntimeException
     */
    public Response makeTransaction(String prefix
            , String username
            , String currency
            , String amount
            , String to) throws RuntimeException {
        HttpHeaders headers = createHeaderFrom(getToken());
        Map body = new HashMap();
        body.put("prefix", prefix);
        body.put("username", username);
        body.put("currency", currency);
        body.put("amount", amount);
        body.put("to", to);
        HttpEntity<Map> entity = new HttpEntity<>(body, headers);
        //https://localhost/api/account/v2/make/transaction
        StringBuilder rootUri = new StringBuilder(((RootUriTemplateHandler)template.getUriTemplateHandler()).getRootUri());
        rootUri.append("/make/transaction");
        ResponseEntity<Response> rs = template.exchange(rootUri.toString()
                , HttpMethod.POST
                , entity
                , Response.class);
        return rs.getBody();
    }

    /**
     * MakeTransactionOnHandover
     * @param prefix e.g. 'CASH', 'REVENUE'
     * @param username e.g. user-name that need to be remember.
     * @param currency e.g. BDT, USD, EUR etc
     * @param amount e.g. 1.00 or 12.09 or 0.00 or Any sum with at least 2 digit after precision.
     * @param to pattern: prefix@username. e.g. CASH@sohana, CASH@Master, REVENUE@Master etc
     * @param baseCharge e.g. 1.00 or 12.09 or 0.00 or Any sum with at least 2 digit after precision.
     * @param serviceCharge e.g. 1.00 or 12.09 or 0.00 or Any sum with at least 2 digit after precision.
     * @return
     * @throws RuntimeException
     */
    public Response makeTransactionOnHandover(String prefix
            , String username
            , String currency
            , String amount
            , String to, String baseCharge, String serviceCharge) throws RuntimeException {
        HttpHeaders headers = createHeaderFrom(getToken());
        Map body = new HashMap();
        body.put("prefix", prefix);
        body.put("username", username);
        body.put("currency", currency);
        body.put("amount", amount);
        body.put("to", to);
        body.put("baseCharge", baseCharge);
        body.put("serviceCharge", serviceCharge);
        HttpEntity<Map> entity = new HttpEntity<>(body, headers);
        //https://localhost/api/account/v2/make/transaction/on/handover
        StringBuilder rootUri = new StringBuilder(((RootUriTemplateHandler)template.getUriTemplateHandler()).getRootUri());
        rootUri.append("/make/transaction/on/handover");
        ResponseEntity<Response> rs = template.exchange(rootUri.toString()
                , HttpMethod.POST
                , entity
                , Response.class);
        return rs.getBody();
    }

    /**
     * MakeTransactionOnCompletion
     * @param prefix e.g. 'CASH', 'REVENUE'
     * @param username e.g. user-name that need to be remember.
     * @param currency e.g. BDT, USD, EUR etc
     * @param amount e.g. 1.00 or 12.09 or 0.00 or Any sum with at least 2 digit after precision.
     * @param to pattern: prefix@username. e.g. CASH@sohana, CASH@Master, REVENUE@Master etc
     * @param baseCharge e.g. 1.00 or 12.09 or 0.00 or Any sum with at least 2 digit after precision.
     * @param serviceCharge e.g. 1.00 or 12.09 or 0.00 or Any sum with at least 2 digit after precision.
     * @return
     * @throws RuntimeException
     */
    public Response makeTransactionOnCompletion(String prefix
            , String username
            , String currency
            , String amount
            , String to, String baseCharge, String serviceCharge) throws RuntimeException {
        HttpHeaders headers = createHeaderFrom(getToken());
        Map body = new HashMap();
        body.put("prefix", prefix);
        body.put("username", username);
        body.put("currency", currency);
        body.put("amount", amount);
        body.put("to", to);
        body.put("baseCharge", baseCharge);
        body.put("serviceCharge", serviceCharge);
        HttpEntity<Map> entity = new HttpEntity<>(body, headers);
        //https://localhost/api/account/v2/make/transaction/on/completion
        StringBuilder rootUri = new StringBuilder(((RootUriTemplateHandler)template.getUriTemplateHandler()).getRootUri());
        rootUri.append("/make/transaction/on/completion");
        ResponseEntity<Response> rs = template.exchange(rootUri.toString()
                , HttpMethod.POST
                , entity
                , Response.class);
        return rs.getBody();
    }

    /**
     * MakeDeposit
     * @param username e.g. user-name that need to be remember.
     * @param currency e.g. BDT, USD, EUR etc
     * @param amount e.g. 1.00 or 12.09 or 0.00 or Any sum with at least 2 digit after precision.
     * @return
     * @throws RuntimeException
     */
    public Response makeDeposit(String username
            , String currency
            , String amount) throws RuntimeException {
        HttpHeaders headers = createHeaderFrom(getToken());
        Map body = new HashMap();
        body.put("username", username);
        body.put("currency", currency);
        body.put("amount", amount);
        HttpEntity<Map> entity = new HttpEntity<>(body, headers);
        //https://localhost/api/account/v2/make/deposit
        StringBuilder rootUri = new StringBuilder(((RootUriTemplateHandler)template.getUriTemplateHandler()).getRootUri());
        rootUri.append("/make/deposit");
        ResponseEntity<Response> rs = template.exchange(rootUri.toString()
                , HttpMethod.POST
                , entity
                , Response.class);
        return rs.getBody();
    }

    /**
     * MakeWithdrawal
     * @param username e.g. user-name that need to be remember.
     * @param currency e.g. BDT, USD, EUR etc
     * @param amount e.g. 1.00 or 12.09 or 0.00 or Any sum with at least 2 digit after precision.
     * @return
     * @throws RuntimeException
     */
    public Response makeWithdrawal(String username
            , String currency
            , String amount) throws RuntimeException {
        HttpHeaders headers = createHeaderFrom(getToken());
        Map body = new HashMap();
        body.put("username", username);
        body.put("currency", currency);
        body.put("amount", amount);
        HttpEntity<Map> entity = new HttpEntity<>(body, headers);
        //https://localhost/api/account/v2/make/withdrawal
        StringBuilder rootUri = new StringBuilder(((RootUriTemplateHandler)template.getUriTemplateHandler()).getRootUri());
        rootUri.append("/make/withdrawal");
        ResponseEntity<Response> rs = template.exchange(rootUri.toString()
                , HttpMethod.POST
                , entity
                , Response.class);
        return rs.getBody();
    }

    /**
    public Response template(String prefix
            , String username
            , String currency
            , String amount
            , String accountType) throws RuntimeException {
        HttpHeaders headers = createHeaderFrom(getToken());
        Map body = new HashMap();
        HttpEntity<Map> entity = new HttpEntity<>(body, headers);
        //https://localhost/api/account/v2/
        StringBuilder rootUri = new StringBuilder(((RootUriTemplateHandler)template.getUriTemplateHandler()).getRootUri());
        rootUri.append("/<>/<>");
        ResponseEntity<Response> rs = template.exchange(rootUri.toString()
                , HttpMethod.POST
                , entity
                , Response.class);
        return rs.getBody();
    } */

}
