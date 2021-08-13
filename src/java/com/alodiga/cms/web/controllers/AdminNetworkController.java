package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Country;
import com.cms.commons.models.Network;
import com.cms.commons.models.State; 
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

public class AdminNetworkController extends GenericAbstractAdminController {

            
    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtName;
    private UtilsEJB utilsEJB = null;
    private Combobox cmbCountry;
    private Network networkParam;
    private Button btnSave;
    private Integer eventType;
    private Toolbarbutton tbbTitle;
    

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);        
        eventType = (Integer) Sessions.getCurrent().getAttribute( WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
           networkParam = null;                    
       } else {
           networkParam = (Network) Sessions.getCurrent().getAttribute("object");            
       }
        initialize();
    }
    
    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.network.interbank.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.network.interbank.view"));
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
        txtName.setRawValue(null);;

    }

    private void loadFields(Network network) {
        try {
            txtName.setText(network.getName());
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
            this.showMessage("sp.error.field.cannotNull", true, null);
        
        } else if (cmbCountry.getSelectedItem() == null) {
            cmbCountry.setFocus(true);
            this.showMessage("cms.error.countryName.error", true, null);
        }else {
            return true;
        }
        return false;

    }


    private void saveNetwork(Network _network) {
        try {
            Network network = null;

            if (_network != null) {

           network = _network;
            } else {//New country
                network = new Network();
            }
            network.setName(txtName.getText());
            network.setCountryId((Country) cmbCountry.getSelectedItem().getValue());
            network = utilsEJB.saveNetwork(network);
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
                    blockFields();
                    saveNetwork(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveNetwork(networkParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(networkParam);
                loadcmbCountry(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(networkParam);
                txtName.setReadonly(true);
                blockFields();
                loadcmbCountry(eventType);
                break;
            case WebConstants.EVENT_ADD:
                loadcmbCountry(eventType);
                break;
            default:
                break;
        }
    }
    
    private void loadcmbCountry(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<Country> countries;
        try {
            countries = utilsEJB.getCountries(request1);
            loadGenericCombobox(countries,cmbCountry, "name",evenInteger,Long.valueOf(networkParam != null? networkParam.getCountryId().getId(): 0) );
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

    
 

