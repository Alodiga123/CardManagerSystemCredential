package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.custom.components.ListcellEditButton;
import com.alodiga.cms.web.custom.components.ListcellViewButton;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import com.alodiga.cms.web.utils.Utils;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.CardRenewalRequest;
import com.cms.commons.models.CardRenewalRequestHasCard;
import com.cms.commons.models.CardStatus;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zk.ui.Component;

public class ListCardByRenewalController extends GenericAbstractListController<CardRenewalRequest> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtName;
    private CardEJB cardEJB = null;
    private UtilsEJB utilsEJB = null;
    private List<CardRenewalRequestHasCard> cardRenewalRequest = null;
    private List<CardRenewalRequest> CardRenewalRequestList = null;
    private List<CardRenewalRequest> cardRenewalRequestAllList = null;
    private List cardByIssuerList = null;
    CardStatus cardStatus = null;

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
            adminPage = "TabCardRenewal.zul";
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            
            //Se obtiene el estatus de la tarjeta ACTIVA
            EJBRequest request1 = new EJBRequest();
            request1.setParam(Constants.CARD_STATUS_CANCELED);
            cardStatus = utilsEJB.loadCardStatus(request1);
            
            //Verificar si en la fecha actual se crearon solicitudes de renovación
//            CardRenewalRequestList = cardEJB.getCardRenewalRequestByCard(cardStatus.getId());
               
        } catch (Exception ex) {
            showError(ex);
        } finally {
            try {
                if (CardRenewalRequestList.size() == 0) {
                    //Se llama al servicio que genera las solicitudes de renovación de tarjetas del día actual.
                    CardRenewalRequestList = cardEJB.createCardRenewalRequestByIssuer(cardStatus.getId());
                }                 
                getData();
                loadDataList(cardRenewalRequestAllList);
            } catch (Exception ex) {
                showError(ex);
            }    
        }
    }

    public void onClick$btnDelete() {
    }

    public void loadDataList(List<CardRenewalRequest> list) {
        try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                for (CardRenewalRequest cardRenewalRequest : list) {
                    item = new Listitem();
                    item.setValue(cardRenewalRequest);

                    String pattern = "yyyy-MM-dd";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                    item.appendChild(new Listcell(cardRenewalRequest.getRequestNumber()));
                    item.appendChild(new Listcell(simpleDateFormat.format(cardRenewalRequest.getRequestDate())));
                    item.appendChild(new Listcell(cardRenewalRequest.getIssuerId().getName()));
                    item.appendChild(new ListcellEditButton(adminPage, cardRenewalRequest));
                    item.appendChild(new ListcellViewButton(adminPage, cardRenewalRequest, true));
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
        cardRenewalRequestAllList = new ArrayList<CardRenewalRequest>();
        try {
            request.setFirst(0);
            request.setLimit(null);
            cardRenewalRequestAllList = cardEJB.getCardRenewalRequest(request);
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
    public List<CardRenewalRequest> getFilterList(String filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
