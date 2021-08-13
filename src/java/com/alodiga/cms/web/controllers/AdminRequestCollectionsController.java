package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.RequestEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import static com.alodiga.cms.web.controllers.AdminRequestController.eventType;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.CollectionType;
import com.cms.commons.models.CollectionsRequest;
import com.cms.commons.models.Country;
import com.cms.commons.models.PersonType;
import com.cms.commons.models.Request;
import com.cms.commons.models.RequestHasCollectionsRequest;
import com.cms.commons.models.StatusRequest;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;

public class AdminRequestCollectionsController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblRequestNumber;
    private Label lblRequestDate;
    private Label lblStatusRequest;
    private Request requestParam = null;
    private CollectionsRequest collectionsRequestParam;
    private RequestHasCollectionsRequest requestHasCollectionsRequestParam;
    private List<RequestHasCollectionsRequest> requestHasCollectionsRequestList;
    private UtilsEJB utilsEJB = null;
    private RequestEJB requestEJB = null;
    private Radio rApprovedYes;
    private Radio rApprovedNo;
    private Label lblInfo;
    private Label txtPrograms;
    private Label txtProductType;
    private Combobox cmbCollectionsRequest;
    private Textbox txtObservations;
    private Label lblPersonType;
    private Label lblCountry;
    private Button btnSave;
    private Button btnUpload;
    private Image image;
    public Window winAdminRequestCollections;
    private Vbox divPreview;
    String UrlFile = "";
    String format = "";
    Request RequestNumber = null;
    private boolean uploaded = false;
    List<RequestHasCollectionsRequest> requestHasCollectionsRequest = new ArrayList<RequestHasCollectionsRequest>();
    private AdminRequestController adminRequest = null;
    private Tab tabApplicationReview;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        adminRequest = new AdminRequestController();
        if (adminRequest.getRequest().getId() != null) {
            requestParam = adminRequest.getRequest();
        }
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                requestHasCollectionsRequestParam = (RequestHasCollectionsRequest) Sessions.getCurrent().getAttribute("object");
                break;
            case WebConstants.EVENT_VIEW:
                requestHasCollectionsRequestParam = (RequestHasCollectionsRequest) Sessions.getCurrent().getAttribute("object");
                break;
            case WebConstants.EVENT_ADD:
                requestHasCollectionsRequestParam = null;
                break;
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
        requestEJB = (RequestEJB) EJBServiceLocator.getInstance().get(EjbConstants.REQUEST_EJB);
        loadData();
    }

    public void clearFields() {
    }

    private void loadField(Request requestData) {
        try {
            String pattern = "dd-MM-yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            if (requestData.getRequestNumber() != null) {
                lblRequestNumber.setValue(requestData.getRequestNumber());
                lblRequestDate.setValue(simpleDateFormat.format(requestData.getRequestDate()));
                lblStatusRequest.setValue(requestData.getStatusRequestId().getDescription());
                lblCountry.setValue(requestData.getCountryId().getName());
                lblPersonType.setValue(requestData.getPersonTypeId().getDescription());
                txtPrograms.setValue(requestData.getProgramId().getName());
                txtProductType.setValue(requestData.getProductTypeId().getName());
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loadFields(RequestHasCollectionsRequest requestHasCollectionsRequest) {
        if (requestHasCollectionsRequest != null) {
            try {
                if (requestHasCollectionsRequest.getIndApproved()!=null) {
                    if (requestHasCollectionsRequest.getIndApproved() == 1) {
                        rApprovedYes.setChecked(true);
                    } else {
                        rApprovedNo.setChecked(true);
                    }
                }
                txtObservations.setText(requestHasCollectionsRequest.getObservations());
                UrlFile = requestHasCollectionsRequest.getUrlImageFile();

                AImage image;
                image = new org.zkoss.image.AImage(requestHasCollectionsRequest.getUrlImageFile());
                org.zkoss.zul.Image imageFile = new org.zkoss.zul.Image();
                imageFile.setWidth("250px");
                imageFile.setContent(image);
                imageFile.setParent(divPreview);
            } catch (Exception ex) {
                showError(ex);
            }
        }
    }

    public Boolean validateEmpty() {
        if ((!rApprovedYes.isChecked()) && (!rApprovedNo.isChecked())) {
            this.showMessage("cms.error.radio.approved", true, null);
        } else if (txtObservations.getText().isEmpty()) {
            txtObservations.setFocus(true);
            this.showMessage("cms.error.renewal.observations", true, null);
        } else if (UrlFile.isEmpty()) {
            this.showMessage("cms.error.urlFile", true, null);
        } else {
            return true;
        }
        return false;
    }
    
    public Boolean validateCollectionsRequet(){
        requestHasCollectionsRequest.clear();
        Request requestCard = null;
        try{
            AdminRequestController adminRequestController = new AdminRequestController();
            if (adminRequestController.getRequest().getId() != null) {
                requestCard = adminRequestController.getRequest();
            }
            CollectionsRequest collectionsRequest = (CollectionsRequest) cmbCollectionsRequest.getSelectedItem().getValue();
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.REQUESTS_KEY, requestCard.getId());
            params.put(Constants.COLLECTIONS_REQUEST_KEY, collectionsRequest.getId());
            request1.setParams(params);
            requestHasCollectionsRequest = requestEJB.getRequestsHasCollectionsRequestByRequestByCollectionRequest(request1);
        } catch (Exception ex) {
            showError(ex);
        } finally {
            if (requestHasCollectionsRequest.size() > 0) {
                this.showMessage("sp.error.collectionsType.duplicate", true, null);
                cmbCollectionsRequest.setFocus(true);
                return false;
            }
        }    
        return true;
    }

    public void onUpload$btnUpload(org.zkoss.zk.ui.event.UploadEvent event) throws Throwable {
        org.zkoss.util.media.Media media = event.getMedia();
        if (media != null) {
            divPreview.getChildren().clear();
            media = event.getMedia();
            File file = new File("/opt/proyecto/cms/imagenes/" + media.getName());
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(media.getByteData());
            fos.flush();
            fos.close();
            UrlFile = file.getAbsolutePath();
            format = media.getFormat();
            org.zkoss.zul.Image image = new org.zkoss.zul.Image();
            image.setContent((org.zkoss.image.Image) media);
            image.setWidth("250px");
            image.setParent(divPreview);
            uploaded = true;
        } else {
            lblInfo.setValue("Error");
        }
    }

    public void blockFields() {
        rApprovedYes.setDisabled(true);
        rApprovedNo.setDisabled(true);
        cmbCollectionsRequest.setDisabled(true);
        txtObservations.setDisabled(true);
        btnUpload.setVisible(false);
        btnSave.setVisible(false);        
    }
   
    public void onClick$btnBack() {
        winAdminRequestCollections.detach();
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

    private void saveRequest(RequestHasCollectionsRequest _requestHasCollectionsRequest) {
        Request request = adminRequest.getRequest();
        Request RequestId = null;
        short indApproved = 0;
        try {
            RequestHasCollectionsRequest requestHasCollectionsRequest = null;

            if (_requestHasCollectionsRequest != null) {
                requestHasCollectionsRequest = _requestHasCollectionsRequest;
            } else {
                requestHasCollectionsRequest = new RequestHasCollectionsRequest();
            }

            if (rApprovedYes.isChecked()) {
                indApproved = 1;
            } else {
                indApproved = 0;
            }

            //Se obtiene la solicitud de tarjeta
            if (adminRequest.getRequest().getId() != null) {
                RequestId = adminRequest.getRequest();
            }

            //Guarda la revisión del Recaudo asociado a la solicitud
            requestHasCollectionsRequest.setCollectionsRequestid((CollectionsRequest) cmbCollectionsRequest.getSelectedItem().getValue());
            requestHasCollectionsRequest.setRequestId(RequestId);
            requestHasCollectionsRequest.setIndApproved(indApproved);
            requestHasCollectionsRequest.setObservations(txtObservations.getText());
            requestHasCollectionsRequest.setUrlImageFile(UrlFile);
            if (eventType == WebConstants.EVENT_ADD) {
                requestHasCollectionsRequest.setCreateDate(new Timestamp(new Date().getTime()));
            } else {
                requestHasCollectionsRequest.setUpdateDate(new Timestamp(new Date().getTime()));
            }
            requestHasCollectionsRequest = requestEJB.saveRequestHasCollectionsRequest(requestHasCollectionsRequest);
            
            //Verificar si todos los recaudos están aprobados
            if (requestHasCollectionRequestCheck() == 0) {
                request.setStatusRequestId(getStatusRequest(request,Constants.STATUS_REQUEST_COLLECTIONS_OK));
                request = requestEJB.saveRequest(request);
            }

            this.showMessage("sp.common.save.success", false, null);
            EventQueues.lookup("updateCollectionsRequest", EventQueues.APPLICATION, true).publish(new Event(""));
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public Long requestHasCollectionRequestCheck() {
        Long requestHasCollectionsRequestCheck = 0L;
        try {
            requestHasCollectionsRequestCheck = requestEJB.getRequestsHasCollectionsRequestCheck(requestParam.getId());
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }    
        return requestHasCollectionsRequestCheck;     
    }
    
    public int activateTabApplicationReview(){
    requestHasCollectionsRequestList = new ArrayList<RequestHasCollectionsRequest>();
    int indApproved = 0;
     try {
            Map params = new HashMap();
            EJBRequest request1 = new EJBRequest();
            params.put(Constants.REQUESTS_KEY, requestParam.getId());
            request1.setParams(params);
            requestHasCollectionsRequestList = requestEJB.getRequestsHasCollectionsRequestByRequest(request1);
            for (RequestHasCollectionsRequest requestHasCollections : requestHasCollectionsRequestList ){
                if(requestHasCollections.getIndApproved() == 0){
                    indApproved = 1;   
                }
            }
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {      
        } catch (GeneralException ex) {
        }
    
    return indApproved;
    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    if (validateCollectionsRequet()) {
                        saveRequest(null);
                    }
                break;
                case WebConstants.EVENT_EDIT:
                    saveRequest(requestHasCollectionsRequestParam);
                break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(requestHasCollectionsRequestParam);
                loadField(requestParam);
                loadCmbCollectionsRequest(eventType);
                cmbCollectionsRequest.setDisabled(true);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(requestHasCollectionsRequestParam);
                loadField(requestParam);
                loadCmbCollectionsRequest(eventType);
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                loadCmbCollectionsRequest(eventType);
                loadField(requestParam);
                break;
            default:
                break;
        }
    }
    
    private void loadCmbCollectionsRequest(Integer evenInteger) {
        Request requestCard = null;
        String descriptionType = "";
        
        AdminRequestController adminRequestController = new AdminRequestController();
        if (adminRequestController.getRequest().getId() != null) {
            requestCard = adminRequestController.getRequest();
        }
        
        List<CollectionsRequest> collectionsRequest;
        try {
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.COUNTRY_KEY, requestCard.getCountryId().getId());
            params.put(Constants.PRODUCT_TYPE_KEY, requestCard.getProductTypeId().getId());
            params.put(Constants.PROGRAM_KEY, requestCard.getProgramId().getId());
            params.put(Constants.PERSON_TYPE_KEY, requestCard.getPersonTypeId().getId());
            request1.setParams(params);
            collectionsRequest = requestEJB.getCollectionsByRequest(request1);
            
            for (int i = 0; i < collectionsRequest.size(); i++) {
                Comboitem item = new Comboitem();
                item.setValue(collectionsRequest.get(i));
                descriptionType = collectionsRequest.get(i).getCollectionTypeId().getDescription();
                item.setLabel(descriptionType);
                item.setParent(cmbCollectionsRequest);
                if (eventType != 1) {
                    if (collectionsRequest.get(i).getCollectionTypeId().getId().equals(requestHasCollectionsRequestParam.getCollectionsRequestid().getCollectionTypeId().getId())) {
                        cmbCollectionsRequest.setSelectedItem(item);
                    }
                }
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
