package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.commons.ejb.ProgramEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Channel;
import com.cms.commons.models.ProgramLoyalty;
import com.cms.commons.models.ProgramLoyaltyTransaction;
import com.cms.commons.models.Transaction;
import com.cms.commons.enumeraciones.TransactionE;
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
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class AdminParametersController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;

    private Label lblLoyalty;
    private Label lblTitle;
    private Label lblLoyaltyProgramType;
    private Label lblIndBonificationFixed;
    private Combobox cmbChannel;
    private Combobox cmbTransaction;
    private Doublebox txtTotal;
    private Doublebox txtTotalMaximumTransactions;
    private Doublebox txtTotalAmountDaily;
    private Doublebox txtTotalAmountMonthly;
    private Radio rBonificationYes;
    private Radio rBonificationNo;
    private Tab tabCommerce;
    private ProductEJB productEJB = null;
    private ProgramEJB programEJB = null;
    private ProgramLoyaltyTransaction programLoyaltyTransactionParam;
    private Button btnSave;
    private Integer eventType;
    public static ProgramLoyaltyTransaction programLoyaltyTransactionParent = null;
    public Window winAdminParameters;
    public Window winTabParametersAndComerce;
    private ProgramLoyalty programLoyalty;
    private AdminLoyaltyController adminLoyalty;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        AdminLoyaltyController adminLoyalty = new AdminLoyaltyController();
        if (adminLoyalty.getProgramLoyaltyParent().getId() != null) {
            programLoyalty = adminLoyalty.getProgramLoyaltyParent();
        }
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                programLoyaltyTransactionParam = (ProgramLoyaltyTransaction) Sessions.getCurrent().getAttribute("object");
                if (programLoyaltyTransactionParam.getTransactionId().getIndTransactionPurchase() != null) {
                    tabCommerce.setDisabled(false);
                } else {
                    tabCommerce.setDisabled(true);
                }
                break;
            case WebConstants.EVENT_VIEW:
                programLoyaltyTransactionParam = (ProgramLoyaltyTransaction) Sessions.getCurrent().getAttribute("object");
                if (programLoyaltyTransactionParam.getTransactionId().getIndTransactionPurchase() != null) {
                    tabCommerce.setDisabled(false);
                } else {
                    tabCommerce.setDisabled(true);
                }
                break;
            case WebConstants.EVENT_ADD:
                programLoyaltyTransactionParam = null;
                tabCommerce.setDisabled(true);
                break;
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                winTabParametersAndComerce.setTitle(Labels.getLabel("cms.crud.loyalty.commerceCategory.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                winTabParametersAndComerce.setTitle(Labels.getLabel("cms.crud.loyalty.commerceCategory.view"));
                break;
            case WebConstants.EVENT_ADD:
                winTabParametersAndComerce.setTitle(Labels.getLabel("cms.crud.loyalty.commerceCategory.add"));
                break;
            default:
                break;
        }
        try {
            productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
            programEJB = (ProgramEJB) EJBServiceLocator.getInstance().get(EjbConstants.PROGRAM_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        txtTotal.setRawValue(null);
        txtTotalMaximumTransactions.setRawValue(null);
        txtTotalAmountDaily.setRawValue(null);
        txtTotalAmountMonthly.setRawValue(null);
    }

    public void onClick$rBonificationYes() {
        txtTotalMaximumTransactions.setDisabled(false);
        txtTotalAmountDaily.setDisabled(false);
        txtTotalAmountMonthly.setDisabled(false);
    }

    public void onClick$rBonificationNo() {
        txtTotalMaximumTransactions.setDisabled(true);
        txtTotalAmountDaily.setDisabled(false);
        txtTotalAmountMonthly.setDisabled(false);
    }

    public void onChange$cmbTransaction() {
        String indMonetaryTypeTrue = WebConstants.ID_MONETARY_TYPE_TRUE;
        String indMonetaryTypeFalse = WebConstants.ID_MONETARY_TYPE_FALSE;
        String cadena1 = (((Transaction) cmbTransaction.getSelectedItem().getValue()).getIndMonetaryType().toString());
        String cadena2 = (((Transaction) cmbTransaction.getSelectedItem().getValue()).getIndTransactionPurchase().toString());
        String cadena3 = (((Transaction) cmbTransaction.getSelectedItem().getValue()).getIndVariationRateChannel().toString());

        if (cadena1.equals(indMonetaryTypeTrue)) {
            txtTotal.setDisabled(false);
            txtTotalAmountDaily.setDisabled(false);
            txtTotalAmountMonthly.setDisabled(false);
            txtTotalMaximumTransactions.setDisabled(false);
        } else if (cadena1.equals(indMonetaryTypeFalse)) {
            if (programLoyalty.getProgramLoyaltyTypeId().getId() == WebConstants.PROGRAM_LOYALTY_TYPE_POINT) {
                lblTitle.setValue(Labels.getLabel("cms.crud.loyalty.parameters.totalPoint"));
                txtTotal.setDisabled(false);
                txtTotalAmountDaily.setDisabled(true);
                txtTotalAmountMonthly.setDisabled(true);
                txtTotalMaximumTransactions.setDisabled(true);
            } else if (programLoyalty.getProgramLoyaltyTypeId().getId() == WebConstants.PROGRAM_LOYALTY_TYPE_BONIFICATION) {
                lblTitle.setValue(Labels.getLabel("cms.crud.loyalty.parameters.totalBonification"));
                txtTotal.setDisabled(false);
                txtTotalAmountDaily.setDisabled(true);
                txtTotalAmountMonthly.setDisabled(true);
                txtTotalMaximumTransactions.setDisabled(true);
            }
        }
    }

    public ProgramLoyaltyTransaction getProgramLoyaltyTransactionParent() {
        return programLoyaltyTransactionParent;
    }
    
    public Boolean validateEmpty() {
        Transaction transaction = (Transaction) cmbTransaction.getSelectedItem().getValue();        
        switch (transaction.getId()) { 
            case WebConstants.RECARGA:
            case WebConstants.RETIRO_DOMESTICO:
            case WebConstants.RETIRO_INTERNACIONAL:
            case WebConstants.COMPRA_DOMESTICA_PIN:
            case WebConstants.COMPRA_INTERNACIONAL_PIN:
            case WebConstants.DEPOSITO:
            case WebConstants.TRANSFERENCIAS_PROPIAS:
            case WebConstants.RECARGA_MANUAL:
                if (txtTotalMaximumTransactions.getText().isEmpty()) {
                    txtTotalMaximumTransactions.setFocus(true);
                    this.showMessage("cms.error.emptyTotalMaximumTransactions", true, null);
                    return false;
                } else if (txtTotalAmountDaily.getText().isEmpty()) {
                    txtTotalAmountDaily.setFocus(true);
                    this.showMessage("cms.error.emptyTotalAmountDaily", true, null);
                    return false;
                } else if (txtTotalAmountMonthly.getText().isEmpty()) {
                    txtTotalAmountMonthly.setFocus(true);
                    this.showMessage("cms.error.emptyTotalAmountMonthly", true, null);
                    return false;
                } else if (txtTotal.getText().isEmpty()) {
                    txtTotal.setFocus(true);
                    this.showMessage("cms.error.emptyTotalPointsBonification", true, null);
                    return false;
                } else {
                    return true;
                }               
        } 
        if (cmbTransaction.getSelectedItem() == null) {
            cmbTransaction.setFocus(true);
            this.showMessage("cms.error.transaction.notSelected", true, null);
        } else if (cmbChannel.getSelectedItem() == null) {
            cmbChannel.setFocus(true);
            this.showMessage("cms.error.channel.notSelected", true, null);
        } else if (programLoyalty.getProgramLoyaltyTypeId().getId() == WebConstants.PROGRAM_LOYALTY_TYPE_BONIFICATION && (!rBonificationYes.isChecked()) && (!rBonificationNo.isChecked())) {
            this.showMessage("cms.error.emptyIndBonificationPoints", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void loadField(ProgramLoyaltyTransaction programLoyaltyTransaction) {
        if (programLoyaltyTransaction == null) {
            lblLoyalty.setValue(programLoyalty.getDescription());
            lblLoyaltyProgramType.setValue(programLoyalty.getProgramLoyaltyTypeId().getName());
            if (programLoyalty.getProgramLoyaltyTypeId().getId() == WebConstants.PROGRAM_LOYALTY_TYPE_POINT) {
                lblIndBonificationFixed.setValue("");
                lblTitle.setValue(Labels.getLabel("cms.crud.loyalty.parameters.totalPoint"));
                rBonificationYes.setVisible(false);
                rBonificationNo.setVisible(false);
            } else {
                lblIndBonificationFixed.setValue(Labels.getLabel("cms.crud.loyalty.indBonificationFixed"));
                lblTitle.setValue(Labels.getLabel("cms.crud.loyalty.parameters.totalBonification"));
                rBonificationYes.setVisible(true);
                rBonificationNo.setVisible(true);
            }
        } else {
            lblLoyalty.setValue(programLoyaltyTransaction.getProgramLoyaltyId().getDescription());
            lblLoyaltyProgramType.setValue(programLoyaltyTransaction.getProgramLoyaltyId().getProgramLoyaltyTypeId().getName());
            if (programLoyaltyTransaction.getTotalMaximumTransactions() != null) {
                txtTotalMaximumTransactions.setValue(programLoyaltyTransaction.getTotalMaximumTransactions());
            }      
            if (programLoyaltyTransaction.getTotalAmountDaily() != null) {
                txtTotalAmountDaily.setValue(programLoyaltyTransaction.getTotalAmountDaily());
            }
            if (programLoyaltyTransaction.getTotalAmountMonthly() != null) {
                txtTotalAmountMonthly.setValue(programLoyaltyTransaction.getTotalAmountMonthly());
            }          
            if (programLoyaltyTransaction.getProgramLoyaltyId().getProgramLoyaltyTypeId().getId() == WebConstants.PROGRAM_LOYALTY_TYPE_POINT) {
                lblTitle.setValue(Labels.getLabel("cms.crud.loyalty.parameters.totalPoint"));                
                lblIndBonificationFixed.setValue("");
                rBonificationYes.setVisible(false);
                rBonificationNo.setVisible(false);
                txtTotal.setValue(programLoyaltyTransaction.getTotalPointsValue());
            } else if (programLoyaltyTransaction.getProgramLoyaltyId().getProgramLoyaltyTypeId().getId() == WebConstants.PROGRAM_LOYALTY_TYPE_BONIFICATION) {
                lblTitle.setValue(Labels.getLabel("cms.crud.loyalty.parameters.totalBonification"));
                txtTotal.setValue(programLoyaltyTransaction.getTotalBonificationValue());
                lblIndBonificationFixed.setValue(Labels.getLabel("cms.crud.loyalty.indBonificationFixed"));
                if (programLoyaltyTransaction.getIndBonificationFixed() == true) {
                    rBonificationYes.setChecked(true);
                } else {
                    rBonificationNo.setChecked(true);
                }
            }
            programLoyaltyTransactionParent = programLoyaltyTransaction;
        }   
        btnSave.setVisible(true);
    }

    public void blockFields() {
        txtTotal.setReadonly(true);
        txtTotalMaximumTransactions.setReadonly(true);
        txtTotalAmountDaily.setReadonly(true);
        txtTotalAmountMonthly.setReadonly(true);
        btnSave.setVisible(false);
    }

    private void saveProgramLoyaltyTransactionParam(ProgramLoyaltyTransaction _programLoyaltyTransaction) throws RegisterNotFoundException, NullParameterException, GeneralException {
        ProgramLoyaltyTransaction programLoyaltyTransaction = null;
        List<ProgramLoyaltyTransaction> programLoyaltyTransactionUnique = null;
        ProgramLoyalty programLoyalty = null;

        try {
            if (_programLoyaltyTransaction != null) {
                programLoyaltyTransaction = _programLoyaltyTransaction;
            } else {
                programLoyaltyTransaction = new ProgramLoyaltyTransaction();
            }

            //Loyalty
            AdminLoyaltyController adminLoyalty = new AdminLoyaltyController();
            if (adminLoyalty.getProgramLoyaltyParent().getId() != null) {
                programLoyalty = adminLoyalty.getProgramLoyaltyParent();
            }

            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.CHANNEL_KEY, ((Channel) cmbChannel.getSelectedItem().getValue()).getId());
            params.put(Constants.PROGRAM_LOYALTY_KEY, programLoyalty.getId());
            params.put(Constants.TRANSACTION_KEY, ((Transaction) cmbTransaction.getSelectedItem().getValue()).getId());
            request1.setParams(params);

            programLoyaltyTransactionUnique = programEJB.getProgramLoyaltyTransactionUnique(request1);
            if (programLoyaltyTransactionUnique != null) {
                switch (eventType) {
                    case WebConstants.EVENT_ADD:
                        this.showMessage("cms.common.RegisterExistInBD", false, null);
                    break;
                    case WebConstants.EVENT_EDIT:
                        buildProgramLoyaltyTransaction(programLoyalty,programLoyaltyTransaction);
                        programLoyaltyTransaction = programEJB.saveProgramLoyaltyTransaction(programLoyaltyTransaction);
                        programLoyaltyTransactionParam = programLoyaltyTransaction;
                        programLoyaltyTransactionParent = programLoyaltyTransaction;
                        this.showMessage("sp.common.save.success", false, null);
                    break;
                    default:
                    break;
                }
            }
            btnSave.setVisible(false);

        } catch (Exception ex) {
            showError(ex);
        } finally {
            if (eventType == 1 && programLoyaltyTransactionUnique == null) {
                programLoyaltyTransaction = new ProgramLoyaltyTransaction();
                buildProgramLoyaltyTransaction(programLoyalty,programLoyaltyTransaction);
                programLoyaltyTransaction = programEJB.saveProgramLoyaltyTransaction(programLoyaltyTransaction);
                programLoyaltyTransactionParam = programLoyaltyTransaction;
                programLoyaltyTransactionParent = programLoyaltyTransaction;
                this.showMessage("sp.common.save.success", false, null);
                if (programLoyaltyTransactionParam.getTransactionId().getIndTransactionPurchase() == true) {
                    tabCommerce.setDisabled(false);
                } else {
                    tabCommerce.setDisabled(true);
                }
            }
            btnSave.setVisible(false);
            EventQueues.lookup("updateParameters", EventQueues.APPLICATION, true).publish(new Event(""));
        }
    }
    
    public void buildProgramLoyaltyTransaction(ProgramLoyalty programLoyalty, ProgramLoyaltyTransaction programLoyaltyTransaction) {
        boolean IndBonificationFixed = true;        
        if (rBonificationYes.isChecked()) {
                IndBonificationFixed = true;
            } else {
                IndBonificationFixed = false;
            }
        programLoyaltyTransaction.setChannelId((Channel) cmbChannel.getSelectedItem().getValue());
        programLoyaltyTransaction.setProgramLoyaltyId(programLoyalty);
        programLoyaltyTransaction.setTransactionId((Transaction) cmbTransaction.getSelectedItem().getValue());
        if (programLoyalty.getProgramLoyaltyTypeId().getId() == WebConstants.PROGRAM_LOYALTY_TYPE_POINT) {
            programLoyaltyTransaction.setTotalPointsValue(txtTotal.getValue().floatValue());
        } else if (programLoyalty.getProgramLoyaltyTypeId().getId() == WebConstants.PROGRAM_LOYALTY_TYPE_BONIFICATION) {
            programLoyaltyTransaction.setTotalBonificationValue(txtTotal.getValue().floatValue());
        }
        if (txtTotalMaximumTransactions.getValue() != null) {
            programLoyaltyTransaction.setTotalMaximumTransactions(txtTotalMaximumTransactions.getValue().floatValue());
        }
        if (txtTotalAmountDaily.getValue() != null) {
            programLoyaltyTransaction.setTotalAmountDaily(txtTotalAmountDaily.getValue().floatValue());
        }
        if (txtTotalAmountMonthly.getValue() != null) {
            programLoyaltyTransaction.setTotalAmountMonthly(txtTotalAmountMonthly.getValue().floatValue());
        }           
        programLoyaltyTransaction.setIndBonificationFixed(IndBonificationFixed);
    }

    public void onClick$btnSave() throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveProgramLoyaltyTransactionParam(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveProgramLoyaltyTransactionParam(programLoyaltyTransactionParam);
                    break;
                default:
                    break;
            }
        }        
    }

    public void onClick$btnBack() {
        winAdminParameters.detach();
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadField(programLoyaltyTransactionParam);
                loadCmbTransaction(WebConstants.EVENT_VIEW);
                loadCmbChannel(WebConstants.EVENT_VIEW);
                break;
            case WebConstants.EVENT_VIEW:
                loadField(programLoyaltyTransactionParam);
                blockFields();
                loadCmbTransaction(eventType);
                loadCmbChannel(eventType);
                break;
            case WebConstants.EVENT_ADD:
                loadField(programLoyaltyTransactionParam);
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
            loadGenericCombobox(transactions, cmbTransaction, "description", evenInteger, Long.valueOf(programLoyaltyTransactionParam != null ? programLoyaltyTransactionParam.getTransactionId().getId() : 0));
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
            loadGenericCombobox(channels, cmbChannel, "name", evenInteger, Long.valueOf(programLoyaltyTransactionParam != null ? programLoyaltyTransactionParam.getChannelId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }
}
