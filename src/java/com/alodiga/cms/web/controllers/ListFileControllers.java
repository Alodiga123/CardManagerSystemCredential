package com.alodiga.cms.web.controllers;

import com.alodiga.cms.commons.ejb.CardEJB;
import com.alodiga.cms.commons.ejb.RequestEJB;
import com.alodiga.cms.commons.ejb.UtilsEJB;
import com.alodiga.cms.commons.exception.EmptyListException;
import com.alodiga.cms.commons.exception.GeneralException;
import com.alodiga.cms.commons.exception.NullParameterException;
import com.alodiga.cms.commons.exception.RegisterNotFoundException;
import com.alodiga.cms.web.generic.controllers.GenericAbstractListController;
import com.cms.commons.genericEJB.EJBRequest;
import com.cms.commons.models.Card;
import com.cms.commons.models.CardStatus;
import com.cms.commons.models.PlastiCustomizingRequestHasCard;
import com.cms.commons.models.PlasticCustomizingRequest;
import com.cms.commons.models.ResultPlasticCustomizingRequest;
import com.cms.commons.models.StatusResultPlasticCustomizing;
import com.cms.commons.util.Constants;
import com.cms.commons.util.EJBServiceLocator;
import com.cms.commons.util.EjbConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

public class ListFileControllers extends GenericAbstractListController<ResultPlasticCustomizingRequest> {

    private static final long serialVersionUID = -9145887024839938515L;
    private Listbox lbxRecords;
    private Label lblNameFile;
    private RequestEJB requestEJB = null;
    private CardEJB cardEJB = null;
    private UtilsEJB utilsEJB = null;
    private PlasticCustomizingRequest plastiCustomerParam;
    private static List<String[]> readList = null;
    private List<Card> cardList;
    private Button btnRead;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            requestEJB = (RequestEJB) EJBServiceLocator.getInstance().get(EjbConstants.REQUEST_EJB);
            cardEJB = (CardEJB) EJBServiceLocator.getInstance().get(EjbConstants.CARD_EJB);
            utilsEJB = (UtilsEJB) EJBServiceLocator.getInstance().get(EjbConstants.UTILS_EJB);
            loadField();

        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnRead() throws InterruptedException {
        leer();
        loadFile(readList);
    }

    private void loadField() {
        String nombreArchivo = "archivo_prueba.csv";

        AdminPlasticRequestController adminPlasticRequest = new AdminPlasticRequestController();
        if (adminPlasticRequest.getPlasticCustomizingRequest().getId() != null) {
            plastiCustomerParam = adminPlasticRequest.getPlasticCustomizingRequest();
        }

        lblNameFile.setValue(nombreArchivo);
    }

