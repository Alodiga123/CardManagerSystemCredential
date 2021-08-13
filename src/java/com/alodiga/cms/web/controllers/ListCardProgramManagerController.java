package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.PersonEJB;
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
import com.cms.commons.models.LegalPerson;
import com.cms.commons.models.User;
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

public class ListCardProgramManagerController extends GenericAbstractListController<LegalPerson> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private List<LegalPerson> legalperson = null;
    private User currentUser;
    private PersonEJB personEJB = null;
    private UtilsEJB utilsEJB = null;
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
            adminPage = "TabCardProgramManager.zul";
            currentUser = (User) session.getAttribute(Constants.USER_OBJ_SESSION);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            getData();
            loadDataList(legalperson);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnAdd() throws InterruptedException {
        Sessions.getCurrent().setAttribute(WebConstants.EVENTYPE, WebConstants.EVENT_ADD);
        Executions.getCurrent().sendRedirect("TabCardProgramManager.zul");
     }

        public void onClick$btnClear() throws InterruptedException {
        txtName.setText("");
    }

        
    public void onClick$btnSearch() throws InterruptedException {
            try {
                loadDataList(getFilterList(txtName.getText()));
            } catch (Exception ex) {
                showError(ex);
        }
    }
        
    public void loadDataList(List<LegalPerson> list) {
        try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {
                for (LegalPerson legalperson : list) {
                    item = new Listitem();
                    item.setValue(legalperson);
                    item.appendChild(new Listcell(legalperson.getPersonId().getCountryId().getName()));
                    item.appendChild(new Listcell(legalperson.getIdentificationNumber().toString()));
                    item.appendChild(new Listcell(legalperson.getEnterpriseName().toString()));
                    item.appendChild(new Listcell(legalperson.getEnterprisePhone().toString()));
                    item.appendChild(new ListcellEditButton(adminPage, legalperson));
                    item.appendChild(new ListcellViewButton(adminPage, legalperson,true));
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
                item.setParent(lbxRecords);
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void getData() {
        legalperson = new ArrayList<LegalPerson>();
        try {
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.PERSON_CLASSIFICATION_KEY, Constants.CLASSIFICATION_CARD_MANAGEMENT_PROGRAM);
            request1.setParams(params);
            legalperson = utilsEJB.getLegalPersonByPersonClassification(request1);
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

    public void onClick$btnDownload() throws InterruptedException {
        try {
            String pattern = "dd-MM-yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            StringBuilder file = new StringBuilder(Labels.getLabel("cms.crud.cardProgramManager.list"));
            file.append("_");
            file.append(date);
            Utils.exportExcel(lbxRecords, file.toString());
        } catch (Exception ex) {
            showError(ex);
        }
        
    }
   
    public List<LegalPerson> getFilterList(String filter) {
        List<LegalPerson> legalPersonList_ = new ArrayList<LegalPerson>();
        try {
            if (filter != null && !filter.equals("")) {
            EJBRequest request1 = new EJBRequest();
      
            Map params = new HashMap();
            params.put(Constants.PERSON_CLASSIFICATION_KEY, Constants.CLASSIFICATION_CARD_MANAGEMENT_PROGRAM);
            params.put(Constants.PERSON_ENTERPRISE_NAME_KEY, filter);
            request1.setParams(params);

            request1.setParams(params);
                legalPersonList_ = personEJB.searchLegalPerson(request1);
            } else {
                return legalperson;
            }
        } catch (Exception ex) {
            showError(ex);
        }
        return legalPersonList_;
    }
    
 

}
