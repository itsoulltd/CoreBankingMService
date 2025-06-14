package com.infoworks.lab.webapp.config;

import com.infoworks.lab.beans.tasks.definition.TaskQueue;
import com.infoworks.lab.domain.models.CreateAccount;
import com.infoworks.lab.domain.types.AccountPrefix;
import com.infoworks.lab.domain.types.AccountType;
import com.infoworks.lab.rest.models.Response;
import com.infoworks.lab.services.ledger.LedgerBook;
import com.infoworks.lab.services.vaccount.CreateChartOfAccountTask;
import com.infoworks.lab.services.vaccount.InitializeVAccountDB;
import com.it.soul.lab.sql.SQLExecutor;
import com.itsoul.lab.ledgerbook.connector.SourceConnector;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.logging.Logger;

@Component
public class StartupConfig implements CommandLineRunner {

    private Logger LOG = Logger.getLogger(this.getClass().getSimpleName());

    private SourceConnector connector;
    private LedgerBook ledgerBook;
    private String appSrcDir;

    public StartupConfig(@Qualifier("DSConnector") SourceConnector connector
            , @Qualifier("GeneralLedger-Unscoped") LedgerBook ledgerBook
            , @Value("${app.src.directory}") String appSrcDir) {
        this.connector = connector;
        this.ledgerBook = ledgerBook;
        this.appSrcDir = appSrcDir;
    }

    @Override
    public void run(String... args) throws Exception {
        boolean notInitialized = true;
        try(SQLExecutor executor = new SQLExecutor(connector.getConnection())) {
            notInitialized = executor.getScalerValue("SELECT COUNT(*) FROM account") <= 0;
        } catch (Exception e) {}
        if (notInitialized) initializeVAccount();
    }

    private void initializeVAccount() throws SQLException {
        TaskQueue taskQueue = TaskQueue.createSync(true);
        InitializeVAccountDB task = new InitializeVAccountDB(appSrcDir, connector);
        taskQueue.onTaskComplete((message, state) -> {
            if (message instanceof Response){
                Response response = (Response) message;
                if (response.getStatus() == HttpStatus.CREATED.value()){
                    LOG.info(((Response) message).getMessage());
                    //Calling For Next Task: Create Master Account:
                    initializeMasterAccount();
                    //
                }else {
                    LOG.info(((Response) message).getMessage());
                }
            }
        });
        //
        taskQueue.add(task);
    }

    private void initializeMasterAccount() {
        CreateAccount cashAccount = new CreateAccount()
                .setPrefix(AccountPrefix.CASH.name())
                .setUsername(AccountType.MASTER.value())
                .setAmount("10000000000.00");
        CreateChartOfAccountTask cashTask = new CreateChartOfAccountTask(ledgerBook, cashAccount);

        CreateAccount revenueAccount = new CreateAccount()
                .setPrefix(AccountPrefix.REVENUE.name())
                .setUsername(AccountType.MASTER.value())
                .setAmount("0.00");
        CreateChartOfAccountTask revenueTask = new CreateChartOfAccountTask(ledgerBook, revenueAccount);
        //
        TaskQueue taskQueue = TaskQueue.createSync(true);
        taskQueue.onTaskComplete(((message, state) -> {
            //LOG.info("CASH#Master Account Creation: " + state.name());
            if (message != null && message instanceof Response){
                Response response = (Response) message;
                LOG.info(response.toString());
            }
        }));
        //
        taskQueue.add(cashTask);
        taskQueue.add(revenueTask);
    }
}
