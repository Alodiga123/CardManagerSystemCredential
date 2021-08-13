package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.ApplicantNaturalPerson;
import com.cms.commons.models.FamilyReferences;
import com.cms.commons.models.NaturalCustomer;
import com.cms.commons.models.Request;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.text.SimpleDateFormat;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class AdminFamilyReferencesController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblRequestNumber;
    private Label lblRequestDate;
    private Label lblStatusRequest;
    private Textbox txtFullName;
    private Textbox txtFullLastName;
    private Textbox txtCity;
    private Textbox txtCellPhone;
    private Textbox txtLocalPhone;
    private PersonEJB personEJB = null;
    private FamilyReferences familyReferencesParam;
    private AdminRequestController adminRequest = null;
    private Request requestCard;
    private Button btnSave;
    private Integer eventType;
    public Window winAdminFamilyReferences;
    public String indGender = null;
    private Long optionMenu;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        adminRequest = new AdminRequestController();
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (adminRequest.getRequest() != null) {
            requestCard = adminRequest.getRequest();
        }
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                familyReferencesParam = (FamilyReferences) Sessions.getCurrent().getAttribute("object");
                break;
            case WebConstants.EVENT_VIEW:
                familyReferencesParam = (FamilyReferences) Sessions.getCurrent().getAttribute("object");
                break;
            case WebConstants.EVENT_ADD:
                familyReferencesParam = null;
                break;
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            optionMenu = (Long) session.getAttribute(WebConstants.OPTION_MENU);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        txtFullName.setRawValue(null);
        txtCity.setRawValue(null);
        txtLocalPhone.setRawValue(null);
        txtCellPhone.setRawValue(null);
        txtFullLastName.setRawValue(null);
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

    private void loadFields(FamilyReferences familyReferences) {
        try {
            txtFullName.setText(familyReferences.getFirstNames());
            txtCity.setText(familyReferences.getCity());
            txtLocalPhone.setText(familyReferences.getLocalPhone());
            txtCellPhone.setText(familyReferences.getCellPhone());
            txtFullLastName.setText(familyReferences.getLastNames());
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtFullName.setReadonly(true);
        txtCity.setReadonly(true);
        txtLocalPhone.setReadonly(true);
        txtCellPhone.setReadonly(true);
        txtFullLastName.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtFullName.getText().isEmpty()) {
            txtFullName.setFocus(true);
            this.showMessage("cms.error.field.fullName", true, null);
        } else if (txtFullLastName.getText().isEmpty()) {
            txtFullLastName.setFocus(true);
            this.showMessage("cms.error.field.lastName", true, null);
        } else if (txtCity.getText().isEmpty()) {
            txtCity.setFocus(true);
            this.showMessage("cms.error.field.city", true, null);
        } else if (txtCellPhone.getText().isEmpty()) {
            txtCellPhone.setFocus(true);
            this.showMessage("cms.error.field.cellPhone", true, null);
        } else if (txtLocalPhone.getText().isEmpty()) {
            txtLocalPhone.setFocus(true);
            this.showMessage("cms.error.field.phoneNumber", true, null);
        } else {
            return true;
        }
        return false;

    }

    private void saveFamilyReferences(FamilyReferences _familyReferences) {
        ApplicantNaturalPerson naturalPerson = null;
        NaturalCustomer naturalCustomer = null;
        try {
            FamilyReferences familyReferences = null;

            if (_familyReferences != null) {
                familyReferences = _familyReferences;
                naturalPerson = familyReferencesParam.getApplicantNaturalPersonId();
                naturalCustomer = familyReferencesParam.getNaturalCustomerId();
            } else {//New LegalPerson
                familyReferences = new FamilyReferences();
            }

            //Solicitante
            if (optionMenu == Constants.LIST_CARD_REQUEST) {
                AdminNaturalPersonController adminNaturalPerson = new AdminNaturalPersonController();
                if (adminNaturalPerson.getApplicantNaturalPerson() != null) {
                    naturalPerson = adminNaturalPerson.getApplicantNaturalPerson();
                }
            } else if (optionMenu == Constants.LIST_CUSTOMER_MANAGEMENT) {
                if (naturalCustomer == null) {
                    AdminNaturalPersonCustomerController adminNaturalCustomer = new AdminNaturalPersonCustomerController();
                    AdminNaturalPersonController adminNaturalPerson = new AdminNaturalPersonController();

                    if (adminNaturalCustomer != null) {
                        naturalCustomer = adminNaturalCustomer.getNaturalCustomer();
                    }
                    if (adminNaturalPerson.getApplicantNaturalPerson() != null) {
                        naturalPerson = adminNaturalPerson.getApplicantNaturalPerson();
                    }
                }
            } else {
                naturalPerson = null;
                naturalCustomer = null;
            }

            //Guarda la referencia familiar asociada al solicitante
            familyReferences.setFirstNames(txtFullName.getText());
            if (naturalPerson != null) {
                familyReferences.setApplicantNaturalPersonId(naturalPerson);
            }
            familyReferences.setCity(txtCity.getText());
            familyReferences.setLocalPhone(txtLocalPhone.getText());
            familyReferences.setCellPhone(txtCellPhone.getText());
            familyReferences.setLastNames(txtFullLastName.getText());
            if (naturalCustomer != null) {
                familyReferences.setNaturalCustomerId(naturalCustomer);
            }
            familyReferences = personEJB.saveFamilyReferences(familyReferences);

            familyReferencesParam = familyReferences;
            this.showMessage("sp.common.save.success", false, null);

            btnSave.setVisible(false);
            EventQueues.lookup("updateFamilyReferences", EventQueues.APPLICATION, true).publish(new Event(""));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveFamilyReferences(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveFamilyReferences(familyReferencesParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void onClick$btnBack() {
        winAdminFamilyReferences.detach();
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFieldR(requestCard);
                loadFields(familyReferencesParam);
                break;
            case WebConstants.EVENT_VIEW:
                loadFieldR(requestCard);
                loadFields(familyReferencesParam);
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                loadFieldR(requestCard);
                break;
            default:
                break;
        }
    }
}
