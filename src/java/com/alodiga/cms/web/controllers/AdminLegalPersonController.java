package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.RequestEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Country;
import com.cms.commons.models.DocumentsPersonType;
import com.cms.commons.models.EconomicActivity;
import com.cms.commons.models.LegalPerson;
import com.cms.commons.models.Person;
import com.cms.commons.models.PersonClassification;
import com.cms.commons.models.Request;
import com.cms.commons.models.StatusApplicant;
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
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tab;

public class AdminLegalPersonController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblRequestNumber;
    private Label lblRequestDate;
    private Label lblStatusRequest;
    private Label lblCountry;
    private Textbox txtIdentificationNumber;
    private Textbox txtTradeName;
    private Textbox txtEnterpriseName;
    private Textbox txtPhoneNumber;
    private Textbox txtRegistryNumber;
    private Doublebox dbxPaidInCapital;
    private Textbox txtPersonId;
    private Textbox txtWebSite;
    private Tab tabMain;
    private Tab tabAddress;
    private Tab tabLegalRepresentatives;
    private Tab tabAdditionalCards;
    private Tab tabRequestbyCollection;
    private Textbox txtEmail;
    private Combobox cmbDocumentsPersonType;
    private Combobox cmbEconomicActivity;
    private Datebox txtDateInscriptionRegister;
    private UtilsEJB utilsEJB = null;
    private PersonEJB personEJB = null;
    private RequestEJB requestEJB = null;
    private LegalPerson legalPersonParam;
    private Person person;
    private Button btnSave;
    private Integer eventType;
    private AdminRequestController adminRequest = null;
    public static LegalPerson legalPersonParent = null;
    private List<LegalPerson> legalPersonList = null;
    private Request request = null;
    private Country requestCountry = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
        utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
        adminRequest = new AdminRequestController();
        if (adminRequest.getEventType() != null) {
            eventType = adminRequest.getEventType();
            switch (eventType) {
                case WebConstants.EVENT_EDIT:
                    if (adminRequest.getRequest().getPersonId() != null) {
                        tabAddress.setDisabled(false);
                        tabLegalRepresentatives.setDisabled(false);
                        tabAdditionalCards.setDisabled(false);
                        tabRequestbyCollection.setDisabled(false);
                    } else {
                        tabAddress.setDisabled(true);
                        tabLegalRepresentatives.setDisabled(true);
                        tabAdditionalCards.setDisabled(true);
                        tabRequestbyCollection.setDisabled(true);
                    }
                    break;
                case WebConstants.EVENT_VIEW:
                    if (adminRequest.getRequest().getPersonId() != null) {
                        tabAddress.setDisabled(false);
                        tabLegalRepresentatives.setDisabled(false);
                        tabAdditionalCards.setDisabled(false);
                        tabRequestbyCollection.setDisabled(false);
                    } else {
                        tabAddress.setDisabled(true);
                        tabLegalRepresentatives.setDisabled(true);
                        tabAdditionalCards.setDisabled(true);
                        tabRequestbyCollection.setDisabled(true);
                    }
                    break;
                case WebConstants.EVENT_ADD:
                    tabAddress.setDisabled(true);
                    tabLegalRepresentatives.setDisabled(true);
                    tabAdditionalCards.setDisabled(true);
                    tabRequestbyCollection.setDisabled(true);
                    break;
            }
            if (eventType == WebConstants.EVENT_ADD) {
                legalPersonParam = null;
            } else {
                if (adminRequest.getRequest().getPersonId() != null) {
                    EJBRequest request1 = new EJBRequest();
                    Map params = new HashMap();
                    params.put(Constants.PERSON_KEY, adminRequest.getRequest().getPersonId().getId());
                    request1.setParams(params);
                    legalPersonList = utilsEJB.getLegalPersonByPerson(request1);
                    for (LegalPerson applicantLegaPerson : legalPersonList) {
                        legalPersonParam = applicantLegaPerson;
                    }
                } else {
                    legalPersonParam = null;
                }
                legalPersonList = null;
            }
        }
        initialize();
        getCountryRequest();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            requestEJB = (RequestEJB) EJBServiceLocator.getInstance().get(EjbConstants.REQUEST_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public LegalPerson getLegalPerson() {
        return legalPersonParent;
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
        txtTradeName.setRawValue(null);
        txtEnterpriseName.setRawValue(null);
        txtDateInscriptionRegister.setRawValue(null);
        txtRegistryNumber.setRawValue(null);
        dbxPaidInCapital.setRawValue(null);
        txtPhoneNumber.setRawValue(null);
        txtWebSite.setRawValue(null);
        txtEmail.setRawValue(null);
        txtIdentificationNumber.setRawValue(null);
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

    public void loadFields(LegalPerson legalPerson) {
        try {
            txtIdentificationNumber.setText(legalPerson.getIdentificationNumber());
            txtTradeName.setText(legalPerson.getTradeName());
            txtEnterpriseName.setText(legalPerson.getEnterpriseName());
            txtDateInscriptionRegister.setValue(legalPerson.getDateInscriptionRegister());
            txtRegistryNumber.setText(legalPerson.getRegisterNumber());
            dbxPaidInCapital.setValue(legalPerson.getPayedCapital());
            txtPhoneNumber.setValue(legalPerson.getEnterprisePhone());
            txtWebSite.setValue(legalPerson.getWebSite());
            txtEmail.setValue(legalPerson.getPersonId().getEmail().toString());
            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtTradeName.setReadonly(true);
        txtEnterpriseName.setReadonly(true);
        txtDateInscriptionRegister.setDisabled(true);
        txtRegistryNumber.setReadonly(true);
        dbxPaidInCapital.setReadonly(true);
        txtPhoneNumber.setReadonly(true);
        txtWebSite.setReadonly(true);
        txtEmail.setReadonly(true);
        cmbEconomicActivity.setDisabled(true);
        cmbDocumentsPersonType.setDisabled(true);
        txtIdentificationNumber.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        Date today = new Date();

        if (cmbDocumentsPersonType.getSelectedItem() == null) {
            cmbDocumentsPersonType.setFocus(true);
            this.showMessage("cms.error.documentType.notSelected", true, null);
        } else if (txtIdentificationNumber.getText().isEmpty()) {
            txtIdentificationNumber.setFocus(true);
            this.showMessage("cms.error.field.identificationNumber", true, null);
        } else if (txtEnterpriseName.getText().isEmpty()) {
            txtEnterpriseName.setFocus(true);
            this.showMessage("cms.error.field.enterpriseName", true, null);
        } else if (txtTradeName.getText().isEmpty()) {
            txtTradeName.setFocus(true);
            this.showMessage("cms.error.field.tradeName", true, null);
        } else if (txtPhoneNumber.getText().isEmpty()) {
            txtPhoneNumber.setFocus(true);
            this.showMessage("cms.error.field.phoneNumber", true, null);
        } else if (cmbEconomicActivity.getSelectedItem() == null) {
            cmbEconomicActivity.setFocus(true);
            this.showMessage("cms.error.economicActivity.noSelected", true, null);
        } else if (txtRegistryNumber.getText().isEmpty()) {
            txtRegistryNumber.setFocus(true);
            this.showMessage("cms.error.field.registerNumber", true, null);
        } else if (txtDateInscriptionRegister.getText().isEmpty()) {
            txtDateInscriptionRegister.setFocus(true);
            this.showMessage("cms.error.date.inscriptionRegister", true, null);
        } else if (today.compareTo(txtDateInscriptionRegister.getValue()) <= 0) {
            txtDateInscriptionRegister.setFocus(true);
            this.showMessage("cms.error.date.inscriptionRegister.invalid", true, null);
        } else if (dbxPaidInCapital.getText().isEmpty()) {
            dbxPaidInCapital.setFocus(true);
            this.showMessage("cms.error.field.paidInCapital", true, null);
        } else if (txtWebSite.getText().isEmpty()) {
            txtWebSite.setFocus(true);
            this.showMessage("cms.error.field.website", true, null);
        } else if (txtEmail.getText().isEmpty()) {
            txtEmail.setFocus(true);
            this.showMessage("cms.error.field.email", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveLegalPerson(LegalPerson _legalPerson) {
        try {
            LegalPerson legalPerson = null;
            Person person = null;

            if (_legalPerson != null) {
                legalPerson = _legalPerson;
                person = legalPerson.getPersonId();
            } else {
                legalPerson = new LegalPerson();
                person = new Person();
            }

            //Obtener la clasificacion del solicitante
            EJBRequest request1 = new EJBRequest();
            request1.setParam(Constants.CLASSIFICATION_PERSON_APPLICANT);
            PersonClassification personClassification = utilsEJB.loadPersonClassification(request1);

            //Obtener el estatus ACTIVO del solicitante
            EJBRequest request = new EJBRequest();
            request.setParam(Constants.STATUS_APPLICANT_ACTIVE);
            StatusApplicant statusApplicant = requestEJB.loadStatusApplicant(request);

            //Guardar Person
            person.setCountryId(requestCountry);
            person.setEmail(txtEmail.getText());
            if (adminRequest.getRequest().getPersonId() != null) {
                person.setUpdateDate(new Timestamp(new Date().getTime()));
            } else {
                person.setPersonTypeId(adminRequest.getRequest().getPersonTypeId());
                person.setCreateDate(new Timestamp(new Date().getTime()));
                person.setPersonClassificationId(personClassification);
            }
            person = personEJB.savePerson(person);

            //Guarda el LegalPerson
            legalPerson.setPersonId(person);
            legalPerson.setTradeName(txtTradeName.getText());
            legalPerson.setEnterpriseName(txtEnterpriseName.getText());
            legalPerson.setDateInscriptionRegister(new Timestamp(txtDateInscriptionRegister.getValue().getTime()));
            legalPerson.setRegisterNumber(txtRegistryNumber.getText());
            legalPerson.setPayedCapital(dbxPaidInCapital.getValue().floatValue());
            legalPerson.setEnterprisePhone(txtPhoneNumber.getText());
            legalPerson.setWebSite(txtWebSite.getText());
            legalPerson.setEconomicActivityId((EconomicActivity) cmbEconomicActivity.getSelectedItem().getValue());
            legalPerson.setDocumentsPersonTypeId((DocumentsPersonType) cmbDocumentsPersonType.getSelectedItem().getValue());
            legalPerson.setIdentificationNumber(txtIdentificationNumber.getText());
            legalPerson.setStatusApplicantId(statusApplicant);
            legalPerson = utilsEJB.saveLegalPerson(legalPerson);
            legalPersonParent = legalPerson;

            //Actualizar Solicitante en la Solicitud de Tarjeta
            if (adminRequest.getRequest() != null) {
                Request requestCard = adminRequest.getRequest();
                requestCard.setPersonId(person);
                requestEJB.saveRequest(requestCard);
            }

            this.showMessage("sp.common.save.success", false, null);
            if (eventType == WebConstants.EVENT_ADD) {
                btnSave.setVisible(false);
            } else {
                btnSave.setVisible(true);
            }
            tabAddress.setDisabled(false);
            tabLegalRepresentatives.setDisabled(false);
            tabAdditionalCards.setDisabled(false);
            tabRequestbyCollection.setDisabled(false);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveLegalPerson(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveLegalPerson(legalPersonParam);
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
                if (legalPersonParam != null) {
                    legalPersonParent = legalPersonParam;
                    loadFields(legalPersonParam);
                }
                loadCmbEconomicActivity(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                loadFieldR(adminRequest.getRequest());
                if (legalPersonParam != null) {
                    legalPersonParent = legalPersonParam;
                    loadFields(legalPersonParam);
                    blockFields();
                }
                loadCmbEconomicActivity(eventType);
                break;
            case WebConstants.EVENT_ADD:
                loadFieldR(adminRequest.getRequest());
                legalPersonParent = null;
                loadCmbEconomicActivity(eventType);
                break;
            default:
                break;
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
            loadGenericCombobox(documentsPersonType, cmbDocumentsPersonType, "description", evenInteger, Long.valueOf(legalPersonParam != null ? legalPersonParam.getDocumentsPersonTypeId().getId() : 0));
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

    private void loadCmbEconomicActivity(Integer evenInteger) {
        //cmbEconomicActivity
        EJBRequest request = new EJBRequest();
        List<EconomicActivity> economicActivity;
        try {
            economicActivity = utilsEJB.getEconomicActivitys(request);
            loadGenericCombobox(economicActivity, cmbEconomicActivity, "description", evenInteger, Long.valueOf(legalPersonParam != null ? legalPersonParam.getEconomicActivityId().getId() : 0));
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
