package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.ejb.UserEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Textbox;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.Country;
import com.cms.commons.models.State;
import com.cms.commons.models.City;
import com.cms.commons.util.Constants;
import com.cms.commons.util.QueryConstants;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Toolbarbutton;

public class AdminCityController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Combobox cmbCountry;
    private Combobox cmbState;
    private Tab tabCityZipZone;
    private Textbox txtCity;
    private PersonEJB personEJB = null;
    private UtilsEJB utilsEJB = null;
    private UserEJB userEJB = null;
    private City cityParam = null;
    private Button btnSave;
    private Integer eventType;
    public static City cityParent = null;
    private Toolbarbutton tbbTitle;
    List<City> cityList = new ArrayList<City>();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        cityParam = (Sessions.getCurrent().getAttribute("object") != null) ? (City) Sessions.getCurrent().getAttribute("object") : null;
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            cityParam = null;
        } else {
            cityParam = (City) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.city.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.city.view"));
                break;
            case WebConstants.EVENT_ADD:
                 tabCityZipZone.setDisabled(true);
                break;
            default:
                break;
        }
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            userEJB = (UserEJB) EJBServiceLocator.getInstance().get(EjbConstants.USER_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }
    public void onChange$cmbCountry() {
        this.clearMessage();
        cmbState.setVisible(true);
        cmbState.setValue("");
        Country country = (Country) cmbCountry.getSelectedItem().getValue();
        loadCmbState(eventType, country.getId());
    }
    
    public void clearFields() {

    }
    
    public City getCityParent() {
        return this.cityParent;
    }

    private void loadFields(City city) {
        try {
            txtCity.setText(city.getName());    
            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
 
    }

    public void blockFields() {
     
       txtCity.setRawValue(null);
       btnSave.setVisible(false);
       cmbCountry.setDisabled(true);
       cmbState.setDisabled(true);
    }

    public Boolean validateEmpty() {
        if (cmbCountry.getSelectedItem()  == null) {
            cmbCountry.setFocus(true);
            this.showMessage("cms.error.country.notSelected", true, null);     
        } else if (cmbState.getSelectedItem() == null) {
            cmbState.setFocus(true);
            this.showMessage("cms.error.state.notSelected", true, null);
        } else if(txtCity.getText() == null){
            txtCity.setFocus(true);
            this.showMessage("cms.error.field.cityEmpty",true, null);
        }  
        else {
            return true;
        }
        return false;

    }
   
    private void saveCity(City _city) throws RegisterNotFoundException, NullParameterException, GeneralException {
       
        try {
            City city = null;

            if (_city != null) {
                city = _city;
            } else {
                city = new City();
            }   
            
            //Guardar la ciudad
            city.setStateId((State) cmbState.getSelectedItem().getValue());
            city.setName(txtCity.getText());
            city = utilsEJB.saveCity(city);
            cityParam = city;
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
        
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveCity(null);
                break;
                case WebConstants.EVENT_EDIT:
                    saveCity(cityParam);
                break;
                default:
                break;
            }
        
    }

    public void onclick$btnBack() {
        Executions.getCurrent().sendRedirect("listEmployee.zul");
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                cityParent = cityParam;
                loadFields(cityParam);
                loadCmbContryId(eventType);
                onChange$cmbCountry();                
                break;
            case WebConstants.EVENT_VIEW:
                cityParent = cityParam;
                loadFields(cityParam);
                loadCmbContryId(eventType);
                blockFields();
                
                break;
            case WebConstants.EVENT_ADD:
                loadCmbContryId(eventType);
                onChange$cmbCountry();

                break;
            default:
                break;
        }
    }
    
    private void loadCmbContryId(Integer evenInteger) {
        //cmbCountry
        EJBRequest request1 = new EJBRequest();
        List<Country> countries;
        try {
            countries = utilsEJB.getCountries(request1);
            loadGenericCombobox(countries, cmbCountry, "name", evenInteger, Long.valueOf(cityParam != null ? cityParam.getStateId().getCountryId().getId() : 0));
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
        
    private void loadCmbState(Integer evenInteger, int countryId) {
        EJBRequest request1 = new EJBRequest();
        cmbState.getItems().clear();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_COUNTRY_ID, countryId);
        request1.setParams(params);
        List<State> state = null;
        try {
            state = utilsEJB.getStatesByCountry(request1);
            loadGenericCombobox(state, cmbState, "name", evenInteger, Long.valueOf(cityParam != null ? cityParam.getStateId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (GeneralException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (NullParameterException ex) {
            showError(ex);
        } finally {
            if (state == null) {
                this.showMessage("cms.msj.DocumentsPersonTypeNull", false, null);
            }
        }
    }
    
    private Object getSelectedItem() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
