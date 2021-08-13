package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.RequestEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.CivilStatus;
import com.cms.commons.models.Country;
import com.cms.commons.models.DocumentsPersonType;
import com.cms.commons.models.LegalCustomer;
import com.cms.commons.models.LegalCustomerHasLegalRepresentatives;
import com.cms.commons.models.LegalPerson;
import com.cms.commons.models.LegalPersonHasLegalRepresentatives;
import com.cms.commons.models.LegalRepresentatives;
import com.cms.commons.models.Person;
import com.cms.commons.models.PersonClassification;
import com.cms.commons.models.PhonePerson;
import com.cms.commons.models.PhoneType;
import com.cms.commons.models.Request;
import com.cms.commons.models.StatusApplicant;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class AdminLegalRepresentativeController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblRequestNumber;
    private Label lblRequestDate;
    private Label lblStatusRequest;
    private Textbox txtIdentificationNumber;
    private Textbox txtFullName;
    private Textbox txtFullLastName;
    private Textbox txtBirthPlace;
    private Label txtAge;
    private Textbox txtPhoneNumber;
    private Combobox cmbCountry;
    private Combobox cmbDocumentsPersonType;
    private Combobox cmbCivilState;
    private Combobox cmbPhoneType;
    private Radio genderMale;
    private Radio genderFemale;
    private Datebox txtDueDateIdentification;
    private Datebox txtBirthDay;
    private PersonEJB personEJB = null;
    private UtilsEJB utilsEJB = null;
    private RequestEJB requestEJB = null;
    private LegalRepresentatives legalRepresentativesParam;
    private Button btnSave;
    private Integer eventType;
    public Window winAdminlegalRepresentative;
    public String indGender = null;
    public AdminRequestController adminRequest = null;
    public AdminLegalPersonController adminLegalPerson = null;
    public AdminLegalPersonCustomerController adminLegalCustomerPerson = null;
    public AdminOwnerLegalPersonController adminOwnerLegalPerson = null;
    private List<PhonePerson> phonePersonList = null;
    private List<LegalRepresentatives> legalRepresentativeList = null;
    private Long optionMenu = 0L;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        adminRequest = new AdminRequestController();
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                legalRepresentativesParam = (LegalRepresentatives) Sessions.getCurrent().getAttribute("object");
                break;
            case WebConstants.EVENT_VIEW:
                legalRepresentativesParam = (LegalRepresentatives) Sessions.getCurrent().getAttribute("object");
                break;
            case WebConstants.EVENT_ADD:
                legalRepresentativesParam = null;
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
            requestEJB = (RequestEJB) EJBServiceLocator.getInstance().get(EjbConstants.REQUEST_EJB);
            optionMenu = (Long) session.getAttribute(WebConstants.OPTION_MENU);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onChange$cmbCountry() {
        cmbDocumentsPersonType.setVisible(true);
        Country country = (Country) cmbCountry.getSelectedItem().getValue();
        loadCmbDocumentsPersonType(eventType, country.getId());
    }

    public void clearFields() {
        txtIdentificationNumber.setRawValue(null);
        txtFullName.setRawValue(null);
        txtFullLastName.setRawValue(null);
        txtBirthPlace.setRawValue(null);
        txtPhoneNumber.setRawValue(null);
        txtDueDateIdentification.setRawValue(null);
        txtBirthDay.setRawValue(null);
    }

    private void loadFieldR(Request requestData) {
        try {
            String pattern = "yyyy-MM-dd";
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

    private void loadFields(LegalRepresentatives legalRepresentatives) {
        try {
            txtFullName.setText(legalRepresentatives.getFirstNames());
            txtFullLastName.setText(legalRepresentatives.getLastNames());
            txtIdentificationNumber.setText(legalRepresentatives.getIdentificationNumber());
            txtDueDateIdentification.setValue(legalRepresentatives.getDueDateDocumentIdentification());
            txtAge.setValue(legalRepresentatives.getAge().toString());
            txtBirthPlace.setText(legalRepresentatives.getPlaceBirth());
            txtBirthDay.setValue(legalRepresentatives.getDateBirth());
            if (legalRepresentatives.getGender().trim().equalsIgnoreCase("F")) {
                genderFemale.setChecked(true);
            } else {
                genderMale.setChecked(true);
            }

            EJBRequest request = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.PERSON_KEY, legalRepresentatives.getPersonId().getId());
            request.setParams(params);
            phonePersonList = personEJB.getPhoneByPerson(request);
            if (phonePersonList != null) {
                for (PhonePerson p : phonePersonList) {
                    if (p.getPhoneTypeId().getId() != null) {
                        txtPhoneNumber.setText(p.getNumberPhone());
                        legalRepresentativesParam.getPersonId().setPhonePerson(p);
                    }
                }
            }

            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtFullName.setReadonly(true);
        txtFullLastName.setReadonly(true);
        txtIdentificationNumber.setReadonly(true);
        txtDueDateIdentification.setDisabled(true);
        txtBirthPlace.setReadonly(true);
        txtBirthDay.setDisabled(true);
        txtPhoneNumber.setReadonly(true);
        genderFemale.setDisabled(true);
        genderMale.setDisabled(true);
        cmbCountry.setDisabled(true);
        cmbDocumentsPersonType.setDisabled(true);
        cmbPhoneType.setDisabled(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {

        Calendar today = Calendar.getInstance();
        Calendar actual = Calendar.getInstance();
        today.add(Calendar.YEAR, -18);
        Calendar cumpleCalendar = Calendar.getInstance();
        if (!(txtBirthDay.getText().isEmpty())) {
            cumpleCalendar.setTime(((Datebox) txtBirthDay).getValue());
        }
        if (cmbCountry.getSelectedItem() == null) {
            cmbCountry.setFocus(true);
            this.showMessage("cms.error.country.notSelected", true, null);
        } else if (cmbDocumentsPersonType.getSelectedItem() == null) {
            cmbDocumentsPersonType.setFocus(true);
            this.showMessage("cms.error.documentType.notSelected", true, null);
        } else if (txtIdentificationNumber.getText().isEmpty()) {
            txtIdentificationNumber.setFocus(true);
            this.showMessage("cms.error.field.identificationNumber", true, null);
        } else if (txtDueDateIdentification.getText().isEmpty()) {
            txtDueDateIdentification.setFocus(true);
            this.showMessage("cms.error.field.dueDateDocumentIdentification", true, null);
        } else if (txtFullName.getText().isEmpty()) {
            txtFullName.setFocus(true);
            this.showMessage("cms.error.field.fullName", true, null);
        } else if (txtFullLastName.getText().isEmpty()) {
            txtFullLastName.setFocus(true);
            this.showMessage("cms.error.field.lastName", true, null);
        } else if (txtBirthDay.getText().isEmpty()) {
            txtBirthDay.setFocus(true);
            this.showMessage("cms.error.field.txtBirthDay", true, null);
        } else if (cumpleCalendar.compareTo(today) > 0) {
            txtBirthDay.setFocus(true);
            this.showMessage("cms.error.field.errorDayBith", true, null);
        } else if ((!genderFemale.isChecked()) && (!genderMale.isChecked())) {
            this.showMessage("cms.error.field.gener", true, null);
        } else if (cmbCivilState.getSelectedItem() == null) {
            cmbCivilState.setFocus(true);
            this.showMessage("cms.error.civilState.notSelected", true, null);
        } else if (txtBirthPlace.getText().isEmpty()) {
            txtBirthPlace.setFocus(true);
            this.showMessage("cms.error.field.txtBirthPlace", true, null);
        } else if (cmbPhoneType.getSelectedItem() == null) {
            cmbPhoneType.setFocus(true);
            this.showMessage("cms.error.phoneType.notSelected", true, null);
        } else if (txtPhoneNumber.getText().isEmpty()) {
            txtPhoneNumber.setFocus(true);
            this.showMessage("cms.error.field.phoneNumber", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveLegalRepresentatives(LegalRepresentatives _legalRepresentatives) {
        LegalPerson legalPerson = null;
        LegalCustomer legalCustomer = null;
        Person personLegalRepresentatives = null;
        int edad = 0;
        try {
            LegalRepresentatives legalRepresentatives = null;
            LegalPersonHasLegalRepresentatives legalPersonHasLegalRepresentatives = null;
            LegalCustomerHasLegalRepresentatives legalCustomerHasLegalRepresentatives = null;
            PhonePerson phonePerson = null;
            Person person = null;

            //Calculando la edad de la persona segun la fecha de nacimiento
            Calendar today = Calendar.getInstance();
            Calendar cumpleCalendar = Calendar.getInstance();
            if (!(txtBirthDay.getText().isEmpty())) {
                cumpleCalendar.setTime(((Datebox) txtBirthDay).getValue());
            }

            if (cumpleCalendar.compareTo(today) < 0) {
                int anioActual = today.get(Calendar.YEAR);
                int anioNacimiento = cumpleCalendar.get(Calendar.YEAR);
                edad = anioActual - anioNacimiento;
            } else {
                edad = 0;
            }

            if (_legalRepresentatives != null) {
                legalRepresentatives = _legalRepresentatives;
                person = legalRepresentatives.getPersonId();
                phonePerson = legalRepresentatives.getPersonId().getPhonePerson();
            } else {//New LegalPerson
                person = new Person();
                legalRepresentatives = new LegalRepresentatives();
                legalPersonHasLegalRepresentatives = new LegalPersonHasLegalRepresentatives();
                legalCustomerHasLegalRepresentatives = new LegalCustomerHasLegalRepresentatives();
                phonePerson = new PhonePerson();
            }

            if (genderFemale.isChecked()) {
                indGender = "F";
            } else {
                indGender = "M";
            }

            if (optionMenu == Constants.LIST_CARD_REQUEST) {
                adminLegalPerson = new AdminLegalPersonController();
                if (adminLegalPerson.getLegalPerson() != null) {
                    legalPerson = adminLegalPerson.getLegalPerson();
                }    
            }
            
            if (optionMenu == Constants.LIST_CUSTOMER_MANAGEMENT) {
                adminLegalCustomerPerson = new AdminLegalPersonCustomerController();
                if (adminLegalCustomerPerson.getLegalCustomer() != null) {
                    legalCustomer = adminLegalCustomerPerson.getLegalCustomer();
                }    
            }
            
            if (optionMenu == Constants.LIST_PROGRAM_OWNER) {
                adminOwnerLegalPerson = new AdminOwnerLegalPersonController();
                if (adminOwnerLegalPerson.getLegalPerson() != null) {
                    legalPerson = adminOwnerLegalPerson.getLegalPerson();    
                }    
            }

            //Obtener la clasificacion del Representante Legal
            EJBRequest request1 = new EJBRequest();
            request1.setParam(Constants.CLASSIFICATION_PERSON_LEGAL_REPRESENTATIVES);
            PersonClassification personClassification = utilsEJB.loadPersonClassification(request1);
            
            //Obtener el estatus ACTIVO del solicitante
            EJBRequest request = new EJBRequest();
            request.setParam(Constants.STATUS_APPLICANT_ACTIVE);
            StatusApplicant statusApplicant = requestEJB.loadStatusApplicant(request);

            //Guardar la persona
            person.setCountryId((Country) cmbCountry.getSelectedItem().getValue());
            person.setPersonTypeId(((DocumentsPersonType) cmbDocumentsPersonType.getSelectedItem().getValue()).getPersonTypeId());
            if (eventType == 1) {
                person.setCreateDate(new Timestamp(new Date().getTime()));
                person.setPersonClassificationId(personClassification);
            } else {
                person.setUpdateDate(new Timestamp(new Date().getTime()));
            }
            person = personEJB.savePerson(person);
            personLegalRepresentatives = person;
            
            //Guarda el telefono del representante legal
            if (txtPhoneNumber.getText() != null) {
                phonePerson.setNumberPhone(txtPhoneNumber.getText());
                phonePerson.setPersonId(personLegalRepresentatives);
                phonePerson.setPhoneTypeId((PhoneType) cmbPhoneType.getSelectedItem().getValue());
                phonePerson = personEJB.savePhonePerson(phonePerson);
            }

            //Guarda el Representante Legal
            legalRepresentatives.setPersonId(personLegalRepresentatives);
            legalRepresentatives.setFirstNames(txtFullName.getText());
            legalRepresentatives.setLastNames(txtFullLastName.getText());
            legalRepresentatives.setIdentificationNumber(txtIdentificationNumber.getText());
            legalRepresentatives.setDueDateDocumentIdentification(txtDueDateIdentification.getValue());
            legalRepresentatives.setAge(edad);
            legalRepresentatives.setGender(indGender);
            legalRepresentatives.setPlaceBirth(txtBirthPlace.getText());
            legalRepresentatives.setDateBirth(txtBirthDay.getValue());
            legalRepresentatives.setDocumentsPersonTypeId((DocumentsPersonType) cmbDocumentsPersonType.getSelectedItem().getValue());
            legalRepresentatives.setCivilStatusId((CivilStatus) cmbCivilState.getSelectedItem().getValue());
            legalRepresentatives.setStatusApplicantId(statusApplicant);
            legalRepresentatives = utilsEJB.saveLegalRepresentatives(legalRepresentatives);
            legalRepresentativesParam = legalRepresentatives;

            //Asocia el Representante Legal a la persona jurídica
            if (eventType == WebConstants.EVENT_ADD) {
                if (legalPerson != null) {
                    legalPersonHasLegalRepresentatives.setLegalPersonId(legalPerson);
                    legalPersonHasLegalRepresentatives.setLegalRepresentativesid(legalRepresentatives);
                    legalPersonHasLegalRepresentatives = personEJB.saveLegalPersonHasLegalRepresentatives(legalPersonHasLegalRepresentatives);
                } else if (legalCustomer != null) {
                    legalCustomerHasLegalRepresentatives.setLegalCustomerId(legalCustomer);
                    legalCustomerHasLegalRepresentatives.setLegalRepresentativesId(legalRepresentatives);
                    legalCustomerHasLegalRepresentatives = personEJB.saveLegalCustomerHasLegalRepresentatives(legalCustomerHasLegalRepresentatives);
                }
            }
            this.showMessage("sp.common.save.success", false, null);
            EventQueues.lookup("updateLegalRepresentative", EventQueues.APPLICATION, true).publish(new Event(""));

            loadFields(legalRepresentatives);
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
        this.clearMessage();
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveLegalRepresentatives(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveLegalRepresentatives(legalRepresentativesParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void onClick$btnBack() {
        winAdminlegalRepresentative.detach();
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(legalRepresentativesParam);
                loadCmbCountry(eventType);
                onChange$cmbCountry();
                loadCmbCivilState(eventType);
                loadCmbPhoneType(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(legalRepresentativesParam);
                blockFields();
                loadCmbCountry(eventType);
                onChange$cmbCountry();
                loadCmbCivilState(eventType);
                loadCmbPhoneType(eventType);
                break;
            case WebConstants.EVENT_ADD:
                loadCmbCountry(eventType);
                loadCmbCivilState(eventType);
                loadCmbPhoneType(eventType);
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
            loadGenericCombobox(countries, cmbCountry, "name", evenInteger, Long.valueOf(legalRepresentativesParam != null ? legalRepresentativesParam.getPersonId().getCountryId().getId() : 0));
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

    private void loadCmbDocumentsPersonType(Integer evenInteger, int countryId) {
        EJBRequest request1 = new EJBRequest();
        cmbDocumentsPersonType.getItems().clear();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_COUNTRY_ID, countryId);
        params.put(QueryConstants.PARAM_IND_NATURAL_PERSON, WebConstants.IND_NATURAL_PERSON);
        if (eventType == WebConstants.EVENT_ADD) {
            params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, Constants.ORIGIN_APPLICATION_CMS_ID);
        } else {
            if (optionMenu == Constants.LIST_CARD_REQUEST) {
                params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, adminRequest.getRequest().getPersonTypeId().getOriginApplicationId().getId());
            } else {
                params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, Constants.ORIGIN_APPLICATION_CMS_ID);
            }
        }
        request1.setParams(params);
        List<DocumentsPersonType> documentsPersonType;
        try {
            documentsPersonType = utilsEJB.getDocumentsPersonByCountry(request1);
            loadGenericCombobox(documentsPersonType, cmbDocumentsPersonType, "description", evenInteger, Long.valueOf(legalRepresentativesParam != null ? legalRepresentativesParam.getDocumentsPersonTypeId().getId() : 0));
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

    private void loadCmbCivilState(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<CivilStatus> civilStatusList;

        try {
            civilStatusList = personEJB.getCivilStatus(request1);
            loadGenericCombobox(civilStatusList, cmbCivilState, "description", evenInteger, Long.valueOf(legalRepresentativesParam != null ? legalRepresentativesParam.getCivilStatusId().getId() : 0));
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

    private void loadCmbPhoneType(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<PhoneType> phoneType;

        try {
            phoneType = personEJB.getPhoneType(request1);
            loadGenericCombobox(phoneType, cmbPhoneType, "description", evenInteger, Long.valueOf(legalRepresentativesParam != null ? legalRepresentativesParam.getPersonId().getPhonePerson().getPhoneTypeId().getId() : 0));
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
