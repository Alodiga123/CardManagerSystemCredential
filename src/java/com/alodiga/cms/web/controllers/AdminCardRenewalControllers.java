package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.Card;
import com.cms.commons.models.CardRenewalRequest;
import com.cms.commons.models.CardRenewalRequestHasCard;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.text.SimpleDateFormat;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;

public class AdminCardRenewalControllers extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblNumberRequest;
    private Label lblDate;
    private Label lblStatus;
    private Label lblIssuer;
    private Textbox txtObservations;
    private CardEJB cardEJB = null;
    private Card cardParam;
    private CardRenewalRequest cardRenewalParam;
    public static CardRenewalRequest cardRenewalRequestHasCardParent = null;
    private Button btnSave;
    private Integer eventType;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) (Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE));
        if (eventType == WebConstants.EVENT_ADD) {
            cardRenewalParam = null;
        } else {
            cardRenewalParam = (CardRenewalRequest) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
        loadData();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public CardRenewalRequest getCardRenewalRequestHasCard() {
        return cardRenewalRequestHasCardParent;
    }

    public void clearFields() {
        txtObservations.setRawValue(null);
    }

    private void loadFields(CardRenewalRequest cardRenawal) {
        try {
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            lblNumberRequest.setValue(cardRenawal.getRequestNumber());
            lblDate.setValue(simpleDateFormat.format(cardRenawal.getRequestDate()));
            lblStatus.setValue(cardRenawal.getStatusCardRenewalRequestId().getDescription());
            lblIssuer.setValue(cardRenawal.getIssuerId().getName());
            if (cardRenawal.getObservations() != null) {
                txtObservations.setText(cardRenawal.getObservations());
            }
            cardRenewalRequestHasCardParent = cardRenawal;
            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtObservations.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtObservations.getText().isEmpty()) {
            txtObservations.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveCardRenewal(CardRenewalRequest _cardRenawal) {
        try {
            CardRenewalRequestHasCard cardRenawalHasCard = null;
            CardRenewalRequest cardRenawal = null;

            if (_cardRenawal != null) {
                cardRenawal = _cardRenawal;
            } else {//New country
                cardRenawal = new CardRenewalRequest();
            }

            cardRenawal.setObservations(txtObservations.getText());
            cardRenawal = cardEJB.saveCardRenewalRequest(cardRenawal);

            this.showMessage("sp.common.save.success", false, null);
            
            btnSave.setVisible(false);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveCardRenewal(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveCardRenewal(cardRenewalParam);
                    cardRenewalRequestHasCardParent = cardRenewalParam;
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(cardRenewalParam);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(cardRenewalParam);
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                break;
            default:
                break;
        }
    }

}
