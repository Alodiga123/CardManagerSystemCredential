package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.AccountType;
import com.cms.commons.models.SubAccountType;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;

public class AdminSubAccountTypesControllers extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtSubAccount;
    private Combobox cmbAccountType;
    private CardEJB cardEJB = null;
    private SubAccountType subAccountTypeParam;
    private Toolbarbutton tbbTitle;
    private Button btnSave;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
//        subAccountTypeParam = (Sessions.getCurrent().getAttribute("object") != null) ? (SubAccountType) Sessions.getCurrent().getAttribute("object") : null;
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            subAccountTypeParam = null;
        } else {
            subAccountTypeParam = (SubAccountType) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
        loadData();
    }

    
    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.common.subAccountType.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.common.subAccountType.view"));
                break;
            case WebConstants.EVENT_ADD:
                tbbTitle.setLabel(Labels.getLabel("cms.common.subAccountType.add"));
                break;
            default:
                break;
        }
        try {
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        txtSubAccount.setRawValue(null);
    }
    
     public Boolean validateEmpty() {
        if (cmbAccountType.getSelectedItem() == null) {
            cmbAccountType.setFocus(true);
            this.showMessage("cms.error.account.subType.noSelected", true, null);
        } else if (txtSubAccount.getText().isEmpty()) {
            txtSubAccount.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void loadFields(SubAccountType subAccountType) {
         try {
            txtSubAccount.setText(subAccountType.getName());
            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtSubAccount.setReadonly(true);
        btnSave.setVisible(false);
    }

    private void saveSubAccountType(SubAccountType _subAccountType) {
        try {
            SubAccountType subAccountType = null;

            if (_subAccountType != null) {
                subAccountType = _subAccountType;
            } else {//New requestType
                subAccountType = new SubAccountType();
            }

            //insertando en collectionType
            subAccountType.setName(txtSubAccount.getText());
            subAccountType.setAccountTypeId((AccountType) cmbAccountType.getSelectedItem().getValue());
            subAccountType = cardEJB.saveSubAccountType(subAccountType);
            subAccountTypeParam = subAccountType;
            this.showMessage("sp.common.save.success", false, null);
            if (eventType == WebConstants.EVENT_EDIT) {
                btnSave.setVisible(true);
            }else {
                btnSave.setVisible(false);
            }
        } catch (Exception ex) {
            showError(ex);
        }

    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveSubAccountType(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveSubAccountType(subAccountTypeParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(subAccountTypeParam);
                loadCmbAccountType(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(subAccountTypeParam);
                loadCmbAccountType(eventType);
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                loadCmbAccountType(eventType);
                break;
            default:
                break;
        }
    }

    private void loadCmbAccountType(Integer evenInteger) {
        //cmbAccountType
        EJBRequest request1 = new EJBRequest();
        List<AccountType> accountTypes;
        try {
            accountTypes = cardEJB.getAccountType(request1);
            loadGenericCombobox(accountTypes,cmbAccountType, "description",evenInteger,Long.valueOf(subAccountTypeParam != null? subAccountTypeParam.getAccountTypeId().getId(): 0));            
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }

}
