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
import com.cms.commons.enumeraciones.StatusRequestE;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.CardRequestNaturalPerson;
import com.cms.commons.models.LegalCustomer;
import com.cms.commons.models.LegalPerson;
import com.cms.commons.models.User;
import com.cms.commons.models.Request;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

public class ListAdditionalCardsController extends GenericAbstractListController<CardRequestNaturalPerson> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtName;
    private UtilsEJB utilsEJB = null;
    private PersonEJB personEJB = null;
    private Tab tabAddress;
    private List<CardRequestNaturalPerson> cardRequestNaturalPersonList = null;
    private User currentUser;
    private Button btnSave;
    private Long optionMenu;
    Boolean statusEditView= false;
    Request request = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
        startListener();
    }

    public void startListener() {
        EventQueue que = EventQueues.lookup("updateCardRequestNaturalPerson", EventQueues.APPLICATION, true);
        que.subscribe(new EventListener() {

            public void onEvent(Event evt) {
                getData();
                loadDataList(cardRequestNaturalPersonList);
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
            adminPage = "adminAdditionalCards.zul";
            optionMenu = (Long) session.getAttribute(WebConstants.OPTION_MENU);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);  
            checkStatusRequest();
            getData();
            loadDataList(cardRequestNaturalPersonList);
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
        cardRequestNaturalPersonList = new ArrayList<CardRequestNaturalPerson>();
        LegalPerson legalPerson = null;
        LegalCustomer legalCustomer = null;
        try {

            if (optionMenu == Constants.LIST_CARD_REQUEST) {
                //Solicitante de Tarjeta
                AdminLegalPersonController adminLegalPerson = new AdminLegalPersonController();
                if (adminLegalPerson.getLegalPerson() != null) {
                    legalPerson = adminLegalPerson.getLegalPerson();
                }
                EJBRequest request1 = new EJBRequest();
                Map params = new HashMap();
                params.put(Constants.APPLICANT_LEGAL_PERSON_KEY, legalPerson.getId());
                request1.setParams(params);
                cardRequestNaturalPersonList = personEJB.getCardRequestNaturalPersonsByLegalApplicant(request1);

            } else if (optionMenu == Constants.LIST_CUSTOMER_MANAGEMENT) {
                AdminLegalPersonCustomerController adminLegalCustomer = new AdminLegalPersonCustomerController();
                if (adminLegalCustomer != null) {
                    legalCustomer = adminLegalCustomer.getLegalCustomer();
                }

                EJBRequest request1 = new EJBRequest();
                Map params = new HashMap();
                params.put(Constants.LEGAL_CUSTOMER_KEY, legalCustomer.getId());
                request1.setParams(params);
                cardRequestNaturalPersonList = personEJB.getCardRequestNaturalPersonsByLegalCustomer(request1);
            } else {
                cardRequestNaturalPersonList = null;
            }

        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
        } catch (GeneralException ex) {
            showError(ex);
        }
    }

    public void onClick$btnAdd() throws InterruptedException {
        try {
            Sessions.getCurrent().setAttribute(WebConstants.EVENTYPE, WebConstants.EVENT_ADD);
            Map<String, Object> paramsPass = new HashMap<String, Object>();
            paramsPass.put("object", cardRequestNaturalPersonList);
            final Window window = (Window) Executions.createComponents(adminPage, null, paramsPass);
            window.doModal();
        } catch (Exception ex) {
            this.showMessage("sp.error.general", true, ex);
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

    public void loadDataList(List<CardRequestNaturalPerson> list) {
        Locale locale = new Locale ("es", "ES");
        NumberFormat numberFormat = NumberFormat.getInstance (locale);
        String proposedLimit = "";
        try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                for (CardRequestNaturalPerson cardRequestNaturalPerson : list) {
                    item = new Listitem();
                    item.setValue(cardRequestNaturalPerson);
                    StringBuilder builder = new StringBuilder(cardRequestNaturalPerson.getFirstNames());
                    builder.append(" ");
                    builder.append(cardRequestNaturalPerson.getLastNames());
                    item.appendChild(new Listcell(builder.toString()));
                    item.appendChild(new Listcell(cardRequestNaturalPerson.getDocumentsPersonTypeId().getDescription()));
                    item.appendChild(new Listcell(cardRequestNaturalPerson.getIdentificationNumber()));
                    item.appendChild(new Listcell(cardRequestNaturalPerson.getPositionEnterprise()));
                    proposedLimit = numberFormat.format(cardRequestNaturalPerson.getProposedLimit().floatValue());
                    item.appendChild(new Listcell(proposedLimit));
                    if(statusEditView == true){   
                        item.appendChild(createButtonEditModal(cardRequestNaturalPerson));
                        item.appendChild(createButtonViewModal(cardRequestNaturalPerson));
                    } else {
                        item.appendChild(new Listcell(" "));
                        item.appendChild(createButtonViewModal(cardRequestNaturalPerson));
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

    @Override
    public List<CardRequestNaturalPerson> getFilterList(String filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
