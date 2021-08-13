package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.BinSponsor;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;

public class AdminBinSponsorController extends GenericAbstractAdminController {
    //test
    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtDescription;
    private UtilsEJB utilsEJB = null;
    private BinSponsor binSponsorParam;
    private Button btnSave;
    private Integer event;
    private Toolbarbutton tbbTitle; 

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        binSponsorParam = (Sessions.getCurrent().getAttribute("object") != null) ? (BinSponsor) Sessions.getCurrent().getAttribute("object") : null;
        event = (Integer) Sessions.getCurrent().getAttribute("eventType");
        if (eventType == WebConstants.EVENT_ADD) {
           binSponsorParam = null;                    
       } else {
           binSponsorParam = (BinSponsor) Sessions.getCurrent().getAttribute("object");            
       }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (event) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.binSponsor.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.binSponsor.view"));
                break;
            case WebConstants.EVENT_ADD:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.binSponsor.add"));
                break;
            default:
                break;
        }
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

    private void loadFields(BinSponsor binSponsor) {
        try {
            txtDescription.setText(binSponsor.getDescription());
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

    private void saveBinSponsor(BinSponsor _binSponsor) {
        try {
            BinSponsor binSponsor = null;

            if (_binSponsor != null) {
                binSponsor = _binSponsor;
            } else {//New binSponsor
                binSponsor = new BinSponsor();
            }
            binSponsor.setDescription(txtDescription.getText());
            binSponsor = utilsEJB.saveBinSponsor(binSponsor);
            binSponsorParam = binSponsor;
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
                    saveBinSponsor(null);
                break;
                case WebConstants.EVENT_EDIT:
                   saveBinSponsor(binSponsorParam);
                break;
            }
        }
    }

    public void loadData() {
        switch (event) {
            case WebConstants.EVENT_EDIT:
                loadFields(binSponsorParam);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(binSponsorParam);
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                break;
            default:
                break;
        }
    }


}
