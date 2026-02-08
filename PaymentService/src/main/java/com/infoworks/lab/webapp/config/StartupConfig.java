package com.infoworks.lab.webapp.config;

import com.infoworks.lab.domain.models.CreateAccount;
import com.infoworks.lab.domain.types.AccountPrefix;
import com.infoworks.lab.domain.types.AccountType;
import com.infoworks.lab.services.ledger.LedgerBook;
import com.infoworks.lab.services.vaccount.CreateChartOfAccountTask;
import com.infoworks.lab.services.vaccount.InitializeVAccountDB;
import com.infoworks.ledgerbook.connector.SourceConnector;
import com.infoworks.objects.Response;
import com.infoworks.sql.executor.SQLExecutor;
import com.infoworks.tasks.queue.TaskQueue;
import com.infoworks.utils.eventq.EventQueue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Component
public class StartupConfig implements CommandLineRunner {

    private Logger LOG = Logger.getLogger(this.getClass().getSimpleName());

    private SourceConnector connector;
    private LedgerBook ledgerBook;
    private String appSrcDir;

    @Value("${server.app.domain}")
    private String serverDomain;

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.servlet.context-path}")
    private String servletContext;

    public StartupConfig(@Qualifier("DSConnector") SourceConnector connector
            , @Qualifier("GeneralLedger-Unscoped") LedgerBook ledgerBook
            , @Value("${app.src.directory}") String appSrcDir) {
        this.connector = connector;
        this.ledgerBook = ledgerBook;
        this.appSrcDir = appSrcDir;
    }

    @Override
    public void run(String... args) throws Exception {
        //Api-Doc Url:
        System.out.println(String.format("http://%s:%s%s/swagger-ui/index.html", serverDomain, serverPort, servletContext));
        //
        boolean notInitialized = true;
        try(SQLExecutor executor = new SQLExecutor(connector.getConnection())) {
            notInitialized = executor.getScalarValue("SELECT COUNT(*) FROM account") <= 0;
        } catch (Exception e) {}
        if (notInitialized) initializeVAccount();
    }

    private void initializeVAccount() throws SQLException {
        //Sync-Queue:
        TaskQueue taskQueue = new EventQueue(Executors.newSingleThreadExecutor());
        InitializeVAccountDB task = new InitializeVAccountDB(connector);
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
        //Sync-Queue:
        TaskQueue taskQueue = new EventQueue(Executors.newSingleThreadExecutor());
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
