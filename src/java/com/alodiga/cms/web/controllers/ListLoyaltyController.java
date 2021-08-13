package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProgramEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.custom.components.ListcellEditButton;
import com.alodiga.cms.web.custom.components.ListcellViewButton;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import static com.alodiga.cms.web.generic.controllers.GenericDistributionController.request;
import com.alodiga.cms.web.utils.Utils;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.ProgramLoyalty;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

public class ListLoyaltyController extends GenericAbstractListController<ProgramLoyalty> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Textbox txtName;
    private List<ProgramLoyalty> programLoyalty = null;
    private ProgramEJB programEJB = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
    }

    public void startListener() {
        EventQueue que = EventQueues.lookup("updateProgramLoyalty", EventQueues.APPLICATION, true);
        que.subscribe(new EventListener() {

            public void onEvent(Event evt) {
                getData();
                loadDataList(programLoyalty);
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
            adminPage = "TabLoyalty.zul";
            programEJB = (ProgramEJB) EJBServiceLocator.getInstance().get(EjbConstants.PROGRAM_EJB);
            getData();
            loadDataList(programLoyalty);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void getData() {
        try {
            programLoyalty = new ArrayList<ProgramLoyalty>();
            request.setFirst(0);
            request.setLimit(null);
            programLoyalty = programEJB.getProgramLoyalty(request);//getProgram(request);
        } catch (EmptyListException ex) {
            Logger.getLogger(ListLoyaltyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GeneralException ex) {
            Logger.getLogger(ListLoyaltyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullParameterException ex) {
            Logger.getLogger(ListLoyaltyController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void onClick$btnDelete() {

    }

    public void loadDataList(List<ProgramLoyalty> list) {
        try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (list != null && !list.isEmpty()) {

                for (ProgramLoyalty programLoyalty : list) {
                    item = new Listitem();
                    item.setValue(programLoyalty);
                    item.appendChild(new Listcell(programLoyalty.getDescription()));
                    item.appendChild(new Listcell(programLoyalty.getProductId().getName()));
                    item.appendChild(new Listcell(programLoyalty.getProgramLoyaltyTypeId().getName()));
                    item.appendChild(new Listcell(programLoyalty.getStatusProgramLoyaltyId().getDescription()));
                    item.appendChild(permissionEdit ? new ListcellEditButton(adminPage, programLoyalty) : new Listcell());
                    item.appendChild(permissionRead ? new ListcellViewButton(adminPage, programLoyalty) : new Listcell());
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
                item.appendChild(new Listcell());
                item.setParent(lbxRecords);
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    public void onClick$btnAdd() throws InterruptedException {
        Sessions.getCurrent().setAttribute(WebConstants.EVENTYPE, WebConstants.EVENT_ADD);
        Executions.getCurrent().sendRedirect(adminPage);
    }

    private void showEmptyList() {
        Listitem item = new Listitem();
        item.appendChild(new Listcell(Labels.getLabel("sp.error.empty.list")));
        item.appendChild(new Listcell());
        item.appendChild(new Listcell());
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
            StringBuilder file = new StringBuilder(Labels.getLabel("cms.crud.loyaltyProgram.listDownload"));
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
    
    public void onClick$btnSearch() throws InterruptedException {
        try {
            loadDataList(getFilterList(txtName.getText()));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    @Override
    public List<ProgramLoyalty> getFilterList(String filter) {
        List<ProgramLoyalty> programList_ = new ArrayList<ProgramLoyalty>();
        try {
            if (filter != null && !filter.equals("")) {
                programList_ = programEJB.getSearchProgramLoyalty(filter);
            } else {
                return programLoyalty;
            }
        } catch (Exception ex) {
            showError(ex);
        }
        return programList_; 
    }

}
