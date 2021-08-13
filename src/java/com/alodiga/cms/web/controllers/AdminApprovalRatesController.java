package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.ApprovalGeneralRate;
import com.cms.commons.models.Country;
import com.cms.commons.models.GeneralRate;
import com.cms.commons.models.User;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

public class AdminApprovalRatesController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;

    private Label txtCity;
    private Label txtAgency;
    private Label txtCommercialAssessorUserCode;
    private Label txtAssessorName;
    private Label txtIdentification;
    private Datebox txtApprovalDate;
    private ProductEJB productEJB = null;
    private User user = null;
    public static ApprovalGeneralRate approvalGeneralRateParam;
    public int indRateApprove = 0;
    private Button btnApprove;
    public Window winAdminApprovalGeneralRates;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            approvalGeneralRateParam = null;
        } else {
            approvalGeneralRateParam = (ApprovalGeneralRate) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            user = (User) session.getAttribute(Constants.USER_OBJ_SESSION);
            productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
            loadData();
            this.clearMessage();
        } catch (Exception ex) {
            showError(ex);
        } finally {
            loadData();
        }
    }

    public void clearFields() {
        txtApprovalDate.setRawValue(null);
    }
    
    public ApprovalGeneralRate getApprovalGeneralRate() {
        return approvalGeneralRateParam;
    }

    private void loadFields(ApprovalGeneralRate approvalGeneralRate) throws EmptyListException, GeneralException, NullParameterException {
        try {
            txtCity.setValue(user.getComercialAgencyId().getCityId().getName());
            txtAgency.setValue(user.getComercialAgencyId().getName());
            txtCommercialAssessorUserCode.setValue(user.getCode());
            txtAssessorName.setValue(user.getFirstNames() + " " + user.getLastNames());
            txtIdentification.setValue(user.getIdentificationNumber());
            if (approvalGeneralRate.getApprovalDate() != null) {
                txtApprovalDate.setValue(approvalGeneralRate.getApprovalDate());
            }

            btnApprove.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loadDate() {
        Date today = new Date();
        txtApprovalDate.setValue(today);
    }

    public void blockFields() {
        txtApprovalDate.setDisabled(true);
        if (eventType != WebConstants.EVENT_ADD) {
            btnApprove.setVisible(false);
        }
    }

    public Boolean validateEmpty() {
        if (txtApprovalDate.getText().isEmpty()) {
            txtApprovalDate.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveApprovalRates(ApprovalGeneralRate _approvalGeneralRate) {
        ApprovalGeneralRate approvalGeneralRate = null;
        boolean indApproved = true;
        try {
            if (_approvalGeneralRate != null) {
                approvalGeneralRate = _approvalGeneralRate;
            } else {
                approvalGeneralRate = new ApprovalGeneralRate();
            }

            //Guarda la aprobación de las tarifas generales
            approvalGeneralRate.setApprovalDate(txtApprovalDate.getValue());
            approvalGeneralRate.setIndApproved(indApproved);
            approvalGeneralRate.setUserId(user);
            approvalGeneralRate.setCreateDate(new Timestamp(new Date().getTime()));
            approvalGeneralRate = productEJB.saveApprovalGeneralRate(approvalGeneralRate);
            approvalGeneralRateParam = approvalGeneralRate;
            btnApprove.setVisible(false);
            indRateApprove = 1;
            Sessions.getCurrent().setAttribute(WebConstants.IND_RATE_APPROVE, indRateApprove);
            updateGeneralRates(approvalGeneralRate);
            
            this.showMessage("cms.common.Approve.success", false, null);
            EventQueues.lookup("updateApprovalGeneralRate", EventQueues.APPLICATION, true).publish(new Event(""));
        } catch (Exception ex) {
            this.showMessage("sp.error.title", false, null);
            showError(ex);
        }
    }

    public void updateGeneralRates(ApprovalGeneralRate approvalGeneralRate) throws RegisterNotFoundException, NullParameterException, GeneralException {
        ListGeneralRateController listGeneralRate = new ListGeneralRateController();
        List<GeneralRate> generalRateList = listGeneralRate.getGeneralRateList();            
        for (GeneralRate generalRate : generalRateList) {
            if (generalRate.getApprovalGeneralRateId()== null){
                generalRate.setApprovalGeneralRateId(approvalGeneralRate);
                productEJB.saveGeneralRate(generalRate);
            }
        }     
    }
    
    
    public void onClick$btnApprove() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveApprovalRates(null);
                    break;
                case WebConstants.EVENT_VIEW:
                    blockFields();
                    break;
                default:
                    break;
            }
        }
    }

    public void onClick$btnBack() {
        winAdminApprovalGeneralRates.detach();
    }

    public void loadData() {
        Date today = new Timestamp(new Date().getTime());
        try {
            switch (eventType) {
                case WebConstants.EVENT_VIEW:
                    loadFields(approvalGeneralRateParam);
                    blockFields();
                    break;
                case WebConstants.EVENT_ADD:
                    txtApprovalDate.setValue(today);
                    txtApprovalDate.setDisabled(true);
                    txtCity.setValue(user.getComercialAgencyId().getCityId().getName());
                    txtAgency.setValue(user.getComercialAgencyId().getName());
                    txtCommercialAssessorUserCode.setValue(user.getCode());
                    txtAssessorName.setValue(user.getFirstNames() + " " + user.getLastNames());
                    txtIdentification.setValue(user.getIdentificationNumber());
                    blockFields();
                    break;
            }
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }

}
