package com.infoworks.lab.components.presenters.Payments.views;

import com.infoworks.lab.components.component.FormActionBar;
import com.infoworks.lab.components.component.IndeterminateDialog;
import com.infoworks.lab.domain.beans.queues.EventQueue;
import com.infoworks.lab.components.presenters.Payments.view.models.Transaction;
import com.infoworks.lab.components.presenters.Payments.view.models.TransactionType;
import com.infoworks.lab.domain.repository.VAccountRepository;
import com.infoworks.lab.rest.models.SearchQuery;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TransactionsView extends Composite<Div> {
    private static Logger LOG = LoggerFactory.getLogger("TransactionsView");
    //Components:
    private HorizontalLayout navBar;
    private Grid<Transaction> mainView;
    private ComboBox<TransactionType> transactionTypeBox = new ComboBox<>("Type:", TransactionType.values());
    private final Integer[] pageSizeItems = new Integer[]{5, 10, 15, 20, 50};
    private ComboBox<Integer> pageSizeBox = new ComboBox<>("Page Size:", pageSizeItems);
    private final DatePicker startDatePicker = new DatePicker("From Date:");
    private final DatePicker endDatePicker = new DatePicker("To Date");
    //Attributes:
    private String accountName;
    private String accountPrefix;
    private VAccountRepository repository;
    private boolean isModalView;
    private FormActionBar actionBar;

    public TransactionsView(String accountPrefix, String accountName, Dialog dialog) {
        this.accountPrefix = accountPrefix;
        this.accountName = accountName;
        this.repository = new VAccountRepository();
        this.actionBar = new FormActionBar(dialog);
        this.isModalView = dialog != null;
    }

    public TransactionsView(String accountPrefix, String accountName) {
        this(accountPrefix, accountName, null);
    }

    public TransactionsView() {
        this(null, null);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (getContent().getChildren().count() > 0){
            getContent().removeAll();
        }
        super.onAttach(attachEvent);
        //
        this.navBar = initNavBar();
        this.mainView = initGridView(20);
        //
        VerticalLayout parent = new VerticalLayout();
        parent.setSpacing(true);
        parent.setSizeFull();
        if (this.isModalView) {
            this.actionBar.setPadding(false);
            this.actionBar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            this.actionBar.setWidthFull();
            parent.add(this.navBar, this.mainView, this.actionBar);
        } else {
            parent.add(this.navBar, this.mainView);
        }
        getContent().add(parent);
        //Send default search:
        update(UI.getCurrent(), this.accountPrefix, this.accountName);
    }

    public void update(UI ui, String prefix, String accountName) {
        this.accountPrefix = prefix;
        this.accountName = accountName;
        //Send default search:
        if (this.accountPrefix != null && !this.accountPrefix.isEmpty()) {
            //Pick field values:
            int pageSize = this.pageSizeBox.getValue();
            TransactionType type = this.transactionTypeBox.getValue();
            LocalDate from = this.startDatePicker.getValue();
            LocalDate to = this.endDatePicker.getValue();
            //Start: Dispatch-Async
            if (pageSize <= this.pageSizeItems[0]) {
                EventQueue.dispatch(500
                        , TimeUnit.MILLISECONDS
                        , () -> ui.access(() -> {
                            List<Transaction> transactions = triggerDataFetch(ui
                                    , this.accountPrefix, this.accountName
                                    , pageSize, type
                                    , from, to);
                            //Update UI:
                            this.mainView.setItems(transactions);
                        }));
            } else {
                //Open delay & progress with:
                Dialog dialog = new IndeterminateDialog("Please wait...");
                dialog.addAttachListener(event -> {
                    //Start rest call with some delay: 1.2 sec
                    EventQueue.dispatch(1200, TimeUnit.MILLISECONDS
                            , () -> ui.access(() -> {
                                //Pass values:
                                List<Transaction> transactions = triggerDataFetch(ui
                                        , this.accountPrefix, this.accountName
                                        , pageSize, type
                                        , from, to);
                                //Update UI:
                                this.mainView.setItems(transactions);
                                dialog.close();
                            }));
                });
                dialog.open();
            }
            //End: Dispatch-Async
        } else {
            ui.access(() -> this.mainView.setItems(new ArrayList<>()));
        }
    }

    private Grid<Transaction> initGridView(int defaultPageSize) {
        Grid<Transaction> grid = new Grid<>(Transaction.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setPageSize(defaultPageSize);
        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        //Columns
        grid.addColumn(Transaction::getAccountPrefix)
                .setHeader("Prefix")
                .setAutoWidth(true);

        grid.addColumn(Transaction::getAccountName)
                .setHeader("AccountName")
                .setAutoWidth(true);

        grid.addColumn(Transaction::getCurrency)
                .setHeader("Currency")
                .setAutoWidth(true);

        grid.addColumn(Transaction::getAmount)
                .setHeader("Amount")
                .setAutoWidth(true);

        grid.addColumn(Transaction::getBalance)
                .setHeader("Balance")
                .setAutoWidth(true);

        grid.addColumn(Transaction::getTransaction_type)
                .setHeader("Type")
                .setAutoWidth(true);

        grid.addColumn(Transaction::getTransaction_date)
                .setHeader("Date")
                .setAutoWidth(true);
        //
        return grid;
    }

    private HorizontalLayout initNavBar() {
        //Setup UIs:
        this.pageSizeBox.setValue(pageSizeItems[0]); //Default value
        this.transactionTypeBox.setValue(TransactionType.Any); //Default value
        //From Datetime field:
        this.startDatePicker.setMin(LocalDate.now().minusMonths(3));
        this.startDatePicker.setMax(LocalDate.now().plusMonths(6));
        this.startDatePicker.setValue(LocalDate.now().minusDays(7));
        //To Datetime field:
        this.endDatePicker.setMin(LocalDate.now().minusMonths(3));
        this.endDatePicker.setMax(LocalDate.now().plusMonths(6));
        this.endDatePicker.setValue(LocalDate.now().plusDays(1));
        //Fetch Action Button:
        Button doFetch = new Button("Search", VaadinIcon.SEARCH.create());
        doFetch.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        doFetch.addClickListener(e -> {
            //Send default search:
            UI ui = e.getSource().getUI().orElse(null);
            update(ui, this.accountPrefix, this.accountName);
        });
        //
        HorizontalLayout layout = new HorizontalLayout();
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        layout.add(pageSizeBox, transactionTypeBox, startDatePicker, endDatePicker, doFetch);
        return layout;
    }

    private List<Transaction> triggerDataFetch(UI ui, String prefix
            , String username
            , int pageSize
            , TransactionType type
            , LocalDate from
            , LocalDate to) {
        //Prepare Search-Query:
        SearchQuery query = new SearchQuery();
        query.setSize(pageSize);
        query.setPage(0);
        //Adding Transaction-type:
        if (type != TransactionType.Any)
            query.add("type").isEqualTo(type.value());
        //Assigning From or To datetime:
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fromDate = from.format(dateFormat);
        query.add("from").isGreaterThenOrEqual(fromDate);
        //
        String toDate = to.format(dateFormat);
        query.add("to").isGreaterThenOrEqual(toDate);
        //query.add("till").isGreaterThenOrEqual(<till-date>);
        //Send search-request:
        try {
            List<Map> response = repository.searchTransactions(prefix, username, query);
            //Sort by transaction_date & balance descending order:
            Comparator sortBy = Comparator.comparing(Transaction::getTransactionLocalDate)
                    .thenComparing(Transaction::getBalance).reversed();
            //Sort by balance descending order:
            //sortBy = Comparator.comparing(Transaction::getBalance).reversed();
            //
            List<Transaction> transactions = Transaction.convert(response, sortBy);
            return transactions;
            //
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            ui.access(() -> {
                Notification notification = Notification.show(e.getLocalizedMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            });
        }
        return new ArrayList<>();
    }
}
