package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import com.cms.commons.enumeraciones.StatusRequestE;
import com.alodiga.cms.web.utils.Utils;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Address;
import com.cms.commons.models.Person;
import com.cms.commons.models.PersonHasAddress;
import com.cms.commons.models.Request;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Window;

public class ListAddressController extends GenericAbstractListController<PersonHasAddress> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtName;
    private PersonEJB personEJB = null;
    private UtilsEJB utilsEJB = null;
    private List<PersonHasAddress> personHasAddress = null;
    private Integer eventType;
    private AdminRequestController adminRequest = null;
    private AdminNaturalPersonCustomerController adminNaturalCustomer = null;
    private AdminLegalPersonCustomerController adminLegalCustomer = null;
    private Long optionMenu;
    private Tab tabAddress;
    private int indPersonTypeCustomer = 0;
    Boolean statusEditView= false;
    Request request = null;
    private Tab tabApplicantOFAC;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
        startListener();
    }

    public void startListener() {
        EventQueue que = EventQueues.lookup("updateAddress", EventQueues.APPLICATION, true);
        que.subscribe(new EventListener() {

            public void onEvent(Event evt) {
                getData();
                loadDataList(personHasAddress);
                tabApplicantOFAC.setDisabled(false);
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
            adminPage = "/adminPersonAddress.zul";
            optionMenu = (Long) session.getAttribute(WebConstants.OPTION_MENU);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            checkStatusRequest();
            getData();
            loadDataList(personHasAddress);
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
    
    public void onSelect$tabAddress() {
        try {
            doAfterCompose(self);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnAdd() throws InterruptedException {
        try {
            Sessions.getCurrent().setAttribute(WebConstants.EVENTYPE, WebConstants.EVENT_ADD);
            Map<String, Object> paramsPass = new HashMap<String, Object>();
            paramsPass.put("object", personHasAddress);
            final Window window = (Window) Executions.createComponents(adminPage, null, paramsPass);
            window.doModal();
        } catch (Exception ex) {
            this.showMessage("sp.error.general", true, ex);
        }
    }

    public void onClick$btnDelete() {
    }

    public void loadDataList(List<PersonHasAddress> list) {
        String indAddressDelivery = "";
        try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                for (PersonHasAddress personHasAddress : list) {
                    item = new Listitem();
                    item.setValue(personHasAddress);
                    item.appendChild(new Listcell(personHasAddress.getAddressId().getCountryId().getName()));
                    item.appendChild(new Listcell(personHasAddress.getAddressId().getCityId().getName()));
                    item.appendChild(new Listcell(personHasAddress.getAddressId().getAddressTypeId().getDescription()));
                    if (personHasAddress.getAddressId().getIndAddressDelivery() != null ) {
                        if (personHasAddress.getAddressId().getIndAddressDelivery() == true) {
                            indAddressDelivery = "Yes";
                        } else {
                            indAddressDelivery = "No";
                        }
                        item.appendChild(new Listcell(indAddressDelivery));
                    } else {
                        item.appendChild(new Listcell("No"));
                    }    
                    if (request.getPersonId().getPersonTypeId().getOriginApplicationId().getId() == Constants.ORIGIN_APPLICATION_CMS_ID) {
                        item.appendChild(new Listcell(personHasAddress.getAddressId().getZipZoneId().getCode()));
                    }
                    if (request.getPersonId().getPersonTypeId().getOriginApplicationId().getId() == Constants.ORIGIN_APPLICATION_WALLET_ID) {
                        item.appendChild(new Listcell(personHasAddress.getAddressId().getZipZoneCode()));
                    }
                    if(statusEditView == true){
                        item.appendChild(createButtonEditModal(personHasAddress));
                        item.appendChild(createButtonViewModal(personHasAddress));
                    } else{
                        item.appendChild(new Listcell(" "));
                        item.appendChild(createButtonViewModal(personHasAddress));
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
        personHasAddress = new ArrayList<PersonHasAddress>();
        Address address = null;
        Person person = null;
        try {
            if (optionMenu == Constants.LIST_CARD_REQUEST) {
                adminRequest = new AdminRequestController();
                person = adminRequest.getRequest().getPersonId();
                eventType = adminRequest.getEventType();
            } 
            if (optionMenu == Constants.LIST_CUSTOMER_MANAGEMENT) {
                indPersonTypeCustomer = (Integer) Sessions.getCurrent().getAttribute(WebConstants.IND_PERSON_TYPE_CUSTOMER);
                if (indPersonTypeCustomer == 1) {
                    AdminNaturalPersonCustomerController adminNaturalCustomer = new AdminNaturalPersonCustomerController();
                    if (adminNaturalCustomer.naturalCustomerParam != null) {
                        person = adminNaturalCustomer.naturalCustomerParam.getPersonId();
                        eventType = adminNaturalCustomer.getEventType();
                    }
                } else {
                    AdminLegalPersonCustomerController adminLegalCustomer = new AdminLegalPersonCustomerController();
                    if (adminLegalCustomer.legalCustomerParam != null) {
                        person = adminLegalCustomer.legalCustomerParam.getPersonId();
                        eventType = adminLegalCustomer.getEventType();
                    }
                }                
            }    
            if (optionMenu == Constants.LIST_PROGRAM_OWNER) {
                AdminOwnerNaturalPersonController adminOwnerNaturalPerson = new AdminOwnerNaturalPersonController();
                if (adminOwnerNaturalPerson.naturalPersonParam != null) {
                    person = adminOwnerNaturalPerson.naturalPersonParam.getPersonId();
                }
            }
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.PERSON_KEY, person.getId());
            request1.setParams(params);
            personHasAddress = personEJB.getPersonHasAddressesByPerson(request1);
            if (eventType == WebConstants.EVENT_EDIT) {
                EJBRequest request2 = new EJBRequest();
                for (PersonHasAddress phs : personHasAddress) {
                    if (phs.getAddressId() == null) {
                        request2.setParam(phs.getAddressId().getId());
                        address = utilsEJB.loadAddress(request2);
                        phs.setAddressId(address);
                    }                    
                }
            }
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
            showEmptyList();
        } catch (GeneralException ex) {
            showError(ex);
        } catch (RegisterNotFoundException ex) {
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
    public List<PersonHasAddress> getFilterList(String filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
