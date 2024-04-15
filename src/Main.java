import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import config.DatabaseConnection;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.NodeList;

public class Main {
    public static void main(String[] args)
    {

        String xPathExpression = "//Ticket/*"; // Expresión XPath para seleccionar todos los nodos dentro de <Ticket>
        Document documento = null;
        NodeList nodos = null;

        String separator = "";
        separator = System.getProperty("file.separator");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {

            String rutaJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            String rutaDirectorio = rutaJar.substring(0, rutaJar.lastIndexOf(File.separator) + 1);

            System.out.print("Transformando archivos XML...\n");
            System.out.print(".............................\n");

            System.out.print(rutaDirectorio);


            File directory = new File(rutaDirectorio);
            File[] archivos = directory.listFiles();
            StringBuilder contentTotal = new StringBuilder();

            if (archivos != null) {
                for (File archivo : archivos) {
                    if (archivo.isFile() && archivo.getName().toLowerCase().endsWith(".xml")) {

                        // Carga del documento XML
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        // documento = builder.parse(new File("C:"+separator+"1918.xml"));
                        documento = builder.parse(new File(rutaDirectorio + separator + archivo.getName())); // Ajusto la ruta al archivo XML según ubicación


                        // Preparación de XPath
                        XPath xpath = XPathFactory.newInstance().newXPath();

                        // Consulta
                        nodos = (NodeList) xpath.evaluate(xPathExpression, documento, XPathConstants.NODESET);


                        StringBuilder content = new StringBuilder();
                        StringBuilder contentMedia = new StringBuilder();
                        String ticketNumber = "";
                        String date = "";
                        String hour = "";
                        String codigoPLU = "";
                        String precio = "";
                        String quantity ="";
                        String totalPay ="";
                        String descuento = "";
                        String lastCodigoPLU = "";
                        String rut = "";
                        String cashierName = "";
                        String cashierId = "";
                        String storeNumber ="";
                        String posNumber = "";
                        String numTender = "";
                        String tenderAmount = "";
                        String productName ="";
                        String firtsCashierName ="";
                        String change ="";
                        String totalQuantity = "";
                        String changeOption = "";
                        String pesable ="";
                        String pesableInfo ="";
                        String quantityInfo ="";
                        String codigoPLUInfo = "";
                        String nullabled = "";
                        String realDiscount = "";
                        List<String> pluCodes = new ArrayList<>();
                        List<String> nullabledCodesPlu = new ArrayList<>();
                        List<String> duplicates = new ArrayList<>();
                        List<String> pesables = new ArrayList<>();
                        List<String> pluSaved = new ArrayList<>();
                        int totalPrice = 0;
                        int countDuplicates = 0;
                        int totalDiscount = 0;
                        int countDiscount = 0;
                        int finalPrice = 0;
                        int realChangeNumber = 0;
                        int pluQuantity = 0 ;
                        int pluPrice = 0;
                        int countNullabe = 0;
                        int totalPrices = 0;
                        int countTotalDiscount = 0;
                        double quantityConverted = 0.0;
                        String changeNumber ="";
                        String  canjePoints = "";
                        for (int i = 0; i < nodos.getLength(); i++) {
                            Node nodo = nodos.item(i);
                            if (nodo.getNodeType() == Node.ELEMENT_NODE) {

                                //Obtengo el Ticket,Fecha y Hora
                                if (nodo.getNodeName().equals("Frame")) {
                                    NamedNodeMap atributos = nodo.getAttributes();
                                    ticketNumber = getAttribute(atributos, "TicketNumber");
                                    //Campo Fecha ajustado
                                    date = convertDateWhitOutBars(getAttribute(atributos, "Tail_Fecha"));
                                    hour = getAttribute(atributos, "Tail_Hora");
                                    cashierId = getAttribute(atributos, "Tail_NumCajero");
                                    storeNumber = getAttribute(atributos, "StoreNumber");
                                    posNumber = getAttribute(atributos, "Tail_NumPOS");
                                }

                                if (nodo.getNodeName().equals("Discount")) {
                                    countTotalDiscount++;
                                    NamedNodeMap atributos = nodo.getAttributes();
                                    if(countTotalDiscount == 2){
                                        realDiscount = getAttribute(atributos, "MontoDescuento");
                                    }
                                }


                                if (nodo.getNodeName().equals("PLU")) {
                                    NamedNodeMap attributes = nodo.getAttributes();
                                    codigoPLUInfo = getAttribute(attributes,"CodigoPLU");
                                    pesableInfo = getAttribute(attributes, "Pesable");
                                    quantityInfo = getAttribute(attributes, "Cantidad");

                                    if (pesableInfo.equals("1")){
                                        String codigoPesable = codigoPLUInfo+"-"+quantityInfo;
                                        pesables.add(codigoPesable);
                                    }
                                }

                                //Obtengo el valor para validar si es canje de puntos
                                if (nodo.getNodeName().equals("CMR_RDMPT")) {
                                    NamedNodeMap rutAttributes = nodo.getAttributes();
                                    canjePoints = getAttribute(rutAttributes, "Canje_Points");
                                }

                                if (nodo.getNodeName().equals("InfoDocData")) {
                                    NamedNodeMap rutAttributes = nodo.getAttributes();
                                    rut = getAttribute(rutAttributes, "DocNumber");
                                }

                                if (nodo.getNodeName().equals("InfoEmployeeID")) {
                                    NamedNodeMap attributes = nodo.getAttributes();
                                    cashierName = getAttribute(attributes, "CashierName");
                                }

                                if (nodo.getNodeName().equals("Media")) {
                                    NamedNodeMap attributes = nodo.getAttributes();
                                    numTender = calculateTextNumTender(getAttribute(attributes, "NumTender"));
                                    tenderAmount = getAttribute(attributes, "MontoTender");
                                    changeOption = getAttribute(attributes, "Change");
                                    //Validamos que siempre ingrese el valor del canje en 0 para que no tome el valor negativo del valor a regresar en efectivo
                                    if (Integer.parseInt(changeOption) == 0) {
                                        contentMedia.append(getStaticLine(nodos)).append(" PAYMENT         ").append(numTender).append(",").append(tenderAmount).append(",,,").append("\n");
                                    }
                                }

                                if (nodo.getNodeName().equals("Total")) {
                                    NamedNodeMap attributes = nodo.getAttributes();
                                    totalPay = getAttribute(attributes, "MontoTicket");
                                    totalQuantity = getAttribute(attributes, "NumItems");
                                }

                                firtsCashierName = convertCashier(cashierName);


                                if (i == 1) {

                                    content = new StringBuilder(getStaticLine(nodos) + " CLOSED          TRUE" + "\n" +
                                            getStaticLine(nodos) + " DOCUMENT_TYPE   TICKET" + "\n" +
                                            getStaticLine(nodos) + " TRX_NUMBER      " + ticketNumber + "\n" +
                                            getStaticLine(nodos) + " EMPLOYEE        cashierText" + " ," + "\n" +
                                            getStaticLine(nodos) + " DOB             " + date + "\n" +
                                            getStaticLine(nodos) + " DATE            " + date + "\n" +
                                            getStaticLine(nodos) + " TIME            " + hour + "\n" +
                                            getStaticLine(nodos) + " HEADER          DEFAULT HEADER" + "\n" +
                                            getStaticLine(nodos) + " FOOTER          DEFAULT FOOTER" + "\n");

                                    pluCodes = resolveString(nodos, "InfoSPF");
                                    duplicates = findDuplicateInStream(pluCodes);
                                }

                                //Valido si es canje
                                if (!canjePoints.isEmpty()) {
                                    if (nodo.getNodeName().equals("PLU")) {
                                        NamedNodeMap atributos = nodo.getAttributes();
                                        for (int j = 0; j < atributos.getLength(); j++) {
                                            Node atributo = atributos.item(j);

                                            if (atributo.getNodeName().equals("CodigoPLU")) {
                                                codigoPLU = atributo.getNodeValue();
                                                productName = DatabaseConnection.getProduct(codigoPLU, rutaDirectorio);

                                            }
                                            if (atributo.getNodeName().equals("Precio")) {
                                                precio = atributo.getNodeValue();
                                            }
                                            //no aplica
                                            if (atributo.getNodeName().equals("MontoDesc")) {
                                                descuento = atributo.getNodeValue();
                                            }
                                        }


                                        finalPrice = finalPrice + Integer.parseInt(precio);


                                        if (!codigoPLU.isEmpty() && !precio.isEmpty()) {
                                            if (descuento.isEmpty() || descuento.equals("0")) {
                                                descuento = "0";
                                            } else {
                                                countDiscount++;
                                            }


                                            if (!codigoPLU.equals(lastCodigoPLU)) {

                                                if (isDuplicate(codigoPLU, duplicates)) {
                                                    countDuplicates = getCountDuplicates(codigoPLU, pluCodes);
                                                    totalPrice = Integer.parseInt(precio);
                                                    content.append(getStaticLine(nodos)).append(" ITEM            ").append(codigoPLU.substring(1)).append(" ").append(productName).append(",").append(precio).append(",").append(totalPrice * countDuplicates).append(",").append(countDuplicates).append(",1,0,0,0,0,0,0,0,").append(descuento).append("\n");
                                                    totalPrices  = totalPrices + (totalPrice * countDuplicates);
                                                } else {
                                                    content.append(getStaticLine(nodos)).append(" ITEM            ").append(codigoPLU.substring(1)).append(" ").append(productName).append(",").append(precio).append(",").append(precio).append(",").append("1,1,0,0,0,0,0,0,0,").append(descuento).append("\n");
                                                    totalPrices  = totalPrices + Integer.parseInt(precio);

                                                }
                                            }
                                            lastCodigoPLU = codigoPLU;

                                        }
                                    }

                                    numTender = calculateTextNumTender("41");
                                    tenderAmount = canjePoints;

                                    pluCodes = resolveString(nodos, "PLU");

                                    //Valido primero si tiene la etiqueta INFOSB
                                } else if (nodo.getNodeName().equals("InfoSPF")) {

                                    NamedNodeMap rutAttributes = nodo.getAttributes();
                                    codigoPLU = getAttribute(rutAttributes, "CodigoPLU");
                                    productName = DatabaseConnection.getProduct(codigoPLU, rutaDirectorio);
                                    quantity = getAttribute(rutAttributes, "Cantidad");
                                    precio = getAttribute(rutAttributes, "Precio");
                                    descuento = getAttribute(rutAttributes, "MontoDesc");


                                    totalDiscount = totalDiscount + Integer.parseInt(descuento);
                                    finalPrice = finalPrice + Integer.parseInt(precio);

                                    String cadenaPesable = codigoPLU+"-"+quantity;
                                    String search = pesables.stream().filter(x-> x.equalsIgnoreCase(cadenaPesable)).findFirst().orElse("");
                                    if(!search.isEmpty()){
                                        double precioConverted = 0;
                                        int intPart = 0 ;
                                        quantityConverted = calculatePounds(Integer.parseInt(quantity));
                                        precioConverted = calculateRealPrice(quantityConverted,Integer.parseInt(precio));
                                        intPart = (int) precioConverted;
                                        content.append(getStaticLine(nodos)).append(" ITEM            ").append(codigoPLU.substring(1)).append(" ").append(productName).append(",").append(precio).append(",").append(intPart).append(",").append(quantityConverted).append(",1,0,0,0,0,0,0,0,").append(descuento).append("\n");

                                        totalPrices  = totalPrices + intPart;
                                    }else {

                                        if (!codigoPLU.isEmpty() && !precio.isEmpty()) {
                                            if (descuento.isEmpty() || descuento.equals("0")) {
                                                descuento = "0";
                                            } else {
                                                countDiscount++;
                                            }


                                            if (!codigoPLU.equals(lastCodigoPLU)) {

                                                String finalCodigoPLU = codigoPLU;
                                                String validDuplicatePLU = pluSaved.stream().filter(x-> x.equalsIgnoreCase(finalCodigoPLU)).findFirst().orElse("");

                                                if(validDuplicatePLU.isEmpty()){
                                                    if (isDuplicate(codigoPLU, duplicates)) {
                                                        countDuplicates = getCountDuplicates(codigoPLU, pluCodes);
                                                        totalPrice = Integer.parseInt(precio);
                                                        content.append(getStaticLine(nodos)).append(" ITEM            ").append(codigoPLU.substring(1)).append(" ").append(productName).append(",").append(precio).append(",").append(totalPrice * countDuplicates).append(",").append(countDuplicates).append(",1,0,0,0,0,0,0,0,").append(countDuplicates * Integer.parseInt(descuento)).append("\n");
                                                        pluSaved.add(codigoPLU);
                                                        totalPrices  = totalPrices + (totalPrice * countDuplicates);
                                                    } else {
                                                        content.append(getStaticLine(nodos)).append(" ITEM            ").append(codigoPLU.substring(1)).append(" ").append(productName).append(",").append(precio).append(",").append(Integer.parseInt(precio) * Integer.parseInt(quantity)).append(",").append(quantity).append(",1,0,0,0,0,0,0,0,").append(descuento).append("\n");
                                                        totalPrices  = totalPrices + (Integer.parseInt(precio) * Integer.parseInt(quantity));
                                                    }
                                                }
                                            }
                                            lastCodigoPLU = codigoPLU;



                                        }
                                    }
                                    //Valido si existe valores en INFOSB con esto aseguro que entrara solo en los PLU
                                } else if(!existsInfosb(nodos)){

                                    if (nodo.getNodeName().equals("PLU")) {
                                        pluCodes = resolveString(nodos, "PLU");
                                        duplicates = findDuplicateInStream(pluCodes);
                                        nullabledCodesPlu = getNullabledCodes(nodos, "PLU");


                                        countDiscount = 0;

                                        NamedNodeMap rutAttributes = nodo.getAttributes();
                                        codigoPLU = getAttribute(rutAttributes, "CodigoPLU");
                                        productName = DatabaseConnection.getProduct(codigoPLU, rutaDirectorio);
                                        quantity = getAttribute(rutAttributes, "Cantidad");
                                        precio = getAttribute(rutAttributes, "Precio");
                                        pesable = getAttribute(rutAttributes, "Pesable");
                                        nullabled = getAttribute(rutAttributes, "AnularItem");


                                        if (pesable.equals("1")){
                                            double precioConverted = 0;
                                            int intPart = 0 ;
                                            quantityConverted = calculatePounds(Integer.parseInt(quantity));
                                            precioConverted = calculateRealPrice(quantityConverted,Integer.parseInt(precio));
                                            intPart = (int) precioConverted;
                                            content.append(getStaticLine(nodos)).append(" ITEM            ").append(codigoPLU.substring(1)).append(" ").append(productName).append(",").append(precio).append(",").append(intPart).append(",").append(quantityConverted).append(",1,0,0,0,0,0,0,0,").append(descuento).append("\n");
                                            totalPrices  = totalPrices + intPart;
                                        }else {

                                            if (!codigoPLU.isEmpty() && !precio.isEmpty() && !quantity.isEmpty()) {
                                                if (descuento.isEmpty() || descuento.equals("0")) {
                                                    descuento = "0";
                                                } else {
                                                    countDiscount++;
                                                }


                                                if (!codigoPLU.equals(lastCodigoPLU)) {

                                                    String finalCodigoPLU = codigoPLU;
                                                    String validDuplicatePLU = pluSaved.stream().filter(x-> x.equalsIgnoreCase(finalCodigoPLU)).findFirst().orElse("");

                                                    //Validamos si existen coincidencias de la anulación
                                                    long countExistence = nullabledCodesPlu.stream()
                                                            .filter(codigo -> codigo.equals(finalCodigoPLU))
                                                            .count();

                                                    if(validDuplicatePLU.isEmpty()) {
                                                        if (isDuplicate(codigoPLU, duplicates)) {
                                                            countDuplicates = getCountDuplicates(codigoPLU, pluCodes);
                                                            if(countExistence > 0){
                                                                countDuplicates = countDuplicates - Math.toIntExact((countExistence * 2));
                                                            }
                                                            totalPrice = Integer.parseInt(precio);
                                                            content.append(getStaticLine(nodos)).append(" ITEM            ").append(codigoPLU.substring(1)).append(" ").append(productName).append(",").append(precio).append(",").append(totalPrice * countDuplicates).append(",").append(countDuplicates).append(",1,0,0,0,0,0,0,0,").append(descuento).append("\n");
                                                            precio = "";
                                                            pluSaved.add(codigoPLU);
                                                            totalPrices  = totalPrices + (totalPrice * countDuplicates);
                                                        } else {
                                                            totalPrice = Integer.parseInt(precio);
                                                            content.append(getStaticLine(nodos)).append(" ITEM            ").append(codigoPLU.substring(1)).append(" ").append(productName).append(",").append(precio).append(",").append(totalPrice * Integer.parseInt(quantity)).append(",").append(quantity).append(",1,0,0,0,0,0,0,0,").append(descuento).append("\n");
                                                            precio = "";
                                                            totalPrices  = totalPrices + (totalPrice * Integer.parseInt(quantity));
                                                        }
                                                    }

                                                }
                                                lastCodigoPLU = codigoPLU;

                                            }

                                        }
                                    }
                                }

                                //Esto es solo para los valores de CANJE
                                if (nodo.getNodeName().equals("Media")) {
                                    NamedNodeMap mediaAttributes = nodo.getAttributes();
                                    for (int j = 0; j < mediaAttributes.getLength(); j++) {
                                        Node mediaAttribute = mediaAttributes.item(j);

                                        if (mediaAttribute.getNodeName().equals("MontoTender")) {
                                            changeNumber = mediaAttribute.getNodeValue();
                                            realChangeNumber = Math.abs(Integer.parseInt(changeNumber));
                                        }
                                        if (mediaAttribute.getNodeName().equals("Change")) {
                                            change = mediaAttribute.getNodeValue();
                                        }
                                    }
                                }
                            }
                        }


                        //Validamos los CMR PUNTOS
                        if(!canjePoints.isEmpty()){
                            content.append(getStaticLine(nodos)).append(" PAYMENT         ").append(numTender).append(",").append(tenderAmount).append(",,,").append("\n");
                        }else{
                            content.append(contentMedia);
                        }


                        String textDate =calculateTextDate(date);

                        if(totalDiscount > 0){
                            content.append(getStaticLine(nodos)).append(" PROMO_AMOUNT    ").append(totalDiscount).append("\n");
                        }
                        content.append(getStaticLine(nodos)).append(" SUBTOTAL        ").append(totalPay).append("\n");
                        if(change.equals("1")){
                            content.append(getStaticLine(nodos)).append(" CHANGE          ").append(realChangeNumber).append("\n");
                        }
                        if(!rut.isEmpty()) {
                            content.append(getStaticLine(nodos)).append(" RUT_CLIENTE     ").append(rut).append("\n");
                        }
                        content.append(getStaticLine(nodos)).append(" TEXT_LINE").append("\n");
                        content.append(getStaticLine(nodos)).append(" TEXT_LINE          TOTAL NUM.ITEMS VENDIDOS =    ").append(totalQuantity).append("\n");

                        if(countDiscount > 0) {
                            content.append(getStaticLine(nodos)).append(" TEXT_LINE").append("\n");
                            content.append(getStaticLine(nodos)).append(" TEXT_LINE       USTED AHORRO HOY!").append("\n");
                            content.append(getStaticLine(nodos)).append(" TEXT_LINE       -------------------").append("\n");
                            content.append(getStaticLine(nodos)).append(" TEXT_LINE        TOTAL DESCUENTOS     ").append(countDiscount).append("            ").append(totalDiscount).append("\n");
                        }
                        content.append(getStaticLine(nodos)).append(" TEXT_LINE").append("\n");
                        content.append(getStaticLine(nodos)).append(" TEXT_LINE").append("\n");
                        content.append(getStaticLine(nodos)).append(" TEXT_LINE        NOMBRE DEL CAJERO:").append(cashierName).append("\n");
                        content.append(getStaticLine(nodos)).append(" TEXT_LINE        ").append("C").append(cashierId).append("    ").append("#").append(ticketNumber).append("    ").append(hour).append("    ").append(textDate).append("\n");
                        content.append(getStaticLine(nodos)).append(" TEXT_LINE                   ").append("T0").append(storeNumber).append("    ").append("R00").append(posNumber).append("\n");

                        //Reemplazo de la cabecera para canje de puntos
                        if(!canjePoints.isEmpty()){
                            int startIndexCanje = content.indexOf("TICKET");
                            int endIndexCanje = startIndexCanje + "TICKET".length();
                            content.replace(startIndexCanje, endIndexCanje, "TICKETPUNTO");
                        }

                        //Reemplazo del nombre del cajero en la cabecera
                        int startIndex = content.indexOf("cashierText");
                        int endIndex = startIndex + "cashierText".length();
                        content.replace(startIndex, endIndex, firtsCashierName);

                        writeFile(rutaDirectorio+separator, content.toString(), ticketNumber);

                        //Generamos el archivo de validación de totales

                        contentTotal.append("****************************").append("\n");
                        contentTotal.append("Numero de Ticket: ").append(ticketNumber).append("\n");
                        contentTotal.append("****************************").append("\n");
                        contentTotal.append("Total Pagado etiqueta: ").append(totalPay).append("\n");
                        contentTotal.append("Total Suma: ").append(totalPrices-totalDiscount).append("\n");
                        contentTotal.append("****************************").append("\n");
                        if(realDiscount.isEmpty()) {
                            realDiscount = "0";
                        }
                        contentTotal.append("Total Descuento Etiqueta: ").append(realDiscount).append("\n");
                        contentTotal.append("Total Suma Descuento: ").append(totalDiscount).append("\n");
                        contentTotal.append("****************************").append("\n");
                        contentTotal.append("****************************").append("\n");
                        contentTotal.append("****************************").append("\n");

                    }
                }
            }
            writeFile(rutaDirectorio+separator, contentTotal.toString(), "Validación_Totales");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private static boolean existsInfosb(NodeList nodos){
        boolean infoSB = false;
        for (int i = 0; i < nodos.getLength(); i++) {
            Node nodo = nodos.item(i);
            if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                if (nodo.getNodeName().equals("InfoSPF")) {
                    infoSB = true;
                }
            }
        }
        return infoSB;
    }

    private static  double calculateRealPrice(double quantityConverted, int precio){


        double numero = quantityConverted * precio;
        BigDecimal bigDecimal = new BigDecimal(numero);
        bigDecimal = bigDecimal.subtract(new BigDecimal(bigDecimal.intValue())); // Resta la parte entera
        bigDecimal = bigDecimal.multiply(new BigDecimal(10)); // Multiplica por 10 para obtener el primer decimal
        int primerDecimal = bigDecimal.intValue(); // Convierte a entero

        if(primerDecimal > 5){
            return Math.ceil(quantityConverted * precio);
        }else {
            return Math.round(quantityConverted * precio);
        }

    }

    private static double calculatePounds(int cantidad){
        double pounds = 0;
        pounds = (double) cantidad / 1000;
        return pounds;
    }

    private static String convertCashier(String cashierName){
        String[] partes = cashierName.split("\\s+");

        StringBuilder cashierNewName= new StringBuilder();

        for (String parte : partes) {
            cashierNewName.append(parte).append(",");
        }

        return cashierNewName.toString();
    }

    private static String calculateTextNumTender(String numTender){

        String descriptionTender = "";
        switch (numTender) {
            case "1":
                descriptionTender = "EFECTIVO";
                break;
            case "2":
                descriptionTender = "CHEQUE";
                break;
            case "3":
                descriptionTender = "Pago CMR Cheque";
                break;
            case "4":
                descriptionTender = "Tarjeta Evento";
                break;
            case "5":
                descriptionTender = "CMR";
                break;
            case "6":
                descriptionTender = "TARJETA CREDITO";
                break;
            case "7":
                descriptionTender = "DEBITO";
                break;
            case "8":
                descriptionTender = "CREDITO FALABELL";
                break;
            case "9":
                descriptionTender = "DEBITO FALABELLA";
                break;
            case "10":
                descriptionTender = "Nota de Credito";
                break;
            case "11":
                descriptionTender = "Ajuste Ley 20956";
                break;
            case "12":
                descriptionTender = "EdenRed";
                break;
            case "13":
                descriptionTender = "GIFTCORP";
                break;
            case "14":
                descriptionTender = "AMIPASS";
                break;
            case "15":
                descriptionTender = "Edenred Dif";
                break;
            case "17":
                descriptionTender = "Avance CMR";
                break;
            case "18":
                descriptionTender = "Pago CMR";
                break;
            case "19":
                descriptionTender = "QuickPayCredito";
                break;
            case "20":
                descriptionTender = "QuickPayDebito";
                break;
            case "21":
                descriptionTender = "GIFTCARD";
                break;
            case "22":
                descriptionTender = "GC ACTIVATION";
                break;
            case "23":
                descriptionTender = "GC DEACTIVATION";
                break;
            case "24":
                descriptionTender = "CREDITO FACTURA";
                break;
            case "25":
                descriptionTender = "FPAY";
                break;
            case "40":
                descriptionTender = "SODEXO";
                break;
            case "41":
                descriptionTender = "CMRPuntos";
                break;
            default:
                throw new IllegalArgumentException("Descripción Invalida: " + descriptionTender);
        }
        return descriptionTender;
    }

    private  static String calculateTextDate(String date){


        String year = date.substring(0,4);
        String month = date.substring(4, 6);
        String day =  date.substring(6, 8);



        String textMonth ="";

        switch (month) {
            case "01":
                textMonth = "ENE";
                break;
            case "02":
                textMonth = "FEB";
                break;
            case "03":
                textMonth = "MAR";
                break;
            case "04":
                textMonth = "ABR";
                break;
            case "05":
                textMonth = "MAY";
                break;
            case "06":
                textMonth = "JUN";
                break;
            case "07":
                textMonth = "JUL";
                break;
            case "08":
                textMonth = "AGO";
                break;
            case "09":
                textMonth = "SEP";
                break;
            case "10":
                textMonth = "OCT";
                break;
            case "11":
                textMonth = "NOV";
                break;
            case "12":
                textMonth = "DIC";
                break;
            default:
                throw new IllegalArgumentException("Fecha Invalida: " + textMonth);
        }

        if(day.charAt(0) == '0'){
            day = day.substring(1,2);
        }

        return day+textMonth+year;

    }

    private static void writeFile(String toPath, String content, String fileName){

        try (FileOutputStream fileOutputStream = new FileOutputStream(toPath+fileName+".txt")) {
            fileOutputStream.write(content.getBytes());
            System.out.println("Archivo: "+fileName+".txt generado");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int getCountDuplicates(String code, List<String> pluCodes){

        int totalCountDuplicates = 0;
        for (String cadena : pluCodes) {
            if(cadena.equals(code)){
                totalCountDuplicates++;
            }
        }
        return totalCountDuplicates;
    }

    private static Boolean isDuplicate(String code, List<String> duplicateCode) {

        Boolean duplicate=false;
        for (int i = 0; i < duplicateCode.size(); i++) {
            if(duplicateCode.get(i).equals(code)){
                duplicate = true;
            }
        }
        return duplicate;

    }

    private static List<String> getNullabledCodes(NodeList nodos, String cadena) {

        List<String> nullCodes = new ArrayList<>();
        String code,pluCode = "";
        for (int i = 0; i < nodos.getLength(); i++) {
            Node nodo = nodos.item(i);
            if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                if (nodo.getNodeName().equals(cadena)) {
                    NamedNodeMap atributos = nodo.getAttributes();
                    pluCode = getAttribute(atributos, "CodigoPLU");
                    code = getAttribute(atributos, "AnularItem");
                    if(code.equals("1")){
                        nullCodes.add(pluCode);
                    }
                }
            }
        }
        return nullCodes;
    }

    private static List<String> resolveString(NodeList nodos, String cadena) {

        List<String> pluCodes = new ArrayList<>();
        for (int i = 0; i < nodos.getLength(); i++) {
            Node nodo = nodos.item(i);
            if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                if (nodo.getNodeName().equals(cadena)) {
                    NamedNodeMap atributos = nodo.getAttributes();
                    pluCodes.add(saveProductCodes(atributos));
                }
            }
        }
        return pluCodes;
    }

    private static List<String> findDuplicateInStream(List<String> codeList)
    {

        return codeList.stream()
                .collect(Collectors.groupingBy(s -> s))
                .entrySet()
                .stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private static String saveProductCodes(NamedNodeMap nodos){

        String code ="";
        for (int i = 0; i < nodos.getLength(); i++) {
            Node atributo = nodos.item(i);
            if(atributo.getNodeName().equals("CodigoPLU")){
                code = atributo.getNodeValue();
            }
        }
        return code;
    }

    private static String getAttribute(NamedNodeMap atributos, String name) {
        String attribute = "";
        for (int j = 0; j < atributos.getLength(); j++) {
            Node atributo = atributos.item(j);
            if (atributo.getNodeName().equals(name)) {
                attribute = atributo.getNodeValue();
            }
        }
        return attribute;
    }

    private static String convertDateWhitOutBars(String reverseDate) {
        String finalDate = "";
        finalDate =reverseDate.substring(0, 4) + reverseDate.substring(5, 7)  + reverseDate.substring(8, 10);
        return finalDate;
    }

    private static String convertStaticDate(String reverseDate) {
        String finalDate = "";
        finalDate =reverseDate.substring(8, 10) +"-"+ reverseDate.substring(5, 7)  +"-"+ reverseDate.substring(0, 4);
        return finalDate;
    }

    private static String getStaticLine(NodeList nodos){

        String staticLine= "";
        String staticDate ="";
        String hour= "";
        for (int i = 0; i < nodos.getLength(); i++) {
            Node nodo = nodos.item(i);
            if (nodo.getNodeType() == Node.ELEMENT_NODE) {

                //Obtengo el Ticket, Fecha, hora
                if(nodo.getNodeName().equals("Frame")){
                    NamedNodeMap atributos = nodo.getAttributes();
                    //Campo Fecha estatico
                    staticDate =convertStaticDate(getAttribute(atributos,"Tail_Fecha"));
                    hour = getAttribute(atributos,"Tail_Hora");
                }
                staticLine = "[BT_17514] "+staticDate+" "+hour;
            }
        }
        return  staticLine;
    }


}


