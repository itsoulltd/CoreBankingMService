package com.infoworks.components.presenters.Payments.views;

import com.infoworks.components.component.FormActionBar;
import com.infoworks.components.component.IndeterminateDialog;
import com.infoworks.components.presenters.Payments.tasks.TransactionHistoryTask;
import com.infoworks.components.presenters.Payments.view.models.Transaction;
import com.infoworks.components.presenters.Payments.view.models.TransactionType;
import com.infoworks.config.AppQueue;
import com.infoworks.config.ExcelWritingService;
import com.infoworks.domain.repositories.VAccountRepository;
import com.infoworks.domain.tasks.DisplayAsyncNotification;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TransactionsView extends Composite<Div> {

    public static final String ACTION_SEARCH = "SEARCH";
    public static final String ACTION_DOWNLOAD_EXCEL = "EXCEL_DOWNLOAD";

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
    private TransactionHistoryTask transactionHistoryTask;

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

    public TransactionHistoryTask getTransactionHistoryTask() {
        return transactionHistoryTask;
    }

    public void setTransactionHistoryTask(TransactionHistoryTask transactionHistoryTask) {
        this.transactionHistoryTask = transactionHistoryTask;
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
        update(UI.getCurrent(), this.accountPrefix, this.accountName, ACTION_SEARCH);
    }

    public void update(UI ui, String prefix, String accountName, String action) {
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
                AppQueue.dispatch(500
                        , TimeUnit.MILLISECONDS
                        , () -> ui.access(() -> {
                            triggerDataFetch(ui, accountPrefix, accountName
                                    , type, from, to, pageSize, action);
                        }));
            } else {
                //Open delay & progress with:
                Dialog dialog = new IndeterminateDialog("Please wait...");
                dialog.addAttachListener(event -> {
                    //Start rest call with some delay: 1.2 sec
                    AppQueue.dispatch(1200, TimeUnit.MILLISECONDS
                            , () -> ui.access(() -> {
                                //Pass values:
                                triggerDataFetch(ui, accountPrefix, accountName
                                        , type, from, to, pageSize, action);
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
            update(ui, this.accountPrefix, this.accountName, ACTION_SEARCH);
        });
        //Download Excel Report:
        Button downloadExcel = new Button("Download Excel", VaadinIcon.RECORDS.create());
        downloadExcel.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        downloadExcel.addClickListener(e -> {
            //Prepare excel for download:
            UI ui = e.getSource().getUI().orElse(null);
            //AppQueue.dispatchTask(new DisplayAsyncNotification(ui, "Under construction!"));
            update(ui, this.accountPrefix, this.accountName, ACTION_DOWNLOAD_EXCEL);
        });
        //
        HorizontalLayout layout = new HorizontalLayout();
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        layout.add(pageSizeBox, transactionTypeBox, startDatePicker, endDatePicker, doFetch, downloadExcel);
        return layout;
    }

    private void triggerDataFetch(UI ui
            , String prefix
            , String username
            , TransactionType type
            , LocalDate from
            , LocalDate to
            , int pageSize, String action) {
        //
        TransactionHistoryTask task = (getTransactionHistoryTask() != null)
                ? getTransactionHistoryTask()
                : new TransactionHistoryTask(repository, prefix, username, type, from, to, pageSize);
        //Update default task.consumer if null:
        if (task.getConsumer() == null) {
            task.setConsumer((response) -> {
                if (response == null || response.size() == 0) {
                    ui.access(() -> {
                        Notification notification = Notification.show("No transactions found!");
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    });
                    return;
                }
                //Sort by transaction_date & balance descending order:
                Comparator sortBy = Comparator.comparing(Transaction::getTransactionDateTime)
                        .thenComparing(Transaction::getBalance).reversed();
                //Sort by balance descending order:
                //sortBy = Comparator.comparing(Transaction::getBalance).reversed();
                //Update UI:
                List<Transaction> transactions = Transaction.convert(response, sortBy);
                if (action.equalsIgnoreCase(ACTION_SEARCH)) {
                    updateGridView(ui, transactions);
                } else {
                    downloadExcel(ui, transactions);
                }
            });
        }
        //Finally execute:
        task.execute(null);
    }

    private void updateGridView(UI ui, List<Transaction> transactions) {
        this.mainView.setItems(transactions);
    }

    private void downloadExcel(UI ui, List<Transaction> transactions) {
        try {
            ExcelWritingService.AsyncWriter writer = new ExcelWritingService.AsyncWriter(true, new ByteArrayOutputStream());
            // Prepare Header-Column and rows:
            Map<Integer, List<String>> data = new HashMap<>();
            data.put(0, Arrays.asList("Prefix","AccountName","Currency","Amount","Balance","Type","Date"));
            //...
            writer.write("data", data, true);
            InputStream ios = new ByteArrayInputStream(((ByteArrayOutputStream) writer.getOutfile()).toByteArray());

            // Now Prepare download:
            // New Api Vaadin (24.9+):
            StreamResource resource = new StreamResource("example.xlsx", () -> ios);
            resource.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            StreamRegistration registry = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);

            // Download steps:
            Anchor downloadLink = new Anchor(registry.getResourceUri().toString(), "");
            downloadLink.getElement().setAttribute("download", true);

            // Trigger download:
            downloadLink.add(new Text(""));
            this.navBar.add(downloadLink);
            downloadLink.getElement().callJsFunction("click");
            this.navBar.remove(downloadLink);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
