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
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.CardRenewalRequest;
import com.cms.commons.models.CardRenewalRequestHasCard;
import com.cms.commons.models.CardStatus;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.zkoss.zul.Button;
import org.zkoss.zul.Window;

public class ListCardRenewalRequestController extends GenericAbstractListController<CardRenewalRequest> {

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
            request1.setParam(Constants.CARD_STATUS_ACTIVE);
            cardStatus = utilsEJB.loadCardStatus(request1);
            
            //Verificar si en la fecha actual se crearon solicitudes de renovación
            CardRenewalRequestList = cardEJB.getCardRenewalRequestByCurrentDate(cardStatus.getId());
               
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
//                    item.appendChild(createButtonEditModal(cardRenewalRequest));
//                    item.appendChild(createButtonViewModal(cardRenewalRequest));
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
            String pattern = "dd-MM-yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            StringBuilder file = new StringBuilder(Labels.getLabel("cms.menu.card.renewal.request.listDownload"));
            file.append("_");
            file.append(date);
            Utils.exportExcel(lbxRecords, file.toString());
        } catch (Exception ex) {
            showError(ex);
        }
        
    }

    public void onClick$btnClear() throws InterruptedException {
        txtName.setText("");
    }
    
    public void onClick$btnSearch() throws InterruptedException {
        try {
            loadDataList(getFilterList(txtName.getText()));
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public List<CardRenewalRequest> getFilterList(String filter) {
      List<CardRenewalRequest> cardRenewalList_ = new ArrayList<CardRenewalRequest>();
        try {
            if (filter != null && !filter.equals("")) {
                cardRenewalList_ = cardEJB.getSearchCardRenewalRequest(filter);
            } else {
                return cardByIssuerList;
            }
        } catch (Exception ex) {
            showError(ex);
        }
        return cardRenewalList_;  
    }
}
