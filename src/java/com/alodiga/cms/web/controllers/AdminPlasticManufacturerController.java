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
import com.cms.commons.models.Country;
import com.cms.commons.models.DocumentsPersonType;
import com.cms.commons.models.PersonType;
import com.cms.commons.models.Person;
import com.cms.commons.models.PersonClassification;
import com.cms.commons.models.PhonePerson;
import com.cms.commons.models.PhoneType;
import com.cms.commons.models.PlasticManufacturer;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;

public class AdminPlasticManufacturerController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtIdentificationNumber;
    private Intbox intPhoneManufacturer;
    private Textbox txtName;
    private Textbox txtContractNumber;
    private Textbox txtEmailManufacturer;
    private Textbox txtContactPerson;
    private Textbox txtEmailContact;
    private Combobox cmbCountry;
    private Combobox cmbPersonType;
    private Combobox cmbDocumentsPersonType;
    private Radio rActiveYes;
    private Radio rActiveNo;
    private Person person;
    private PlasticManufacturer plasticManufacturerParam;
    private UtilsEJB utilsEJB = null;
    private PersonEJB personEJB = null;
    private RequestEJB requestEJB = null;
    private Button btnSave;
    private Integer eventType;
    private Toolbarbutton tbbTitle;
    public static PlasticManufacturer plasticManufacturerParent = null;
    private List<PhonePerson> phonePersonList = null;
    private boolean indNaturalPerson;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            plasticManufacturerParam = null;
        } else {
            plasticManufacturerParam = (PlasticManufacturer) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.plasticManufacturer.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.plasticManufacturer.view"));
                break;
            default:
                break;
        }
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            requestEJB = (RequestEJB) EJBServiceLocator.getInstance().get(EjbConstants.REQUEST_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onChange$cmbCountry() {
        this.clearMessage();
        cmbPersonType.setValue("");
        cmbPersonType.setVisible(true);
        Country country = (Country) cmbCountry.getSelectedItem().getValue();
        loadCmbPersonType(eventType, country.getId());
    }

    public void onChange$cmbPersonType() {
        cmbDocumentsPersonType.setVisible(true);
        PersonType personType = (PersonType) cmbPersonType.getSelectedItem().getValue();
        Country country = (Country) cmbCountry.getSelectedItem().getValue();
        loadCmbDocumentsPersonType(eventType, personType.getId());
    }

    public void clearFields() {
        txtIdentificationNumber.setRawValue(null);
        txtName.setRawValue(null);
        txtContractNumber.setRawValue(null);
        txtEmailManufacturer.setRawValue(null);
        intPhoneManufacturer.setRawValue(null);
        txtContactPerson.setRawValue(null);
        txtEmailContact.setRawValue(null);
    }

    public void loadFields(PlasticManufacturer plasticManufacturer) {
        EJBRequest request = new EJBRequest();
        Map params = new HashMap();
        try {
            txtIdentificationNumber.setText(plasticManufacturer.getIdentificationNumber());
            txtName.setText(plasticManufacturer.getName());
            txtContractNumber.setValue(plasticManufacturer.getContractNumber());
            txtEmailManufacturer.setText(plasticManufacturer.getPersonId().getEmail());
            if (personEJB.havePhonesByPerson(plasticManufacturer.getPersonId().getId()) > 0) {
                params.put(Constants.PERSON_KEY, plasticManufacturer.getPersonId().getId());
                request.setParams(params);
                phonePersonList = personEJB.getPhoneByPerson(request);
            }
            if (phonePersonList != null) {
                for (PhonePerson p : phonePersonList) {
                    intPhoneManufacturer.setText(plasticManufacturer.getPersonId().getPhonePerson().getNumberPhone());
                }
            }
            txtContactPerson.setText(plasticManufacturer.getContactPerson());
            txtEmailContact.setText(plasticManufacturer.getEmailContactPerson());
            if (plasticManufacturer.getIndStatus() == true) {
                rActiveYes.setChecked(true);
            } else {
                rActiveNo.setChecked(true);
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtIdentificationNumber.setReadonly(true);
        txtName.setReadonly(true);
        txtContractNumber.setDisabled(true);
        txtEmailManufacturer.setReadonly(true);
        intPhoneManufacturer.setReadonly(true);
        txtContactPerson.setReadonly(true);
        txtEmailContact.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (cmbCountry.getSelectedItem() == null) {
            cmbCountry.setFocus(true);
            this.showMessage("cms.error.country.notSelected", true, null);
        } else if (cmbPersonType.getSelectedItem() == null) {
            cmbPersonType.setFocus(true);
            this.showMessage("cms.error.personType.notSelected", true, null);
        } else if (cmbDocumentsPersonType.getSelectedItem() == null) {
            cmbDocumentsPersonType.setFocus(true);
            this.showMessage("cms.error.documentType.notSelected", true, null);
        } else if (txtIdentificationNumber.getText().isEmpty()) {
            txtIdentificationNumber.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtName.getText().isEmpty()) {
            txtName.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtContractNumber.getText().isEmpty()) {
            txtContractNumber.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtEmailManufacturer.getText().isEmpty()) {
            txtEmailManufacturer.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (intPhoneManufacturer.getText().isEmpty()) {
            intPhoneManufacturer.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtContactPerson.getText().isEmpty()) {
            txtContactPerson.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtEmailContact.getText().isEmpty()) {
            txtEmailContact.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        }  else {
            return true;
        }
        return false;

    }

    private void savePlasticManufacturer(PlasticManufacturer _plasticManufacturer) throws RegisterNotFoundException, NullParameterException, GeneralException {
        boolean indStatus = true;
        try {
            PlasticManufacturer plasticManufacturer = null;
            Person person = null;
            PhonePerson phonePerson = null;

            if (_plasticManufacturer != null) {
                plasticManufacturer = _plasticManufacturer;
                person = plasticManufacturer.getPersonId();
                phonePerson = person.getPhonePerson();
            } else {//New PlasticManufacturer
                plasticManufacturer = new PlasticManufacturer();
                person = new Person();
                phonePerson = new PhonePerson();
            }

            if (rActiveYes.isChecked()) {
                indStatus = true;
            } else {
                indStatus = false;
            }

            //Obtener la clasificacion del fabricante de plastico
            EJBRequest request1 = new EJBRequest();
            request1.setParam(Constants.CLASSIFICATION_PERSON_PLASTIC_MANUFACTURER);
            PersonClassification personClassification = utilsEJB.loadPersonClassification(request1);

            //Guardar la persona
            person.setCountryId((Country) cmbCountry.getSelectedItem().getValue());
            person.setPersonTypeId((PersonType) cmbPersonType.getSelectedItem().getValue());
            person.setEmail(txtEmailManufacturer.getText());
            person.setCreateDate(new Timestamp(new Date().getTime()));
            person.setPersonClassificationId(personClassification);
            person = personEJB.savePerson(person);

            //Obtener el tipo de telefono celular
            EJBRequest request = new EJBRequest();
            request.setParam(Constants.PHONE_TYPE_MOBILE);
            PhoneType phoneType = personEJB.loadPhoneType(request);

            //Guarda el telefono del Fabricante de Plastico
            if (phonePerson == null) {
                phonePerson = new PhonePerson();
            }
            phonePerson.setPersonId(person);
            phonePerson.setPhoneTypeId(phoneType);
            phonePerson.setNumberPhone(intPhoneManufacturer.getValue().toString());
            phonePerson = personEJB.savePhonePerson(phonePerson);

            //Guarda el Fabricante de Plastico
            plasticManufacturer.setPersonId(person);
            plasticManufacturer.setDocumentsPersonTypeId((DocumentsPersonType) cmbDocumentsPersonType.getSelectedItem().getValue());
            plasticManufacturer.setIdentificationNumber(txtIdentificationNumber.getText());
            plasticManufacturer.setName(txtName.getText());
            plasticManufacturer.setContractNumber(txtContractNumber.getValue());
            plasticManufacturer.setContactPerson(txtContactPerson.getText().toString());
            plasticManufacturer.setEmailContactPerson(txtEmailContact.getValue().toString());
            plasticManufacturer.setIndStatus(indStatus);
            plasticManufacturer = personEJB.savePlasticManufacturer(plasticManufacturer);
            plasticManufacturerParam = plasticManufacturer;
            this.showMessage("sp.common.save.success", false, null);
            
            if (eventType == WebConstants.EVENT_ADD) {
                btnSave.setVisible(false);
            } else {
                btnSave.setVisible(true);
            }

            EventQueues.lookup("updatePlasticManufacturer", EventQueues.APPLICATION, true).publish(new Event(""));
            
            } catch (Exception ex) {
                showError(ex);
            }

    }

    public void onClick$btnSave() throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    savePlasticManufacturer(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    savePlasticManufacturer(plasticManufacturerParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(plasticManufacturerParam);
                loadCmbCountry(eventType);
                onChange$cmbCountry();
                onChange$cmbPersonType();
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(plasticManufacturerParam);
                blockFields();
                loadCmbCountry(eventType);
                rActiveYes.setDisabled(true);
                rActiveNo.setDisabled(true);
                onChange$cmbCountry();
                onChange$cmbPersonType();
                break;
            case WebConstants.EVENT_ADD:
                loadCmbCountry(eventType);
                break;
            default:
                break;
        }
    }

    private void loadCmbCountry(Integer eventType) {
        EJBRequest request1 = new EJBRequest();
        List<Country> country;
        try {
            country = utilsEJB.getCountries(request1);
            loadGenericCombobox(country, cmbCountry, "name", eventType, Long.valueOf(plasticManufacturerParam != null ? plasticManufacturerParam.getPersonId().getCountryId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }

    private void loadCmbPersonType(Integer evenInteger, Integer countryId) {
        EJBRequest request1 = new EJBRequest();
        cmbPersonType.getItems().clear();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_COUNTRY_ID, countryId);
        params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, Constants.ORIGIN_APPLICATION_CMS_ID);
        if (eventType == WebConstants.EVENT_ADD) {
            params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, Constants.ORIGIN_APPLICATION_CMS_ID);
        } else {
            params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, plasticManufacturerParam.getDocumentsPersonTypeId().getPersonTypeId().getOriginApplicationId().getId());
        }
        if (evenInteger == 1) {
            params.put(QueryConstants.PARAM_IND_NATURAL_PERSON, indNaturalPerson);
        } else {
            params.put(QueryConstants.PARAM_IND_NATURAL_PERSON, plasticManufacturerParam.getDocumentsPersonTypeId().getPersonTypeId().getIndNaturalPerson());
        }
        request1.setParams(params);
        List<PersonType> personType = null;
        try {
            personType = utilsEJB.getPersonTypeByCountryByIndNaturalPerson(request1);
            loadGenericCombobox(personType, cmbPersonType, "description", eventType, Long.valueOf(plasticManufacturerParam != null ? plasticManufacturerParam.getDocumentsPersonTypeId().getPersonTypeId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        } finally {
            if (personType == null) {
                this.showMessage("cms.msj.PersonTypeNull", false, null);
            }
        }
    }

    private void loadCmbDocumentsPersonType(Integer evenInteger, Integer PersonTypeId) {
        cmbDocumentsPersonType.getItems().clear();
        EJBRequest request1 = new EJBRequest();
        Map params = new HashMap();
        params.put(Constants.PERSON_TYPE_KEY, PersonTypeId);
        request1.setParams(params);
        List<DocumentsPersonType> documentsPersonType;
        try {
            documentsPersonType = personEJB.getDocumentsPersonTypeByPersonType(request1);
            loadGenericCombobox(documentsPersonType, cmbDocumentsPersonType, "description", evenInteger, Long.valueOf(plasticManufacturerParam != null ? plasticManufacturerParam.getDocumentsPersonTypeId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }

}
