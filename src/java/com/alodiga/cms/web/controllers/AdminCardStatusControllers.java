package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.CardStatus;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;

public class AdminCardStatusControllers extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtName;
    private UtilsEJB utilsEJB = null;
    private CardStatus  cardStatusParam;
    private Button btnSave;
    private Integer event;
    private Toolbarbutton tbbTitle;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        event = (Integer) Sessions.getCurrent().getAttribute("eventType");
        if (event == WebConstants.EVENT_ADD) {
           cardStatusParam = null;                    
       } else {
           cardStatusParam = (CardStatus) Sessions.getCurrent().getAttribute("object");            
       }
        initialize();
    }


    @Override
    public void initialize() {
        super.initialize(); 
        switch (event) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.status.card.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.status.card.view"));
                break;
            case WebConstants.EVENT_ADD:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.status.card.add"));
                break;    
        }
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        txtName.setRawValue(null);
    }

    private void loadFields(CardStatus cardStatus) {
        try {txtName.setText(cardStatus.getDescription());
        btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtName.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtName.getText().isEmpty()) {
            txtName.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
            return  false;
        }
        return true;
    }


    private void saveCardStatus(CardStatus cardStatus_) throws RegisterNotFoundException, NullParameterException, GeneralException {
        try {
            CardStatus cardStatus = null;

            if (cardStatus_ != null) {
                cardStatus = cardStatus_;
            } else {//New cardStatus
                cardStatus = new CardStatus();
            }
            cardStatus.setDescription(txtName.getText());
            cardStatus = utilsEJB.saveCardStatus(cardStatus);
            cardStatusParam = cardStatus;
            this.showMessage("sp.common.save.success", false, null);
            btnSave.setVisible(false);
        } catch (WrongValueException ex) {
            showError(ex);
        }
    }

    public void onClick$btnSave() throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (validateEmpty()) {
            switch (event) {
                case WebConstants.EVENT_ADD:
                    saveCardStatus(null);
                break;
                case WebConstants.EVENT_EDIT:
                   saveCardStatus(cardStatusParam);
                break;
            }
        }
    }

    public void loadData() {
        switch (event) {
            case WebConstants.EVENT_EDIT:
                loadFields(cardStatusParam);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(cardStatusParam);
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                break;
            default:
                break;
        }
    }
}
