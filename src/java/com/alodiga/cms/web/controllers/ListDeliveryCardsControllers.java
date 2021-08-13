package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Card;
import com.cms.commons.models.CardStatus;
import com.cms.commons.models.DeliveryRequest;
import com.cms.commons.models.DeliveryRequetsHasCard;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.sql.Timestamp;
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
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class ListDeliveryCardsControllers extends GenericAbstractListController<Card> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtName;
    private CardEJB cardEJB = null;
    private UtilsEJB utilsEJB = null;
    private List<Card> card = null;
    private Checkbox cbxEnabled;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            getDataCard();
            loadDataList(card);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public List<Card> getDataCard() {
        card = new ArrayList<Card>();
        try {
            EJBRequest request2 = new EJBRequest();
            Map params = new HashMap();
            params = new HashMap();
            params.put(QueryConstants.PARAM_CARDS_STATUS_ID, Constants.CARD_STATUS_CUSTOMIZING);
            request2.setParams(params);
            card = cardEJB.getCardByStatus(request2);
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        }
        return card;
    }

    public void onClick$btnAdd() throws InterruptedException {
        Sessions.getCurrent().setAttribute("eventType", WebConstants.EVENT_ADD);
        Sessions.getCurrent().removeAttribute("object");
        Executions.getCurrent().sendRedirect(adminPage);
    }

    public void onClick$btnClear() throws InterruptedException {
        txtName.setText("");
        cbxEnabled.setChecked(false);
    }

    public void startListener() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void loadDataList(List<Card> list) {
        Listcell tmpCell = new Listcell();
        try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                for (Card card : list) {

                    item = new Listitem();
                    item.setValue(card);
                    item.appendChild(new Listcell(card.getCardNumber()));
                    item.appendChild(new Listcell(card.getProductId().getName()));
                    item.appendChild(new Listcell(card.getCardHolder()));
                    item.appendChild(new Listcell(card.getCardStatusId().getDescription()));
                    tmpCell = new Listcell();
                    Checkbox chkRequired = new Checkbox();
                    chkRequired.setParent(tmpCell);
                    item.appendChild(tmpCell);
                    item.setParent(lbxRecords);
                }
            } else {
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

    public void onClick$btnAssigment() throws InterruptedException {
        try {
            saveCard(card);
            getDataCard();
            loadDataList(card);
            ListCardInventoryControllers listCardInventory = new ListCardInventoryControllers();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void saveCard(List<Card> list) {
        int countRecords = list.size();
        DeliveryRequetsHasCard deliveryRequetsHasCard = null;
        DeliveryRequest deliveryRequest = null;

        try {
            AdminDeliveryRequestController adminDeliveryRequest = new AdminDeliveryRequestController();
            if (adminDeliveryRequest.getDeliveryRequest().getId() != null) {
                deliveryRequest = adminDeliveryRequest.getDeliveryRequest();
            }

            for (int i = 0; i < countRecords; i++) {
                Integer a = i;
                List<Listcell> listCells = ((Listitem) lbxRecords.getItems().get(i)).getChildren();
                for (Listcell l : listCells) {
                    for (Object cell : ((Listcell) l).getChildren()) {
                        if (cell instanceof Checkbox) {
                            Checkbox myCheckbox = (Checkbox) cell;
                            if (myCheckbox.isChecked()) {
                                Card card = new Card();
                                card = (Card) ((Listitem) lbxRecords.getItems().get(i)).getValue();

                                deliveryRequetsHasCard = new DeliveryRequetsHasCard();
                                deliveryRequetsHasCard.setDeliveryRequestId(deliveryRequest);
                                deliveryRequetsHasCard.setCardId(card);
                                deliveryRequetsHasCard.setCreateDate(new Timestamp(new Date().getTime()));
                                deliveryRequetsHasCard = cardEJB.saveDeliveryRequestHasCard(deliveryRequetsHasCard);
                                
                                updateStatusCardDelivered(card);
                            }
                        }
                    }
                }                
                this.showMessage("cms.msj.assignCardToDeliveryRequest", false, null);
            }
        } catch (GeneralException ex) {
            Logger.getLogger(ListCardAssigmentControllers.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullParameterException ex) {
            Logger.getLogger(ListCardAssigmentControllers.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RegisterNotFoundException ex) {
            Logger.getLogger(ListDeliveryCardsControllers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateStatusCardDelivered(Card card) {
        CardStatus cardStatus = null;
        boolean indDelivery = true;
        try {
            //Estatus de la tarjeta Entregada
            EJBRequest request1 = new EJBRequest();
            request1.setParam(Constants.CARD_STATUS_PENDING_DELIVERY);
            cardStatus = utilsEJB.loadCardStatus(request1);

            card.setCardStatusId(cardStatus);
            card.setIndDeliveryRequest(indDelivery);
            card = cardEJB.saveCard(card);
            
        } catch (Exception ex) {
            showError(ex);
        }
    }

    @Override
    public List<Card> getFilterList(String filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void getData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
