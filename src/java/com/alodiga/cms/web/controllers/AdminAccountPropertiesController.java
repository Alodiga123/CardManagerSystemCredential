package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.commons.ejb.ProgramEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.AccountProperties;
import com.cms.commons.models.AccountType;
import com.cms.commons.models.AccountTypeHasProductType;
import com.cms.commons.models.Country;
import com.cms.commons.models.Product;
import com.cms.commons.models.Program;
import com.cms.commons.models.SubAccountType;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Toolbarbutton;

public class AdminAccountPropertiesController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private UtilsEJB utilsEJB = null;
    private PersonEJB personEJB = null;
    private ProductEJB productEJB = null;
    private ProgramEJB programEJB = null;
    private CardEJB cardEJB = null;
    private AccountProperties accountPropertiesParam;
    private Product productParam;
    private Program programParam;
    private Label lblProductType;
    private Label lblIssuer;
    private Label lblAccountType;
    private Textbox txtIdentifier;
    private Intbox txtLengthAccount;
    private Doublebox txtMinimunAmount;
    private Doublebox txtMaximunAmount;
    private Combobox cmbCountry;
    private Combobox cmbProgram;
    private Combobox cmbSubAccountType;
    private Radio rOverdraftYes;
    private Radio rOverdraftNo;
    private Tab tabAccountSegment;
    private Button btnSave;
    private Integer eventType;
    private Toolbarbutton tbbTitle;
    public static AccountProperties accountPropertiesParent = null;
    private AccountType accountType = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            accountPropertiesParam = null;
        } else {
            accountPropertiesParam = (AccountProperties) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.account.properties.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.account.properties.view"));
                break;
            case WebConstants.EVENT_ADD:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.account.properties.add"));
                tabAccountSegment.setDisabled(true);
                break;
            default:
                break;
        }
        try {
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
            programEJB = (ProgramEJB) EJBServiceLocator.getInstance().get(EjbConstants.PROGRAM_EJB);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        lblProductType.setValue(null);
        lblIssuer.setValue(null);
        lblAccountType.setValue(null);
        txtIdentifier.setRawValue(null);
        txtLengthAccount.setRawValue(null);
        txtMinimunAmount.setRawValue(null);
        txtMaximunAmount.setRawValue(null);
    }

    public AccountProperties getAccountPropertiesParent() {
        return accountPropertiesParent;
    }

    private void loadFields(AccountProperties accountProperties) {
        try {
            lblProductType.setValue(accountProperties.getProgramId().getProductTypeId().getName());
            lblIssuer.setValue(accountProperties.getProgramId().getIssuerId().getName());
            lblAccountType.setValue(accountProperties.getAccountTypeId().getDescription());
            txtIdentifier.setValue(accountProperties.getIdentifier());
            txtLengthAccount.setValue(accountProperties.getLenghtAccount());
            txtMinimunAmount.setValue(accountProperties.getMinimunAmount());
            txtMaximunAmount.setValue(accountProperties.getMaximumAmount());
            if (accountProperties.getIndOverDraft() == true) {
                rOverdraftYes.setChecked(true);
            } else {
                rOverdraftNo.setChecked(true);
            }

            accountPropertiesParent = accountProperties;
            btnSave.setVisible(true);

        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtIdentifier.setReadonly(true);
        txtLengthAccount.setReadonly(true);
        txtMinimunAmount.setReadonly(true);
        txtMaximunAmount.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (cmbCountry.getSelectedItem() == null) {
            cmbCountry.setFocus(true);
            this.showMessage("cms.error.country.notSelected", true, null);
        } else if (cmbProgram.getSelectedItem() == null) {
            cmbProgram.setFocus(true);
            this.showMessage("cms.error.program.notSelected", true, null);
        } else if (txtIdentifier.getText().isEmpty()) {
            txtIdentifier.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (cmbSubAccountType.getSelectedItem() == null) {
            cmbSubAccountType.setFocus(true);
            this.showMessage("cms.error.subAccountType.notSelected", true, null);
        } else if (txtLengthAccount.getText().isEmpty()) {
            txtLengthAccount.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if ((!rOverdraftYes.isChecked()) && (!rOverdraftNo.isChecked())) {
            this.showMessage("cms.error.field.overdraft", true, null);
        } else if (txtMinimunAmount.getText().isEmpty()) {
            txtMinimunAmount.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtMaximunAmount.getText().isEmpty()) {
            txtMaximunAmount.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtMaximunAmount.getValue()<= txtMinimunAmount.getValue()) {
            txtMaximunAmount.setFocus(true);
            this.showMessage("cms.error.account.maximunAmount", true, null);
        } else {
            return true;
        }
        return false;
    }

    public void onChange$cmbCountry() {
        this.clearMessage();
        cmbProgram.setVisible(true);
        cmbProgram.setValue("");
        Country country = (Country) cmbCountry.getSelectedItem().getValue();
        loadCmbProgram(eventType, country.getId());
    }

    public void onChange$cmbProgram() {
        int accountTypeId = 0;
        try {
            lblProductType.setVisible(true);
            lblAccountType.setVisible(true);
            lblIssuer.setVisible(true);

            Program program = (Program) cmbProgram.getSelectedItem().getValue();
            lblProductType.setValue(program.getProductTypeId().getName());
            lblIssuer.setValue(program.getIssuerId().getName());
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.PRODUCT_TYPE_KEY, program.getProductTypeId().getId());
            request1.setParams(params);
            List<AccountTypeHasProductType> accountTypeHasProductTypeList = cardEJB.getAccountTypeHasProductTypeByProductType(request1);
            for (AccountTypeHasProductType a : accountTypeHasProductTypeList) {
                accountTypeId = a.getAccountTypeId().getId();
                lblAccountType.setValue(a.getAccountTypeId().getDescription());
                accountType = a.getAccountTypeId();
            }
            loadCmbSubAccountType(eventType, accountTypeId);
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (NullParameterException ex) {
            showError(ex);
            ex.printStackTrace();
        }
    }

    private void saveAccountProperties(AccountProperties _accountProperties) throws RegisterNotFoundException, NullParameterException, GeneralException {
        boolean indOverDraft = true;
        try {
            AccountProperties accountProperties = null;

            if (_accountProperties != null) {
                accountProperties = _accountProperties;
            } else {//New Account Properties
                accountProperties = new AccountProperties();
            }

            if (rOverdraftYes.isChecked()) {
                indOverDraft = true;
            } else {
                indOverDraft = false;
            }

            //Guardar Propiedades Cuenta
            accountProperties.setCountryId((Country) cmbCountry.getSelectedItem().getValue());
            accountProperties.setProgramId((Program) cmbProgram.getSelectedItem().getValue());
            accountProperties.setAccountTypeId(accountType);
            accountProperties.setSubAccountTypeId((SubAccountType) cmbSubAccountType.getSelectedItem().getValue());
            accountProperties.setIdentifier(txtIdentifier.getValue());
            accountProperties.setLenghtAccount(txtLengthAccount.getValue());
            accountProperties.setMinimunAmount(txtMinimunAmount.getValue().floatValue());
            accountProperties.setMaximumAmount(txtMaximunAmount.getValue().floatValue());
            accountProperties.setIndOverDraft(indOverDraft);
            accountProperties = cardEJB.saveAccountProperties(accountProperties);
            accountPropertiesParam = accountProperties;
            
            this.showMessage("sp.common.save.success", false, null);
            accountPropertiesParent = accountProperties;
            tabAccountSegment.setDisabled(false);
            EventQueues.lookup("updateAccountProperties", EventQueues.APPLICATION, true).publish(new Event(""));
            btnSave.setVisible(false);
            if (eventType == WebConstants.EVENT_EDIT) {
                btnSave.setVisible(true);
            }else{
                btnSave.setVisible(false);
            }
        } catch (Exception ex) {
            showError(ex);
        }

    }

    public void onClick$btnSave() throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveAccountProperties(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveAccountProperties(accountPropertiesParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(accountPropertiesParam);
                txtIdentifier.setDisabled(false);
                txtLengthAccount.setDisabled(false);
                txtMinimunAmount.setDisabled(false);
                txtMaximunAmount.setDisabled(false);
                loadCmbCountry(eventType);
                onChange$cmbCountry();
                onChange$cmbProgram();
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(accountPropertiesParam);
                txtIdentifier.setDisabled(false);
                txtLengthAccount.setDisabled(false);
                txtMinimunAmount.setDisabled(false);
                txtMaximunAmount.setDisabled(false);
                loadCmbCountry(eventType);
                blockFields();
                onChange$cmbCountry();
                onChange$cmbProgram();
                rOverdraftYes.setDisabled(true);
                rOverdraftNo.setDisabled(true);
                break;
            case WebConstants.EVENT_ADD:
                loadCmbCountry(eventType);
                onChange$cmbProgram();
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
            loadGenericCombobox(countries, cmbCountry, "name", evenInteger, Long.valueOf(accountPropertiesParam != null ? accountPropertiesParam.getCountryId().getId() : 0));
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

    private void loadCmbProgram(Integer evenInteger, int countryId) {
        EJBRequest request1 = new EJBRequest();
        List<Program> programs = null;
        try {
            Map params = new HashMap();
            params.put(Constants.COUNTRY_KEY, countryId);
            request1.setParams(params);
            programs = programEJB.getProgramByCountry(request1);
            loadGenericCombobox(programs, cmbProgram, "name", evenInteger, Long.valueOf(accountPropertiesParam != null ? accountPropertiesParam.getProgramId().getId() : 0));
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

    private void loadCmbSubAccountType(Integer eventType, int accountTypeId) {
        EJBRequest request1 = new EJBRequest();
        List<SubAccountType> subAccountTypeList;
        cmbSubAccountType.getItems().clear();
        Map params = new HashMap();
        params.put(Constants.ACCOUNT_TYPE_KEY, accountTypeId);
        request1.setParams(params);
        try {
            subAccountTypeList = cardEJB.getSubAccountTypeByAccountType(request1);
            loadGenericCombobox(subAccountTypeList, cmbSubAccountType, "name", eventType, Long.valueOf(accountPropertiesParam != null ? accountPropertiesParam.getAccountTypeId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);

        } catch (GeneralException ex) {
            showError(ex);

        } catch (NullParameterException ex) {
            showError(ex);

        }
    }

}
