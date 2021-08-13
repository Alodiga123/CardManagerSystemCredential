package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Card;
import com.cms.commons.models.CardRenewalRequestHasCard;
import com.cms.commons.models.CardStatus;
import com.cms.commons.models.CardStatusHasUpdateReason;
import com.cms.commons.models.StatusUpdateReason;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;

public class AdminCardRenewalRequestControllers extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblCardRequestRenewal;
    private Label lblCardNumber;
    private Label lblCardHolder;
    private Label lblCardProgram;
    private Label lblCardProduct;
    private Label lblCreateDate;
    private Label lblExpirationDate;
    private Textbox txtReason;
//    private Radio rRenewalYes;
//    private Radio rRenewalNo;
    private Combobox cmbStatusUpdateReason;
    private Combobox cmbCardStatus;
    private CardEJB cardEJB = null;
    private UtilsEJB utilsEJB = null;
    private CardRenewalRequestHasCard cardRenewalParam;
//    public Window winAdminCardRenewalRequest;
    private Button btnSave;
    private Integer eventType;
    private Toolbarbutton tbbTitle;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) (Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE));
        if (eventType == WebConstants.EVENT_ADD) {
            cardRenewalParam = null;
        } else {
            cardRenewalParam = (CardRenewalRequestHasCard) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
        loadData();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {

    }

    public void onChange$cmbStatusUpdateReason() {
        cmbCardStatus.setValue("");
        StatusUpdateReason statusUpdateReason = (StatusUpdateReason) cmbStatusUpdateReason.getSelectedItem().getValue();
        loadCmbCardStatus(eventType, statusUpdateReason.getId());
    }

    private void loadFields(CardRenewalRequestHasCard cardRenawal) {
        try {
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            lblCardRequestRenewal.setValue(cardRenawal.getCardRenewalRequestId().getRequestNumber());
            lblCardNumber.setValue(cardRenawal.getCardId().getCardNumber());
            lblCardHolder.setValue(cardRenawal.getCardId().getCardHolder());
            lblCardProgram.setValue(cardRenawal.getCardId().getProgramId().getName());
            lblCardProduct.setValue(cardRenawal.getCardId().getProductId().getName());
            lblCreateDate.setValue(simpleDateFormat.format(cardRenawal.getCardId().getIssueDate()));
            lblExpirationDate.setValue(simpleDateFormat.format(cardRenawal.getCardId().getExpirationDate()));

            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        cmbStatusUpdateReason.setReadonly(true);
        cmbCardStatus.setReadonly(true);
        txtReason.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (cmbStatusUpdateReason.getSelectedItem() == null) {
            cmbStatusUpdateReason.setFocus(true);
            this.showMessage("cms.error.country.notSelected", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveCard(Card _card) {
        CardStatus cardStatus = null;
        boolean indRenewal;
        try {
            CardRenewalRequestHasCard cardRenawalHasCard = null;
            Card card = null;

            if (_card != null) {
                card = _card;
            } else {//New card
                card = new Card();
            }

//            if (rRenewalYes.isChecked()) {
//                //se actualiza el estatus de la tarjeta a APROBADA
//                EJBRequest request1 = new EJBRequest();
//                request1.setParam(Constants.STATUS_REQUEST_APPROVED);
//                cardStatus = utilsEJB.loadCardStatus(request1);
//                
//                indRenewal = true;
//            } else {
//                //se actualiza el estatus de la tarjeta a ANULADA
//                EJBRequest request1 = new EJBRequest();
//                request1.setParam(Constants.CARD_STATUS_CANCELED);
//                cardStatus = utilsEJB.loadCardStatus(request1);
//                
//                indRenewal = false;
//            }
            card.setUpdateDate(new Timestamp(new Date().getTime()));
//            card.setIndRenewal(indRenewal);
            card.setStatusUpdateReasonId((StatusUpdateReason) cmbStatusUpdateReason.getSelectedItem().getValue());
            card.setObservations(txtReason.toString());
            card = cardEJB.saveCard(card);

            this.showMessage("sp.common.save.success", false, null);
            btnSave.setVisible(false);
        } catch (Exception ex) {
            showError(ex);
        }
    }

//    public void onClick$btnBack() {
//        winAdminCardRenewalRequest.detach();
//    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveCard(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveCard(cardRenewalParam.getCardId());
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
                loadCmbStatusUpdateReason(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(cardRenewalParam);
                loadCmbStatusUpdateReason(eventType);
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                loadCmbStatusUpdateReason(eventType);
                break;
            default:
                break;
        }
    }

    private void loadCmbStatusUpdateReason(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<StatusUpdateReason> statusUpdateReason;
        try {
            statusUpdateReason = cardEJB.getStatusUpdateReason(request1);
            loadGenericCombobox(statusUpdateReason, cmbStatusUpdateReason, "description", evenInteger, Long.valueOf(cardRenewalParam != null ? cardRenewalParam.getCardId().getStatusUpdateReasonId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }

    private void loadCmbCardStatus(Integer evenInteger, int statusUpdateReasonId) {
        cmbCardStatus.getItems().clear();
        EJBRequest request1 = new EJBRequest();
        Map params = new HashMap();
        params.put(Constants.STATUS_UPDATE_REASON_KEY, statusUpdateReasonId);
        request1.setParams(params);
        List<CardStatusHasUpdateReason> cardStatusList;
        try {
            cardStatusList = cardEJB.getCardStatusByUpdateReason(request1);
            for (int i = 0; i < cardStatusList.size(); i++) {
                Comboitem item = new Comboitem();
                item.setValue(cardStatusList.get(i));
                item.setLabel(cardStatusList.get(i).getCardStatusId().getDescription());
                item.setParent(cmbCardStatus);
                if (cardRenewalParam != null && cardStatusList.get(i).getId().equals(cardRenewalParam.getCardId().getCardStatusId().getId())) {
                    cmbCardStatus.setSelectedItem(item);
                }
            }
            if (evenInteger.equals(WebConstants.EVENT_VIEW)) {
                cmbCardStatus.setDisabled(true);
            }
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }
}
