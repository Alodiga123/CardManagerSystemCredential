package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.commons.ejb.RequestEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.enumeraciones.StatusApplicantE;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.ApplicantNaturalPerson;
import com.cms.commons.models.CardRequestNaturalPerson;
import com.cms.commons.models.CollectionsRequest;
import com.cms.commons.models.FamilyReferences;
import com.cms.commons.models.LegalCustomer;
import com.cms.commons.models.LegalCustomerHasLegalRepresentatives;
import com.cms.commons.models.LegalPerson;
import com.cms.commons.models.LegalPersonHasLegalRepresentatives;
import com.cms.commons.models.NaturalCustomer;
import com.cms.commons.models.Person;
import com.cms.commons.models.PersonClassification;
import com.cms.commons.models.PersonHasAddress;
import com.cms.commons.models.PhonePerson;
import com.cms.commons.models.Product;
import com.cms.commons.models.ReasonRejectionRequest;
import com.cms.commons.models.Request;
import com.cms.commons.models.RequestHasCollectionsRequest;
import com.cms.commons.models.ReviewRequest;
import com.cms.commons.models.ReviewRequestType;
import com.cms.commons.models.StatusApplicant;
import com.cms.commons.models.StatusCustomer;
import com.cms.commons.models.StatusRequest;
import com.cms.commons.models.User;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;

