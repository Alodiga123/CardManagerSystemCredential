package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Card;
import com.cms.commons.models.NewCardIssueRequest;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;

public class AdminRequestCardCanceledControllers extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblCardNumber;
    private Label lblNamesCardHolder;
    private Label lblRequestNumber;
    private Label lblStatusNewCardIssue;
    private Label lblNewCardIssueDate;
    private Radio rConfirmationYes;
    private Radio rConfirmationNo;
    private Textbox txtObservations;
    private Datebox txtRequestDate;
    private CardEJB cardEJB = null;
    private Card cardCanceledParam;
    private NewCardIssueRequest newCardIssueRequestParam;
    private List<NewCardIssueRequest> newCardIssueRequestList = null;
    private Button btnActivate;
    private Integer eventType;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) (Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE));

        if (eventType == WebConstants.EVENT_ADD) {
            cardCanceledParam = null;
        } else {
            cardCanceledParam = (Card) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
        loadData();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);

            newCardIssueRequestList = new ArrayList<NewCardIssueRequest>();
            newCardIssueRequestList = cardEJB.createCardNewCardIssueRequest(cardCanceledParam);

            for (NewCardIssueRequest r : newCardIssueRequestList) {
                newCardIssueRequestParam = r;
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
    }

    private void loadFields(Card cardCanceled) {
        try {
            EJBRequest request = new EJBRequest();
            Map params = new HashMap();

            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            lblCardNumber.setValue(cardCanceled.getCardNumber());
            lblNamesCardHolder.setValue(cardCanceled.getCardHolder());

        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loadFieldCanceled(NewCardIssueRequest newCardIssueRequest) {
        try {

            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            lblRequestNumber.setValue(newCardIssueRequest.getRequestNumber());
            txtRequestDate.setValue(newCardIssueRequest.getRequestDate());
            lblStatusNewCardIssue.setValue(newCardIssueRequest.getStatusNewCardIssueRequestId().getDescription());
            lblNewCardIssueDate.setValue(simpleDateFormat.format(newCardIssueRequest.getNewCardIssueDate()));
            if (txtObservations != null) {
                txtRequestDate.setValue(newCardIssueRequest.getRequestDate());
                txtObservations.setValue(newCardIssueRequest.getObservations());
            }
            if (newCardIssueRequest.getIndConfirmation() == true) {
                rConfirmationYes.setChecked(true);
            } else {
                rConfirmationNo.setChecked(true);
            }

            btnActivate.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtObservations.setReadonly(true);
        rConfirmationYes.setDisabled(true);
        rConfirmationNo.setDisabled(true);
        btnActivate.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtRequestDate.getText().isEmpty()) {
            txtRequestDate.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if ((!rConfirmationYes.isChecked()) && (!rConfirmationNo.isChecked())) {
            rConfirmationYes.setFocus(true);
            this.showMessage("cms.error.field.renewal", true, null);
        } else if (txtObservations.getText().isEmpty()) {
            txtObservations.setFocus(true);
            this.showMessage("cms.error.renewal.observations", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveCardRenewal(NewCardIssueRequest _cardIssuerRequest) {
        boolean indConfirmation;
        try {
            NewCardIssueRequest cardIssuerRequest = null;

            if (_cardIssuerRequest != null) {
                cardIssuerRequest = _cardIssuerRequest;
            } else {//New country
                cardIssuerRequest = new NewCardIssueRequest();
            }

            if (rConfirmationYes.isChecked()) {
                indConfirmation = true;
            } else {
                indConfirmation = false;
            }

            cardIssuerRequest.setIndConfirmation(indConfirmation);
            cardIssuerRequest.setObservations(txtObservations.getText());
            cardIssuerRequest.setRequestDate(txtRequestDate.getValue());
            cardIssuerRequest = cardEJB.saveNewCardIssueRequest(cardIssuerRequest);
            
            updateIndPendingNewCard(cardIssuerRequest.getCardId(), indConfirmation);

            this.showMessage("sp.common.save.success", false, null);

            btnActivate.setVisible(false);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void updateIndPendingNewCard(Card card, boolean ind) {
        boolean indPendingNewCardIssue;
        try {
            if(ind = true){
                indPendingNewCardIssue = true;
            }else{
                indPendingNewCardIssue = false;
            }
            
            card.setIndPendingNewCardIssue(indPendingNewCardIssue );
            card = cardEJB.saveCard(card);
            
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnActivate() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveCardRenewal(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveCardRenewal(newCardIssueRequestParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                if (newCardIssueRequestParam != null) {
                    loadFields(cardCanceledParam);
                    loadFieldCanceled(newCardIssueRequestParam);
                } else {
                    loadFields(cardCanceledParam);
                }
                break;
            case WebConstants.EVENT_VIEW:
                if (newCardIssueRequestParam != null) {
                    loadFields(cardCanceledParam);
                    loadFieldCanceled(newCardIssueRequestParam);
                } else {
                    loadFields(cardCanceledParam);
                }
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                break;
            default:
                break;
        }
    }
}
