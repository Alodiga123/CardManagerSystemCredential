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
import com.cms.commons.models.Card;
import com.cms.commons.models.CardRenewalRequest;
import com.cms.commons.models.CardRenewalRequestHasCard;
import com.cms.commons.models.Sequences;
import com.cms.commons.models.StatusCardRenewalRequest;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

public class ListCardRenewalControllers extends GenericAbstractListController<CardRenewalRequestHasCard> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtName;
    private UtilsEJB utilsEJB = null;
    private CardEJB cardEJB = null;
    private List<Card> card = null;
    private List<CardRenewalRequestHasCard> cardRenewal = null;
//    private User currentUser;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
//            currentUser = (User) session.getAttribute(Constants.USER_OBJ_SESSION);
            adminPage = "TabCardRenewal.zul";
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            getData();
            saveCardStatus(card);
            loadDataList(cardRenewal);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void getDataCard() {
        card = new ArrayList<Card>();
        try {

            EJBRequest request2 = new EJBRequest();
            Map params = new HashMap();
            params = new HashMap();
            params.put(QueryConstants.PARAM_IND_RENEWAL_ID, Constants.CARD_IND_RENEWAL);
            params.put(Constants.PLASTIC_CARD_STATUS_KEY, Constants.CARD_STATUS_ACTIVE);
            request2.setParams(params);

            card = cardEJB.getCardByIndRenewal(request2);

        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
            showEmptyList();
        } catch (GeneralException ex) {
            showError(ex);
        }
    }

    public void getData() {
        cardRenewal = new ArrayList<CardRenewalRequestHasCard>();
        try {
            request.setFirst(0);
            request.setLimit(null);
            cardRenewal = cardEJB.getCardRenewalRequestHasCard(request);
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
            showEmptyList();
        } catch (GeneralException ex) {
            showError(ex);
        }
    }

