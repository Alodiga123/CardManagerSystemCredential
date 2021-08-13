package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.ProgramEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.InvalidQuestionException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Card;
import com.cms.commons.models.CardStatus;
import com.cms.commons.models.LegalCustomer;
import com.cms.commons.models.NaturalCustomer;
import com.cms.commons.models.PhonePerson;
import com.cms.commons.models.SecurityQuestion;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;

public class AdminCardActivationController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Label txtCardHolder = null;
    private Label txtCardNumber = null;
    private Label txtQuestionOne = null;
    private Textbox rOneIdentificationNumber;
    private Textbox rTwoNumberPhone;
    private Datebox rThreeDateBirth;
    private Datebox rFourDateOfIssue;
    private Datebox rFiveExpirationDate;
    private Textbox rSixCVV;
    private Tab tabCardActivation;
    private UtilsEJB utilsEJB = null;
    private PersonEJB personEJB = null;
    private ProgramEJB programEJB = null;
    private CardEJB cardEJB = null;
    private Button btnActivation;
    private Integer eventType;
    private Card cardParam;
    public static Card cardActivation = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            cardParam = null;
        } else {
            cardParam = (Card) Sessions.getCurrent().getAttribute("object");
            if (cardParam.getCardStatusId().getId() != 7) {
                tabCardActivation.setDisabled(true);
            } else {
                tabCardActivation.setDisabled(false);
            }
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            programEJB = (ProgramEJB) EJBServiceLocator.getInstance().get(EjbConstants.PROGRAM_EJB);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public Card getCardActivation() {
        return this.cardActivation;
    }

    public Integer getEventType() {
        return this.eventType;
    }

    public void clearFields() {
    }

    private void loadFields(Card card) {
        try {
            txtCardHolder.setValue(card.getCardHolder());
            txtCardNumber.setValue(card.getCardNumber());

            cardActivation = card;
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loadQuestions(SecurityQuestion question) {
        try {
            txtQuestionOne.setValue(question.getSecurityQuestion());
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public Boolean validateEmpty() {
        if (rOneIdentificationNumber.getText().isEmpty()) {
            rOneIdentificationNumber.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (rTwoNumberPhone.getText().isEmpty()) {
            rTwoNumberPhone.setFocus(true);
            this.showMessage("cms.error.field.phoneNumber", true, null);
        } else if (rThreeDateBirth.getText().isEmpty()) {
            rThreeDateBirth.setFocus(true);
            this.showMessage("cms.error.field.txtBirthDay", true, null);
        } else if (rFourDateOfIssue.getText().isEmpty()) {
            rFourDateOfIssue.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (rFiveExpirationDate.getText().isEmpty()) {
            rFiveExpirationDate.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (rSixCVV.getText().isEmpty()) {
            rSixCVV.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else {
            return true;
        }
        return false;
    }

    public void blockFields() {
        rOneIdentificationNumber.setReadonly(true);
        rTwoNumberPhone.setReadonly(true);
        rThreeDateBirth.setReadonly(true);
        rFourDateOfIssue.setReadonly(true);
        rFiveExpirationDate.setReadonly(true);
        rSixCVV.setReadonly(true);

        btnActivation.setVisible(false);
    }

    private void ActivateCard(Card _card) {
        CardStatus cardStatus = null;
        try {
            Card card = null;

            if (_card != null) {
                card = _card;

            } else {//New collectionsRequest
                card = new Card();
            }
            
            //Estatus de la tarjeta Entregada
            EJBRequest request1 = new EJBRequest();
            request1.setParam(Constants.CARD_STATUS_ACTIVE);
            cardStatus = utilsEJB.loadCardStatus(request1);

            card.setCardStatusId(cardStatus);
            card = cardEJB.saveCard(card);

            this.showMessage("cms.common.save.success.activation", false, null);
            btnActivation.setVisible(false);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnActivation() throws InterruptedException, InvalidQuestionException {
        if (validateEmpty()) {
            try {
                Card card = new Card();
                PhonePerson phonePerson = new PhonePerson();
                card = cardEJB.validateQuestionCard(cardParam.getId(), rFiveExpirationDate.getValue(), rFourDateOfIssue.getValue(), rSixCVV.getText());
//                phonePerson = personEJB.validatePhoneQuestion(cardParam.getPersonCustomerId().getNaturalCustomer().getPersonId().getId(), rTwoNumberPhone.getText());

                if (cardParam.getPersonCustomerId().getPersonTypeId().getIndNaturalPerson() == true) {
                    NaturalCustomer naturalCustomer = new NaturalCustomer();
                    naturalCustomer = personEJB.validateQuestionNatural(cardParam.getPersonCustomerId().getNaturalCustomer().getPersonId().getId(), rOneIdentificationNumber.getText(), rThreeDateBirth.getValue());
                } else {
                    LegalCustomer legalCustomer = new LegalCustomer();
                    legalCustomer = personEJB.validateQuestionLegal(cardParam.getPersonCustomerId().getLegalCustomer().getPersonId().getId(), rOneIdentificationNumber.getText(), rThreeDateBirth.getValue());
                }
                ActivateCard(cardParam);
            } catch (RegisterNotFoundException ex) {
                this.showMessage("cms.crud.securityQuestions", true, null);
            } catch (NullParameterException ex) {
                this.showMessage("cms.crud.securityQuestions", true, null);
            } catch (GeneralException ex) {
                this.showMessage("cms.crud.securityQuestions", true, null);
            } catch (InvalidQuestionException ex) {
                this.showMessage("cms.crud.securityQuestions", true, null);
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                cardActivation = cardParam;
                loadFields(cardParam);
                break;
            case WebConstants.EVENT_VIEW:
                cardActivation = cardParam;
                loadFields(cardParam);
                blockFields();
                break;
            case WebConstants.EVENT_ADD:
                txtCardHolder.setVisible(true);
                break;
            default:
                break;
        }
    }
}
