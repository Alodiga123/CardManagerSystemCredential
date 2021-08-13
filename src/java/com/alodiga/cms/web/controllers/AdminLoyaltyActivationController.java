package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProgramEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.ProgramLoyalty;
import com.cms.commons.models.StatusProgramLoyalty;
import com.cms.commons.models.User;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;

public class AdminLoyaltyActivationController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;

    private Label lblLoyaltyProgram;
    private Label lblProducto;
    private Label lblLoyaltyMain;
    private Label lblProgramLoyaltyType;
    private Label lblCity;
    private Label lblAgency;
    private Label lblCommercialAssessorUserCode;
    private Label lblAssessorName;
    private Label lblIdentification;
    private Textbox txtObservations;
    private Radio rActivationYes;
    private Radio rActivationNo;
    private ProgramEJB programEJB = null;
    private User user = null;
    private ProgramLoyalty programLoyaltyParam;
    private Button btnSave;
    private AdminLoyaltyController adminLoyalty = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        adminLoyalty = new AdminLoyaltyController();
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);

        if (adminLoyalty.getProgramLoyaltyParent() != null) {
            programLoyaltyParam = adminLoyalty.getProgramLoyaltyParent();
        } else {
            programLoyaltyParam = null;
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            user = (User) session.getAttribute(Constants.USER_OBJ_SESSION);
            programEJB = (ProgramEJB) EJBServiceLocator.getInstance().get(EjbConstants.PROGRAM_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        txtObservations.setRawValue(null);
    }

    private void loadUser() {
        lblCity.setValue(user.getComercialAgencyId().getCityId().getName());
        lblAgency.setValue(user.getComercialAgencyId().getName());
        lblCommercialAssessorUserCode.setValue(user.getCode());
        lblAssessorName.setValue(user.getFirstNames() + " " + user.getLastNames());
        lblIdentification.setValue(user.getIdentificationNumber());
    }

    private void loadFields(ProgramLoyalty programLoyalty) throws EmptyListException, GeneralException, NullParameterException {
        try {
            if (programLoyalty != null) {
                lblLoyaltyProgram.setValue(programLoyalty.getProgramId().getName());
                lblProducto.setValue(programLoyalty.getProductId().getName());
                lblLoyaltyMain.setValue(programLoyalty.getDescription());
                lblProgramLoyaltyType.setValue(programLoyalty.getProgramLoyaltyTypeId().getName());

                lblCity.setValue(programLoyalty.getUserActivationId().getComercialAgencyId().getCityId().getName());
                lblAgency.setValue(programLoyalty.getUserActivationId().getComercialAgencyId().getName());
                lblCommercialAssessorUserCode.setValue(programLoyalty.getUserActivationId().getCode());
                lblAssessorName.setValue(programLoyalty.getUserActivationId().getFirstNames() + " " + programLoyalty.getUserActivationId().getLastNames());
                lblIdentification.setValue(programLoyalty.getUserActivationId().getIdentificationNumber());

                if (programLoyalty.getObservations() != null) {
                    txtObservations.setText(programLoyalty.getActivationObservations());
                }
                if (programLoyalty.getIndActivation() != null) {
                    if (programLoyalty.getIndActivation() == true) {
                        rActivationYes.setChecked(true);
                    } else {
                        rActivationNo.setChecked(true);
                    }
                }
            } else {
                lblCity = null;
                lblAgency = null;
                lblCommercialAssessorUserCode = null;
                lblAssessorName = null;
            }
        } catch (Exception ex) {
            showError(ex);
        } finally {
            lblCity.setValue(user.getComercialAgencyId().getCityId().getName());
            lblAgency.setValue(user.getComercialAgencyId().getName());
            lblCommercialAssessorUserCode.setValue(user.getCode());
            lblAssessorName.setValue(user.getFirstNames() + " " + user.getLastNames());
        }
    }

    public void blockFields() {
        txtObservations.setReadonly(true);
        rActivationNo.setDisabled(true);
        rActivationNo.setDisabled(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtObservations.getText().isEmpty()) {
            txtObservations.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveActivationLoyaltyProgram(ProgramLoyalty _programLoyalty) {
        try {
            ProgramLoyalty programLoyalty = null;
            boolean indActivation;
            StatusProgramLoyalty statusProgramLoyalty = new StatusProgramLoyalty();
            EJBRequest request1 = new EJBRequest();

            if (_programLoyalty != null) {
                programLoyalty = _programLoyalty;
            } else {
                programLoyalty = new ProgramLoyalty();
            }

            if (rActivationYes.isChecked()) {
                indActivation = true;
                request1 = new EJBRequest();
                request1.setParam(WebConstants.STATUS_PROGRAM_LOYALTY_ACTIVE);
                statusProgramLoyalty = programEJB.loadStatusProgramLoyalty(request1);
            } else {
                indActivation = false;
                request1 = new EJBRequest();
                request1.setParam(WebConstants.STATUS_PROGRAM_LOYALTY_INACTIVE);
                statusProgramLoyalty = programEJB.loadStatusProgramLoyalty(request1);
            }
            
            //Guarda el registro para la activacion
            programLoyalty.setActivationDate(new Timestamp(new Date().getTime()));
            programLoyalty.setActivationObservations(txtObservations.getText());
            programLoyalty.setIndActivation(indActivation);
            programLoyalty.setUserActivationId(user);
            if (eventType == WebConstants.EVENT_ADD) {
                programLoyalty.setCreateDate(new Timestamp(new Date().getTime()));
            } else {
                programLoyalty.setUpdateDate(new Timestamp(new Date().getTime()));
            }
            programLoyalty.setStatusProgramLoyaltyId(statusProgramLoyalty);
            programLoyalty = programEJB.saveProgramLoyalty(programLoyalty);
            this.showMessage("cms.common.msj", false, null);

        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveActivationLoyaltyProgram(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveActivationLoyaltyProgram(programLoyaltyParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        try {
            switch (eventType) {
                case WebConstants.EVENT_EDIT:
                    if (programLoyaltyParam != null) {
                        loadFields(programLoyaltyParam);
                    } else {
                        loadUser();
                    }
                    break;
                case WebConstants.EVENT_VIEW:
                    if (programLoyaltyParam != null) {
                        loadFields(programLoyaltyParam);
                    } else {
                        loadUser();
                    }
                    blockFields();
                    break;
                case WebConstants.EVENT_ADD:
                    loadUser();
                    break;
            }
        } catch (EmptyListException ex) {
            Logger.getLogger(AdminLoyaltyActivationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GeneralException ex) {
            Logger.getLogger(AdminLoyaltyActivationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullParameterException ex) {
            Logger.getLogger(AdminLoyaltyActivationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
