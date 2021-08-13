package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.StatusUpdateReason;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.sql.Timestamp;
import java.util.Date;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;

public class AdminCardUpdateReasonController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtName;
    private CardEJB cardEJB = null;
    private StatusUpdateReason cardUpdateReasonParam;
    private Button btnSave;
    private Integer eventType;
    private Toolbarbutton tbbTitle;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute("eventType");
        if (eventType == WebConstants.EVENT_ADD) {
            cardUpdateReasonParam = null;
        } else {
            cardUpdateReasonParam = (StatusUpdateReason) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.cardUpdateReason.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.cardUpdateReasonview"));
                break;
            case WebConstants.EVENT_ADD:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.cardUpdateReason.add"));
                break;
        }
        try {
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        txtName.setRawValue(null);
    }

    private void loadFields(StatusUpdateReason cardUpdateReason) {
        try {
            txtName.setText(cardUpdateReason.getDescription());
            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtName.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtName.getText().isEmpty()) {
            txtName.setFocus(true);
            this.showMessage("cms.error.description", true, null);
            return false;
        }
        return true;
    }

    private void saveCardStatus(StatusUpdateReason _statusUpdateReason) throws RegisterNotFoundException, NullParameterException, GeneralException {
        try {
            StatusUpdateReason statusUpdateReason = null;

            if (_statusUpdateReason != null) {
                statusUpdateReason = _statusUpdateReason;
            } else {//New cardStatus
                statusUpdateReason = new StatusUpdateReason();
            }
            statusUpdateReason.setDescription(txtName.getText());
            if (eventType == WebConstants.EVENT_ADD) {
                statusUpdateReason.setCreateDate(new Timestamp(new Date().getTime()));
            } else {
                statusUpdateReason.setUpdateDate(new Timestamp(new Date().getTime()));
            }
            statusUpdateReason = cardEJB.saveStatusUpdateReason(statusUpdateReason);
            cardUpdateReasonParam = statusUpdateReason;
            this.showMessage("sp.common.save.success", false, null);
            
            if (eventType == WebConstants.EVENT_ADD) {
                btnSave.setVisible(false);
            } else {
                btnSave.setVisible(true);
            }
        } catch (WrongValueException ex) {
            showError(ex);
        }
    }

    public void onClick$btnSave() throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveCardStatus(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveCardStatus(cardUpdateReasonParam);
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(cardUpdateReasonParam);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(cardUpdateReasonParam);
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                break;
            default:
                break;
        }
    }
}