public class AdminApplicationReviewController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblRequestNumber;
    private Label lblRequestDate;
    private Label lblStatusRequest;
    private Label txtCity;
    private Label txtAgency;
    private Label txtCommercialAssessorUserCode;
    private Label txtAssessorName;
    private Label txtIdentification;
    private Doublebox txtMaximumRechargeAmount;
    private Textbox txtObservations;
    private Datebox txtReviewDate;
    private Combobox cmbProduct;
    private Radio rApprovedYes;
    private Radio rApprovedNo;
    private ProductEJB productEJB = null;
    private User user = null;
    private RequestEJB requestEJB = null;
    private PersonEJB personEJB = null;
    private UtilsEJB utilsEJB = null;
    private ReviewRequest reviewCollectionsRequestParam;
    private List<ReviewRequest> reviewCollectionsRequest;
    private Button btnSave;
    private Request requestCard;
    private Request requestNumber = null;
    private List<RequestHasCollectionsRequest> requestHasCollectionsRequestList;
    private List<CollectionsRequest> collectionsByRequestList;
    private List<ApplicantNaturalPerson> cardComplementaryList = null;
    private NaturalCustomer naturalCustomerParent = null;
    public static Person customer = null;
    private AdminRequestController adminRequest = null;
    private AdminNaturalPersonController adminNaturalPerson = null;
    private ApplicantNaturalPerson applicantNaturalPerson = null;
    private List<PhonePerson> phonePersonList = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        adminRequest = new AdminRequestController();
        adminNaturalPerson = new AdminNaturalPersonController();
        if (adminRequest.getRequest() != null) {
            requestCard = adminRequest.getRequest();
            eventType = adminRequest.getEventType();
            if (adminNaturalPerson.getApplicantNaturalPerson() != null) {
                applicantNaturalPerson = adminNaturalPerson.getApplicantNaturalPerson();
            }
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            user = (User) session.getAttribute(Constants.USER_OBJ_SESSION);
            productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
            requestEJB = (RequestEJB) EJBServiceLocator.getInstance().get(EjbConstants.REQUEST_EJB);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            getReviewCollectionsRequestParam();
            this.clearMessage();
        } catch (Exception ex) {
            showError(ex);
        } finally {
            loadData();
        }
    }

    public ReviewRequest getReviewCollectionsRequestParam() {
        try {
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(QueryConstants.PARAM_REQUEST_ID, requestCard.getId());
            params.put(QueryConstants.PARAM_REVIEW_REQUEST_TYPE_ID, Constants.REVIEW_REQUEST_TYPE_COLLECTIONS);
            request1.setParams(params);
            reviewCollectionsRequest = requestEJB.getReviewRequestByRequest(request1);
            for (ReviewRequest r : reviewCollectionsRequest) {
                reviewCollectionsRequestParam = r;
            }
        } catch (Exception ex) {
            showError(ex);
        }
        return reviewCollectionsRequestParam;
    }

    public void clearFields() {
        txtMaximumRechargeAmount.setRawValue(null);
        txtReviewDate.setRawValue(null);
        txtObservations.setRawValue(null);
    }

    private void loadField(Request requestData) {
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

    private void loadUser() {
        txtCity.setValue(user.getComercialAgencyId().getCityId().getName());
        txtAgency.setValue(user.getComercialAgencyId().getName());
        txtCommercialAssessorUserCode.setValue(user.getCode());
        txtAssessorName.setValue(user.getFirstNames() + " " + user.getLastNames());
        txtIdentification.setValue(user.getIdentificationNumber());
    }

    private void loadDate() {
        Date today = new Date();
        txtReviewDate.setValue(today);
    }

    private void loadFields(ReviewRequest reviewCollectionsRequest) throws EmptyListException, GeneralException, NullParameterException {
        try {
            if (reviewCollectionsRequest != null) {
                NumberFormat n = NumberFormat.getCurrencyInstance();
                txtCity.setValue(reviewCollectionsRequest.getUserId().getComercialAgencyId().getCityId().getName());
                txtAgency.setValue(reviewCollectionsRequest.getUserId().getComercialAgencyId().getName());
                txtCommercialAssessorUserCode.setValue(reviewCollectionsRequest.getUserId().getCode());
                txtAssessorName.setValue(reviewCollectionsRequest.getUserId().getFirstNames() + " " + reviewCollectionsRequest.getUserId().getLastNames());
                txtIdentification.setValue(reviewCollectionsRequest.getUserId().getIdentificationNumber());
                if (reviewCollectionsRequest.getMaximumRechargeAmount() != null) {
                    txtMaximumRechargeAmount.setText(reviewCollectionsRequest.getMaximumRechargeAmount().toString());
                }
                if (reviewCollectionsRequest.getReviewDate() != null) {
                    txtReviewDate.setValue(reviewCollectionsRequest.getReviewDate());
                }
                if (reviewCollectionsRequest.getObservations() != null) {
                    txtObservations.setText(reviewCollectionsRequest.getObservations());
                }
                if (reviewCollectionsRequest.getIndApproved() != null) {
                    if (reviewCollectionsRequest.getIndApproved() == true) {
                        rApprovedYes.setChecked(true);
                        if (reviewCollectionsRequest.getRequestId().getStatusRequestId().getId() != Constants.STATUS_REQUEST_COLLECTIONS_WITHOUT_APPROVAL) {
                            blockFields();
                        }
                        cmbProduct.setDisabled(true);
                    } else {
                        rApprovedNo.setChecked(true);
                    }
                }
            } else {
                txtCity = null;
                txtAgency = null;
                txtCommercialAssessorUserCode = null;
                txtAssessorName = null;
            }
        } catch (Exception ex) {
            showError(ex);
        } finally {
            txtCity.setValue(user.getComercialAgencyId().getCityId().getName());
            txtAgency.setValue(user.getComercialAgencyId().getName());
            txtCommercialAssessorUserCode.setValue(user.getCode());
            txtAssessorName.setValue(user.getFirstNames() + " " + user.getLastNames());
        }
    }

    public void blockFields() {
        txtReviewDate.setDisabled(true);
        txtMaximumRechargeAmount.setReadonly(true);
        txtObservations.setReadonly(true);
        rApprovedYes.setDisabled(true);
        rApprovedNo.setDisabled(true);
        cmbProduct.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtMaximumRechargeAmount.getText().isEmpty()) {
            txtMaximumRechargeAmount.setFocus(true);
            this.showMessage("cms.error.maximumRechargeAmount", true, null);
        } else if (cmbProduct.getSelectedItem() == null) {
            cmbProduct.setFocus(true);
            this.showMessage("cms.error.product.notSelected", true, null);
        } else if ((!rApprovedYes.isChecked()) && (!rApprovedNo.isChecked())) {
            this.showMessage("cms.error.field.approved", true, null);
        } else if (txtObservations.getText().isEmpty()) {
            txtObservations.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveReviewCollectionsRequest(ReviewRequest _reviewCollectionsRequest) {
        try {
            ReviewRequest reviewCollectionsRequest = null;
            boolean indApproved;
            int indReviewCollectionApproved = 0;
            int indReviewCollectionIncomplete = 0;

            if (_reviewCollectionsRequest != null) {
                reviewCollectionsRequest = _reviewCollectionsRequest;
            } else {
                reviewCollectionsRequest = new ReviewRequest();
            }

            if (rApprovedYes.isChecked()) {
                indApproved = true;
            } else {
                indApproved = false;
            }

            //Obtiene el tipo de revision Recaudos
            EJBRequest request = new EJBRequest();
            request.setParam(Constants.REVIEW_REQUEST_TYPE_COLLECTIONS);
            ReviewRequestType reviewRequestType = requestEJB.loadReviewRequestType(request);

            if (rApprovedYes.isChecked()) {
                //Recaudos que han sido revisados por Agente Comercial
                Map params = new HashMap();
                EJBRequest request1 = new EJBRequest();
                params.put(Constants.REQUESTS_KEY, adminRequest.getRequest().getId());
                request1.setParams(params);
                requestHasCollectionsRequestList = requestEJB.getRequestsHasCollectionsRequestByRequest(request1);
                //Recaudos asociados a la Solicitud
                params = new HashMap();
                params.put(Constants.COUNTRY_KEY, adminRequest.getRequest().getCountryId().getId());
                params.put(Constants.PROGRAM_KEY, adminRequest.getRequest().getProgramId().getId());
                params.put(Constants.PRODUCT_TYPE_KEY, adminRequest.getRequest().getProductTypeId().getId());
                params.put(Constants.PERSON_TYPE_KEY, adminRequest.getRequest().getPersonTypeId().getId());
                request1.setParams(params);
                collectionsByRequestList = requestEJB.getCollectionsByRequest(request1);
                //Se chequea si hay recaudos sin revisar
                if (collectionsByRequestList.size() > requestHasCollectionsRequestList.size()) {
                    indReviewCollectionIncomplete = 1;
                }
                for (RequestHasCollectionsRequest r : requestHasCollectionsRequestList) {
                    if (r.getIndApproved() == 0) {
                        indReviewCollectionApproved = 1;
                    }
                    if (r.getUrlImageFile() == null) {
                        indReviewCollectionIncomplete = 1;
                    }
                }
            }

            //Guarda la revision
            reviewCollectionsRequest.setRequestId(requestCard);
            reviewCollectionsRequest.setReviewDate(txtReviewDate.getValue());
            reviewCollectionsRequest.setMaximumRechargeAmount(txtMaximumRechargeAmount.getValue().floatValue());
            reviewCollectionsRequest.setUserId(user);
            reviewCollectionsRequest.setProductId((Product) cmbProduct.getSelectedItem().getValue());
            reviewCollectionsRequest.setObservations(txtObservations.getText());
            reviewCollectionsRequest.setReviewRequestTypeId(reviewRequestType);
            reviewCollectionsRequest.setIndApproved(indApproved);
            reviewCollectionsRequest.setCreateDate(new Timestamp(new Date().getTime()));
            reviewCollectionsRequest = requestEJB.saveReviewRequest(reviewCollectionsRequest);

            //Actualiza el agente comercial en la solicitud de tarjeta
            requestCard.setUserId(user);
            requestCard = requestEJB.saveRequest(requestCard);

            //Si los recaudos están incompletos, se rechaza la solicitud
            if (indReviewCollectionIncomplete == 1) {
                updateRequestByCollectionsIncomplete(requestCard);
            } else {
                //Verificar que todos los recaudos estén aprobados y que la solicitud este aprobada por el agente comercial
                if (indReviewCollectionApproved == 0 && reviewCollectionsRequest.getIndApproved() == true) {
                    //Se aprueba la solicitud
                    requestCard.setStatusRequestId(getStatusRequest(requestCard, Constants.STATUS_REQUEST_APPROVED));
                    requestCard = requestEJB.saveRequest(requestCard);
                    //Verificar si el solicitante es jurídico o natural
                    if (requestCard.getIndPersonNaturalRequest() == true) {
                        //Se crea el cliente natural
                        saveNaturalCustomer(requestCard);
                    } else {
                        //Se crea el cliente jurídico
                        saveLegalCustomer(requestCard);
                    }
                    this.showMessage("cms.common.save.success.customer", false, null);
                } else {
                    UpdateRequestWithoutApproving(reviewCollectionsRequest);
                }
            }
            btnSave.setVisible(false);
        } catch (Exception ex) {
            showError(ex);
        }
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

    public void UpdateRequestWithoutApproving(ReviewRequest reviewCollectionsRequest) {
        boolean indApproved;
        try {
            updateRequestByCollectionsWithoutApproval(requestCard);
            rApprovedYes.setChecked(false);
            rApprovedNo.setChecked(true);
            indApproved = false;
            reviewCollectionsRequest.setIndApproved(indApproved);
            reviewCollectionsRequest = requestEJB.saveReviewRequest(reviewCollectionsRequest);
            this.showMessage("cms.common.requestNotApproved", false, null);
        } catch (Exception ex) {
            showError(ex);
        }

    }

    public void updateRequestByCollectionsWithoutApproval(Request requestCard) {
        try {
            EJBRequest request = new EJBRequest();
            request.setParam(Constants.STATUS_REQUEST_COLLECTIONS_WITHOUT_APPROVAL);
            StatusRequest statusRequestRejected = requestEJB.loadStatusRequest(request);
            requestCard.setStatusRequestId(statusRequestRejected);
            requestCard = requestEJB.saveRequest(requestCard);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void updateRequestByCollectionsIncomplete(Request requestCard) {
        try {
            EJBRequest request = new EJBRequest();
            request.setParam(Constants.STATUS_REQUEST_REJECTED);
            StatusRequest statusRequestRejected = requestEJB.loadStatusRequest(request);

            requestCard.setStatusRequestId(statusRequestRejected);
            request.setParam(Constants.REASON_REQUEST_REJECTED_BY_COLLECTIONS);
            ReasonRejectionRequest reasonRejectionRequest = requestEJB.loadReasonRejectionRequest(request);

            requestCard.setReasonRejectionRequestId(reasonRejectionRequest);
            requestCard = requestEJB.saveRequest(requestCard);
            this.showMessage("cms.error.requestRejected", false, null);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void saveNaturalCustomer(Request requestCard) {
        try {
            Person person = new Person();
            NaturalCustomer naturalCustomer = new NaturalCustomer();
            PhonePerson phonePerson = new PhonePerson();
            ApplicantNaturalPerson applicant = requestCard.getPersonId().getApplicantNaturalPerson();
            
            //Se actualiza el estatus del solicitante a APROBADO
            EJBRequest request = new EJBRequest();
            request.setParam(StatusApplicantE.APROBA.getId());
            StatusApplicant statusApplicant = requestEJB.loadStatusApplicant(request); 
            applicant.setStatusApplicantId(statusApplicant);
            applicant = personEJB.saveApplicantNaturalPerson(applicant);

            //Guardar la persona asociada al cliente
            person.setCountryId(requestCard.getPersonId().getCountryId());
            person.setPersonTypeId(requestCard.getPersonId().getPersonTypeId());
            person.setEmail(requestCard.getPersonId().getEmail());
            person.setCreateDate(new Timestamp(new Date().getTime()));
            person.setPersonClassificationId(getClassificationCustomer());
            person = personEJB.savePerson(person);

            //Guarda el Cliente
            naturalCustomer.setPersonId(person);
            naturalCustomer.setDocumentsPersonTypeId(applicant.getDocumentsPersonTypeId());
            naturalCustomer.setIdentificationNumber(applicant.getIdentificationNumber());
            naturalCustomer.setDueDateDocumentIdentification(applicant.getDueDateDocumentIdentification());
            naturalCustomer.setStatusCustomerId(getStatusActiveCustomer());
            naturalCustomer.setFirstNames(applicant.getFirstNames());
            naturalCustomer.setLastNames(applicant.getLastNames());
            if (applicant.getMarriedLastName() != null) {
                naturalCustomer.setMarriedLastName(applicant.getMarriedLastName());
            }
            naturalCustomer.setGender(applicant.getGender());
            if (applicant.getPlaceBirth() != null) {
                naturalCustomer.setPlaceBirth(applicant.getPlaceBirth());
            }
            naturalCustomer.setDateBirth(applicant.getDateBirth());
            naturalCustomer.setCivilStatusId(applicant.getCivilStatusId());
            if (applicant.getFamilyResponsibilities() != null) {
                naturalCustomer.setFamilyResponsibilities(applicant.getFamilyResponsibilities());
            }
            if (applicant.getProfessionId() != null) {
                naturalCustomer.setProfessionId(applicant.getProfessionId());
            }
            naturalCustomer.setCreateDate(new Timestamp(new Date().getTime()));
            naturalCustomer = personEJB.saveNaturalCustomer(naturalCustomer);
            naturalCustomerParent = naturalCustomer;
            
            //Guarda los teléfonos del cliente
            if (requestCard.getPersonId().getPhonePerson() == null) {
                Long personHavePhone = personEJB.havePhonesByPerson(requestCard.getPersonId().getId());
                if (personHavePhone > 0) {
                    request = new EJBRequest();
                    Map params = new HashMap();
                    params.put(Constants.PERSON_KEY, requestCard.getPersonId().getId());
                    request.setParams(params);
                    phonePersonList = personEJB.getPhoneByPerson(request);
                    for (PhonePerson p: phonePersonList) {
                        requestCard.getPersonId().setPhonePerson(p);
                    }
                }
            }
            phonePerson.setPersonId(person);
            phonePerson.setIndMainPhone(true);
            phonePerson.setNumberPhone(requestCard.getPersonId().getPhonePerson().getNumberPhone());
            phonePerson.setPhoneTypeId(requestCard.getPersonId().getPhonePerson().getPhoneTypeId());
            phonePerson = personEJB.savePhonePerson(phonePerson);

            //Actualiza el cliente en la solicitud de tarjeta
            requestCard.setPersonCustomerId(naturalCustomer.getPersonId());
            requestCard = requestEJB.saveRequest(requestCard);

            //Guarda el resto de la información relacionada con el cliente
            saveCardComplementariesCustomer(naturalCustomer);
            saveFamilyReferentCustomer(naturalCustomer);
            saveAddressCustomer(naturalCustomer);

        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void saveFamilyReferentCustomer(NaturalCustomer naturalCustomer) {
        try {
            List<FamilyReferences> familyReferences;
            FamilyReferences familyCustomer = null;

            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.APPLICANT_NATURAL_PERSON_KEY, applicantNaturalPerson.getId());
            request1.setParams(params);
            familyReferences = personEJB.getFamilyReferencesByApplicant(request1);

            if (familyReferences != null) {
                for (FamilyReferences r : familyReferences) {
                    //Actualiza la referencia familiar colocandole ID del cliente nuevo
                    familyCustomer = r;
                    familyCustomer.setNaturalCustomerId(naturalCustomer);
                    familyCustomer = personEJB.saveFamilyReferences(familyCustomer);
                }
            } else {
                this.showMessage("sp.common.save.success", false, null);
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void saveAddressCustomer(NaturalCustomer naturalCustomer) {
        try {
            List<PersonHasAddress> personHasAddress;
            personHasAddress = new ArrayList<PersonHasAddress>();
            PersonHasAddress personAddressCustomer = null;

            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.PERSON_KEY, applicantNaturalPerson.getPersonId().getId());
            request1.setParams(params);
            personHasAddress = personEJB.getPersonHasAddressesByPerson(request1);

            if (personHasAddress != null) {
                for (PersonHasAddress r : personHasAddress) {
                    personAddressCustomer = new PersonHasAddress();
                    personAddressCustomer.setAddressId(r.getAddressId());
                    personAddressCustomer.setPersonId(naturalCustomer.getPersonId());
                    personAddressCustomer = personEJB.savePersonHasAddress(personAddressCustomer);
                }
            } else {
                this.showMessage("sp.common.save.success", false, null);
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void saveCardComplementariesCustomer(NaturalCustomer naturalCustomerMain) {
        try {
            Long countCardComplementary = 0L;
            Long countPhoneByPerson = 0L;
            Person person = null;
            PhonePerson phonePerson = new PhonePerson();
            NaturalCustomer naturalCustomer = null;

            countCardComplementary = personEJB.countCardComplementaryByApplicant(applicantNaturalPerson.getId());

            if (countCardComplementary != 0) {
                EJBRequest request1 = new EJBRequest();
                Map params = new HashMap();
                params.put(Constants.APPLICANT_NATURAL_PERSON_KEY, applicantNaturalPerson.getId());
                request1.setParams(params);
                cardComplementaryList = personEJB.getCardComplementaryByApplicant(request1);

                //colocar la clasificacion de la persona como Tarjetas Complementarias
                EJBRequest request3 = new EJBRequest();
                request3 = new EJBRequest();
                request3.setParam(Constants.PERSON_CARD_COMPLEMENTARIES_CUSTOMER);
                PersonClassification personClassification = utilsEJB.loadPersonClassification(request3);

                for (ApplicantNaturalPerson r : cardComplementaryList) {
                    person = new Person();
                    phonePerson = new PhonePerson();
                    naturalCustomer = new NaturalCustomer();

                    //Se actualiza el estatus del solicitante complementario a APROBADO
                    EJBRequest request = new EJBRequest();
                    request.setParam(StatusApplicantE.APROBA.getId());
                    StatusApplicant statusApplicant = requestEJB.loadStatusApplicant(request); 
                    r.setStatusApplicantId(statusApplicant);
                    r = personEJB.saveApplicantNaturalPerson(r);

                    //Guardar la persona
                    person.setCountryId(r.getPersonId().getCountryId());
                    person.setPersonTypeId(r.getPersonId().getPersonTypeId());
                    person.setEmail(r.getPersonId().getEmail());
                    person.setCreateDate(new Timestamp(new Date().getTime()));
                    person.setPersonClassificationId(personClassification);
                    person = personEJB.savePerson(person);
                    
                    //Se guardan los teléfonos del solicitante adicional
                    countPhoneByPerson = personEJB.havePhonesByPerson(r.getPersonId().getId());
                    if (countPhoneByPerson != 0) {
                        request = new EJBRequest(); 
                        params = new HashMap();
                        params.put(Constants.PERSON_KEY, r.getPersonId().getId());
                        request.setParams(params);
                        phonePersonList = personEJB.getPhoneByPerson(request);
                        if (phonePersonList != null) {
                            for (PhonePerson p : phonePersonList) {
                                phonePerson.setPersonId(person);
                                phonePerson.setPhoneTypeId(p.getPhoneTypeId());
                                phonePerson.setNumberPhone(p.getNumberPhone());
                                phonePerson = personEJB.savePhonePerson(phonePerson);
                            }
                        }
                    }                    

                    //Se guarda el solicitante para tarjeta complementaria
                    naturalCustomer.setPersonId(person);
                    naturalCustomer.setDocumentsPersonTypeId(r.getDocumentsPersonTypeId());
                    naturalCustomer.setIdentificationNumber(r.getIdentificationNumber());
                    naturalCustomer.setDueDateDocumentIdentification(r.getDueDateDocumentIdentification());
                    naturalCustomer.setStatusCustomerId(getStatusActiveCustomer());
                    naturalCustomer.setFirstNames(r.getFirstNames());
                    naturalCustomer.setLastNames(r.getLastNames());
                    if (r.getMarriedLastName() != null) {
                        naturalCustomer.setMarriedLastName(r.getMarriedLastName());
                    }
                    naturalCustomer.setGender(r.getGender());
                    if (r.getPlaceBirth() != null) {
                        naturalCustomer.setPlaceBirth(r.getPlaceBirth());
                    }
                    naturalCustomer.setDateBirth(r.getDateBirth());
                    naturalCustomer.setCivilStatusId(r.getCivilStatusId());
                    if (r.getFamilyResponsibilities() != null) {
                        naturalCustomer.setFamilyResponsibilities(r.getFamilyResponsibilities());
                    }
                    if (r.getProfessionId() != null) {
                        naturalCustomer.setProfessionId(r.getProfessionId());
                    }
                    naturalCustomer.setNaturalCustomerId(naturalCustomerMain);
                    naturalCustomer.setKinShipApplicantId(r.getKinShipApplicantId());
                    naturalCustomer.setCreateDate(new Timestamp(new Date().getTime()));
                    naturalCustomer = personEJB.saveNaturalCustomer(naturalCustomer);
                }
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public StatusCustomer getStatusActiveCustomer() {
        StatusCustomer statusCustomer = null;
        try {
            EJBRequest request = new EJBRequest();
            request.setParam(Constants.STATUS_CUSTOMER_ACTIVE);
            statusCustomer = personEJB.loadStatusCustomer(request);
        } catch (Exception ex) {
            showError(ex);
        }
        return statusCustomer;
    }

    public PersonClassification getClassificationCustomer() {
        PersonClassification personClassification = null;
        try {
            EJBRequest request = new EJBRequest();
            request.setParam(Constants.PERSON_CLASSIFICATION_CUSTOMER);
            personClassification = utilsEJB.loadPersonClassification(request);
        } catch (Exception ex) {
            showError(ex);
        }
        return personClassification;
    }

    public void saveLegalCustomer(Request requestCard) {
        List<LegalPersonHasLegalRepresentatives> legalRepresentativesByApplicantList = null;
        List<PersonHasAddress> AddressByApplicantList = null;
        PersonHasAddress personHasAddress = null;
        List<CardRequestNaturalPerson> cardAdditionalList = null;
        CardRequestNaturalPerson cardRequestNaturalPerson = null;
        try {
            Person person = new Person();
            LegalCustomer legalCustomer = new LegalCustomer();
            LegalPerson applicant = requestCard.getPersonId().getLegalPerson();

            //1. Se crea la persona asociada al cliente
            person.setCountryId(requestCard.getPersonId().getCountryId());
            person.setPersonTypeId(requestCard.getPersonId().getPersonTypeId());
            person.setEmail(requestCard.getPersonId().getEmail());
            person.setPersonClassificationId(getClassificationCustomer());
            person.setCreateDate(new Timestamp(new Date().getTime()));
            person = personEJB.savePerson(person);

            //2. Se crea el cliente        
            legalCustomer.setPersonId(person);
            legalCustomer.setDocumentsPersonTypeId(applicant.getDocumentsPersonTypeId());
            legalCustomer.setIdentificationNumber(applicant.getIdentificationNumber());
            legalCustomer.setTradeName(applicant.getTradeName());
            legalCustomer.setEnterpriseName(applicant.getEnterpriseName());
            legalCustomer.setStatusCustomerId(getStatusActiveCustomer());
            legalCustomer.setEconomicActivityId(applicant.getEconomicActivityId());
            legalCustomer.setDateInscriptionRegister(applicant.getDateInscriptionRegister());
            legalCustomer.setRegisterNumber(applicant.getRegisterNumber());
            legalCustomer.setPayedCapital(applicant.getPayedCapital());
            legalCustomer.setWebSite(applicant.getWebSite());
            legalCustomer.setCreateDate(new Timestamp(new Date().getTime()));
            legalCustomer = personEJB.saveLegalCustomer(legalCustomer);

            //Actualiza el cliente en la solicitud de tarjeta
            requestCard.setPersonCustomerId(legalCustomer.getPersonId());
            requestCard = requestEJB.saveRequest(requestCard);

            //3 Agregar la dirección del cliente
            EJBRequest request = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.PERSON_KEY, applicant.getPersonId().getId());
            request.setParams(params);
            AddressByApplicantList = personEJB.getPersonHasAddressesByPerson(request);
            for (PersonHasAddress addressApplicant : AddressByApplicantList) {
                personHasAddress = new PersonHasAddress();
                personHasAddress.setAddressId(addressApplicant.getAddressId());
                personHasAddress.setPersonId(person);
                personHasAddress = personEJB.savePersonHasAddress(personHasAddress);
            }

            //4. Agregar los representantes legales asociados al cliente
            request = new EJBRequest();
            params = new HashMap();
            params.put(Constants.APPLICANT_LEGAL_PERSON_KEY, applicant.getId());
            request.setParams(params);
            legalRepresentativesByApplicantList = personEJB.getLegalRepresentativesesBylegalPerson(request);
            for (LegalPersonHasLegalRepresentatives legalRepresentatives : legalRepresentativesByApplicantList) {
                LegalCustomerHasLegalRepresentatives legalRepresentativesByCustomer = new LegalCustomerHasLegalRepresentatives();
                legalRepresentativesByCustomer.setLegalCustomerId(legalCustomer);
                legalRepresentativesByCustomer.setLegalRepresentativesId(legalRepresentatives.getLegalRepresentativesid());
                legalRepresentativesByCustomer = personEJB.saveLegalCustomerHasLegalRepresentatives(legalRepresentativesByCustomer);
            }

            //5. Agregar las tarjetas adicionales asociadas al cliente
            request = new EJBRequest();
            params = new HashMap();
            params.put(Constants.APPLICANT_LEGAL_PERSON_KEY, applicant.getId());
            request.setParams(params);
            cardAdditionalList = personEJB.getCardRequestNaturalPersonsByLegalApplicant(request);
            for (CardRequestNaturalPerson cardAdditional : cardAdditionalList) {
                request = new EJBRequest();
                request.setParam(StatusApplicantE.APROBA.getId());
                StatusApplicant statusApplicant = requestEJB.loadStatusApplicant(request); 
                cardRequestNaturalPerson = cardAdditional;
                cardRequestNaturalPerson.setStatusApplicantId(statusApplicant);
                cardRequestNaturalPerson.setLegalCustomerId(legalCustomer);
                cardRequestNaturalPerson = personEJB.saveCardRequestNaturalPerson(cardRequestNaturalPerson);
            }

        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveReviewCollectionsRequest(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveReviewCollectionsRequest(reviewCollectionsRequestParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        try {
            switch (eventType) {
                case WebConstants.EVENT_EDIT:
                    getReviewCollectionsRequestParam();
                    loadField(requestCard);
                    if (reviewCollectionsRequestParam != null) {
                        loadFields(reviewCollectionsRequestParam);
                    } else {
                        loadUser();
                        loadDate();
                        txtReviewDate.setDisabled(true);
                    }
                    loadCmbProduct(eventType, requestCard.getProgramId().getId());
                    break;
                case WebConstants.EVENT_VIEW:
                    getReviewCollectionsRequestParam();
                    loadField(requestCard);
                    if (reviewCollectionsRequestParam != null) {
                        loadFields(reviewCollectionsRequestParam);
                    } else {
                        loadUser();
                        loadDate();
                        txtReviewDate.setDisabled(true);
                    }
                    blockFields();
                    loadCmbProduct(eventType, requestCard.getProgramId().getId());
                    break;
                case WebConstants.EVENT_ADD:
                    loadField(requestCard);
                    loadUser();
                    loadDate();
                    txtReviewDate.setDisabled(true);
                    loadCmbProduct(eventType, requestCard.getProgramId().getId());
                    break;

            }
        } catch (EmptyListException ex) {
            Logger.getLogger(AdminApplicationReviewController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GeneralException ex) {
            Logger.getLogger(AdminApplicationReviewController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullParameterException ex) {
            Logger.getLogger(AdminApplicationReviewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadCmbProduct(Integer evenInteger, Long programId) {
        EJBRequest request1 = new EJBRequest();
        List<Product> product;
        cmbProduct.getItems().clear();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_PROGRAM_ID, programId);
        request1.setParams(params);
        try {
            product = productEJB.getProductByProgram(request1);
            loadGenericCombobox(product, cmbProduct, "name", evenInteger, Long.valueOf(reviewCollectionsRequestParam != null ? reviewCollectionsRequestParam.getProductId().getId() : 0));
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
