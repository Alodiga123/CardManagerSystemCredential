package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.commons.ejb.ProgramEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import static com.alodiga.cms.web.controllers.ListRateByProductController.program;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import com.alodiga.cms.web.utils.Utils;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Card;
import com.cms.commons.models.Product;
import com.cms.commons.models.Program;
import com.cms.commons.models.RateByProduct;
import com.cms.commons.models.RateByCard;
import com.cms.commons.models.Request;
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
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Window;

public class ListRateByCardController extends GenericAbstractListController<Request> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Combobox cmbProgram;
    private Combobox cmbProduct;
    private Combobox cmbCardHolders;
    private ProductEJB productEJB = null;
    private ProgramEJB programEJB = null;
    private CardEJB cardEJB = null;
    private List<RateByCard> rateByCardByCardList = new ArrayList<RateByCard>();
    private List<RateByProduct> rateByProductList = null;
    private RateByCard RateByCardParam;
    private Card card = null;
    private Tab tabApprovalRates;
    private Product product = null;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
        startListener();
    }

    public void startListener() {
        EventQueue que = EventQueues.lookup("updateRateByCard", EventQueues.APPLICATION, true);
        que.subscribe(new EventListener() {
            public void onEvent(Event evt) {
                if (card != null) {
                    getData(card.getProductId().getId());
                    loadList(rateByProductList, card);
                }
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
            adminPage = "adminRateByCard.zul";
            productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
            programEJB = (ProgramEJB) EJBServiceLocator.getInstance().get(EjbConstants.PROGRAM_EJB);
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
            loadCmbProgram(WebConstants.EVENT_ADD);
            tabApprovalRates.setDisabled(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onChange$cmbProgram() {
        cmbCardHolders.setValue("");
        cmbProduct.setValue("");
        program = (Program) cmbProgram.getSelectedItem().getValue();
        Sessions.getCurrent().setAttribute(WebConstants.PROGRAM, program);
        loadCmbCardHolder(WebConstants.EVENT_ADD, program.getId());
    }
    
    public void onChange$cmbCardHolders() {
        cmbProduct.setValue("");
        Card card = (Card) cmbCardHolders.getSelectedItem().getValue();
        Sessions.getCurrent().setAttribute(WebConstants.CARD, card);
        loadCmbProduct(WebConstants.EVENT_ADD, card.getCardHolder());
    }
    
    public void onChange$cmbProduct() {
        card = (Card) cmbProduct.getSelectedItem().getValue();
        Sessions.getCurrent().setAttribute(WebConstants.PRODUCT, card.getProductId());
        getData(card.getProductId().getId());
    }

    public void onClick$btnViewRates() throws InterruptedException {
        loadList(rateByProductList, card);
        tabApprovalRates.setDisabled(false);
    }

    public void onClick$btnDelete() {
    }

    public void loadList(List<RateByProduct> list, Card card) {
        List<RateByCard> rateByCardList = new ArrayList<RateByCard>();
        RateByCard rateByCard = null;
        EJBRequest request1 = new EJBRequest();
        Map params = new HashMap();
        int indLoadList = 0;
        String rbc;
        String rbp;
        int indExist = 0;
         AdminApprovalCardRateController adminApprovalCardRate = new AdminApprovalCardRateController();
        try {
            params.put(QueryConstants.PARAM_CARD_ID, card.getId());
            request1.setParams(params);
            rateByCardByCardList = cardEJB.getRateByCardByCard(request1);
            if (rateByCardByCardList != null) {
                indLoadList = 1;                
                for (RateByCard r : rateByCardByCardList) {
                    if (r.getApprovalCardRateId() == null) {
                        r.setApprovalCardRateId(adminApprovalCardRate.getApprovalCardRate());
                    }
                    rateByCardList.add(r);
                }
                if (list != null && !list.isEmpty()) {
                    for (RateByProduct rp : list) {
                        rbp = rp.getChannelId().getId().toString() + rp.getTransactionId().getId().toString() + card.getProductId().getId().toString();
                        for (RateByCard rc : rateByCardByCardList) {
                            rbc = rc.getChannelId().getId().toString() + rc.getTransactionId().getId().toString() + rc.getCardId().getProductId().getId().toString();
                            if (rbp.equals(rbc)) {
                                indExist = 1;
                            }
                        }
                        if (indExist != 1) {
                            rateByCard = new RateByCard();
                            rateByCard.setCardId(card);
                            rateByCard.setChannelId(rp.getChannelId());
                            if (rp.getFixedRate() != null) {
                                rateByCard.setFixedRate(rp.getFixedRate());
                            }
                            if (rp.getPercentageRate() != null) {
                                rateByCard.setPercentageRate(rp.getPercentageRate());
                            }                            
                            rateByCard.setIndCardHolderModification(rp.getIndCardHolderModification());
                            rateByCard.setRateApplicationTypeId(rp.getRateApplicationTypeId());
                            rateByCard.setTotalInitialTransactionsExempt(rp.getTotalInitialTransactionsExempt());
                            rateByCard.setTotalTransactionsExemptPerMonth(rp.getTotalTransactionsExemptPerMonth());
                            rateByCard.setTransactionId(rp.getTransactionId());
                            if (rp.getFixedRate() != null) {
                                rateByCard.setFixedRateCR(rp.getFixedRate());
                            }    
                            if (rp.getPercentageRate() != null) {
                                rateByCard.setPercentageRateCR(rp.getPercentageRate());
                            }                            
                            rateByCard.setTotalInitialTransactionsExemptCR(rp.getTotalInitialTransactionsExempt());
                            rateByCard.setTotalTransactionsExemptPerMonthCR(rp.getTotalTransactionsExemptPerMonth());
                            rateByCard.setCreateDate(new Timestamp(new Date().getTime()));
                            rateByCard = cardEJB.saveRateByCard(rateByCard);
                            rateByCardList.add(rateByCard);
                        }
                        indExist = 0;                        
                    }
                }
            }
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (rateByCardList != null && !rateByCardList.isEmpty()) {
                for (RateByCard r : rateByCardList) {
                    item = new Listitem();
                    item.setValue(r);
                    item.appendChild(new Listcell (r.getCardId().getProductId().getCountryId().getName()));
                    item.appendChild(new Listcell(r.getChannelId().getName()));
                    if(r.getTransactionId().getCode() != null){
                        item.appendChild(new Listcell(r.getTransactionId().getCode()));
                    }else{
                        item.appendChild(new Listcell("-"));
                    }
                    item.appendChild(new Listcell(r.getTransactionId().getDescription()));
                    if (r.getFixedRate() != null) {
                        item.appendChild(new Listcell(r.getFixedRate().toString()));
                    }else{
                        item.appendChild(new Listcell("-"));
                    }
                    if (r.getPercentageRate() != null) {
                        item.appendChild(new Listcell(r.getPercentageRate().toString()));
                    }else{
                        item.appendChild(new Listcell("-"));
                    }  
                    if(r.getApprovalCardRateId() != null){
                         item.appendChild(new Listcell(r.getApprovalCardRateId().getIndApproved()?"Si":"No"));
                    } else {
                        item.appendChild(new Listcell("No"));
                    }
                    item.appendChild(r.getIndCardHolderModification()?createButtonEditModal(r):new Listcell());
                    item.appendChild(createButtonViewModal(r));
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
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
            showError(ex); 
        } catch (GeneralException ex) {
            showError(ex);
        } catch (RegisterNotFoundException ex) {
            showError(ex); 
        } 
        finally {
            try {
                if (indLoadList == 0) {
                    lbxRecords.getItems().clear();
                    Listitem item = null;
                    if (list != null && !list.isEmpty()) {
                        for (RateByProduct rp : list) {
                            rateByCard = new RateByCard();
                            rateByCard.setCardId(card);
                            rateByCard.setChannelId(rp.getChannelId());
                            if (rp.getFixedRate() != null) {
                                rateByCard.setFixedRate(rp.getFixedRate());
                            }
                            if (rp.getPercentageRate() != null) {
                                rateByCard.setPercentageRate(rp.getPercentageRate());
                            } 
                            rateByCard.setRateApplicationTypeId(rp.getRateApplicationTypeId());
                            rateByCard.setIndCardHolderModification(rp.getIndCardHolderModification());
                            rateByCard.setTotalInitialTransactionsExempt(rp.getTotalInitialTransactionsExempt());
                            rateByCard.setTotalTransactionsExemptPerMonth(rp.getTotalTransactionsExemptPerMonth());
                            rateByCard.setTransactionId(rp.getTransactionId());
                            rateByCard.setFixedRateCR(rp.getFixedRate());
                            rateByCard.setPercentageRateCR(rp.getPercentageRate());
                            rateByCard.setTotalInitialTransactionsExemptCR(rp.getTotalInitialTransactionsExempt());
                            rateByCard.setTotalTransactionsExemptPerMonthCR(rp.getTotalTransactionsExemptPerMonth());
                            rateByCard.setCreateDate(new Timestamp(new Date().getTime()));
                            rateByCard = cardEJB.saveRateByCard(rateByCard);
                            rateByCardList.add(rateByCard);
                        }
                        for (RateByCard r : rateByCardList) {
                            item = new Listitem();
                            item.setValue(r);
                            item.appendChild(new Listcell(r.getCardId().getProductId().getCountryId().getName()));
                            item.appendChild(new Listcell(r.getChannelId().getName()));
                            item.appendChild(new Listcell(r.getTransactionId().getCode()));
                            item.appendChild(new Listcell(r.getTransactionId().getDescription()));
                            if (r.getFixedRate() != null) {
                                item.appendChild(new Listcell(r.getFixedRate().toString()));
                            }else{
                                item.appendChild(new Listcell("-"));
                            }
                            if (r.getPercentageRate() != null) {
                                item.appendChild(new Listcell(r.getPercentageRate().toString()));
                            }else{
                                item.appendChild(new Listcell("-"));
                            }  
                            if(r.getApprovalCardRateId() != null){
                                item.appendChild(new Listcell(r.getApprovalCardRateId().getIndApproved()?"Si":"No"));
                            } else {
                                item.appendChild(new Listcell("No"));
                            }
                            item.appendChild(createButtonEditModal(r));
                            item.appendChild(createButtonViewModal(r));
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
                }
            } catch (RegisterNotFoundException ex) {
                showError(ex);
            } catch (NullParameterException ex) {
                showError(ex);
            } catch (GeneralException ex) {
                showError(ex);
            }
        }
    }

    public void getData(Long productId) {
        try {
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(QueryConstants.PARAM_PRODUCT_ID, productId);
            request1.setParams(params);
            rateByProductList = productEJB.getRateByProductByProduct(request1);
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
        item.appendChild(new Listcell(Labels.getLabel("")));
        item.appendChild(new Listcell());
        item.appendChild(new Listcell());
        item.appendChild(new Listcell());
        item.appendChild(new Listcell());
        item.setParent(lbxRecords);
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

    private void loadCmbProgram(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<Program> programs;
        try {
            programs = programEJB.getProgram(request1);
            loadGenericCombobox(programs, cmbProgram, "name", evenInteger, Long.valueOf(0));
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

    private void loadCmbProduct(Integer evenInteger, String cardHolder) {
        cmbProduct.getItems().clear();
        EJBRequest request1 = new EJBRequest();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_CARDHOLDER, cardHolder);
        request1.setParams(params);
        List<Card> cardList;
        try {
            cardList = cardEJB.getCardByCardHolder(request1);
            for (int i = 0; i < cardList.size(); i++) {
                Comboitem item = new Comboitem();
                item.setValue(cardList.get(i));
                item.setLabel(cardList.get(i).getProductId().getName());
                item.setParent(cmbProduct);
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
    
    private void loadCmbCardHolder(Integer evenInteger, long programId) {
        cmbCardHolders.getItems().clear();
        EJBRequest request1 = new EJBRequest();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_PROGRAM_ID, programId);
        request1.setParams(params);
        List<Card> cardByProgramList;
        try {
            cardByProgramList = cardEJB.getCardByProgram(request1);
            loadGenericCombobox(cardByProgramList, cmbCardHolders, "cardHolder", evenInteger, Long.valueOf(0));
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

    public void onClick$btnDownload() throws InterruptedException {
        try {
            String pattern = "dd-MM-yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            StringBuilder file = new StringBuilder(Labels.getLabel("cms.menu.rate.by.card.list"));
            file.append("_");
            file.append(date);
            Utils.exportExcel(lbxRecords, file.toString());
        } catch (Exception ex) {
            showError(ex);
        }
        
    } 

    @Override
    public List<Request> getFilterList(String filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void loadDataList(List<Request> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void getData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
