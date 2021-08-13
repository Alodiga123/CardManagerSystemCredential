package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.custom.components.ListcellEditButton;
import com.alodiga.cms.web.custom.components.ListcellViewButton;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import com.alodiga.cms.web.utils.Utils;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.CardStatusHasUpdateReason;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

public class ListCardStatusByReasonControllers extends GenericAbstractListController<CardStatusHasUpdateReason> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private CardEJB cardEJB = null;
    private List<CardStatusHasUpdateReason> cardStatusByReason = null;
   

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
    }


    @Override
    public void initialize() {
        super.initialize();
        try {
            adminPage = "adminCardStatusByReason.zul";
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            getData();
            loadDataList(cardStatusByReason);
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
   public void getData() {
    cardStatusByReason = new ArrayList<CardStatusHasUpdateReason>();
        try {
            request.setFirst(0);
            request.setLimit(null);
            cardStatusByReason = cardEJB.getCardStatusHasUpdateReason(request);
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
        } catch (GeneralException ex) {
            showError(ex);
        }
    }


    public void onClick$btnAdd() throws InterruptedException {
        Sessions.getCurrent().setAttribute("eventType", WebConstants.EVENT_ADD);
        Sessions.getCurrent().removeAttribute("object");
        Executions.getCurrent().sendRedirect(adminPage);
    }
    
       
   public void onClick$btnDownload() throws InterruptedException {
        try {
            String pattern = "dd-MM-yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            StringBuilder file = new StringBuilder(Labels.getLabel("cms.crud.cardStatusByReason.list"));
            file.append("_");
            file.append(date);
            Utils.exportExcel(lbxRecords, file.toString());
        } catch (Exception ex) {
            showError(ex);
        }
        
    }


    public void onClick$btnClear() throws InterruptedException {
        
    }

    public void startListener() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void loadDataList(List<CardStatusHasUpdateReason> list) {
        String allowTable = null;
          try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                btnDownload.setVisible(true);
                for (CardStatusHasUpdateReason cardStatusByReason : list) {

                    item = new Listitem();
                    item.setValue(cardStatusByReason);
                    item.appendChild(new Listcell(cardStatusByReason.getCardStatusId().getDescription()));
                    item.appendChild(new Listcell(cardStatusByReason.getStatusUpdateReasonId().getDescription()));
                    if (cardStatusByReason.getIndAllowTable() == true) {
                        item.appendChild(new Listcell(Labels.getLabel("sp.common.yes")));
                    } else {
                        item.appendChild(new Listcell(Labels.getLabel("sp.common.no")));
                    }
                    item.appendChild( new ListcellEditButton(adminPage, cardStatusByReason));
                    item.appendChild(new ListcellViewButton(adminPage, cardStatusByReason,true));
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

    @Override
    public List<CardStatusHasUpdateReason> getFilterList(String filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
 

}
