package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Country;
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

public class AdminStateController extends GenericAbstractAdminController {

            
    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtName;
    private UtilsEJB utilsEJB = null;
    private Combobox cmbCountry;
    private State stateParam;
    private Button btnSave;
    private Integer eventType;
    private Toolbarbutton tbbTitle;
    

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute( WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
           stateParam = null;                    
       } else {
           stateParam = (State) Sessions.getCurrent().getAttribute("object");            
       }
        initialize();
    }
    
    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.state.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.state.view"));
                break;
            case WebConstants.EVENT_ADD:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.state.add"));
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

    private void loadFields(State state) {
        try {
            txtName.setText(state.getName());
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
        
        } else {
            return true;
        }
        return false;

    }


    private void saveState(State _state) {
        try {
            State state = null;

            if (_state != null) {

           state = _state;
            } else {//New country
                state = new State();
            }
            state.setName(txtName.getText());
            state.setCountryId((Country) cmbCountry.getSelectedItem().getValue());
            state = utilsEJB.saveState(state);
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
                    saveState(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveState(stateParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(stateParam);
                loadcmbCountry(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(stateParam);
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
            cmbCountry.getItems().clear();
            for (Country c : countries) {
 
                Comboitem item = new Comboitem();
                item.setValue(c);
                item.setLabel(c.getName());
                item.setDescription(c.getName());
                item.setParent(cmbCountry);
                if (stateParam != null && c.getId().equals(stateParam.getCountryId().getId())) {
                    cmbCountry.setSelectedItem(item);
                }
            }
            if (evenInteger.equals(WebConstants.EVENT_VIEW)) {
                cmbCountry.setDisabled(true);
            }
            
            
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
