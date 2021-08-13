package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.RateByProduct;
import com.cms.commons.models.RateByProgram;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;

import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;

public class AdminRateByProductController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private RateByProduct rateByProductParam;
    public static RateByProgram rateProgram = null;
    public static RateByProduct rateProduct = null;
    private ProductEJB productEJB = null;
    private Label lblProgram;
    private Label lblProductType;
    private Label lblProduct;
    private Label lblTransactionCode;
    private Label lblStatus;
    private Label lblChannel;
    private Label lblTransaction;
    private Label lblRateApplicationType;
    private Textbox txtFixedRate;
    private Textbox txtPercentageRate;
    private Textbox txtTotalTransactionInitialExempt;
    private Textbox txtTotalTransactionExemptPerMonth;
    private Radio rModificationCardHolderYes;
    private Radio rModificationCardHolderNo;
    private Button btnSave;
    private Toolbarbutton tbbTitle;
    public Window winAdminRateByProduct;
    private Float fixedRate;
    private Float percentageRate;
    private int totalTransactionInitialExempt;
    private int totalTransactionExemptPerMonth;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        rateByProductParam = (Sessions.getCurrent().getAttribute("object") != null) ? (RateByProduct) Sessions.getCurrent().getAttribute("object") : null;
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
           rateByProductParam = null;                    
       } else {
           rateByProductParam = (RateByProduct) Sessions.getCurrent().getAttribute("object");            
       }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public RateByProduct getRateByProduct() {
        return rateProduct;
    }
    
    public void clearFields() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void loadFields(RateByProduct rateByProduct) {
        try {
            lblProgram.setValue(rateByProduct.getProductId().getProgramId().getName());
            lblProductType.setValue(rateByProduct.getProductId().getProgramId().getProductTypeId().getName());
            lblProduct.setValue(rateByProduct.getProductId().getName());
            lblChannel.setValue(rateByProduct.getChannelId().getName());
            lblTransactionCode.setValue(rateByProduct.getTransactionId().getCode());
            if (rateByProduct.getApprovalProductRateId() != null) {
                lblStatus.setValue(Labels.getLabel("cms.common.approved"));
            }else{
                lblStatus.setValue(Labels.getLabel("cms.common.tobeApproved"));
            }   
            lblTransaction.setValue(rateByProduct.getTransactionId().getDescription());
            lblRateApplicationType.setValue(rateByProduct.getRateApplicationTypeId().getDescription());
             if (rateByProduct.getFixedRate() != null) {
                txtFixedRate.setText(rateByProduct.getFixedRate().toString());
                fixedRate = rateByProduct.getFixedRatePR();
                txtPercentageRate.setDisabled(true);
            } else {
                txtPercentageRate.setText(rateByProduct.getPercentageRate().toString());
                percentageRate = rateByProduct.getPercentageRatePR();
                txtFixedRate.setDisabled(true);
            }    
            txtTotalTransactionInitialExempt.setText(rateByProduct.getTotalInitialTransactionsExempt().toString());
            totalTransactionInitialExempt = rateByProduct.getTotalInitialTransactionsExemptPR();
            txtTotalTransactionExemptPerMonth.setText(rateByProduct.getTotalTransactionsExemptPerMonth().toString());
            totalTransactionExemptPerMonth = rateByProduct.getTotalTransactionsExemptPerMonthPR();
            if (rateByProduct.getIndCardHolderModification() == true) {
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
    
    public void onChange$txtFixedRate() {
        this.clearMessage();
        if (Float.parseFloat(txtFixedRate.getText()) > fixedRate ) {
            this.showMessage("cms.rateByProduct.Validation.fixedRate", false, null);
            btnSave.setDisabled(true);
        } else {
            this.clearMessage();
            btnSave.setDisabled(false);
        }
    }
    
    public void onChange$txtPercentageRate() {
        this.clearMessage();
        if (Float.parseFloat(txtPercentageRate.getText()) > percentageRate ) {
            this.showMessage("cms.rateByProduct.Validation.percentageRate", false, null);
            btnSave.setDisabled(true);
        } else {
            this.clearMessage();
            btnSave.setDisabled(false);
        }
    }
    
    public void onChange$txtTotalTransactionInitialExempt() {
        this.clearMessage();
        if (Float.parseFloat(txtTotalTransactionInitialExempt.getText()) > totalTransactionInitialExempt ) {
            this.showMessage("cms.rateByProduct.Validation.totalTransactionInitialExempt", false, null);
            btnSave.setDisabled(true);
        } else {
            this.clearMessage();
            btnSave.setDisabled(false);
        }
    }
    
    public void onChange$txtTotalTransactionExemptPerMonth() {
        this.clearMessage();
        if (Float.parseFloat(txtTotalTransactionExemptPerMonth.getText()) > totalTransactionExemptPerMonth ) {
            this.showMessage("cms.rateByProduct.Validation.totalTransactionExemptPerMonth", false, null);
            btnSave.setDisabled(true);
        } else {
            this.clearMessage();
            btnSave.setDisabled(false);
        }
    }

    private void saveRateByProduct(RateByProduct _rateByProduct) {
        try {
            boolean indModificationCardHolder = true;
            RateByProduct rateByProduct = null;

            if (_rateByProduct != null) {
                rateByProduct = _rateByProduct;
            } else {
                rateByProduct = new RateByProduct();
            }
            
            if (rModificationCardHolderYes.isChecked()) {
                indModificationCardHolder = true;
            } else {
                indModificationCardHolder = false;
            }
            
            //Guarda las tarifas del producto en la BD
            rateByProduct.setChannelId(rateByProduct.getChannelId());
            rateByProduct.setTransactionId(rateByProduct.getTransactionId());
            rateByProduct.setFixedRate(Float.parseFloat(txtFixedRate.getText()));
            rateByProduct.setPercentageRate(Float.parseFloat(txtPercentageRate.getText()));
            rateByProduct.setTotalInitialTransactionsExempt(Integer.parseInt(txtTotalTransactionInitialExempt.getText()));
            rateByProduct.setTotalTransactionsExemptPerMonth(Integer.parseInt(txtTotalTransactionExemptPerMonth.getText()));
            rateByProduct.setRateApplicationTypeId(rateByProduct.getRateApplicationTypeId());
            rateByProduct.setIndCardHolderModification(indModificationCardHolder);
            rateByProduct = productEJB.saveRateByProduct(rateByProduct);
            rateByProductParam = rateByProduct;
            this.showMessage("sp.common.save.success", false, null);
            EventQueues.lookup("updateRateByProduct", EventQueues.APPLICATION, true).publish(new Event(""));
        } catch (Exception ex) {
            showError(ex);
        }

    }

    public void onClick$btnSave() {
        this.clearMessage();
        if (validateEmpty() && validateRateByProduct()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveRateByProduct(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveRateByProduct(rateByProductParam);
                    break;
                default:
                    break;
            }
         }
    }
    
    public void onclick$btnBack() {
        winAdminRateByProduct.detach();
//        Executions.getCurrent().sendRedirect("listRateByProduct.zul");
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(rateByProductParam);
            break;
            case WebConstants.EVENT_VIEW:
                loadFields(rateByProductParam);
                txtFixedRate.setDisabled(true);
                txtPercentageRate.setDisabled(true);
                txtTotalTransactionInitialExempt.setDisabled(true);
                txtTotalTransactionExemptPerMonth.setDisabled(true);
                blockFields();
                rModificationCardHolderYes.setDisabled(true);
                rModificationCardHolderNo.setDisabled(true);
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
    
    public boolean validateRateByProduct() {
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