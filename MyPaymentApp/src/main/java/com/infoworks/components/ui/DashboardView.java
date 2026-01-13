package com.infoworks.components.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.infoworks.config.AppQueue;
import com.infoworks.objects.MessageParser;
import com.infoworks.utils.rest.client.GetTask;
import com.infoworks.components.presenters.Payments.forms.DepositForm;
import com.infoworks.components.presenters.Payments.forms.TransferForm;
import com.infoworks.components.presenters.Payments.forms.WithdrawForm;
import com.infoworks.components.presenters.Payments.parser.VAccountResponseParser;
import com.infoworks.components.presenters.Payments.tasks.CreateAccountTask;
import com.infoworks.components.presenters.Payments.view.models.AccountPrefix;
import com.infoworks.components.presenters.Payments.view.models.AccountType;
import com.infoworks.components.presenters.Payments.views.TransactionsView;
import com.infoworks.config.ApplicationProperties;
import com.infoworks.config.RequestURI;
import com.infoworks.domain.entities.User;
import com.infoworks.domain.repositories.AuthRepository;
import com.infoworks.domain.repositories.VAccountRepository;
import com.infoworks.applayouts.RootLayout;
import com.infoworks.applayouts.RoutePath;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PageTitle("Dashboard")
@Route(value = RoutePath.DASHBOARD_VIEW, layout = RootLayout.class)
public class DashboardView extends Composite<Div> {

    private final VAccountRepository repository;
    private VerticalLayout balanceView;
    private TransactionsView transView;
    private String selectedReceiverAccountTitle;

    public DashboardView() {
        repository = new VAccountRepository(new RestTemplate(), AuthRepository.parseToken(UI.getCurrent()));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (getContent().getChildren().count() > 0){
            getContent().removeAll();
        }
        super.onAttach(attachEvent);
        //TODO:
        ProgressBar bar = new ProgressBar();
        bar.setIndeterminate(true);
        Div label = new Div();
        label.setText("");
        getContent().add(label, bar);
        //Dispatch Async-Call:
        UI ui = UI.getCurrent();
        AppQueue.dispatch(300, TimeUnit.MILLISECONDS
                , () -> ui.access(() -> {
                    User user = AuthRepository.currentPrincipleFromToken(ui, new Property("username"));
                    if (user == null) {
                        //TODO: notification
                        return;
                    }
                    AccountPrefix prefix = AccountPrefix.valueOf(ApplicationProperties.APP_ACCOUNT_CASH_PREFIX);
                    String username = user.getName();
                    Response response = repository.accountExist(prefix.name(), username);
                    boolean exist = VAccountResponseParser.isExist(response);
                    if (exist) {
                        loadExistingAccount(prefix, AccountType.USER, user);
                    } else {
                        createNewAccount(prefix, AccountType.USER, user);
                    }
                }));
        //
    }

    private void createNewAccount(AccountPrefix prefix, AccountType type, User user) {
        if (getContent().getChildren().count() > 0){
            getContent().removeAll();
        }
        //Create New Account:
        VerticalLayout layout = new VerticalLayout();
        Label accountTitle = new Label("New Account Title: " + prefix.name() + "@" + user.getName());
        Button createAccount = new Button("Create New Account", (event) -> {
            UI ui = event.getSource().getUI().orElse(null);
            CreateAccountTask task = createNewAccountTask(ui, user, prefix, type);
            Response response = task.execute(null);
            if (response == null || response.getStatus() != 200) {
                Notification notification = Notification.show("Account Creation Failed!"
                        , 1000
                        , Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            } else {
                loadExistingAccount(prefix, type, user);
            }
        });
        //
        layout.add(accountTitle, createAccount);
        getContent().add(new Span("Create New Account"), layout);
    }

    private CreateAccountTask createNewAccountTask(UI ui, User user, AccountPrefix prefix, AccountType type) {
        // First Check AccountName is null or empty;
        if (user.getName() == null
                || user.getName().isEmpty())
            return null;
        //Create Ledger Create Task Using PostTask
        String token = AuthRepository.parseToken(ui);
        RestTemplate template = new RestTemplate();
        VAccountRepository repository = new VAccountRepository(template, token);
        CreateAccountTask task = new CreateAccountTask(user.getName()
                , prefix.name()
                , type.name()
                , ApplicationProperties.CURRENCY
                , "0.00");
        task.setRepository(repository);
        return task;
    }

    private void loadExistingAccount(AccountPrefix prefix, AccountType type, User user) {
        if (getContent().getChildren().count() > 0){
            getContent().removeAll();
        }
        //Current-Balance View:
        balanceView = createBalanceView();
        //Action View: (Deposit, Withdraw, Transfer)
        Component actionBar = configActionBar(prefix.name(), user.getName());
        Component transferBar = configTransferView(UI.getCurrent(), prefix.name(), user.getName());
        //TransactionView:
        transView = new TransactionsView();
        //Add all view to Dashboard-Content:
        getContent().add(balanceView, actionBar, transferBar, transView);
        //Fetch transactions:
        UI ui = UI.getCurrent();
        updateTransactionView(ui, prefix.name(), user.getName());
        //Fetch Balance:
        updateBalance(ui, balanceView, prefix.name(), user.getName());
    }

    private VerticalLayout createBalanceView() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(new Span("Current Balance: (loading...)"));
        return layout;
    }

