package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Country;
import com.cms.commons.models.State;
import com.cms.commons.models.City;
import com.cms.commons.models.ZipZone;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Radio;

public class AdminCityZipZoneController extends GenericAbstractAdminController {

            
    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtName;
    private Textbox txtCode;
    private Textbox txtCity;
    private UtilsEJB utilsEJB = null;
    private ZipZone zipZoneParam;
    private Button btnSave;
    private Integer eventType;
    private Toolbarbutton tbbTitle;
    public Window winAdminCityZipZone;
    private City city = null;
    List<ZipZone> phonePersonList = new ArrayList<ZipZone>();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);        
        eventType = (Integer) Sessions.getCurrent().getAttribute( WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
           zipZoneParam = null;                    
       } else {
           zipZoneParam = (ZipZone) Sessions.getCurrent().getAttribute("object");            
       }
        initialize();
    }
    
    @Override
    public void initialize() {
        super.initialize();
        try {
            utilsEJB  = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public void obtainCity() {
        this.clearMessage();  
        
         AdminCityController adminCity = new AdminCityController();
            if (adminCity.getCityParent().getId() != null) {
                city = adminCity.getCityParent();
            }
            
        txtCity.setVisible(true);
        txtCity.setValue("");        
        txtCity.setValue(String.valueOf(city.getName()));
    }
    
    public void clearFields() {
        txtName.setRawValue(null);
        txtCode.setRawValue(null);

    }

    private void loadFields(ZipZone zipZone) {
        try {
            txtName.setText(zipZone.getName());
            txtCode.setText(zipZone.getCode());
            txtCity.setText(String.valueOf(zipZone.getCityId()));
            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtName.setReadonly(true);
        txtCode.setReadonly(true);
        txtCity.setReadonly(true);
        btnSave.setVisible(false);
    }
    
    public void onClick$btnBack() {
        winAdminCityZipZone.detach();
    }

    public Boolean validateEmpty() {
        if (txtName.getText().isEmpty()) {
            txtName.setFocus(true);
            this.showMessage("cms.error.field.fullName", true, null);
        } else if (txtCode.getText().isEmpty()) {
            txtCode.setFocus(true);
            this.showMessage("cms.error.field.cityEmptyCode", true, null);
        } else if (txtCity.getText().isEmpty()) {
            txtCity.setFocus(true);
            this.showMessage("cms.error.field.city", true, null);
        } else {
            return true;
        }
        return false;

    }
    
    private void saveZipZone(ZipZone _zipZone) {
        try {
            ZipZone zipZone = null;
            if (_zipZone != null) {
            
            zipZone = _zipZone;
            } else {
                zipZone = new ZipZone();
            }
            
            //Obtener el Estado
            AdminCityController adminCity = new AdminCityController();
            if (adminCity.getCityParent().getId() != null) {
                city = adminCity.getCityParent();
            }
            
            //Guardar ZipZone
            zipZone.setCityId(city);
            zipZone.setCode(txtCode.getText());
            zipZone.setName(txtName.getText());
            zipZone = utilsEJB.saveZipZone(zipZone);
            this.showMessage("sp.common.save.success", false, null);
            btnSave.setVisible(false);
        
        } catch (Exception ex) {
                 showError(ex);
            }
    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    obtainCity();
                    saveZipZone(null);   
                                       
                    break;
                case WebConstants.EVENT_EDIT:
                    saveZipZone(zipZoneParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(zipZoneParam);
                obtainCity();
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(zipZoneParam);
                obtainCity();
                blockFields();         
                break;
            case WebConstants.EVENT_ADD:
                obtainCity();
                break;
            default:
                break;
        }
    }    
}

    
 

