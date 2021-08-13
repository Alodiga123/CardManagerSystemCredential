package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.RequestEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import static com.alodiga.cms.web.controllers.AdminRequestController.eventType;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.ApplicantNaturalPerson;
import com.cms.commons.models.CivilStatus;
import com.cms.commons.models.Country;
import com.cms.commons.models.DocumentsPersonType;
import com.cms.commons.models.Person;
import com.cms.commons.models.PersonClassification;
import com.cms.commons.models.PhonePerson;
import com.cms.commons.models.PhoneType;
import com.cms.commons.models.Profession;
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
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tab;

public class AdminNaturalPersonController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblRequestNumber;
    private Label lblRequestDate;
    private Label lblStatusRequest;
    private Label txtCodeCountryPhoneL;
    private Label txtCodeCountryPhone; 
    private Label lblCountry;
    private Textbox txtAreaCodePhone;
    private Textbox txtPhoneCel;
    private Textbox txtAreaCodePhoneL;
    private Textbox txtPhoneCelL;          
    private Textbox txtIdentificationNumber;
    private Textbox txtIdentificationNumberOld;
    private Textbox txtFullName;
    private Textbox txtFullLastName;
    private Textbox txtMarriedLastName;
    private Textbox txtBirthPlace;
    private Intbox txtFamilyResponsibilities;
    private Textbox txtEmail;
    private Textbox txtObservations;
    private Combobox cmbDocumentsPersonType;
    private Combobox cmbCivilState;
    private Combobox cmbProfession;
    private Combobox cmbPhoneType;
    private Combobox cmbCountryPhoneL;
    private Combobox cmbCountryPhone;      
    private Datebox txtBirthDay;
    private Datebox txtDueDateDocumentIdentification;
    private Radio genderMale;
    private Radio genderFemale;
    private Radio rIsPrincipalNumberLYes;
    private Radio rIsPrincipalNumerLNo;
    private Radio rIsPrincipalNumberYes;
    private Radio rIsPrincipalNumerNo;       
    public String indGender = null;
    private Tab tabAddress;
    private Tab tabFamilyReferencesMain;
    private Tab tabAdditionalCards;
    private Tab tabMain;
    private UtilsEJB utilsEJB = null;
    private PersonEJB personEJB = null;
    private RequestEJB requestEJB = null;
    private Person person;
    private Button btnSave;
    private Integer eventType;
    public static Person applicant = null;
    public static ApplicantNaturalPerson applicantNaturalPersonParent = null;
    private AdminRequestController adminRequest = null;
    public Person applicantPersonParam;
    public ApplicantNaturalPerson applicantNaturalPersonParam;
    private List<PhonePerson> phonePersonList = null;
    List<ApplicantNaturalPerson> applicantNaturalPersonList = null;
    private PhonePerson cellPhone = null;
    private PhonePerson localPhone = null;
    private Profession profesion = null;
    private Request request = null;
    private Country requestCountry = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
        adminRequest = new AdminRequestController();
        if (adminRequest.getEventType() != null) {
            eventType = adminRequest.getEventType();
            if (eventType == WebConstants.EVENT_ADD) {
                applicantNaturalPersonParam = null;
            } else {
                if (adminRequest.getRequest().getPersonId() != null) {
                    EJBRequest request1 = new EJBRequest();
                    Map params = new HashMap();
                    params.put(Constants.PERSON_KEY, adminRequest.getRequest().getPersonId().getId());
                    request1.setParams(params);
                    applicantNaturalPersonList = personEJB.getApplicantByPerson(request1);
                    for (ApplicantNaturalPerson applicantNaturalPerson : applicantNaturalPersonList) {
                        if (applicantNaturalPerson.getPersonId().getPhonePerson() == null) {
                            request1 = new EJBRequest();
                            params = new HashMap();
                            params.put(Constants.PERSON_KEY, applicantNaturalPerson.getPersonId().getId());
                            request1.setParams(params);
                            phonePersonList = personEJB.getPhoneByPerson(request1);
                            for (PhonePerson phone : phonePersonList) {
                                if (phone.getPhoneTypeId().getId() == Constants.PHONE_TYPE_MOBILE) {
                                    applicantNaturalPerson.getPersonId().setPhonePerson(phone);
                                }                                
                            }
                        }
                        applicantNaturalPersonParam = applicantNaturalPerson;
                    }
                } else {
                    applicantNaturalPersonParam = null;
                }
            }
            switch (eventType) {
                case WebConstants.EVENT_EDIT:
                    if (adminRequest.getRequest().getPersonId() != null) {
                        tabAddress.setDisabled(false);
                        tabFamilyReferencesMain.setDisabled(false);
                        tabAdditionalCards.setDisabled(false);
                    } else {
                        tabAddress.setDisabled(true);
                        tabFamilyReferencesMain.setDisabled(true);
                        tabAdditionalCards.setDisabled(true);
                    }
                    break;
                case WebConstants.EVENT_VIEW:
                    if (adminRequest.getRequest().getPersonId() != null) {
                        tabAddress.setDisabled(false);
                        tabFamilyReferencesMain.setDisabled(false);
                        tabAdditionalCards.setDisabled(false);
                    } else {
                        tabAddress.setDisabled(true);
                        tabFamilyReferencesMain.setDisabled(true);
                        tabAdditionalCards.setDisabled(true);
                    }
                    break;
                case WebConstants.EVENT_ADD:
                    applicantNaturalPersonParam = null;
                    tabAddress.setDisabled(true);
                    tabFamilyReferencesMain.setDisabled(true);
                    tabAdditionalCards.setDisabled(true);
                    break;
            }
        }
        initialize();
        getCountryRequest();
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

    public Person getApplicant() {
        return applicant;
    }

    public ApplicantNaturalPerson getApplicantNaturalPerson() {
        return applicantNaturalPersonParent;
    }
    
    public void getCountryRequest(){
        AdminRequestController  adminRequest = new AdminRequestController();
        if(adminRequest.getRequest().getId() != null){
            request = adminRequest.getRequest();
            requestCountry = request.getCountryId();
            lblCountry.setValue(request.getCountryId().getName());
            loadCmbDocumentsPersonType(eventType, request.getCountryId().getId());
        }
    }
    
    public void onSelect$tabMain() {
        try {
            doAfterCompose(self);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        txtIdentificationNumber.setRawValue(null);
        txtDueDateDocumentIdentification.setRawValue(null);
        txtIdentificationNumberOld.setRawValue(null);
        txtFullName.setRawValue(null);
        txtFullLastName.setRawValue(null);
        txtMarriedLastName.setRawValue(null);
        txtBirthPlace.setRawValue(null);
        txtBirthDay.setRawValue(null);
        txtFamilyResponsibilities.setRawValue(null);
        txtEmail.setRawValue(null);
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

    private void loadFields(ApplicantNaturalPerson applicantNaturalPerson) {
        try {
            String Gender = "M";
            txtIdentificationNumber.setText(applicantNaturalPerson.getIdentificationNumber());
            txtDueDateDocumentIdentification.setValue(applicantNaturalPerson.getDueDateDocumentIdentification());
            txtIdentificationNumberOld.setText(applicantNaturalPerson.getIdentificationNumberOld());
            txtFullName.setText(applicantNaturalPerson.getFirstNames());
            txtFullLastName.setText(applicantNaturalPerson.getLastNames());
            if (txtMarriedLastName != null) {
                txtMarriedLastName.setText(applicantNaturalPerson.getMarriedLastName());
            }
            if (applicantNaturalPerson.getPlaceBirth() != null) {
                txtBirthPlace.setText(applicantNaturalPerson.getPlaceBirth());
            }
            txtBirthDay.setValue(applicantNaturalPerson.getDateBirth());
            if (applicantNaturalPerson.getFamilyResponsibilities() != null) {
                txtFamilyResponsibilities.setText(applicantNaturalPerson.getFamilyResponsibilities().toString());
            }
            if (applicantNaturalPerson.getPersonId().getEmail() != null) {
                txtEmail.setText(applicantNaturalPerson.getPersonId().getEmail());
            }
            if (applicantNaturalPerson.getGender().equals(Gender)) {
                genderMale.setChecked(true);
            } else {
                genderFemale.setChecked(true);
            }
            
            if(applicantNaturalPerson.getObservations() != null){
                txtObservations.setText(applicantNaturalPerson.getObservations());
            }
            
            EJBRequest request = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.PERSON_KEY, applicantNaturalPerson.getPersonId().getId());
            request.setParams(params);
            phonePersonList = personEJB.getPhoneByPerson(request);
            if (phonePersonList != null) {
                for (PhonePerson p : phonePersonList) {
                    if (p.getPhoneTypeId().getId() == Constants.PHONE_TYPE_ROOM) {
                        txtPhoneCelL.setText(p.getNumberPhone());
                        txtCodeCountryPhoneL.setValue(p.getCountryCode());
                        txtAreaCodePhoneL.setText(p.getAreaCode());
                        if (p.getIndMainPhone() == true) {
                                rIsPrincipalNumberLYes.setChecked(true);
                        } else {
                                rIsPrincipalNumerLNo.setChecked(true);
                        }
                        localPhone = p;
                    }
                    if (p.getPhoneTypeId().getId() == Constants.PHONE_TYPE_MOBILE) {                         
                         txtPhoneCel.setText(p.getNumberPhone());
                         txtCodeCountryPhone.setValue(p.getCountryCode());
                         txtAreaCodePhone.setText(p.getAreaCode());
                         if (p.getIndMainPhone() != null) {
                            if (p.getIndMainPhone() == true) {
                                 rIsPrincipalNumberYes.setChecked(true);
                             } else {
                                 rIsPrincipalNumerNo.setChecked(true);
                             }
                         }                         
                         cellPhone = p;
                    }
                }
            }            
            applicantNaturalPersonParent = applicantNaturalPerson;
            if (eventType == WebConstants.EVENT_ADD) {
                btnSave.setVisible(false);
            } else {
                btnSave.setVisible(true);
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loadFieldsRequest(Request requestData) {
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

    public void blockFields() {
        txtIdentificationNumber.setReadonly(true);
        txtDueDateDocumentIdentification.setReadonly(true);
        txtIdentificationNumberOld.setReadonly(true);
        txtFullName.setReadonly(true);
        txtFullLastName.setReadonly(true);
        txtMarriedLastName.setReadonly(true);
        txtBirthPlace.setReadonly(true);
        txtBirthDay.setReadonly(true);
        txtFamilyResponsibilities.setReadonly(true);
        txtEmail.setReadonly(true);
        txtAreaCodePhoneL.setReadonly(true);
        txtPhoneCelL.setReadonly(true);        
        txtAreaCodePhone.setReadonly(true);
        txtPhoneCel.setReadonly(true);        
        genderFemale.setDisabled(true);
        genderMale.setDisabled(true);
        cmbCountryPhoneL.setDisabled(true);
        cmbCountryPhone.setDisabled(true);
        cmbCivilState.setReadonly(true);
        cmbProfession.setReadonly(true);
        rIsPrincipalNumberLYes.setDisabled(true);
        rIsPrincipalNumerLNo.setDisabled(true);
        rIsPrincipalNumberYes.setDisabled(true);
        rIsPrincipalNumerNo.setDisabled(true);
        txtObservations.setDisabled(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {     
        Calendar today = Calendar.getInstance();
        today.add(Calendar.YEAR, -18);
        Calendar cumpleCalendar = Calendar.getInstance();
        if (!(txtBirthDay.getText().isEmpty())) {
            cumpleCalendar.setTime(((Datebox) txtBirthDay).getValue());  
        }
        
         if (cmbDocumentsPersonType.getSelectedItem() == null) {
            cmbDocumentsPersonType.setFocus(true);
            this.showMessage("cms.error.documentType.notSelected", true, null);
        } else if (txtIdentificationNumber.getText().isEmpty()) {
            txtIdentificationNumber.setFocus(true);
            this.showMessage("cms.error.field.identificationNumber", true, null);
        } else if (txtDueDateDocumentIdentification.getText().isEmpty()) {
            txtDueDateDocumentIdentification.setFocus(true);
            this.showMessage("cms.error.field.dueDateDocumentIdentification", true, null);
        } else if (txtFullName.getText().isEmpty()) {
            txtFullName.setFocus(true);
            this.showMessage("cms.error.field.fullName", true, null);
        } else if (txtFullLastName.getText().isEmpty()) {
            txtFullLastName.setFocus(true);
            this.showMessage("cms.error.field.lastName", true, null);
        } else if (txtBirthPlace.getText().isEmpty()) {
            txtBirthPlace.setFocus(true);
            this.showMessage("cms.error.field.txtBirthPlace", true, null);
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
        } else if (txtFamilyResponsibilities.getText().isEmpty()) {
            txtFamilyResponsibilities.setFocus(true);
            this.showMessage("cms.error.familyResponsibilities", true, null);
        } else if (txtEmail.getText().isEmpty()) {
            txtEmail.setFocus(true);
            this.showMessage("cms.error.field.email", true, null);
        } else if (cmbCountryPhoneL.getSelectedItem()  == null) {
            cmbCountryPhoneL.setFocus(true);
            this.showMessage("cms.error.country.notSelected.PhoneLocal", true, null);     
        }  else if (txtAreaCodePhoneL.getText().isEmpty()) {
            txtAreaCodePhoneL.setFocus(true);
            this.showMessage("cms.error.employee.areaCode.PhoneLocal", true, null);
        } else if (txtPhoneCelL.getText().isEmpty()) {
            txtPhoneCelL.setFocus(true);
            this.showMessage("cms.error.field.phoneLocalNumber", true, null);
        } else if ((!rIsPrincipalNumberLYes.isChecked()) && (!rIsPrincipalNumerLNo.isChecked())){
            this.showMessage("cms.error.employee.phoneLocalMain", true, null);
        } else if (cmbCountryPhone.getSelectedItem()  == null) {
            cmbCountryPhone.setFocus(true);
            this.showMessage("cms.error.country.notSelected.CellPhone", true, null);     
        } else if (txtAreaCodePhone.getText().isEmpty()) {
            txtAreaCodePhone.setFocus(true);
            this.showMessage("cms.error.employee.areaCode.CellPhone", true, null);
        } else if (txtPhoneCel.getText().isEmpty()) {
            txtPhoneCel.setFocus(true);
            this.showMessage("cms.error.field.cellPhoneNumber", true, null);
        } else if ((!rIsPrincipalNumberYes.isChecked()) && (!rIsPrincipalNumerNo.isChecked())){
            this.showMessage("cms.error.employee.cellPhoneMain", true, null);
        } else {
            return true;
        }
        return false;
    }
    
    public Boolean validateNaturalApplicant() {
        if (rIsPrincipalNumberLYes.isChecked() && rIsPrincipalNumberYes.isChecked()) {
            this.showMessage("cms.error.principalPhoneError", true, null);
            return false;
        } else if(rIsPrincipalNumerLNo.isChecked() && rIsPrincipalNumerNo.isChecked()){
            this.showMessage("cms.error.noPrincipalPhoneError", true, null);
            return false;
        }
        return true;
    }

    private void saveNaturalPerson(ApplicantNaturalPerson _applicantNaturalPerson) {
        ApplicantNaturalPerson applicantNaturalPerson = null;
        AdminRequestController adminRequest = new AdminRequestController();
        boolean indPrincipalPhoneL  = true;
        boolean indPrincipalPhone  = true;
        PhonePerson phonePerson1 = null;
        PhonePerson phonePerson2 = null;
        PhoneType phonePersonH = null;
        PhoneType phonePersonC = null;
        Person person = null;
        try {
            
            //Obtiene los tipos de telefonos
            EJBRequest request4 = new EJBRequest();
            request4.setParam(Constants.PHONE_TYPE_ROOM);
            phonePersonH = personEJB.loadPhoneType(request4);

            request4 = new EJBRequest();
            request4.setParam(Constants.PHONE_TYPE_MOBILE);
            phonePersonC = personEJB.loadPhoneType(request4);

            if (_applicantNaturalPerson != null) {
                applicantNaturalPerson = _applicantNaturalPerson;
                person = applicantNaturalPerson.getPersonId();
            } else {
                applicantNaturalPerson = new ApplicantNaturalPerson();
                person = new Person();
            }

            if (genderFemale.isChecked()) {
                indGender = "F";
            } else {
                indGender = "M";
            }
            
            if (rIsPrincipalNumberLYes.isChecked()) {
                indPrincipalPhoneL = true;
            } else {
                indPrincipalPhoneL = false;
            }
            
            if (rIsPrincipalNumberYes.isChecked()) {
                indPrincipalPhone = true;
            } else {
                indPrincipalPhone = false;
            }

            //Obtener la clasificacion del solicitante
            EJBRequest request1 = new EJBRequest();
            request1.setParam(Constants.CLASSIFICATION_PERSON_APPLICANT);
            PersonClassification personClassification = utilsEJB.loadPersonClassification(request1);

            //Obtener el estatus ACTIVO del solicitante
            EJBRequest request = new EJBRequest();
            request.setParam(Constants.STATUS_APPLICANT_ACTIVE);
            StatusApplicant statusApplicant = requestEJB.loadStatusApplicant(request);

            //Guardar la persona           
            person.setCountryId(requestCountry);
            person.setEmail(txtEmail.getText());
            if (adminRequest.getRequest().getPersonId() != null) {
                person.setCreateDate(new Timestamp(new Date().getTime()));
            } else {
                person.setPersonTypeId(adminRequest.getRequest().getPersonTypeId());
                person.setPersonClassificationId(personClassification);
                person.setUpdateDate(new Timestamp(new Date().getTime()));
            }
            person = personEJB.savePerson(person);
            applicant = person;

            //Guarda el solicitante principal en la BD
            applicantNaturalPerson.setPersonId(person);
            applicantNaturalPerson.setIdentificationNumber(txtIdentificationNumber.getText());
            applicantNaturalPerson.setDueDateDocumentIdentification(txtDueDateDocumentIdentification.getValue());
            applicantNaturalPerson.setIdentificationNumberOld(txtIdentificationNumberOld.getText());
            applicantNaturalPerson.setFirstNames(txtFullName.getText());
            applicantNaturalPerson.setLastNames(txtFullLastName.getText());
            applicantNaturalPerson.setMarriedLastName(txtMarriedLastName.getText());
            applicantNaturalPerson.setGender(indGender);
            applicantNaturalPerson.setPlaceBirth(txtBirthPlace.getText());
            applicantNaturalPerson.setDateBirth(txtBirthDay.getValue());
            applicantNaturalPerson.setFamilyResponsibilities(txtFamilyResponsibilities.getValue());
            applicantNaturalPerson.setCivilStatusId((CivilStatus) cmbCivilState.getSelectedItem().getValue());
            if(txtObservations.getValue() != null){
                applicantNaturalPerson.setObservations(txtObservations.getText());
            }
            if(cmbProfession.getSelectedItem() != null){
              applicantNaturalPerson.setProfessionId((Profession) cmbProfession.getSelectedItem().getValue());  
            }
            if (eventType == WebConstants.EVENT_ADD) {
                applicantNaturalPerson.setCreateDate(new Timestamp(new Date().getTime()));
            } else {
                applicantNaturalPerson.setUpdateDate(new Timestamp(new Date().getTime()));
            }
            applicantNaturalPerson.setDocumentsPersonTypeId((DocumentsPersonType) cmbDocumentsPersonType.getSelectedItem().getValue());
            applicantNaturalPerson.setStatusApplicantId(statusApplicant);
            applicantNaturalPerson = personEJB.saveApplicantNaturalPerson(applicantNaturalPerson);
            applicantNaturalPersonParent = applicantNaturalPerson;

            //Actualiza el Solicitante en la Solicitud de Tarjeta
            if (adminRequest.getRequest() != null) {
                Request requestCard = adminRequest.getRequest();
                requestCard.setPersonId(person);
                requestEJB.saveRequest(requestCard);
            }
            
            //Se obtienen los Teléfonos del solicitante
            if (eventType != 1) {
                request = new EJBRequest();
                Map params = new HashMap();
                params.put(Constants.PERSON_KEY, person.getId());
                request.setParams(params);
                phonePersonList = personEJB.getPhoneByPerson(request);

                if (phonePersonList != null) {
                    for (PhonePerson p : phonePersonList) {
                        if (p.getPhoneTypeId().getId() == Constants.PHONE_TYPE_ROOM) {
                            phonePerson1 = p;
                        }
                        if (p.getPhoneTypeId().getId() == Constants.PHONE_TYPE_MOBILE) {
                            phonePerson2 = p;
                        }
                    }
                
                    //Telefono Local         
                    phonePerson1.setPersonId(person);
                    phonePerson1.setCountryId((Country) cmbCountryPhoneL.getSelectedItem().getValue());
                    phonePerson1.setCountryCode(txtCodeCountryPhoneL.getValue());
                    phonePerson1.setAreaCode(txtAreaCodePhoneL.getText());
                    phonePerson1.setNumberPhone(txtPhoneCelL.getText());
                    phonePerson1.setPhoneTypeId(phonePersonH);
                    phonePerson1.setIndMainPhone(indPrincipalPhoneL);
                    phonePerson1 = personEJB.savePhonePerson(phonePerson1);
                    
                    //Telefono Celular
                    phonePerson2.setPersonId(person);
                    phonePerson2.setCountryId((Country) cmbCountryPhone.getSelectedItem().getValue());
                    phonePerson2.setCountryCode(txtCodeCountryPhone.getValue());
                    phonePerson2.setAreaCode(txtAreaCodePhone.getText());
                    phonePerson2.setNumberPhone(txtPhoneCel.getText());
                    phonePerson2.setPhoneTypeId(phonePersonC);
                    phonePerson2.setIndMainPhone(indPrincipalPhone);
                    phonePerson2 = personEJB.savePhonePerson(phonePerson2);
                }
            }
            
            this.showMessage("sp.common.save.success", false, null);
            if (eventType == WebConstants.EVENT_ADD) {
                btnSave.setVisible(false);
            } else {
                btnSave.setVisible(true);
            }
            tabAddress.setDisabled(false);
            tabFamilyReferencesMain.setDisabled(false);
            tabAdditionalCards.setDisabled(false);

        } catch (Exception ex) {
            showError(ex);
        } finally {
            try {
                if (phonePersonList == null) {
                    //Guarda el Telefono de Habitacion
                    phonePerson1 = new PhonePerson();
                    phonePerson1.setPersonId(person);
                    phonePerson1.setCountryId((Country) cmbCountryPhoneL.getSelectedItem().getValue());
                    phonePerson1.setCountryCode(txtCodeCountryPhoneL.getValue());
                    phonePerson1.setAreaCode(txtAreaCodePhoneL.getText());
                    phonePerson1.setPhoneTypeId(phonePersonH);
                    phonePerson1.setNumberPhone(txtPhoneCelL.getText());
                    phonePerson1.setIndMainPhone(indPrincipalPhoneL);
                    phonePerson1 = personEJB.savePhonePerson(phonePerson1);
                    //Guarda el Telefono Celular
                    phonePerson2 = new PhonePerson();
                    phonePerson2.setPersonId(person);
                    phonePerson2.setCountryId((Country) cmbCountryPhone.getSelectedItem().getValue());
                    phonePerson2.setCountryCode(txtCodeCountryPhone.getValue());
                    phonePerson2.setAreaCode(txtAreaCodePhone.getText());
                    phonePerson2.setPhoneTypeId(phonePersonC);
                    phonePerson2.setNumberPhone(txtPhoneCel.getText());
                    phonePerson2.setIndMainPhone(indPrincipalPhone);
                    phonePerson2 = personEJB.savePhonePerson(phonePerson2);
                    this.showMessage("sp.common.save.success", false, null);
                    tabAddress.setDisabled(false);
                    tabFamilyReferencesMain.setDisabled(false);
                    tabAdditionalCards.setDisabled(false);
                }
            } catch (RegisterNotFoundException ex) {
                showError(ex);
            } catch (NullParameterException ex) {
                showError(ex);
            } catch (GeneralException ex) {
                showError(ex);
            }
        }
    }

    public void onClick$btnSave() {
        if (validateEmpty() && validateNaturalApplicant()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveNaturalPerson(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveNaturalPerson(applicantNaturalPersonParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFieldR(adminRequest.getRequest());
                if (applicantNaturalPersonParam != null) {
                    applicantNaturalPersonParent = applicantNaturalPersonParam;
                    applicant = applicantNaturalPersonParam.getPersonId();
                    loadFieldsRequest(adminRequest.getRequest());
                    loadFields(applicantNaturalPersonParam);
                }
                loadCmbCountry(eventType);
                loadCmbCivilState(eventType);
                if (adminRequest.getRequest().getPersonTypeId().getOriginApplicationId().getId() == Constants.ORIGIN_APPLICATION_CMS_ID) {
                    loadCmbProfession(eventType);
                }                
                break;
            case WebConstants.EVENT_VIEW:
                loadFieldR(adminRequest.getRequest());                
                if (applicantNaturalPersonParam != null) {
                    applicantNaturalPersonParent = applicantNaturalPersonParam;
                    applicant = applicantNaturalPersonParam.getPersonId();
                    loadFieldsRequest(adminRequest.getRequest());
                    loadFields(applicantNaturalPersonParam);
                    blockFields();
                }
                loadCmbCountry(eventType);
                loadCmbCivilState(eventType);
                if (adminRequest.getRequest().getPersonTypeId().getOriginApplicationId().getId() == Constants.ORIGIN_APPLICATION_CMS_ID) {
                    loadCmbProfession(eventType);
                } 
                break;
            case WebConstants.EVENT_ADD:
                loadFieldR(adminRequest.getRequest());
                applicantNaturalPersonParent = null;
                loadFieldsRequest(adminRequest.getRequest());
                loadCmbCountry(eventType);
                loadCmbCivilState(eventType);
                loadCmbProfession(eventType);
                onChange$cmbCountryPhone();
                onChange$cmbCountryPhoneL();
                break;
            default:
                break;
        }
    }
    
    public void onChange$cmbCountryPhoneL() {
        this.clearMessage();        
        txtCodeCountryPhoneL.setVisible(true);
        txtCodeCountryPhoneL.setValue("");        
        Country country = (Country) cmbCountryPhoneL.getSelectedItem().getValue();
        txtCodeCountryPhoneL.setValue(country.getCode());
    }
    
    public void onChange$cmbCountryPhone() {
        this.clearMessage();        
        txtCodeCountryPhone.setVisible(true);
        txtCodeCountryPhone.setValue("");        
        Country country = (Country) cmbCountryPhone.getSelectedItem().getValue();
        txtCodeCountryPhone.setValue(country.getCode());
    }

    private void loadCmbCountry(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<Country> countries;
        try {
            countries = utilsEJB.getCountries(request1);
            if ((applicantNaturalPersonParam == null) || (phonePersonList.size() == 0)){
                loadGenericCombobox(countries, cmbCountryPhoneL, "name", evenInteger, Long.valueOf(0));
                loadGenericCombobox(countries, cmbCountryPhone, "name", evenInteger, Long.valueOf(0));
            } else {
                if (localPhone != null) {
                    loadGenericCombobox(countries, cmbCountryPhoneL, "name", evenInteger, Long.valueOf(localPhone.getCountryId() != null ? localPhone.getCountryId().getId() : 0)); 
                }
                if (cellPhone != null) {
                    loadGenericCombobox(countries, cmbCountryPhone, "name", evenInteger, Long.valueOf(cellPhone.getCountryId() != null ? cellPhone.getCountryId().getId() : 0)); 
                }                
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

    private void loadCmbDocumentsPersonType(Integer evenInteger, int countryId) {
        EJBRequest request1 = new EJBRequest();
        cmbDocumentsPersonType.getItems().clear();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_COUNTRY_ID, countryId);
        params.put(QueryConstants.PARAM_IND_NATURAL_PERSON, adminRequest.getRequest().getPersonTypeId().getIndNaturalPerson());
        if (eventType == WebConstants.EVENT_ADD) {
            params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, Constants.ORIGIN_APPLICATION_CMS_ID);
        } else {
            params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, adminRequest.getRequest().getPersonTypeId().getOriginApplicationId().getId());
        }
        request1.setParams(params);
        List<DocumentsPersonType> documentsPersonType = null;
        try {
            documentsPersonType = utilsEJB.getDocumentsPersonByCountry(request1);
            loadGenericCombobox(documentsPersonType, cmbDocumentsPersonType, "description", evenInteger, Long.valueOf(applicantNaturalPersonParam != null ? applicantNaturalPersonParam.getDocumentsPersonTypeId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (GeneralException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (NullParameterException ex) {
            showError(ex);
        } finally {
            if (documentsPersonType == null) {
                this.showMessage("cms.msj.DocumentsPersonTypeNull", false, null);
            }
        }
    }

    private void loadCmbCivilState(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<CivilStatus> civilStatuses;

        try {
            civilStatuses = personEJB.getCivilStatus(request1);
            loadGenericCombobox(civilStatuses, cmbCivilState, "description", evenInteger, Long.valueOf(applicantNaturalPersonParam != null ? applicantNaturalPersonParam.getCivilStatusId().getId() : 0));
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

    private void loadCmbProfession(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<Profession> profession;
        try {
            profession = personEJB.getProfession(request1);
            if ((applicantNaturalPersonParam == null) || (applicantNaturalPersonParam.getProfessionId() == null)) {
                loadGenericCombobox(profession, cmbProfession, "name", evenInteger, Long.valueOf(0));
            } else {
                loadGenericCombobox(profession, cmbProfession, "name", evenInteger, Long.valueOf(applicantNaturalPersonParam != null ? applicantNaturalPersonParam.getProfessionId().getId() : 0));
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
