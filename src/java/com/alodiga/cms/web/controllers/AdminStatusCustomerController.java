package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.StatusCustomer;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;

public class AdminStatusCustomerController extends GenericAbstractAdminController {
    //test
    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtDescription;
    private PersonEJB personEJB = null;
    private StatusCustomer statusCustomerParam;
    private Button btnSave;
    private Integer event;
    private Toolbarbutton tbbTitle; 

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        statusCustomerParam = (Sessions.getCurrent().getAttribute("object") != null) ? (StatusCustomer) Sessions.getCurrent().getAttribute("object") : null;
        event = (Integer) Sessions.getCurrent().getAttribute("eventType");
        if (eventType == WebConstants.EVENT_ADD) {
           statusCustomerParam = null;                    
       } else {
           statusCustomerParam = (StatusCustomer) Sessions.getCurrent().getAttribute("object");            
       }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (event) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.statusCustomer.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.statusCustomer.view"));
                break;
            case WebConstants.EVENT_ADD:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.statusCustomer.add"));
                break;
            default:
                break;
        }
        try {
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        txtDescription.setRawValue(null);
    }

    private void loadFields(StatusCustomer statusCustomer) {
        try {
            txtDescription.setText(statusCustomer.getDescription());
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

    private void saveStatusCustomer(StatusCustomer _statusCustomer) {
        try {
            StatusCustomer statusCustomer = null;

            if (_statusCustomer != null) {
                statusCustomer = _statusCustomer;
            } else {//New status Customer
                statusCustomer = new StatusCustomer();
            }
            statusCustomer.setDescription(txtDescription.getText());
            statusCustomer = personEJB.saveStatusCustomer(statusCustomer);
            statusCustomerParam = statusCustomer;
            this.showMessage("sp.common.save.success", false, null);
            btnSave.setDisabled(true);
        } catch (Exception ex) {
            showError(ex);
        }

    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (event) {
                case WebConstants.EVENT_ADD:
                    saveStatusCustomer(null);
                break;
                case WebConstants.EVENT_EDIT:
                   saveStatusCustomer(statusCustomerParam);
                break;
            }
        }
    }

    public void loadData() {
        switch (event) {
            case WebConstants.EVENT_EDIT:
                loadFields(statusCustomerParam);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(statusCustomerParam);
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                break;
            default:
                break;
        }
    }


}
