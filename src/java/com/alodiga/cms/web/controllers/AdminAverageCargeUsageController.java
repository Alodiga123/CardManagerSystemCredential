package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProgramEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.AverageCargeUsage;
import com.cms.commons.models.Program;
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
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;

public class AdminAverageCargeUsageController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Combobox cmbYear;
    private Doublebox dbxAverageLoadMonth;
    private Doublebox dbxAverageSpendMonth;
    private ProgramEJB programEJB = null;
    private AverageCargeUsage averageCargeUsageParam;
    private Button btnSave;
    public Window winAverageCargeUsage;
    private Integer eventType;
    private AverageCargeUsage averageCargeUsage = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            averageCargeUsageParam = null;
        } else {
            averageCargeUsageParam = (AverageCargeUsage) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                averageCargeUsageParam = (AverageCargeUsage) Sessions.getCurrent().getAttribute("object");
                break;
            case WebConstants.EVENT_VIEW:
                averageCargeUsageParam = (AverageCargeUsage) Sessions.getCurrent().getAttribute("object");
                break;
            case WebConstants.EVENT_ADD:
                averageCargeUsageParam = null;
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
        dbxAverageLoadMonth.setRawValue(null);
        dbxAverageSpendMonth.setRawValue(null);
    }

    private void loadFields(AverageCargeUsage averageCargeUsage) {
        try {
            dbxAverageLoadMonth.setValue(averageCargeUsage.getAverageLoadMonth().floatValue());
            dbxAverageSpendMonth.setValue(averageCargeUsage.getAverageSpendMonth().floatValue());
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        cmbYear.setDisabled(true);
        dbxAverageLoadMonth.setReadonly(true);
        dbxAverageSpendMonth.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (cmbYear.getSelectedItem() == null) {
            cmbYear.setFocus(true);
            this.showMessage("cms.error.year.notSelected", true, null);
        } else if (dbxAverageLoadMonth.getText().isEmpty()) {
            dbxAverageLoadMonth.setFocus(true);
            this.showMessage("cms.error.field.averageLoad", true, null);
        } else if (dbxAverageSpendMonth.getText().isEmpty()) {
            dbxAverageSpendMonth.setFocus(true);
            this.showMessage("cms.error.field.averageSpendMonth", true, null);
        } else if(dbxAverageSpendMonth.getValue() > dbxAverageLoadMonth.getValue()){
            this.showMessage("cms.error.field.averageSpendMonthError", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveAverageCargeUsage(AverageCargeUsage _averageCargeUsage) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Program program = null;
        List<AverageCargeUsage> averageCargeUsageList = null;
        int indRegisterExist = 0;
        try {
            if (_averageCargeUsage != null) {
                averageCargeUsage = _averageCargeUsage;
            } else {//New address
                averageCargeUsage = new AverageCargeUsage();
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
            averageCargeUsageList = programEJB.getAverageCargeUsageByProgram(request1);
            for (AverageCargeUsage a : averageCargeUsageList) {
                if (eventType == 1) {
                    if (a.getYear() == Integer.parseInt(cmbYear.getSelectedItem().getValue().toString())) {
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
                CreateAverageCargeUsage(program, averageCargeUsage);
                averageCargeUsage = programEJB.saveAverageCargeUsage(averageCargeUsage);
                this.showMessage("sp.common.save.success", false, null);
                EventQueues.lookup("updateAverageCargeUsage", EventQueues.APPLICATION, true).publish(new Event(""));
            }
        }
    }

    public AverageCargeUsage CreateAverageCargeUsage(Program program, AverageCargeUsage averageCargeUsage) {
        averageCargeUsage.setProgramId(program);
        averageCargeUsage.setYear(Integer.parseInt(cmbYear.getSelectedItem().getValue().toString()));
        averageCargeUsage.setAverageLoadMonth(dbxAverageLoadMonth.getValue().floatValue());
        averageCargeUsage.setAverageSpendMonth(dbxAverageSpendMonth.getValue().floatValue());
        return averageCargeUsage;
    }

    public void onClick$btnSave() throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveAverageCargeUsage(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveAverageCargeUsage(averageCargeUsageParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void onClick$btnBack() {
        winAverageCargeUsage.detach();
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(averageCargeUsageParam);
                loadCmbYear(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(averageCargeUsageParam);
                blockFields();
                loadCmbYear(eventType);
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
                    if (y == Integer.valueOf(averageCargeUsageParam.getYear())) {
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
