package com.infoworks.lab.webapp.config;

import com.infoworks.lab.services.ledger.LedgerBook;
import com.itsoul.lab.ledgerbook.connector.SQLConnector;
import com.itsoul.lab.ledgerbook.connector.SQLDataSourceConnector;
import com.itsoul.lab.ledgerbook.connector.SourceConnector;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;

@Configuration
public class BeanConfig {

    private Environment env;

    public BeanConfig(Environment env) {
        this.env = env;
    }

    @Bean("TenantID")
    String getTenantID(){
        return env.getProperty("app.tenant.id");
    }

    @Bean("LedgerBookUsername")
    String getLedgerBookUser(){
        return env.getProperty("app.ledger.book.username");
    }

    @Bean("LedgerBookPassword")
    String getLedgerBookPassword(){
        return env.getProperty("app.ledger.book.password");
    }

    @Bean("LedgerBookCurrency")
    String getCurrency(){
        return env.getProperty("app.ledger.book.currency");
    }

    @Bean("SQLConnector")
    //@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    SourceConnector getSourceConnector(@Value("${spring.datasource.url}") String dbUrl
            , @Value("${spring.datasource.driver-class-name}") String driver
            , @Value("${app.db.username}") String username
            , @Value("${app.db.password}") String password
            , @Value("${app.db.file.schema}") String fileSchema){
        //
        SourceConnector connector = new SQLConnector(driver)
                .url(dbUrl)
                .username(username)
                .password(password)
                .schema(fileSchema)
                .skipSchemaGeneration(true);
        return connector;
    }

    @Bean("DSConnector")
    SourceConnector getDataSourceConnector(@Value("${spring.datasource.url}") String dbUrl
            , @Value("${spring.datasource.driver-class-name}") String driver
            , @Value("${app.db.username}") String username
            , @Value("${app.db.password}") String password
            , @Value("${app.db.file.schema}") String fileSchema
            , DataSource dataSource) {
        //
        SourceConnector connector = new SQLDataSourceConnector(dataSource)
                .driverClassName(driver)
                .url(dbUrl)
                .username(username)
                .password(password)
                .schema(fileSchema)
                .skipSchemaGeneration(true);
        return connector;
    }

    @Bean("GeneralLedger")
    //@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public LedgerBook getGeneralLedger(@Qualifier("DSConnector") SourceConnector connector){
        return new LedgerBook(connector, getLedgerBookUser(), getLedgerBookPassword(), getTenantID(), getCurrency());
    }

    @Bean("GeneralLedger-Unscoped")
    public LedgerBook getUnscopedGeneralLedger(@Qualifier("DSConnector") SourceConnector connector){
        return new LedgerBook(connector, getLedgerBookUser(), getLedgerBookPassword(), getTenantID(), getCurrency());
    }

}
