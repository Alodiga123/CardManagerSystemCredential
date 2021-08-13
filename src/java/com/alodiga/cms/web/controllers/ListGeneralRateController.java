package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import static com.alodiga.cms.web.generic.controllers.GenericDistributionController.request;
import com.alodiga.cms.web.utils.Utils;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Country;
import com.cms.commons.models.GeneralRate;
import com.cms.commons.models.Request;
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
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;

public class ListGeneralRateController extends GenericAbstractListController<GeneralRate> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtRequestNumber;
    private ProductEJB productEJB = null;
    private UtilsEJB utilsEJB = null;
    public static List<GeneralRate> generalRateList = null;
    private Toolbarbutton tbbTitle;
    private Combobox cmbCountry;
    private Textbox txtName;
    private Tab tabGeneralRates;
    private Tab tabApprovalRates;
    private static Country country = null;
    public int indRateApprove = 0;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
        startListener();
    }

    public void startListener() {
        EventQueue que = EventQueues.lookup("updateGeneralRate", EventQueues.APPLICATION, true);
        que.subscribe(new EventListener() {
            public void onEvent(Event evt) {                
                if (country != null) {
                    getData(country.getId());
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
            permissionEdit = true;
            permissionAdd = true; 
            permissionRead = true;
            tbbTitle.setLabel(Labels.getLabel("cms.common.generalRate.list"));
            adminPage = "adminGeneralRate.zul";
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
            loadCmbCountry(eventType); 
            if (country == null) {
                tabApprovalRates.setDisabled(true);
            } else {
                if (cmbCountry.getSelectedItem() != null) {
                    getData(country.getId());
                    loadList(generalRateList);
                }
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public List<GeneralRate> getGeneralRateList() {
        return generalRateList;
    }
    
    public void onSelect$tabGeneralRates() {
        try {
            doAfterCompose(self);
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public Country getCountry() {
        return country;
    }
    
    public void onChange$cmbCountry() {
        country = (Country) cmbCountry.getSelectedItem().getValue();
        Sessions.getCurrent().setAttribute(WebConstants.COUNTRY, country);
        getData(country.getId());
    }
 
    public void onClick$btnDelete() {
    }

    public void loadList(List<GeneralRate> list) {
        String applicantName = "";
        try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                for (GeneralRate generalRate : list) {
                    item = new Listitem();
                    item.setValue(generalRate);
                    item.appendChild(new Listcell(generalRate.getCountryId().getName()));
                    item.appendChild(new Listcell(generalRate.getProductTypeId().getName()));
                    item.appendChild(new Listcell(generalRate.getChannelId().getName()));
                    item.appendChild(new Listcell(generalRate.getTransactionId().getCode()));
                    if(generalRate.getApprovalGeneralRateId() != null){
                     item.appendChild(new Listcell((generalRate.getApprovalGeneralRateId().getIndApproved().toString()).equals("true")?"Si":"No"));
                    }else{
                     item.appendChild(new Listcell("No"));
                    }
                    item.appendChild(createButtonEditModal(generalRate));
                    item.appendChild(createButtonViewModal(generalRate));
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

    public void getData(int countryId) {
        generalRateList = new ArrayList<GeneralRate>();
        AdminApprovalRatesController adminApprovalRates = new AdminApprovalRatesController();
        try {           
            request.setFirst(0);
            request.setLimit(null);
            generalRateList = productEJB.getGeneralRateByCountry(country);
            if (Sessions.getCurrent().getAttribute(WebConstants.IND_RATE_APPROVE) != null) {
                indRateApprove = (Integer) Sessions.getCurrent().getAttribute(WebConstants.IND_RATE_APPROVE);
            }            
            if (indRateApprove == 1) {
                for (GeneralRate gr: generalRateList) {
                    if (gr.getApprovalGeneralRateId() == null) {
                        gr.setApprovalGeneralRateId(adminApprovalRates.getApprovalGeneralRate());
                    }
                }
            }
            indRateApprove = 0;
            Sessions.getCurrent().setAttribute(WebConstants.IND_RATE_APPROVE, indRateApprove);
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
           showEmptyList();
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
                item.setParent(lbxRecords);  
    }

    public void onClick$btnDownload() throws InterruptedException {
        try {
            String pattern = "dd-MM-yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            StringBuilder file = new StringBuilder(Labels.getLabel("cms.crud.generalRate.listDownload"));
            file.append("_");
            file.append(date);
            Utils.exportExcel(lbxRecords, file.toString());
        } catch (Exception ex) {
            showError(ex);
        }        
    } 

    public void onClick$btnClear() throws InterruptedException {
        loadList(generalRateList);
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
    
   public void onClick$btnAddGeneralRate() throws InterruptedException {
        try {
            Sessions.getCurrent().setAttribute(WebConstants.EVENTYPE, WebConstants.EVENT_ADD);
            Map<String, Object> paramsPass = new HashMap<String, Object>();
            paramsPass.put("object", generalRateList);
            final Window window = (Window) Executions.createComponents(adminPage, null, paramsPass);
            window.doModal();
        } catch (Exception ex) {
            this.showMessage("sp.error.general", true, ex);
        }
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
   
    private void loadCmbCountry(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<Country> countries;
        try {
            countries = utilsEJB.getCountries(request1);  
            loadGenericCombobox(countries, cmbCountry, "name", evenInteger, Long.valueOf(0));
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
    
    public void onClick$btnViewRates() throws InterruptedException {
        try {
            if (cmbCountry.getSelectedIndex() == -1) {
                this.showMessage("cms.error.countryName.error", true, null);
                cmbCountry.setFocus(true);                
            } else {
                loadList(generalRateList);
                if (!generalRateList.isEmpty()) {
                     tabApprovalRates.setDisabled(false);
                } else {
                     tabApprovalRates.setDisabled(true);
                }
            }
            
        } catch (Exception ex) {
            showError(ex);
        }
    }

    @Override
    public List<GeneralRate> getFilterList(String filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void loadDataList(List<GeneralRate> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void getData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
