package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.RequestEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.custom.components.ListcellEditButton;
import com.alodiga.cms.web.custom.components.ListcellViewButton;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import static com.alodiga.cms.web.generic.controllers.GenericDistributionController.request;
import com.alodiga.cms.web.utils.Utils;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.Request;
import com.cms.commons.models.StatusRequest;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.enumeraciones.StatusRequestE;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.util.QueryConstants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

public class ListRequestController extends GenericAbstractListController<Request> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtRequestNumber;
    private RequestEJB requestEJB = null;
    private List<Request> requests = null;
    public static int indAddRequestPerson;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
    }

    public void startListener() {
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            //Evaluar Permisos
            permissionEdit = true;
            permissionAdd = true;
            permissionRead = true;
            Sessions.getCurrent().setAttribute(WebConstants.OPTION_MENU, Constants.LIST_CARD_REQUEST);
            requestEJB = (RequestEJB) EJBServiceLocator.getInstance().get(EjbConstants.REQUEST_EJB);
            getData();
            loadList(requests);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public int getAddRequestPerson() {
        return indAddRequestPerson;
    }

    public void onClick$btnAddNaturalPersonRequest() throws InterruptedException {
        Sessions.getCurrent().setAttribute(WebConstants.EVENTYPE, WebConstants.EVENT_ADD);
        Executions.getCurrent().sendRedirect("TabNaturalPerson.zul");
        indAddRequestPerson = 1;
    }

    public void onClick$btnAddLegalPersonRequest() throws InterruptedException {
        Sessions.getCurrent().setAttribute(WebConstants.EVENTYPE, WebConstants.EVENT_ADD);
        Executions.getCurrent().sendRedirect("TabLegalPerson.zul");
        indAddRequestPerson = 2;
    }

    public void onClick$btnDelete() {
    }

    public void loadList(List<Request> list) {
        String applicantNameLegal = "";
        String tipo = "";
        String statusRequestCodeRejected= StatusRequestE.SOLREC.getStatusRequestCode();
        String statusRequestCodeApproved= StatusRequestE.SOLAPR.getStatusRequestCode();
        String statusRequestCodeAssignedClient = StatusRequestE.TAASCL.getStatusRequestCode();
        try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                for (Request request : list) {
                    item = new Listitem();
                    item.setValue(request);
                    String pattern = "yyyy-MM-dd";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                    item.appendChild(new Listcell(request.getRequestNumber()));
                    item.appendChild(new Listcell(simpleDateFormat.format(request.getRequestDate())));
                    item.appendChild(new Listcell(request.getProgramId().getName()));
                    if (request.getPersonId() != null) {
                        if (request.getIndPersonNaturalRequest() == true) {
                            tipo = "PN";
                            item.appendChild(new Listcell(tipo));
                            StringBuilder applicantNameNatural = new StringBuilder(request.getPersonId().getApplicantNaturalPerson().getFirstNames());
                            applicantNameNatural.append(" ");
                            applicantNameNatural.append(request.getPersonId().getApplicantNaturalPerson().getLastNames());
                            item.appendChild(new Listcell(applicantNameNatural.toString()));
                            item.appendChild(new Listcell(request.getStatusRequestId().getDescription()));
                            adminPage = "TabNaturalPerson.zul";                                     
                            if(!(request.getStatusRequestId().getId().equals(statusRequestCodeApproved)) 
                                    && !(request.getStatusRequestId().getCode().equals(statusRequestCodeRejected))
                                    && !(request.getStatusRequestId().getCode().equals(statusRequestCodeAssignedClient)))
                            {   
                            item.appendChild(permissionEdit ? new ListcellEditButton(adminPage, request) : new Listcell());
                            item.appendChild(permissionRead ? new ListcellViewButton(adminPage, request) : new Listcell());   
                            }  else {
                            item.appendChild(new Listcell(" "));
                            item.appendChild(permissionRead ? new ListcellViewButton(adminPage, request) : new Listcell());
                            }

                        } else {
                            tipo = "PJ";
                            item.appendChild(new Listcell(tipo));
                            applicantNameLegal = request.getPersonId().getLegalPerson().getEnterpriseName();
                            item.appendChild(new Listcell(applicantNameLegal));
                            item.appendChild(new Listcell(request.getStatusRequestId().getDescription()));
                            adminPage = "TabLegalPerson.zul";
                            if(!(request.getStatusRequestId().getId().equals(statusRequestCodeApproved)) 
                                    && !(request.getStatusRequestId().getCode().equals(statusRequestCodeRejected))
                                    && !(request.getStatusRequestId().getCode().equals(statusRequestCodeAssignedClient)))
                            {
                            item.appendChild(permissionEdit ? new ListcellEditButton(adminPage, request) : new Listcell());
                            item.appendChild(permissionRead ? new ListcellViewButton(adminPage, request) : new Listcell());
                            }  else {
                            item.appendChild(new Listcell(" "));
                            item.appendChild(permissionRead ? new ListcellViewButton(adminPage, request) : new Listcell());
                            }
                        }
                    } else {
                        if (request.getIndPersonNaturalRequest() == true) {
                            tipo = "PN";
                            item.appendChild(new Listcell(tipo));
                            item.appendChild(new Listcell("SIN REGISTRAR"));
                            item.appendChild(new Listcell(request.getStatusRequestId().getDescription()));
                            adminPage = "TabNaturalPerson.zul";
                            if(!(request.getStatusRequestId().getId().equals(statusRequestCodeApproved)) 
                                    && !(request.getStatusRequestId().getCode().equals(statusRequestCodeRejected))
                                    && !(request.getStatusRequestId().getCode().equals(statusRequestCodeAssignedClient)))
                            {
                            item.appendChild(permissionEdit ? new ListcellEditButton(adminPage, request) : new Listcell());
                            item.appendChild(permissionRead ? new ListcellViewButton(adminPage, request) : new Listcell());
                            }  else {
                            item.appendChild(new Listcell(" "));
                            item.appendChild(permissionRead ? new ListcellViewButton(adminPage, request) : new Listcell());
                            }
                        } else {
                            tipo = "PJ";
                            item.appendChild(new Listcell(tipo));
                            item.appendChild(new Listcell("SIN REGISTRAR"));
                            item.appendChild(new Listcell(request.getStatusRequestId().getDescription()));
                            adminPage = "TabLegalPerson.zul";
                            if(!(request.getStatusRequestId().getId().equals(statusRequestCodeApproved)) 
                                    && !(request.getStatusRequestId().getCode().equals(statusRequestCodeRejected))
                                    && !(request.getStatusRequestId().getCode().equals(statusRequestCodeAssignedClient)))
                            {
                            item.appendChild(permissionEdit ? new ListcellEditButton(adminPage, request) : new Listcell());
                            item.appendChild(permissionRead ? new ListcellViewButton(adminPage, request) : new Listcell());
                            }  else {
                            item.appendChild(new Listcell(" "));
                            item.appendChild(permissionRead ? new ListcellViewButton(adminPage, request) : new Listcell());
                            }
                        }
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

    public void getData() {
        requests = new ArrayList<Request>();
        try {
            request.setFirst(0);
            request.setLimit(null);
            requests = requestEJB.getRequests(request);
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
            String pattern = "dd-MM-yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            StringBuilder file = new StringBuilder(Labels.getLabel("cms.crud.request.listDownload"));
            file.append("_");
            file.append(date);
            Utils.exportExcel(lbxRecords, file.toString());
        } catch (Exception ex) {
            showError(ex);
        }
        
    } 

    public void onClick$btnClear() throws InterruptedException {
        txtRequestNumber.setText("");
    }

    public void onClick$btnSearch() throws InterruptedException {
        try {
            loadList(getFilterList(txtRequestNumber.getText()));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    @Override
    public List<Request> getFilterList(String filter) {
        List<Request> requestaux = new ArrayList<Request>();
        try {
            
            if (filter != null && !filter.equals("")) {
                requestaux = requestEJB.searchCardRequest(filter);
            } else {
                return requests;
            }
        } catch (RegisterNotFoundException ex) {
            Logger.getLogger(ListRequestController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            showError(ex);
        }
        return requestaux;
    }

    @Override
    public void loadDataList(List<Request> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
