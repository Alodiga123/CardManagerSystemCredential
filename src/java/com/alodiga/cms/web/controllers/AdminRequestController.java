package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.ProgramEJB;
import com.alodiga.cms.commons.ejb.RequestEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.enumeraciones.StatusApplicantE;
import com.cms.commons.enumeraciones.StatusRequestE;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.ApplicantNaturalPerson;
import com.cms.commons.models.CollectionsRequest;
import com.cms.commons.models.Country;
import com.cms.commons.models.Person;
import com.cms.commons.models.PersonHasAddress;
import com.cms.commons.models.PersonType;
import com.cms.commons.models.ProductType;
import com.cms.commons.models.Program;
import com.cms.commons.models.Request;
import com.cms.commons.models.RequestHasCollectionsRequest;
import com.cms.commons.models.RequestType;
import com.cms.commons.models.Sequences;
import com.cms.commons.models.StatusRequest;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;

import org.zkoss.zul.Toolbarbutton;

public class AdminRequestController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblRequestNumber;
    private Label lblRequestDate;
    private Label lblStatusRequest;
    private Request requestParam;
    public static Request requestCard = null;
    private UtilsEJB utilsEJB = null;
    private ProgramEJB programEJB = null;
    private PersonEJB personEJB = null;
    private RequestEJB requestEJB = null;
    private Combobox cmbCountry;
    private Combobox cmbPrograms;
    private Combobox cmbPersonType;
    private Combobox cmbProductType;
    private Combobox cmbRequestType;
    private Button btnSave;
    private Tab tabMain;
    private Tab tabAddress;
    private Tab tabFamilyReferencesMain;
    private Tab tabAdditionalCards;
    private Tab tabLegalRepresentatives;
    private Tab tabApplicantOFAC;
    private Tab tabRequestbyCollection;
    private Tab tabApplicationReview;
    public static Integer eventType;
    private Toolbarbutton tbbTitle;
    public Tabbox tb;
    private ListRequestController listRequest = null;
    private boolean indNaturalPerson;
    private List<PersonHasAddress> personHasAddress = null;
    private List<ApplicantNaturalPerson> applicantNaturalPersonList = null;
    private List<ApplicantNaturalPerson> applicantCardsComplementariesPersonList = null;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
        requestEJB = (RequestEJB) EJBServiceLocator.getInstance().get(EjbConstants.REQUEST_EJB);
        listRequest = new ListRequestController();
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            requestParam = null;
        } else {
            requestParam = (Request) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                if (requestParam.getPersonId() != null) {
                    if (requestParam.getIndPersonNaturalRequest() == true) {
                        tabMain.setDisabled(false);
                        tabAddress.setDisabled(false);
                        tabFamilyReferencesMain.setDisabled(false);
                        tabAdditionalCards.setDisabled(false);
                        activeTabOFAC();
                        activeTabRequestsCollections();
                        activateTabApplicationReview();
//                        tabRequestbyCollection.setDisabled(false);
//                        tabApplicationReview.setDisabled(false);
                    } else {
                        tabMain.setDisabled(false);
                        tabAddress.setDisabled(false);
                        tabLegalRepresentatives.setDisabled(false);
                        tabAdditionalCards.setDisabled(false);
                        activeTabOFAC();
                        tabRequestbyCollection.setDisabled(true);
                        tabApplicationReview.setDisabled(true);
                    }
                } else {
                    if (requestParam.getIndPersonNaturalRequest() == true) {
                        tabMain.setDisabled(false);
                        tabAddress.setDisabled(true);
                        tabFamilyReferencesMain.setDisabled(true);
                        tabAdditionalCards.setDisabled(true);
                        tabApplicantOFAC.setDisabled(true);
                        tabRequestbyCollection.setDisabled(true);
                        tabApplicationReview.setDisabled(true);
                    } else {
                        //OJO Validar que la solicitud tenga al menos una tarjeta adicional 
                        tabMain.setDisabled(false);
                        tabAddress.setDisabled(true);
                        tabLegalRepresentatives.setDisabled(true);
                        tabAdditionalCards.setDisabled(true);
                        tabApplicantOFAC.setDisabled(true);
                        tabRequestbyCollection.setDisabled(true);
                        tabApplicationReview.setDisabled(true);
                    }
                }
                tbbTitle.setLabel(Labels.getLabel("cms.crud.request.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                if (requestParam.getPersonId() != null) {
                   if (requestParam.getIndPersonNaturalRequest() == true) {
                    tabMain.setDisabled(false);
                    tabAddress.setDisabled(false);
                    tabFamilyReferencesMain.setDisabled(false);
                    tabAdditionalCards.setDisabled(false);
                    activeTabOFAC();
                    activeTabRequestsCollections();
                    activateTabApplicationReview();
                    blockFields();
                } else {
                     tabMain.setDisabled(false);
                     tabAddress.setDisabled(false);
                     tabLegalRepresentatives.setDisabled(false);
                     tabAdditionalCards.setDisabled(false);
                     tabRequestbyCollection.setDisabled(false);
                     tabApplicationReview.setDisabled(false);
                    blockFields();
                }
            }
                tbbTitle.setLabel(Labels.getLabel("cms.crud.request.view"));
                break;
            case WebConstants.EVENT_ADD:
                if (listRequest.getAddRequestPerson() == 1) {
                    tabMain.setDisabled(true);
                    tabAddress.setDisabled(true);
                    tabFamilyReferencesMain.setDisabled(true);
                    tabAdditionalCards.setDisabled(true);
                    tabApplicantOFAC.setDisabled(true);
                    tabRequestbyCollection.setDisabled(true);
                    tabApplicationReview.setDisabled(true);
                    indNaturalPerson = true;
                } else {
                    tabMain.setDisabled(true);
                    tabAddress.setDisabled(true);
                    tabLegalRepresentatives.setDisabled(true);
                    tabAdditionalCards.setDisabled(true);
                    tabRequestbyCollection.setDisabled(true);
                    tabApplicationReview.setDisabled(true);
                    indNaturalPerson = false;
                }
                tbbTitle.setLabel(Labels.getLabel("cms.crud.request.add"));
                break;
            default:
                break;
        }
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            programEJB = (ProgramEJB) EJBServiceLocator.getInstance().get(EjbConstants.PROGRAM_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public Request getRequest() {
        return this.requestCard;
    }

    public Integer getEventType() {
        return this.eventType;
    }
    
    public void activeTabOFAC(){
        Person person = null;
        personHasAddress = new ArrayList<PersonHasAddress>();
        person = requestParam.getPersonId();
        try{
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.PERSON_KEY, person.getId());
            request1.setParams(params);
            personHasAddress = personEJB.getPersonHasAddressesByPerson(request1); 
        } catch (Exception ex) {
            showError(ex);
        } finally {
            if (personHasAddress.size() > 0){
                tabApplicantOFAC.setDisabled(false);
            } else {
                tabApplicantOFAC.setDisabled(true);
            }
        }
    }
    
    public void activeTabRequestsCollections(){
        String statusRequestCodeLineOK= StatusRequestE.LINEOK.getStatusRequestCode();
        // Hacer validacion de que si esta un status arriba de lineOk igual desbloquee el tab
        if (requestParam.getStatusRequestId().getCode().equals(statusRequestCodeLineOK)){
            tabRequestbyCollection.setDisabled(false);
        } else {
            tabRequestbyCollection.setDisabled(true);
        }
    }
    
    public void activateTabApplicationReview(){
         String statusRequestApproved= StatusRequestE.RECAPR.getStatusRequestCode(); 
         // Hacer validacion de que si esta un status arriba de lineOk igual desbloquee el tab
         if(requestParam.getStatusRequestId().getCode().equals(statusRequestApproved)){
            tabApplicationReview.setDisabled(false);
         } else {
            tabApplicationReview.setDisabled(true); 
         }
  
    }
    
    public void onChange$cmbCountry() {
        this.clearMessage();
        cmbPersonType.setVisible(true);
        cmbPersonType.setValue("");
        Country country = (Country) cmbCountry.getSelectedItem().getValue();
        loadCmbPersonType(eventType, country.getId());
    }

    public void onChange$cmbProductType() {
        cmbPrograms.setVisible(true);
        ProductType productType = (ProductType) cmbProductType.getSelectedItem().getValue();
        loadCmbProgram(eventType, productType.getId());
    }

    public void clearFields() {

    }

    private void loadFields(Request requestData) {
        try {
            String pattern = "dd-MM-yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            
            if (requestData.getRequestNumber() != null) {
                lblRequestNumber.setValue(requestData.getRequestNumber());
                lblRequestDate.setValue(simpleDateFormat.format(requestData.getRequestDate()));
                lblStatusRequest.setValue(requestData.getStatusRequestId().getDescription());
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        btnSave.setVisible(false);
    }
    
    public void blockCmbs(){
        cmbCountry.setDisabled(true);
        cmbPrograms.setDisabled(true);
        cmbPersonType.setDisabled(true);
        cmbProductType.setDisabled(true);
        cmbRequestType.setDisabled(true);
        btnSave.setVisible(false);
    }
    
    
    
    public Boolean validateEmpty() {
        if (cmbCountry.getSelectedItem()  == null) {
            cmbCountry.setFocus(true);
            this.showMessage("cms.error.country.notSelected", true, null);     
        } else if (cmbPersonType.getSelectedItem() == null) {
            cmbPersonType.setFocus(true);
            this.showMessage("cms.error.personType.notSelected", true, null);
        } else if (cmbProductType.getSelectedItem() == null) {
            cmbProductType.setFocus(true);
            this.showMessage("cms.error.requestTarjetType", true, null);
        } else if (cmbPrograms.getSelectedItem() == null) {
            cmbPrograms.setFocus(true);
            this.showMessage("cms.error.program.notSelected", true, null);
        } else if (cmbRequestType.getSelectedItem() == null) {
            cmbRequestType.setFocus(true);
            this.showMessage("cms.error.requestTipeSolicitude", true, null);
        }
        else {
            return true;
        }
        return false;

    }

    private void saveRequest(Request _request) {
        boolean indPersonNaturalRequest = true;
        EJBRequest request1 = new EJBRequest();
        String numberRequest = "";
        Date dateRequest = null;
        try {
            Request request = null;

            if (_request != null) {
                request = _request;
                numberRequest = request.getRequestNumber();
                dateRequest = request.getRequestDate();
                indPersonNaturalRequest = request.getIndPersonNaturalRequest();
            } else {//New Request
                request = new Request();
                ListRequestController listRequest = new ListRequestController();
                if (listRequest.getAddRequestPerson() == 1) {
                    indPersonNaturalRequest = true;
                } else {
                    indPersonNaturalRequest = false;
                }
                //Obtiene el numero de secuencia para documento Request
                Map params = new HashMap();
                params.put(Constants.DOCUMENT_TYPE_KEY, Constants.DOCUMENT_TYPE_REQUEST);
                request1.setParams(params);
                List<Sequences> sequence = utilsEJB.getSequencesByDocumentType(request1);
                numberRequest = utilsEJB.generateNumberSequence(sequence, Constants.ORIGIN_APPLICATION_CMS_ID);
                dateRequest = new Date();
                lblRequestNumber.setValue(numberRequest);
                lblRequestDate.setValue(dateRequest.toString());
                
            }

            //colocar estatus de solicitud "EN PROCESO"
            request1 = new EJBRequest();
            request1.setParam(Constants.STATUS_REQUEST_IN_PROCESS);
            StatusRequest statusRequest = requestEJB.loadStatusRequest(request1);
            lblStatusRequest.setValue(statusRequest.getDescription());

            //Guarda la solicitud en la BD
            request.setRequestNumber(numberRequest);
            request.setRequestDate(dateRequest);
            request.setStatusRequestId(statusRequest);
            request.setCountryId((Country) cmbCountry.getSelectedItem().getValue());
            request.setPersonTypeId((PersonType) cmbPersonType.getSelectedItem().getValue());
            request.setProductTypeId((ProductType) cmbProductType.getSelectedItem().getValue());
            request.setProgramId((Program) cmbPrograms.getSelectedItem().getValue());
            request.setRequestTypeId((RequestType) cmbRequestType.getSelectedItem().getValue());
            request.setIndPersonNaturalRequest(indPersonNaturalRequest);
            if (eventType != 1) {
                request.setUpdateDate(new Timestamp(new Date().getTime()));
            }
            request = requestEJB.saveRequest(request);
            requestParam = request;
            this.showMessage("sp.common.save.success", false, null);
            if (eventType == WebConstants.EVENT_ADD) {
                btnSave.setVisible(false);
            } else {
                btnSave.setVisible(true);
            }
            loadFields(requestParam);
            tabMain.setDisabled(false);
            requestCard = request;
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnSave() {
       if (validateEmpty()) {
        switch (eventType) {
            case WebConstants.EVENT_ADD:
                saveRequest(null);
                break;
            case WebConstants.EVENT_EDIT:
                saveRequest(requestParam);
                break;
            default:
                break;
            }
        } 
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                requestCard = requestParam;
                loadFields(requestParam);
                loadCmbCountry(eventType);
                loadCmbProductType(eventType);
                loadCmbRequestType(eventType);
                onChange$cmbCountry();
                onChange$cmbProductType();
                blockCmbs();
                break;
            case WebConstants.EVENT_VIEW:
                requestCard = requestParam;
                loadFields(requestParam);
                loadCmbCountry(eventType);
                loadCmbProductType(eventType);
                loadCmbRequestType(eventType);
                onChange$cmbCountry();
                onChange$cmbProductType();
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                loadCmbCountry(eventType);
                loadCmbProductType(eventType);
                loadCmbRequestType(eventType);
                break;
            default:
                break;
        }
    }

    private void loadCmbCountry(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<Country> countries;
        try {
            countries = utilsEJB.getCountries(request1);
            loadGenericCombobox(countries, cmbCountry, "name", evenInteger, Long.valueOf(requestParam != null ? requestParam.getCountryId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (GeneralException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (NullParameterException ex) {
            showError(ex);
            ex.printStackTrace();
        }
    }

    private void loadCmbProductType(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<ProductType> productTypes;
        try {
            productTypes = utilsEJB.getProductTypes(request1);
            loadGenericCombobox(productTypes, cmbProductType, "name", evenInteger, Long.valueOf(requestParam != null ? requestParam.getProductTypeId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (GeneralException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (NullParameterException ex) {
            showError(ex);
            ex.printStackTrace();
        }
    }

    private void loadCmbProgram(Integer evenInteger, Integer productTypeId) {
        EJBRequest request1 = new EJBRequest();
        cmbPrograms.getItems().clear();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_PRODUCT_TYPE_ID, productTypeId);
        request1.setParams(params);
        List<Program> programs;
        try {
            programs = programEJB.getProgramByProductType(request1);
            loadGenericCombobox(programs, cmbPrograms, "name", evenInteger, Long.valueOf(requestParam != null ? requestParam.getProgramId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (GeneralException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (NullParameterException ex) {
            showError(ex);
            ex.printStackTrace();
        }
    }

    private void loadCmbPersonType(Integer evenInteger, int countryId) {
        EJBRequest request1 = new EJBRequest();
        cmbPersonType.getItems().clear();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_COUNTRY_ID, countryId);
        if (eventType == WebConstants.EVENT_ADD) {
            params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, Constants.ORIGIN_APPLICATION_CMS_ID);
        } else {
            params.put(QueryConstants.PARAM_ORIGIN_APPLICATION_ID, requestParam.getPersonTypeId().getOriginApplicationId().getId());
        }
        if (evenInteger == 1) {
            params.put(QueryConstants.PARAM_IND_NATURAL_PERSON, indNaturalPerson);
        } else {
            params.put(QueryConstants.PARAM_IND_NATURAL_PERSON, requestParam.getPersonTypeId().getIndNaturalPerson());
        }
        request1.setParams(params);
        List<PersonType> personTypes = null;
        try {
            personTypes = utilsEJB.getPersonTypeByCountryByIndNaturalPerson(request1);
            loadGenericCombobox(personTypes, cmbPersonType, "description", evenInteger, Long.valueOf(requestParam != null ? requestParam.getPersonTypeId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (GeneralException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (NullParameterException ex) {
            showError(ex);
        } finally {
            if (personTypes == null) {
                this.showMessage("cms.msj.PersonTypeNull", false, null);
         }            
      }
   }

    private void loadCmbRequestType(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<RequestType> requestTypeList;
        try {
            requestTypeList = utilsEJB.getRequestType(request1);
            loadGenericCombobox(requestTypeList, cmbRequestType, "description", evenInteger, Long.valueOf(requestParam != null ? requestParam.getRequestTypeId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (GeneralException ex) {
            showError(ex);
            ex.printStackTrace();
        } catch (NullParameterException ex) {
            showError(ex);
            ex.printStackTrace();
        }
    }

}
