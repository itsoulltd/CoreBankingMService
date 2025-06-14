package com.infoworks.lab.config;

import java.util.Optional;

public class ApplicationProperties {

    public static final int APP_MAX_SIZE_IN_MB = 2;
    public static final String SAMPLE_CREATE_ORDER_XLSX = "/Download/file_example_XLSX_50.xlsx";
    public static final String SAMPLE_CREATE_ORDER_XLS = "/Download/file_example_XLS_10.xls";

    public static Boolean IS_AUTH_DISABLE = Boolean.parseBoolean(
            Optional.ofNullable(
                    System.getProperty("app.auth.disable") != null
                            ? System.getProperty("app.auth.disable")
                            : System.getenv("app.auth.disable")
            ).orElse("false")
    );

    public static String GOOGLE_MAP_API_KEY = Optional.ofNullable(
            System.getProperty("google.maps.api") != null
                    ? System.getProperty("google.maps.api")
                    : System.getenv("google.maps.api")
    ).orElse(null);

    public static String APP_DISPLAY_NAME = Optional.ofNullable(
            System.getProperty("app.display.name") != null
                    ? System.getProperty("app.display.name")
                    : System.getenv("app.display.name")
    ).orElse("MyApp");

    public static String APP_DISPLAY_VERSION = Optional.ofNullable(
            System.getProperty("app.display.version") != null
                    ? System.getProperty("app.display.version")
                    : System.getenv("app.display.version")
    ).orElse("v1.0");

    public static String APP_DISPLAY_MODE = Optional.ofNullable(
            System.getProperty("app.display.mode") != null
                    ? System.getProperty("app.display.mode")
                    : System.getenv("app.display.mode")
    ).orElse("DEV");

    public static String CURRENCY = Optional.ofNullable(
            System.getProperty("app.default.currency") != null
                    ? System.getProperty("app.default.currency")
                    : System.getenv("app.default.currency")
    ).orElse("BDT");

    public static String APP_ACCOUNT_CASH_PREFIX = Optional.ofNullable(
            System.getProperty("app.account.cash.prefix") != null
                    ? System.getProperty("app.account.cash.prefix")
                    : System.getenv("app.account.cash.prefix")
    ).orElse("CASH");

    public static String APP_ACCOUNT_CASH_NAME = Optional.ofNullable(
            System.getProperty("app.account.cash.name") != null
                    ? System.getProperty("app.account.cash.name")
                    : System.getenv("app.account.cash.name")
    ).orElse("Master");

    public static String APP_ACCOUNT_REVENUE_PREFIX = Optional.ofNullable(
            System.getProperty("app.account.revenue.prefix") != null
                    ? System.getProperty("app.account.revenue.prefix")
                    : System.getenv("app.account.revenue.prefix")
    ).orElse("REVENUE");

    public static String APP_ACCOUNT_REVENUE_NAME = Optional.ofNullable(
            System.getProperty("app.account.revenue.name") != null
                    ? System.getProperty("app.account.revenue.name")
                    : System.getenv("app.account.revenue.name")
    ).orElse("Master");

    /*public static String ABC_XY = Optional.ofNullable(
            System.getProperty("") != null ? System.getProperty("") : System.getenv("")
    ).orElse("");*/
}
