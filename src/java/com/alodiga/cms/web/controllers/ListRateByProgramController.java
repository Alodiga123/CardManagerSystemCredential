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
import com.cms.commons.models.GeneralRate;
import com.cms.commons.models.ProductType;
import com.cms.commons.models.Program;
import com.cms.commons.models.RateByProgram;
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

public class ListRateByProgramController extends GenericAbstractListController<RateByProgram> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Combobox cmbProgram;
    private Label lblProductType;
    private ProductEJB productEJB = null;
    private ProgramEJB programEJB = null;
    private List<GeneralRate> generalRateList = null;
    private List<RateByProgram> rateByProgramByProgramList = new ArrayList<RateByProgram>();
    public static Program program = null;
    private ProductType productType = null;
    private Tab tabApprovalRates;
    private Tab tabRatesByProgram;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize(); 
        startListener();
    }

    public void startListener() {
        EventQueue que = EventQueues.lookup("updateRateByProgram", EventQueues.APPLICATION, true);
        que.subscribe(new EventListener() {
            public void onEvent(Event evt) {
                if (program != null) {
                    getData(program.getProductTypeId().getId(),program.getIssuerId().getCountryId().getId());
                    loadList(generalRateList);
                    tabApprovalRates.setDisabled(false);
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
            adminPage = "adminRateByProgram.zul";
            productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
            programEJB = (ProgramEJB) EJBServiceLocator.getInstance().get(EjbConstants.PROGRAM_EJB);
            eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
            loadCmbProgram(WebConstants.EVENT_ADD);
            if (program == null) {
                tabApprovalRates.setDisabled(true);
            } else {
                if (cmbProgram.getSelectedItem() != null) {
                    getData(program.getProductTypeId().getId(),program.getIssuerId().getCountryId().getId());
                    loadList(generalRateList);
                }
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public void onSelect$tabRatesByProgram() {
        try {
            doAfterCompose(self);
        } catch (Exception ex) {
            showError(ex);
        }
    }
       
    public Program getProgram() {
        return program;
    }
    
    public void onChange$cmbProgram() {
        lblProductType.setVisible(true);
        program = (Program) cmbProgram.getSelectedItem().getValue();
        lblProductType.setValue(program.getProductTypeId().getName());
        Sessions.getCurrent().setAttribute(WebConstants.PROGRAM, program);
        lbxRecords.getItems().clear();
        getData(program.getProductTypeId().getId(),program.getIssuerId().getCountryId().getId());
    }
  
    public void onClick$btnViewRates() throws InterruptedException {
        loadList(generalRateList);
        tabApprovalRates.setDisabled(false);
    }

    public void onClick$btnDelete() {
    }

    public void loadList(List<GeneralRate> list) {
        List<RateByProgram> rateByProgramList = new ArrayList<RateByProgram>();
        RateByProgram rateByProgram = null;
        EJBRequest request1 = new EJBRequest();
        Map params = new HashMap();
        int indLoadList = 0;
        String rbp;
        String gr;
        int indExist = 0;
        AdminApprovalProgramRateController adminApprovalProgramRate = new AdminApprovalProgramRateController();
        try {
            params.put(QueryConstants.PARAM_PROGRAM_ID, program.getId());
            request1.setParams(params);
            rateByProgramByProgramList = productEJB.getRateByProgramByProgram(request1);
            if (rateByProgramByProgramList != null) {
                indLoadList = 1;
                for (RateByProgram r : rateByProgramByProgramList) {
                    if (r.getApprovalProgramRateId() == null) {
                        r.setApprovalProgramRateId(adminApprovalProgramRate.getApprovalProgramRate());
                    }
                    rateByProgramList.add(r);
                }    
                if (list != null && !list.isEmpty()) {
                    for (GeneralRate g : list) {
                        gr = g.getChannelId().getId().toString()+g.getTransactionId().getId().toString()+program.getId().toString();
                        for (RateByProgram r : rateByProgramByProgramList) {
                            rbp = r.getChannelId().getId().toString()+r.getTransactionId().getId().toString()+r.getProgramId().getId().toString();
                            if (gr.equals(rbp)) {
                                indExist = 1;
                            }
                        }
                        if (indExist != 1) {
                            rateByProgram = new RateByProgram();
                            rateByProgram.setChannelId(g.getChannelId());
                            if (g.getFixedRate() != null) {
                                rateByProgram.setFixedRate(g.getFixedRate());
                            }
                            if (g.getPercentageRate() != null) {
                                rateByProgram.setPercentageRate(g.getPercentageRate());
                            }                            
                            rateByProgram.setIndCardHolderModification(g.getIndCardHolderModification());
                            rateByProgram.setProgramId(program);
                            rateByProgram.setRateApplicationTypeId(g.getRateApplicationTypeId());
                            rateByProgram.setTotalInitialTransactionsExempt(g.getTotalInitialTransactionsExempt());
                            rateByProgram.setTotalTransactionsExemptPerMonth(g.getTotalTransactionsExemptPerMonth());
                            rateByProgram.setTransactionId(g.getTransactionId());
                            if (g.getFixedRate() != null) {
                                rateByProgram.setFixedRateGR(g.getFixedRate());
                            }
                            if (g.getPercentageRate() != null) {
                                rateByProgram.setPercentageRateGR(g.getPercentageRate());
                            }
                            rateByProgram.setTotalInitialTransactionsExemptGR(g.getTotalInitialTransactionsExempt());
                            rateByProgram.setTotalTransactionsExemptPerMonthGR(g.getTotalTransactionsExemptPerMonth());
                            rateByProgram.setCreateDate(new Timestamp(new Date().getTime()));
                            rateByProgram = productEJB.saveRateByProgram(rateByProgram);
                            rateByProgramList.add(rateByProgram);
                        }
                        indExist = 0;
                    }
                } 
            }
            if (lbxRecords != null) {
                lbxRecords.getItems().clear();
                Listitem item = null;
                if (rateByProgramList != null && !rateByProgramList.isEmpty()) {
                    for (RateByProgram r : rateByProgramList) {
                        item = new Listitem();
                        item.setValue(r);
                        item.appendChild(new Listcell(r.getProgramId().getCardProgramManagerId().getCountryId().getName()));
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
                        if(r.getApprovalProgramRateId() != null){
                            item.appendChild(new Listcell((r.getApprovalProgramRateId().getIndApproved().toString()).equals("true")?"Si":"No"));
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
                        for (GeneralRate g : list) {
                            rateByProgram = new RateByProgram();
                            rateByProgram.setChannelId(g.getChannelId());
                            if (g.getFixedRate() != null) {
                                rateByProgram.setFixedRate(g.getFixedRate());
                            }
                            if (g.getPercentageRate() != null) {
                                rateByProgram.setPercentageRate(g.getPercentageRate());
                            } 
                            rateByProgram.setIndCardHolderModification(g.getIndCardHolderModification());
                            rateByProgram.setProgramId(program);
                            rateByProgram.setRateApplicationTypeId(g.getRateApplicationTypeId());
                            rateByProgram.setTotalInitialTransactionsExempt(g.getTotalInitialTransactionsExempt());
                            rateByProgram.setTotalTransactionsExemptPerMonth(g.getTotalTransactionsExemptPerMonth());
                            rateByProgram.setTransactionId(g.getTransactionId());
                            rateByProgram.setFixedRateGR(g.getFixedRate());
                            rateByProgram.setPercentageRateGR(g.getPercentageRate());
                            rateByProgram.setTotalInitialTransactionsExemptGR(g.getTotalInitialTransactionsExempt());
                            rateByProgram.setTotalTransactionsExemptPerMonthGR(g.getTotalTransactionsExemptPerMonth());
                            rateByProgram.setCreateDate(new Timestamp(new Date().getTime()));
                            rateByProgram = productEJB.saveRateByProgram(rateByProgram);
                            rateByProgramList.add(rateByProgram);
                        }
                        for (RateByProgram r : rateByProgramList) {
                            item = new Listitem();
                            item.setValue(r);
                            item.appendChild(new Listcell(r.getProgramId().getCardProgramManagerId().getCountryId().getName()));
                            item.appendChild(new Listcell(r.getChannelId().getName()));
                            item.appendChild(new Listcell(r.getTransactionId().getCode()));
                            item.appendChild(new Listcell(r.getTransactionId().getDescription()));
                            if (r.getFixedRate() != null) {
                                item.appendChild(new Listcell(r.getFixedRate().toString()));
                            }
                            if (r.getPercentageRate() != null) {
                                item.appendChild(new Listcell(r.getPercentageRate().toString()));
                            }                            
                            if(r.getApprovalProgramRateId() != null){
                                item.appendChild(new Listcell((r.getApprovalProgramRateId().getIndApproved().toString()).equals("true")?"Si":"No"));
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

    public void getData(Integer productTypeId, Integer countryId) {
        try {
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(QueryConstants.PARAM_PRODUCT_TYPE_ID, productTypeId);
            params.put(QueryConstants.PARAM_COUNTRY_ID, countryId);
            request1.setParams(params);
            generalRateList = productEJB.getGeneralRateByProductType(request1);
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
           showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        }
    }
    
    private void showEmptyList(){
                Listitem item = new Listitem();
                item.appendChild(new Listcell(Labels.getLabel("sp.error.empty.list")));
                item.appendChild(new Listcell());
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
            loadGenericCombobox(programs,cmbProgram,"name",evenInteger,Long.valueOf(0));
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
            StringBuilder file = new StringBuilder(Labels.getLabel("cms.menu.rate.by.program.list"));
            file.append("_");
            file.append(date);
            Utils.exportExcel(lbxRecords, file.toString());
        } catch (Exception ex) {
            showError(ex);
        }
        
    } 

    @Override
    public List<RateByProgram> getFilterList(String filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void loadDataList(List<RateByProgram> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void getData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
