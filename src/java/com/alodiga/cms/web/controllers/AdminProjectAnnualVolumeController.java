package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProgramEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Program;
import com.cms.commons.models.ProjectAnnualVolume;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;

public class AdminProjectAnnualVolumeController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Combobox cmbYear;
    private Doublebox intAccountsNumber;
    private Doublebox intActiveCardNumber;
    private Doublebox dbxAverageLoad;
    private Doublebox dbxAverageCardBalance;
    private ProgramEJB programEJB = null;
    private ProjectAnnualVolume projectAnnualVolumeParam;
    private Button btnSave;
    public Window winProjectAnnualVolume;
    private Integer eventType;
    private ProjectAnnualVolume projectAnnualVolume = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            projectAnnualVolumeParam = null;
        } else {
            projectAnnualVolumeParam = (ProjectAnnualVolume) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                projectAnnualVolumeParam = (ProjectAnnualVolume) Sessions.getCurrent().getAttribute("object");
                break;
            case WebConstants.EVENT_VIEW:
                projectAnnualVolumeParam = (ProjectAnnualVolume) Sessions.getCurrent().getAttribute("object");
                break;
            case WebConstants.EVENT_ADD:
                projectAnnualVolumeParam = null;
                break;
            default:
                break;
        }
        try {
            programEJB = (ProgramEJB) EJBServiceLocator.getInstance().get(EjbConstants.PROGRAM_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        intAccountsNumber.setRawValue(null);
        intActiveCardNumber.setRawValue(null);
        dbxAverageLoad.setRawValue(null);
        dbxAverageCardBalance.setRawValue(null);
    }

    private void loadFields(ProjectAnnualVolume projectAnnualVolume) {
        try {
            intAccountsNumber.setValue(projectAnnualVolume.getAccountsNumber());
            intActiveCardNumber.setValue(projectAnnualVolume.getActiveCardNumber());
            dbxAverageLoad.setValue(projectAnnualVolume.getAverageLoad().doubleValue());
            dbxAverageCardBalance.setValue(projectAnnualVolume.getAverageCardBalance().doubleValue());
            
            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        intAccountsNumber.setReadonly(true);
        intActiveCardNumber.setReadonly(true);
        dbxAverageLoad.setReadonly(true);
        dbxAverageCardBalance.setReadonly(true);
        cmbYear.setDisabled(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (cmbYear.getSelectedItem() == null) {
            cmbYear.setFocus(true);
            this.showMessage("cms.error.year.notSelected", true, null);
        } else if (intAccountsNumber.getText().isEmpty()) {
            intAccountsNumber.setFocus(true);
            this.showMessage("cms.error.field.accountsNumber", true, null);
        } else if (intActiveCardNumber.getText().isEmpty()) {
            intActiveCardNumber.setFocus(true);
            this.showMessage("cms.error.field.activeCardNumber", true, null);
        } else if (dbxAverageLoad.getText().isEmpty()) {
            dbxAverageLoad.setFocus(true);
            this.showMessage("cms.error.field.averageLoad", true, null);
        } else if (dbxAverageCardBalance.getText().isEmpty()) {
            dbxAverageCardBalance.setFocus(true);
            this.showMessage("cms.error.field.averageCardBalance", true, null);
        } else {
            return true;
        }
        return false;
    }
    
    public boolean validateProjectAnnualVolume() {
        if (intActiveCardNumber.getValue() > intAccountsNumber.getValue()) {
                intActiveCardNumber.setFocus(true);
                this.showMessage("cms.error.activeCardNumber>AccountNumber.valid", true, null);
                return false;
            }
        return true;
    }

    private void saveProjectAnnualVolume(ProjectAnnualVolume _projectAnnualVolume) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Program program = null;
        List<ProjectAnnualVolume> projectAnnualVolumeList = null;
        int indRegisterExist = 0;
        try {
            if (_projectAnnualVolume != null) {
                projectAnnualVolume = _projectAnnualVolume;
            } else {//New address
                projectAnnualVolume = new ProjectAnnualVolume();
            }

            //Program
            AdminProgramController adminProgram = new AdminProgramController();
            if (adminProgram.getProgramParent().getId() != null) {
                program = adminProgram.getProgramParent();
            }

            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.PROGRAM_KEY, program.getId());
            request1.setParams(params);
            projectAnnualVolumeList = programEJB.getProjectAnnualVolumeByProgram(request1);
            for (ProjectAnnualVolume p : projectAnnualVolumeList) {
                if (eventType == 1) {
                    if (p.getYear() == Integer.parseInt(cmbYear.getSelectedItem().getValue().toString())) {
                        indRegisterExist = 1;
                        this.showMessage("sp.common.yearRegisterBD", false, null);
                    }
                }
            }
        } catch (NullParameterException ex) {
            showError(ex);
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } finally {
            if (indRegisterExist != 1) {
                CreateProjectAnnualVolume(program, projectAnnualVolume);
                projectAnnualVolume = programEJB.saveProjectAnnualVolume(projectAnnualVolume);
                this.showMessage("sp.common.save.success", false, null);
                EventQueues.lookup("updateProjectAnnualVolume", EventQueues.APPLICATION, true).publish(new Event(""));
                btnSave.setVisible(false);
            }
        }
    }

    public ProjectAnnualVolume CreateProjectAnnualVolume(Program program, ProjectAnnualVolume projectAnnualVolume) {
        projectAnnualVolume.setProgramId(program);
        projectAnnualVolume.setYear(Integer.parseInt(cmbYear.getSelectedItem().getValue().toString()));
        projectAnnualVolume.setAccountsNumber(intAccountsNumber.intValue());
        projectAnnualVolume.setActiveCardNumber(intActiveCardNumber.intValue());
        projectAnnualVolume.setAverageLoad(dbxAverageLoad.getValue().floatValue());
        projectAnnualVolume.setAverageCardBalance(dbxAverageCardBalance.getValue().floatValue());
        return projectAnnualVolume;
    }

    public void onClick$btnSave() throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (validateEmpty() && validateProjectAnnualVolume()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveProjectAnnualVolume(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveProjectAnnualVolume(projectAnnualVolumeParam);
                    break;
                default:
                    break;
            }
        }
    }

    
    public void onClick$btnBack() {
        winProjectAnnualVolume.detach();
    }

    
    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(projectAnnualVolumeParam);
                loadCmbYear(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(projectAnnualVolumeParam);
                loadCmbYear(eventType);
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                loadCmbYear(eventType);
                break;
            default:
                break;
        }
    }

    private void loadCmbYear(Integer evenInteger) {
        ArrayList<Integer> yearProjection = new ArrayList<Integer>();
        for (int i = 1; i < 6; i++) {
            yearProjection.add(Integer.valueOf(i));
        }
        try {
            Comboitem item = new Comboitem();
            for (Integer y : yearProjection) {
                item.setValue(y);
                item.setLabel("Year " + y);
                item.setParent(cmbYear);
                if (eventType != 1) {
                    if (y == Integer.valueOf(projectAnnualVolumeParam.getYear())) {
                        cmbYear.setSelectedItem(item);
                    }
                }
                item = new Comboitem();
            }
        } catch (Exception ex) {
            showError(ex);
            ex.printStackTrace();
        }
    }
}
