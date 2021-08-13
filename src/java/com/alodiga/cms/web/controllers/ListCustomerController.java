package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.custom.components.ListcellEditButton;
import com.alodiga.cms.web.custom.components.ListcellViewButton;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import com.alodiga.cms.web.utils.Utils;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.LegalCustomer;
import com.cms.commons.models.NaturalCustomer;
import com.cms.commons.models.Person;
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
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

public class ListCustomerController extends GenericAbstractListController<Person> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtRequestNumber;
    private PersonEJB personEJB = null;
    private List<Person> persons = null;
    private List<LegalCustomer> legalCustomerList = null;
    private List<NaturalCustomer> naturalCustomerList = null;
    private Textbox txtName;


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
            Sessions.getCurrent().setAttribute(WebConstants.OPTION_MENU, Constants.LIST_CUSTOMER_MANAGEMENT);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            getData();
            loadDataList(persons);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnDelete() {
    }

    public void loadDataList(List<Person> list) {
        String customerNameLegal = "";
        NaturalCustomer naturalCustomer = null;
        LegalCustomer legalCustomer = null;
        try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                for (Person person : list) {
                    item = new Listitem();
                    item.setValue(person);
                    item.appendChild(new Listcell(person.getCountryId().getName()));
                    item.appendChild(new Listcell(person.getPersonTypeId().getDescription()));
                    if (person.getPersonTypeId().getIndNaturalPerson() == true) {                 
                        item.appendChild(new Listcell(person.getNaturalCustomer().getIdentificationNumber()));
                        StringBuilder customerName = new StringBuilder(person.getNaturalCustomer().getFirstNames());
                        customerName.append(" ");
                        customerName.append(person.getNaturalCustomer().getLastNames());
                        item.appendChild(new Listcell(customerName.toString()));
                        item.appendChild(new Listcell(person.getNaturalCustomer().getStatusCustomerId().getDescription()));
                        adminPage = "TabNaturalPersonCustommer.zul"; 
                        item.appendChild(permissionEdit ? new ListcellEditButton(adminPage, person.getNaturalCustomer()) : new Listcell());
                        item.appendChild(permissionRead ? new ListcellViewButton(adminPage, person.getNaturalCustomer()) : new Listcell());                        
                    } else {                        
                        item.appendChild(new Listcell(person.getLegalCustomer().getIdentificationNumber()));
                        customerNameLegal = person.getLegalCustomer().getEnterpriseName();
                        item.appendChild(new Listcell(customerNameLegal));
                        item.appendChild(new Listcell(person.getLegalCustomer().getStatusCustomerId().getDescription()));
                        adminPage = "TabLegalPersonCustommer.zul";
                        item.appendChild(permissionEdit ? new ListcellEditButton(adminPage, person.getLegalCustomer()) : new Listcell());
                        item.appendChild(permissionRead ? new ListcellViewButton(adminPage, person.getLegalCustomer()) : new Listcell());                       
                    }
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
        persons = new ArrayList<Person>();
        NaturalCustomer naturalCustomer = null;
        LegalCustomer legalCustomer = null;
        try {
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.PERSON_CLASSIFICATION_KEY, Constants.PERSON_CLASSIFICATION_CUSTOMER);
            request1.setParams(params);
            persons = personEJB.getPersonByClassification(request1);
            for (Person person : persons) {
                if (person.getPersonTypeId().getIndNaturalPerson() == true) {
                    if (person.getNaturalCustomer() == null) {
                        request1 = new EJBRequest();
                        params = new HashMap();
                        params.put(Constants.PERSON_KEY, person.getId());
                        request1.setParams(params);
                        naturalCustomerList = personEJB.getNaturalCustomerByPerson(request1);
                        for (NaturalCustomer nc : naturalCustomerList) {
                            person.setNaturalCustomer(nc);                                
                        }
                    }
                } else {
                    if (person.getLegalCustomer() == null) {
                        request1 = new EJBRequest();
                        params = new HashMap();
                        params.put(Constants.PERSON_KEY, person.getId());
                        request1.setParams(params);
                        legalCustomerList = personEJB.getLegalCustomerByPerson(request1);
                        for (LegalCustomer lc : legalCustomerList) {
                            person.setLegalCustomer(lc);
                        }
                    }
                }           
            }               
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
            StringBuilder file = new StringBuilder(Labels.getLabel("cms.crud.customer.listDownload"));
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

    public List<Person> getFilterList(String filter) {
    List<Person> personList_ = new ArrayList<Person>();
        try {
            if (filter != null && !filter.equals("")) {
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.PERSON_CLASSIFICATION_KEY, Constants.PERSON_CLASSIFICATION_CUSTOMER);
            params.put(Constants.PARAM_PERSON_NAME, filter);
            request1.setParams(params);
               personList_ = personEJB.searchPersonByClassification(request1);
            } else {
                return persons;
            }
        } catch (Exception ex) {
            showError(ex);
        }
        return personList_;    
    }

      public void onClick$btnSearch() throws InterruptedException {
        try {
            loadDataList(getFilterList(txtName.getText()));
        } catch (Exception ex) {
            showError(ex);
        }
    }
}
