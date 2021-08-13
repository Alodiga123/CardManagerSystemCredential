package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.RequestEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Address;
import com.cms.commons.models.AddressType;
import com.cms.commons.models.ApplicantNaturalPerson;
import com.cms.commons.models.City;
import com.cms.commons.models.Country;
import com.cms.commons.models.EdificationType;
import com.cms.commons.models.Person;
import com.cms.commons.models.PersonHasAddress;
import com.cms.commons.models.Request;
import com.cms.commons.models.State;
import com.cms.commons.models.StreetType;
import com.cms.commons.models.ZipZone;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Textbox;

public class AdminCardComplementariesAddressController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtUbanization;
    private Textbox txtNameStreet;
    private Textbox txtNameEdification;
    private Textbox txtTower;
    private Textbox txtFloor;
    private Textbox txtZipZone;
    private Combobox cmbCountry;
    private Combobox cmbState;
    private Combobox cmbCity;
    private Combobox cmbStreetType;
    private Combobox cmbEdificationType;
    private Combobox cmbZipZone;
    private Hlayout layaoutTextZipZone;
    private Hlayout layaoutCmbZipZone;
    private PersonEJB personEJB = null;
    private UtilsEJB utilsEJB = null;
    private RequestEJB requestEJB = null;
    private Address addressParam;
    private ApplicantNaturalPerson applicantNaturalPerson = null;
    private List<PersonHasAddress> personHasAddressList = null;
    private Button btnSave;
    private Integer eventType;
    private Integer indHaveAddress = 0;


    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
        try {
            if (eventType != WebConstants.EVENT_ADD) {
                if (((ApplicantNaturalPerson) Sessions.getCurrent().getAttribute("object")) != null) {
                    applicantNaturalPerson = (ApplicantNaturalPerson) Sessions.getCurrent().getAttribute("object");
                    if (applicantNaturalPerson.getPersonId().getPersonHasAddress() != null) {
                        indHaveAddress = 1;
                        addressParam = applicantNaturalPerson.getPersonId().getPersonHasAddress().getAddressId();
                    } else {
                        EJBRequest request1 = new EJBRequest();
                        Map params = new HashMap();
                        params.put(Constants.PERSON_KEY, applicantNaturalPerson.getPersonId().getId());
                        request1.setParams(params);
                        personHasAddressList = personEJB.getPersonHasAddressesByPerson(request1);                                               
                        for (PersonHasAddress pha : personHasAddressList) {
                            applicantNaturalPerson.getPersonId().setPersonHasAddress(pha);
                        }
                        addressParam = applicantNaturalPerson.getPersonId().getPersonHasAddress().getAddressId();
                        indHaveAddress = 1;
                    }
                }
            } else {
                addressParam = null;
            }
        } catch (Exception ex) {
            showError(ex);
        } finally {
            if (indHaveAddress == 0) {
                addressParam = null;
            }
        }        
        initialize();
    }
    
    @Override
    public void initialize() {
        super.initialize();
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            requestEJB = (RequestEJB) EJBServiceLocator.getInstance().get(EjbConstants.REQUEST_EJB);
            loadData();
            
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onChange$cmbCountry() {
        cmbState.setVisible(true);
        Country country = (Country) cmbCountry.getSelectedItem().getValue();
        loadCmbState(eventType, country.getId());
    }

    public void onChange$cmbState() {
        cmbCity.setVisible(true);
        State state = (State) cmbState.getSelectedItem().getValue();
        loadCmbCity(eventType, state.getId());
    }

    public void onChange$cmbCity() {
        cmbZipZone.setVisible(true);
        City city = (City) cmbCity.getSelectedItem().getValue();
        LoadCmbZipZone(eventType, city.getId());
    }

    public void clearFields() {
        txtUbanization.setRawValue(null);
        txtNameStreet.setRawValue(null);
        txtNameEdification.setRawValue(null);
        txtTower.setRawValue(null);
        txtFloor.setRawValue(null);
    }
    
    private void loadFields(Address address) {
        try {
            List<Request> requestList = new ArrayList<Request>();
            ApplicantNaturalPerson applicant = applicantNaturalPerson.getApplicantParentId();
            if(applicant != null){
                EJBRequest request1 = new EJBRequest();
                request1.setParam(applicant.getId());
                ApplicantNaturalPerson applicantPrimary = personEJB.loadApplicantNaturalPerson(request1);

                if(applicantPrimary != null){
                    Person personPrimary = applicantPrimary.getPersonId();
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put(QueryConstants.PARAM_PERSON_ID, personPrimary.getId());
                    request1.setParams(params);
                    requestList = requestEJB.searchCardRequestFromAppByPersonId(request1);
                    if(requestList != null){
                        layaoutTextZipZone.setVisible(true);
                        layaoutCmbZipZone.setVisible(false);
                        txtZipZone.setText(address.getZipZoneCode());
                    }
                }
            }

            txtUbanization.setText(address.getUrbanization());
            txtNameStreet.setText(address.getNameStreet());
            txtNameEdification.setText(address.getNameEdification());
            txtTower.setText(address.getTower());
            txtFloor.setText(address.getFloor().toString());
            btnSave.setVisible(true);
            

            
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtUbanization.setReadonly(true);
        txtNameStreet.setReadonly(true);
        txtNameEdification.setReadonly(true);
        txtTower.setReadonly(true);
        txtFloor.setReadonly(true);
        cmbCountry.setDisabled(true);
        cmbState.setDisabled(true);
        cmbCity.setDisabled(true);
        cmbEdificationType.setDisabled(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (cmbCountry.getSelectedItem() == null) {
            cmbCountry.setFocus(true);
            this.showMessage("cms.error.country.notSelected", true, null);
        } else if (cmbState.getSelectedItem() == null) {
            cmbState.setFocus(true);
            this.showMessage("cms.error.state.noSelected", true, null);
        } else if (cmbCity.getSelectedItem() == null) {
            cmbCity.setFocus(true);
            this.showMessage("cms.error.field.city", true, null);
        } else if (txtUbanization.getText().isEmpty()) {
            txtUbanization.setFocus(true);
            this.showMessage("cms.error.field.urbanization", true, null);
        } else if (cmbStreetType.getSelectedItem() == null) {
            cmbStreetType.setFocus(true);
            this.showMessage("cms.error.streetType.noSelected", true, null);
        } else if (txtNameStreet.getText().isEmpty()) {
            txtNameStreet.setFocus(true);
            this.showMessage("cms.error.field.namesStreet", true, null);
        } else if (cmbEdificationType.getSelectedItem() == null) {
            cmbEdificationType.setFocus(true);
            this.showMessage("cms.error.edificationType.notSelected", true, null);
        } else if (txtNameEdification.getText().isEmpty()) {
            txtNameEdification.setFocus(true);
            this.showMessage("cms.error.field.nameEdification", true, null);
        } else if (txtTower.getText().isEmpty()) {
            txtTower.setFocus(true);
            this.showMessage("cms.error.field.tower", true, null);
        } else if (txtFloor.getText().isEmpty()) {
            txtFloor.setFocus(true);
            this.showMessage("sp.error.field.floor", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveAddress(Address _address) {
        Person applicantPersonCard = null;
        try {
            Address address = null;
            PersonHasAddress personHasAddress = null;

            //Se obtiene la persona asociada a la tarjeta complementaria
            AdminCardComplementariesController adminCardComplementary = new AdminCardComplementariesController();
            if (adminCardComplementary.getPersonCardComplementary() != null) {
                applicantPersonCard = adminCardComplementary.getPersonCardComplementary();
            }

            if (_address != null) {
                address = _address;
                personHasAddress = applicantPersonCard.getPersonHasAddress();
            } else {//New address
                address = new Address();
                personHasAddress = new PersonHasAddress();
            }

            EJBRequest request = new EJBRequest();
            request.setParam(Constants.ADDRESS_TYPE_DELIVERY);
            AddressType addressType = utilsEJB.loadAddressType(request);

            address.setEdificationTypeId((EdificationType) cmbEdificationType.getSelectedItem().getValue());
            address.setNameEdification(txtNameEdification.getText());
            address.setTower(txtTower.getText());
            address.setFloor(Integer.parseInt(txtFloor.getText()));
            address.setStreetTypeId((StreetType) cmbStreetType.getSelectedItem().getValue());
            address.setNameStreet(txtNameStreet.getText());
            address.setUrbanization(txtUbanization.getText());
            address.setCityId((City) cmbCity.getSelectedItem().getValue());
            
            if(cmbZipZone.getSelectedItem() != null){
               address.setZipZoneId((ZipZone) cmbZipZone.getSelectedItem().getValue());
            } else if (!txtZipZone.getText().isEmpty()){
               address.setZipZoneCode(txtZipZone.getText());
            }
            
            address.setCountryId((Country) cmbCountry.getSelectedItem().getValue());
            address.setAddressTypeId(addressType);
            address = utilsEJB.saveAddress(address);
            addressParam = address;

            //PersonHasAddress
            personHasAddress.setAddressId(address);
            personHasAddress.setPersonId(applicantPersonCard);
            personHasAddress = personEJB.savePersonHasAddress(personHasAddress);

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
                    saveAddress(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveAddress(addressParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(addressParam);
                loadCmbCountry(eventType);
                onChange$cmbCountry();
                onChange$cmbState();
                onChange$cmbCity();
                LoadCmbStreetType(eventType);
                loadCmbEdificationType(eventType);                
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(addressParam);
                blockFields();
                loadCmbCountry(eventType);
                onChange$cmbCountry();
                onChange$cmbState();
                onChange$cmbCity();
                LoadCmbStreetType(eventType);
                loadCmbEdificationType(eventType);
                break;
            case WebConstants.EVENT_ADD:
                loadCmbCountry(eventType);
                LoadCmbStreetType(eventType);
                loadCmbEdificationType(eventType);
                break;
            default:
                break;
        }
    }

    private void loadCmbCountry(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<Country> countries;
        try {
            countries = utilsEJB.getCountries(request1);
            loadGenericCombobox(countries, cmbCountry, "name", evenInteger, Long.valueOf(addressParam != null ? addressParam.getCountryId().getId() : 0));
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

    private void loadCmbState(Integer evenInteger, int country) {
        //cmbState
        EJBRequest request1 = new EJBRequest();
        cmbState.getItems().clear();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_COUNTRY_ID, country);
        request1.setParams(params);
        List<State> states;
        try {
            states = utilsEJB.getStatesByCountry(request1);
            loadGenericCombobox(states, cmbState, "name", evenInteger, Long.valueOf(addressParam != null ? addressParam.getCityId().getStateId().getId() : 0));
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

    private void loadCmbCity(Integer evenInteger, int stateId) {
        //cmbCity
        EJBRequest request1 = new EJBRequest();
        cmbCity.getItems().clear();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_STATE_ID, stateId);
        request1.setParams(params);
        List<City> citys;
        try {
            citys = utilsEJB.getCitiesByState(request1);
            loadGenericCombobox(citys, cmbCity, "name", evenInteger, Long.valueOf(addressParam != null ? addressParam.getCityId().getId() : 0));
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

    private void LoadCmbStreetType(Integer evenInteger) {
        //cmbStreetType
        EJBRequest request1 = new EJBRequest();
        List<StreetType> streetTypes;
        try {
            streetTypes = utilsEJB.getStreetTypes(request1);
            if((addressParam == null) || (addressParam.getStreetTypeId() == null)){
               loadGenericCombobox(streetTypes, cmbStreetType, "description", evenInteger, Long.valueOf(0)); 
            } else {
               loadGenericCombobox(streetTypes, cmbStreetType, "description", evenInteger, Long.valueOf(addressParam != null ? addressParam.getStreetTypeId().getId() : 0)); 
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

    private void loadCmbEdificationType(Integer evenInteger) {
        //cmbEdificationType
        EJBRequest request1 = new EJBRequest();
        List<EdificationType> edificationTypes;
        try {
            
            edificationTypes = utilsEJB.getEdificationTypes(request1);
            if((addressParam == null) || (addressParam.getEdificationTypeId() == null)){
               loadGenericCombobox(edificationTypes, cmbEdificationType, "description", evenInteger, Long.valueOf(0)); 
            } else {
               loadGenericCombobox(edificationTypes, cmbEdificationType, "description", evenInteger, Long.valueOf(addressParam != null ? addressParam.getEdificationTypeId().getId() : 0)); 
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

    private void LoadCmbZipZone(Integer evenInteger, int cityId) {
        //cmbZipZone
        EJBRequest request1 = new EJBRequest();
        cmbZipZone.getItems().clear();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_CITY_ID, cityId);
        request1.setParams(params);
        List<ZipZone> zipZones;
        try {
            zipZones = utilsEJB.getZipZoneByCities(request1);
            cmbZipZone.getItems().clear();
            for (ZipZone c : zipZones) {

                Comboitem item = new Comboitem();
                item.setValue(c);
                item.setLabel(c.getCode());
                item.setDescription(c.getName());
                item.setParent(cmbZipZone);
                if (addressParam != null && c.getId().equals(addressParam.getZipZoneId().getId())) {
                    cmbZipZone.setSelectedItem(item);
                }
            }
            if (evenInteger.equals(WebConstants.EVENT_VIEW)) {
                cmbZipZone.setDisabled(true);
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
