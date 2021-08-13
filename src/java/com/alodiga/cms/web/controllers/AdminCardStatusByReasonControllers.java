package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.CardStatus;
import com.cms.commons.models.CardStatusHasUpdateReason;
import com.cms.commons.models.StatusUpdateReason;
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
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Toolbarbutton;

public class AdminCardStatusByReasonControllers extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Combobox cmbCardStatus;
    private Combobox cmbCardStatusReason;
    private Radio rAllowTableYes;
    private Radio rAllowTableNo;
    boolean indAllowTable;
    private UtilsEJB utilsEJB = null;
    private CardEJB cardEJB = null;
    private CardStatusHasUpdateReason cardStatusByReasonParam;
    private Button btnSave;
    private Button btnAdd;
    private Integer eventType;
    private Toolbarbutton tbbTitle;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute("eventType");
        if (eventType == WebConstants.EVENT_ADD) {
            cardStatusByReasonParam = null;
        } else {
            cardStatusByReasonParam = (CardStatusHasUpdateReason) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.status.card.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.status.card.view"));
                break;
            case WebConstants.EVENT_ADD:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.status.card.add"));
                break;
        }
        try {
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {

    }

    private void loadFields(CardStatusHasUpdateReason cardStatusByReason) {
        try {
            if (cardStatusByReason.getIndAllowTable() == true) {
                rAllowTableYes.setChecked(true);
            } else {
                rAllowTableNo.setChecked(true);
            }

            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnAdd() {
        btnSave.setVisible(true);
        cmbCardStatus.setValue("");
        cmbCardStatusReason.setValue("");
        this.clearMessage();
    }
    
    public void blockFields() {
        rAllowTableYes.setDisabled(true);
        rAllowTableNo.setDisabled(true);
        btnSave.setVisible(false);
    }

    public void onChange$cmbCardStatus() {
        this.clearMessage();
    }

    public void onChange$cmbCardStatusReason() {
        this.clearMessage();
    }

    public Boolean validateEmpty() {
        if (cmbCardStatus.getText().isEmpty()) {
            cmbCardStatus.setFocus(true);
            this.showMessage("cms.error.statusCard.notSelected", true, null);
        } else if (cmbCardStatusReason.getText().isEmpty()) {
            cmbCardStatusReason.setFocus(true);
            this.showMessage("cms.error.statusUpdateReason.noSelected", true, null);
        } else if ((!rAllowTableYes.isChecked()) && (!rAllowTableNo.isChecked())) {
            this.showMessage("cms.error.field.allowTable", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveCardStatus(CardStatusHasUpdateReason _cardStatusByReason) throws RegisterNotFoundException, NullParameterException, GeneralException {
        List<CardStatusHasUpdateReason> cardStatusByReasonUnique = null;
        int indRegisterExist = 0;
        CardStatusHasUpdateReason cardStatusByReason = null;

        try {

            if (_cardStatusByReason != null) {
                cardStatusByReason = _cardStatusByReason;
            } else {//New cardStatus
                cardStatusByReason = new CardStatusHasUpdateReason();
            }

            if (rAllowTableYes.isChecked()) {
                indAllowTable = true;
            } else {
                indAllowTable = false;
            }

            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.STATUS_UPDATE_REASON_KEY, ((StatusUpdateReason) cmbCardStatusReason.getSelectedItem().getValue()).getId());
            params.put(Constants.CARD_STATUS_KEY, ((CardStatus) cmbCardStatus.getSelectedItem().getValue()).getId());
            params.put(Constants.CARD_IND_ALLOW_TABLE, indAllowTable);
            request1.setParams(params);
            cardStatusByReasonUnique = cardEJB.getCardStatusHasUpdateReasonUnique(request1);

            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    this.showMessage("cms.common.RegisterExistInBD", true, null);
                    break;
                case WebConstants.EVENT_EDIT:
                    if (cardStatusByReasonUnique.isEmpty()) {

                        buildCardStatusByReason(cardStatusByReason);
                        cardStatusByReason = cardEJB.saveCardStatusHasUpdateReason(cardStatusByReason);

                        cardStatusByReasonParam = cardStatusByReason;
                        this.showMessage("sp.common.save.success", false, null);
                    } else {
                        this.showMessage("cms.common.RegisterExistInBD", true, null);
                    }
                    break;
                default:
                    break;
            }
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } finally {
            if (eventType == 1 && cardStatusByReasonUnique == null) {
                cardStatusByReason = new CardStatusHasUpdateReason();

                buildCardStatusByReason(cardStatusByReason);
                cardStatusByReason = cardEJB.saveCardStatusHasUpdateReason(cardStatusByReason);

                cardStatusByReasonParam = cardStatusByReason;
                this.showMessage("sp.common.save.success", false, null);
                
                if (eventType == WebConstants.EVENT_ADD) {
                    btnSave.setVisible(false);
                } else {
                    btnSave.setVisible(true);
                }
            }
        }
    }

    public void buildCardStatusByReason(CardStatusHasUpdateReason cardStatusByReason) {
        if (rAllowTableYes.isChecked()) {
            indAllowTable = true;
        } else {
            indAllowTable = false;
        }

        cardStatusByReason.setStatusUpdateReasonId((StatusUpdateReason) cmbCardStatusReason.getSelectedItem().getValue());
        cardStatusByReason.setCardStatusId((CardStatus) cmbCardStatus.getSelectedItem().getValue());
        cardStatusByReason.setIndAllowTable(indAllowTable);

        if (eventType == WebConstants.EVENT_ADD) {
            cardStatusByReason.setCreateDate(new Timestamp(new Date().getTime()));
        } else {
            cardStatusByReason.setUpdateDate(new Timestamp(new Date().getTime()));
        }
    }

    public void onClick$btnSave() throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveCardStatus(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveCardStatus(cardStatusByReasonParam);
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(cardStatusByReasonParam);
                loadCmbCardStatus(eventType);
                loadCmbStatusUpdateReason(eventType);
                btnAdd.setVisible(false);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(cardStatusByReasonParam);
                loadCmbCardStatus(eventType);
                loadCmbStatusUpdateReason(eventType);
                blockFields();
                btnAdd.setVisible(false);
                break;
            case WebConstants.EVENT_ADD:
                loadCmbCardStatus(eventType);
                loadCmbStatusUpdateReason(eventType);
                break;
            default:
                break;
        }
    }

    private void loadCmbCardStatus(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<CardStatus> cardStatus;

        try {
            cardStatus = utilsEJB.getCardStatus(request1);
            loadGenericCombobox(cardStatus, cmbCardStatus, "description", evenInteger, Long.valueOf(cardStatusByReasonParam != null ? cardStatusByReasonParam.getCardStatusId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (GeneralException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (NullParameterException ex) {
            showError(ex);
            ex.printStackTrace();
        }
    }

    private void loadCmbStatusUpdateReason(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<StatusUpdateReason> statusUpdateReason;

        try {
            statusUpdateReason = cardEJB.getStatusUpdateReason(request1);
            loadGenericCombobox(statusUpdateReason, cmbCardStatusReason, "description", evenInteger, Long.valueOf(cardStatusByReasonParam != null ? cardStatusByReasonParam.getStatusUpdateReasonId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (GeneralException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (NullParameterException ex) {
            showError(ex);
            ex.printStackTrace();
        }
    }
}
