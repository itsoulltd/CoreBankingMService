package com.infoworks.lab.components.presenters.Payments.forms;

import com.infoworks.lab.components.component.FormActionBar;
import com.infoworks.lab.components.component.IndeterminateDialog;
import com.infoworks.lab.config.ApplicationProperties;
import com.infoworks.lab.config.ValidationConfig;
import com.infoworks.lab.domain.beans.queues.EventQueue;
import com.infoworks.lab.components.presenters.Payments.view.models.TransactionAmount;
import com.infoworks.lab.components.presenters.Payments.parser.VAccountResponseParser;
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

public class TransferForm extends FormLayout {

    private final String fromPrefix;
    private final String fromAccountName;
    private final String fromAccountTitle;
    private final String toPrefix;
    private final String toAccountName;
    private final String toAccountTitle;
    //
    private Label fromAccountTitleLb = new Label("Sender: ");
    private Label fromAccountBalanceLb = new Label("Balance: ");
    private BigDecimal fromCurrentBalance = new BigDecimal("0.00");
    //
    private Label toAccountTitleLb = new Label("Receiver: ");
    private Label toAccountBalanceLb = new Label("Balance: ");
    private BigDecimal toCurrentBalance = new BigDecimal("0.00");
    //
    private BigDecimalField transferAmount = new BigDecimalField("Transfer:");
    //
    private FormActionBar actionBar;
    private VAccountRepository repository;
    private Dialog dialog;

    public TransferForm(String fromPrefix
            , String fromAccountName
            , String toPrefix
            , String toAccountName
            , Dialog dialog) {
        //
        this.fromPrefix = fromPrefix;
        this.fromAccountName = fromAccountName;
        this.fromAccountTitle = String.format("%s@%s", this.fromPrefix, this.fromAccountName);
        //
        this.toPrefix = toPrefix;
        this.toAccountName = toAccountName;
        this.toAccountTitle = String.format("%s@%s", this.toPrefix, this.toAccountName);
        //
        this.actionBar = new FormActionBar(dialog);
        this.repository = new VAccountRepository(AuthRepository.parseToken(UI.getCurrent()));
        this.dialog = dialog;
    }

    public TransferForm(String fromPrefix
            , String fromAccountName
            , String toPrefix
            , String toAccountName) {
        this(fromPrefix, fromAccountName, toPrefix, toAccountName, null);
    }

    public String getFromAccountTitle() {
        return fromAccountTitle;
    }

    public String getFromPrefix() {
        return fromPrefix;
    }

    public String getFromAccountName() {
        return fromAccountName;
    }

    public String getToAccountTitle() {
        return toAccountTitle;
    }

    public String getToPrefix() {
        return toPrefix;
    }

    public String getToAccountName() {
        return toAccountName;
    }

    public boolean isModalWindow() {
        return this.dialog != null;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        //
        this.fromAccountTitleLb.setText(String.format("Sender: %s", getFromAccountTitle()));
        this.toAccountTitleLb.setText(String.format("Receiver: %s", getToAccountTitle()));
        //
        this.transferAmount.setRequiredIndicatorVisible(true);
        this.transferAmount.setValue(new BigDecimal("0.00"));
        //
        this.actionBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.STRETCH);
        this.actionBar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        this.actionBar.setSaveButtonDisableOnClick(true);
        this.actionBar.addOnSaveAction(onSaveAction());
        //
        add(fromAccountTitleLb, fromAccountBalanceLb
                , toAccountTitleLb, toAccountBalanceLb
                , transferAmount
                , actionBar);
        //UI config:
        setResponsiveSteps(
                // Use one column by default
                new ResponsiveStep("0", 1),
                // Use two columns, if layout's width exceeds 500px
                new ResponsiveStep("300px", 2)
        );
        // Stretch the username & actionBar field over 2 columns
        setColspan(transferAmount, 2);
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
                            Response response = repository.accountBalance(getFromPrefix(), getFromAccountName());
                            fromCurrentBalance = VAccountResponseParser.getBalance(response);
                            fromAccountBalanceLb.setText("Balance: " + fromCurrentBalance.toString());
                            //
                            response = repository.accountBalance(getToPrefix(), getToAccountName());
                            toCurrentBalance = VAccountResponseParser.getBalance(response);
                            toAccountBalanceLb.setText("Balance: " + toCurrentBalance.toString());
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
            BigDecimal depositAmount = Optional.ofNullable(this.transferAmount.getValue())
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
                boolean isFromAccountBalanceEqualOrLess = fromCurrentBalance.compareTo(new BigDecimal("0.00")) <= 0;
                boolean isTransactionEqualOrLess = transactionAmount.getMoney().equals("0.00");
                if (isFromAccountBalanceEqualOrLess || isTransactionEqualOrLess) {
                    String msg = isTransactionEqualOrLess
                            ? "Transfer not possible, amount is 0.00!"
                            : "Transfer not possible, Sender Balance is 0.00!";
                    Notification notification = Notification.show(msg);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    this.actionBar.setSaveButtonEnable(true);
                } else {
                    saveTransferAmount(ui, transactionAmount);
                }
            }
        };
    }

    private void saveTransferAmount(UI ui, TransactionAmount transactionAmount) {
        Dialog dialog = new IndeterminateDialog("Transfer balance...");
        dialog.addAttachListener(event -> {
            EventQueue.dispatch(500, TimeUnit.MILLISECONDS
                    , () -> ui.access(() -> {
                        //Save Deposit:
                        try {
                            Response response = repository.accountExist(getFromPrefix(), getFromAccountName());
                            boolean isFromAccountExist = VAccountResponseParser.isExist(response);
                            //
                            response = this.repository.accountExist(getToPrefix(), getToAccountName());
                            boolean isToAccountExist = VAccountResponseParser.isExist(response);
                            //
                            if (isFromAccountExist && isToAccountExist) {
                                String amount = transactionAmount.getMoney();
                                response = repository.makeTransaction(
                                        getFromPrefix()
                                        , getFromAccountName()
                                        , ApplicationProperties.CURRENCY
                                        , amount
                                        , getToAccountTitle());
                                //
                                if (response.getStatus() == 200) {
                                    fromCurrentBalance = fromCurrentBalance.subtract(new BigDecimal(amount));
                                    fromAccountBalanceLb.setText("Balance: " + fromCurrentBalance.toString());
                                    //
                                    toCurrentBalance = toCurrentBalance.add(new BigDecimal(amount));
                                    toAccountBalanceLb.setText("Balance: " + toCurrentBalance.toString());
                                } else {
                                    Notification notification = Notification.show(response.getError());
                                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                                }
                            } else {
                                //
                                Notification notification = Notification.show("Transfer can not be possible, accounts are unavailable!");
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
