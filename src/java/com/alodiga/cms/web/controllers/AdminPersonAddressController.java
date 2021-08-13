package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.enumeraciones.EdificationTypeE;
import com.cms.commons.models.Address;
import com.cms.commons.models.AddressType;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class AdminPersonAddressController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblRequestNumber;
    private Label lblRequestDate;
    private Label lblStatusRequest;
    private Textbox txtUbanization;
    private Textbox txtNameStreet;
    private Textbox txtNameEdification;
    private Textbox txtTower;
    private Textbox txtLine1;
    private Textbox txtLine2;
    private Intbox txtFloor;
    private Combobox cmbCountry;
    private Combobox cmbState;
    private Combobox cmbCity;
    private Combobox cmbStreetType;
    private Combobox cmbEdificationType;
    private Combobox cmbZipZone;
    private Combobox cmbAddressTypes;
    private Radio rAddressDeliveryYes;
    private Radio rAddressDeliveryNo;
    private PersonEJB personEJB = null;
    private UtilsEJB utilsEJB = null;
    public static Address addressParent = null;
    private PersonHasAddress personHasAddressParam;
    private Button btnSave;
    private Integer eventType;
    public Window winAdminNaturalPersonAddress;
    private AdminRequestController adminRequest = null;
    private Request requestCard;
    private Long optionMenu;
    private EdificationType edificationType = null;
    Map params = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        AdminRequestController adminRequest = new AdminRequestController();
        if (adminRequest != null) {
            requestCard = adminRequest.getRequest();
        }
        AdminNaturalPersonController adminPerson = new AdminNaturalPersonController();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                personHasAddressParam = (PersonHasAddress) Sessions.getCurrent().getAttribute("object");
                break;
            case WebConstants.EVENT_VIEW:
                personHasAddressParam = (PersonHasAddress) Sessions.getCurrent().getAttribute("object");
                break;
            case WebConstants.EVENT_ADD:
                personHasAddressParam = null;
                break;
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            optionMenu = (Long) session.getAttribute(WebConstants.OPTION_MENU);
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

    private void loadFieldR(Request requestData) {
        try {

            String pattern = "dd-MM-yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            if (requestData.getRequestNumber() != null) {
                lblRequestNumber.setValue(requestData.getRequestNumber());
                lblRequestDate.setValue(simpleDateFormat.format(requestData.getRequestDate()));
                lblStatusRequest.setValue(requestData.getStatusRequestId().getDescription());
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loadFields(PersonHasAddress personHasAddress) {
        try {
            if (personHasAddress.getAddressId().getUrbanization() != null) {
                txtUbanization.setValue(personHasAddress.getAddressId().getUrbanization());
            }
            if (personHasAddress.getAddressId().getNameStreet() != null) {
                txtNameStreet.setValue(personHasAddress.getAddressId().getNameStreet());
            }
            if (personHasAddress.getAddressId().getNameEdification() != null) {
                txtNameEdification.setValue(personHasAddress.getAddressId().getNameEdification());
            }
            if (personHasAddress.getAddressId().getTower() != null) {
                txtTower.setValue(personHasAddress.getAddressId().getTower());
            }
            if (personHasAddress.getAddressId().getFloor() != null) {
                txtFloor.setValue(personHasAddress.getAddressId().getFloor());
            }

            if (personHasAddress.getAddressId().getIndAddressDelivery() != null) {
                if (personHasAddress.getAddressId().getIndAddressDelivery() == true) {
                    rAddressDeliveryYes.setChecked(true);
                } else {
                    rAddressDeliveryNo.setChecked(true);
                }
            }

            if (personHasAddress.getAddressId().getAddressLine1() != null) {
                txtLine1.setValue(personHasAddress.getAddressId().getAddressLine1());
            }
            if (personHasAddress.getAddressId().getAddressLine2() != null) {
                txtLine2.setValue(personHasAddress.getAddressId().getAddressLine2());
            }
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
        rAddressDeliveryYes.setDisabled(true);
        rAddressDeliveryNo.setDisabled(true);
        txtLine1.setReadonly(true);
        txtLine2.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        String edificiationTypeCode = "";
        EdificationType edificationType = (EdificationType) cmbEdificationType.getSelectedItem().getValue();
        String edificationTypeComercial = EdificationTypeE.CECOM.getEdificationTypeCode();
        String edificationTypeResidencia = EdificationTypeE.RESCIA.getEdificationTypeCode();
        String edificationTypeEdif = EdificationTypeE.EDFCIO.getEdificationTypeCode();
        edificiationTypeCode = edificationType.getCode();
        
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
        } else if (cmbZipZone.getSelectedItem() == null){
            this.showMessage("cms.error.zipZone.notSelected", true, null);
        } else if (cmbAddressTypes.getSelectedItem() == null){
            this.showMessage("cms.error.request.addressDeliveryType", true, null);
        } else if ((!rAddressDeliveryYes.isChecked()) && (!rAddressDeliveryNo.isChecked())){
            this.showMessage("cms.error.request.addressDeliveryCheck", true, null);
        } else if ((edificiationTypeCode.equals(edificationTypeResidencia)) || 
          (edificiationTypeCode.equals(edificationTypeComercial)) ){
            if (txtTower.getText().isEmpty()){
                txtTower.setFocus(true);
                this.showMessage("cms.error.field.tower", true, null);
            } else {
                return true;
            }
        } else if((edificiationTypeCode.equals(edificationTypeResidencia)) || (edificiationTypeCode.equals(edificationTypeComercial)) 
            || (edificiationTypeCode.equals(edificationTypeEdif))){
            if ((txtFloor.getText().isEmpty())) {
                txtFloor.setFocus(true);
                this.showMessage("cms.error.floor.noSelected", true, null);
            } else {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }
    
    public void onChange$cmbEdificationType() {
        String edificationTypeResidencia = EdificationTypeE.RESCIA.getEdificationTypeCode();
        String edificationTypeComercial = EdificationTypeE.CECOM.getEdificationTypeCode();
        String edificationTypeEdif = EdificationTypeE.EDFCIO.getEdificationTypeCode();
        String edificiationTypeCode = "";
        
        EdificationType edificationType = (EdificationType) cmbEdificationType.getSelectedItem().getValue();
        edificiationTypeCode = edificationType.getCode();
        
        if((edificiationTypeCode.equals(edificationTypeResidencia)) || (edificiationTypeCode.equals(edificationTypeComercial)) ){
            txtTower.setDisabled(false);
        } else{
            txtTower.setDisabled(true);
        }
        
        if((edificiationTypeCode.equals(edificationTypeResidencia)) || (edificiationTypeCode.equals(edificationTypeComercial)) 
            || (edificiationTypeCode.equals(edificationTypeEdif))){
            txtFloor.setDisabled(false);
        } else{
            txtFloor.setDisabled(true);
        }
        
    }
    

    private void saveAddress(PersonHasAddress _personHasAddress) {
        Person person = null;
        boolean indAddressDelivery = true;
        try {
            Address address = null;
            PersonHasAddress personHasAddress = null;

            if (_personHasAddress != null) {
                address = _personHasAddress.getAddressId();
                personHasAddress = _personHasAddress;

            } else {//New address
                address = new Address();
                personHasAddress = new PersonHasAddress();
            }

            if (rAddressDeliveryYes.isChecked()) {
                indAddressDelivery = true;
            } else {
                indAddressDelivery = false;
            }

            if (optionMenu == Constants.LIST_CARD_REQUEST) {
                AdminRequestController adminRequest = new AdminRequestController();
                if (adminRequest.getRequest().getPersonId() != null) {
                    person = adminRequest.getRequest().getPersonId();
                }
            } else if (optionMenu == Constants.LIST_CUSTOMER_MANAGEMENT) {
                if (AdminNaturalPersonCustomerController.naturalCustomerParam != null) {
                    person = AdminNaturalPersonCustomerController.naturalCustomerParam.getPersonId();
                } else if (AdminLegalPersonCustomerController.legalCustomerParam.getPersonId() != null) {
                    person = AdminLegalPersonCustomerController.legalCustomerParam.getPersonId();
                }
            } else {
                person = null;
            }

            //Guarda la dirección del solicitante
            address.setEdificationTypeId((EdificationType) cmbEdificationType.getSelectedItem().getValue());
            address.setNameEdification(txtNameEdification.getText());
            address.setTower(txtTower.getText());
            address.setFloor(txtFloor.getValue());
            address.setStreetTypeId((StreetType) cmbStreetType.getSelectedItem().getValue());
            address.setNameStreet(txtNameStreet.getText());
            address.setUrbanization(txtUbanization.getText());
            address.setCityId((City) cmbCity.getSelectedItem().getValue());
            address.setZipZoneId((ZipZone) cmbZipZone.getSelectedItem().getValue());
            address.setCountryId((Country) cmbCountry.getSelectedItem().getValue());
            address.setIndAddressDelivery(indAddressDelivery);
            address.setAddressTypeId((AddressType) cmbAddressTypes.getSelectedItem().getValue());
            if (eventType == WebConstants.EVENT_ADD) {
                address.setCreateDate(new Timestamp(new Date().getTime()));
            } else {
                address.setUpdateDate(new Timestamp(new Date().getTime()));
            }
            
            if ((txtTower.getText() != null) && (txtNameEdification.getText() != null)) {
                StringBuilder linea1 = new StringBuilder((((StreetType) cmbStreetType.getSelectedItem().getValue()).getDescription()));
                linea1.append(": ");
                linea1.append(txtNameStreet.getText());
                linea1.append(", Urbanizacion: ");
                linea1.append(txtUbanization.getText());
                linea1.append(", ");
                linea1.append((((EdificationType) cmbEdificationType.getSelectedItem().getValue()).getDescription()));
                linea1.append(": ");
                linea1.append(txtNameEdification.getText());
                linea1.append(", Torre: ");
                linea1.append(txtTower.getText());
                linea1.append(", Piso: ");
                linea1.append(txtFloor.getText());
                txtLine1.setText(linea1.toString());

                StringBuilder linea2 = new StringBuilder("Pais: ");
                linea2.append((((Country) cmbCountry.getSelectedItem().getValue()).getName()));
                linea2.append(", Ciudad: ");
                linea2.append((((City) cmbCity.getSelectedItem().getValue()).getName()));
                linea2.append(", Codigo Postal: ");
                linea2.append((((ZipZone) cmbZipZone.getSelectedItem().getValue()).getCode()));
                txtLine1.setText(linea2.toString());

                address.setAddressLine1(linea1.toString());
                address.setAddressLine2(linea2.toString());
            } else {
                address.setAddressLine1(txtLine1.getText());
                address.setAddressLine2(txtLine2.getText());
            }
            address = utilsEJB.saveAddress(address);
            addressParent = address;

            //Asocia la dirección al solicitante y la guarda en BD
            personHasAddress.setAddressId(address);
            personHasAddress.setPersonId(person);
            personHasAddress = personEJB.savePersonHasAddress(personHasAddress);
            personHasAddressParam = personHasAddress;
            this.showMessage("sp.common.save.success", false, null);
            EventQueues.lookup("updateAddress", EventQueues.APPLICATION, true).publish(new Event(""));
            loadFields(personHasAddress);
            
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
                    onChange$cmbEdificationType();
                    saveAddress(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveAddress(personHasAddressParam);
                    onChange$cmbEdificationType();
                    break;
                default:
                    break;
            }
        }
    }

    public void onClick$btnBack() {
        winAdminNaturalPersonAddress.detach();
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                if (requestCard != null) {
                    loadFieldR(requestCard);
                }
                loadFields(personHasAddressParam);
                loadCmbCountry(eventType);
                if (personHasAddressParam.getAddressId().getStreetTypeId() != null) {
                    loadCmbStreetType(eventType);
                }
                if (personHasAddressParam.getAddressId().getEdificationTypeId() != null) {
                    loadCmbEdificationType(eventType);
                }
                loadCmbAddressTypes(eventType);
                onChange$cmbCountry();
                onChange$cmbState();
                onChange$cmbCity();
                onChange$cmbEdificationType();
                break;
            case WebConstants.EVENT_VIEW:
                if (requestCard != null) {
                    loadFieldR(requestCard);
                }
                loadFields(personHasAddressParam);
                loadCmbCountry(eventType);
                loadCmbStreetType(eventType);
                loadCmbEdificationType(eventType);
                loadCmbAddressTypes(eventType);
                onChange$cmbCountry();
                onChange$cmbState();
                onChange$cmbCity();
                onChange$cmbEdificationType();
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                if (requestCard != null) {
                    loadFieldR(requestCard);
                }
                loadCmbCountry(eventType);
                loadCmbStreetType(eventType);
                loadCmbEdificationType(eventType);
                loadCmbAddressTypes(eventType);
                break;
        }
    }

    private void loadCmbCountry(Integer evenInteger) {
        //cmbCountry
        EJBRequest request1 = new EJBRequest();
        List<Country> countries;
        try {
            countries = utilsEJB.getCountries(request1);
            loadGenericCombobox(countries, cmbCountry, "name", evenInteger, Long.valueOf(personHasAddressParam != null ? personHasAddressParam.getAddressId().getCountryId().getId() : 0));
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
        //cmbState
        EJBRequest request1 = new EJBRequest();
        cmbState.getItems().clear();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_COUNTRY_ID, countryId);
        request1.setParams(params);
        List<State> states;
        try {
            states = utilsEJB.getStatesByCountry(request1);
            loadGenericCombobox(states, cmbState, "name", evenInteger, Long.valueOf(personHasAddressParam != null ? personHasAddressParam.getAddressId().getCityId().getStateId().getId() : 0));
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
            loadGenericCombobox(citys, cmbCity, "name", evenInteger, Long.valueOf(personHasAddressParam != null ? personHasAddressParam.getAddressId().getCityId().getId() : 0));
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

    private void loadCmbStreetType(Integer evenInteger) {
        //cmbStreetType
        EJBRequest request1 = new EJBRequest();
        List<StreetType> streetTypes;
        try {
            streetTypes = utilsEJB.getStreetTypes(request1);
            loadGenericCombobox(streetTypes, cmbStreetType, "description", evenInteger, Long.valueOf(personHasAddressParam != null ? personHasAddressParam.getAddressId().getStreetTypeId().getId() : 0));
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
            loadGenericCombobox(edificationTypes, cmbEdificationType, "description", evenInteger, Long.valueOf(personHasAddressParam != null ? personHasAddressParam.getAddressId().getEdificationTypeId().getId() : 0));
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
                if (personHasAddressParam != null && c.getId().equals(personHasAddressParam.getAddressId().getZipZoneId().getId())) {
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

    private void loadCmbAddressTypes(Integer evenInteger) {
        //cmbAddressTypes
        EJBRequest request1 = new EJBRequest();
        List<AddressType> addressTypes;
        try {
            addressTypes = utilsEJB.getAddressType(request1);
            loadGenericCombobox(addressTypes, cmbAddressTypes, "description", evenInteger, Long.valueOf(personHasAddressParam != null ? personHasAddressParam.getAddressId().getAddressTypeId().getId() : 0));
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
