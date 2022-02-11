package hu.gov.allamkincstar.eurofxref;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FXMLController implements Initializable {

    private static final String CURRENCY = "currency";
    private static final String RATE = "rate";
    private float hufRate;

    @FXML
    private Label label1;

    @FXML
    private Label label2;

    @FXML
    private ComboBox<String> combobox1;

    @FXML
    private TextField textfield1;

    @FXML
    private void handleButton1Action(ActionEvent event) {
        System.out.println("You clicked Button1!");
        label1.setText("Árfolyam letöltés!");
        URL url;
        ReadableByteChannel readableByteChannel = null;
        FileOutputStream fileOutputStream = null;
        try {
            url = new URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
            readableByteChannel = Channels.newChannel(url.openStream());
            fileOutputStream = new FileOutputStream("eurofxref-daily.xml");
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

            File konyvtar = new File("eurofxref-daily.xml");

            java.util.List<String> rates = xmlLoad(konyvtar);

            combobox1.getItems().setAll(rates);

            label1.setText("Árfolyam letöltés elkészült!");

        } catch (MalformedURLException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (readableByteChannel != null) {
                    readableByteChannel.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @FXML
    private void handleButton2Action(ActionEvent event) {
        System.out.println("You clicked Button2!");
        label2.setText("Hiba");

        float foreignExchangeMoney;
        float foreignExchangeRates;
        float hufMoney;

        if (combobox1.getValue() != null
                && !combobox1.getValue().isEmpty()
                && textfield1.getText() != null
                && !textfield1.getText().isEmpty()) {
            foreignExchangeMoney = Float.valueOf(textfield1.getText());
            foreignExchangeRates = Float.valueOf(combobox1.getValue().substring(combobox1.getValue().indexOf("-") + 1));
            hufMoney = ((foreignExchangeMoney / foreignExchangeRates) * hufRate)  ;
            label2.setText(String.valueOf(hufMoney) + " Ft");
        }
    }

    public java.util.List<String> xmlLoad(File xmlFile) {
        java.util.List<String> rates = new ArrayList<>();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("Cube");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element elem = (Element) nodeList.item(i);
                if (elem.getAttribute(CURRENCY) != null && !elem.getAttribute(CURRENCY).isEmpty()) {
                    rates.add(elem.getAttribute(CURRENCY) + "-" + elem.getAttribute(RATE));

                    if ("HUF".equalsIgnoreCase(elem.getAttribute(CURRENCY))) {
                        hufRate = Float.valueOf(elem.getAttribute(RATE));
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            java.util.logging.Logger.getLogger(FXMLController.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
        }
        rates.add("EUR-1");
        return rates;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
}
