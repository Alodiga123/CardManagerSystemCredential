package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.commons.ejb.ProgramEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.AccountCard;
import com.cms.commons.models.AccountProperties;
import com.cms.commons.models.AccountTypeHasProductType;
import com.cms.commons.models.Card;
import com.cms.commons.models.Country;
import com.cms.commons.models.Product;
import com.cms.commons.models.Program;
import com.cms.commons.models.User;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Toolbarbutton;

public class AdminAccountCardController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private UtilsEJB utilsEJB = null;
    private PersonEJB personEJB = null;
    private ProductEJB productEJB = null;
    private ProgramEJB programEJB = null;
    private CardEJB cardEJB = null;
    private AccountCard accountCardParam;
    private Product productParam;
    private Program programParam;
    private Label lblCardNumber;
    private Label lblCardHolder;
    private Label lblExpirationDate;
    private Label lblAccountNumber;
    private Label lblStatusAccount;
    private Label lblUserCancellationAccount;
    private Textbox txtReasonCancellation;
    private Datebox dtbCancellationDate;
    private Button btnSave;
    private Integer eventType;
    private Toolbarbutton tbbTitle;
    private AccountCard accountCard = null;
    private User user = null;
    private Card card = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            accountCardParam = null;
        } else {
            accountCardParam = (AccountCard) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        user = (User) session.getAttribute(Constants.USER_OBJ_SESSION);
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.account.card.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.account.card.view"));
                break;
            default:
                break;
        }
        try {
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
            programEJB = (ProgramEJB) EJBServiceLocator.getInstance().get(EjbConstants.PROGRAM_EJB);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        lblCardNumber.setValue(null);
        lblCardHolder.setValue(null);
        lblExpirationDate.setValue(null); 
        lblAccountNumber.setValue(null);
        lblStatusAccount.setValue(null);
        lblUserCancellationAccount.setValue(null);
        txtReasonCancellation.setRawValue(null);
        dtbCancellationDate.setValue(null);   
    }


    private void loadFields(AccountCard accountCard) {
        try {
            lblCardNumber.setValue(accountCard.getCardId().getCardNumber());
            lblCardHolder.setValue(accountCard.getCardId().getCardHolder());
            lblExpirationDate.setValue(accountCard.getCardId().getExpirationDate().toString());
            lblAccountNumber.setValue(accountCard.getAccountNumber().toString());
            lblStatusAccount.setValue(accountCard.getStatusAccountId().getDescription());
            lblUserCancellationAccount.setValue(accountCard.getUserCancellationAccountId().getFirstNames() + " " + accountCard.getUserCancellationAccountId().getLastNames());
            txtReasonCancellation.setText(accountCard.getReasonCancellation().toString());
            dtbCancellationDate.setValue(accountCard.getCancellationDate());
            btnSave.setVisible(true);

        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtReasonCancellation.setReadonly(true);
        dtbCancellationDate.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtReasonCancellation.getText() == null) {
            txtReasonCancellation.setFocus(true);
            this.showMessage("cms.error.field.cannotNull", true, null);
        } else if (dtbCancellationDate.getValue() == null) {
            dtbCancellationDate.setFocus(true);
            this.showMessage("cms.error.cancellation.date.notSelected", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveAccountCard(AccountCard _accountCard) throws RegisterNotFoundException, NullParameterException, GeneralException {
        try {
            AccountCard accountCard = null;

            if (_accountCard != null) {
                accountCard = _accountCard;
            } else {//New Account Properties
                accountCard = new AccountCard();
            }

            //Guardar Cancelación Cuenta de Tarjeta
            accountCard.setReasonCancellation(txtReasonCancellation.getText());
            accountCard.setCancellationDate(new Timestamp(new Date().getTime()));
            accountCard.setUserCancellationAccountId(user);
            accountCard = cardEJB.saveAccountCard(accountCard);
            accountCardParam = accountCard;
            this.showMessage("sp.common.save.success", false, null);
            btnSave.setVisible(false);
        } catch (Exception ex) {
            showError(ex);
        }

    }

    public void onClick$btnSave() throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveAccountCard(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveAccountCard(accountCardParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(accountCardParam);
                txtReasonCancellation.setDisabled(false);
                dtbCancellationDate.setDisabled(false);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(accountCardParam);
                txtReasonCancellation.setReadonly(true);
                blockFields();
                dtbCancellationDate.setDisabled(false);
                break;
            default:
                break;
        }
    }




    }


