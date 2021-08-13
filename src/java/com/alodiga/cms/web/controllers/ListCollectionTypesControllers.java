package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.RequestEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.custom.components.ListcellEditButton;
import com.alodiga.cms.web.custom.components.ListcellViewButton;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import com.alodiga.cms.web.utils.Utils;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.CollectionType;
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

public class ListCollectionTypesControllers extends GenericAbstractListController<CollectionType> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtName;
    private RequestEJB requestEJB = null;
    private List<CollectionType> collectionType = null;
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
            adminPage = "adminCollectionTypes.zul";
            requestEJB = (RequestEJB) EJBServiceLocator.getInstance().get(EjbConstants.REQUEST_EJB);
            getData();
            loadDataList(collectionType);
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
   public void getData() {
    collectionType = new ArrayList<CollectionType>();
        try {
            request.setFirst(0);
            request.setLimit(null);
            collectionType = requestEJB.getCollectionType(request);
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
            StringBuilder file = new StringBuilder(Labels.getLabel("cms.crud.collectionsType.listDownload"));
            file.append("_");
            file.append(date);
            Utils.exportExcel(lbxRecords, file.toString());
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
    
    @Override
    public List<CollectionType> getFilterList(String filter) {
        List<CollectionType> collectionTypeList = new ArrayList<CollectionType>();
        try {
            if (filter != null && !filter.equals("")) {
                collectionTypeList = requestEJB.getSearchCollectionType(filter);
            } else {
                return collectionType;
            }
        } catch (Exception ex) {
            showError(ex);
        }
        return collectionTypeList;
    }

//    @Override
//    public List<CollectionType> getFilterList(String filter) {
//        List<CollectionType> collectionTypeaux = new ArrayList<CollectionType>();
//        CollectionType collectionTypes;
//        try {
//            if (filter != null && !filter.equals("")) {
//                collectionTypes = requestEJB.searchCollectionType(filter);
//                collectionTypeaux.add(collectionTypes);
//            } else {
//                return collectionType;
//            }
//        } catch (RegisterNotFoundException ex) {
//            Logger.getLogger(ListCollectionTypesControllers.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            showError(ex);
//        }
//        return collectionTypeaux;
//    }
//    
    public void startListener() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void loadDataList(List<CollectionType> list) {
          try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                btnDownload.setVisible(true);
                for (CollectionType collectionType : list) {
                    item = new Listitem();
                    item.setValue(collectionType);
                    item.appendChild(new Listcell(collectionType.getCountryId().getName()));
                    item.appendChild(new Listcell(collectionType.getDescription()));
                    
                    if (collectionType.getPersonTypeId() != null) {
                        item.appendChild(new Listcell(collectionType.getPersonTypeId().getDescription()));
                    } else {
                        item.appendChild(new Listcell(""));
                    }
                    item.appendChild( new ListcellEditButton(adminPage, collectionType));
                    item.appendChild(new ListcellViewButton(adminPage, collectionType,true));
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
}
