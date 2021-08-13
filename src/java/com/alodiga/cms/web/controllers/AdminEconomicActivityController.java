package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Country;
import com.cms.commons.models.EconomicActivity;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;

public class AdminEconomicActivityController extends GenericAbstractAdminController {

            
    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtCode, txtDescription;
    private UtilsEJB utilsEJB = null;
    private Combobox cmbContryId;
    private EconomicActivity economicActivityParam;
    private Button btnSave;
    private Integer eventType;
    private Toolbarbutton tbbTitle;
    

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute( WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
           economicActivityParam = null;                    
       } else {
           economicActivityParam = (EconomicActivity) Sessions.getCurrent().getAttribute("object");            
       }
        initialize();
    }
    
    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.economic.activity.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.economic.activity.viw"));
                break;
            case WebConstants.EVENT_ADD:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.economic.activity.add"));
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
        txtCode.setRawValue(null);
        txtDescription.setRawValue(null);
        

    }

    private void loadFields(EconomicActivity economicActivity) {
        try {
            txtCode.setText(economicActivity.getCode());
            txtDescription.setText(economicActivity.getDescription());
            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtCode.setReadonly(true);
        txtDescription.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtCode.getText().isEmpty()) {
            txtCode.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtDescription.getText().isEmpty()){
            txtDescription.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);        
        } else {
            return true;
        }
        return false;

    }


    private void saveEconomicActivity(EconomicActivity _economicActivity) {
        try {
            EconomicActivity economicActivity = null;

            if (_economicActivity != null) {

           economicActivity = _economicActivity;
            } else {
                economicActivity = new EconomicActivity();
            }
            economicActivity.setCode(txtCode.getText());
            economicActivity.setDescription(txtDescription.getText());
            economicActivity.setCountryId((Country) cmbContryId.getSelectedItem().getValue());
            economicActivity = utilsEJB.saveEconomicActivity(economicActivity);
            economicActivityParam = economicActivity;
            this.showMessage("sp.common.save.success", false, null);
            
            if (eventType == WebConstants.EVENT_ADD) {
                btnSave.setVisible(false);
            } else {
                btnSave.setVisible(true);
            }
            
        } catch (Exception ex) {
           showError(ex);
        }

    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveEconomicActivity(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveEconomicActivity(economicActivityParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(economicActivityParam);
                loadCmbContryId(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(economicActivityParam);
                blockFields();
                loadCmbContryId(eventType);
                break;
            case WebConstants.EVENT_ADD:
                loadCmbContryId(eventType);
                break;
            default:
                break;
        }
    }
    
    private void loadCmbContryId(Integer evenInteger) {
        //cmbContryId 
        EJBRequest request1 = new EJBRequest();
        List<Country> countries;
        try {
            countries = utilsEJB.getCountries(request1);
            loadGenericCombobox(countries, cmbContryId, "name", evenInteger, Long.valueOf(economicActivityParam != null ? economicActivityParam.getCountryId().getId() : 0));
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