    private void updateBalance(UI ui, VerticalLayout balanceView, String prefix, String username) {
        AppQueue.dispatch(300, TimeUnit.MILLISECONDS
                , () -> ui.access(() -> {
                    //Fetch current balance:
                    try {
                        Response response = repository.accountBalance(prefix, username);
                        BigDecimal currentBalance = VAccountResponseParser.getBalance(response);
                        balanceView.removeAll();
                        balanceView.add(new Span("Current Balance: " + currentBalance.toString()));
                    } catch (RuntimeException e) {
                        Notification notification = Notification.show(e.getLocalizedMessage());
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }));
    }

    private void updateTransactionView(UI ui, String prefix, String username) {
        //Load Transactions for CASH@Username
        AppQueue.dispatch(200, TimeUnit.MILLISECONDS
                , () -> ui.access(() -> transView.update(ui, prefix, username)));
    }

    private Component configActionBar(String prefix, String username) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        //Deposit Action:
        layout.add(new Button("Deposit", (event) -> {
            Dialog dialog = new Dialog();
            dialog.addDetachListener((e) -> {
                UI ui = e.getSource().getUI().orElse(null);
                updateBalance(ui, this.balanceView, prefix, username);
                updateTransactionView(ui, prefix, username);
            });
            dialog.add(new DepositForm(prefix, username, dialog));
            dialog.open();
        }));
        //Withdraw Action:
        layout.add(new Button("Withdraw", (event) -> {
            Dialog dialog = new Dialog();
            dialog.addDetachListener((e) -> {
                UI ui = e.getSource().getUI().orElse(null);
                updateBalance(ui, this.balanceView, prefix, username);
                updateTransactionView(ui, prefix, username);
            });
            dialog.add(new WithdrawForm(prefix, username, dialog));
            dialog.open();
        }));
        return layout;
    }

    private Component configTransferView(UI ui, String fromPrefix, String fromAccName) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        //Add SearchView
        ComboBox<String> accountBox = new ComboBox<>();
        accountBox.setPlaceholder("Select or search....");
        accountBox.setWidth("250px");
        accountBox.getStyle().set("--vaadin-combo-box-overlay-width", "350px");
        accountBox.setAllowCustomValue(true);
        accountBox.setItems(new ArrayList<>()); //Initially Empty:
        //Value-Change listener:
        accountBox.addValueChangeListener((e) -> {
            selectedReceiverAccountTitle = e.getValue();
        });
        //Custom-Value change listener for search:
        accountBox.addCustomValueSetListener((e) -> {
            //TODO:
        });
        layout.add(accountBox);
        //Dispatch fetch:
        fetchAccountTitles(ui, accountBox
                , String.format("%s@%s", fromPrefix, fromAccName), "CASH@Master", "REVENUE@Master");
        //Transfer Action:
        layout.add(new Button("Transfer", (event) -> {
            Dialog dialog = new Dialog();
            dialog.addDetachListener((e) -> {
                UI uia = e.getSource().getUI().orElse(null);
                updateBalance(uia, this.balanceView, fromPrefix, fromAccName);
                updateTransactionView(uia, fromPrefix, fromAccName);
            });
            String toPrefix = "", toAccName = "";
            if (selectedReceiverAccountTitle != null
            && selectedReceiverAccountTitle.contains("@")) {
                String[] parts = selectedReceiverAccountTitle.split("@");
                toPrefix = parts[0];
                toAccName = parts[1];
            }
            dialog.add(new TransferForm(fromPrefix, fromAccName, toPrefix, toAccName, dialog));
            dialog.open();
        }));
        return layout;
    }

    private void fetchAccountTitles(UI ui, ComboBox<String> accountBox, String...excludes) {
        AppQueue.dispatch(300, TimeUnit.MILLISECONDS
                , () -> ui.access(() -> {
                    GetTask task = new GetTask(RequestURI.USER_BASE
                            , RequestURI.USER_API
                            , new Property("limit", 20), new Property("page", 1));
                    task.setToken(AuthRepository.parseToken(ui));
                    Response response = task.execute(null);
                    try {
                        List<String> exclusionList = Arrays.asList(excludes);
                        List<Map<String, Object>> rows = MessageParser.unmarshal(
                                new TypeReference<>() {}, response.getMessage());
                        List<String> accTitles = rows.stream().flatMap(row ->
                                Stream.of(Optional.ofNullable(row.get("account_ref")).orElse("").toString())
                        ).filter(val -> !exclusionList.contains(val))
                                .collect(Collectors.toList());
                        accountBox.setItems(accTitles);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
    }
}
