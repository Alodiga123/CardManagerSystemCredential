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
import com.cms.commons.models.PlasticCustomizingRequest;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

public class ListPlasticRequestControllers extends GenericAbstractListController<PlasticCustomizingRequest> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtName;
    private RequestEJB requestEJB = null;
    private List<PlasticCustomizingRequest> plasticCustomizingRequest = null;    

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
    }


    @Override
    public void initialize() {
        super.initialize();
        try {
            adminPage = "TabPlasticRequest.zul";
            requestEJB = (RequestEJB) EJBServiceLocator.getInstance().get(EjbConstants.REQUEST_EJB);
            getData();
            loadDataList(plasticCustomizingRequest);
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
   public void getData() {
    plasticCustomizingRequest = new ArrayList<PlasticCustomizingRequest>();
        try {
            request.setFirst(0);
            request.setLimit(null);
            plasticCustomizingRequest= requestEJB.getPlasticCustomizingRequest(request);
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
        } catch (GeneralException ex) {
            showError(ex);
        }
    }
   
    public void onClick$btnAdd() throws InterruptedException {
        Sessions.getCurrent().setAttribute("eventType", WebConstants.EVENT_ADD);
        Executions.getCurrent().sendRedirect(adminPage);
    }
           
   public void onClick$btnDownload() throws InterruptedException {
        try {
            Utils.exportExcel(lbxRecords, Labels.getLabel("cms.crud.plasticRequest.listDownload"));
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

    public void startListener() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void loadDataList(List<PlasticCustomizingRequest> list) {
          try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                btnDownload.setVisible(true);
                for (PlasticCustomizingRequest plasticCustomizingRequest : list) {

                    item = new Listitem();
                    item.setValue(plasticCustomizingRequest);
                    String pattern = "yyyy-MM-dd";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                    item.appendChild(new Listcell(plasticCustomizingRequest.getRequestNumber()));
                    item.appendChild(new Listcell(simpleDateFormat.format(plasticCustomizingRequest.getRequestDate())));
                    item.appendChild(new Listcell(plasticCustomizingRequest.getPlasticManufacturerId().getName()));
                    item.appendChild(new Listcell(plasticCustomizingRequest.getStatusPlasticCustomizingRequestId().getDescription()));
                    item.appendChild( new ListcellEditButton(adminPage, plasticCustomizingRequest));
                    item.appendChild(new ListcellViewButton(adminPage, plasticCustomizingRequest,true));
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

    @Override
    public List<PlasticCustomizingRequest> getFilterList(String filter) {
        List<PlasticCustomizingRequest> plasticCustomizingRequestList_ = new ArrayList<PlasticCustomizingRequest>();
        try {
            if (filter != null && !filter.equals("")) {
                plasticCustomizingRequestList_ = requestEJB.getSearchPlasticCustomizingRequest(filter);
            } else {
                return plasticCustomizingRequest;
            }
        } catch (Exception ex) {
            showError(ex);
        }
        return plasticCustomizingRequestList_; 
    }

}
