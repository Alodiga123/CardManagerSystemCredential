package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Channel;
import com.cms.commons.models.Product;
import com.cms.commons.models.ProductHasChannelHasTransaction;
import com.cms.commons.models.ProductUse;
import com.cms.commons.models.Transaction;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class AdminLimitAndRestrictionsController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;

    private Combobox cmbTransaction;
    private Combobox cmbChannel;
    private Label txtProductUse;
    private Label txtProduct;
    private Label lblReqInternational1;
    private Label lblReqInternational2;
    private Label lblReqInternational3;
    private Label lblReqInternational4;
    private Label lblReqDomestic1;
    private Label lblReqDomestic2;
    private Label lblReqDomestic3;
    private Label lblReqDomestic4;
    private Intbox txtMaxNumbTransDaily;
    private Intbox txtMaxNumbTransMont;
    private Doublebox txtAmountMinTransDomestic;
    private Doublebox txtAmountMaxTransDomestic;
    private Doublebox txtAmountMinTransInt;
    private Doublebox txtAmountMaxTransInt;
    private Doublebox txtDailyAmountLimitDomestic;
    private Doublebox txtMonthlyAmountLimitDomestic;
    private Doublebox txtDailyAmountLimitInt;
    private Doublebox txtMonthlyAmountLimitInt;
    private ProductEJB productEJB = null;
    private ProductHasChannelHasTransaction productHasChannelHasTransactionParam;
    private Button btnSave;
    private Integer eventType;
    public Window winAdminLimitAndRestrictions;
    private Product product = null;
    private ProductUse productUse = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                productHasChannelHasTransactionParam = (ProductHasChannelHasTransaction) Sessions.getCurrent().getAttribute("object");
                break;
            case WebConstants.EVENT_VIEW:
                productHasChannelHasTransactionParam = (ProductHasChannelHasTransaction) Sessions.getCurrent().getAttribute("object");
                break;
            case WebConstants.EVENT_ADD:
                productHasChannelHasTransactionParam = null;
                break;
        }
        getProductData();
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
    
    public void clearFields() {
        txtMaxNumbTransDaily.setRawValue(null);
        txtMaxNumbTransMont.setRawValue(null);
        txtAmountMinTransDomestic.setRawValue(null);
        txtAmountMaxTransDomestic.setRawValue(null);
        txtAmountMinTransInt.setRawValue(null);
        txtAmountMaxTransInt.setRawValue(null);
        txtDailyAmountLimitDomestic.setRawValue(null);
        txtMonthlyAmountLimitDomestic.setRawValue(null);
        txtDailyAmountLimitInt.setRawValue(null);
        txtMonthlyAmountLimitInt.setRawValue(null);
    }
    
    public void getProductData(){
        //Se obtiene el Producto y el tipo de uso del Producto
        AdminProductController adminProduct = new AdminProductController();
        if (adminProduct.getProductParent().getId() != null) {
            product = adminProduct.getProductParent();
            productUse = adminProduct.getProductParent().getProductUseId();
            txtProduct.setValue(product.getName());    
            txtProductUse.setValue(product.getProductUseId().getDescription());
            validateProductUse(product.getProductUseId().getId());
        }
    }

    private void loadFields(ProductHasChannelHasTransaction productHasChannelHasTransaction) {
        try {
            txtMaxNumbTransDaily.setValue(productHasChannelHasTransaction.getMaximumNumberTransactionsDaily());
            txtMaxNumbTransMont.setValue(productHasChannelHasTransaction.getMaximumNumberTransactionsMonthly());
            if (productHasChannelHasTransaction.getAmountMinimumTransactionDomestic() != null) {
                txtAmountMinTransDomestic.setValue(productHasChannelHasTransaction.getAmountMinimumTransactionDomestic());
            }
            if (productHasChannelHasTransaction.getAmountMaximumTransactionDomestic() != null) {
                txtAmountMaxTransDomestic.setValue(productHasChannelHasTransaction.getAmountMaximumTransactionDomestic());
            }
            if (productHasChannelHasTransaction.getAmountMinimumTransactionInternational() != null) {
                txtAmountMinTransInt.setValue(productHasChannelHasTransaction.getAmountMinimumTransactionInternational());
            }
            if (productHasChannelHasTransaction.getAmountMaximumTransactionInternational() != null) {
                txtAmountMaxTransInt.setValue(productHasChannelHasTransaction.getAmountMaximumTransactionInternational());
            }
            if (productHasChannelHasTransaction.getDailyAmountLimitDomestic() != null) {
                txtDailyAmountLimitDomestic.setValue(productHasChannelHasTransaction.getDailyAmountLimitDomestic());
            }
            if (productHasChannelHasTransaction.getMonthlyAmountLimitDomestic() != null) {
                txtMonthlyAmountLimitDomestic.setValue(productHasChannelHasTransaction.getMonthlyAmountLimitDomestic());
            }
            if (productHasChannelHasTransaction.getDailyAmountLimitInternational() != null) {
                txtDailyAmountLimitInt.setValue(productHasChannelHasTransaction.getDailyAmountLimitInternational());
            }
            if (productHasChannelHasTransaction.getMonthlyAmountLimitInternational() != null) {
                txtMonthlyAmountLimitInt.setValue(productHasChannelHasTransaction.getMonthlyAmountLimitInternational());
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtMaxNumbTransDaily.setReadonly(true);
        txtMaxNumbTransMont.setReadonly(true);
        txtAmountMinTransDomestic.setReadonly(true);
        txtAmountMaxTransDomestic.setReadonly(true);
        txtAmountMinTransInt.setReadonly(true);
        txtAmountMaxTransInt.setReadonly(true);
        txtDailyAmountLimitDomestic.setReadonly(true);
        txtMonthlyAmountLimitDomestic.setReadonly(true);
        txtDailyAmountLimitInt.setReadonly(true);
        txtMonthlyAmountLimitInt.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (cmbChannel.getSelectedItem() == null) {
            cmbChannel.setFocus(true);
            this.showMessage("cms.error.chanel.noSelected", true, null);
        } else if (cmbTransaction.getSelectedItem() == null) {
            cmbTransaction.setFocus(true);
            this.showMessage("cms.error.transaction.noSelected", true, null);
        } 
        else if (txtMaxNumbTransDaily.getText().isEmpty()) {
            txtMaxNumbTransDaily.setFocus(true);
            this.showMessage("cms.error.maxNumbTransDaily", true, null);
        } else if (txtMaxNumbTransMont.getText().isEmpty()) {
            txtMaxNumbTransMont.setFocus(true);
            this.showMessage("cms.error.maxNumbTransMont", true, null);
        }  else if (productUse.getId() == WebConstants.PRODUCT_USE_DOMESTIC) {
            if (txtAmountMinTransDomestic.getText().isEmpty()) {
                txtAmountMinTransDomestic.setFocus(true);
                this.showMessage("cms.error.amountMinTransDomestic", true, null);
            } else if (txtAmountMaxTransDomestic.getText().isEmpty()) {
                txtAmountMaxTransDomestic.setFocus(true);
                this.showMessage("cms.error.amountMaxTransDomestic", true, null);
            } else if (txtDailyAmountLimitDomestic.getText().isEmpty()) {
                txtDailyAmountLimitDomestic.setFocus(true);
                this.showMessage("cms.error.dailyAmountLimitDomestic", true, null);
            } else if (txtMonthlyAmountLimitDomestic.getText().isEmpty()) {
                txtMonthlyAmountLimitDomestic.setFocus(true);
                this.showMessage("cms.error.monthlyAmountLimitDomestic", true, null);
            } else if (txtMaxNumbTransDaily.getValue() > txtMaxNumbTransMont.getValue()){
                this.showMessage("cms.product.limitAndRestrictions.DailylessMont", true, null);
            } else if (txtAmountMinTransDomestic.getValue() > txtAmountMaxTransDomestic.getValue()){
                this.showMessage("cms.product.limitAndRestrictions.DailylessMontNational", true, null);
            } else if(txtDailyAmountLimitDomestic.getValue() > txtMonthlyAmountLimitDomestic.getValue()){
            this.showMessage("cms.product.limitAndRestrictions.DailyLessMontLimit", true, null);
            }  else {
                return true;
            }
        } else if (productUse.getId() == WebConstants.PRODUCT_USE_INTERNATIONAL) {
            if (txtAmountMinTransInt.getText().isEmpty()) {
                txtAmountMinTransInt.setFocus(true);
                this.showMessage("cms.error.amountMinTransInt", true, null);
            } else if (txtAmountMaxTransInt.getText().isEmpty()) {
                txtAmountMaxTransInt.setFocus(true);
                this.showMessage("cms.error.amountMaxTransInt", true, null);
            } else if (txtDailyAmountLimitInt.getText().isEmpty()) {
                txtDailyAmountLimitInt.setFocus(true);
                this.showMessage("cms.error.dailyAmountLimitInt", true, null);
            } else if (txtMonthlyAmountLimitInt.getText().isEmpty()) {
                txtMonthlyAmountLimitInt.setFocus(true);
                this.showMessage("cms.error.monthlyAmountLimitInt", true, null);
            } else if (txtMaxNumbTransDaily.getValue() > txtMaxNumbTransMont.getValue()){
                this.showMessage("cms.product.limitAndRestrictions.DailylessMont", true, null);
            } else if (txtAmountMinTransInt.getValue() > txtAmountMaxTransInt.getValue()){
                this.showMessage("cms.product.limitAndRestrictions.DailylessMontInter", true, null);
            } else if(txtDailyAmountLimitInt.getValue() > txtMonthlyAmountLimitInt.getValue()){
                this.showMessage("cms.product.limitAndRestrictions.DailyLessMontLimitInter", true, null);
            } else {
                return true;
            }
        } else if (productUse.getId() == WebConstants.PRODUCT_USE_BOTH) {
            if (txtAmountMinTransDomestic.getText().isEmpty()) {
                txtAmountMinTransDomestic.setFocus(true);
                this.showMessage("cms.error.amountMinTransDomestic", true, null);
            } else if (txtAmountMaxTransDomestic.getText().isEmpty()) {
                txtAmountMaxTransDomestic.setFocus(true);
                this.showMessage("cms.error.amountMaxTransDomestic", true, null);
            } else if (txtAmountMinTransInt.getText().isEmpty()) {
                txtAmountMinTransInt.setFocus(true);
                this.showMessage("cms.error.amountMinTransInt", true, null);
            } else if (txtAmountMaxTransInt.getText().isEmpty()) {
                txtAmountMaxTransInt.setFocus(true);
                this.showMessage("cms.error.amountMaxTransInt", true, null);
            } else if (txtDailyAmountLimitDomestic.getText().isEmpty()) {
                txtDailyAmountLimitDomestic.setFocus(true);
                this.showMessage("cms.error.dailyAmountLimitDomestic", true, null);
            } else if (txtMonthlyAmountLimitDomestic.getText().isEmpty()) {
                txtMonthlyAmountLimitDomestic.setFocus(true);
                this.showMessage("cms.error.monthlyAmountLimitDomestic", true, null);
            } else if (txtDailyAmountLimitInt.getText().isEmpty()) {
                txtDailyAmountLimitInt.setFocus(true);
                this.showMessage("cms.error.dailyAmountLimitInt", true, null);
            } else if (txtMonthlyAmountLimitInt.getText().isEmpty()) {
                txtMonthlyAmountLimitInt.setFocus(true);
                this.showMessage("cms.error.monthlyAmountLimitInt", true, null);
            } else if (txtMaxNumbTransDaily.getValue() > txtMaxNumbTransMont.getValue()){
                this.showMessage("cms.product.limitAndRestrictions.DailylessMont", true, null);
            } else if (txtAmountMinTransDomestic.getValue() > txtAmountMaxTransDomestic.getValue()){
                this.showMessage("cms.product.limitAndRestrictions.DailylessMontNational", true, null);
            } else if (txtAmountMinTransInt.getValue() > txtAmountMaxTransInt.getValue()){
                this.showMessage("cms.product.limitAndRestrictions.DailylessMontInter", true, null);
            } else if(txtDailyAmountLimitDomestic.getValue() > txtMonthlyAmountLimitDomestic.getValue()){
                this.showMessage("cms.product.limitAndRestrictions.DailyLessMontLimit", true, null);
            } else if(txtDailyAmountLimitInt.getValue() > txtMonthlyAmountLimitInt.getValue()){
                this.showMessage("cms.product.limitAndRestrictions.DailyLessMontLimitInter", true, null);
            } else {
                return true;
            }
        } else {
            return true;
        }

        return false;
    }

    public void validateProductUse(int productUseId) {
        switch (productUseId) {
            case 1:
                txtAmountMaxTransDomestic.setDisabled(false);
                txtAmountMinTransDomestic.setDisabled(false);
                txtDailyAmountLimitDomestic.setDisabled(false);
                txtMonthlyAmountLimitDomestic.setDisabled(false);
                txtAmountMaxTransInt.setRawValue(null);
                txtAmountMinTransInt.setRawValue(null);
                txtDailyAmountLimitInt.setRawValue(null);
                txtMonthlyAmountLimitInt.setRawValue(null);
                txtAmountMaxTransInt.setDisabled(true);
                txtAmountMinTransInt.setDisabled(true);
                txtDailyAmountLimitInt.setDisabled(true);
                txtMonthlyAmountLimitInt.setDisabled(true);
                lblReqDomestic1.setVisible(true);
                lblReqDomestic2.setVisible(true);
                lblReqDomestic3.setVisible(true);
                lblReqDomestic4.setVisible(true);
                lblReqInternational1.setVisible(false);
                lblReqInternational2.setVisible(false);
                lblReqInternational3.setVisible(false);
                lblReqInternational4.setVisible(false);
                break;
            case 2:
                txtAmountMaxTransInt.setDisabled(false);
                txtAmountMinTransInt.setDisabled(false);
                txtDailyAmountLimitInt.setDisabled(false);
                txtMonthlyAmountLimitInt.setDisabled(false);
                txtAmountMaxTransDomestic.setRawValue(null);
                txtAmountMinTransDomestic.setRawValue(null);
                txtDailyAmountLimitDomestic.setRawValue(null);
                txtMonthlyAmountLimitDomestic.setRawValue(null);
                txtAmountMaxTransDomestic.setDisabled(true);
                txtAmountMinTransDomestic.setDisabled(true);
                txtDailyAmountLimitDomestic.setDisabled(true);
                txtMonthlyAmountLimitDomestic.setDisabled(true);
                lblReqDomestic1.setVisible(false);
                lblReqDomestic2.setVisible(false);
                lblReqDomestic3.setVisible(false);
                lblReqDomestic4.setVisible(false);
                lblReqInternational1.setVisible(true);
                lblReqInternational2.setVisible(true);
                lblReqInternational3.setVisible(true);
                lblReqInternational4.setVisible(true);
                break;
            case 3:
                txtAmountMaxTransDomestic.setDisabled(false);
                txtAmountMinTransDomestic.setDisabled(false);
                txtDailyAmountLimitDomestic.setDisabled(false);
                txtMonthlyAmountLimitDomestic.setDisabled(false);
                txtAmountMaxTransInt.setDisabled(false);
                txtAmountMinTransInt.setDisabled(false);
                txtDailyAmountLimitInt.setDisabled(false);
                txtMonthlyAmountLimitInt.setDisabled(false);
                lblReqDomestic1.setVisible(true);
                lblReqDomestic2.setVisible(true);
                lblReqDomestic3.setVisible(true);
                lblReqDomestic4.setVisible(true);
                lblReqInternational1.setVisible(true);
                lblReqInternational2.setVisible(true);
                lblReqInternational3.setVisible(true);
                lblReqInternational4.setVisible(true);
                break;
        }
    }

    private void saveProductHasChannelHasTransaction(ProductHasChannelHasTransaction _productHasChannelHasTransaction) {
        try {
            ProductHasChannelHasTransaction productHasChannelHasTransaction = null;

            if (_productHasChannelHasTransaction != null) {
                productHasChannelHasTransaction = _productHasChannelHasTransaction;
            } else {//New LegalPerson
                productHasChannelHasTransaction = new ProductHasChannelHasTransaction();
            }

            productHasChannelHasTransaction.setMaximumNumberTransactionsDaily(txtMaxNumbTransDaily.getValue());
            productHasChannelHasTransaction.setMaximumNumberTransactionsMonthly(txtMaxNumbTransMont.getValue());
            productHasChannelHasTransaction.setProductUseId(productUse);
            if (productUse.getId() == WebConstants.PRODUCT_USE_DOMESTIC) {
                productHasChannelHasTransaction.setAmountMaximumTransactionDomestic(txtAmountMaxTransDomestic.getValue().floatValue());
                productHasChannelHasTransaction.setAmountMinimumTransactionDomestic(txtAmountMinTransDomestic.getValue().floatValue());
                productHasChannelHasTransaction.setDailyAmountLimitDomestic(txtDailyAmountLimitDomestic.getValue().floatValue());
                productHasChannelHasTransaction.setMonthlyAmountLimitDomestic(txtMonthlyAmountLimitDomestic.getValue().floatValue());
            }
            if (productUse.getId() == WebConstants.PRODUCT_USE_INTERNATIONAL) {
                productHasChannelHasTransaction.setAmountMaximumTransactionInternational(txtAmountMaxTransInt.getValue().floatValue());
                productHasChannelHasTransaction.setAmountMinimumTransactionInternational(txtAmountMinTransInt.getValue().floatValue());
                productHasChannelHasTransaction.setDailyAmountLimitInternational(txtDailyAmountLimitInt.getValue().floatValue());
                productHasChannelHasTransaction.setMonthlyAmountLimitInternational(txtMonthlyAmountLimitInt.getValue().floatValue());
            }
            if (productUse.getId() == WebConstants.PRODUCT_USE_BOTH) {
                productHasChannelHasTransaction.setAmountMaximumTransactionDomestic(txtAmountMaxTransDomestic.getValue().floatValue());
                productHasChannelHasTransaction.setAmountMinimumTransactionDomestic(txtAmountMinTransDomestic.getValue().floatValue());
                productHasChannelHasTransaction.setDailyAmountLimitDomestic(txtDailyAmountLimitDomestic.getValue().floatValue());
                productHasChannelHasTransaction.setMonthlyAmountLimitDomestic(txtMonthlyAmountLimitDomestic.getValue().floatValue());
                productHasChannelHasTransaction.setAmountMaximumTransactionInternational(txtAmountMaxTransInt.getValue().floatValue());
                productHasChannelHasTransaction.setAmountMinimumTransactionInternational(txtAmountMinTransInt.getValue().floatValue());
                productHasChannelHasTransaction.setDailyAmountLimitInternational(txtDailyAmountLimitInt.getValue().floatValue());
                productHasChannelHasTransaction.setMonthlyAmountLimitInternational(txtMonthlyAmountLimitInt.getValue().floatValue());
            }
            productHasChannelHasTransaction.setTransactionId((Transaction) cmbTransaction.getSelectedItem().getValue());
            productHasChannelHasTransaction.setChannelId((Channel) cmbChannel.getSelectedItem().getValue());
            productHasChannelHasTransaction.setProductId(product);
            productHasChannelHasTransaction = productEJB.saveProductHasChannelHasTransaction(productHasChannelHasTransaction);
            productHasChannelHasTransactionParam = productHasChannelHasTransaction;
            this.showMessage("sp.common.save.success", false, null);
            
            if (eventType == WebConstants.EVENT_ADD) {
                btnSave.setVisible(false);
            } else {
                btnSave.setVisible(true);
            }

            EventQueues.lookup("updateLimitAndRestrictions", EventQueues.APPLICATION, true).publish(new Event(""));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveProductHasChannelHasTransaction(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveProductHasChannelHasTransaction(productHasChannelHasTransactionParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void onClick$btnBack() {
        winAdminLimitAndRestrictions.detach();
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(productHasChannelHasTransactionParam);
                loadCmbTransaction(eventType);
                loadCmbChannel(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(productHasChannelHasTransactionParam);
                blockFields();
                loadCmbTransaction(eventType);
                loadCmbChannel(eventType);
                break;
            case WebConstants.EVENT_ADD:
                loadCmbTransaction(eventType);
                loadCmbChannel(eventType);
                break;
            default:
                break;
        }
    }

    private void loadCmbTransaction(Integer evenInteger) {
        //cmbTransaction
        EJBRequest request1 = new EJBRequest();
        List<Transaction> transactions;
        try {
            transactions = productEJB.getTransaction(request1);
            loadGenericCombobox(transactions, cmbTransaction, "description", evenInteger, Long.valueOf(productHasChannelHasTransactionParam != null ? productHasChannelHasTransactionParam.getTransactionId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }

    private void loadCmbChannel(Integer evenInteger) {
        //cmbChannel
        EJBRequest request1 = new EJBRequest();
        List<Channel> channels;
        try {
            channels = productEJB.getChannel(request1);
            loadGenericCombobox(channels, cmbChannel, "name", evenInteger, Long.valueOf(productHasChannelHasTransactionParam != null ? productHasChannelHasTransactionParam.getChannelId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }
}
