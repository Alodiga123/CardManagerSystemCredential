package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.commons.ejb.ProgramEJB;
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
import com.cms.commons.models.AccountCard;
import com.cms.commons.models.AccountProperties;
import com.cms.commons.models.Card;
import com.cms.commons.models.Country;
import com.cms.commons.models.Product;
import com.cms.commons.models.Program;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import com.cms.commons.util.QueryConstants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;

public class ListAccountCardController extends GenericAbstractListController<AccountCard> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Combobox cmbCountry;
    private Combobox cmbProgram;
    private Combobox cmbProduct;
    private ProductEJB productEJB = null;
    private ProgramEJB programEJB = null;
    private CardEJB cardEJB = null;
    private UtilsEJB utilsEJB = null;
    private List<Program> programList = new ArrayList<Program>(); 
    private List<Product> productList = new ArrayList<Product>();
    private List<Card> cardList = null;
    private List<AccountCard> accountCardByProductList = new ArrayList<AccountCard>();
    private AccountCard  accountCardParam;
    private Program programParam; 
    private AccountCard accountCard = null;
    private Card card = null;
    private Product product = null;
    private Program program = null;
    private AccountProperties accountProperties = null;
    private Button btnViewAccounts;
    private List<AccountCard> accountCardList = null;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize(); 
        startListener();
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
            adminPage = "adminAccountCard.zul";
            productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
            programEJB = (ProgramEJB) EJBServiceLocator.getInstance().get(EjbConstants.PROGRAM_EJB);
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            btnViewAccounts.setDisabled(true);
            loadCmbCountry(WebConstants.EVENT_ADD);
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    
     public void onChange$cmbCountry() {
        cmbProgram.setValue("");
        Country country = (Country) cmbCountry.getSelectedItem().getValue();
        loadCmbProgram (WebConstants.EVENT_ADD, country.getId());
    }
     
    public void onChange$cmbProgram() {
        cmbProduct.setValue("");
        program = (Program) cmbProgram.getSelectedItem().getValue();
        loadCmbProduct(WebConstants.EVENT_ADD, program.getId());
    }
    
    public void onChange$cmbProduct() {
        btnViewAccounts.setDisabled(false); 
        product = (Product) cmbProduct.getSelectedItem().getValue();
    }
    
    public void onClick$btnViewAccounts() throws InterruptedException {
        getData();
        loadDataList(accountCardList);
    }    

    public void onClick$btnDelete() {
    }

    public void loadDataList(List<AccountCard> list) { 
        try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                btnDownload.setVisible(true);
                for (AccountCard accountCard : accountCardList) {
                    item = new Listitem();
                    item.setValue(accountCard);
                    item.appendChild(new Listcell(accountCard.getCardId().getCardNumber().toString()));
                    item.appendChild(new Listcell(accountCard.getCardId().getCardHolder().toString()));
                    item.appendChild(new Listcell(accountCard.getAccountNumber().toString()));
                    item.appendChild(new Listcell(accountCard.getStatusAccountId().getDescription()));
                    item.appendChild(new ListcellEditButton(adminPage, accountCard));
                    item.appendChild(new ListcellViewButton(adminPage, accountCard,true));
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
        try {
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(QueryConstants.PARAM_PRODUCT_ID, product.getId());
            request1.setParams(params);
            accountCardList = cardEJB.getAccountCardByProduct(request1);
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
    
    private void loadCmbCountry(Integer evenInteger) {
        cmbCountry.getItems().clear();
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
    
    private void loadCmbProgram(Integer evenInteger, int countryId) {
        EJBRequest request1 = new EJBRequest();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_COUNTRY_ID, countryId);
        request1.setParams(params);
        List<Program> programs;
        try {
            programs = programEJB.getProgramByCountry(request1);
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

    
    private void loadCmbProduct(Integer evenInteger, Long programId) {
        EJBRequest request1 = new EJBRequest();
        cmbProduct.getItems().clear();
        Map params = new HashMap();
        params.put(QueryConstants.PARAM_PROGRAM_ID, programId);
        request1.setParams(params);
        List<Product> product;
        try {
            product = productEJB.getProductByProgram(request1);
            loadGenericCombobox(product, cmbProduct, "name", evenInteger, Long.valueOf(0));      
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
            StringBuilder file = new StringBuilder(Labels.getLabel("cms.crud.accountCard.listDownload"));
            file.append("_");
            file.append(date);
            Utils.exportExcel(lbxRecords, file.toString());
        } catch (Exception ex) {
            showError(ex);
        }
        
    } 

   @Override
    public List<AccountCard> getFilterList(String filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    public void loadList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}






