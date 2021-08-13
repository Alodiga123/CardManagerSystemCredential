package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Card;
import com.cms.commons.models.CardStatus;
import com.cms.commons.models.PlastiCustomizingRequestHasCard;
import com.cms.commons.models.PlasticCustomizingRequest;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.text.SimpleDateFormat;
import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

public class AdminPlasticCardController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblRequestNumber;
    private Label lblRequestDate;
    private Label lblProgram;
    private Label lblPlasticManufacturer;
    private Label lblCardNumber;
    private Label lblExpirationDate;
    private Label lblCardHolder;
    private Combobox cmbCardStatus;
    private PlasticCustomizingRequest plastiCustomerParam;
    private Card cardParam;
    private PlastiCustomizingRequestHasCard plastiCustomizingRequestHasCardParam;
    private CardEJB cardEJB = null;
    private UtilsEJB utilsEJB = null;
    private Button btnSave;
    private Integer eventType;
    public Window winAdminPlasticCard;
    public static PlasticCustomizingRequest plasticCustomer = null;
    public int optionList = 0;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        optionList = (Integer) Sessions.getCurrent().getAttribute(WebConstants.OPTION_LIST);
        if (eventType == WebConstants.EVENT_ADD) {
            cardParam = null;
        } else {
            if (optionList == 1) {
                cardParam = (Card) Sessions.getCurrent().getAttribute("object");
            } else {
                plastiCustomizingRequestHasCardParam = (PlastiCustomizingRequestHasCard) Sessions.getCurrent().getAttribute("object");
                cardParam = plastiCustomizingRequestHasCardParam.getCardId();
            }
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {

    }

    public void onClick$btnBack() {
        winAdminPlasticCard.detach();
    }

    private void loadFields(Card card) {
        try {
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            AdminPlasticRequestController adminPlasticRequest = new AdminPlasticRequestController();
            if (adminPlasticRequest.getPlasticCustomizingRequest().getId() != null) {
                plastiCustomerParam = adminPlasticRequest.getPlasticCustomizingRequest();
            }

            lblRequestNumber.setValue(plastiCustomerParam.getRequestNumber());
            lblRequestDate.setValue(simpleDateFormat.format(plastiCustomerParam.getRequestDate()));
            lblProgram.setValue(plastiCustomerParam.getProgramId().getName());
            lblPlasticManufacturer.setValue(plastiCustomerParam.getPlasticManufacturerId().getName());

            lblCardNumber.setValue(card.getCardNumber());
            lblExpirationDate.setValue(card.getExpirationDate().toString());
            lblCardHolder.setValue(card.getCardHolder());
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (cmbCardStatus.getText().isEmpty()) {
            cmbCardStatus.setFocus(true);
            this.showMessage("cms.error.statusCard.notSelected", true, null);
        } else {
            return true;
        }
        return false;
    }

    private void saveCard(Card _card) {
        String numberRequest = "";
        try {
            Card card = null;

            if (_card != null) {
                card = _card;

            } else {//New collectionsRequest
                card = new Card();
            }

            card.setCardStatusId((CardStatus) cmbCardStatus.getSelectedItem().getValue());
            card = cardEJB.saveCard(card);
            this.showMessage("sp.common.save.success", false, null);

            EventQueues.lookup("updatePlasticCard", EventQueues.APPLICATION, true).publish(new Event(""));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    saveCard(null);
                    break;
                case WebConstants.EVENT_EDIT:
                    saveCard(cardParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(cardParam);
                loadCmbCardStatus(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(cardParam);
                loadCmbCardStatus(eventType);
                break;
            case WebConstants.EVENT_ADD:
                loadCmbCardStatus(eventType);
                break;
            default:
                break;
        }
    }

    private void loadCmbCardStatus(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<CardStatus> cardStatus;

        try {
            cardStatus = utilsEJB.getCardStatus(request1);
            loadGenericCombobox(cardStatus, cmbCardStatus, "description", evenInteger, Long.valueOf(cardParam != null ? cardParam.getCardStatusId().getId() : 0));
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

}
