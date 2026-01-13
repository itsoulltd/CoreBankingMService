package com.infoworks.components.presenters.Payments.tasks;

import com.infoworks.components.presenters.Payments.view.models.TransactionType;
import com.infoworks.domain.repositories.VAccountRepository;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.sql.query.pagination.SearchQuery;
import com.infoworks.tasks.ExecutableTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TransactionHistoryTask extends ExecutableTask<Message, Response> {

    private static Logger LOG = LoggerFactory.getLogger("TransactionHistoryTask");
    private VAccountRepository repository;
    private String accountName;
    private String accountPrefix;
    private TransactionType type = TransactionType.Any;
    private int pageSize = 10;
    private LocalDate from;
    private LocalDate to;
    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private Consumer<List<Map>> consumer;

    public TransactionHistoryTask(VAccountRepository repository
            , String accountPrefix
            , String accountName
            , TransactionType type
            , LocalDate from
            , LocalDate to
            , int pageSize
            , Consumer<List<Map>> consumer) {
        this.repository = repository;
        this.accountPrefix = accountPrefix;
        this.accountName = accountName;
        this.type = type;
        this.from = from;
        this.to = to;
        this.pageSize = pageSize;
        this.consumer = consumer;
    }

    public TransactionHistoryTask(VAccountRepository repository
            , String accountPrefix
            , String accountName
            , TransactionType type
            , LocalDate from
            , LocalDate to
            , int pageSize) {
        this(repository, accountPrefix, accountName, type, from, to, pageSize, null);
    }

    public VAccountRepository getRepository() {
        if (this.repository == null) {
            this.repository = new VAccountRepository();
        }
        return repository;
    }

    public Consumer<List<Map>> getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer<List<Map>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        //Prepare Search-Query:
        SearchQuery query = new SearchQuery();
        query.setSize(pageSize);
        query.setPage(0);

        //Adding Transaction-type, when other than Any:
        if (type != TransactionType.Any) {
            //Server-Side API, does not accept TransactionType.Any
            query.add("type").isEqualTo(type.value());
        }

        //Assigning From and To date to Query:
        String fromDate = from.format(dateFormat);
        query.add("from").isGreaterThenOrEqual(fromDate);
        String toDate = to.format(dateFormat);
        query.add("to").isGreaterThenOrEqual(toDate);
        //query.add("till").isGreaterThenOrEqual(<till-date>);

        //Send search-request:
        try {
            List<Map> response = repository.searchTransactions(accountPrefix, accountName, query);
            if (consumer != null) consumer.accept(response);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            if (consumer != null) consumer.accept(new ArrayList<>());
        }
        return new Response().setStatus(200);
    }
}
