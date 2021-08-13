package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Country;
import com.cms.commons.models.OriginApplication;
import com.cms.commons.models.PersonType;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;

public class AdminPersonTypeController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtName;
    private UtilsEJB utilsEJB = null;
    private Combobox cmbCountry;
    private Combobox cmbOriginApplication;
    private Radio rIsNaturalPersonYes;
    private Radio rIsNaturalPersonNo;
    private PersonType personTypeParam;
    private Button btnSave;
    private Integer eventType;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        Sessions.getCurrent();
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            personTypeParam = null;
        } else {
            personTypeParam = (PersonType) Sessions.getCurrent().getAttribute("object");
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

    public void clearFields() {
        txtName.setRawValue(null);
    }

    private void loadFields(PersonType personType) {
        try {
            txtName.setText(personType.getDescription());
            if (personType.getIndNaturalPerson() == true) {
                rIsNaturalPersonYes.setChecked(true);
            } else {
                rIsNaturalPersonNo.setChecked(true);
            }
            cmbCountry.setReadonly(true);
            cmbOriginApplication.setReadonly(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtName.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtName.getText().isEmpty()) {
            txtName.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (cmbCountry.getSelectedItem() == null) {
            cmbCountry.setFocus(true);
            this.showMessage("cms.error.country.notSelected", true, null);
        } else if (cmbOriginApplication.getSelectedItem() == null) {
            cmbOriginApplication.setFocus(true);
            this.showMessage("cms.error.OriginApplication.noSelected", true, null);
        } else if ((!rIsNaturalPersonYes.isChecked()) && (!rIsNaturalPersonNo.isChecked())) {
            this.showMessage("cms.error.field.naturalPerson", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void savePersonType(PersonType _personType) {
        boolean indIsNaturalPerson;
        try {
            PersonType personType = null;
            if (_personType != null) {
                personType = _personType;
            } else {//New personType
                personType = new PersonType();
            }

            if (rIsNaturalPersonYes.isChecked()) {
                indIsNaturalPerson = true;
            } else {
                indIsNaturalPerson = false;
            }

            personType.setDescription(txtName.getText());
            personType.setCountryId((Country) cmbCountry.getSelectedItem().getValue());
            personType.setOriginApplicationId((OriginApplication) cmbOriginApplication.getSelectedItem().getValue());
            personType.setIndNaturalPerson(indIsNaturalPerson);
            personType = utilsEJB.savePersonType(personType);
            personTypeParam = personType;
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
                    savePersonType(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    savePersonType(personTypeParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(personTypeParam);
                loadCmbCountry(eventType);
                loadCmbOriginApplication(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(personTypeParam);
                txtName.setDisabled(true);
                blockFields();
                loadCmbCountry(eventType);
                loadCmbOriginApplication(eventType);
                break;
            case WebConstants.EVENT_ADD:
                loadCmbCountry(eventType);
                loadCmbOriginApplication(eventType);
                break;
            default:
                break;
        }
    }

    private void loadCmbCountry(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<Country> countryList;
        try {
            countryList = utilsEJB.getCountries(request1);
            loadGenericCombobox(countryList, cmbCountry, "name", evenInteger, Long.valueOf(personTypeParam != null ? personTypeParam.getCountryId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }

    private void loadCmbOriginApplication(Integer evenInteger) {
        EJBRequest request = new EJBRequest();
        List<OriginApplication> originApplicationList;
        try {
            originApplicationList = utilsEJB.getOriginApplication(request);
            loadGenericCombobox(originApplicationList, cmbOriginApplication, "name", evenInteger, Long.valueOf(personTypeParam != null ? personTypeParam.getOriginApplicationId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }
}
