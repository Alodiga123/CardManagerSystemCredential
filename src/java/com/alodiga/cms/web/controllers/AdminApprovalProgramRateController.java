package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.ApprovalProgramRate;
import com.cms.commons.models.Program;
import com.cms.commons.models.RateByProgram;
import com.cms.commons.models.User;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Window;

public class AdminApprovalProgramRateController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblProgram;
    private Label txtCity;
    private Label txtAgency;
    private Label txtCommercialAssessorUserCode;
    private Label txtAssessorName;
    private Label txtIdentification;
    private Datebox txtApprovalDate;
    private Radio rApprovedYes;
    private Radio rApprovedNo;
    private ProductEJB productEJB = null;
    private User user = null;
    public static ApprovalProgramRate approvalProgramRateParam;
    private Button btnApprove;
    public Window winAdminApprovalProgramRate;
    private Program program;
    private List<RateByProgram> rateByProgramByProgramList = new ArrayList<RateByProgram>();
    public int indRateApprove = 0;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            approvalProgramRateParam = null;
        } else {
            approvalProgramRateParam = (ApprovalProgramRate) Sessions.getCurrent().getAttribute("object");
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
    
    public ApprovalProgramRate getApprovalProgramRate() {
        return approvalProgramRateParam;
    }

    private void loadFields(ApprovalProgramRate approvalProgramRate) throws EmptyListException, GeneralException, NullParameterException {
        try {
            program = (Program) session.getAttribute(WebConstants.PROGRAM);
            lblProgram.setValue(program.getName());
            txtCity.setValue(approvalProgramRate.getUserId().getComercialAgencyId().getCityId().getName());
            txtAgency.setValue(approvalProgramRate.getUserId().getComercialAgencyId().getName());
            txtCommercialAssessorUserCode.setValue(approvalProgramRate.getUserId().getCode());
            txtAssessorName.setValue(approvalProgramRate.getUserId().getFirstNames() + " " + approvalProgramRate.getUserId().getLastNames());
            txtIdentification.setValue(approvalProgramRate.getUserId().getIdentificationNumber());
            txtApprovalDate.setValue(approvalProgramRate.getApprovalDate());
            if (approvalProgramRate.getIndApproved() != null) {
                if (approvalProgramRate.getIndApproved() == true) {
                    rApprovedYes.setChecked(true);
                } else {
                    rApprovedNo.setChecked(true);
                }
            }
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
            this.showMessage("cms.error.approvalDate", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveApprovalRates(ApprovalProgramRate _approvalProgramRate) {
        ApprovalProgramRate approvalProgramRate = null;
        boolean indApproved = true;
        try {
            if (_approvalProgramRate != null) {
                approvalProgramRate = _approvalProgramRate;
            } else {
                approvalProgramRate = new ApprovalProgramRate();
            }

            //Guarda la aprobación de las tarifas por programa
            approvalProgramRate.setProgramId(program);
            approvalProgramRate.setApprovalDate(txtApprovalDate.getValue());
            approvalProgramRate.setIndApproved(indApproved);
            approvalProgramRate.setUserId(user);
            approvalProgramRate.setCreateDate(new Timestamp(new Date().getTime()));
            approvalProgramRate = productEJB.saveApprovalProgramRate(approvalProgramRate);
            approvalProgramRateParam = approvalProgramRate;
            btnApprove.setVisible(false);

            //Actualiza las tarifas del programa que se está aprobando
            updateProgramRate(approvalProgramRate);

            this.showMessage("cms.common.Approve.success", false, null);
            EventQueues.lookup("updateApprovalProgramRate", EventQueues.APPLICATION, true).publish(new Event(""));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void updateProgramRate(ApprovalProgramRate approvalProgramRate) {
        try {
            Map params = new HashMap();
            EJBRequest request1 = new EJBRequest();
            params.put(QueryConstants.PARAM_PROGRAM_ID, program.getId());
            request1.setParams(params);
            rateByProgramByProgramList = productEJB.getRateByProgramByProgram(request1);
            for (RateByProgram rateByProgram : rateByProgramByProgramList) {
                rateByProgram.setApprovalProgramRateId(approvalProgramRate);
                rateByProgram = productEJB.saveRateByProgram(rateByProgram);
            }
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (RegisterNotFoundException ex) {
            showError(ex);
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
        winAdminApprovalProgramRate.detach();
    }

    public void loadData() {
        Date today = new Timestamp(new Date().getTime());
        try {
            switch (eventType) {
                case WebConstants.EVENT_VIEW:
                    loadFields(approvalProgramRateParam);
                    blockFields();
                    break;
                case WebConstants.EVENT_ADD:
                    txtApprovalDate.setValue(today);
                    lblProgram.setValue(program.getName());
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
