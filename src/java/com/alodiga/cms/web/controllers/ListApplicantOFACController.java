package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.RequestEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import com.alodiga.cms.web.utils.Utils;
import com.alodiga.cms.web.utils.WebConstants;
import com.alodiga.ws.remittance.services.WSOFACMethodProxy;
import com.alodiga.ws.remittance.services.WsExcludeListResponse;
import com.alodiga.ws.remittance.services.WsLoginResponse;
import com.cms.commons.enumeraciones.StatusRequestE;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.ApplicantNaturalPerson;
import com.cms.commons.models.CardRequestNaturalPerson;
import com.cms.commons.models.LegalPerson;
import com.cms.commons.models.LegalPersonHasLegalRepresentatives;
import com.cms.commons.models.LegalRepresentatives;
import com.cms.commons.models.Person;
import com.cms.commons.models.Request;
import com.cms.commons.models.ReviewOFAC;
import com.cms.commons.models.User;
import com.cms.commons.models.StatusApplicant;
import com.cms.commons.models.StatusRequest;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.rmi.RemoteException;
import java.text.NumberFormat;
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

public class ListApplicantOFACController extends GenericAbstractListController<Person> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtName;
    private PersonEJB personEJB = null;
    private RequestEJB requestEJB = null;
    private UtilsEJB utilsEJB = null;
    private List<ApplicantNaturalPerson> applicantNaturalList = null;
    private List<ApplicantNaturalPerson> applicantNaturalComplementaryList = null;
    private List<Person> applicantList = null;
    private List<CardRequestNaturalPerson> applicantEmployeeList = null;
    private List<LegalPersonHasLegalRepresentatives> legalRepresentativesList = null;
    private User currentUser;
    private Button btnSave;
    private Button btnReviewOFAC;
    private AdminRequestController adminRequest = null;
    private Tab tabApplicantOFAC;
    private Boolean statusEditView= false;
    private Request request = null;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
        startListener();
    }

    public void startListener() {
        EventQueue que = EventQueues.lookup("updateApplicantOFAC", EventQueues.APPLICATION, true);
        que.subscribe(new EventListener() {
            public void onEvent(Event evt) {
                getData();
                loadDataList(applicantList);
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
            adminPage = "/adminApplicantOFAC.zul";
            adminRequest = new AdminRequestController();
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            requestEJB = (RequestEJB) EJBServiceLocator.getInstance().get(EjbConstants.REQUEST_EJB);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            checkStatusRequest();               
            getData();
            loadDataList(applicantList);
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
            btnReviewOFAC.setVisible(false);
        } 
    }
    
    public void onSelect$tabApplicantOFAC() {
        try {
            doAfterCompose(self);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void getData() {
        applicantList = new ArrayList<Person>();
        applicantNaturalList = new ArrayList<ApplicantNaturalPerson>();
        applicantNaturalComplementaryList = new ArrayList<ApplicantNaturalPerson>();
        applicantEmployeeList = new ArrayList<CardRequestNaturalPerson>();
        legalRepresentativesList = new ArrayList<LegalPersonHasLegalRepresentatives>();
        ApplicantNaturalPerson applicantNaturalPerson = null;
        Person applicantPerson = null;
        LegalPerson applicantLegalPerson = null;
        Request requestCard = null;
        EJBRequest request1 = new EJBRequest();
        Map params = new HashMap();
        try {
            //Solicitud de Tarjeta
            AdminRequestController adminRequestController = new AdminRequestController();
            if (adminRequestController.getRequest().getId() != null) {
                requestCard = adminRequestController.getRequest();
            }
            if (requestCard.getPersonTypeId().getIndNaturalPerson() == true) {
                //Solicitante Natural
                if (requestCard.getPersonId() != null) {
                    //Solicitante Principal de Tarjeta
                    AdminNaturalPersonController adminNaturalPerson = new AdminNaturalPersonController();
                    if (adminNaturalPerson.getApplicantNaturalPerson() != null) {
                        applicantNaturalPerson = adminNaturalPerson.getApplicantNaturalPerson();
                        if (applicantNaturalPerson.getPersonId().getApplicantNaturalPerson() == null) {
                            request1 = new EJBRequest();
                            params = new HashMap();
                            params.put(Constants.PERSON_KEY, applicantNaturalPerson.getPersonId().getId());
                            request1.setParams(params);
                            applicantNaturalList = personEJB.getApplicantByPerson(request1);
                            for (ApplicantNaturalPerson applicant: applicantNaturalList) {
                                applicantPerson = applicant.getPersonId();
                                applicantPerson.setApplicantNaturalPerson(applicant);
                            }
                        } else {
                            applicantPerson = applicantNaturalPerson.getPersonId();
                        }
                    }
                }   
                //Se obtienen los solicitantes complementarios
                request1 = new EJBRequest();
                params = new HashMap();
                params.put(Constants.APPLICANT_NATURAL_PERSON_KEY, applicantNaturalPerson.getId());
                request1.setParams(params);                
                applicantNaturalComplementaryList = personEJB.getCardComplementaryByApplicant(request1);
                for (ApplicantNaturalPerson applicantNatural : applicantNaturalList) {
                    if (applicantNatural.getPersonId().getApplicantNaturalPerson() == null) {
                        request1 = new EJBRequest();
                        params = new HashMap();
                        params.put(Constants.PERSON_KEY, applicantNatural.getPersonId().getId());
                        request1.setParams(params);
                        List<ApplicantNaturalPerson> applicantNaturalList = personEJB.getApplicantByPerson(request1);
                        for (ApplicantNaturalPerson an : applicantNaturalList) {
                            applicantNatural.getPersonId().setApplicantNaturalPerson(an);
                        }
                    }
                    applicantList.add(applicantNatural.getPersonId());
                }
            } else {
                //Solicitante Jurídico
                if (requestCard.getPersonId() != null) {
                    //Solicitante Principal de Tarjeta
                    AdminLegalPersonController adminLegalPerson = new AdminLegalPersonController();
                    if (adminLegalPerson.getLegalPerson() != null) {
                        applicantLegalPerson = adminLegalPerson.getLegalPerson();
                    }
                }
                //Se obtienen los empleados asociados a la empresa
                request1 = new EJBRequest();
                params = new HashMap();
                params.put(Constants.APPLICANT_LEGAL_PERSON_KEY, applicantLegalPerson.getId());
                request1.setParams(params);
                applicantEmployeeList = personEJB.getCardRequestNaturalPersonsByLegalApplicant(request1);
                for (CardRequestNaturalPerson employees : applicantEmployeeList) {
                    applicantList.add(employees.getPersonId());
                }
                //Se obtienen los representantes legales asociados a la empresa
                request1 = new EJBRequest();
                params = new HashMap();
                params.put(Constants.APPLICANT_LEGAL_PERSON_KEY, applicantLegalPerson.getId());
                request1.setParams(params);
                legalRepresentativesList = personEJB.getLegalRepresentativesesBylegalPerson(request1);
                for (LegalPersonHasLegalRepresentatives lr : legalRepresentativesList) {
                    request1 = new EJBRequest();
                    params = new HashMap();
                    params.put(Constants.PERSON_KEY, lr.getLegalRepresentativesid().getPersonId().getId());
                    request1.setParams(params);
                    List<LegalRepresentatives> legalRepresentativeList = utilsEJB.getLegalRepresentativesByPerson(request1);
                    for (LegalRepresentatives legalRepresentative : legalRepresentativeList) {
                        lr.setLegalRepresentativesid(legalRepresentative);
                    }
                    applicantList.add(lr.getLegalRepresentativesid().getPersonId());
                }
            }               
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } finally {
            if (requestCard.getPersonTypeId().getIndNaturalPerson() == true) {
                applicantList.add(applicantPerson);
                updateReviewOFAC();
            } else {
                applicantList.add(applicantLegalPerson.getPersonId());
                updateReviewOFAC();
            }         
        }
    } 
    
    public void updateReviewOFAC() {
        try {
            for (Person applicant : applicantList) {
                Long haveReviewOFAC = requestEJB.haveReviewOFACByPerson(applicant.getId());
                if (haveReviewOFAC > 0) {
                    btnReviewOFAC.setVisible(false);
                    EJBRequest request = new EJBRequest();
                    Map params = new HashMap();
                    params.put(Constants.PERSON_KEY, applicant.getId());
                    params.put(Constants.REQUESTS_KEY, adminRequest.getRequest().getId());
                    request.setParams(params);
                    List<ReviewOFAC> reviewOFAC = requestEJB.getReviewOFACByApplicantByRequest(request);
                    for (ReviewOFAC r: reviewOFAC) {
                        applicant.setReviewOFAC(r);
                    }
                }
            }
        } catch (NullParameterException ex) {
                showError(ex);
        } catch (EmptyListException ex) {
                showError(ex);
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
    
    public void onClick$btnReviewOFAC() {
        int indBlackList = 0;
        String lastName = "";
        String firstName = "";
        float ofacPercentege = 0.1F;
        Request request = adminRequest.getRequest();
        WSOFACMethodProxy ofac = new WSOFACMethodProxy();
        try {
            WsLoginResponse loginResponse = new WsLoginResponse();
            loginResponse = ofac.loginWS("alodiga", "d6f80e647631bb4522392aff53370502");
            WsExcludeListResponse ofacResponse = new WsExcludeListResponse();   
            for (Person applicant: applicantList) {
                if (adminRequest.getRequest().getPersonTypeId().getIndNaturalPerson() == true) {
                    lastName = applicant.getApplicantNaturalPerson().getLastNames();
                    firstName = applicant.getApplicantNaturalPerson().getFirstNames();
                    ofacResponse = ofac.queryOFACList(loginResponse.getToken(),lastName, firstName, null, null, null, null, ofacPercentege);
                } else {
                    if (applicant.getPersonClassificationId().getId() == 4) {
                        firstName = applicant.getLegalPerson().getEnterpriseName();
                        ofacResponse = ofac.queryOFACLegalPersonList(loginResponse.getToken(),firstName, ofacPercentege);
                    } else if (applicant.getPersonClassificationId().getId() == 8) {
                        lastName = applicant.getCardRequestNaturalPerson().getLastNames();
                        firstName = applicant.getCardRequestNaturalPerson().getFirstNames();
                        ofacResponse = ofac.queryOFACList(loginResponse.getToken(),lastName, firstName, null, null, null, null, ofacPercentege);
                    } else if (applicant.getPersonClassificationId().getId() == 5) {
                        lastName = applicant.getLegalRepresentatives().getLastNames();
                        firstName = applicant.getLegalRepresentatives().getFirstNames();
                        ofacResponse = ofac.queryOFACList(loginResponse.getToken(),lastName, firstName, null, null, null, null, ofacPercentege);
                    }
                }                
                //Guardar el resultado de revisión en lista OFAC para cada solicitante
                ReviewOFAC reviewOFAC = new ReviewOFAC();
                reviewOFAC.setPersonId(applicant);
                reviewOFAC.setRequestId(request);
                reviewOFAC.setResultReview(Float.valueOf(ofacResponse.getPercentMatch()));
                reviewOFAC = requestEJB.saveReviewOFAC(reviewOFAC);
                
                //Actualizar el estatus del solicitante si tiene coincidencia con lista OFAC                
                if (adminRequest.getRequest().getPersonTypeId().getIndNaturalPerson() == true) {
                    if (Double.parseDouble(ofacResponse.getPercentMatch()) <= 0.75) {
                        applicant.getApplicantNaturalPerson().setStatusApplicantId(getStatusApplicant(Constants.STATUS_APPLICANT_BLACK_LIST));
                        indBlackList = 1;
                    } else {
                      applicant.getApplicantNaturalPerson().setStatusApplicantId(getStatusApplicant(Constants.STATUS_APPLICANT_BLACK_LIST_OK));  
                    }
                    ApplicantNaturalPerson applicantNatural = personEJB.saveApplicantNaturalPerson(applicant.getApplicantNaturalPerson());
                } else {
                    if (applicant.getPersonClassificationId().getId() == 4) {
                        if (Double.parseDouble(ofacResponse.getPercentMatch()) <= 0.75) {
                            applicant.getLegalPerson().setStatusApplicantId(getStatusApplicant(Constants.STATUS_APPLICANT_BLACK_LIST));
                            indBlackList = 1;
                        } else {
                          applicant.getLegalPerson().setStatusApplicantId(getStatusApplicant(Constants.STATUS_APPLICANT_BLACK_LIST_OK));  
                        }
                        LegalPerson applicantLegalPerson = personEJB.saveLegalegalPerson(applicant.getLegalPerson());
                    }
                    if (applicant.getPersonClassificationId().getId() == 8) {
                        if (Double.parseDouble(ofacResponse.getPercentMatch()) <= 0.75) {
                            applicant.getCardRequestNaturalPerson().setStatusApplicantId(getStatusApplicant(Constants.STATUS_APPLICANT_BLACK_LIST));
                            indBlackList = 1;
                        } else {
                          applicant.getCardRequestNaturalPerson().setStatusApplicantId(getStatusApplicant(Constants.STATUS_APPLICANT_BLACK_LIST_OK));  
                        }
                        CardRequestNaturalPerson cardRequestNaturalPerson = personEJB.saveCardRequestNaturalPerson(applicant.getCardRequestNaturalPerson());
                    }
                    if (applicant.getPersonClassificationId().getId() == 5) {
                        if (Double.parseDouble(ofacResponse.getPercentMatch()) <= 0.75) {
                            applicant.getLegalRepresentatives().setStatusApplicantId(getStatusApplicant(Constants.STATUS_APPLICANT_BLACK_LIST));
                            indBlackList = 1;
                        } else {
                          applicant.getLegalRepresentatives().setStatusApplicantId(getStatusApplicant(Constants.STATUS_APPLICANT_BLACK_LIST_OK));  
                        }
                        LegalRepresentatives legalRepresentatives = utilsEJB.saveLegalRepresentatives(applicant.getLegalRepresentatives());
                    }
                }                
            }
            //Si algun(os) solicitante(s) coincide(n) con la Lista OFAC se actualiza estatus de la solicitud
            if (indBlackList == 1) {
                request.setStatusRequestId(getStatusRequest(request,Constants.STATUS_REQUEST_PENDING_APPROVAL));
            } else {
                request.setStatusRequestId(getStatusRequest(request,Constants.STATUS_REQUEST_BLACK_LIST_OK));
            }
            btnReviewOFAC.setVisible(false);
            request = requestEJB.saveRequest(request);
            this.showMessage("sp.common.finishReviewOFAC", false, null);
            onSelect$tabApplicantOFAC();
	} catch (Exception ex) {
            showError(ex);
        }
    }
    
    public StatusApplicant getStatusApplicant(int statusApplicantId) {
        StatusApplicant statusApplicant = null;
        try {
            EJBRequest request = new EJBRequest();
            request.setParam(statusApplicantId);
            statusApplicant = requestEJB.loadStatusApplicant(request);
        } catch (Exception ex) {
            showError(ex);
        }
        return statusApplicant;
    }
    
    public StatusRequest getStatusRequest(Request requestCard, int statusRequestId) {
        StatusRequest statusRequest = requestCard.getStatusRequestId();
        try {
            EJBRequest request = new EJBRequest();
            request.setParam(statusRequestId);
            statusRequest = requestEJB.loadStatusRequest(request);
        } catch (Exception ex) {
            showError(ex);
        }
        return statusRequest;
    }

    public void onClick$btnClear() throws InterruptedException {
        txtName.setText("");
    }    
    
    public void loadDataList(List<Person> list) {
    NumberFormat formatoPorcentaje = NumberFormat.getPercentInstance(); 
        try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                for (Person applicant : list) {
                    item = new Listitem();
                    item.setValue(applicant);
                    if (adminRequest.getRequest().getPersonTypeId().getIndNaturalPerson() == true) {
                        StringBuilder builder = new StringBuilder(applicant.getApplicantNaturalPerson().getFirstNames());
                        builder.append(" ");
                        builder.append(applicant.getApplicantNaturalPerson().getLastNames());                    
                        item.appendChild(new Listcell(builder.toString()));
                        item.appendChild(new Listcell(applicant.getApplicantNaturalPerson().getIdentificationNumber()));
                        if (applicant.getApplicantNaturalPerson().getKinShipApplicantId() == null) {
                            item.appendChild(new Listcell(WebConstants.MAIN_APPLICANT));
                        } else {
                            item.appendChild(new Listcell(applicant.getApplicantNaturalPerson().getKinShipApplicantId().getDescription()));
                        }
                        if (applicant.getApplicantNaturalPerson().getStatusApplicantId() != null) {
                            item.appendChild(new Listcell(applicant.getApplicantNaturalPerson().getStatusApplicantId().getDescription()));
                        } else {
                            item.appendChild(new Listcell("SIN REGISTRAR"));
                        }
                        if (applicant.getApplicantNaturalPerson().getPersonId().getReviewOFAC() != null) {
                            item.appendChild(new Listcell(formatoPorcentaje.format(applicant.getReviewOFAC().getResultReview())));
                        } else {
                            item.appendChild(new Listcell(""));
                        }
                        if(statusEditView == true){
                            item.appendChild(createButtonEditModal(applicant));
                            item.appendChild(createButtonViewModal(applicant));   
                        } else {
                            item.appendChild(new Listcell(""));
                            item.appendChild(createButtonViewModal(applicant));
                        }
                        
                        item.setParent(lbxRecords);
                    } else {
                        if (applicant.getPersonClassificationId().getId() == 4) {
                            item.appendChild(new Listcell(applicant.getLegalPerson().getEnterpriseName()));
                            item.appendChild(new Listcell(applicant.getLegalPerson().getIdentificationNumber()));
                            item.appendChild(new Listcell(WebConstants.MAIN_APPLICANT));
                            if (applicant.getLegalPerson().getStatusApplicantId() != null) {
                                item.appendChild(new Listcell(applicant.getLegalPerson().getStatusApplicantId().getDescription()));
                            } else {
                                item.appendChild(new Listcell("SIN REGISTRAR"));
                            } 
                            if (applicant.getLegalPerson().getPersonId().getReviewOFAC() != null) {
                                item.appendChild(new Listcell(formatoPorcentaje.format(applicant.getReviewOFAC().getResultReview())));
                            } else {
                                item.appendChild(new Listcell(""));
                            }
                            item.appendChild(createButtonEditModal(applicant));
                            item.appendChild(createButtonViewModal(applicant));
                            item.setParent(lbxRecords);
                        } else if (applicant.getPersonClassificationId().getId() == 8) {
                            StringBuilder builder = new StringBuilder(applicant.getCardRequestNaturalPerson().getFirstNames());
                            builder.append(" ");
                            builder.append(applicant.getCardRequestNaturalPerson().getLastNames());
                            item.appendChild(new Listcell(builder.toString()));
                            item.appendChild(new Listcell(applicant.getCardRequestNaturalPerson().getIdentificationNumber()));
                            item.appendChild(new Listcell(WebConstants.CARD_REQUEST_NATURAL_PERSON));
                            if (applicant.getCardRequestNaturalPerson().getStatusApplicantId() != null) {
                                item.appendChild(new Listcell(applicant.getCardRequestNaturalPerson().getStatusApplicantId().getDescription()));
                            } else {
                                item.appendChild(new Listcell("SIN REGISTRAR"));
                            }
                            if (applicant.getCardRequestNaturalPerson().getPersonId().getReviewOFAC() != null) {
                                item.appendChild(new Listcell(formatoPorcentaje.format(applicant.getReviewOFAC().getResultReview())));
                            } else {
                                item.appendChild(new Listcell(""));
                            }
                            if(statusEditView == true){
                                item.appendChild(createButtonEditModal(applicant));
                                item.appendChild(createButtonViewModal(applicant));   
                            } else {
                                item.appendChild(new Listcell(""));
                                item.appendChild(createButtonViewModal(applicant));
                            }
                            item.setParent(lbxRecords);
                        } else if (applicant.getPersonClassificationId().getId() == 5) {
                            StringBuilder builder = new StringBuilder(applicant.getLegalRepresentatives().getFirstNames());
                            builder.append(" ");
                            builder.append(applicant.getLegalRepresentatives().getLastNames());                    
                            item.appendChild(new Listcell(builder.toString()));
                            item.appendChild(new Listcell(applicant.getLegalRepresentatives().getIdentificationNumber()));
                            item.appendChild(new Listcell(WebConstants.LEGAL_REPRESENTATIVE));
                            if (applicant.getLegalRepresentatives().getStatusApplicantId() != null) {
                                item.appendChild(new Listcell(applicant.getLegalRepresentatives().getStatusApplicantId().getDescription()));
                            } else {
                                item.appendChild(new Listcell("SIN REGISTRAR"));
                            }
                            if (applicant.getLegalRepresentatives().getPersonId().getReviewOFAC() != null) {
                                item.appendChild(new Listcell(formatoPorcentaje.format(applicant.getReviewOFAC().getResultReview())));
                            } else {
                                item.appendChild(new Listcell(""));
                            }
                            if(statusEditView == true){
                                item.appendChild(createButtonEditModal(applicant));
                                item.appendChild(createButtonViewModal(applicant));   
                            } else {
                                item.appendChild(new Listcell(""));
                                item.appendChild(createButtonViewModal(applicant));
                            }
                            item.setParent(lbxRecords);
                        }                        
                    }      
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
    
    
    
    public List<Person> getFilterList(String filter) {
        List<Person> personList_ = new ArrayList<Person>();
        try {
            if (adminRequest.getRequest().getPersonTypeId().getIndNaturalPerson() == true) {
                if (filter != null && !filter.equals("")) {
                    EJBRequest request1 = new EJBRequest();
                    Map params = new HashMap();
                    params.put(Constants.APPLICANT_NATURAL_PERSON_KEY, adminRequest.getRequest().getPersonId().getApplicantNaturalPerson().getId());
                    params.put(Constants.REQUESTS_KEY, adminRequest.getRequest().getId());
                    params.put(Constants.PARAM_PERSON_NAME, filter);
                    request1.setParams(params);
                    personList_ = personEJB.searchPersonByApplicantNaturalPerson(request1);
                } else {
                    return applicantList;
                } 
            } else {
                if (filter != null && !filter.equals("")) {;
                    EJBRequest request1 = new EJBRequest();
                    Map params = new HashMap();
                    params.put(Constants.PERSON_KEY , adminRequest.getRequest().getPersonId().getLegalPerson().getId());
                    params.put(Constants.REQUESTS_KEY, adminRequest.getRequest().getId());
                    params.put(Constants.PARAM_PERSON_NAME, filter);
                    request1.setParams(params);
                    personList_ = personEJB.searchPersonByLegalPerson(request1);
                } else {
                    return applicantList;
                }    
            }
        } catch (Exception ex) {
            showError(ex);
        }
        return personList_;
    }
    
     public void onClick$btnSearch() throws InterruptedException {
        try {
            loadDataList(getFilterList(txtName.getText()));
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
}