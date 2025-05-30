package com.infoworks.lab.domain.repository;

import com.infoworks.lab.config.RequestURI;
import com.infoworks.lab.config.RestTemplateConfig;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.rest.models.SearchQuery;
import com.vaadin.flow.component.UI;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VAccountRepository {

    protected RestTemplate template;
    private String token;
    private String baseUri;

    public VAccountRepository(RestTemplate template, String token) {
        this.template = template;
        this.token = token;
    }

    public VAccountRepository(RestTemplate template) {
        this(template, null);
    }

    public VAccountRepository(String token) {
        this(null, token);
    }

    public VAccountRepository() {
        this(null, null);
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token == null ? AuthRepository.parseToken(UI.getCurrent()) : token;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public String getBaseUri() {
        if (baseUri == null)
            return RequestURI.PAYMENT_BASE + RequestURI.PAYMENT_API;
        return baseUri;
    }

    public RestTemplate getTemplate() {
        if (template == null) {
            template = RestTemplateConfig.getTemplate();
        }
        return template;
    }

    protected HttpHeaders createHeaderFrom(String token) {
        return RequestURI.createHeaderFrom(token);
    }

    /**
     * AccountExist
     * @param prefix e.g. 'CASH', 'REVENUE'
     * @param username e.g. user-name that need to be remember.
     * @return
     * @throws RuntimeException
     */
    public Response accountExist(String prefix, String username) throws RuntimeException {
        try {
            HttpHeaders headers = createHeaderFrom(getToken());
            Map body = new HashMap();
            HttpEntity<Map> entity = new HttpEntity<>(body, headers);
            //https://localhost/api/account/v2/exist?prefix={prefix}&username={username}
            StringBuilder rootUri = new StringBuilder(getBaseUri() + "/exist?prefix={prefix}&username={username}");
            ResponseEntity<Response> rs = getTemplate().exchange(rootUri.toString()
                    , HttpMethod.GET
                    , entity
                    , Response.class
                    , prefix, username);
            return rs.getBody();
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    /**
     * AccountBalance
     * @param prefix e.g. 'CASH', 'REVENUE'
     * @param username e.g. user-name that need to be remember.
     * @return
     * @throws RuntimeException
     */
    public Response accountBalance(String prefix, String username) throws RuntimeException {
        try {
            HttpHeaders headers = createHeaderFrom(getToken());
            Map body = new HashMap();
            HttpEntity<Map> entity = new HttpEntity<>(body, headers);
            //https://localhost/api/account/v2/balance?prefix={prefix}&username={username}
            StringBuilder rootUri = new StringBuilder(getBaseUri() + "/balance?prefix={prefix}&username={username}");
            ResponseEntity<Response> rs = getTemplate().exchange(rootUri.toString()
                    , HttpMethod.GET
                    , entity
                    , Response.class
                    , prefix, username);
            return rs.getBody();
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    /**
     * RecentTransactions
     * @param prefix e.g. 'CASH', 'REVENUE'
     * @param username e.g. user-name that need to be remember.
     * @return
     * @throws RuntimeException
     */
    public List<Map> recentTransactions(String prefix, String username) throws RuntimeException {
        try {
            HttpHeaders headers = createHeaderFrom(getToken());
            Map body = new HashMap();
            HttpEntity<Map> entity = new HttpEntity<>(body, headers);
            //https://localhost/api/account/v2/recent/transactions?prefix={prefix}&username={username}
            StringBuilder rootUri = new StringBuilder(getBaseUri() + "/recent/transactions?prefix={prefix}&username={username}");
            ResponseEntity<List<Map>> rs = getTemplate().exchange(rootUri.toString()
                    , HttpMethod.GET
                    , entity
                    , new ParameterizedTypeReference<List<Map>>() {}
                    , prefix, username);
            return rs.getBody();
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
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
        try {
            HttpHeaders headers = createHeaderFrom(getToken());
            HttpEntity<SearchQuery> entity = new HttpEntity<>(query, headers);
            //https://localhost/api/account/v2/search/transactions?prefix={prefix}&username={username}
            StringBuilder rootUri = new StringBuilder(getBaseUri() + "/search/transactions?prefix={prefix}&username={username}");
            ResponseEntity<List<Map>> rs = getTemplate().exchange(rootUri.toString()
                    , HttpMethod.POST
                    , entity
                    , new ParameterizedTypeReference<List<Map>>() {}
                    , prefix, username);
            return rs.getBody();
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
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
        try {
            HttpHeaders headers = createHeaderFrom(getToken());
            Map body = new HashMap();
            body.put("prefix", prefix);
            body.put("username", username);
            body.put("currency", currency);
            body.put("amount", amount);
            body.put("accountType", accountType);
            HttpEntity<Map> entity = new HttpEntity<>(body, headers);
            //https://localhost/api/account/v2/new/account
            StringBuilder rootUri = new StringBuilder(getBaseUri() + "/new/account");
            ResponseEntity<Response> rs = getTemplate().exchange(rootUri.toString()
                    , HttpMethod.POST
                    , entity
                    , Response.class);
            return rs.getBody();
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
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
        try {
            HttpHeaders headers = createHeaderFrom(getToken());
            Map body = new HashMap();
            body.put("prefix", prefix);
            body.put("username", username);
            body.put("currency", currency);
            body.put("amount", amount);
            body.put("to", to);
            HttpEntity<Map> entity = new HttpEntity<>(body, headers);
            //https://localhost/api/account/v2/make/transaction
            StringBuilder rootUri = new StringBuilder(getBaseUri() + "/make/transaction");
            ResponseEntity<Response> rs = getTemplate().exchange(rootUri.toString()
                    , HttpMethod.POST
                    , entity
                    , Response.class);
            return rs.getBody();
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
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
        try {
            HttpHeaders headers = createHeaderFrom(getToken());
            Map body = new HashMap();
            body.put("username", username);
            body.put("currency", currency);
            body.put("amount", amount);
            HttpEntity<Map> entity = new HttpEntity<>(body, headers);
            //https://localhost/api/account/v2/make/deposit
            StringBuilder rootUri = new StringBuilder(getBaseUri() + "/make/deposit");
            ResponseEntity<Response> rs = getTemplate().exchange(rootUri.toString()
                    , HttpMethod.POST
                    , entity
                    , Response.class);
            return rs.getBody();
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
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
        try {
            HttpHeaders headers = createHeaderFrom(getToken());
            Map body = new HashMap();
            body.put("username", username);
            body.put("currency", currency);
            body.put("amount", amount);
            HttpEntity<Map> entity = new HttpEntity<>(body, headers);
            //https://localhost/api/account/v2/make/withdrawal
            StringBuilder rootUri = new StringBuilder(getBaseUri() + "/make/withdrawal");
            ResponseEntity<Response> rs = getTemplate().exchange(rootUri.toString()
                    , HttpMethod.POST
                    , entity
                    , Response.class);
            return rs.getBody();
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    /**
    public Response template(String prefix
            , String username
            , String currency
            , String amount
            , String accountType) throws RuntimeException {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    } */

}
