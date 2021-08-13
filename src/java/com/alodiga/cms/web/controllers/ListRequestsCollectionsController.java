package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.RequestEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import com.alodiga.cms.web.utils.Utils;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.enumeraciones.StatusRequestE;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.CollectionsRequest;
import com.cms.commons.models.Request;
import com.cms.commons.models.RequestHasCollectionsRequest;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class ListRequestsCollectionsController extends GenericAbstractListController<RequestHasCollectionsRequest> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtName;
    private Button btnAdd;
    private RequestEJB requestEJB = null;
    private List<RequestHasCollectionsRequest> requestHasCollectionsRequestList = null;
    private Tab tabRequestbyCollection;
    private AdminRequestController adminRequest = null;
    Boolean statusEditView= false;
    Request request = null;


    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
        startListener();
    }

    public void startListener() {
        EventQueue que = EventQueues.lookup("updateCollectionsRequest", EventQueues.APPLICATION, true);
        que.subscribe(new EventListener() {
            public void onEvent(Event evt) {
                getData();
                loadDataList(requestHasCollectionsRequestList);
            }
        });
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            //Evaluar Permisos
            permissionEdit = true;
            permissionAdd = true;
            permissionRead = true;
            adminPage = "adminRequestCollections.zul";
            requestEJB = (RequestEJB) EJBServiceLocator.getInstance().get(EjbConstants.REQUEST_EJB);
            adminRequest = new AdminRequestController();
            checkStatusRequest();
            getData();
            loadDataList(requestHasCollectionsRequestList);
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public void checkStatusRequest() {
       String statusRequestCodeRejected= StatusRequestE.SOLREC.getStatusRequestCode();
       String statusRequestCodeApproved= StatusRequestE.SOLAPR.getStatusRequestCode();
       String statusRequestCodeAssignedClient = StatusRequestE.TAASCL.getStatusRequestCode();
       AdminRequestController adminRequest = new AdminRequestController();
       if(adminRequest.getRequest().getStatusRequestId() != null){
            request = adminRequest.getRequest();
       }
       if(!(adminRequest.getRequest().getStatusRequestId().getId().equals(statusRequestCodeApproved)) 
          && !(request.getStatusRequestId().getCode().equals(statusRequestCodeRejected))
          && !(request.getStatusRequestId().getCode().equals(statusRequestCodeAssignedClient)))
        {
         statusEditView = true;
        } else{
              statusEditView= false;
              btnAdd.setVisible(false);
          }  
    }
    
    public void onSelect$tabRequestbyCollection() {
        try {
            doAfterCompose(self);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void loadDataList(List<RequestHasCollectionsRequest> list) {
        String applicantName = "";
        RequestHasCollectionsRequest requestHasCollectionsRequest = null;
        try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                for (RequestHasCollectionsRequest requestCollectionsRequest : list) {
                    item = new Listitem();
                    item.setValue(requestCollectionsRequest);
                    item.appendChild(new Listcell(requestCollectionsRequest.getCollectionsRequestid().getCountryId().getName()));
                    item.appendChild(new Listcell(requestCollectionsRequest.getCollectionsRequestid().getProductTypeId().getName()));
                    item.appendChild(new Listcell(requestCollectionsRequest.getCollectionsRequestid().getCollectionTypeId().getDescription()));
                    if (requestCollectionsRequest.getIndApproved() != null) {
                        item.appendChild(new Listcell((requestCollectionsRequest.getIndApproved().toString()).equals("1")?"Aprobado":"Rechazado"));
                    } else {
                        item.appendChild(new Listcell("Pendiente"));
                    }                    
                    if(statusEditView == true){
                        item.appendChild(createButtonEditModal(requestCollectionsRequest));
                        item.appendChild(createButtonViewModal(requestCollectionsRequest));
                    } else {
                        item.appendChild(new Listcell(" "));
                        item.appendChild(createButtonViewModal(requestCollectionsRequest));
                    }
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
    
    public void onClick$btnAdd() throws InterruptedException {
        try {
            Sessions.getCurrent().setAttribute(WebConstants.EVENTYPE, WebConstants.EVENT_ADD);
            Map<String, Object> paramsPass = new HashMap<String, Object>();
            paramsPass.put("object", requestHasCollectionsRequestList);
            if (requestHasCollectionsRequestList.size() == 0) {
                requestHasCollectionsRequestList = null;
            }
            final Window window = (Window) Executions.createComponents(adminPage, null, paramsPass);
            window.doModal();
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public Listcell createButtonEditModal(final Object obg) {
        Listcell listcellEditModal = new Listcell();
        try {
            Button button = new Button();
            button.setImage("/images/icon-edit.png");
            button.setTooltiptext(Labels.getLabel("sp.common.actions.edit"));
            button.setClass("open orange");
            button.addEventListener("onClick", new EventListener() {
                @Override
                public void onEvent(Event arg0) throws Exception {
                    Sessions.getCurrent().setAttribute("object", obg);
                    Sessions.getCurrent().setAttribute(WebConstants.EVENTYPE, WebConstants.EVENT_EDIT);
                    Map<String, Object> paramsPass = new HashMap<String, Object>();
                    paramsPass.put("object", obg);
                    final Window window = (Window) Executions.createComponents(adminPage, null, paramsPass);
                    window.doModal();
                }

            });
            button.setParent(listcellEditModal);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return listcellEditModal;
    }

    public Listcell createButtonViewModal(final Object obg) {
        Listcell listcellViewModal = new Listcell();
        try {
            Button button = new Button();
            button.setImage("/images/icon-invoice.png");
            button.setTooltiptext(Labels.getLabel("sp.common.actions.view"));
            button.setClass("open orange");
            button.addEventListener("onClick", new EventListener() {
                @Override
                public void onEvent(Event arg0) throws Exception {
                    Sessions.getCurrent().setAttribute("object", obg);
                    Sessions.getCurrent().setAttribute(WebConstants.EVENTYPE, WebConstants.EVENT_VIEW);
                    Map<String, Object> paramsPass = new HashMap<String, Object>();
                    paramsPass.put("object", obg);
                    final Window window = (Window) Executions.createComponents(adminPage, null, paramsPass);
                    window.doModal();
                }

            });
            button.setParent(listcellViewModal);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return listcellViewModal;
    }

    public void getData() {;
        requestHasCollectionsRequestList = new ArrayList<RequestHasCollectionsRequest>();
        Request requestCard = null;        
        AdminRequestController adminRequestController = new AdminRequestController();
        if (adminRequestController.getRequest().getId() != null) {
            requestCard = adminRequestController.getRequest();
        }
        try {
            Map params = new HashMap();
            EJBRequest request1 = new EJBRequest();
            params.put(Constants.REQUESTS_KEY, requestCard.getId());
            request1.setParams(params);
            requestHasCollectionsRequestList = requestEJB.getRequestsHasCollectionsRequestByRequest(request1);
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
            showEmptyList();
        } catch (GeneralException ex) {
            showError(ex);
        }
    }

    private void showEmptyList() {
        Listitem item = new Listitem();
        item.appendChild(new Listcell(Labels.getLabel("sp.error.empty.list")));
        item.appendChild(new Listcell());
        item.appendChild(new Listcell());
        item.appendChild(new Listcell());
        item.setParent(lbxRecords);
    }

    public void onClick$btnDownload() throws InterruptedException {
        try {
            Utils.exportExcel(lbxRecords, Labels.getLabel("cms.common.cardRequest.list"));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnClear() throws InterruptedException {
        txtName.setText("");
    }

    @Override
    public List<RequestHasCollectionsRequest> getFilterList(String filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
