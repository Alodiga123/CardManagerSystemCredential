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
import com.cms.commons.models.LegalCustomer;
import com.cms.commons.models.Person;
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
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Textbox;

public class AdminLegalPersonCustomerController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtIdentificationNumber;
    private Textbox txtTradeName;
    private Textbox txtEnterpriseName;
    private Textbox txtRegistryNumber;
    private Doublebox txtPaidInCapital;
    private Textbox txtPersonId;
    private Textbox txtWebSite;
    private Textbox txtEmail;
    private Combobox cmbCountry;
    private Combobox cmbDocumentsPersonType;
    private Combobox cmbEconomicActivity;
    private Datebox txtDateInscriptionRegister;
    private UtilsEJB utilsEJB = null;
    private PersonEJB personEJB = null;
    private RequestEJB requestEJB = null;
    public static LegalCustomer legalCustomerParam = null;
    private Person person;
    private Button btnSave;
    public static Integer eventType;
    private int indPersonTypeCustomer = 2;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            legalCustomerParam = null;
        } else {
            legalCustomerParam = (LegalCustomer) Sessions.getCurrent().getAttribute("object");
            Sessions.getCurrent().setAttribute(WebConstants.IND_PERSON_TYPE_CUSTOMER, indPersonTypeCustomer);
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public LegalCustomer getLegalCustomer() {
        return legalCustomerParam;
    }
    
    public Integer getEventType() {
        return this.eventType;
    }

    public void onChange$cmbCountry() {
        cmbDocumentsPersonType.setVisible(true);
        Country country = (Country) cmbCountry.getSelectedItem().getValue();
        loadCmbDocumentsPersonType(eventType, country.getId());
    }

    public void clearFields() {
        txtPersonId.setRawValue(null);
        txtTradeName.setRawValue(null);
        txtEnterpriseName.setRawValue(null);
        txtDateInscriptionRegister.setRawValue(null);
        txtRegistryNumber.setRawValue(null);
        txtPaidInCapital.setRawValue(null);
        txtWebSite.setRawValue(null);
        txtEmail.setRawValue(null);
        txtIdentificationNumber.setRawValue(null);
    }

    private void loadFields(LegalCustomer legalCustomer) {
        try {
            txtPersonId.setText(legalCustomer.getPersonId().toString());
            txtTradeName.setText(legalCustomer.getTradeName());
            txtEnterpriseName.setText(legalCustomer.getEnterpriseName());
            txtDateInscriptionRegister.setValue(legalCustomer.getDateInscriptionRegister());
            txtRegistryNumber.setText(legalCustomer.getRegisterNumber());
            txtPaidInCapital.setValue(legalCustomer.getPayedCapital());
            txtWebSite.setValue(legalCustomer.getWebSite());
            if (legalCustomer.getPersonId().getEmail() != null) {
                if (legalCustomer.getPersonId().getEmail().contains("@")) {
                    txtEmail.setValue(legalCustomer.getPersonId().getEmail());
                }
            }
            txtIdentificationNumber.setText(legalCustomer.getIdentificationNumber());
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtTradeName.setReadonly(true);
        txtEnterpriseName.setReadonly(true);
        txtDateInscriptionRegister.setDisabled(true);
        txtRegistryNumber.setReadonly(true);
        txtPaidInCapital.setReadonly(true);
        txtWebSite.setReadonly(true);
        txtEmail.setReadonly(true);
        txtIdentificationNumber.setReadonly(true);
        cmbCountry.setDisabled(true);
        cmbDocumentsPersonType.setDisabled(true);
        cmbEconomicActivity.setDisabled(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtIdentificationNumber.getText().isEmpty()) {
            txtIdentificationNumber.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtEnterpriseName.getText().isEmpty()) {
            txtEnterpriseName.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtRegistryNumber.getText().isEmpty()) {
            txtRegistryNumber.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtPaidInCapital.getText().isEmpty()) {
            txtPaidInCapital.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else {
            return true;
        }
        return false;

    }

    public void onClick$btnCodes() {
        Executions.getCurrent().sendRedirect("/docs/T-SP-E.164D-2009-PDF-S.pdf", "_blank");
    }

    private void saveLegalPerson(LegalCustomer _legalCustomer) {
        try {
            LegalCustomer legalCustomer = null;
            Person person = null;

            if (_legalCustomer != null) {
                legalCustomer = _legalCustomer;
                person = legalCustomer.getPersonId();
            } else {//New LegalPerson
                legalCustomer = new LegalCustomer();
                person = new Person();
            }
            
            String id = cmbCountry.getSelectedItem().getParent().getId();
            person.setCountryId((Country) cmbCountry.getSelectedItem().getValue());
            person.setEmail(txtEmail.getText());
            person.setUpdateDate(new Timestamp(new Date().getTime()));
            person = personEJB.savePerson(person);

            //Guarda los cambios en el Cliente Jurídico
            legalCustomer.setPersonId(person);
            legalCustomer.setTradeName(txtTradeName.getText());
            legalCustomer.setEnterpriseName(txtEnterpriseName.getText());
            legalCustomer.setDateInscriptionRegister(new Timestamp(txtDateInscriptionRegister.getValue().getTime()));
            legalCustomer.setRegisterNumber(txtRegistryNumber.getText());
            legalCustomer.setPayedCapital(txtPaidInCapital.getValue().floatValue());
            legalCustomer.setWebSite(txtWebSite.getText());
            legalCustomer.setEconomicActivityId((EconomicActivity) cmbEconomicActivity.getSelectedItem().getValue());
            legalCustomer.setDocumentsPersonTypeId((DocumentsPersonType) cmbDocumentsPersonType.getSelectedItem().getValue());
            legalCustomer.setIdentificationNumber(txtIdentificationNumber.getText());
            legalCustomer.setUpdateDate(new Timestamp(new Date().getTime()));
            legalCustomer = personEJB.saveLegalCustomer(legalCustomer);
            legalCustomerParam = legalCustomer;
            this.showMessage("sp.common.save.success", false, null);
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
                    saveLegalPerson(legalCustomerParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(legalCustomerParam);
                loadCmbCountry(eventType);
                loadCmbEconomicActivity(eventType);
                onChange$cmbCountry();
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(legalCustomerParam);
                blockFields();
                loadCmbCountry(eventType);
                loadCmbEconomicActivity(eventType);
                onChange$cmbCountry();
                break;
            case WebConstants.EVENT_ADD:
                loadCmbCountry(eventType);
                loadCmbEconomicActivity(eventType);
                onChange$cmbCountry();
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
            loadGenericCombobox(countries, cmbCountry, "name", evenInteger, Long.valueOf(legalCustomerParam != null ? legalCustomerParam.getPersonId().getCountryId().getId() : 0));
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
        params.put(QueryConstants.PARAM_IND_NATURAL_PERSON, legalCustomerParam.getDocumentsPersonTypeId().getPersonTypeId().getIndNaturalPerson());
        if (eventType == WebConstants.EVENT_ADD) {
            params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, Constants.ORIGIN_APPLICATION_CMS_ID);
        } else {
            params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, legalCustomerParam.getDocumentsPersonTypeId().getPersonTypeId().getOriginApplicationId().getId());
        }
        request1.setParams(params);
        List<DocumentsPersonType> documentsPersonType = null;
        try {
            documentsPersonType = utilsEJB.getDocumentsPersonByCountry(request1);
            loadGenericCombobox(documentsPersonType, cmbDocumentsPersonType, "description", evenInteger, Long.valueOf(legalCustomerParam != null ? legalCustomerParam.getDocumentsPersonTypeId().getId() : 0));
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
            loadGenericCombobox(economicActivity, cmbEconomicActivity, "description", evenInteger, Long.valueOf(legalCustomerParam != null ? legalCustomerParam.getEconomicActivityId().getId() : 0));
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
