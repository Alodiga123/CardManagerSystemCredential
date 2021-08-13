package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.commons.ejb.ProgramEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import static com.alodiga.cms.web.controllers.AdminRequestController.eventType;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Country;
import com.cms.commons.models.Currency;
import com.cms.commons.models.KindCard;
import com.cms.commons.models.LevelProduct;
import com.cms.commons.models.Product;
import com.cms.commons.models.ProductUse;
import com.cms.commons.models.Program;
import com.cms.commons.models.ProgramType;
import com.cms.commons.models.SegmentMarketing;
import com.cms.commons.models.StatusProduct;
import com.cms.commons.models.StorageMedio;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Toolbarbutton;

public class AdminProductController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private UtilsEJB utilsEJB = null;
    private ProductEJB productEJB = null;
    private ProgramEJB programEJB = null;
    private Product productParam;
    private Textbox txtName;
    private Intbox intDaysBeforeExpiration;
    private Intbox intDaysToInactivate;
    private Intbox intDaysToActivate;
    private Intbox intDaysToUse;
    private Intbox intDaysToWithdrawCard;
    private Intbox intMaximunDeactivationTimeBlocking;
    private Doublebox intMimBalance;
    private Doublebox intMaxBalance;
    private Datebox dtbBeginDateValidity;
    private Datebox dtbEndDateValidity;
    private Combobox cmbCountry;
    private Combobox cmbProgram;
    private Combobox cmbProgramType;
    private Combobox cmbKindCard;
    private Combobox cmbLevelProduct;
    private Combobox cmbProductUse;
    private Label lblDomesticCurrency;
    private Combobox cmbInternationalCurrency;
    private Combobox cmbStorageMedio;
    private Combobox cmbSegmentMarketing;
    private Radio r24Months;
    private Radio r36Months;
    private Radio r48Months;
    private Label lblIssuer;
    private Label lblProductType;
    private Label lblBinSponsor;
    private Label lblBinNumber;
    private Tab tabCommerceCategory;
    private Tab tabRestrictions;
    private Tab tabActivationProduct;
    private Button btnSave;
    public static Integer eventType;
    private Toolbarbutton tbbTitle;
    public static Product productParent = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            productParam = null;
        } else {
            productParam = (Product) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.product.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.product.view"));
                break;
            case WebConstants.EVENT_ADD:
                tabCommerceCategory.setDisabled(true);
                tabRestrictions.setDisabled(true);
                tabActivationProduct.setDisabled(true);
                break;
            default:
                break;
        }
        try {
            productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            programEJB = (ProgramEJB) EJBServiceLocator.getInstance().get(EjbConstants.PROGRAM_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        txtName.setRawValue(null);
        intDaysBeforeExpiration.setRawValue(null);
        intDaysToInactivate.setRawValue(null);
        intDaysToActivate.setRawValue(null);
        intDaysToUse.setRawValue(null);
        intDaysToWithdrawCard.setRawValue(null);
        intMaximunDeactivationTimeBlocking.setRawValue(null);
        intMimBalance.setRawValue(null);
        intMaxBalance.setRawValue(null);
        dtbBeginDateValidity.setRawValue(null);
        dtbEndDateValidity.setRawValue(null);
    }

    public Product getProductParent() {
        return productParent;
    }

    public Integer getEventType() {
        return eventType;
    }

    private void loadFields(Product product) {
        try {
            txtName.setText(product.getName());
            intDaysBeforeExpiration.setValue(product.getDaysBeforeExpiration().intValue());
            intDaysToInactivate.setValue(product.getDaysToInactivate().intValue());
            intDaysToActivate.setValue(product.getDaysToActivate().intValue());
            intDaysToUse.setValue(product.getDaysToUse().intValue());
            intDaysToWithdrawCard.setValue(product.getDaysToWithdrawCard().intValue());
            intMimBalance.setValue(product.getMinimumBalance());
            intMaxBalance.setValue(product.getMaximumBalance());
            if (product.getMaximunDeactivationTimeBlocking() != null) {
                intMaximunDeactivationTimeBlocking.setValue(product.getMaximunDeactivationTimeBlocking().intValue());
            }
            dtbBeginDateValidity.setValue(product.getBeginDateValidity());
            dtbEndDateValidity.setValue(product.getEndDateValidity());
            switch (product.getValidityMonths()) {
                case WebConstants.VALIDITY_MONTH_24:
                    r24Months.setChecked(true);
                    break;
                case WebConstants.VALIDITY_MONTH_36:
                    r36Months.setChecked(true);
                    break;
                case WebConstants.VALIDITY_MONTH_48:
                    r48Months.setChecked(true);
                    break;
            }
            loadProgramData(product.getProgramId());
            validateProductUse(product.getProductUseId().getId());
            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtName.setDisabled(true);
        r24Months.setDisabled(true);
        r36Months.setDisabled(true);
        r48Months.setDisabled(true);
        intDaysBeforeExpiration.setDisabled(true);
        intDaysToInactivate.setDisabled(true);
        intDaysToActivate.setDisabled(true);
        intDaysToUse.setDisabled(true);
        intDaysToWithdrawCard.setDisabled(true);
        intMaximunDeactivationTimeBlocking.setDisabled(true);
        intMimBalance.setDisabled(true);
        intMaxBalance.setDisabled(true);
        dtbBeginDateValidity.setDisabled(true);
        dtbEndDateValidity.setDisabled(true);
        btnSave.setVisible(false);
    }

    private void saveProduct(Product _product) throws RegisterNotFoundException, NullParameterException, GeneralException {
        int validityMonth = 0;
        try {
            Product product = null;

            if (_product != null) {
                product = _product;
            } else {//New Product
                product = new Product();
            }

            if (r24Months.isChecked()) {
                validityMonth = WebConstants.VALIDITY_MONTH_24;
            } else if (r36Months.isChecked()) {
                validityMonth = WebConstants.VALIDITY_MONTH_36;
            } else {
                validityMonth = WebConstants.VALIDITY_MONTH_48;
            }

            //Calculando la fecha de vigencia de la tarjeta
            Date fechaFinVigencia = new Date();
            Calendar feVecimiento = Calendar.getInstance();
            feVecimiento.setTime(((Datebox) dtbBeginDateValidity).getValue());
            feVecimiento.add(Calendar.MONTH, +validityMonth);
            fechaFinVigencia = feVecimiento.getTime();
            
            //Obtener estatus PENDING para asociarlo al producto
            EJBRequest request1 = new EJBRequest();
            request1.setParam(WebConstants.PRODUCT_STATUS_PENDING);
            StatusProduct statusProduct = productEJB.loadStatusProduct(request1);

            //Guardar Producto
            product.setName(txtName.getText());
            product.setCountryId((Country) cmbCountry.getSelectedItem().getValue());
            product.setIssuerId(((Program) cmbProgram.getSelectedItem().getValue()).getIssuerId());
            product.setProductTypeId(((Program) cmbProgram.getSelectedItem().getValue()).getProductTypeId());
            product.setBinSponsorId(((Program) cmbProgram.getSelectedItem().getValue()).getBinSponsorId());
            product.setBinNumber(((Program) cmbProgram.getSelectedItem().getValue()).getBiniinNumber());
            product.setKindCardId((KindCard) cmbKindCard.getSelectedItem().getValue());
            product.setProgramTypeId((ProgramType) cmbProgramType.getSelectedItem().getValue());
            product.setLevelProductId((LevelProduct) cmbLevelProduct.getSelectedItem().getValue());
            product.setProductUseId((ProductUse) cmbProductUse.getSelectedItem().getValue());
            ProductUse productUse = (ProductUse) cmbProductUse.getSelectedItem().getValue();
            if (productUse.getId() == WebConstants.PRODUCT_USE_DOMESTIC) {
                product.setDomesticCurrencyId(getCurrencyByCountry());
            }
            if (productUse.getId() == WebConstants.PRODUCT_USE_INTERNATIONAL) {
                product.setInternationalCurrencyId((Currency) cmbInternationalCurrency.getSelectedItem().getValue());
            }
            if (productUse.getId() == WebConstants.PRODUCT_USE_BOTH) {
                product.setDomesticCurrencyId(getCurrencyByCountry());
                product.setInternationalCurrencyId((Currency) cmbInternationalCurrency.getSelectedItem().getValue());
            }
            product.setStorageMedioid((StorageMedio) cmbStorageMedio.getSelectedItem().getValue());
            product.setDaysBeforeExpiration(intDaysBeforeExpiration.getValue());
            product.setDaysToInactivate(intDaysToInactivate.getValue());
            product.setDaysToActivate(intDaysToActivate.getValue());
            product.setDaysToUse(intDaysToUse.getValue());
            product.setDaysToWithdrawCard(intDaysToWithdrawCard.getValue());
            product.setBeginDateValidity((dtbBeginDateValidity.getValue()));
            product.setEndDateValidity(fechaFinVigencia);
            product.setsegmentMarketingId((SegmentMarketing) cmbSegmentMarketing.getSelectedItem().getValue());
            product.setProgramId((Program) cmbProgram.getSelectedItem().getValue());
            product.setMaximunDeactivationTimeBlocking(intMaximunDeactivationTimeBlocking.getValue());
            product.setMinimumBalance(intMimBalance.getValue().floatValue());
            product.setMaximumBalance(intMaxBalance.getValue().floatValue());
            product.setValidityMonths(validityMonth);
            if (eventType == WebConstants.EVENT_ADD) {
                product.setCreateDate(new Timestamp(new Date().getTime()));
            } else {
                product.setUpdatedate(new Timestamp(new Date().getTime()));
            }
            product.setStatusProductId(statusProduct);
            product = productEJB.saveProduct(product);

            productParam = product;
            productParent = product;
            loadFields(productParam);
            this.showMessage("sp.common.save.success", false, null);
            if (eventType == WebConstants.EVENT_ADD) {
                btnSave.setVisible(false);
            } else {
                btnSave.setVisible(true);
            }

            tabCommerceCategory.setDisabled(false);
            tabRestrictions.setDisabled(false);
            tabActivationProduct.setDisabled(false);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public Boolean validateEmpty() {
        
        if (cmbCountry.getSelectedItem() == null) {
            cmbCountry.setFocus(true);
            this.showMessage("cms.error.country.notSelected", true, null);
        } else if (cmbProgramType.getSelectedItem() == null) {
            cmbProgramType.setFocus(true);
            this.showMessage("cms.error.programType.notSelected", true, null);
        } else if (cmbProgram.getSelectedItem() == null) {
            cmbProgram.setFocus(true);
            this.showMessage("cms.error.program.notSelected", true, null);
        } else if (txtName.getText().isEmpty()) {
            txtName.setFocus(true);
            this.showMessage("cms.error.field.product", true, null);
        } else if (cmbKindCard.getSelectedItem() == null) {
            cmbKindCard.setFocus(true);
            this.showMessage("cms.error.kindCard.notSelected", true, null);
        } else if (cmbLevelProduct.getSelectedItem() == null) {
            cmbLevelProduct.setFocus(true);
            this.showMessage("cms.error.levelProduct.notSelected", true, null);
        } else if (cmbProductUse.getSelectedItem() == null) {
            cmbProductUse.setFocus(true);
            this.showMessage("cms.error.use.notSelected", true, null);
        } else if ((!r24Months.isChecked()) && (!r36Months.isChecked()) && (!r48Months.isChecked())) {
            this.showMessage("cms.error.validityMonths", true, null);
        } else if (cmbStorageMedio.getSelectedItem() == null) {
            cmbStorageMedio.setFocus(true);
            this.showMessage("cms.error.storageMedio.noSelected", true, null);
        } else if (intDaysBeforeExpiration.getText().isEmpty()) {
            intDaysBeforeExpiration.setFocus(true);
            this.showMessage("cms.error.field.intDaysBeforeExpiration", true, null);
        } else if (intDaysToInactivate.getText().isEmpty()) {
            intDaysToInactivate.setFocus(true);
            this.showMessage("cms.error.field.daysToInactivate", true, null);
        } else if (intDaysToActivate.getText().isEmpty()) {
            intDaysToActivate.setFocus(true);
            this.showMessage("cms.error.field.daysToActivate", true, null);
        } else if (intDaysToUse.getText().isEmpty()) {
            intDaysToUse.setFocus(true);
            this.showMessage("cms.error.field.daysToUse", true, null);
        } else if (intDaysToWithdrawCard.getText().isEmpty()) {
            intDaysToWithdrawCard.setFocus(true);
            this.showMessage("cms.error.field.daysToToWithdrawCard", true, null);
        } else if (intMaximunDeactivationTimeBlocking.getText().isEmpty()) {
            intMaximunDeactivationTimeBlocking.setFocus(true);
            this.showMessage("cms.error.maximunDeactivationTimeBlocking", true, null);
        } else if (dtbBeginDateValidity.getText().isEmpty()) {
            dtbBeginDateValidity.setFocus(true);
            this.showMessage("cms.error.field.beginDate", true, null);
        } else if (cmbSegmentMarketing.getSelectedItem() == null) {
            cmbSegmentMarketing.setFocus(true);
            this.showMessage("cms.error.segmentMarketing.noSelected", true, null);
        } else if(intMimBalance.getValue() > intMaxBalance.getValue()){
            this.showMessage("cms.error.mimBalanceHigher.Than.maxBalance", true, null);
        } else {
            return true;
        }
        return false;
    }
    
    public Boolean validateProduct() {
        Date today = new Date();
        if (today.compareTo(dtbBeginDateValidity.getValue()) > 0) {
            dtbBeginDateValidity.setFocus(true);
            this.showMessage("cms.error.date.beginDateValidity", true, null);
        } else {
            return true;
        }
        return false;
    }

    public void onClick$btnSave() throws RegisterNotFoundException, NullParameterException, GeneralException {
        this.clearMessage();
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    if (validateProduct()) {
                        saveProduct(null);
                    }
                    break;
                case WebConstants.EVENT_EDIT:
                    saveProduct(productParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void onChange$cmbProgramType() {
        cmbProgram.setVisible(true);
        ProgramType programType = (ProgramType) cmbProgramType.getSelectedItem().getValue();
        loadCmbProgram(eventType, programType.getId());
    }

    public void onChange$cmbProgram() {
        Program program = (Program) cmbProgram.getSelectedItem().getValue();
        lblIssuer.setVisible(true);
        lblProductType.setVisible(true);
        lblBinSponsor.setVisible(true);
        lblBinNumber.setVisible(true);
        loadProgramData(program);
    }

    public void loadProgramData(Program program) {
        lblIssuer.setValue(program.getIssuerId().getName());
        lblProductType.setValue(program.getProductTypeId().getName());
        lblBinSponsor.setValue(program.getBinSponsorId().getDescription());
        lblBinNumber.setValue(program.getBiniinNumber());
    }
    
    public Currency getCurrencyByCountry() {
        Currency currencyByCountry = null;
        try {
            Country country = (Country) cmbCountry.getSelectedItem().getValue();
            EJBRequest request = new EJBRequest();
            HashMap params = new HashMap();
            params.put(Constants.COUNTRY_KEY, country.getId());
            request.setParams(params);
            currencyByCountry = utilsEJB.loadCurrencyByCountry(request);
        } catch (RegisterNotFoundException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        } 
        return currencyByCountry;
    }        
    
    public void onChange$cmbCountry() {
        if (cmbProductUse.getSelectedItem() != null) {
            ProductUse productUse = (ProductUse) cmbProductUse.getSelectedItem().getValue();
            validateProductUse(productUse.getId());
        }
    }

    public void onChange$cmbProductUse() {
        ProductUse productUse = (ProductUse) cmbProductUse.getSelectedItem().getValue();
        validateProductUse(productUse.getId()); 
    }

    public void validateProductUse(int productUseId) {
            switch (productUseId) {
                case 1:
                    lblDomesticCurrency.setValue(getCurrencyByCountry().getName());
                    cmbInternationalCurrency.setValue("");
                    cmbInternationalCurrency.setDisabled(true);
                    break;
                case 2:
                    lblDomesticCurrency.setValue("");
                    cmbInternationalCurrency.setDisabled(false);
                    break;
                case 3:
                    lblDomesticCurrency.setValue(getCurrencyByCountry().getName());
                    cmbInternationalCurrency.setDisabled(false);
                    break;
            }          
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                productParent = productParam;
                dtbEndDateValidity.setDisabled(true);
                loadCmbCountry(eventType);
                loadFields(productParam);
                loadCmbKindCard(eventType);
                loadCmbProgramType(eventType);
                loadCmbLevelProduct(eventType);
                loadCmbProductUse(eventType);
                loadCmbInternationalCurrency(eventType);
                loadCmbStorageMedio(eventType);
                loadCmbSegmentMarketing(eventType);
                onChange$cmbProgramType();
                break;
            case WebConstants.EVENT_VIEW:
                productParent = productParam;
                blockFields();
                loadCmbCountry(eventType);
                loadFields(productParam);
                loadCmbKindCard(eventType);
                loadCmbProgramType(eventType);
                loadCmbLevelProduct(eventType);
                loadCmbProductUse(eventType);
                loadCmbInternationalCurrency(eventType);
                loadCmbStorageMedio(eventType);
                loadCmbSegmentMarketing(eventType);
                onChange$cmbProgramType();
                break;
            case WebConstants.EVENT_ADD:
                dtbEndDateValidity.setDisabled(true);
                loadCmbCountry(eventType);
                loadCmbKindCard(eventType);
                loadCmbProgramType(eventType);
                loadCmbLevelProduct(eventType);
                loadCmbProductUse(eventType);
                loadCmbInternationalCurrency(eventType);
                loadCmbStorageMedio(eventType);
                loadCmbSegmentMarketing(eventType);
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
            loadGenericCombobox(countries, cmbCountry, "name", evenInteger, Long.valueOf(productParam != null ? productParam.getCountryId().getId() : 0));
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

    private void loadCmbKindCard(Integer eventType) {
        EJBRequest request1 = new EJBRequest();
        List<KindCard> kindCardList;
        try {
            kindCardList = utilsEJB.getKindCard(request1);
            loadGenericCombobox(kindCardList, cmbKindCard, "description", eventType, Long.valueOf(productParam != null ? productParam.getKindCardId().getId() : 0));
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

    private void loadCmbProgramType(Integer eventType) {
        EJBRequest request1 = new EJBRequest();
        List<ProgramType> programTypeList;
        try {
            programTypeList = utilsEJB.getProgramType(request1);
            loadGenericCombobox(programTypeList, cmbProgramType, "name", eventType, Long.valueOf(productParam != null ? productParam.getProgramTypeId().getId() : 0));
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

    private void loadCmbLevelProduct(Integer eventType) {
        EJBRequest request1 = new EJBRequest();
        List<LevelProduct> levelProductList;
        try {
            levelProductList = productEJB.getLevelProduct(request1);
            loadGenericCombobox(levelProductList, cmbLevelProduct, "description", eventType, Long.valueOf(productParam != null ? productParam.getLevelProductId().getId() : 0));
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

    private void loadCmbProductUse(Integer eventType) {
        EJBRequest request1 = new EJBRequest();
        List<ProductUse> productUseList;
        try {
            productUseList = productEJB.getProductUse(request1);
            loadGenericCombobox(productUseList, cmbProductUse, "description", eventType, Long.valueOf(productParam != null ? productParam.getProductUseId().getId() : 0));
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

    private void loadCmbInternationalCurrency(Integer eventType) {
        EJBRequest request1 = new EJBRequest();
        List<Currency> internationalCurrencyList;
        try {
            internationalCurrencyList = utilsEJB.getCurrency(request1);
            if (eventType == WebConstants.EVENT_ADD) {
                loadGenericCombobox(internationalCurrencyList, cmbInternationalCurrency, "name", eventType, Long.valueOf(productParam != null ? productParam.getInternationalCurrencyId().getId() : 0));
            } else {
                loadGenericCombobox(internationalCurrencyList, cmbInternationalCurrency, "name", eventType, Long.valueOf(productParam.getInternationalCurrencyId() != null ? productParam.getInternationalCurrencyId().getId() : 0));
            }
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

    private void loadCmbStorageMedio(Integer eventType) {
        EJBRequest request1 = new EJBRequest();
        List<StorageMedio> storageMedioList;
        try {
            storageMedioList = productEJB.getStorageMedio(request1);
            loadGenericCombobox(storageMedioList, cmbStorageMedio, "description", eventType, Long.valueOf(productParam != null ? productParam.getStorageMedioid().getId() : 0));
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

    private void loadCmbSegmentMarketing(Integer eventType) {
        EJBRequest request1 = new EJBRequest();
        List<SegmentMarketing> segmentMarketingList;
        try {
            segmentMarketingList = productEJB.getSegmentMarketing(request1);
            loadGenericCombobox(segmentMarketingList, cmbSegmentMarketing, "name", eventType, Long.valueOf(productParam != null ? productParam.getsegmentMarketingId().getId() : 0));
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

    private void loadCmbProgram(Integer evenInteger, int programTypeId) {
        EJBRequest request1 = new EJBRequest();
        cmbProgram.getItems().clear();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_PROGRAM_TYPE_ID, programTypeId);
        request1.setParams(params);
        List<Program> programList;
        try {
            programList = programEJB.getProgramByProgramType(request1);
            loadGenericCombobox(programList, cmbProgram, "name", evenInteger, Long.valueOf(productParam != null ? productParam.getProgramId().getId() : 0));
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

    private void setText(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
