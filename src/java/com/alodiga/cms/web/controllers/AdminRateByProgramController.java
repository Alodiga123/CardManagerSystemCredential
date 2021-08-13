package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.RateByProgram;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;

public class AdminRateByProgramController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private RateByProgram rateByProgramParam;
    public static RateByProgram rateProgram = null;
    private UtilsEJB utilsEJB = null;
    private ProductEJB productEJB = null;
    private Label lblProgram;
    private Label lblProductType;
    private Label lblChannel;
    private Label lblTransaction;
    private Label lblRateApplicationType;
    private Label lblTransactionCode;
    private Label lblStatus;
    private Textbox txtFixedRate;
    private Textbox txtPercentageRate;
    private Textbox txtTotalTransactionInitialExempt;
    private Textbox txtTotalTransactionExemptPerMonth;
    private Radio rModificationCardHolderYes;
    private Radio rModificationCardHolderNo;
    private Button btnSave;
    private Toolbarbutton tbbTitle;
    public Tabbox tb;
    public Window winAdminRateByProgram;
    private Float fixedRate;
    private Float percentageRate;
    private int totalTransactionInitialExempt;
    private int totalTransactionExemptPerMonth;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
           rateByProgramParam = null;                    
        } else {
           rateByProgramParam = (RateByProgram) Sessions.getCurrent().getAttribute("object");            
        }        
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();        
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public RateByProgram getRateByProgram() {
        return rateProgram;
    }
    
    public void clearFields() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void loadFields(RateByProgram rateByProgram) {
        try {
            lblProgram.setValue(rateByProgram.getProgramId().getName());
            lblProductType.setValue(rateByProgram.getProgramId().getProductTypeId().getName());
            lblChannel.setValue(rateByProgram.getChannelId().getName());
            lblTransactionCode.setValue(rateByProgram.getTransactionId().getCode());
            if (rateByProgram.getApprovalProgramRateId() != null) {
                lblStatus.setValue(Labels.getLabel("cms.common.approved"));
            }else{
                lblStatus.setValue(Labels.getLabel("cms.common.tobeApproved"));
            }   
            lblTransaction.setValue(rateByProgram.getTransactionId().getDescription());
            lblRateApplicationType.setValue(rateByProgram.getRateApplicationTypeId().getDescription());
            txtTotalTransactionInitialExempt.setText(rateByProgram.getTotalInitialTransactionsExempt().toString());
            totalTransactionInitialExempt = rateByProgram.getTotalInitialTransactionsExemptGR();
            txtTotalTransactionExemptPerMonth.setText(rateByProgram.getTotalTransactionsExemptPerMonth().toString());
            totalTransactionExemptPerMonth = rateByProgram.getTotalTransactionsExemptPerMonthGR();
            if (rateByProgram.getFixedRate() != null) {
                txtFixedRate.setText(rateByProgram.getFixedRate().toString());
                fixedRate = rateByProgram.getFixedRateGR();
                txtPercentageRate.setDisabled(true);
            } else {
                txtPercentageRate.setText(rateByProgram.getPercentageRate().toString());
                percentageRate = rateByProgram.getPercentageRateGR();
                txtFixedRate.setDisabled(true);
            }                   
            if (rateByProgram.getIndCardHolderModification() == true) {
                rModificationCardHolderYes.setChecked(true);
            } else {
                rModificationCardHolderNo.setChecked(true);
            }
            
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        btnSave.setVisible(false);
    }

    private void saveRateByProgram(RateByProgram _rateByProgram) {
        try {
            boolean indModificationCardHolder = true;
            RateByProgram rateByProgram = null;

            if (_rateByProgram != null) {
                rateByProgram = _rateByProgram;
            } else {//New Request
                rateByProgram = new RateByProgram();
            }
            
            if (rModificationCardHolderYes.isChecked()) {
                indModificationCardHolder = true;
            } else {
                indModificationCardHolder = false;
            }
            
            //Guarda las tarifas del programa en la BD
            rateByProgram.setProgramId(rateByProgram.getProgramId());
            rateByProgram.setChannelId(rateByProgram.getChannelId());
            rateByProgram.setTransactionId(rateByProgram.getTransactionId());
            if (!txtFixedRate.getText().equalsIgnoreCase("")) {
             rateByProgram.setFixedRate(Float.parseFloat(txtFixedRate.getText()));
            }
            if (!txtPercentageRate.getText().equalsIgnoreCase("")) {
                rateByProgram.setPercentageRate(Float.parseFloat(txtPercentageRate.getText()));
            }   
            if (!txtTotalTransactionInitialExempt.getText().equalsIgnoreCase("")) {
                rateByProgram.setTotalInitialTransactionsExempt(Integer.parseInt(txtTotalTransactionInitialExempt.getText()));
            }
            if (!txtTotalTransactionExemptPerMonth.getText().equalsIgnoreCase("")) {
                rateByProgram.setTotalTransactionsExemptPerMonth(Integer.parseInt(txtTotalTransactionExemptPerMonth.getText()));
            }
            
            rateByProgram.setRateApplicationTypeId(rateByProgram.getRateApplicationTypeId());
            rateByProgram.setIndCardHolderModification(indModificationCardHolder);
            rateByProgram = productEJB.saveRateByProgram(rateByProgram);
            rateByProgramParam = rateByProgram;
            this.showMessage("sp.common.save.success", false, null);
            EventQueues.lookup("updateRateByProgram", EventQueues.APPLICATION, true).publish(new Event(""));
        } catch (Exception ex) {
            showError(ex);
        }

    }

    public void onClick$btnSave() {
       this.clearMessage();
       if (validateEmpty() && validateRateByProgram()) {
        switch (eventType) {
            case WebConstants.EVENT_ADD:
                saveRateByProgram(null);
                break;
            case WebConstants.EVENT_EDIT:
                saveRateByProgram(rateByProgramParam);
                break;
            default:
                break;
        } 
      }
    }
    
    public void onclick$btnBack() {
        winAdminRateByProgram.detach();
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(rateByProgramParam);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(rateByProgramParam);
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                break;
            default:
                break;
        }
    }
    
    public Boolean validateEmpty() {
        if (txtFixedRate.isDisabled() == false) {
            if (txtFixedRate.getText().equalsIgnoreCase("") ) {
               txtFixedRate.setFocus(true);
               this.showMessage("cms.common.fixedRate.error2", true, null);
               return false;
        }
            if (Float.parseFloat(txtFixedRate.getText())<=0 ) {
               txtFixedRate.setFocus(true);
               this.showMessage("sp.error.invalid.amount", true, null);
               return false;
            }
        }
        
        if (txtPercentageRate.isDisabled() == false) {
            if (txtPercentageRate.getText().equalsIgnoreCase("") ) {
               txtPercentageRate.setFocus(true);
               this.showMessage("cms.common.percentageRate.error", true, null); 
               return false;
            }
            if (Float.parseFloat(txtPercentageRate.getText())<=0 ) {
               txtPercentageRate.setFocus(true);
               this.showMessage("sp.error.invalid.amount", true, null);
               return false;
            }
        }
        
        if (txtTotalTransactionInitialExempt.getText().equalsIgnoreCase("")) {
           txtTotalTransactionInitialExempt.setFocus(true);
           this.showMessage("sp.error.field.cannotNull", true, null);
           return false;
        }
        if (txtTotalTransactionExemptPerMonth.getText().equalsIgnoreCase("")) {
           txtTotalTransactionExemptPerMonth.setFocus(true);
           this.showMessage("sp.error.field.cannotNull", true, null);
           return false;
        }
        
        if(!(rModificationCardHolderYes.isChecked() || rModificationCardHolderNo.isChecked())){
            this.showMessage("cms.common.indModificationCardHolder.error", true, null);
            rModificationCardHolderYes.setFocus(true);
            return false;
        }
   
        return true;
    }
    
    public boolean validateRateByProgram() {
        if (txtPercentageRate.isDisabled() == false) {
            if (Float.parseFloat(txtPercentageRate.getText()) > percentageRate ) {
                this.showMessage("cms.rateByProgram.Validation.percentageRate", false, null);
                txtPercentageRate.setFocus(true);
                return false;
            }
        }              
        if (txtFixedRate.isDisabled() == false) {
            if (Float.parseFloat(txtFixedRate.getText()) > fixedRate ) {
                this.showMessage("cms.rateByProgram.Validation.fixedRate", false, null);
                txtFixedRate.setFocus(true);
                return false;
            }
        }       
        if(!txtTotalTransactionInitialExempt.getText().equalsIgnoreCase("")){
            if (Float.parseFloat(txtTotalTransactionInitialExempt.getText()) > totalTransactionInitialExempt ) {
                this.showMessage("cms.rateByProgram.Validation.totalTransactionInitialExempt", false, null);
                txtTotalTransactionInitialExempt.setFocus(true);
                return false;
            } 
        }        
        if(!txtTotalTransactionExemptPerMonth.getText().equalsIgnoreCase("")){
            if (Float.parseFloat(txtTotalTransactionExemptPerMonth.getText()) > totalTransactionExemptPerMonth ) {
                this.showMessage("cms.rateByProgram.Validation.totalTransactionExemptPerMonth", false, null);
                txtTotalTransactionExemptPerMonth.setFocus(true);
                return false;
            }
        }
        return true;
    }
    
}