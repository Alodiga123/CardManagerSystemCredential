package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.StatusRequest;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;

public class AdminStatusRequestController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtDescription;
    private UtilsEJB utilsEJB = null;
    private StatusRequest statusRequestParam;
    private Button btnSave;
    private Integer eventType;
    

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
//        statusRequestParam = (Sessions.getCurrent().getAttribute("object") != null) ? (StatusRequest) Sessions.getCurrent().getAttribute("object") : null;
        eventType = (Integer) Sessions.getCurrent().getAttribute( WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            statusRequestParam = null;
        } else {
            statusRequestParam = (StatusRequest) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }
    
    @Override
    public void initialize() {
        super.initialize();
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        txtDescription.setRawValue(null);
    }

    private void loadFields(StatusRequest statusRequest) {
        try {
            txtDescription.setText(statusRequest.getDescription());
         } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtDescription.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtDescription.getText().isEmpty()) {
            txtDescription.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else {
            return true;
        }
        return false;
    }

    public void onClick$btnCodes() {
        Executions.getCurrent().sendRedirect("/docs/T-SP-E.164D-2009-PDF-S.pdf", "_blank");
    }

    private void saveStatusRequest(StatusRequest _statusRequest) {
        try {
            StatusRequest statusRequest = null;

            if (_statusRequest != null) {
                statusRequest = _statusRequest;
            } else {//New Status Request
                statusRequest = new StatusRequest();
            }
            statusRequest.setDescription(txtDescription.getText());
            
            statusRequest = utilsEJB.saveStatusRequest(statusRequest);
            statusRequestParam = statusRequest;
            this.showMessage("sp.common.save.success", false, null);
        } catch (Exception ex) {
           showError(ex);
        }
    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveStatusRequest(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveStatusRequest(statusRequestParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(statusRequestParam);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(statusRequestParam);
                txtDescription.setDisabled(true);
                break;
            case WebConstants.EVENT_ADD:
                break;
            default:
                break;
        }
    }
}
