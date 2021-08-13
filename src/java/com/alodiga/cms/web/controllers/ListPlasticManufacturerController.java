package com.alodiga.cms.web.controllers;
import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.custom.components.ListcellEditButton;
import com.alodiga.cms.web.custom.components.ListcellViewButton;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import com.alodiga.cms.web.utils.Utils;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.DocumentsPersonType;
import com.cms.commons.models.PersonType;
import com.cms.commons.models.PhonePerson;
import com.cms.commons.models.PhoneType;
import com.cms.commons.models.PlasticManufacturer;
import com.cms.commons.models.User;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

public class ListPlasticManufacturerController extends GenericAbstractListController<PlasticManufacturer> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtName;
    private PersonEJB personEJB = null;
    private List<PlasticManufacturer> plasticManufacturerList = null;
    private User currentUser;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            currentUser = (User) session.getAttribute(Constants.USER_OBJ_SESSION);
            adminPage = "adminPlasticManufacturer.zul";
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            getData();
            loadDataList(plasticManufacturerList);
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
   public void getData() {
    plasticManufacturerList = new ArrayList<PlasticManufacturer>();
    List<PhonePerson> phonePersonList = null; 
        try {
            request.setFirst(0);
            request.setLimit(null);
            plasticManufacturerList = personEJB.getPlasticManufacturer(request);
            for (PlasticManufacturer pm : plasticManufacturerList ){
                if (pm.getPersonId().getPhonePerson() == null) {
                    EJBRequest request = new EJBRequest();
                    Map params = new HashMap();
                    params.put(Constants.PERSON_KEY, pm.getPersonId().getId());
                    request.setParams(params);
                    phonePersonList = personEJB.getPhoneByPerson(request);
                        for (PhonePerson phone : phonePersonList ){
                            pm.getPersonId().setPhonePerson(phone);
                        }
                }
            }
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
        } catch (GeneralException ex) {
            showError(ex);
        }
    }

    public void onClick$btnAdd() throws InterruptedException {
        Sessions.getCurrent().setAttribute("eventType", WebConstants.EVENT_ADD);
        Sessions.getCurrent().removeAttribute("object");
        Executions.getCurrent().sendRedirect(adminPage);
    }
    
       
   public void onClick$btnDownload() throws InterruptedException {
        try {
            String pattern = "dd-MM-yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            StringBuilder file = new StringBuilder(Labels.getLabel("cms.crud.plastic.manufacturer.listDownload"));
            file.append("_");
            file.append(date);
            Utils.exportExcel(lbxRecords, file.toString());
        } catch (Exception ex) {
            showError(ex);
        }
        
    } 
    public void startListener() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void loadDataList(List<PlasticManufacturer> list) {
          try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                btnDownload.setVisible(true);
                for (PlasticManufacturer plasticManufacturer : list) {
                    item = new Listitem();
                    item.setValue(plasticManufacturer);
                    item.appendChild(new Listcell(plasticManufacturer.getPersonId().getCountryId().getName()));
                    item.appendChild(new Listcell(plasticManufacturer.getName()));
                    item.appendChild(new Listcell(plasticManufacturer.getIdentificationNumber()));
                    if (plasticManufacturer.getPersonId().getPhonePerson() != null) {
                        item.appendChild(new Listcell(plasticManufacturer.getPersonId().getPhonePerson().getNumberPhone()));
                    } else {
                        item.appendChild(new Listcell());
                    }
                    item.appendChild(new Listcell(plasticManufacturer.getPersonId().getEmail()));
                    item.appendChild(new ListcellEditButton(adminPage, plasticManufacturer));
                    item.appendChild(new ListcellViewButton(adminPage, plasticManufacturer,true));
                    item.setParent(lbxRecords);
                }
            } else {
                btnDownload.setVisible(false);
                item = new Listitem();
                item.appendChild(new Listcell(Labels.getLabel("sp.error.empty.list")));
                item.appendChild(new Listcell());
                item.appendChild(new Listcell());
                item.appendChild(new Listcell());
                item.setParent(lbxRecords);
            }

        } catch (Exception ex) {
           showError(ex);
        }
    }
    
    public void onClick$btnClear() throws InterruptedException {
        txtName.setText("");
    }
    
    public List<PlasticManufacturer> getFilterList(String filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
