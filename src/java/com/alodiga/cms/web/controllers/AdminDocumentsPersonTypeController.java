package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Country;
import com.cms.commons.models.DocumentsPersonType;
import com.cms.commons.models.OriginApplication;
import com.cms.commons.models.PersonType;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;

public class AdminDocumentsPersonTypeController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private UtilsEJB utilsEJB = null;
    private DocumentsPersonType documentsPersonTypeParam;
    private Combobox cmbPersonType;
    private Combobox cmbCountry;
    private Combobox cmbOriginApplication;
    private Textbox txtDocumentPerson;
    private Textbox txtIdentityCode;
    private Button btnSave;
    private Integer eventType;
    private Toolbarbutton tbbTitle;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        Sessions.getCurrent();
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            documentsPersonTypeParam = null;
        } else {
            documentsPersonTypeParam = (DocumentsPersonType) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.documentsPersonType.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.documentsPersonType.view"));
                break;
            case WebConstants.EVENT_ADD:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.documentsPersonType.add"));
                break;
            default:
                break;
        }
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public void onChange$cmbOriginApplication() {
        Country country = (Country) cmbCountry.getSelectedItem().getValue();
        OriginApplication origin = (OriginApplication) cmbOriginApplication.getSelectedItem().getValue();
        loadCmbPersonType(eventType, country.getId().intValue(), origin.getId());
    }
    
    public void onChange$cmbCountry() {
        if (cmbCountry.getSelectedItem() == null) {
           cmbOriginApplication.setDisabled(true);
           cmbPersonType.setDisabled(true);
        } else {
            cmbOriginApplication.setDisabled(false);
            cmbPersonType.setDisabled(false);
        } 
    }

    public void clearFields() {
        txtDocumentPerson.setRawValue(null);
        txtIdentityCode.setRawValue(null);
    }

    private void loadFields(DocumentsPersonType documentsPersonType) {
        try {
            txtDocumentPerson.setText(documentsPersonType.getDescription());
            if (txtDocumentPerson != null) {
                txtIdentityCode.setText(documentsPersonType.getCodeIdentificationNumber());
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtDocumentPerson.setReadonly(true);
        txtIdentityCode.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtDocumentPerson.getText().isEmpty()) {
            txtDocumentPerson.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtIdentityCode.getText().isEmpty()) {
            txtIdentityCode.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveDocumentsPersonType(DocumentsPersonType _documentsPersonType) {
        try {
            DocumentsPersonType documentsPersonType = null;
            if (_documentsPersonType != null) {
                documentsPersonType = _documentsPersonType;
            } else {//New DocumentsPersonType
                documentsPersonType = new DocumentsPersonType();
            }
            documentsPersonType.setDescription(txtDocumentPerson.getText());
            documentsPersonType.setCodeIdentificationNumber(txtIdentityCode.getText());
            documentsPersonType.setPersonTypeId((PersonType) cmbPersonType.getSelectedItem().getValue());
            documentsPersonType = utilsEJB.saveDocumentsPersonType(documentsPersonType);
            documentsPersonTypeParam = documentsPersonType;
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
                    saveDocumentsPersonType(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveDocumentsPersonType(documentsPersonTypeParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(documentsPersonTypeParam);
                loadCmbCountry(eventType);
                loadCmbOriginApplication(eventType);
                onChange$cmbOriginApplication();
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(documentsPersonTypeParam);
                txtDocumentPerson.setDisabled(true);
                txtIdentityCode.setDisabled(true);
                blockFields();
                loadCmbCountry(eventType);
                loadCmbOriginApplication(eventType);
                onChange$cmbOriginApplication();
                break;
            case WebConstants.EVENT_ADD:
                loadCmbCountry(eventType);
                loadCmbOriginApplication(eventType);
                onChange$cmbCountry();
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
            loadGenericCombobox(countries,cmbCountry, "name",evenInteger,Long.valueOf(documentsPersonTypeParam != null? documentsPersonTypeParam.getPersonTypeId().getCountryId().getId(): 0) );            
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
    
    private void loadCmbPersonType(Integer evenInteger, int countryId , int originApplicationId) {
        EJBRequest request1 = new EJBRequest();
        cmbPersonType.getItems().clear();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_COUNTRY_ID, countryId);
        params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, originApplicationId);
        request1.setParams(params);
        List<PersonType> personTypes = null;
        try {
            personTypes = utilsEJB.getPersonTypeByCountryByOriginApplication(request1);
            loadGenericCombobox(personTypes,cmbPersonType, "description",evenInteger,Long.valueOf(documentsPersonTypeParam != null? documentsPersonTypeParam.getPersonTypeId().getId(): 0) );            
        } catch (EmptyListException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (GeneralException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (NullParameterException ex) {
            showError(ex);
            ex.printStackTrace();
        } finally {
            if (personTypes == null) {
                this.showMessage("cms.msj.PersonTypeNull", false, null);
            }            
        }
    
      }
    
    private void loadCmbOriginApplication(Integer evenInteger) {
        //cmbOriginAplication
        EJBRequest request1 = new EJBRequest();
        List<OriginApplication> originAplication;

        try {
            originAplication = utilsEJB.getOriginApplication(request1);
            loadGenericCombobox(originAplication, cmbOriginApplication, "name", evenInteger, Long.valueOf(documentsPersonTypeParam != null ? documentsPersonTypeParam .getPersonTypeId().getOriginApplicationId().getId() : 0));
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