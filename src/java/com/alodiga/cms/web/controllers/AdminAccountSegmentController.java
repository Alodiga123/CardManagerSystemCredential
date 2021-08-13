package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.AccountProperties;
import com.cms.commons.models.AccountSegment;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class AdminAccountSegmentController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Label lblAccountIdentifier;
    private Textbox txtAccountDescription;
    private Intbox txtLengthSegment;
    private CardEJB cardEJB = null;
    private AccountSegment accountSegmentParam;
    private AccountProperties accountProperties;
    private Button btnSave;
    public Window winAdminAccountSegment;
    private Integer eventType;
    Map param = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            accountSegmentParam = null;
        } else {
            accountSegmentParam = (AccountSegment) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void loadFields(AccountSegment accountSegment) {
        AdminAccountPropertiesController adminAccountProperties = new AdminAccountPropertiesController();
        if (adminAccountProperties.getAccountPropertiesParent().getId() != null) {
            accountProperties = adminAccountProperties.getAccountPropertiesParent();
        }
        try {
            lblAccountIdentifier.setValue(accountProperties.getIdentifier());
            txtAccountDescription.setValue(accountSegment.getDescription());
            txtLengthSegment.setValue(accountSegment.getLenghtSegment());
            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        lblAccountIdentifier.setValue(null);
        txtAccountDescription.setRawValue(null);
        txtLengthSegment.setRawValue(null);
    }

    public void blockFields() {
        txtAccountDescription.setReadonly(true);
        txtLengthSegment.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtAccountDescription.getText().isEmpty()) {
            txtAccountDescription.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtLengthSegment.getText().isEmpty()) {
            txtLengthSegment.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (Integer.parseInt(txtLengthSegment.getText()) >= (Integer.parseInt(accountProperties.getLenghtAccount().toString()))) {
            txtLengthSegment.setFocus(true);
            this.showMessage("cms.error.lengthSegment", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveAccountSegment(AccountSegment _accountSegment) {
        AccountSegment accountSegment = null;

        List<AccountSegment> accountSegmentList = null;

        try {
            if (_accountSegment != null) {
                accountSegment = _accountSegment;
            } else {
                accountSegment = new AccountSegment();
            }

            //Se obtiene el identificador cuenta
            AdminAccountPropertiesController adminAccountProperties = new AdminAccountPropertiesController();
            if (adminAccountProperties.getAccountPropertiesParent().getId() != null) {
                accountProperties = adminAccountProperties.getAccountPropertiesParent();
            }

            EJBRequest request = new EJBRequest();
            Map param = new HashMap();
            param.put(Constants.ACCOUNT_PROPERTIES_KEY, accountProperties.getId());
            request.setParam(param);

            accountSegmentList = cardEJB.getAccountSegmentByAccountProperties(request);
            if (accountSegmentList != null) {
                this.showMessage("cms.error.accountSegmentExist", false, null);
            }
        } catch (Exception ex) {
            showError(ex);
        } finally {
            try {
                if ((accountSegmentList == null) || (_accountSegment != null)) {
                //Guardar AccountSegment
                    if (eventType == 1) {
                        accountSegment = new AccountSegment();
                    }
                    accountSegment.setAccountPropertiesId(accountProperties);
                    accountSegment.setDescription(txtAccountDescription.getValue());
                    accountSegment.setLenghtSegment(txtLengthSegment.getValue());
                    accountSegment = cardEJB.saveAccountSegment(accountSegment);
                    accountSegmentParam = accountSegment;

                    this.showMessage("sp.common.save.success", false, null);
                    btnSave.setVisible(false);
                }
                EventQueues.lookup("updateAccountSegment", EventQueues.APPLICATION, true).publish(new Event(""));
            } catch (RegisterNotFoundException ex) {
                showError(ex);
            } catch (NullParameterException ex) {
                showError(ex);
            } catch (GeneralException ex) {
                showError(ex);
            }
        }
    }

    public void onClick$btnSave() throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveAccountSegment(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveAccountSegment(accountSegmentParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void onClick$btnBack() {
        winAdminAccountSegment.detach();
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(accountSegmentParam);
                txtAccountDescription.setDisabled(false);
                txtLengthSegment.setDisabled(false);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(accountSegmentParam);
                blockFields();
                txtAccountDescription.setDisabled(true);
                txtLengthSegment.setDisabled(true);
                break;
            case WebConstants.EVENT_ADD:
                loadFields(accountSegmentParam);
                break;
            default:
                break;
        }
    }

}
