package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.ProductEJB;
import com.alodiga.cms.commons.ejb.PersonEJB;
import com.alodiga.cms.commons.ejb.ProgramEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractAdminController;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Currency;
import com.cms.commons.models.Issuer;
import com.cms.commons.models.BinSponsor;
import com.cms.commons.models.CardIssuanceType;
import com.cms.commons.models.NaturalPerson;
import com.cms.commons.models.ProductType;
import com.cms.commons.models.Program;
import com.cms.commons.models.ProgramType;
import com.cms.commons.models.ResponsibleNetworkReporting;
import com.cms.commons.models.SourceFunds;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Textbox;
import com.alodiga.cms.web.utils.WebConstants;
import com.cms.commons.models.LegalPerson;
import com.cms.commons.models.Person;
import com.cms.commons.util.Constants;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Toolbarbutton;

public class AdminProgramController extends GenericAbstractAdminController {

    private static final long serialVersionUID = -9145887024839938515L;
    private Textbox txtName;
    private Textbox txtDescription;
    private Textbox txtBinIin;
    private Textbox txtOtherSourceOfFound;
    private Textbox txtOtheResponsibleNetwoork;
    private Textbox website;
    private Datebox dtbContractDate;
    private Datebox dtbExpectedLaunchDate;
    private Combobox cmbProgramType;
    private Combobox cmbProductType;
    private Combobox cmbBinSponsor;
    private Combobox cmbIssuer;
    private Combobox cmbProgramOwner;
    private Combobox cmbCardProgramManager;
    private Combobox cmbSourceOfFound;
    private Combobox cmbCurrency;
    private Combobox cmbResponsibleNetwoork;
    private Combobox cmbCardIssuanceType;
    private Radio rBrandedYes;
    private Radio rBrandedNo;
    private Radio rReloadableYes;
    private Radio rReloadableNo;
    private Radio rCashAccesYes;
    private Radio rCashAccesNo;
    private Radio rInternationalYes;
    private Radio rInternationalNo;
    private ProgramEJB programEJB = null;
    private PersonEJB personEJB = null;
    private UtilsEJB utilsEJB = null;
    private ProductEJB productEJB = null;
    private Program programParam;
    private Tab tabNetwork;
    private Tab tabProjectedAnnualVolume;
    private Tab tabAverageCargeUsage;
    private Button btnSave;
    private Button btnAddNetWork;
    private Integer eventType;
    private Toolbarbutton tbbTitle;
    public static Program programParent = null;
    private List<LegalPerson> legalPersonList = null;
    private List<NaturalPerson> naturalPersonList = null;
    NaturalPerson programOwnerNatural = null;
    LegalPerson programOwnerLegal = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        eventType = (Integer) Sessions.getCurrent().getAttribute(WebConstants.EVENTYPE);
        if (eventType == WebConstants.EVENT_ADD) {
            programParam = null;
        } else {
            programParam = (Program) Sessions.getCurrent().getAttribute("object");
        }
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.program.edit"));
                break;
            case WebConstants.EVENT_VIEW:
                tbbTitle.setLabel(Labels.getLabel("cms.crud.program.view"));
                break;
            case WebConstants.EVENT_ADD:
                tabNetwork.setDisabled(true);
                tabProjectedAnnualVolume.setDisabled(true);
                tabAverageCargeUsage.setDisabled(true);
                tbbTitle.setLabel(Labels.getLabel("cms.crud.program.add"));
                break;
            default:
                break;
        }
        try {
            programEJB = (ProgramEJB) EJBServiceLocator.getInstance().get(EjbConstants.PROGRAM_EJB);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
            personEJB = (PersonEJB) EJBServiceLocator.getInstance().get(EjbConstants.PERSON_EJB);
            loadData();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void clearFields() {
        txtName.setRawValue(null);
        txtDescription.setRawValue(null);
        dtbContractDate.setRawValue(null);
        txtOtherSourceOfFound.setRawValue(null);
        txtBinIin.setRawValue(null);
        txtOtheResponsibleNetwoork.setRawValue(null);
        dtbExpectedLaunchDate.setRawValue(null);
        website.setRawValue(null);
    }

    public Program getProgramParent() {
        return this.programParent;
    }

    private void loadFields(Program program) {
        try {
            txtName.setText(program.getName());
            txtDescription.setText(program.getDescription());
            dtbContractDate.setValue(program.getContractDate());
            dtbExpectedLaunchDate.setValue(program.getExpectedLaunchDate());
            txtOtheResponsibleNetwoork.setText(program.getOtherResponsibleNetworkReporting());
            txtOtherSourceOfFound.setText(program.getOtherSourceFunds());
            txtBinIin.setText(program.getBiniinNumber());
            
            if (program.getReloadable() == 1) {
                rReloadableYes.setChecked(true);
            } else {
                rReloadableNo.setChecked(true);
            }
            
            if (program.getSharedBrand() == 1) {
                rBrandedYes.setChecked(true);
            } else {
                rBrandedNo.setChecked(true);
            }
            
            if (program.getWebSite() != null) {
                website.setText(program.getWebSite());
            }
            
            if (program.getCashAccess() == 1) {
                rCashAccesYes.setChecked(true);
            } else {
                rCashAccesNo.setChecked(true);
            }            
            
            if (program.getUseInternational() == 1) {
                rInternationalYes.setChecked(true);
            } else {
                rInternationalNo.setChecked(true);
            }            

            if (eventType == WebConstants.EVENT_EDIT) {
                dtbContractDate.setDisabled(true);
                dtbExpectedLaunchDate.setDisabled(true);
            }
            btnSave.setVisible(true);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void blockFields() {
        txtName.setReadonly(true);
        txtDescription.setReadonly(true);
        dtbContractDate.setDisabled(true);
        txtOtherSourceOfFound.setReadonly(true);
        txtBinIin.setReadonly(true);
        txtOtheResponsibleNetwoork.setReadonly(true);
        dtbExpectedLaunchDate.setDisabled(true);
        rBrandedYes.setDisabled(true);
        rBrandedNo.setDisabled(true);
        rReloadableYes.setDisabled(true);
        rReloadableNo.setDisabled(true);
        rCashAccesYes.setDisabled(true);
        rCashAccesNo.setDisabled(true);
        rInternationalYes.setDisabled(true);
        rInternationalNo.setDisabled(true);
        website.setReadonly(true);
        btnSave.setVisible(false);
    }

    public Boolean validateEmpty() {
        if (txtName.getText().isEmpty()) {
            txtName.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (txtDescription.getText().isEmpty()) {
            txtDescription.setFocus(true);
            this.showMessage("sp.error.field.cannotNull", true, null);
        } else if (dtbContractDate.getText().isEmpty()) {
            dtbContractDate.setFocus(true);
            this.showMessage("cms.error.date.contrato", true, null);
        } else if (cmbProgramType.getSelectedItem() == null) {
            cmbProgramType.setFocus(true);
            this.showMessage("cms.error.programType.notSelected", true, null);
        } else if (cmbProductType.getSelectedItem() == null) {
            cmbProductType.setFocus(true);
            this.showMessage("cms.error.productoType.notSelected", true, null);
        } else if (cmbBinSponsor.getSelectedItem() == null) {
            cmbBinSponsor.setFocus(true);
            this.showMessage("cms.error.binSponsor.notSelected", true, null);
        } else if (cmbIssuer.getSelectedItem() == null) {
            cmbIssuer.setFocus(true);
            this.showMessage("cms.error.Issuer.notSelected", true, null);
        } else if (cmbProgramOwner.getSelectedItem() == null) {
            cmbProgramOwner.setFocus(true);
            this.showMessage("cms.error.programOwner.notSelected", true, null);
        } else if (cmbCardProgramManager.getSelectedItem() == null) {
            cmbCardProgramManager.setFocus(true);
            this.showMessage("cms.error.programManager.notSelected", true, null);
        } else if (cmbCardIssuanceType.getSelectedItem() == null) {
            cmbCardIssuanceType.setFocus(true);
            this.showMessage("cms.error.cardIssuanceType.notSelected", true, null);
        } else if ((!rBrandedYes.isChecked()) && (!rBrandedNo.isChecked())) {
            this.showMessage("cms.error.field.branded", true, null);
        } else if ((!rReloadableYes.isChecked()) && (!rReloadableNo.isChecked())) {
            this.showMessage("cms.error.field.reloadable", true, null);
        } else if (cmbSourceOfFound.getSelectedItem() == null) {
            cmbSourceOfFound.setFocus(true);
            this.showMessage("cms.error.sourceOfFound.notSelected", true, null);
        } else if ((!rCashAccesYes.isChecked()) && (!rCashAccesNo.isChecked())) {
            this.showMessage("cms.error.field.cashAcces", true, null);
        } else if ((!rInternationalYes.isChecked()) && (!rInternationalNo.isChecked())) {
            this.showMessage("cms.error.field.international", true, null);
        } else if (txtBinIin.getText().isEmpty()) {
            txtBinIin.setFocus(true);
            this.showMessage("cms.error.field.binIin", true, null);
        } else if (cmbCurrency.getSelectedItem() == null) {
            cmbCurrency.setFocus(true);
            this.showMessage("cms.error.currency.notSelected", true, null);
        } else if (cmbResponsibleNetwoork.getSelectedItem() == null) {
            cmbResponsibleNetwoork.setFocus(true);
            this.showMessage("cms.error.responsibleNetwoork.notSelected", true, null);
        } else if (dtbExpectedLaunchDate.getText().isEmpty()) {
            dtbExpectedLaunchDate.setFocus(true);
            this.showMessage("cms.error.date.expectedLaunchDate.empty", true, null);
        } else if (website.getText().isEmpty()) {
            website.setFocus(true);
            this.showMessage("cms.error.field.website", true, null);
        } else {
            return true;
        }
        return false;
    }
    
    public Boolean validateProgram() {
        Date today = new Date();
        if (eventType == WebConstants.EVENT_ADD) {
            if (today.compareTo(dtbContractDate.getValue()) < 0) {
                dtbContractDate.setFocus(true);
                this.showMessage("cms.error.contractDate.valid", true, null);
            } else if (today.compareTo(dtbExpectedLaunchDate.getValue()) > 0) {
                dtbExpectedLaunchDate.setFocus(true);
                this.showMessage("cms.error.date.expectedLaunchDate.valid", true, null);    
            } else {
                return true;
            }
        }        
        return false;
    }

    public void onChange$cmbSourceOfFound() {
        String sourceOfFoundsOther = WebConstants.PROGRAM_SOURCE_OF_FOUND_OTROS;
        String sourceOfFoundsOthers = WebConstants.PROGRAM_SOURCE_OF_FOUND_OTHER;

        String cadena = (((SourceFunds) cmbSourceOfFound.getSelectedItem().getValue()).getDescription());

        if ((cadena.equals(sourceOfFoundsOther)) || (cadena.equals(sourceOfFoundsOthers))) {
            txtOtherSourceOfFound.setDisabled(false);
        } else {
            txtOtherSourceOfFound.setDisabled(true);
        }
    }

    public void onChange$cmbResponsibleNetwoork() {
        String responsibleNetwoorkOther = WebConstants.PROGRAM_SOURCE_OF_FOUND_OTROS;
        String responsibleNetwoorkOthers = WebConstants.PROGRAM_SOURCE_OF_FOUND_OTHER;

        String cadena = (((ResponsibleNetworkReporting) cmbResponsibleNetwoork.getSelectedItem().getValue()).getDescription());

        if ((cadena.equals(responsibleNetwoorkOther)) || (cadena.equals(responsibleNetwoorkOthers))) {
            txtOtheResponsibleNetwoork.setDisabled(false);
        } else {
            txtOtheResponsibleNetwoork.setDisabled(true);
        }
    }

    private void saveProgram(Program _program) {
        short indBranded = 0;
        short indReloadable = 0;
        short indCashAcces = 0;
        short indInternational = 0;
        try {
            Program program = null;

            if (_program != null) {
                program = _program;
            } else {
                program = new Program();
            }

            if (rBrandedYes.isChecked()) {
                indBranded = 1;
            } else {
                indBranded = 0;
            }

            if (rReloadableYes.isChecked()) {
                indReloadable = 1;
            } else {
                indReloadable = 0;
            }

            if (rCashAccesYes.isChecked()) {
                indCashAcces = 1;
            } else {
                indCashAcces = 0;
            }

            if (rInternationalYes.isChecked()) {
                indInternational = 1;
            } else {
                indInternational = 0;
            }

            program.setName(txtName.getText());
            program.setDescription(txtDescription.getText());
            program.setContractDate(dtbContractDate.getValue());
            program.setProgramTypeId((ProgramType) cmbProgramType.getSelectedItem().getValue());
            program.setProductTypeId((ProductType) cmbProductType.getSelectedItem().getValue());
            program.setIssuerId((Issuer) cmbIssuer.getSelectedItem().getValue());
            program.setProgramOwnerId((Person) cmbProgramOwner.getSelectedItem().getValue());
            program.setCardProgramManagerId(((LegalPerson) cmbCardProgramManager.getSelectedItem().getValue()).getPersonId());
            program.setBinSponsorId((BinSponsor) cmbBinSponsor.getSelectedItem().getValue());
            program.setExpectedLaunchDate(dtbExpectedLaunchDate.getValue());
            program.setCardIssuanceTypeId((CardIssuanceType) cmbCardIssuanceType.getSelectedItem().getValue());
            program.setReloadable(indReloadable);
            program.setSourceFundsId((SourceFunds) cmbSourceOfFound.getSelectedItem().getValue());
            if (!txtOtherSourceOfFound.getText().equals("")) {
                program.setOtherSourceFunds(txtOtherSourceOfFound.getText());
            }
            program.setSharedBrand(indBranded);
            program.setWebSite(website.getText());
            program.setCashAccess(indCashAcces);
            program.setBiniinNumber(txtBinIin.getText());
            program.setCurrencyId((Currency) cmbCurrency.getSelectedItem().getValue());
            program.setUseInternational(indInternational);
            if (eventType == WebConstants.EVENT_ADD) {
                program.setCreateDate(new Timestamp(new Date().getTime()));
            } else {
                program.setUpdateDate(new Timestamp(new Date().getTime()));
            }
            program.setResponsibleNetworkReportingId((ResponsibleNetworkReporting) cmbResponsibleNetwoork.getSelectedItem().getValue());
            if (!txtOtheResponsibleNetwoork.getText().equals("")) {
                program.setOtherResponsibleNetworkReporting(txtOtheResponsibleNetwoork.getText());
            }
            program = programEJB.saveProgram(program);
            programParent = program;
            tabNetwork.setDisabled(false);
            tabProjectedAnnualVolume.setDisabled(false);
            tabAverageCargeUsage.setDisabled(false);

            this.showMessage("sp.common.save.success", false, null);
            btnSave.setVisible(false);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnSave() {
        if (validateEmpty()) {
            switch (eventType) {
                case WebConstants.EVENT_ADD:
                    if (validateProgram()) {
                        saveProgram(null);
                    }
                    break;
                case WebConstants.EVENT_EDIT:
                    saveProgram(programParam);
                    break;
                default:
                    break;
            }
        }
    }

    public void loadData() {
        switch (eventType) {
            case WebConstants.EVENT_EDIT:
                programParent = programParam;
                txtOtherSourceOfFound.setDisabled(true);
                txtOtheResponsibleNetwoork.setDisabled(true);
                loadFields(programParam);
                loadCmbCurrency(eventType);
                loadCmbProductType(eventType);
                loadCmbProgramType(eventType);
                loadCmbIssuer(eventType);
                loadCmbProgramOwner(eventType);
                loadCmbCardProgramManager(eventType);
                loadCmbBinSponsor(eventType);
                loadCmbSourceOfFound(eventType);
                loadCmbresponsibleNetwoork(eventType);
                loadCmbcardIssuanceType(eventType);
                break;
            case WebConstants.EVENT_VIEW:
                programParent = programParam;
                loadFields(programParam);
                blockFields();
                loadCmbCurrency(eventType);
                loadCmbProgramType(eventType);
                loadCmbProductType(eventType);
                loadCmbIssuer(eventType);
                loadCmbProgramOwner(eventType);
                loadCmbCardProgramManager(eventType);
                loadCmbBinSponsor(eventType);
                loadCmbSourceOfFound(eventType);
                loadCmbresponsibleNetwoork(eventType);
                loadCmbcardIssuanceType(eventType);
                break;
            case WebConstants.EVENT_ADD:
                loadCmbCurrency(eventType);
                loadCmbProgramType(eventType);
                loadCmbProductType(eventType);
                loadCmbIssuer(eventType);
                loadCmbProgramOwner(eventType);
                loadCmbCardProgramManager(eventType);
                loadCmbBinSponsor(eventType);
                loadCmbSourceOfFound(eventType);
                loadCmbresponsibleNetwoork(eventType);
                loadCmbcardIssuanceType(eventType);
            default:
                break;
        }
    }

    private void loadCmbIssuer(Integer evenInteger) {
        cmbIssuer.getItems().clear();
        EJBRequest request1 = new EJBRequest();
        List<Issuer> issuers;
        try {
            issuers = utilsEJB.getIssuers(request1);
            loadGenericCombobox(issuers, cmbIssuer, "name", evenInteger, Long.valueOf(programParam != null ? programParam.getIssuerId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }

    private void loadCmbBinSponsor(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<BinSponsor> binSponsors;
        try {
            binSponsors = utilsEJB.getBinSponsor(request1);
            loadGenericCombobox(binSponsors, cmbBinSponsor, "description", evenInteger, Long.valueOf(programParam != null ? programParam.getBinSponsorId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }

    private void loadCmbSourceOfFound(Integer evenInteger) {
        //CmbSourceOfFound
        EJBRequest request1 = new EJBRequest();
        List<SourceFunds> sourceFundses;
        try {
            sourceFundses = utilsEJB.getSourceFunds(request1);
            loadGenericCombobox(sourceFundses, cmbSourceOfFound, "description", evenInteger, Long.valueOf(programParam != null ? programParam.getSourceFundsId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }

    private void loadCmbresponsibleNetwoork(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<ResponsibleNetworkReporting> responsibleNetworkReportings;
        try {
            responsibleNetworkReportings = utilsEJB.getResponsibleNetworkReportings(request1);
            loadGenericCombobox(responsibleNetworkReportings, cmbResponsibleNetwoork, "description", evenInteger, Long.valueOf(programParam != null ? programParam.getResponsibleNetworkReportingId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }

    private void loadCmbcardIssuanceType(Integer evenInteger) {
        //cmbCardIssuanceType
        EJBRequest request1 = new EJBRequest();
        List<CardIssuanceType> cardIssuanceTypes;
        try {
            cardIssuanceTypes = utilsEJB.getCardIssuanceTypes(request1);
            loadGenericCombobox(cardIssuanceTypes, cmbCardIssuanceType, "description", evenInteger, Long.valueOf(programParam != null ? programParam.getCardIssuanceTypeId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }

    }

    private void loadCmbCurrency(Integer evenInteger) {
        //cmbCurrency
        EJBRequest request1 = new EJBRequest();
        List<Currency> currencies;
        try {
            currencies = utilsEJB.getCurrency(request1);
            loadGenericCombobox(currencies, cmbCurrency, "name", evenInteger, Long.valueOf(programParam != null ? programParam.getCurrencyId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }

    private void loadCmbProgramType(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<ProgramType> programType;
        try {
            programType = utilsEJB.getProgramType(request1);
            loadGenericCombobox(programType, cmbProgramType, "name", evenInteger, Long.valueOf(programParam != null ? programParam.getProgramTypeId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }

    private void loadCmbProductType(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<ProductType> productType;
        try {
            productType = utilsEJB.getProductTypes(request1);
            loadGenericCombobox(productType, cmbProductType, "name", evenInteger, Long.valueOf(programParam != null ? programParam.getProductTypeId().getId() : 0));
        } catch (EmptyListException ex) {
            showError(ex);
        } catch (GeneralException ex) {
            showError(ex);
        } catch (NullParameterException ex) {
            showError(ex);
        }
    }

    private void loadCmbProgramOwner(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<Person> personsProgramOwner;
        try {
            Map params = new HashMap();
            params.put(Constants.PERSON_CLASSIFICATION_KEY, Constants.PERSON_CLASSIFICATION_PROGRAM_OWNER);
            request1.setParams(params);
            personsProgramOwner = personEJB.getPersonByClassification(request1);
            for (Person p: personsProgramOwner) {                
                if (p.getPersonTypeId().getIndNaturalPerson() == true) {
                    //Obtiene el Gerente del Programa (persona natural)
                    request1 = new EJBRequest();
                    params = new HashMap(); 
                    params.put(Constants.PERSON_KEY, p.getId());
                    request1.setParams(params);
                    naturalPersonList = personEJB.getNaturalPersonByPerson(request1);
                    for (NaturalPerson n : naturalPersonList) {
                        programOwnerNatural = n;
                    }
                    //Actualiza la lista de gerentes de programas
                    p.setNaturalPerson(programOwnerNatural);
                } else {
                    //Obtiene el Gerente del Programa (persona jurídica)
                    request1 = new EJBRequest();
                    params = new HashMap(); 
                    params.put(Constants.PERSON_KEY, p.getId());
                    request1.setParams(params);
                    legalPersonList = personEJB.getLegalPersonByPerson(request1);
                    for (LegalPerson n : legalPersonList) {
                        programOwnerLegal = n;
                    }
                    //Actualiza la lista de gerentes de programas
                    p.setLegalPerson(programOwnerLegal);
                }           
            } 
            cmbProgramOwner.getItems().clear();
            for (Person c : personsProgramOwner) {
                StringBuilder nameProgramOwner = new StringBuilder();
                Comboitem item = new Comboitem();
                item.setValue(c);
                if (c.getNaturalPerson() != null) {
                    nameProgramOwner.append(c.getNaturalPerson().getFirstNames());
                    nameProgramOwner.append(" ");
                    nameProgramOwner.append(c.getNaturalPerson().getLastNames());
                }
                if (c.getLegalPerson() != null) {
                    nameProgramOwner.append(c.getLegalPerson().getEnterpriseName());
                }                
                item.setLabel(nameProgramOwner.toString());
                item.setParent(cmbProgramOwner);
                if (programParam != null && c.getId().equals(programParam.getProgramOwnerId().getId())) {
                    cmbProgramOwner.setSelectedItem(item);
                }
            }
            if (evenInteger.equals(WebConstants.EVENT_VIEW)) {
                cmbProgramOwner.setDisabled(true);
            }
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

    private void loadCmbCardProgramManager(Integer evenInteger) {
        EJBRequest request1 = new EJBRequest();
        List<LegalPerson> legalPersons;
        try {
            legalPersons = (List<LegalPerson>) programEJB.getCardManagementProgram(request1);
            cmbCardProgramManager.getItems().clear();
            for (LegalPerson c : legalPersons) {
                Comboitem item = new Comboitem();
                item.setValue(c);
                StringBuilder nameCardProgramManager = new StringBuilder(c.getEnterpriseName());
                item.setLabel(nameCardProgramManager.toString());
                item.setParent(cmbCardProgramManager);
                if (programParam != null && c.getId().equals(programParam.getCardProgramManagerId().getLegalPerson().getId())) {
                    cmbCardProgramManager.setSelectedItem(item);
                }
            }
            if (evenInteger.equals(WebConstants.EVENT_VIEW)) {
                cmbCardProgramManager.setDisabled(true);
            }
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
