package com.infoworks.lab.components.presenters.Payments.forms;

import com.infoworks.lab.components.component.FormActionBar;
import com.infoworks.lab.components.component.IndeterminateDialog;
import com.infoworks.lab.components.presenters.Payments.parser.VAccountResponseParser;
import com.infoworks.lab.components.presenters.Payments.view.models.TransactionAmount;
import com.infoworks.lab.config.ApplicationProperties;
import com.infoworks.lab.config.ValidationConfig;
import com.infoworks.lab.domain.beans.queues.EventQueue;
import com.infoworks.lab.domain.repository.AuthRepository;
import com.infoworks.lab.domain.repository.VAccountRepository;
import com.infoworks.lab.rest.models.Response;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.BigDecimalField;

import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class WithdrawForm extends FormLayout {

    private final String accountPrefix;
    private final String accountName;
    private final String accountTitle;
    private Label accountTitleLb = new Label("Account: ");
    private Label accountBalanceLb = new Label("Balance: ");
    private BigDecimal currentBalance = new BigDecimal("0.00");
    private BigDecimalField withdraw = new BigDecimalField("Withdraw:");
    private FormActionBar actionBar;
    private VAccountRepository repository;
    private Dialog dialog;

    public WithdrawForm(String accountPrefix, String accountName, Dialog dialog) {
        this.accountPrefix = accountPrefix;
        this.accountName = accountName;
        this.accountTitle = String.format("%s@%s", this.accountPrefix, this.accountName);
        this.actionBar = new FormActionBar(dialog);
        this.repository = new VAccountRepository(AuthRepository.parseToken(UI.getCurrent()));
        this.dialog = dialog;
    }

    public WithdrawForm(String accountPrefix, String accountName) {
        this(accountPrefix, accountName, null);
    }

    public String getAccountTitle() {
        return accountTitle;
    }

    public String getAccountPrefix() {
        return accountPrefix;
    }

    public String getAccountName() {
        return accountName;
    }

    public boolean isModalWindow() {
        return this.dialog != null;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        //
        this.accountTitleLb.setText(String.format("Account: %s", getAccountTitle()));
        this.withdraw.setRequiredIndicatorVisible(true);
        this.withdraw.setValue(new BigDecimal("0.00"));
        //
        this.actionBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.STRETCH);
        this.actionBar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        this.actionBar.setSaveButtonDisableOnClick(true);
        this.actionBar.addOnSaveAction(onSaveAction());
        //
        add(accountTitleLb, accountBalanceLb
                , withdraw
                , actionBar);
        //UI config:
        setResponsiveSteps(
                // Use one column by default
                new ResponsiveStep("0", 1),
                // Use two columns, if layout's width exceeds 500px
                new ResponsiveStep("300px", 2)
        );
        // Stretch the username & actionBar field over 2 columns
        setColspan(withdraw, 2);
        setColspan(actionBar, 2);
        //Fetch User Account-Info: Update accountBalance
        fetchBalance(UI.getCurrent());
    }

    private void fetchBalance(UI ui) {
        Dialog dialog = new IndeterminateDialog("Fetching balance...");
        dialog.addAttachListener(event -> {
            EventQueue.dispatch(500, TimeUnit.MILLISECONDS
                    , () -> ui.access(() -> {
                        //Fetch and update:
                        try {
                            Response response = repository.accountBalance(getAccountPrefix(), getAccountName());
                            currentBalance = VAccountResponseParser.getBalance(response);
                            accountBalanceLb.setText("Balance: " + currentBalance.toString());
                        } catch (RuntimeException e) {
                            Notification notification = Notification.show(e.getLocalizedMessage());
                            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                        dialog.close();
                    }));
        });
        dialog.open();
    }

    private ComponentEventListener<ClickEvent<Button>> onSaveAction() {
        return (event) -> {
            //On Save Action:
            UI ui = event.getSource().getUI().orElse(null);
            //
            BigDecimal depositAmount = Optional.ofNullable(this.withdraw.getValue())
                    .orElse(new BigDecimal("0.00"));
            TransactionAmount transactionAmount = new TransactionAmount(depositAmount.toString());
            //
            Validator validator = ValidationConfig.getValidator();
            String messages = ValidationConfig.validateWithMessage(validator, transactionAmount);
            if (messages != null) {
                Notification notification = Notification.show(messages);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                this.actionBar.setSaveButtonEnable(true);
            } else {
                if (transactionAmount.getMoney().equals("0.00")) {
                    Notification notification = Notification.show("Withdraw not possible, value is 0.00!");
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    this.actionBar.setSaveButtonEnable(true);
                } else {
                    saveWithdraw(ui, transactionAmount);
                }
            }
        };
    }

    private void saveWithdraw(UI ui, TransactionAmount transactionAmount) {
        Dialog dialog = new IndeterminateDialog("Update balance...");
        dialog.addAttachListener(event -> {
            EventQueue.dispatch(500, TimeUnit.MILLISECONDS
                    , () -> ui.access(() -> {
                        //Save Deposit:
                        try {
                            Response response = repository.accountExist(getAccountPrefix(), getAccountName());
                            //
                            if (VAccountResponseParser.isExist(response)) {
                                String amount = transactionAmount.getMoney();
                                response = repository.makeWithdrawal(
                                        getAccountName()
                                        , ApplicationProperties.CURRENCY
                                        , amount);
                                if (response.getStatus() == 200) {
                                    currentBalance = currentBalance.add(new BigDecimal(amount));
                                    accountBalanceLb.setText("Balance: " + currentBalance.toString());
                                } else {
                                    Notification notification = Notification.show(response.getError());
                                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                                }
                            } else {
                                Notification notification = Notification.show("Withdraw can not be possible, account is unavailable!");
                                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                            }
                        } catch (Exception e) {
                            Notification notification = Notification.show(e.getMessage());
                            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                        actionBar.setSaveButtonEnable(true);
                        dialog.close();
                    }));
        });
        dialog.open();
    }

}
