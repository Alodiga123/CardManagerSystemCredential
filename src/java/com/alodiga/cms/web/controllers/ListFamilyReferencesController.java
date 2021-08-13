package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import com.alodiga.cms.web.utils.Utils;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.enumeraciones.StatusRequestE;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.ApplicantNaturalPerson;
import com.cms.commons.models.FamilyReferences;
import com.cms.commons.models.NaturalCustomer;
import com.cms.commons.models.Request;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Window;

public class ListFamilyReferencesController extends GenericAbstractListController<FamilyReferences> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Tab tabAddress;
    private Textbox txtName;
    private UtilsEJB utilsEJB = null;
    private PersonEJB personEJB = null;
    private List<FamilyReferences> familyReferences = null;
    private Long optionMenu;
    private int indPersonTypeCustomer = 0;
    Boolean statusEditView= false;
    Request request = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
        startListener();
    }

    public void startListener() {
        EventQueue que = EventQueues.lookup("updateFamilyReferences", EventQueues.APPLICATION, true);
        que.subscribe(new EventListener() {

            public void onEvent(Event evt) {
                getData();
                loadDataList(familyReferences);
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
            adminPage = "/adminFamilyReferences.zul";
            optionMenu = (Long) session.getAttribute(WebConstants.OPTION_MENU);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            checkStatusRequest();     
            getData();
            loadDataList(familyReferences);
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
    
    public void onClick$btnAdd() throws InterruptedException {
        try {
            Sessions.getCurrent().setAttribute(WebConstants.EVENTYPE, WebConstants.EVENT_ADD);
            Map<String, Object> paramsPass = new HashMap<String, Object>();
            paramsPass.put("object", familyReferences);
            final Window window = (Window) Executions.createComponents(adminPage, null, paramsPass);
            window.doModal();
        } catch (Exception ex) {
            this.showMessage("sp.error.general", true, ex);
        }
    }

    public void onClick$btnDelete() {
    }

    public void loadDataList(List<FamilyReferences> list) {
        try {
            Request request = null;
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                for (FamilyReferences familyReferences : list) {
                    item = new Listitem();
                    item.setValue(familyReferences);
                    StringBuilder builder = new StringBuilder(familyReferences.getFirstNames());
                    builder.append(" ");
                    builder.append(familyReferences.getLastNames());
                    item.appendChild(new Listcell(builder.toString()));
                    item.appendChild(new Listcell(familyReferences.getLocalPhone()));
                    item.appendChild(new Listcell(familyReferences.getCellPhone()));
                    item.appendChild(new Listcell(familyReferences.getCity()));
                    if(statusEditView == true){
                        item.appendChild(createButtonEditModal(familyReferences));
                        item.appendChild(createButtonViewModal(familyReferences));
                    } else {
                        item.appendChild(new Listcell(" "));
                        item.appendChild(createButtonViewModal(familyReferences));
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

    public void getData() {
        familyReferences = new ArrayList<FamilyReferences>();
        ApplicantNaturalPerson applicantNaturalPerson = null;
        NaturalCustomer naturalCustomer = null;

        try {
            if (optionMenu == Constants.LIST_CARD_REQUEST) {
                AdminNaturalPersonController adminNaturalPerson = new AdminNaturalPersonController();
                if (adminNaturalPerson.getApplicantNaturalPerson() != null) {
                    applicantNaturalPerson = adminNaturalPerson.getApplicantNaturalPerson();
                }
                EJBRequest request1 = new EJBRequest();
                Map params = new HashMap();
                params.put(Constants.APPLICANT_NATURAL_PERSON_KEY, applicantNaturalPerson.getId());
                request1.setParams(params);
                familyReferences = personEJB.getFamilyReferencesByApplicant(request1);                
            } else if (optionMenu == Constants.LIST_CUSTOMER_MANAGEMENT) {
                indPersonTypeCustomer = (Integer) Sessions.getCurrent().getAttribute(WebConstants.IND_PERSON_TYPE_CUSTOMER);
                if (indPersonTypeCustomer == 1) {
                    AdminNaturalPersonCustomerController adminNaturalCustomer = new AdminNaturalPersonCustomerController();
                    if (adminNaturalCustomer != null) {
                        naturalCustomer = adminNaturalCustomer.getNaturalCustomer();
                    }
                }               
                EJBRequest request1 = new EJBRequest();
                Map params = new HashMap();
                params.put(Constants.APPLICANT_NATURAL_CUSTOMER_KEY, naturalCustomer.getId());
                request1.setParams(params);
                familyReferences = personEJB.getFamilyReferencesByCustomer(request1);
            }
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
    public List<FamilyReferences> getFilterList(String filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
