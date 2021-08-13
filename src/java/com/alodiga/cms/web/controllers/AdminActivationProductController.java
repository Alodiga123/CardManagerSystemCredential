package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.commons.ejb.ProgramEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Product;
import com.cms.commons.models.StatusProduct;
import com.cms.commons.models.User;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.sql.Timestamp;
import java.util.Date;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;


public class AdminActivationProductController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private ProductEJB productEJB = null;
    private UtilsEJB utilsEJB = null;
    private ProgramEJB programEJB = null;
    private Label lblProduct;
    private Label lblAgency;
    private Label lblUserActivation;
    private Label lblIdentification;
    private Label lblCity;
    private Datebox dtbActivationDate;
    private Radio rActivationYes;
    private Radio rActivationNo;
    private Textbox txtObservations;
    private User user = null;
    private Product productParam;
    private Button btnSave;
    public Window winAdminActivationProduct;
    private AdminProductController adminProduct = null;
    
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        adminProduct = new AdminProductController();
        eventType = adminProduct.getEventType();
        productParam = adminProduct.getProductParent();         
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        user = (User) session.getAttribute(Constants.USER_OBJ_SESSION);
        productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
        loadData();
    }

    public void clearFields() {
        dtbActivationDate.setRawValue(null);
    }
    
    private void loadDate() {
        Date today = new Date();
        dtbActivationDate.setValue(today);
    }
    
    private void loadFields(Product product) {
        try {
            lblProduct.setValue(product.getName());
            if (product.getIndActivation() != null ) {
                lblCity.setValue(user.getComercialAgencyId().getCityId().getName());
                lblAgency.setValue(product.getUserActivationId().getComercialAgencyId().getCityId().getName());
                lblUserActivation.setValue(product.getUserActivationId().getFirstNames() + " " + product.getUserActivationId().getLastNames());
                lblIdentification.setValue(product.getUserActivationId().getIdentificationNumber());
                if (product.getActivationDate() != null) {
                    dtbActivationDate.setValue(product.getActivationDate());
                }
                if (product.getObservations() != null) {
                    txtObservations.setText(product.getObservations());
                }
                if (product.getIndActivation() != null) {
                    if (product.getIndActivation() == true) {
                        rActivationYes.setChecked(true);    
                    } else {
                        rActivationNo.setChecked(true);
                    }
                }
            } else {
                lblCity.setValue(user.getComercialAgencyId().getCityId().getName());
                lblAgency.setValue(user.getComercialAgencyId().getName());
                lblUserActivation.setValue(user.getFirstNames() + " " + user.getLastNames());
                lblIdentification.setValue(user.getIdentificationNumber());
            }        
        } catch (Exception ex) {
            showError(ex);    
        }
    }

    public void blockFields() {
            rActivationYes.setDisabled(true);
            rActivationNo.setDisabled(true);  
            dtbActivationDate.setDisabled(true);
            txtObservations.setReadonly(true);
            btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (dtbActivationDate.getText().isEmpty()) {
            dtbActivationDate.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtObservations.getText().isEmpty()) {
            txtObservations.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveProduct(Product _product) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Product product= null;
        boolean indActivation;
        EJBRequest request1 = new EJBRequest();
        StatusProduct statusProduct;
        try {
            if (_product != null) {
                product = _product;
            } else {

                //Se obtiene el producto a activar
                adminProduct = new AdminProductController();
                product = adminProduct.getProductParent();
                product = new Product();
            }                   

    
            if (rActivationYes.isChecked()) {
                indActivation = true;
                request1.setParam(WebConstants.PRODUCT_STATUS_ACTIVATED);
                statusProduct = productEJB.loadStatusProduct(request1);
            } else {
                indActivation = false;
                request1.setParam(WebConstants.PRODUCT_STATUS_INACTIVATED);
                statusProduct = productEJB.loadStatusProduct(request1);
            }
            
            //Guarda la activación de las tarjetas
            product.setName(lblProduct.getValue());
            product.setUserActivationId(user);   
            product.setActivationDate(dtbActivationDate.getValue());
            product.setIndActivation(indActivation);
            product.setStatusProductId(statusProduct);
            product.setObservations(txtObservations.getText().toString());
            product.setCreateDate(new Timestamp(new Date().getTime()));
            product = productEJB.saveProduct(product);
            this.showMessage("sp.common.save.success", false, null);
            EventQueues.lookup("updateActivationProduct", EventQueues.APPLICATION, true).publish(new Event(""));
        } catch (Exception ex) {
            showError(ex);
        }

      }   
        
    public void onClick$btnSave() throws RegisterNotFoundException, NullParameterException, GeneralException  {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveProduct(productParam);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveProduct(productParam);
                    break;
                default:
                    break;
            }
        }
    }
    
    public void onClick$btnBack() {
        winAdminActivationProduct.detach();
    }

    public void loadData() {
        Date today = new Timestamp(new Date().getTime());
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                dtbActivationDate.setValue(today);
                dtbActivationDate.setDisabled(true);
                loadFields(productParam);
            break;
            case WebConstants.EVENT_VIEW:           
                loadFields(productParam);
                blockFields();
            break;
            case WebConstants.EVENT_ADD:
                dtbActivationDate.setValue(today);
                dtbActivationDate.setDisabled(true);
                lblCity.setValue(user.getComercialAgencyId().getCityId().getName());
                lblAgency.setValue(user.getComercialAgencyId().getName());
                lblUserActivation.setValue(user.getFirstNames() + " " + user.getLastNames());
                lblIdentification.setValue(user.getIdentificationNumber());
            break;
            default:
                break;
        }
  }
    
}  
    
