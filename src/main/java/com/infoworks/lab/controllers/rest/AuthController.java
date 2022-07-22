package com.infoworks.lab.controllers.rest;

import com.infoworks.lab.domain.models.CreateAccount;
import com.infoworks.lab.domain.models.LoginRequest;
import com.infoworks.lab.domain.models.NewAccountRequest;
import com.infoworks.lab.jjwt.JWTHeader;
import com.infoworks.lab.jjwt.JWTPayload;
import com.infoworks.lab.jjwt.TokenValidator;
import com.infoworks.lab.jwtoken.definition.TokenProvider;
import com.infoworks.lab.jwtoken.services.JWTokenProvider;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.webapp.config.AuthorizationFilter;
import com.infoworks.lab.webapp.config.JWTokenValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Date;

@RestController
@RequestMapping("/auth/v1")
public class AuthController {

    private static Logger LOG = LoggerFactory.getLogger("AuthController");
    private PasswordEncoder passwordEncoder;
    private AccountController accountController;

    public AuthController(PasswordEncoder passwordEncoder
            , AccountController accountController) {
        this.passwordEncoder = passwordEncoder;
        this.accountController = accountController;
    }

    @GetMapping("/isValidToken")
    public ResponseEntity<String> isValid(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token
                    , @ApiIgnore @AuthenticationPrincipal UserDetails principal){
        //
        Response response = new Response().
                setStatus(HttpStatus.NOT_IMPLEMENTED.value())
                .setMessage("IsValidToken is under construction!");
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response.toString());
    }

    @GetMapping("/refreshToken")
    public ResponseEntity<String> refreshToken(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token
            , @ApiIgnore @AuthenticationPrincipal UserDetails principal){
        //
        Response response = new Response().
                setStatus(HttpStatus.NOT_IMPLEMENTED.value())
                .setMessage("RefreshToken is under construction!");
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response.toString());
    }

    @PostMapping("/new/account")
    public ResponseEntity<String> newAccount(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token
            , @ApiIgnore @AuthenticationPrincipal UserDetails principal
            , @Valid @RequestBody NewAccountRequest account){
        Response response = new Response().setStatus(HttpStatus.OK.value())
                .setMessage("Hello NewAccount");
        //First Create New User:
        //...
        //Then Create A New Bank-Account For that User:
        CreateAccount createAccount = new CreateAccount();
        createAccount.setUsername(account.getUsername());
        createAccount.setPrefix("CASH");
        createAccount.setAccountType("USER");
        createAccount.setCurrency(account.getCurrency());
        createAccount.setAmount(account.getAmount());
        ResponseEntity<Response> res = accountController.createVAccount(token, createAccount);
        return ResponseEntity.ok(response.toString());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request){
        Response response = new Response().setStatus(HttpStatus.OK.value())
                .setMessage("Hello Login");
        //Generate Jwt-Token:
        String kid = JWTokenValidator.getRandomSecretKey();
        String secret = JWTokenValidator.getSecretKeyMap().get(kid);
        //Create the JWT-Token:
        JWTHeader header = new JWTHeader().setTyp("round").setKid(kid);
        JWTPayload payload = new JWTPayload().setSub(request.getUsername())
                .setIss(request.getUsername())
                .setIat(new Date().getTime())
                .setExp(TokenProvider.defaultTokenTimeToLive().getTimeInMillis())
                .addData(AuthorizationFilter.AUTHORITIES_KEY, request.getRole());
        //
        TokenProvider token = new JWTokenProvider(secret)
                .setHeader(header)
                .setPayload(payload);
        //
        String tokenKey = token.generateToken(TokenProvider.defaultTokenTimeToLive());
        LOG.info(tokenKey);
        response.setMessage(tokenKey);
        return ResponseEntity.ok(response.toString());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token
            , @ApiIgnore @AuthenticationPrincipal UserDetails principal){
        //
        Response response = new Response().
                setStatus(HttpStatus.NOT_IMPLEMENTED.value())
                .setMessage("Logout is under construction!");
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response.toString());
    }

    @GetMapping("/forget")
    public ResponseEntity<String> forget(@RequestParam String email){
        //
        Response response = new Response().
                setStatus(HttpStatus.NOT_IMPLEMENTED.value())
                .setMessage("Forget is under construction!");
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response.toString());
    }

    @GetMapping("/reset")
    public ResponseEntity<String> reset(@RequestParam String resetToken){
        //
        Response response = new Response().
                setStatus(HttpStatus.NOT_IMPLEMENTED.value())
                .setMessage("Reset is under construction!");
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response.toString());
    }

}
