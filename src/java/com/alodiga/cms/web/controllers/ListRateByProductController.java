package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.commons.ejb.ProgramEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import com.alodiga.cms.web.utils.Utils;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Product;
import com.cms.commons.models.Program;
import com.cms.commons.models.RateByProduct;
import com.cms.commons.models.RateByProgram;
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
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Window;

public class ListRateByProductController extends GenericAbstractListController<Request> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Combobox cmbProgram;
    private Combobox cmbProduct;
    private Label lblProductType;
    private ProductEJB productEJB = null;
    private ProgramEJB programEJB = null;
    private List<RateByProgram> rateByProgramList = null;
    private List<RateByProduct> rateByProductByProductList = new ArrayList<RateByProduct>();
    private RateByProduct RateByProductParam;
    private Product product = null;
    private Tab tabApprovalRates;
    public static Program program = null;
    private Tab tabRatesByProduct;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
        startListener();
    }

    public void startListener() {
        EventQueue que = EventQueues.lookup("updateRateByProduct", EventQueues.APPLICATION, true);
        que.subscribe(new EventListener() {
            public void onEvent(Event evt) {
                getData(program.getId());
                if (product != null) {
                    loadList(rateByProgramList, product);
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
            adminPage = "adminRateByProduct.zul";
            productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
            programEJB = (ProgramEJB) EJBServiceLocator.getInstance().get(EjbConstants.PROGRAM_EJB);
            eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
            loadCmbProgram(WebConstants.EVENT_ADD);
            tabApprovalRates.setDisabled(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public void onSelect$tabRatesByProduct() {
        try {
            doAfterCompose(self);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onChange$cmbProgram() {
        cmbProduct.setVisible(true);
        lblProductType.setVisible(true);
        program = (Program) cmbProgram.getSelectedItem().getValue();
        lblProductType.setValue(program.getProductTypeId().getName());
        cmbProduct.setValue("");
        lbxRecords.getItems().clear();
        Sessions.getCurrent().setAttribute(WebConstants.PROGRAM, program);
        loadCmbProduct(WebConstants.EVENT_ADD, program.getId());
        getData(program.getId());
    }
    
    public void onChange$cmbProduct() {
        lbxRecords.getItems().clear();
        product = (Product) cmbProduct.getSelectedItem().getValue();
        Sessions.getCurrent().setAttribute(WebConstants.PRODUCT, product);
    }

    public void onClick$btnViewRates() throws InterruptedException {
        loadList(rateByProgramList, product);
        tabApprovalRates.setDisabled(false);
    }

    public void onClick$btnDelete() {
    }

    public void loadList(List<RateByProgram> list, Product productId) {
        List<RateByProduct> rateByProductList = new ArrayList<RateByProduct>();
        RateByProduct rateByProduct = null;
        EJBRequest request1 = new EJBRequest();
        Map params = new HashMap();
        int indLoadList = 0;
        String rbp1;
        String rbp2;
        int indExist = 0;
        AdminApprovalProductRateController adminApprovalProductRate = new AdminApprovalProductRateController();
        try {
            params.put(QueryConstants.PARAM_PRODUCT_ID, product.getId());
            request1.setParams(params);
            rateByProductByProductList = productEJB.getRateByProductByProduct(request1);
            if (rateByProductByProductList != null) {
                indLoadList = 1;
                for (RateByProduct r : rateByProductByProductList) {
                    if (r.getApprovalProductRateId() == null) {
                        r.setApprovalProductRateId(adminApprovalProductRate.getApprovalProductRateParam());
                    }
                    rateByProductList.add(r);
                }
                if (list != null && !list.isEmpty()) {
                    for (RateByProgram rp : list) {
                        rbp1 = rp.getChannelId().getId().toString() + rp.getTransactionId().getId().toString() + productId.getProgramId().getId().toString();
                        for (RateByProduct r: rateByProductByProductList) {
                            rbp2 = r.getChannelId().getId().toString() + r.getTransactionId().getId().toString() + r.getProductId().getProgramId().getId().toString();
                            if (rbp1.equals(rbp2)) {
                                indExist = 1;
                            }
                        }
                        if (indExist != 1) {
                            rateByProduct = new RateByProduct();
                            rateByProduct.setProductId(product);
                            rateByProduct.setChannelId(rp.getChannelId());
                            if (rp.getFixedRate() != null) {
                                rateByProduct.setFixedRate(rp.getFixedRate());
                            }
                            if (rp.getPercentageRate() != null) {
                                rateByProduct.setPercentageRate(rp.getPercentageRate());
                            }
                            rateByProduct.setIndCardHolderModification(rp.getIndCardHolderModification());
                            rateByProduct.setRateApplicationTypeId(rp.getRateApplicationTypeId());
                            rateByProduct.setTotalInitialTransactionsExempt(rp.getTotalInitialTransactionsExempt());
                            rateByProduct.setTotalTransactionsExemptPerMonth(rp.getTotalTransactionsExemptPerMonth());
                            rateByProduct.setTransactionId(rp.getTransactionId());
                            if (rp.getFixedRate() != null) {
                                rateByProduct.setFixedRatePR(rp.getFixedRate());
                            }
                            if (rp.getPercentageRate() != null) {
                                rateByProduct.setPercentageRatePR(rp.getPercentageRate());
                            }    
                            rateByProduct.setTotalInitialTransactionsExemptPR(rp.getTotalInitialTransactionsExempt());
                            rateByProduct.setTotalTransactionsExemptPerMonthPR(rp.getTotalTransactionsExemptPerMonth());
                            rateByProduct.setCreateDate(new Timestamp(new Date().getTime()));
                            rateByProduct = productEJB.saveRateByProduct(rateByProduct);
                            rateByProductList.add(rateByProduct);
                        }
                        indExist = 0;
                    }
                }
            }
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (rateByProductList != null && !rateByProductList.isEmpty()) {
                for (RateByProduct r : rateByProductList) {
                    item = new Listitem();
                    item.setValue(r);
                    item.appendChild(new Listcell (r.getProductId().getCountryId().getName()));
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
                    if(r.getApprovalProductRateId() != null){
                            item.appendChild(new Listcell((r.getApprovalProductRateId().getIndApproved().toString()).equals("true")?"Si":"No"));
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
        } finally {
            try {
                if (indLoadList == 0) {
                    lbxRecords.getItems().clear();
                    Listitem item = null;
                    if (list != null && !list.isEmpty()) {
                        for (RateByProgram rp : list) {
                            rateByProduct = new RateByProduct();
                            rateByProduct.setProductId(product);
                            rateByProduct.setChannelId(rp.getChannelId());
                            rateByProduct.setTransactionId(rp.getTransactionId());
                            rateByProduct.setFixedRate(rp.getFixedRate());
                            rateByProduct.setPercentageRate(rp.getPercentageRate());
                            rateByProduct.setIndCardHolderModification(rp.getIndCardHolderModification());
                            rateByProduct.setRateApplicationTypeId(rp.getRateApplicationTypeId());
                            rateByProduct.setTotalInitialTransactionsExempt(rp.getTotalInitialTransactionsExempt());
                            rateByProduct.setTotalTransactionsExemptPerMonth(rp.getTotalTransactionsExemptPerMonth());
                            rateByProduct.setFixedRatePR(rp.getFixedRate());
                            rateByProduct.setPercentageRatePR(rp.getPercentageRate());
                            rateByProduct.setTotalInitialTransactionsExemptPR(rp.getTotalInitialTransactionsExempt());
                            rateByProduct.setTotalTransactionsExemptPerMonthPR(rp.getTotalTransactionsExemptPerMonth());
                            rateByProduct.setCreateDate(new Timestamp(new Date().getTime()));
                            rateByProduct = productEJB.saveRateByProduct(rateByProduct);
                            rateByProductList.add(rateByProduct);
                        }
                        for (RateByProduct r : rateByProductList) {
                            item = new Listitem();
                            item.setValue(r);
                            item.appendChild(new Listcell(r.getProductId().getProgramId().getCardProgramManagerId().getCountryId().getName()));
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
                            if(r.getApprovalProductRateId() != null){
                                item.appendChild(new Listcell((r.getApprovalProductRateId().getIndApproved().toString()).equals("true")?"Si":"No"));
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

    public void getData(Long programId) {
        try {
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(QueryConstants.PARAM_PROGRAM_ID, programId);
            request1.setParams(params);
            rateByProgramList = productEJB.getRateByProgramByProgram(request1);
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

    private void loadCmbProduct(Integer evenInteger, long programId) {
        EJBRequest request1 = new EJBRequest();
        cmbProduct.getItems().clear();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_PROGRAM_ID, programId);
        request1.setParams(params);
        List<Product> product;
        try {
            product = productEJB.getProductByProgram(request1);
            loadGenericCombobox(product, cmbProduct, "name", evenInteger, Long.valueOf(RateByProductParam != null ? RateByProductParam.getProductId().getId() : 0));
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
            StringBuilder file = new StringBuilder(Labels.getLabel("cms.menu.rate.by.product.list"));
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