//     public void getData() {
//        cardRenewal = new ArrayList<CardRenewalRequestHasCard>();
//        try {
//
//            EJBRequest request2 = new EJBRequest();
//            Map params = new HashMap();
//            params = new HashMap();
//            params.put(QueryConstants.PARAM_IND_RENEWAL_ID, Constants.CARD_IND_RENEWAL);
//            params.put(Constants.PLASTIC_CARD_STATUS_KEY, Constants.CARD_STATUS_ACTIVE);
//            request2.setParams(params);
//
//            card = cardEJB.getCardByIndRenewal(request2);
//
//        } catch (NullParameterException ex) {
//            showError(ex);
//        } catch (EmptyListException ex) {
//            showEmptyList();
//        } catch (GeneralException ex) {
//            showError(ex);
//        }
//    }
    
    private void saveCardStatus(List<Card> list) {
        String numberRequest = "";
        StatusCardRenewalRequest statusRenewal = null;
        CardRenewalRequest cardRenewalRequest = null;
        CardRenewalRequestHasCard cardRenewalRequestHasCard = null;

        Date expirationDateCard = null;
        Calendar today = Calendar.getInstance();

        Calendar hoy = Calendar.getInstance();
        hoy.setTime(new Timestamp(new Date().getTime()));

        try {
            lbxRecords.getItems().clear();
            Listitem item = null;

            if (list != null && !list.isEmpty()) {
                btnDownload.setVisible(true);

                cardRenewalRequest = new CardRenewalRequest();

                //Obtiene el numero de secuencia para documento Request
//                EJBRequest request1 = new EJBRequest();
//                Map params = new HashMap();
//                params.put(Constants.DOCUMENT_TYPE_KEY, Constants.DOCUMENT_TYPE_RENEWAL_REQUEST);
//                request1.setParams(params);
//                List<Sequences> sequence = utilsEJB.getSequencesByDocumentType(request1);
//                numberRequest = utilsEJB.generateNumberSequence(sequence, Constants.ORIGIN_APPLICATION_CMS_ID);
                EJBRequest request1 = new EJBRequest();
                Map params = new HashMap();
                params.put(Constants.DOCUMENT_TYPE_KEY, Constants.DOCUMENT_TYPE_DELIVERY_REQUEST);
                request1.setParams(params);
                List<Sequences> sequence = utilsEJB.getSequencesByDocumentType(request1);
                numberRequest = utilsEJB.generateNumberSequence(sequence, Constants.ORIGIN_APPLICATION_CMS_ID);

                //Estatus de la tarjeta Entregada
                EJBRequest status = new EJBRequest();
                status.setParam(Constants.CARD_STATUS_DELIVERED);
                statusRenewal = cardEJB.loadStatusCardRenewalRequest(status);

                //Guardando el CardRenewalRequest
                cardRenewalRequest.setRequestNumber(numberRequest);
                cardRenewalRequest.setRequestDate(new Timestamp(new Date().getTime()));
                cardRenewalRequest.setCreateDate(new Timestamp(new Date().getTime()));
                cardRenewalRequest.setStatusCardRenewalRequestId(statusRenewal);
                cardRenewalRequest = cardEJB.saveCardRenewalRequest(cardRenewalRequest);

                for (Card card : list) {
                    card = new Card();
                    cardRenewalRequestHasCard = new CardRenewalRequestHasCard();

                    //Guardando la relacion en la tabla CardRenewalRequestHasCard
                    cardRenewalRequestHasCard.setCardId(card);
                    cardRenewalRequestHasCard.setCardRenewalRequestId(cardRenewalRequest);
                    cardRenewalRequestHasCard.setCreateDate(new Timestamp(new Date().getTime()));
                    cardRenewalRequestHasCard = cardEJB.saveCardRenewalRequestHasCard(cardRenewalRequestHasCard);

//                    updateIndRenewalCard(card);
                }
            } else {
                btnDownload.setVisible(false);
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void updateIndRenewalCard(Card card) {
        boolean indRenewal = false;
        try {
            card.setIndRenewal(indRenewal);
            card = cardEJB.saveCard(card);

        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void loadDataList(List<CardRenewalRequestHasCard> list) {
        Date expirationDateCard = null;
        Calendar today = Calendar.getInstance();

        Calendar hoy = Calendar.getInstance();
        hoy.setTime(new Timestamp(new Date().getTime()));
        try {
            lbxRecords.getItems().clear();
            Listitem item = null;

            if (list != null && !list.isEmpty()) {
                btnDownload.setVisible(true);
                for (CardRenewalRequestHasCard cardRenewal : list) {
                    //Se calcula la fecha de vencimiento de la tarjeta

                    item = new Listitem();
                    item.setValue(cardRenewal);
                    String pattern = "yyyy-MM-dd";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                    item.appendChild(new Listcell(cardRenewal.getCardId().getCardNumber()));
                    item.appendChild(new Listcell(cardRenewal.getCardId().getCardHolder()));
                    item.appendChild(new Listcell(simpleDateFormat.format(cardRenewal.getCardId().getExpirationDate())));
                    item.appendChild(new Listcell(cardRenewal.getCardId().getCardStatusId().getDescription()));
                    item.appendChild(new ListcellEditButton(adminPage, cardRenewal));
                    item.appendChild(new ListcellViewButton(adminPage, cardRenewal, true));
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

    private void showEmptyList() {
        Listitem item = new Listitem();
        item.appendChild(new Listcell(Labels.getLabel("sp.error.empty.list")));
        item.appendChild(new Listcell());
        item.appendChild(new Listcell());
        item.appendChild(new Listcell());
        item.setParent(lbxRecords);
    }

    @Override
    public List<CardRenewalRequestHasCard> getFilterList(String filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void onClick$btnDownload() throws InterruptedException {
        try {
            Utils.exportExcel(lbxRecords, Labels.getLabel("sp.crud.enterprise.list"));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnClear() throws InterruptedException {
        txtName.setText("");
    }

    public void startListener() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
