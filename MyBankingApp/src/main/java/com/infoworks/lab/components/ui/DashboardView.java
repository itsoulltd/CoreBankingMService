package com.infoworks.lab.components.ui;

import com.infoworks.lab.components.presenters.Payments.tasks.CreateAccountTask;
import com.infoworks.lab.components.presenters.Payments.views.TransactionsView;
import com.infoworks.lab.config.ApplicationProperties;
import com.infoworks.lab.config.RestTemplateConfig;
import com.infoworks.lab.domain.beans.queues.EventQueue;
import com.infoworks.lab.domain.entities.User;
import com.infoworks.lab.components.presenters.Payments.view.models.AccountPrefix;
import com.infoworks.lab.components.presenters.Payments.view.models.AccountType;
import com.infoworks.lab.components.presenters.Payments.parser.VAccountResponseParser;
import com.infoworks.lab.domain.repository.AuthRepository;
import com.infoworks.lab.domain.repository.VAccountRepository;
import com.infoworks.lab.layouts.ApplicationLayout;
import com.infoworks.lab.layouts.RoutePath;
import com.infoworks.lab.rest.models.Response;
import com.it.soul.lab.sql.query.models.Property;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@PageTitle("Dashboard")
@Route(value = RoutePath.DASHBOARD_VIEW, layout = ApplicationLayout.class)
public class DashboardView extends Composite<Div> {

    private final VAccountRepository repository;

    public DashboardView() {
        repository = new VAccountRepository(RestTemplateConfig.getTemplate());
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
        EventQueue.dispatch(300, TimeUnit.MILLISECONDS
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
        RestTemplate template = RestTemplateConfig.getTemplate();
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
        //TransactionView:
        TransactionsView transView = new TransactionsView();
        getContent().add(transView);
        //Fetch transactions:
        UI ui = UI.getCurrent();
        EventQueue.dispatch(200, TimeUnit.MILLISECONDS
                , () -> ui.access(() -> {
                    //Load Transactions for CASH@Username
                    transView.update(ui, prefix.name(), user.getName());
                }));
    }
}
