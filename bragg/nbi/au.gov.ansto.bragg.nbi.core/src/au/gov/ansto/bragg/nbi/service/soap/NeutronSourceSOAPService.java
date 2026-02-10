/**
 * 
 */
package au.gov.ansto.bragg.nbi.service.soap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//import javax.xml.soap.MessageFactory;
//import javax.xml.soap.SOAPConstants;
//import javax.xml.soap.SOAPMessage;
//import javax.xml.soap.SOAPPart;
//import javax.xml.transform.stream.StreamSource;
//import jakarta.xml.soap.*;
//import jakarta.xml.soap.MessageFactory;
//import jakarta.xml.soap.SOAPConstants;
//import jakarta.xml.soap.SOAPMessage;
//import jakarta.xml.soap.SOAPPart;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.control.controllers.ComponentData;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ansto.bragg.nbi.core.NBISystemProperties;

/**
 * @author nxi
 *
 */
public class NeutronSourceSOAPService {

	private final static String SOAP_XML = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
			+ "<soap:Body xmlns:ns1=\"http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl\">"
			+ "<ns1:getReactorDisplayElement/></soap:Body></soap:Envelope>";
	private final static String SOAP_URL = NBISystemProperties.PORTAL_ADDRESS + "/WebServices/WebServiceAppServiceSoapHttpPort?invoke=";
	private final static int DEFAULT_POST_THREAD_HARTBEAT = 60000;
	private static HttpClient client;
	private static PostMethod postMethod;
	private static Thread postThread;
	private static List<ISoapEventListener> eventListeners;
	private static final String SOAP_HEARTBEAT_PROPERTY_ID = "gumtree.soap.heartBeat";
	private static final String SICS_ALLOW_SOAP_PROPERTY_ID = "gumtree.sics.allowSoapEvent";
	private static int heartbeat = DEFAULT_POST_THREAD_HARTBEAT;
	private static boolean allowSicsUpdate = false;

	static{
		try {
			heartbeat = Integer.valueOf(System.getProperty(SOAP_HEARTBEAT_PROPERTY_ID));
		} catch (Exception e) {
		}
		try {
			allowSicsUpdate = Boolean.valueOf(System.getProperty(SICS_ALLOW_SOAP_PROPERTY_ID));
		} catch (Exception e) {
		}
		if (postThread == null || !postThread.isAlive()) {
			createPostThread();
		}

	}

	private static void triggerUpdateEvent(Element element) {
		for (ISoapEventListener listener : eventListeners) {
			listener.post(element);
		}
		if (allowSicsUpdate) {
			updateSicsNode(element);
		}
	}

	private static void updateSicsNode(Element element) {
		ISicsController sics = SicsCore.getSicsController();
		if (sics != null) {
//			SOAPPart soapPart = message.getSOAPPart();
//			Element ele = soapPart.getDocumentElement();
			updateController(sics, element, "reactor_power", "reactorPower");
			updateController(sics, element, "cns_inlet_temp", "cnsInTemp");
			updateController(sics, element, "cns_outlet_temp", "cnsOutTemp");
			updateController(sics, element, "cns_flow", "cnsTemp");
		}

	}

	private static void updateController(ISicsController sics, Element ele, String deviceName, String soapName) {
		IComponentController reactorController = sics.findDeviceController(deviceName);
		if (reactorController != null) {
			try {
				String reactorPower = ele.getElementsByTagName("ns1:" + soapName).item(0).getTextContent();
				((IDynamicController) reactorController).setTargetValue(ComponentData.createData(Float.valueOf(reactorPower)));
				((IDynamicController) reactorController).commitTargetValue(null);
			} catch (Exception e) {
			}
		}
	}

	private static void createPostThread() {
		postMethod = new PostMethod(SOAP_URL);
		client = new HttpClient();
		postMethod.setDoAuthentication(false);
		RequestEntity entity = null;
		try {
			entity = new StringRequestEntity(SOAP_XML, "text/xml", "ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		postMethod.setRequestEntity(entity);
		eventListeners = new ArrayList<ISoapEventListener>();
		postThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					try {
						int statusCode = 0;
						try {
							statusCode = client.executeMethod(postMethod);
						} catch (HttpException e1) {
							e1.printStackTrace();
						} catch (IOException e2) {
							e2.printStackTrace();
						}
						if (statusCode != HttpStatus.SC_OK) {
							System.err.println("HTTP GET failed: " + postMethod.getStatusLine());
						} else {
//							System.err.println(postMethod.getResponseBodyAsString());
							try{
//								SOAPMessage message = MessageFactory.newInstance().createMessage();  
//								SOAPPart soapPart = message.getSOAPPart();  
//								soapPart.setContent(new StreamSource(postMethod.getResponseBodyAsStream()));
//								triggerUpdateEvent(message);
								
//								MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
//						        SOAPMessage response = factory.createMessage(null, postMethod.getResponseBodyAsStream());
//						        String response = postMethod.getResponseBody
//						        triggerUpdateEvent(response);
								
								DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
							    dbf.setNamespaceAware(true); // SOAP requires this!

							    DocumentBuilder builder = dbf.newDocumentBuilder();
							    Document doc = builder.parse(postMethod.getResponseBodyAsStream());
							    triggerUpdateEvent(doc.getDocumentElement());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						postMethod.releaseConnection();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					try {
						Thread.sleep(heartbeat);
					} catch (InterruptedException e) {
						break;
					} 
				}
			}
		});
		postThread.start();		
	}

	/**
	 * 
	 */
	public NeutronSourceSOAPService() {
		synchronized (NeutronSourceSOAPService.class) {
			if (postThread == null || !postThread.isAlive()){
				createPostThread();
			}
		}
	}

	public interface ISoapEventListener {
		public void post(Element element);
	}
	
	public static void addEventListener(ISoapEventListener listener) {
		eventListeners.add(listener);
	}
	
	public static void removeEventListener(ISoapEventListener listener) {
		eventListeners.remove(listener);
	}
}