    public static void leer() {
        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;

        try {
            // Apertura del fichero y creacion de BufferedReader para poder hacer una lectura comoda
            archivo = new File("/opt/cms/files/archivo_prueba.csv");
            fr = new FileReader(archivo);
            br = new BufferedReader(fr);
            readList = new ArrayList<String[]>();

            // Lectura del fichero  
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");
                readList.add(datos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // cerramos el fichero, para asegurarnos que se cierra tanto si todo va bien como si falla
            try {
                if (null != fr) {
                    fr.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public void loadFile(List<String[]> archivo) {
        String statusResultFile = "";
        String statusDes = null;
        List<StatusResultPlasticCustomizing> statusResultList = null;
        StatusResultPlasticCustomizing statusResultPlasticCustomizing = null;
        try {
            lbxRecords.getItems().clear();
            Listitem item = null;
            if (archivo != null && !archivo.isEmpty()) {
                for (int i = 0; i < archivo.size(); i++) {
                    String[] linea = archivo.get(i);
                    item = new Listitem();
                    item.setValue(linea);

                    statusResultFile = linea[5].trim();
                    
                    EJBRequest request1 = new EJBRequest();
                    Map params = new HashMap();
                    params.put(Constants.STATUS_PLASTIC_CUSTOMIZING_KEY, statusResultFile);
                    request1.setParams(params);
                    statusResultList = requestEJB.getStatusByStatusPlasticCustomizing(request1);
                    for (StatusResultPlasticCustomizing s: statusResultList) {
                        statusResultPlasticCustomizing = s;
                    }

                    item.appendChild(new Listcell(linea[0]));
                    item.appendChild(new Listcell(linea[3]));
                    item.appendChild(new Listcell(linea[2]));
                    item.appendChild(new Listcell(statusResultPlasticCustomizing.getCardStatusId().getDescription()));
                    item.setParent(lbxRecords);
                }
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    public void onClick$btnUpdateStatusCard() throws InterruptedException {
        try {
            saveResult(readList);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void saveResult(List<String[]> archivo) throws ParseException, EmptyListException {
        ResultPlasticCustomizingRequest resultPlasticCustomizingRequest = null;
        List<StatusResultPlasticCustomizing> statusResultList = null;
        StatusResultPlasticCustomizing statusResultPlasticCustomizing = null;
        StatusResultPlasticCustomizing statusResultP = null;
        Card card = null;
        List<Card> cardList = new ArrayList<Card>();
 
        String statusResultFile = "";
        try {
            if (archivo != null && !archivo.isEmpty()) {
                for (int i = 0; i < archivo.size(); i++) {

                    String[] linea = archivo.get(i);
                    String pattern = "yyyy-MM-dd";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                    resultPlasticCustomizingRequest = new ResultPlasticCustomizingRequest();
                    statusResultFile = linea[5].trim();

                    EJBRequest request1 = new EJBRequest();
                    Map params = new HashMap();
                    params.put(Constants.STATUS_PLASTIC_CUSTOMIZING_KEY, statusResultFile);
                    request1.setParams(params);
                    statusResultList = requestEJB.getStatusByStatusPlasticCustomizing(request1);
                    for (StatusResultPlasticCustomizing s: statusResultList) {
                        statusResultPlasticCustomizing = s;
                    }

                    resultPlasticCustomizingRequest.setCardNumber(linea[0]);
                    resultPlasticCustomizingRequest.setCardHolder(linea[2]);
                    resultPlasticCustomizingRequest.setIdentificationNumberCardHolder(linea[1]);
                    resultPlasticCustomizingRequest.setProductTypeDescription(linea[4]);
                    resultPlasticCustomizingRequest.setExpirationCardDate(simpleDateFormat.parse(linea[3]));
                    resultPlasticCustomizingRequest.setStatusResult(linea[5]);
                    resultPlasticCustomizingRequest.setStatusResultPlasticCustomizingId(statusResultPlasticCustomizing);
                    resultPlasticCustomizingRequest.setPlasticCustomizingRequestId(plastiCustomerParam);

                    //Actualiza la Tarjeta
                    card = updateCard(resultPlasticCustomizingRequest, statusResultFile, statusResultPlasticCustomizing);
                    cardList.add(card);
                    //Guarda la lÍnea del archivo
                    resultPlasticCustomizingRequest = requestEJB.saveResultPlasticCustomizingRequest(resultPlasticCustomizingRequest);

                }
                this.showMessage("cms.msj.UpdateStatusCard", false, null);
                btnRead.setVisible(false);
                ListPlasticCardControllers ListPlasticCard = new ListPlasticCardControllers();
                PlasticCustomizingRequest plasticCustomizingRequest = ListPlasticCard.getPlasticCustomizingRequest();
                List<PlastiCustomizingRequestHasCard> listPlasticCard = ListPlasticCard.getDataPlastic(plasticCustomizingRequest);
                for (PlastiCustomizingRequestHasCard p : listPlasticCard) {
                    for (Card c : cardList) {
                        if (c != null) {
                            if (p.getCardId().getCardNumber().equalsIgnoreCase(c.getCardNumber())) {
                                p.setCardId(c);
                            }
                        }
                    }
                }
                ListPlasticCard.loadDataPlasticList(listPlasticCard);
            }
        } catch (GeneralException ex) {
            Logger.getLogger(ListCardAssigmentControllers.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullParameterException ex) {
            Logger.getLogger(ListCardAssigmentControllers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Card updateCard(ResultPlasticCustomizingRequest resultPlastic, String statusResultFile, StatusResultPlasticCustomizing statusResultPlasticCustomizing) {
        Card card = null;
        CardStatus statusCard = null;

        try {
            //Busca en BD la tarjeta para actualizar el estatus
            EJBRequest request1 = new EJBRequest();
            Map params = new HashMap();
            params.put(Constants.CARDNUMBER_KEY, resultPlastic.getCardNumber());
            request1.setParams(params);
            cardList = cardEJB.getCardByCardNumber(request1);

            //Obtiene el estatus de la tarjeta
            EJBRequest request2 = new EJBRequest();
            request2.setParam(statusResultPlasticCustomizing.getCardStatusId().getId());
            statusCard = utilsEJB.loadCardStatus(request2);

            for (Card r : cardList) {                
                //Actualiza el estatus de la tarjeta
                card = r;
                card.setCardStatusId(statusCard);
                card = cardEJB.saveCard(card);
            }
        } catch (Exception ex) {
            showError(ex);
        }
        return card;
    }

    @Override
    public List<ResultPlasticCustomizingRequest> getFilterList(String filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void startListener() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void getData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void loadDataList(List<ResultPlasticCustomizingRequest> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
