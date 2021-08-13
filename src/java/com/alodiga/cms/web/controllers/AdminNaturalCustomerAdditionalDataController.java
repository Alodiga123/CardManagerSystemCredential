package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.AdditionalInformationNaturalCustomer;
import com.cms.commons.models.Country;
import com.cms.commons.models.DocumentsPersonType;
import com.cms.commons.models.NaturalCustomer;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Textbox;

public class AdminNaturalCustomerAdditionalDataController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtIdentificationNumber;
    private Textbox txtFirstNames;
    private Textbox txtLastNames;
    private Textbox txtPhone;
    private Textbox txtEmail;
    private Textbox txtCarBrand;
    private Textbox txtCarModel;
    private Intbox txtCarYear;
    private Textbox txtCarPlate;
    private Doublebox txtSalary;
    private Doublebox txtProfession;
    private Doublebox txtBonuses;
    private Doublebox txtRentIncome;
    private Doublebox txtOtherIncome;
    private Doublebox txtTotalIncome;
    private Doublebox txtHousingExpenses;
    private Doublebox txtMonthlyRentMortgage;
    private Doublebox txtMonthlyPaymentCreditCard;
    private Doublebox txtMonthlyPaymentOtherCredit;
    private Doublebox txtEducationExpenses;
    private Doublebox txtTotalExpenses;
    private Float totalIngresos;
    private Float totalEgresos;
    private Combobox cmbCountry;
    private Combobox cmbDocumentsPersonType;
    private UtilsEJB utilsEJB = null;
    private PersonEJB personEJB = null;
    private Button btnSave;
    private Integer eventType;
    private NaturalCustomer naturalCustomer;
    private AdminNaturalPersonCustomerController customer = null;
    public AdditionalInformationNaturalCustomer additionalInformationNaturalCustomerParam;
    private List<AdditionalInformationNaturalCustomer> additionalInformationList;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            customer = new AdminNaturalPersonCustomerController();
            naturalCustomer = customer.getNaturalCustomer();
            Map params = new HashMap();
            EJBRequest request2 = new EJBRequest();
            params.put(Constants.NATURAL_CUSTOMER_KEY, naturalCustomer.getId());
            request2.setParams(params);
            additionalInformationList = personEJB.getAdditionalInformationNaturalCustomeByCustomer(request2);

            switch (eventType) {
                case WebConstants.EVENT_EDIT:
                    if (additionalInformationList != null) {
                        for (AdditionalInformationNaturalCustomer r : additionalInformationList) {
                            additionalInformationNaturalCustomerParam = r;
                        }
                    } else {
                        additionalInformationNaturalCustomerParam = null;
                    }
                    break;
                case WebConstants.EVENT_VIEW:
                    if (additionalInformationList != null) {
                        for (AdditionalInformationNaturalCustomer r : additionalInformationList) {
                            additionalInformationNaturalCustomerParam = r;
                        }
                    } else {
                        additionalInformationNaturalCustomerParam = null;
                    }
                    break;
                case WebConstants.EVENT_ADD:
                    additionalInformationNaturalCustomerParam = null;
                    break;
            }
            loadData();
        } catch (Exception ex) {
            showError(ex);
        } finally {
            if (additionalInformationNaturalCustomerParam == null) {
                additionalInformationNaturalCustomerParam = null;
                loadData();
            }
        }
    }

    public void onChange$cmbCountry() {
        cmbDocumentsPersonType.setVisible(true);
        Country country = (Country) cmbCountry.getSelectedItem().getValue();
        loadCmbDocumentsPersonType(eventType, country.getId());
    }

    public void clearFields() {
        txtFirstNames.setRawValue(null);
        txtLastNames.setRawValue(null);
        txtIdentificationNumber.setRawValue(null);
        txtPhone.setRawValue(null);
        txtEmail.setRawValue(null);
        txtCarBrand.setRawValue(null);
        txtCarModel.setRawValue(null);
        txtCarYear.setRawValue(null);
        txtCarPlate.setRawValue(null);
        txtSalary.setRawValue(null);
        txtProfession.setRawValue(null);
        txtBonuses.setRawValue(null);
        txtRentIncome.setRawValue(null);
        txtOtherIncome.setRawValue(null);
        txtTotalIncome.setRawValue(null);
        txtHousingExpenses.setRawValue(null);
        txtMonthlyRentMortgage.setRawValue(null);
        txtMonthlyPaymentCreditCard.setRawValue(null);
        txtMonthlyPaymentOtherCredit.setRawValue(null);
        txtEducationExpenses.setRawValue(null);
        txtTotalExpenses.setRawValue(null);
    }

    private void loadFields(AdditionalInformationNaturalCustomer additionalInformationNaturalCustomer) {
        try {
            if (additionalInformationNaturalCustomer.getFirstNamesHusband() != null) {
                txtFirstNames.setText(additionalInformationNaturalCustomer.getFirstNamesHusband());
            }
            if (additionalInformationNaturalCustomer.getLastNamesHusband() != null) {
                txtLastNames.setText(additionalInformationNaturalCustomer.getLastNamesHusband());
            }
            if (additionalInformationNaturalCustomer.getIdentificationNumberHusband() != null) {
                txtIdentificationNumber.setText(additionalInformationNaturalCustomer.getIdentificationNumberHusband());
            }
            if (additionalInformationNaturalCustomer.getPhoneHusband() != null) {
                txtPhone.setText(additionalInformationNaturalCustomer.getPhoneHusband());
            }
            if (additionalInformationNaturalCustomer.getEmailHusband() != null) {
                txtEmail.setText(additionalInformationNaturalCustomer.getEmailHusband());
            }
            if (additionalInformationNaturalCustomer.getCarBrand() != null) {
                txtCarBrand.setText(additionalInformationNaturalCustomer.getCarBrand());
            }
            if (additionalInformationNaturalCustomer.getCarModel() != null) {
                txtCarModel.setText(additionalInformationNaturalCustomer.getCarModel());
            }
            if (additionalInformationNaturalCustomer.getCarYear() != null) {
                txtCarYear.setValue(additionalInformationNaturalCustomer.getCarYear());
            }
            if (additionalInformationNaturalCustomer.getCarPlate() != null) {
                txtCarPlate.setText(additionalInformationNaturalCustomer.getCarPlate());
            }
            if (additionalInformationNaturalCustomer.getSalary() != null) {
                txtSalary.setValue(additionalInformationNaturalCustomer.getSalary());
            }
            if (additionalInformationNaturalCustomer.getFreeProfessionalExercise() != null) {
                txtProfession.setValue(additionalInformationNaturalCustomer.getFreeProfessionalExercise());
            }
            if (additionalInformationNaturalCustomer.getBonusesCommissionsFee() != null) {
                txtBonuses.setValue(additionalInformationNaturalCustomer.getBonusesCommissionsFee());
            }
            if (additionalInformationNaturalCustomer.getRentsIncome() != null) {
                txtRentIncome.setValue(additionalInformationNaturalCustomer.getRentsIncome());
            }
            if (additionalInformationNaturalCustomer.getOtherIncome() != null) {
                txtOtherIncome.setValue(additionalInformationNaturalCustomer.getOtherIncome());
            }
            if (additionalInformationNaturalCustomer.getTotalIncome() != null) {
                txtTotalIncome.setValue(additionalInformationNaturalCustomer.getTotalIncome());
            }
            if (additionalInformationNaturalCustomer.getHousingExpenses() != null) {
                txtHousingExpenses.setValue(additionalInformationNaturalCustomer.getHousingExpenses());
            }
            if (additionalInformationNaturalCustomer.getMonthlyRentMortgage() != null) {
                txtMonthlyRentMortgage.setValue(additionalInformationNaturalCustomer.getMonthlyRentMortgage());
            }
            if (additionalInformationNaturalCustomer.getMonthlyPaymentCreditCard() != null) {
                txtMonthlyPaymentCreditCard.setValue(additionalInformationNaturalCustomer.getMonthlyPaymentCreditCard());
            }
            if (additionalInformationNaturalCustomer.getMonthlyPaymentOtherCredit() != null) {
                txtMonthlyPaymentOtherCredit.setValue(additionalInformationNaturalCustomer.getMonthlyPaymentOtherCredit());
            }
            if (additionalInformationNaturalCustomer.getEducationExpenses() != null) {
                txtEducationExpenses.setValue(additionalInformationNaturalCustomer.getEducationExpenses());
            }
            if (additionalInformationNaturalCustomer.getTotalExpenses() != null) {
                txtTotalExpenses.setValue(additionalInformationNaturalCustomer.getTotalExpenses());
            }

            additionalInformationNaturalCustomerParam = additionalInformationNaturalCustomer;
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtFirstNames.setReadonly(true);
        txtLastNames.setReadonly(true);
        txtIdentificationNumber.setReadonly(true);
        txtPhone.setReadonly(true);
        txtEmail.setReadonly(true);
        txtCarBrand.setReadonly(true);
        txtCarModel.setReadonly(true);
        txtCarYear.setReadonly(true);
        txtCarPlate.setReadonly(true);
        txtSalary.setReadonly(true);
        txtProfession.setReadonly(true);
        txtBonuses.setReadonly(true);
        txtRentIncome.setReadonly(true);
        txtOtherIncome.setReadonly(true);
        txtTotalIncome.setReadonly(true);
        txtHousingExpenses.setReadonly(true);
        txtMonthlyRentMortgage.setReadonly(true);
        txtMonthlyPaymentCreditCard.setReadonly(true);
        txtMonthlyPaymentOtherCredit.setReadonly(true);
        txtEducationExpenses.setReadonly(true);
        txtTotalExpenses.setReadonly(true);
        cmbCountry.setReadonly(true);

        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (cmbCountry.getSelectedItem() == null) {
            cmbCountry.setFocus(true);
            this.showMessage("cms.error.country.notSelected", true, null);
        } else if (txtFirstNames.getText() != null) {
            if (cmbDocumentsPersonType.getSelectedItem() == null) {
                cmbDocumentsPersonType.setFocus(true);
                this.showMessage("cms.error.documentType.notSelected", true, null);
            } else if (txtIdentificationNumber.getText().isEmpty()) {
                txtIdentificationNumber.setFocus(true);
                this.showMessage("cms.error.field.identificationNumber", true, null);
            } else if (txtFirstNames.getText().isEmpty()) {
                txtFirstNames.setFocus(true);
                this.showMessage("cms.error.field.fullName", true, null);
            } else if (txtLastNames.getText().isEmpty()) {
                txtLastNames.setFocus(true);
                this.showMessage("cms.error.field.lastName", true, null);
            } else if (txtPhone.getText().isEmpty()) {
                txtPhone.setFocus(true);
                this.showMessage("cms.error.field.phoneNumber", true, null);
            } else if (txtEmail.getText().isEmpty()) {
                txtEmail.setFocus(true);
                this.showMessage("cms.error.field.email", true, null);
            } else {
                return true;
            }
        } else if (txtCarBrand.getText().isEmpty()) {
            txtCarBrand.setFocus(true);
            this.showMessage("cms.error.field.email", true, null);
        } else {
            return true;
        }

        return false;
    }

    private void saveNaturalPersonCustomer(AdditionalInformationNaturalCustomer _addiAdditionalInformationNaturalCustomer) {
        NaturalCustomer naturalCustomer = null;
        totalIngresos = 0.0f;
        totalEgresos = 0.0f;
        Float valueZero = 0.0f;
        AdminNaturalPersonCustomerController adminNaturalCustomer = new AdminNaturalPersonCustomerController();
        try {
            AdditionalInformationNaturalCustomer additionalInformationNaturalCustomer = null;

            if (_addiAdditionalInformationNaturalCustomer != null) {
                additionalInformationNaturalCustomer = _addiAdditionalInformationNaturalCustomer;
            } else {//New ApplicantNaturalPerson
                additionalInformationNaturalCustomer = new AdditionalInformationNaturalCustomer();
            }

            //Cliente
            if (adminNaturalCustomer.getNaturalCustomer() != null) {
                naturalCustomer = adminNaturalCustomer.getNaturalCustomer();
            }

            //additionalInformationNaturalCustomer  
            additionalInformationNaturalCustomer.setNaturalCustomerId(naturalCustomer);
            if (cmbDocumentsPersonType.getSelectedItem() != null) {
                additionalInformationNaturalCustomer.setDocumentsPersonTypeId((DocumentsPersonType) cmbDocumentsPersonType.getSelectedItem().getValue());
            } else {
                additionalInformationNaturalCustomer.setDocumentsPersonTypeId(naturalCustomer.getDocumentsPersonTypeId());
            }
            //Datos del conyuge
            if (!txtFirstNames.getText().isEmpty()) {
                additionalInformationNaturalCustomer.setFirstNamesHusband(txtFirstNames.getText());
                additionalInformationNaturalCustomer.setLastNamesHusband(txtLastNames.getText());
                additionalInformationNaturalCustomer.setIdentificationNumberHusband(txtIdentificationNumber.getText());
                additionalInformationNaturalCustomer.setPhoneHusband(txtPhone.getText());
                additionalInformationNaturalCustomer.setEmailHusband(txtEmail.getText());
            }
            //Datos financieros
            if (!txtCarBrand.getText().isEmpty()) {
                additionalInformationNaturalCustomer.setCarBrand(txtCarBrand.getText());
                additionalInformationNaturalCustomer.setCarModel(txtCarModel.getText());
                additionalInformationNaturalCustomer.setCarYear(txtCarYear.getValue());
                additionalInformationNaturalCustomer.setCarPlate(txtCarPlate.getValue());
            }
            //Ingresos
            if (txtSalary.getValue() != null) {
                additionalInformationNaturalCustomer.setSalary(txtSalary.getValue().floatValue());
                totalIngresos = txtSalary.getValue().floatValue();
            } else {
                additionalInformationNaturalCustomer.setSalary(valueZero);
            }
            if (txtProfession.getValue() != null) {
                additionalInformationNaturalCustomer.setFreeProfessionalExercise(txtProfession.getValue().floatValue());
                totalIngresos = totalIngresos + txtProfession.getValue().floatValue();
            } else {
                additionalInformationNaturalCustomer.setFreeProfessionalExercise(valueZero);
            }
            if (txtBonuses.getValue() != null) {
                additionalInformationNaturalCustomer.setBonusesCommissionsFee(txtBonuses.getValue().floatValue());
                totalIngresos = totalIngresos + txtBonuses.getValue().floatValue();
            } else {
                additionalInformationNaturalCustomer.setBonusesCommissionsFee(valueZero);
            }
            if (txtRentIncome.getValue() != null) {
                additionalInformationNaturalCustomer.setRentsIncome(txtRentIncome.getValue().floatValue());
                totalIngresos = totalIngresos + txtRentIncome.getValue().floatValue();
            } else {
                additionalInformationNaturalCustomer.setRentsIncome(valueZero);
            }
            if (txtOtherIncome.getValue() != null) {
                additionalInformationNaturalCustomer.setOtherIncome(txtOtherIncome.getValue().floatValue());
                totalIngresos = totalIngresos + txtOtherIncome.getValue().floatValue();
            } else {
                additionalInformationNaturalCustomer.setOtherIncome(valueZero);
            }
            if (totalIngresos > 0) {
//                additionalInformationNaturalCustomer.setOtherIncome(txtTotalIncome.getValue().floatValue());
                additionalInformationNaturalCustomer.setTotalIncome(totalIngresos);
            }

            //Egresos
            if (txtHousingExpenses.getValue() != null) {
                additionalInformationNaturalCustomer.setHousingExpenses(txtHousingExpenses.getValue().floatValue());
                totalEgresos = txtHousingExpenses.getValue().floatValue();
            } else {
                additionalInformationNaturalCustomer.setHousingExpenses(valueZero);
            }
            if (txtMonthlyRentMortgage.getValue() != null) {
                additionalInformationNaturalCustomer.setMonthlyRentMortgage(txtMonthlyRentMortgage.getValue().floatValue());
                totalEgresos = totalEgresos + txtMonthlyRentMortgage.getValue().floatValue();
            } else {
                additionalInformationNaturalCustomer.setMonthlyRentMortgage(valueZero);
            }
            if (txtMonthlyPaymentCreditCard.getValue() != null) {
                additionalInformationNaturalCustomer.setMonthlyPaymentCreditCard(txtMonthlyPaymentCreditCard.getValue().floatValue());
                totalEgresos = totalEgresos + txtMonthlyPaymentCreditCard.getValue().floatValue();
            } else {
                additionalInformationNaturalCustomer.setMonthlyPaymentCreditCard(valueZero);
            }
            if (txtMonthlyPaymentOtherCredit.getValue() != null) {
                additionalInformationNaturalCustomer.setMonthlyPaymentOtherCredit(txtMonthlyPaymentOtherCredit.getValue().floatValue());
                totalEgresos = totalEgresos + txtMonthlyPaymentOtherCredit.getValue().floatValue();
            } else {
                additionalInformationNaturalCustomer.setMonthlyPaymentOtherCredit(valueZero);
            }
            if (txtEducationExpenses.getValue() != null) {
                additionalInformationNaturalCustomer.setEducationExpenses(txtEducationExpenses.getValue().floatValue());
                totalEgresos = totalEgresos + txtEducationExpenses.getValue().floatValue();
            } else {
                additionalInformationNaturalCustomer.setEducationExpenses(valueZero);
            }
            if (totalEgresos > 0) {
                additionalInformationNaturalCustomer.setTotalExpenses(totalEgresos);
            }
            if (cmbCountry.getSelectedItem() != null) {
                additionalInformationNaturalCustomer.setCountryId((Country) cmbCountry.getSelectedItem().getValue());
            } else {
                additionalInformationNaturalCustomer.setCountryId(naturalCustomer.getPersonId().getCountryId());
            }
            additionalInformationNaturalCustomer = personEJB.saveAdditionalInformationNaturalCustomer(additionalInformationNaturalCustomer);

            this.showMessage("sp.common.save.success", false, null);
            additionalInformationNaturalCustomerParam = additionalInformationNaturalCustomer;

            loadFields(additionalInformationNaturalCustomerParam);

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
                    saveNaturalPersonCustomer(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveNaturalPersonCustomer(additionalInformationNaturalCustomerParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                txtTotalIncome.setDisabled(true);
                txtTotalExpenses.setDisabled(true);
                loadCmbCountry(eventType);
                if (additionalInformationNaturalCustomerParam != null) {
                    loadFields(additionalInformationNaturalCustomerParam);
                    onChange$cmbCountry();
                } else {
                    additionalInformationNaturalCustomerParam = null;
                }
                break;
            case WebConstants.EVENT_VIEW:
                blockFields();
                if (additionalInformationNaturalCustomerParam != null) {
                    loadFields(additionalInformationNaturalCustomerParam);
                } else {
                    additionalInformationNaturalCustomerParam = null;
                }
                txtTotalIncome.setDisabled(true);
                txtTotalExpenses.setDisabled(true);
                loadCmbCountry(eventType);
                onChange$cmbCountry();
                break;
            case WebConstants.EVENT_ADD:
                txtTotalIncome.setDisabled(true);
                txtTotalExpenses.setDisabled(true);
                loadCmbCountry(eventType);
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
            loadGenericCombobox(countries, cmbCountry, "name", evenInteger, Long.valueOf(additionalInformationNaturalCustomerParam != null ? additionalInformationNaturalCustomerParam.getCountryId().getId() : 0));
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
        if (additionalInformationNaturalCustomerParam != null) {
            params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, additionalInformationNaturalCustomerParam.getDocumentsPersonTypeId().getPersonTypeId().getOriginApplicationId().getId());
        } else {
            params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, Constants.ORIGIN_APPLICATION_CMS_ID);
        }
        request1.setParams(params);
        List<DocumentsPersonType> documentsPersonType = null;
        try {
            documentsPersonType = utilsEJB.getDocumentsPersonByCountry(request1);
            loadGenericCombobox(documentsPersonType, cmbDocumentsPersonType, "description", evenInteger, Long.valueOf(additionalInformationNaturalCustomerParam != null ? additionalInformationNaturalCustomerParam.getDocumentsPersonTypeId().getId() : 0));
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

}
