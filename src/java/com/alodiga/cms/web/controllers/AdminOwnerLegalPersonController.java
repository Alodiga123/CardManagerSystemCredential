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
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;

public class AdminOwnerLegalPersonController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtIdentificationNumber;
    private Textbox txtTradeName;
    private Textbox txtEnterpriseName;
    private Textbox txtRegistryNumber;
    private Doublebox dbxPaidInCapital;
    private Textbox txtPersonId;
    private Textbox txtWebSite;
    private Textbox txtEmail;
    private Textbox txtPhoneNumber;
    private Combobox cmbCountry;
    private Combobox cmbDocumentsPersonType;
    private Combobox cmbEconomicActivity;
    private Datebox txtDateInscriptionRegister;
    private UtilsEJB utilsEJB = null;
    private PersonEJB personEJB = null;
    private RequestEJB requestEJB = null;
    public LegalPerson legalOwnerParam = null;
    private Person person;
    private Button btnSave;
    private Toolbarbutton tbbTitle;
    private Integer eventType;
    private Integer indSelect = 2;
    public static LegalPerson ownerlegalPerson = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        Sessions.getCurrent().setAttribute(WebConstants.IND_OWNER_PROGRAM_SELECT, indSelect);
        if (eventType == WebConstants.EVENT_ADD) {
            legalOwnerParam = null;
        } else {
            legalOwnerParam = (LegalPerson) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.programOwner.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.programOwner.view"));
                break;
            case WebConstants.EVENT_ADD:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.programOwner.add"));
                break;
            default:
                break;
        }
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public LegalPerson getLegalPerson() {
        return ownerlegalPerson;
    }

    public void onChange$cmbCountry() {
        this.clearMessage();
        cmbDocumentsPersonType.setVisible(true);
        cmbDocumentsPersonType.setValue("");
        Country country = (Country) cmbCountry.getSelectedItem().getValue();
        loadCmbDocumentsPersonType(eventType, country.getId());
    }

    public void clearFields() {
        txtPersonId.setRawValue(null);
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

    private void loadFields(LegalPerson legalOwner) {
        try {
            txtPersonId.setText(legalOwner.getPersonId().toString());
            txtTradeName.setText(legalOwner.getTradeName());
            txtEnterpriseName.setText(legalOwner.getEnterpriseName());
            txtDateInscriptionRegister.setValue(legalOwner.getDateInscriptionRegister());
            txtRegistryNumber.setText(legalOwner.getRegisterNumber());
            dbxPaidInCapital.setValue(legalOwner.getPayedCapital());
            txtWebSite.setValue(legalOwner.getWebSite());
            if(legalOwner.getEnterprisePhone() != null){
                txtPhoneNumber.setValue(legalOwner.getEnterprisePhone());
            }
            txtEmail.setValue(legalOwner.getPersonId().getEmail());
            txtIdentificationNumber.setText(legalOwner.getIdentificationNumber());

            legalOwnerParam = legalOwner;
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
        txtWebSite.setReadonly(true);
        txtEmail.setReadonly(true);
        txtIdentificationNumber.setReadonly(true);
        txtPhoneNumber.setReadonly(true);
        cmbCountry.setDisabled(true);
        cmbDocumentsPersonType.setDisabled(true);
        cmbEconomicActivity.setDisabled(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        Date today = new Date();

        if (cmbCountry.getSelectedItem() == null) {
            cmbCountry.setFocus(true);
            this.showMessage("cms.error.country.notSelected", true, null);
        } else if (cmbDocumentsPersonType.getSelectedItem() == null) {
            cmbDocumentsPersonType.setFocus(true);
            this.showMessage("cms.error.documentType.notSelected", true, null);
        } else if (txtIdentificationNumber.getText().isEmpty()) {
            txtIdentificationNumber.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtEnterpriseName.getText().isEmpty()) {
            txtEnterpriseName.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (cmbEconomicActivity.getSelectedItem() == null) {
            cmbEconomicActivity.setFocus(true);
            this.showMessage("cms.error.comercialActivity.noSelected", true, null);
        } else if (txtDateInscriptionRegister.getText().isEmpty()) {
            txtDateInscriptionRegister.setFocus(true);
            this.showMessage("cms.error.date.inscriptionRegister", true, null);
        } else if (today.compareTo(txtDateInscriptionRegister.getValue()) < 0) {
            txtDateInscriptionRegister.setFocus(true);
            this.showMessage("cms.error.date.inscriptionRegister.invalid", true, null);
        } else if (txtRegistryNumber.getText().isEmpty()) {
            txtRegistryNumber.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (dbxPaidInCapital.getText().isEmpty()) {
            dbxPaidInCapital.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtPhoneNumber.getText().isEmpty()) {
            txtPhoneNumber.setFocus(true);
            this.showMessage("cms.error.field.phoneNumber", true, null);
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

    private void saveLegalOwner(LegalPerson _legalOwner) {
        try {
            LegalPerson legalOwner = null;
            Person person = null;

            if (_legalOwner != null) {
                legalOwner = _legalOwner;
                person = legalOwner.getPersonId();
            } else {//New LegalPerson
                legalOwner = new LegalPerson();
                person = new Person();
            }

            //PersonClassification
            EJBRequest request1 = new EJBRequest();
            request1.setParam(Constants.CLASSIFICATION_PERSON_OWNER);
            PersonClassification personClassification = utilsEJB.loadPersonClassification(request1);

            //Guardar Person
            String id = cmbCountry.getSelectedItem().getParent().getId();
            person.setCountryId((Country) cmbCountry.getSelectedItem().getValue());
            person.setEmail(txtEmail.getText());
            if (eventType == WebConstants.EVENT_ADD) {
                person.setPersonTypeId(((DocumentsPersonType) cmbDocumentsPersonType.getSelectedItem().getValue()).getPersonTypeId());
                person.setCreateDate(new Timestamp(new Date().getTime()));
                person.setPersonClassificationId(personClassification);
            } else {
                person.setUpdateDate(new Timestamp(new Date().getTime()));
            }
            person = personEJB.savePerson(person);

            //Guarda los cambios en el Propietario Jurídico
            legalOwner.setPersonId(person);
            legalOwner.setTradeName(txtTradeName.getText());
            legalOwner.setEnterpriseName(txtEnterpriseName.getText());
            legalOwner.setDateInscriptionRegister(new Timestamp(txtDateInscriptionRegister.getValue().getTime()));
            legalOwner.setRegisterNumber(txtRegistryNumber.getText());
            legalOwner.setPayedCapital(dbxPaidInCapital.getValue().floatValue());
            legalOwner.setEnterprisePhone(txtPhoneNumber.getText());
            legalOwner.setWebSite(txtWebSite.getText());
            legalOwner.setEconomicActivityId((EconomicActivity) cmbEconomicActivity.getSelectedItem().getValue());
            legalOwner.setDocumentsPersonTypeId((DocumentsPersonType) cmbDocumentsPersonType.getSelectedItem().getValue());
            legalOwner.setIdentificationNumber(txtIdentificationNumber.getText());
            legalOwner = utilsEJB.saveLegalPerson(legalOwner);
            legalOwnerParam = legalOwner;
            ownerlegalPerson = legalOwner;
            
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
                    saveLegalOwner(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveLegalOwner(legalOwnerParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                ownerlegalPerson = legalOwnerParam;
                loadFields(legalOwnerParam);
                loadCmbCountry(eventType);
                onChange$cmbCountry();
                loadCmbEconomicActivity(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                ownerlegalPerson = legalOwnerParam;
                loadFields(legalOwnerParam);
                blockFields();
                loadCmbCountry(eventType);
                loadCmbEconomicActivity(eventType);
                onChange$cmbCountry();
                break;
            case WebConstants.EVENT_ADD:
                loadCmbCountry(eventType);
                loadCmbEconomicActivity(eventType);
                break;
            default:
                break;
        }
    }

    private void loadCmbCountry(Integer evenInteger) {
        //cmbCountry
        EJBRequest request1 = new EJBRequest();
        List<Country> countries;

        try {
            countries = utilsEJB.getCountries(request1);
            loadGenericCombobox(countries, cmbCountry, "name", evenInteger, Long.valueOf(legalOwnerParam != null ? legalOwnerParam.getPersonId().getCountryId().getId() : 0));
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
        params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, Constants.ORIGIN_APPLICATION_CMS_ID);
        params.put(QueryConstants.PARAM_IND_NATURAL_PERSON, WebConstants.IND_LEGAL_PERSON);
        request1.setParams(params);
        List<DocumentsPersonType> documentsPersonType = null;
        try {
            documentsPersonType = utilsEJB.getDocumentsPersonByCountry(request1);
            loadGenericCombobox(documentsPersonType, cmbDocumentsPersonType, "description", evenInteger, Long.valueOf(legalOwnerParam != null ? legalOwnerParam.getDocumentsPersonTypeId().getId() : 0));
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
                this.showMessage("cms.msj.DocumentsPersonTypeLegalPersonNull", false, null);
            }
        }
    }

    private void loadCmbEconomicActivity(Integer evenInteger) {
        //cmbEconomicActivity
        EJBRequest request = new EJBRequest();
        List<EconomicActivity> economicActivity;

        try {
            economicActivity = utilsEJB.getEconomicActivitys(request);
            loadGenericCombobox(economicActivity, cmbEconomicActivity, "description", evenInteger, Long.valueOf(legalOwnerParam != null ? legalOwnerParam.getEconomicActivityId().getId() : 0));
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
