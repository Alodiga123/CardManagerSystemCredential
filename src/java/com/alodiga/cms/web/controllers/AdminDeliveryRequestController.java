package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.ProgramEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.DeliveryRequest;
import com.cms.commons.models.LegalPerson;
import com.cms.commons.models.Program;
import com.cms.commons.models.Sequences;
import com.cms.commons.models.StatusDeliveryRequest;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Toolbarbutton;

public class AdminDeliveryRequestController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblRequestNumber;
    private Label lblRequestDate;
    private Label txtStatus = null;
    private Combobox cmbShippingCompany;
    private Combobox cmbPrograms;
    private Combobox cmbStatusDeliveryRequest;
    private Datebox dtbRequestDate;
    private UtilsEJB utilsEJB = null;
    private ProgramEJB programEJB = null;
    private PersonEJB personEJB = null;
    private CardEJB cardEJB = null;
    private Button btnSave;
    private Integer eventType;
    private Toolbarbutton tbbTitle;
    private StatusDeliveryRequest statusPending;
    private DeliveryRequest deliveryRequestParam;
    public static DeliveryRequest deliveryRequestCard = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            deliveryRequestParam = null;
        } else {
            deliveryRequestParam = (DeliveryRequest) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.common.deliveryRequest.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.common.deliveryRequest.view"));
                break;
            case WebConstants.EVENT_ADD:
                tbbTitle.setLabel(Labels.getLabel("cms.common.deliveryRequest.add"));
                break;
            default:
                break;
        }
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            programEJB = (ProgramEJB) EJBServiceLocator.getInstance().get(EjbConstants.PROGRAM_EJB);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            if (eventType == WebConstants.EVENT_ADD) {
                EJBRequest request1 = new EJBRequest();
                request1.setParam(WebConstants.STATUS_DELIVERY_REQUEST_PENDING);
                statusPending = cardEJB.loadStatusDeliveryRequest(request1);
                txtStatus.setValue(statusPending.getDescription());
            }
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public DeliveryRequest getDeliveryRequest() {
        return this.deliveryRequestCard;
    }
    
    public Integer getEventType() {
        return this.eventType;
    }

    public void clearFields() {
    }

    private void loadFields(DeliveryRequest deliveryRequest) {
        try {
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            
            if (deliveryRequest.getRequestNumber() != null) {
                lblRequestNumber.setValue(deliveryRequest.getRequestNumber());
                lblRequestDate.setValue(simpleDateFormat.format(deliveryRequest.getRequestDate()));
            }
            
            txtStatus.setValue(deliveryRequest.getStatusDeliveryRequestId().getDescription());
            dtbRequestDate.setValue(deliveryRequest.getRequestDate());
            
            deliveryRequestCard = deliveryRequest;
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        dtbRequestDate.setReadonly(true);
        cmbShippingCompany.setReadonly(true);
        cmbPrograms.setReadonly(true);
        btnSave.setVisible(false);
    }
    
    public Boolean validateEmpty() {
        if (dtbRequestDate.getValue() == null) {
            dtbRequestDate.setFocus(true);
            this.showMessage("cms.error.deliveryRequest.date", true, null);
        } else if (cmbShippingCompany.getSelectedItem() == null) {
            cmbShippingCompany.setFocus(true);
            this.showMessage("cms.error.deliveryRequest.business", true, null);
        } else if (cmbPrograms.getSelectedItem() == null) {
            cmbPrograms.setFocus(true);
            this.showMessage("cms.error.program.notSelected", true, null);
        }  else {
            return true;
        }
        return false;

    }

    private void saveDeliveryRequest(DeliveryRequest _deliveryRequest) {
        String numberRequest = "";
        try {
            DeliveryRequest deliveryRequest = null;

            if (_deliveryRequest != null) {
                deliveryRequest = _deliveryRequest;
                numberRequest = deliveryRequest.getRequestNumber();
            } else {//New collectionsRequest
                deliveryRequest = new DeliveryRequest();

                //Obtiene el numero de secuencia para documento Request
                EJBRequest request1 = new EJBRequest();
                Map params = new HashMap();
                params.put(Constants.DOCUMENT_TYPE_KEY, Constants.DOCUMENT_TYPE_DELIVERY_REQUEST);
                request1.setParams(params);
                List<Sequences> sequence = utilsEJB.getSequencesByDocumentType(request1);
                numberRequest = utilsEJB.generateNumberSequence(sequence, Constants.ORIGIN_APPLICATION_CMS_ID);
            }

            deliveryRequest.setRequestNumber(numberRequest);
            deliveryRequest.setRequestDate((dtbRequestDate.getValue()));
            deliveryRequest.setShippingCompanyId((LegalPerson) cmbShippingCompany.getSelectedItem().getValue());
            if (eventType == WebConstants.EVENT_ADD) {
                deliveryRequest.setStatusDeliveryRequestId(statusPending);
            } else {
                deliveryRequest.setStatusDeliveryRequestId((StatusDeliveryRequest) cmbStatusDeliveryRequest.getSelectedItem().getValue());
            }
            deliveryRequest.setProgramId((Program) cmbPrograms.getSelectedItem().getValue());
            if (eventType == WebConstants.EVENT_ADD) {
                deliveryRequest.setCreateDate(new Timestamp(new Date().getTime()));
            } else {
                deliveryRequest.setUpdateDate(new Timestamp(new Date().getTime()));
            }
            deliveryRequest = cardEJB.saveDeliveryRequest(deliveryRequest);
            this.showMessage("sp.common.save.success", false, null);
            
            deliveryRequestCard = deliveryRequest;
            loadFields(deliveryRequestCard);
            
            btnSave.setVisible(false);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
        switch (eventType) {
            case WebConstants.EVENT_ADD:
                saveDeliveryRequest(null);
                break;
            case WebConstants.EVENT_EDIT:
                saveDeliveryRequest(deliveryRequestParam);
                break;
            default:
                break;
        }
      }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                txtStatus.setVisible(false);
                deliveryRequestCard = deliveryRequestParam;
                loadFields(deliveryRequestParam);
                loadFields(deliveryRequestParam);
                loadCmbPrograms(eventType);
                loadCmbShippingCompany(eventType);
                loadCmbStatusDeliveryRequest(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                txtStatus.setVisible(false);
                deliveryRequestCard = deliveryRequestParam;
                loadFields(deliveryRequestParam);
                loadFields(deliveryRequestParam);
                loadCmbPrograms(eventType);
                loadCmbShippingCompany(eventType);
                loadCmbStatusDeliveryRequest(eventType);
                break;
            case WebConstants.EVENT_ADD:
                txtStatus.setVisible(true);
                cmbStatusDeliveryRequest.setVisible(false);
                loadCmbPrograms(eventType);
                loadCmbShippingCompany(eventType);
                break;
            default:
                break;
        }
    }

    private void loadCmbPrograms(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<Program> programs;

        try {
            programs = programEJB.getProgram(request1);
            loadGenericCombobox(programs, cmbPrograms, "name", evenInteger, Long.valueOf(deliveryRequestParam != null ? deliveryRequestParam.getProgramId().getId() : 0));
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

    private void loadCmbShippingCompany(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_PERSON_CLASSIFICATION_ID, Constants.CLASSIFICATION_PERSON_SHIPPING_COMPANY);
        request1.setParams(params);
        List<LegalPerson> shippingCompany;

        try {
            shippingCompany = personEJB.getLegalPersonByPersonClassification(request1);
            loadGenericCombobox(shippingCompany, cmbShippingCompany, "enterpriseName", evenInteger, Long.valueOf(deliveryRequestParam != null ? deliveryRequestParam.getShippingCompanyId().getId() : 0));
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

    private void loadCmbStatusDeliveryRequest(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<StatusDeliveryRequest> statusDelivery;
        try {
            statusDelivery = (List<StatusDeliveryRequest>) cardEJB.getStatusDeliveryRequest(request1);
            cmbStatusDeliveryRequest.getItems().clear();
            for (StatusDeliveryRequest c : statusDelivery) {
                Comboitem item = new Comboitem();
                item.setValue(c);
                item.setLabel(c.getDescription());
                item.setDescription(c.getDescription());
                item.setParent(cmbStatusDeliveryRequest);
                if (deliveryRequestParam != null && c.getId().equals(deliveryRequestParam.getStatusDeliveryRequestId().getId())) {
                    cmbStatusDeliveryRequest.setSelectedItem(item);
                }
            }
            if (eventType.equals(WebConstants.EVENT_ADD)) {
                cmbStatusDeliveryRequest.setSelectedIndex(0);
            }
            if (evenInteger.equals(WebConstants.EVENT_VIEW)) {
                cmbStatusDeliveryRequest.setDisabled(true);
            }
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
