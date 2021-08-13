package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Address;
import com.cms.commons.models.AddressType;
import com.cms.commons.models.City;
import com.cms.commons.models.Country;
import com.cms.commons.models.EdificationType;
import com.cms.commons.models.NaturalPerson;
import com.cms.commons.models.Person;
import com.cms.commons.models.PersonHasAddress;
import com.cms.commons.models.State;
import com.cms.commons.models.StreetType;
import com.cms.commons.models.ZipZone;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Textbox;

public class AdminOwnerAddressController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
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
    private PersonEJB personEJB = null;
    private UtilsEJB utilsEJB = null;
    private Address addressParam;
    private Button btnSave;
    private Integer eventType;
    private NaturalPerson ownerNaturalPerson;
    private AdminOwnerLegalPersonController adminLegalPerson = null;
    private AdminOwnerNaturalPersonController adminNaturalPerson = null;
    private List<PersonHasAddress> personHasAddressList;
    private Integer indHaveAddress = 0;
    private Integer indSelect;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        indSelect = (Integer) Sessions.getCurrent().getAttribute(WebConstants.IND_OWNER_PROGRAM_SELECT);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
        personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);

        try {
            if (eventType == WebConstants.EVENT_ADD) {
                addressParam = null;
            } else {
                if (indSelect == 1) {
                    adminNaturalPerson = new AdminOwnerNaturalPersonController();
                    if (adminNaturalPerson.getNaturalPerson() != null) {
                        if (adminNaturalPerson.getNaturalPerson().getPersonId().getPersonHasAddress() == null) {
                            EJBRequest request1 = new EJBRequest();
                            Map params = new HashMap();
                            params.put(Constants.PERSON_KEY, adminNaturalPerson.getNaturalPerson().getPersonId().getId());
                            request1.setParams(params);
                            personHasAddressList = personEJB.getPersonHasAddressesByPerson(request1);
                            for (PersonHasAddress pha : personHasAddressList) {
                                adminNaturalPerson.getNaturalPerson().getPersonId().setPersonHasAddress(pha);
                            }
                            addressParam = adminNaturalPerson.getNaturalPerson().getPersonId().getPersonHasAddress().getAddressId();
                        } else {
                            indHaveAddress = 1;
                            addressParam = adminNaturalPerson.getNaturalPerson().getPersonId().getPersonHasAddress().getAddressId();
                        }
                    }
                } else {
                    adminLegalPerson = new AdminOwnerLegalPersonController();
                    if (adminLegalPerson.getLegalPerson() != null) {
                        if (adminLegalPerson.getLegalPerson().getPersonId().getPersonHasAddress() == null) {
                            EJBRequest request1 = new EJBRequest();
                            Map params = new HashMap();
                            params.put(Constants.PERSON_KEY, adminLegalPerson.getLegalPerson().getPersonId().getId());
                            request1.setParams(params);
                            personHasAddressList = personEJB.getPersonHasAddressesByPerson(request1);
                            for (PersonHasAddress pha : personHasAddressList) {
                                adminLegalPerson.getLegalPerson().getPersonId().setPersonHasAddress(pha);
                            }
                            addressParam = adminLegalPerson.getLegalPerson().getPersonId().getPersonHasAddress().getAddressId();
                        } else {
                            indHaveAddress = 1;
                            addressParam = adminLegalPerson.getLegalPerson().getPersonId().getPersonHasAddress().getAddressId();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            showError(ex);
        } finally {
            if (indHaveAddress == 0 && addressParam == null) {
                addressParam = null;
            }
            initialize();
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
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
            if (address.getUrbanization() != null) {
                txtUbanization.setValue(address.getUrbanization());
            }
            if (address.getNameStreet() != null) {
                txtNameStreet.setValue(address.getNameStreet());
            }
            if (address.getNameEdification() != null) {
                txtNameEdification.setValue(address.getNameEdification());
            }
            if (address.getTower() != null) {
                txtTower.setValue(address.getTower());
            }
            if (address.getFloor() != null) {
                txtFloor.setValue(address.getFloor());
            }
            if (address.getAddressLine1() != null) {
                txtLine1.setValue(address.getAddressLine1());
            }
            if (address.getAddressLine2() != null) {
                txtLine2.setValue(address.getAddressLine2());
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
        txtLine1.setReadonly(true);
        txtLine2.setReadonly(true);
        cmbCountry.setReadonly(true);
        cmbState.setReadonly(true);
        cmbCity.setReadonly(true);
        cmbEdificationType.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (cmbCountry.getSelectedItem() == null) {
            cmbCountry.setFocus(true);
            this.showMessage("cms.error.country.notSelected", true, null);
        }   else if (cmbState.getSelectedItem() == null) {
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
        } else if ((txtTower.getText().isEmpty())){
            txtTower.setFocus(true);
            this.showMessage("cms.error.field.tower", true, null);
        } else if ((txtFloor.getText().isEmpty())) {
            txtFloor.setFocus(true);
            this.showMessage("cms.error.floor.noSelected", true, null);
        } else if (cmbZipZone.getSelectedItem() == null){
            this.showMessage("cms.error.zipZone.notSelected", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveAddress(Address _address) {
        Person ownerPerson = null;
        try {
            Address address = null;
            PersonHasAddress personHasAddress = null;

            if (_address != null) {
                address = _address;
            } else {
                address = new Address();
                personHasAddress = new PersonHasAddress();
            }

            //Se obtiene la persona asociada al Propietario del Programa
            if (indSelect == 1) {
                if (eventType == WebConstants.EVENT_ADD) {
                    adminNaturalPerson = new AdminOwnerNaturalPersonController();    
                }
                ownerPerson = adminNaturalPerson.getNaturalPerson().getPersonId();
            } else {
                if (eventType == WebConstants.EVENT_ADD) {
                    adminLegalPerson = new AdminOwnerLegalPersonController();    
                }
                ownerPerson = adminLegalPerson.getLegalPerson().getPersonId();
            }

            //Obtiene el tipo de Dirección
            EJBRequest request = new EJBRequest();
            request.setParam(Constants.ADDRESS_TYPE_DELIVERY);
            AddressType addressType = utilsEJB.loadAddressType(request);

            //Se guarda la dirección del propietario del programa
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
            address.setAddressTypeId(addressType);
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
            addressParam = address;

            //Se asocia la dirección a la persona
            personHasAddress.setAddressId(address);
            personHasAddress.setPersonId(ownerPerson);
            personHasAddress = personEJB.savePersonHasAddress(personHasAddress);

            this.showMessage("sp.common.save.success", false, null);
            loadFields(addressParam);

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
                LoadCmbStreetType(eventType);
                loadCmbEdificationType(eventType);
                onChange$cmbCountry();
                onChange$cmbState();
                onChange$cmbCity();
                txtLine1.setReadonly(true);
                txtLine2.setReadonly(true);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(addressParam);
                blockFields();
                loadCmbCountry(eventType);
                LoadCmbStreetType(eventType);
                loadCmbEdificationType(eventType);
                onChange$cmbCountry();
                onChange$cmbState();
                onChange$cmbCity();
                break;
            case WebConstants.EVENT_ADD:
                loadCmbCountry(eventType);
                LoadCmbStreetType(eventType);
                loadCmbEdificationType(eventType);
                txtLine1.setReadonly(true);
                txtLine2.setReadonly(true);
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
            loadGenericCombobox(streetTypes, cmbStreetType, "description", evenInteger, Long.valueOf(addressParam != null ? addressParam.getStreetTypeId().getId() : 0));
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
            loadGenericCombobox(edificationTypes, cmbEdificationType, "description", evenInteger, Long.valueOf(addressParam != null ? addressParam.getEdificationTypeId().getId() : 0));
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
