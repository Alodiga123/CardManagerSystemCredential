package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Transaction;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;

public class AdminTransactionController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private ProductEJB productEJB = null;
    private Textbox txtCodeTransaction;
    private Textbox txtDescriptionTransaction;
    private Radio rMonetaryTypeYes;
    private Radio rMonetaryTypeNo;
    private Radio rTransactionPurchaseYes;
    private Radio rTransactionPurchaseNo;
    private Radio rVariationRateChannelYes;
    private Radio rVariationRateChannelNo;
    private Transaction transactionParam;
    private Button btnSave;
    private Integer eventType;
    private Toolbarbutton tbbTitle;
    List<Transaction> transactionList = new ArrayList<Transaction>();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        Sessions.getCurrent();
        transactionParam = (Sessions.getCurrent().getAttribute("object") != null) ? (Transaction) Sessions.getCurrent().getAttribute("object") : null;
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            transactionParam = null;
        } else {
            transactionParam = (Transaction) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.transaction.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.transaction.view"));
                break;
            default:
                break;
        }
        try {
            productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        txtCodeTransaction.setRawValue(null);
        txtDescriptionTransaction.setRawValue(null);
    }

    private void loadFields(Transaction transaction) {
        try {
            txtCodeTransaction.setText(transaction.getCode().toString());
            txtDescriptionTransaction.setText(transaction.getDescription().toString());
            if (transaction.getIndMonetaryType() == true) {
                rMonetaryTypeYes.setChecked(true);
            } else {
                rMonetaryTypeNo.setChecked(true);
            }
            if (transaction.getIndTransactionPurchase() == true) {
                rTransactionPurchaseYes.setChecked(true);
            } else {
                rTransactionPurchaseNo.setChecked(true);
            }
            if (transaction.getIndVariationRateChannel() == true) {
                rVariationRateChannelYes.setChecked(true);
            } else {
                rVariationRateChannelNo.setChecked(true);
            }
            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }

    }

    public void blockFields() {
        txtCodeTransaction.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtCodeTransaction.getText().isEmpty()) {
            txtCodeTransaction.setFocus(true);
            this.showMessage("msj.error.fieldCodeEmpty", true, null);
        } else if (txtDescriptionTransaction.getText().isEmpty()) {
            txtDescriptionTransaction.setFocus(true);
            this.showMessage("cms.error.description", true, null);
        } else if ((!rMonetaryTypeYes.isChecked()) && (!rMonetaryTypeNo.isChecked())) {
            this.showMessage("cms.error.field.monetaryType", true, null);
        } else if ((!rTransactionPurchaseYes.isChecked()) && (!rTransactionPurchaseNo.isChecked())) {
            this.showMessage("cms.error.field.transactionPurchase", true, null);
        } else if ((!rVariationRateChannelYes.isChecked()) && (!rVariationRateChannelNo.isChecked())) {
            this.showMessage("cms.error.field.variationRateChannel", true, null);
        } else {
            return true;
        }
        return false;

    }
    
    public boolean validateTransaction(){
        transactionList.clear();
        try {
            //Valida si el code ingresado ya existe en BD
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.PARAM_USER, txtCodeTransaction.getValue());
            request1.setParams(params);
            transactionList = productEJB.getTransactionByCode(request1); 
        } catch (Exception ex) {
            showError(ex);
        } finally {
                if (transactionList.size() > 0) {
                    this.showMessage("cms.error.field.codeExistInBdTransactions", true, null);
                    txtCodeTransaction.setFocus(true);
                    return false;
                }
            }
        return true;
    }
    
    private void saveTransaction(Transaction _transaction) {
        Boolean indMonetaryType = true;
        Boolean indTransactionPurchase = true;
        Boolean indVariationRateChannel = true;
        try {
            Transaction transaction = null;
            if (_transaction != null) {
                transaction = _transaction;
            } else {
                transaction = new Transaction();
            }

            if (rMonetaryTypeYes.isChecked()) {
                indMonetaryType = true;
            } else {
                indMonetaryType = false;
            }
            if (rTransactionPurchaseYes.isChecked()) {
                indTransactionPurchase = true;
            } else {
                indTransactionPurchase = false;
            }
            if (rVariationRateChannelYes.isChecked()) {
                indVariationRateChannel = true;
            } else {
                indVariationRateChannel = false;
            }
            transaction.setCode(txtCodeTransaction.getText());
            transaction.setDescription(txtDescriptionTransaction.getText());
            transaction.setIndMonetaryType(indMonetaryType);
            transaction.setIndTransactionPurchase(indTransactionPurchase);
            transaction.setIndVariationRateChannel(indVariationRateChannel);
            transaction = productEJB.saveTransaction(transaction);
            transactionParam = transaction;
            this.showMessage("sp.common.save.success", false, null);
            btnSave.setDisabled(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    if(validateTransaction()){
                       saveTransaction(null); 
                    }
                    break;
                case WebConstants.EVENT_EDIT:
                    saveTransaction(transactionParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(transactionParam);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(transactionParam);
                txtCodeTransaction.setReadonly(true);
                txtDescriptionTransaction.setReadonly(true);
                blockFields();
                rMonetaryTypeYes.setDisabled(true);
                rMonetaryTypeNo.setDisabled(true);
                rTransactionPurchaseYes.setDisabled(true);
                rTransactionPurchaseNo.setDisabled(true);
                rVariationRateChannelYes.setDisabled(true);
                rVariationRateChannelNo.setDisabled(true);
                break;
            case WebConstants.EVENT_ADD:
                loadFields(transactionParam);
                break;
            default:
                break;
        }
    }

}
