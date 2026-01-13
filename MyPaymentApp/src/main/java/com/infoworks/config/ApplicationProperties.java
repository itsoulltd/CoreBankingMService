package com.infoworks.config;


import com.infoworks.utils.services.iProperties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class ApplicationProperties {

    public static iProperties properties;

    static {
        try (InputStream ios = ApplicationProperties.class
                .getClassLoader().getResourceAsStream("application.properties")) {
            if (ios == null) {
                throw new RuntimeException("application.properties not found");
            }
            properties = iProperties.createInMemory(ios, null);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static int APP_MAX_SIZE_IN_MB = Integer.parseInt(
            Optional.ofNullable(
                    System.getProperty("app.max.file.size.in.mb") != null
                            ? System.getProperty("app.max.file.size.in.mb")
                            : properties.read("app.max.file.size.in.mb")
            ).orElse("2")
    );

    public static final String SAMPLE_CREATE_ORDER_XLSX = "/Download/file_example_XLSX_50.xlsx";
    public static final String SAMPLE_CREATE_ORDER_XLS = "/Download/file_example_XLS_10.xls";

    public static Boolean IS_AUTH_DISABLE = Boolean.parseBoolean(
            Optional.ofNullable(
                    System.getProperty("app.auth.disable") != null
                            ? System.getProperty("app.auth.disable")
                            : properties.read("app.auth.disable")
            ).orElse("false")
    );

    public static String GOOGLE_MAP_API_KEY = Optional.ofNullable(
            System.getProperty("google.maps.api") != null
                    ? System.getProperty("google.maps.api")
                    : properties.read("google.maps.api")
    ).orElse(null);

    public static String APP_DISPLAY_NAME = Optional.ofNullable(
            System.getProperty("app.display.name") != null
                    ? System.getProperty("app.display.name")
                    : properties.read("app.display.name")
    ).orElse("MyApp");

    public static String APP_DISPLAY_VERSION = Optional.ofNullable(
            System.getProperty("app.display.version") != null
                    ? System.getProperty("app.display.version")
                    : properties.read("app.display.version")
    ).orElse("v1.0");

    public static String APP_DISPLAY_MODE = Optional.ofNullable(
            System.getProperty("app.display.mode") != null
                    ? System.getProperty("app.display.mode")
                    : properties.read("app.display.mode")
    ).orElse("DEV");

    public static String CURRENCY = Optional.ofNullable(
            System.getProperty("app.default.currency") != null
                    ? System.getProperty("app.default.currency")
                    : properties.read("app.default.currency")
    ).orElse("BDT");

    public static String APP_ACCOUNT_CASH_PREFIX = Optional.ofNullable(
            System.getProperty("app.account.cash.prefix") != null
                    ? System.getProperty("app.account.cash.prefix")
                    : properties.read("app.account.cash.prefix")
    ).orElse("CASH");

    public static String APP_ACCOUNT_CASH_NAME = Optional.ofNullable(
            System.getProperty("app.account.cash.name") != null
                    ? System.getProperty("app.account.cash.name")
                    : properties.read("app.account.cash.name")
    ).orElse("Master");

    public static String APP_ACCOUNT_REVENUE_PREFIX = Optional.ofNullable(
            System.getProperty("app.account.revenue.prefix") != null
                    ? System.getProperty("app.account.revenue.prefix")
                    : properties.read("app.account.revenue.prefix")
    ).orElse("REVENUE");

    public static String APP_ACCOUNT_REVENUE_NAME = Optional.ofNullable(
            System.getProperty("app.account.revenue.name") != null
                    ? System.getProperty("app.account.revenue.name")
                    : properties.read("app.account.revenue.name")
    ).orElse("Master");

    /*public static String ABC_XY = Optional.ofNullable(
            System.getProperty("") != null ? System.getProperty("") : properties.read("")
    ).orElse("");*/
}
