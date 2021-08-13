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
import com.cms.commons.models.NaturalCustomer;
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

public class AdminCustomerCardAddressController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtUbanization;
    private Textbox txtNameStreet;
    private Textbox txtNameEdification;
    private Textbox txtTower;
    private Intbox txtFloor;
    private Textbox txtEmail;
    private Textbox txtLine1;
    private Textbox txtLine2;
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
    private NaturalCustomer naturalCustomer;
    private AdminCustomerCardComplementariesController adminCustomerCardComplementary = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        adminCustomerCardComplementary = new AdminCustomerCardComplementariesController();        
        eventType = adminCustomerCardComplementary.getEventType();
        personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
        if (eventType != WebConstants.EVENT_ADD) {
            if (((NaturalCustomer) Sessions.getCurrent().getAttribute("object")) != null) {
                naturalCustomer = (NaturalCustomer) Sessions.getCurrent().getAttribute("object");
                if (naturalCustomer.getPersonId().getPersonHasAddress() != null) {
                    addressParam = naturalCustomer.getPersonId().getPersonHasAddress().getAddressId();
                } else {
                    Long customerHasAddress = personEJB.countAddressByPerson(naturalCustomer.getPersonId().getId());
                    if (customerHasAddress > 0) {
                        EJBRequest request = new EJBRequest();
                        Map params = new HashMap();
                        params.put(Constants.PERSON_KEY, naturalCustomer.getPersonId().getId());
                        request.setParams(params);
                        List<PersonHasAddress> phaList = personEJB.getPersonHasAddressesByPerson(request);
                        for (PersonHasAddress pha: phaList) {
                            naturalCustomer.getPersonId().setPersonHasAddress(pha);
                        }
                        addressParam = naturalCustomer.getPersonId().getPersonHasAddress().getAddressId();
                    } else {
                        addressParam = null;
                    }                        
                }
            }
        } else {
            addressParam = null;
        }        
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
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
        txtEmail.setRawValue(null);
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
        cmbCountry.setDisabled(true);
        cmbState.setDisabled(true);
        cmbCity.setDisabled(true);
        cmbEdificationType.setDisabled(true);
        txtLine1.setReadonly(true);
        txtLine2.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtUbanization.getText().isEmpty()) {
            txtUbanization.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtNameStreet.getText().isEmpty()) {
            txtNameStreet.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtNameEdification.getText().isEmpty()) {
            txtNameEdification.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtTower.getText().isEmpty()) {
            txtTower.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtFloor.getText().isEmpty()) {
            txtFloor.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (cmbZipZone.getSelectedItem() == null) {
            cmbZipZone.setFocus(true);
            this.showMessage("cms.error.zipZone.notSelected", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveAddress(Address _address) {
        Person customerPersonCard = null;
        try {
            Address address = null;
            PersonHasAddress personHasAddress = null;

            if (eventType != WebConstants.EVENT_ADD && naturalCustomer.getPersonId() != null) {
                if (naturalCustomer.getPersonId().getPersonHasAddress() != null) {
                    personHasAddress = naturalCustomer.getPersonId().getPersonHasAddress();
                }                                 
            }            

            if (_address != null) {
                address = _address;
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
                StringBuilder linea1 = new StringBuilder("Tipo de Calle: ");
                linea1.append((((StreetType) cmbStreetType.getSelectedItem().getValue()).getDescription()));
                linea1.append(", Calle: ");
                linea1.append(txtNameStreet.getText());
                linea1.append(", Urbanizacion: ");
                linea1.append(txtUbanization.getText());
                linea1.append(", Edificacion: ");
                linea1.append((((EdificationType) cmbEdificationType.getSelectedItem().getValue()).getDescription()));
                linea1.append(", Nombre del edificio: ");
                linea1.append(txtNameEdification.getText());
                linea1.append(", Torre: ");
                linea1.append(txtTower.getText());
                linea1.append(", Piso: ");
                linea1.append(txtFloor.getText());

                StringBuilder linea2 = new StringBuilder("Pais: ");
                linea2.append((((Country) cmbCountry.getSelectedItem().getValue()).getName()));
                linea2.append(", Ciudad: ");
                linea2.append((((City) cmbCity.getSelectedItem().getValue()).getName()));
                linea2.append(", Codigo Postal: ");
                linea2.append((((ZipZone) cmbZipZone.getSelectedItem().getValue()).getCode()));

                address.setAddressLine1(linea1.toString());
                address.setAddressLine2(linea2.toString());
            } else {
                address.setAddressLine1(txtLine1.getText());
                address.setAddressLine2(txtLine2.getText());
            }
            address = utilsEJB.saveAddress(address);
            addressParam = address;

            //PersonHasAddress
            if (eventType != WebConstants.EVENT_ADD) {
                personHasAddress.setPersonId(naturalCustomer.getPersonId());
            } else {
                //Se obtiene la persona asociada a la tarjeta complementaria
                if (adminCustomerCardComplementary.getCustomerCard() != null) {
                    customerPersonCard = adminCustomerCardComplementary.getCustomerCard();
                }
                personHasAddress.setPersonId(customerPersonCard);                
            }            
            personHasAddress.setAddressId(address);
            personHasAddress = personEJB.savePersonHasAddress(personHasAddress);

            this.showMessage("sp.common.save.success", false, null);
            loadFields(addressParam);
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
