package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Card;
import com.cms.commons.models.CardStatus;
import com.cms.commons.models.DeliveryRequest;
import com.cms.commons.models.DeliveryRequetsHasCard;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class AdminCardInventoryControllers extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblNumber;
    private Label lblDeliveryRequestDate;
    private Intbox intCardNumberAttemps;
    private Textbox txtReceiverFirstName;
    private Textbox txtReceiverLastName;
    private Textbox txtObservations;
    private Datebox txtDaliveryDate;
    private Radio rDeliveryYes;
    private Radio rDeliveryNo;
    private UtilsEJB utilsEJB = null;
    private CardEJB cardEJB = null;
    private DeliveryRequetsHasCard cardParam;
    public Window winAdminCardInventory;
    private Button btnSave;
    private Integer eventType;
    private AdminDeliveryRequestController adminDeliveryRequest = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        adminDeliveryRequest = new AdminDeliveryRequestController();
        eventType = (Integer) (Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE));
        if (adminDeliveryRequest.getDeliveryRequest() != null) {
            cardParam = (DeliveryRequetsHasCard) Sessions.getCurrent().getAttribute("object");
        } else {
            cardParam = null;
        }
        initialize();
        loadData();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
    }

    public void onClick$btnBack() {
        winAdminCardInventory.detach();
    }

    private void loadDelivery(DeliveryRequest deliveryRequest) {
        try {
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            if (deliveryRequest.getRequestNumber() != null) {
                lblNumber.setValue(deliveryRequest.getRequestNumber());
                lblDeliveryRequestDate.setValue(simpleDateFormat.format(deliveryRequest.getRequestDate()));
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loadFields(DeliveryRequetsHasCard card) {
        try {
            intCardNumberAttemps.setValue(card.getNumberDeliveryAttempts());
            txtReceiverFirstName.setValue(card.getReceiverFirstName());
            txtReceiverLastName.setValue(card.getReceiverLastName());
            txtObservations.setValue(card.getDeliveryObservations());
            txtDaliveryDate.setValue(card.getDeliveryDate());
            if (card.getIndDelivery() == true) {
                rDeliveryYes.setChecked(true);
            } else {
                rDeliveryNo.setChecked(true);
            }
            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        intCardNumberAttemps.setReadonly(true);
        txtDaliveryDate.setReadonly(true);
        txtReceiverFirstName.setReadonly(true);
        txtReceiverLastName.setReadonly(true);

        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (intCardNumberAttemps.getText().isEmpty()) {
            intCardNumberAttemps.setFocus(true);
            this.showMessage("cms.error.field.identificationNumber", true, null);
        } else if (txtDaliveryDate.getText().isEmpty()) {
            txtDaliveryDate.setFocus(true);
            this.showMessage("cms.error.field.fullName", true, null);
        } else if (txtReceiverFirstName.getText().isEmpty()) {
            txtReceiverFirstName.setFocus(true);
            this.showMessage("cms.error.field.fullName", true, null);
        } else if (txtReceiverLastName.getText().isEmpty()) {
            txtReceiverLastName.setFocus(true);
            this.showMessage("cms.error.field.fullName", true, null);
        } else if ((!rDeliveryYes.isChecked()) && (!rDeliveryNo.isChecked())) {
            rDeliveryYes.setFocus(true);
            this.showMessage("cms.error.field.delivery", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveCardStatus(DeliveryRequetsHasCard _card) {
        CardStatus cardStatus = null;
        try {
            DeliveryRequetsHasCard card = null;

            if (_card != null) {
                card = _card;
            } else {
                card = new DeliveryRequetsHasCard();
            }

            if (rDeliveryYes.isChecked()) {
                //se actualiza el estatus de la tarjeta a ENTREGADA
                EJBRequest request1 = new EJBRequest();
                request1.setParam(Constants.CARD_STATUS_DELIVERED);
                cardStatus = utilsEJB.loadCardStatus(request1);
            } else {
                //se actualiza el estatus de la tarjeta a NO ENTREGADA
                EJBRequest request1 = new EJBRequest();
                request1.setParam(Constants.CARD_STATUS_NOT_DELIVERED);
                cardStatus = utilsEJB.loadCardStatus(request1);
            }
            
            updateStatusCardInventory(card.getCardId(), cardStatus);
            saveCardInvetory(card);

            this.showMessage("sp.common.save.success", false, null);
            btnSave.setVisible(false);
            EventQueues.lookup("updateCardInventory", EventQueues.APPLICATION, true).publish(new Event(""));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void updateStatusCardInventory(Card card, CardStatus status) {
        boolean indDelivery = true;
        try {
            card.setCardStatusId(status);
            card = cardEJB.saveCard(card);

        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void saveCardInvetory(DeliveryRequetsHasCard card) {
        boolean indDelivery;
        try {

            if (rDeliveryYes.isChecked()) {
                indDelivery = true;
            } else {
                indDelivery = false;
            }

            //Se actualiza el objeto DeliveryRequetsHasCard
            card.setNumberDeliveryAttempts(intCardNumberAttemps.intValue());
            card.setDeliveryDate(txtDaliveryDate.getValue());
            card.setReceiverFirstName(txtReceiverFirstName.getValue());
            card.setReceiverLastName(txtReceiverLastName.getValue());
            card.setDeliveryObservations(txtObservations.getValue());
            card.setIndDelivery(indDelivery);
            if (eventType == WebConstants.EVENT_ADD) {
                card.setCreateDate(new Timestamp(new Date().getTime()));
            } else {
                card.setUpdateDate(new Timestamp(new Date().getTime()));
            }
            card = cardEJB.saveDeliveryRequestHasCard(card);

        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveCardStatus(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveCardStatus(cardParam);
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(cardParam);
                loadDelivery(adminDeliveryRequest.getDeliveryRequest());
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(cardParam);
                loadDelivery(adminDeliveryRequest.getDeliveryRequest());
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                break;
            default:
                break;
        }
    }
}
