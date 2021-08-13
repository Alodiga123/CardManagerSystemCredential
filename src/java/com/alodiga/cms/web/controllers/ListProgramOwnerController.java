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
import com.cms.commons.models.LegalPerson;
import com.cms.commons.models.NaturalPerson;
import com.cms.commons.models.Person;
import com.cms.commons.models.PhonePerson;
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
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

public class ListProgramOwnerController extends GenericAbstractListController<Person> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtRequestNumber;
    private PersonEJB personEJB = null;
    private List<Person> persons = null;
    private List<LegalPerson> legalPersonList = null;
    private List<NaturalPerson> naturalPersonList = null;
    public static int indOwnerOption = 0;
    private Textbox txtName;
    private List<PhonePerson> phonePersonList = null;
    NaturalPerson programOwnerNatural = null;
    LegalPerson programOwnerLegal = null;
    private Long indRequestOption = 0L;

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
            Sessions.getCurrent().setAttribute(WebConstants.OPTION_MENU, Constants.LIST_PROGRAM_OWNER);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            getData();
            loadDataList(persons);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public int getIndOwnerOption() {
        return indOwnerOption;
    }

    public void onClick$btnAddProgramOwnerNaturalPerson() throws InterruptedException {
        Sessions.getCurrent().setAttribute(WebConstants.EVENTYPE, WebConstants.EVENT_ADD);
        Executions.getCurrent().sendRedirect("TabProgramOwnerNaturalPerson.zul");
    }

    public void onClick$btnAddProgramOwnerLegalPerson() throws InterruptedException {
        Sessions.getCurrent().setAttribute(WebConstants.EVENTYPE, WebConstants.EVENT_ADD);
        Executions.getCurrent().sendRedirect("TabProgramOwnerLegalPerson.zul");
    }

    public void onClick$btnDelete() {
    }

    public void loadDataList(List<Person> list) {
        String ownerNameLegal = "";
        String phoneNaturalPerson = "";
        try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                for (Person person : list) {
                    item = new Listitem();
                    item.setValue(person);
                    if (person.getPersonTypeId().getIndNaturalPerson() == true) {
                        adminPage = "TabProgramOwnerNaturalPerson.zul";
                        StringBuilder OwnerName = new StringBuilder(person.getNaturalPerson().getFirstNames());
                        OwnerName.append(" ");
                        OwnerName.append(person.getNaturalPerson().getLastNames());
                        item.appendChild(new Listcell(OwnerName.toString()));
                        item.appendChild(new Listcell(person.getCountryId().getName()));
                        item.appendChild(new Listcell(person.getPersonTypeId().getDescription()));
                        if (person.getEmail() != null) {
                            item.appendChild(new Listcell(person.getEmail()));
                        }
                        //Se obtiene el teléfono de la persona natural
                        if (person.getPhonePerson() == null) {
                            EJBRequest request1 = new EJBRequest();
                            Map params = new HashMap();
                            params.put(Constants.PERSON_KEY, person.getId());
                            request1.setParams(params);
                            phonePersonList = personEJB.getPhoneByPerson(request1);
                            for (PhonePerson phone : phonePersonList) {
                                phoneNaturalPerson = phone.getNumberPhone();
                            }
                            item.appendChild(new Listcell(phoneNaturalPerson));                          
                        } else {
                            item.appendChild(new Listcell(person.getPhonePerson().getNumberPhone()));
                        }
                        item.appendChild(permissionEdit ? new ListcellEditButton(adminPage, person.getNaturalPerson()) : new Listcell());
                        item.appendChild(permissionRead ? new ListcellViewButton(adminPage, person.getNaturalPerson()) : new Listcell());
                    } else {

                        //Obtiene el Gerente del Programa (persona jurídica)
                        EJBRequest request1 = new EJBRequest();
                        Map params = new HashMap();
                        params.put(Constants.PERSON_KEY, person.getId());
                        request1.setParams(params);
                        legalPersonList = personEJB.getLegalPersonByPerson(request1);
                        for (LegalPerson n : legalPersonList) {
                            programOwnerLegal = n;
                        }
                        adminPage = "TabProgramOwnerLegalPerson.zul";
                        ownerNameLegal = programOwnerLegal.getEnterpriseName();
                        item.appendChild(new Listcell(ownerNameLegal));
                        item.appendChild(new Listcell(person.getCountryId().getName()));
                        item.appendChild(new Listcell(person.getPersonTypeId().getDescription()));
                        if (person.getEmail() != null) {
                            item.appendChild(new Listcell(person.getEmail()));
                        }
                        if (programOwnerLegal.getEnterprisePhone() != null) {
                            if (!programOwnerLegal.getEnterprisePhone().equalsIgnoreCase("")) {
                                item.appendChild(new Listcell(programOwnerLegal.getEnterprisePhone()));
                            } else {
                                item.appendChild(new Listcell(""));
                            }
                        } else {
                            item.appendChild(new Listcell(""));
                        }
                        item.appendChild(permissionEdit ? new ListcellEditButton(adminPage, programOwnerLegal) : new Listcell());
                        item.appendChild(permissionRead ? new ListcellViewButton(adminPage, programOwnerLegal) : new Listcell());
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
        try {
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.PERSON_CLASSIFICATION_KEY, Constants.PERSON_CLASSIFICATION_PROGRAM_OWNER);
            request1.setParams(params);
            persons = personEJB.getPersonByClassification(request1);
            for (Person p: persons) {                
                if (p.getPersonTypeId().getIndNaturalPerson() == true) {
                    //Obtiene el Gerente del Programa (persona natural)
                    request1 = new EJBRequest();
                    params = new HashMap(); 
                    params.put(Constants.PERSON_KEY, p.getId());
                    request1.setParams(params);
                    naturalPersonList = personEJB.getNaturalPersonByPerson(request1);
                    for (NaturalPerson n : naturalPersonList) {
                        programOwnerNatural = n;
                        if (programOwnerNatural.getPersonId().getPhonePerson() == null) {
                            
                        }
                    }
                    //Actualiza la lista de gerentes de programas
                    p.setNaturalPerson(programOwnerNatural);
                } else {
                    //Obtiene el Gerente del Programa (persona jurídica)
                    request1 = new EJBRequest();
                    params = new HashMap(); 
                    params.put(Constants.PERSON_KEY, p.getId());
                    request1.setParams(params);
                    legalPersonList = personEJB.getLegalPersonByPerson(request1);
                    for (LegalPerson n : legalPersonList) {
                        programOwnerLegal = n;
                    }
                    //Actualiza la lista de gerentes de programas
                    p.setLegalPerson(programOwnerLegal);
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
            StringBuilder file = new StringBuilder(Labels.getLabel("cms.crud.programOwner.listDownload"));
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
                params.put(Constants.PERSON_CLASSIFICATION_KEY, Constants.PERSON_CLASSIFICATION_PROGRAM_OWNER);
                params.put(Constants.PARAM_PERSON_NAME, filter);
                request1.setParams(params);
                personList_ = personEJB.searchPerson(request1);
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
