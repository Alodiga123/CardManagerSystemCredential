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
import com.cms.commons.models.DocumentsPersonType;
import com.cms.commons.models.User;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

public class ListDocumentsPersonTypeController extends GenericAbstractListController<DocumentsPersonType> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtName;
    private PersonEJB personEJB = null;
    private UtilsEJB utilsEJB = null;
    private List<DocumentsPersonType> documentsPersonType = null;
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
            permissionEdit = true;
            permissionAdd = true; 
            permissionRead = true;
            currentUser = (User) session.getAttribute(Constants.USER_OBJ_SESSION);
            adminPage = "adminDocumentsPersonType.zul";
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            getData();
            loadDataList(documentsPersonType);
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
   public void getData() {
        documentsPersonType = new ArrayList<DocumentsPersonType>();
        try {
            request.setFirst(0);
            request.setLimit(null);
            documentsPersonType = utilsEJB.getDocumentsPersonType(request);
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
            StringBuilder file = new StringBuilder(Labels.getLabel("cms.crud.documentsPersonType.listDownload"));
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

    public void loadDataList(List<DocumentsPersonType> list) {
          try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                btnDownload.setVisible(true);
                for (DocumentsPersonType documentsPersonType : list) {
                    item = new Listitem();
                    item.setValue(documentsPersonType);
                    item.appendChild(new Listcell(documentsPersonType.getPersonTypeId().getCountryId().getName()));
                    item.appendChild(new Listcell(documentsPersonType.getPersonTypeId().getDescription()));
                    item.appendChild(new Listcell(documentsPersonType.getDescription()));
                    item.appendChild(new Listcell(documentsPersonType.getCodeIdentificationNumber()));
                    item.appendChild(permissionEdit ? new ListcellEditButton(adminPage, documentsPersonType) : new Listcell());
                    item.appendChild(permissionRead ? new ListcellViewButton(adminPage, documentsPersonType) : new Listcell());
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
    
    public void onClick$btnSearch() throws InterruptedException {
        try {
            loadDataList(getFilterList(txtName.getText()));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public List<DocumentsPersonType> getFilterList(String filter) {
       List<DocumentsPersonType> documentsPersonTypeaux = new ArrayList<DocumentsPersonType>();
       DocumentsPersonType documentsPersonTypes;
        try {
            if (filter != null && !filter.equals("")) {
                documentsPersonTypeaux = utilsEJB.getSearchDocumentsPersonType(filter);
            } else {
                return documentsPersonType;
            }
        } catch (Exception ex) {
            showError(ex);
        }
        return documentsPersonTypeaux;
    }

}
