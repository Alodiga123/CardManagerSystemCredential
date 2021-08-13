package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Card;
import com.cms.commons.models.NewCardIssueRequest;
import com.cms.commons.models.PhonePerson;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;

public class AdminCardCanceledControllers extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label lblCardNumber;
    private Label lblProgram;
    private Label lblProduct;
    private Label lblDate;
    private Label lblStatus;
    private Label lblIdentificationCardHolder;
    private Label lblNamesCardHolder;
    private Label lblEmail;
    private Label lblPhone;
    private CardEJB cardEJB = null;
    private PersonEJB personEJB = null;
    private Card cardCanceledParam;
    private List<NewCardIssueRequest> newCardIssueRequestList = null;
    private List<PhonePerson> phonePersonList = null;
    private Button btnSave;
    private Integer eventType;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) (Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE));
        if (eventType == WebConstants.EVENT_ADD) {
            cardCanceledParam = null;
        } else {
            cardCanceledParam = (Card) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
        loadData();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            
            
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
    }

    private void loadFields(Card cardCanceled) {
        try {
            EJBRequest request = new EJBRequest();
            Map params = new HashMap();
            
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            lblCardNumber.setValue(cardCanceled.getCardNumber());
            lblProgram.setValue(cardCanceled.getProgramId().getName());
            lblProduct.setValue(cardCanceled.getProductId().getName());
            lblDate.setValue(simpleDateFormat.format(cardCanceled.getIssueDate()));
            lblStatus.setValue(cardCanceled.getCardStatusId().getDescription());

            lblNamesCardHolder.setValue(cardCanceled.getCardHolder());
            lblEmail.setValue(cardCanceled.getPersonCustomerId().getEmail());
            if (cardCanceled.getPersonCustomerId().getPersonTypeId().getIndNaturalPerson() == true) {
                lblIdentificationCardHolder.setValue(cardCanceled.getPersonCustomerId().getNaturalCustomer().getIdentificationNumber());
                if (personEJB.havePhonesByPerson(cardCanceled.getPersonCustomerId().getNaturalCustomer().getPersonId().getId()) > 0) {
                    params.put(Constants.PERSON_KEY, cardCanceled.getPersonCustomerId().getNaturalCustomer().getPersonId().getId());
                    request.setParams(params);
                    phonePersonList = personEJB.getPhoneByPerson(request);
                }                
            } else {
                lblIdentificationCardHolder.setValue(cardCanceled.getPersonCustomerId().getLegalCustomer().getIdentificationNumber());
                if (personEJB.havePhonesByPerson(cardCanceled.getPersonCustomerId().getLegalCustomer().getPersonId().getId()) > 0) {
                    params.put(Constants.PERSON_KEY, cardCanceled.getPersonCustomerId().getLegalCustomer().getPersonId().getId());
                    request.setParams(params);
                    phonePersonList = personEJB.getPhoneByPerson(request);
                }                
            }
            if (phonePersonList != null) {
                for (PhonePerson p : phonePersonList) {
                    lblPhone.setValue(p.getNumberPhone());
                }
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {

    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                loadFields(cardCanceledParam);
                break;
            case WebConstants.EVENT_VIEW:
                loadFields(cardCanceledParam);
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                break;
            default:
                break;
        }
    }
}
