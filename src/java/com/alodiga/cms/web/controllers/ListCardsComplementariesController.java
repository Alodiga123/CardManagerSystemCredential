package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import com.alodiga.cms.web.utils.Utils;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.enumeraciones.StatusRequestE;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.ApplicantNaturalPerson;
import com.cms.commons.models.Request;
import com.cms.commons.models.User;
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

public class ListCardsComplementariesController extends GenericAbstractListController<ApplicantNaturalPerson> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtName;
    private PersonEJB personEJB = null;
    private Tab tabAddress;
    private List<ApplicantNaturalPerson> cardComplementaryList = null;
    private User currentUser;
    private Button btnSave;
    Boolean statusEditView= false;
    Request request = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
        startListener();
    }
    
    public void startListener() {
        EventQueue que = EventQueues.lookup("updateCardComplementaries", EventQueues.APPLICATION, true);
        que.subscribe(new EventListener() {
            public void onEvent(Event evt) {
                getData();
                loadDataList(cardComplementaryList);
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
            adminPage = "/TabCardsComplementaries.zul";
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            checkStatusRequest();    
            getData();
            loadDataList(cardComplementaryList);
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

    public void getData() {
        cardComplementaryList = new ArrayList<ApplicantNaturalPerson>();
        ApplicantNaturalPerson applicantNaturalPerson = null;
        try {
            //Solicitante de Tarjeta
            AdminNaturalPersonController adminNaturalPerson = new AdminNaturalPersonController();
            if (adminNaturalPerson.getApplicantNaturalPerson() != null) {
                applicantNaturalPerson = adminNaturalPerson.getApplicantNaturalPerson();
            }
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.APPLICANT_NATURAL_PERSON_KEY, applicantNaturalPerson.getId());
            request1.setParams(params);
            cardComplementaryList = personEJB.getCardComplementaryByApplicant(request1);
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
        } catch (GeneralException ex) {
            showError(ex);
        }
    }  

    public void onClick$btnDownload() throws InterruptedException {
        try {
            Utils.exportExcel(lbxRecords, Labels.getLabel("cms.common.additionalCards.list"));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnClear() throws InterruptedException {
        txtName.setText("");
    }    
    
    public void loadDataList(List<ApplicantNaturalPerson> list) {
        try {
            Request request = null;
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                for (ApplicantNaturalPerson applicantNaturalPerson : list) {
                    item = new Listitem();
                    item.setValue(applicantNaturalPerson);
                    StringBuilder builder = new StringBuilder(applicantNaturalPerson.getFirstNames());
                    builder.append(" ");
                    builder.append(applicantNaturalPerson.getLastNames());                    
                    item.appendChild(new Listcell(builder.toString()));
                    item.appendChild(new Listcell(applicantNaturalPerson.getDocumentsPersonTypeId().getDescription()));
                    item.appendChild(new Listcell(applicantNaturalPerson.getIdentificationNumber()));
                    item.appendChild(new Listcell(applicantNaturalPerson.getKinShipApplicantId().getDescription()));
                    if(statusEditView == true){
                        item.appendChild(createButtonEditModal(applicantNaturalPerson));
                        item.appendChild(createButtonViewModal(applicantNaturalPerson));
                    } else {
                        item.appendChild(new Listcell(" "));
                        item.appendChild(createButtonViewModal(applicantNaturalPerson));
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
    
       public void onClick$btnAdd() throws InterruptedException {
        try {
            Sessions.getCurrent().setAttribute(WebConstants.EVENTYPE, WebConstants.EVENT_ADD);
            Map<String, Object> paramsPass = new HashMap<String, Object>();
            paramsPass.put("object", cardComplementaryList);
            final Window window = (Window) Executions.createComponents(adminPage, null, paramsPass);
            window.doModal();
        } catch (Exception ex) {
            this.showMessage("sp.error.general", true, ex);
        }
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

    public List<ApplicantNaturalPerson> getFilterList(String filter) {
       List<ApplicantNaturalPerson> applicantNaturalPersonList_ = new ArrayList<ApplicantNaturalPerson>();
       ApplicantNaturalPerson applicantNaturalPerson = null;
        try {
            if (filter != null && !filter.equals("")) {
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.PARAM_USER, filter);
            request1.setParams(params);
                
            //Solicitante de Tarjeta
            AdminNaturalPersonController adminNaturalPerson = new AdminNaturalPersonController();
            if (adminNaturalPerson.getApplicantNaturalPerson() != null) {
                applicantNaturalPerson = adminNaturalPerson.getApplicantNaturalPerson();
            }
     
            params.put(Constants.APPLICANT_NATURAL_PERSON_KEY, applicantNaturalPerson.getId());
            params.put(Constants.PARAM_APPLICANT_NATURAL_PERSON_NAME_KEY, filter);

            request1.setParams(params);    
                                
            applicantNaturalPersonList_ = personEJB.searchCardComplementaryByApplicant(request1);
            } else {
                return cardComplementaryList;
            }
        } catch (Exception ex) {
            showError(ex);
        }
        return applicantNaturalPersonList_;  
    }
    
    public void onClick$btnSearch() throws InterruptedException {
        try {
            loadDataList(getFilterList(txtName.getText()));
        } catch (Exception ex) {
            showError(ex);
        }
    }

}