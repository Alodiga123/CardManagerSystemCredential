package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.CivilStatus;
import com.cms.commons.models.Country;
import com.cms.commons.models.DocumentsPersonType;
import com.cms.commons.models.NaturalCustomer;
import com.cms.commons.models.Person;
import com.cms.commons.models.PhonePerson;
import com.cms.commons.models.PhoneType;
import com.cms.commons.models.Profession;
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
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;

public class AdminNaturalPersonCustomerController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtIdentificationNumber;
    private Textbox txtIdentificationNumberOld;
    private Textbox txtCountryStayTime;
    private Textbox txtFullName;
    private Textbox txtFullLastName;
    private Textbox txtMarriedLastName;
    private Textbox txtBirthPlace;
    private Textbox txtPhoneNumber;
    private Intbox txtFamilyResponsibilities;
    private Textbox txtEmail;
    private Combobox cmbCountry;
    private Combobox cmbDocumentsPersonType;
    private Combobox cmbCivilState;
    private Combobox cmbProfession;
    private Combobox cmbPhoneType;
    private Datebox txtBirthDay;
    private Datebox txtDueDateDocumentIdentification;
    private Radio genderMale;
    private Radio genderFemale;
    private Radio naturalizedYes;
    private Radio naturalizedNo;
    private Radio foreignYes;
    private Radio foreignNo;
    public String indGender = null;
    public Boolean indNaturalized = null;
    public Boolean indForeign = null;
    private UtilsEJB utilsEJB = null;
    private PersonEJB personEJB = null;
    private List<PhonePerson> phonePersonList = null;
    private Button btnSave;
    public static Integer eventType;
    public static NaturalCustomer naturalCustomerParam = null;
    private Toolbarbutton tbbTitle;
    private int indPersonTypeCustomer = 1;
    Long countPhoneByPerson = 0L;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
            if (eventType == WebConstants.EVENT_ADD) {
                naturalCustomerParam = null;
            } else {
                naturalCustomerParam = (NaturalCustomer) Sessions.getCurrent().getAttribute("object");
                Sessions.getCurrent().setAttribute(WebConstants.IND_PERSON_TYPE_CUSTOMER, indPersonTypeCustomer);                  
                
                //Se actualiza el teléfono del cliente en el objeto naturalCustomerParam
                if (naturalCustomerParam.getPersonId().getPhonePerson() == null) {
                    countPhoneByPerson = personEJB.havePhonesByPerson(naturalCustomerParam.getPersonId().getId());
                    if (countPhoneByPerson != 0) {
                        EJBRequest request1 = new EJBRequest();
                        Map params = new HashMap();
                        params.put(Constants.PERSON_KEY, naturalCustomerParam.getPersonId().getId());
                        request1.setParams(params);
                        phonePersonList = personEJB.getPhoneByPerson(request1);
                        for (PhonePerson phone : phonePersonList) {
                            naturalCustomerParam.getPersonId().setPhonePerson(phone);
                        }
                    }                    
                }            
            }
        } catch (Exception ex) {
            showError(ex);
        } finally {
            initialize();
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.customer.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.customer.view"));
                break;
            case WebConstants.EVENT_ADD:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.customer.add"));
                break;
            default:
                break;
        }
        try {
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
    
    public Integer getEventType() {
        return this.eventType;
    }

    public NaturalCustomer getNaturalCustomer() {
        return naturalCustomerParam;
    }

    public void onClick$naturalizedYes() {
        txtIdentificationNumberOld.setDisabled(false);
    }

    public void onClick$naturalizedNo() {
        txtIdentificationNumberOld.setDisabled(true);
    }

    public void onClick$foreignYes() {
        txtCountryStayTime.setDisabled(false);
    }

    public void onClick$foreignNo() {
        txtCountryStayTime.setDisabled(true);
    }

    public void clearFields() {
        txtIdentificationNumber.setRawValue(null);
        txtDueDateDocumentIdentification.setRawValue(null);
        txtIdentificationNumberOld.setRawValue(null);
        txtCountryStayTime.setRawValue(null);
        txtFullName.setRawValue(null);
        txtFullLastName.setRawValue(null);
        txtMarriedLastName.setRawValue(null);
        txtBirthPlace.setRawValue(null);
        txtBirthDay.setRawValue(null);
        txtFamilyResponsibilities.setRawValue(null);
    }
    
    public Boolean validateEmpty() {
        if ((!naturalizedYes.isChecked()) && (!naturalizedNo.isChecked())) {
           this.showMessage("cms.crud.customer.validationData.naturalize", true, null);
        } else if((!foreignYes.isChecked()) && (!foreignNo.isChecked())) {
            this.showMessage("cms.crud.customer.validationData.ifForeign", true, null);
        } else {
            return true;
        }
            return false;
    }
            

    private void loadFields(NaturalCustomer naturalCustomer) {
        String Gender = "M";
        try {
            txtIdentificationNumber.setText(naturalCustomer.getIdentificationNumber());
            if (txtDueDateDocumentIdentification != null) {
                txtDueDateDocumentIdentification.setValue(naturalCustomer.getDueDateDocumentIdentification());
            }
            if (naturalCustomer.getIndNaturalized() != null) {
                if (naturalCustomer.getIndNaturalized() == true) {
                    naturalizedYes.setChecked(true);
                    txtIdentificationNumberOld.setDisabled(false);
                } else {
                    naturalizedNo.setChecked(true);
                    txtIdentificationNumberOld.setDisabled(true);
                }
            }
            if (naturalCustomer.getIdentificationNumberOld() != null) {
                txtIdentificationNumberOld.setText(naturalCustomer.getIdentificationNumberOld());
            }
            if (naturalCustomer.getIndForeign() != null) {
                if (naturalCustomer.getIndForeign() == true) {
                    foreignYes.setChecked(true);
                    txtCountryStayTime.setDisabled(false);
                    txtCountryStayTime.setText(naturalCustomer.getCountryStayTime().toString());
                } else {
                    foreignNo.setChecked(true);
                    txtCountryStayTime.setDisabled(true);
                }
            }
            txtFullName.setText(naturalCustomer.getFirstNames());
            txtFullLastName.setText(naturalCustomer.getLastNames());
            if (naturalCustomer.getGender().equals(Gender)) {
                genderMale.setChecked(true);
            } else {
                genderFemale.setChecked(true);
            }
            txtBirthPlace.setText(naturalCustomer.getPlaceBirth());
            txtBirthDay.setValue(naturalCustomer.getDateBirth());
            if (txtCountryStayTime.getText() != "") {
                txtCountryStayTime.setText(naturalCustomer.getCountryStayTime().toString());
            }
            if (naturalCustomer.getMarriedLastName() != null) {
                txtMarriedLastName.setText(naturalCustomer.getMarriedLastName());
            }
            txtEmail.setText(naturalCustomer.getPersonId().getEmail());
            if (naturalCustomer.getFamilyResponsibilities() != null) {
                txtFamilyResponsibilities.setText(naturalCustomer.getFamilyResponsibilities().toString());
            }
            if (naturalCustomer.getPersonId().getPhonePerson() != null) {
                txtPhoneNumber.setText(naturalCustomer.getPersonId().getPhonePerson().getNumberPhone());
            }                               
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtIdentificationNumber.setReadonly(true);
        txtDueDateDocumentIdentification.setDisabled(true);
        naturalizedYes.setDisabled(true);
        naturalizedNo.setDisabled(true);
        txtIdentificationNumberOld.setReadonly(true);
        foreignYes.setDisabled(true);
        foreignNo.setDisabled(true);
        txtCountryStayTime.setReadonly(true);
        txtFullName.setReadonly(true);
        txtFullLastName.setReadonly(true);
        txtMarriedLastName.setReadonly(true);
        genderMale.setDisabled(true);
        genderFemale.setDisabled(true);
        txtBirthPlace.setReadonly(true);
        txtBirthDay.setReadonly(true);
        txtFamilyResponsibilities.setReadonly(true);
        cmbCountry.setReadonly(true);
        cmbCivilState.setReadonly(true);
        cmbProfession.setReadonly(true);
        btnSave.setVisible(false);
    }

    private void saveNaturalPersonCustomer(NaturalCustomer _naturalCustomer) {
        NaturalCustomer naturalCustomer = null;
        PhonePerson phonePerson = null;
        Person person = null;
        try {          

            if (_naturalCustomer != null) {
                naturalCustomer = _naturalCustomer;
                person = naturalCustomer.getPersonId();
                if (naturalCustomer.getPersonId().getPhonePerson() != null) {
                    phonePerson = naturalCustomer.getPersonId().getPhonePerson();
                } else {
                    phonePerson = new PhonePerson();
                }                
            }

            if (genderFemale.isChecked()) {
                indGender = "F";
            } else {
                indGender = "M";
            }

            if (naturalizedYes.isChecked()) {
                indNaturalized = true;
            } else {
                indNaturalized = false;
            }

            if (foreignYes.isChecked()) {
                indForeign = true;
            } else {
                indForeign = false;
            }            
            
            //Se guardan los teléfonos del cliente
            phonePerson.setPersonId(person);
            phonePerson.setPhoneTypeId((PhoneType) cmbPhoneType.getSelectedItem().getValue());
            phonePerson.setNumberPhone(txtPhoneNumber.getText());
            phonePerson = personEJB.savePhonePerson(phonePerson);

            //Se guardan los datos del cliente            
            naturalCustomer.setPersonId(person);
            naturalCustomer.setDocumentsPersonTypeId((DocumentsPersonType) cmbDocumentsPersonType.getSelectedItem().getValue());
            naturalCustomer.setIdentificationNumber(txtIdentificationNumber.getText());
            naturalCustomer.setDueDateDocumentIdentification(txtDueDateDocumentIdentification.getValue());
            naturalCustomer.setIndNaturalized(indNaturalized);
            if (!txtIdentificationNumberOld.getText().equals("")) {
                naturalCustomer.setIdentificationNumberOld(txtIdentificationNumberOld.getText());
            }
            naturalCustomer.setIndForeign(indForeign);
            if (!txtCountryStayTime.getText().equals("")) {
                naturalCustomer.setCountryStayTime(Integer.parseInt(txtCountryStayTime.getText()));
            }
            naturalCustomer.setFirstNames(txtFullName.getText());
            naturalCustomer.setLastNames(txtFullLastName.getText());
            if (!txtMarriedLastName.getText().equals("")) {
                naturalCustomer.setMarriedLastName(txtMarriedLastName.getText());
            }
            naturalCustomer.setGender(indGender);
            naturalCustomer.setPlaceBirth(txtBirthPlace.getText());
            naturalCustomer.setDateBirth(txtBirthDay.getValue());
            naturalCustomer.setCivilStatusId((CivilStatus) cmbCivilState.getSelectedItem().getValue());
            naturalCustomer.setFamilyResponsibilities(Integer.parseInt(txtFamilyResponsibilities.getText()));
            naturalCustomer.setProfessionId((Profession) cmbProfession.getSelectedItem().getValue());
            naturalCustomer.setUpdatedate(new Timestamp(new Date().getTime()));
            naturalCustomer = personEJB.saveNaturalCustomer(naturalCustomer);
            naturalCustomerParam = naturalCustomer;
            this.showMessage("sp.common.save.success", false, null);

        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveNaturalPersonCustomer(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveNaturalPersonCustomer(naturalCustomerParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(naturalCustomerParam);
                loadCmbCountry(eventType);
                onChange$cmbCountry();
                loadCmbCivilState(eventType);
                loadCmbProfession(eventType);
                loadCmbPhoneType(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                blockFields();
                loadFields(naturalCustomerParam);
                loadCmbCountry(eventType);
                onChange$cmbCountry();
                loadCmbCivilState(eventType);
                loadCmbProfession(eventType);
                loadCmbPhoneType(eventType);
                break;
            case WebConstants.EVENT_ADD:
                loadCmbCountry(eventType);
                loadCmbCivilState(eventType);
                loadCmbProfession(eventType);
                loadCmbPhoneType(eventType);
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
            loadGenericCombobox(countries, cmbCountry, "name", evenInteger, Long.valueOf(naturalCustomerParam != null ? naturalCustomerParam.getPersonId().getCountryId().getId() : 0));
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
        params.put(QueryConstants.PARAM_IND_NATURAL_PERSON, naturalCustomerParam.getDocumentsPersonTypeId().getPersonTypeId().getIndNaturalPerson());
        if (eventType == WebConstants.EVENT_ADD) {
            params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, Constants.ORIGIN_APPLICATION_CMS_ID);
        } else {
            params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, naturalCustomerParam.getDocumentsPersonTypeId().getPersonTypeId().getOriginApplicationId().getId());
        }
        request1.setParams(params);
        List<DocumentsPersonType> documentsPersonType = null;
        try {
            documentsPersonType = utilsEJB.getDocumentsPersonByCountry(request1);
            loadGenericCombobox(documentsPersonType, cmbDocumentsPersonType, "description", evenInteger, Long.valueOf(naturalCustomerParam != null ? naturalCustomerParam.getDocumentsPersonTypeId().getId() : 0));
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
            loadGenericCombobox(civilStatuses, cmbCivilState, "description", evenInteger, Long.valueOf(naturalCustomerParam != null ? naturalCustomerParam.getCivilStatusId().getId() : 0));
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
            loadGenericCombobox(profession, cmbProfession, "name", evenInteger, Long.valueOf(naturalCustomerParam != null ? naturalCustomerParam.getProfessionId().getId() : 0));
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
            loadGenericCombobox(phoneType, cmbPhoneType, "description", evenInteger, Long.valueOf(naturalCustomerParam.getPersonId().getPhonePerson() != null ? naturalCustomerParam.getPersonId().getPhonePerson().getPhoneTypeId().getId() : 0));
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
