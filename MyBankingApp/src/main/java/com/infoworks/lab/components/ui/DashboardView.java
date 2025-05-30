package com.infoworks.lab.components.ui;

import com.infoworks.lab.beans.tasks.definition.Task;
import com.infoworks.lab.components.presenters.Payments.tasks.CreateAccountTask;
import com.infoworks.lab.config.ApplicationProperties;
import com.infoworks.lab.config.RestTemplateConfig;
import com.infoworks.lab.domain.beans.queues.EventQueue;
import com.infoworks.lab.domain.entities.User;
import com.infoworks.lab.domain.models.payments.AccountPrefix;
import com.infoworks.lab.domain.models.payments.AccountType;
import com.infoworks.lab.domain.models.payments.VAccountResponseParser;
import com.infoworks.lab.domain.repository.AuthRepository;
import com.infoworks.lab.domain.repository.VAccountRepository;
import com.infoworks.lab.layouts.ApplicationLayout;
import com.infoworks.lab.layouts.RoutePath;
import com.infoworks.lab.rest.models.Response;
import com.it.soul.lab.sql.query.models.Property;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
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
                    String prefix = ApplicationProperties.APP_ACCOUNT_CASH_PREFIX;
                    String username = user.getName();
                    Response response = repository.accountExist(prefix, username);
                    boolean exist = VAccountResponseParser.isExist(response);
                    if (exist) {
                        loadExistingAccount(prefix, user);
                    } else {
                        createNewAccount(prefix, user);
                    }
                }));
        //
    }

    private void createNewAccount(String prefix, User user) {
        if (getContent().getChildren().count() > 0){
            getContent().removeAll();
        }
        //TODO:
        getContent().add(new Span("Create New Account"));

    }

    private void loadExistingAccount(String prefix, User user) {
        if (getContent().getChildren().count() > 0){
            getContent().removeAll();
        }
        //TODO:
        getContent().add(new Span("Load Existing Account"));
    }

    private Task createNewAccountTask(UI ui, User user, AccountPrefix prefix, AccountType type) {
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
}
