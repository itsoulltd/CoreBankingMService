package com.infoworks.lab.components.presenters.Payments.parser;

import com.infoworks.lab.rest.models.Message;
import com.infoworks.lab.rest.models.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VAccountResponseParser {

    public static Map<String, Object> parsePayload(Response response) throws IOException {
        if (response == null)
            throw new IOException("Response can't be null!");
        if (response.getStatus() >= 400)
            throw new IOException("Response status:" + response.getStatus()
                    + "; error: " + response.getError());
        //Let's parse payload:
        Map<String, Object> payload = Message.unmarshal(Map.class, response.getPayload());
        return payload == null ? new HashMap<>() : payload;
    }

    public static String getUsername(Response response) {
        try {
            Map<String, Object> payload = parsePayload(response);
            return Optional.ofNullable(payload.get("username")).orElse("").toString();
        } catch (IOException e) {}
        return null;
    }

    public static String getTitle(Response response) {
        try {
            Map<String, Object> payload = parsePayload(response);
            return Optional.ofNullable(payload.get("title")).orElse("").toString();
        } catch (IOException e) {}
        return null;
    }

    public static BigDecimal getBalance(Response response) {
        try {
            Map<String, Object> payload = parsePayload(response);
            String val = Optional.ofNullable(payload.get("balance")).orElse("0.00").toString();
            return new BigDecimal(val);
        } catch (IOException e) {}
        return BigDecimal.ZERO;
    }

    public static BigDecimal getAmount(Response response) {
        try {
            Map<String, Object> payload = parsePayload(response);
            String val = Optional.ofNullable(payload.get("balance")).orElse("0.00").toString();
            return new BigDecimal(val);
        } catch (IOException e) {}
        return BigDecimal.ZERO;
    }

    public static String getCurrency(Response response) {
        try {
            Map<String, Object> payload = parsePayload(response);
            return Optional.ofNullable(payload.get("currency")).orElse("").toString();
        } catch (IOException e) {}
        return null;
    }

    public static String getCurrencyDisplayName(Response response) {
        try {
            Map<String, Object> payload = parsePayload(response);
            return Optional.ofNullable(payload.get("currencyDisplayName")).orElse("").toString();
        } catch (IOException e) {}
        return null;
    }

    public static boolean isExist(Response response) {
        try {
            Map<String, Object> payload = parsePayload(response);
            String val = Optional.ofNullable(payload.get("exist")).orElse("false").toString();
            return Boolean.parseBoolean(val);
        } catch (IOException e) {}
        return false;
    }
}
