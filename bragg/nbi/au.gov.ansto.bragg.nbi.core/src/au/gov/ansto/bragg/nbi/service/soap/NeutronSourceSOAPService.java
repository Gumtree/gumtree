/**
 * 
 */
package au.gov.ansto.bragg.nbi.service.soap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;

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
import org.w3c.dom.Element;

/**
 * @author nxi
 *
 */
public class NeutronSourceSOAPService {

	private final static String SOAP_XML = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
			+ "<soap:Body xmlns:ns1=\"http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl\">"
			+ "<ns1:getReactorDisplayElement/></soap:Body></soap:Envelope>";
	private final static String SOAP_URL = "http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort?invoke=";
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

	private static void triggerUpdateEvent(SOAPMessage message) {
		for (ISoapEventListener listener : eventListeners) {
			listener.post(message);
		}
		if (allowSicsUpdate) {
			updateSicsNode(message);
		}
	}

	private static void updateSicsNode(SOAPMessage message) {
		ISicsController sics = SicsCore.getSicsController();
		if (sics != null) {
			SOAPPart soapPart = message.getSOAPPart();
			Element ele = soapPart.getDocumentElement();
			updateController(sics, ele, "reactor_power", "reactorPower");
			updateController(sics, ele, "cns_inlet_temp", "cnsInTemp");
			updateController(sics, ele, "cns_outlet_temp", "cnsOutTemp");
			updateController(sics, ele, "cns_flow", "cnsTemp");
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
				try {
					while (true) {
						int statusCode = 0;
						try {
							statusCode = client.executeMethod(postMethod);
						} catch (HttpException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						if (statusCode != HttpStatus.SC_OK) {
							System.err.println("HTTP GET failed: " + postMethod.getStatusLine());
						} else {
							System.err.println(postMethod.getResponseBodyAsString());
							try{
								SOAPMessage message = MessageFactory.newInstance().createMessage();  
								SOAPPart soapPart = message.getSOAPPart();  
								soapPart.setContent(new StreamSource(postMethod.getResponseBodyAsStream()));
								triggerUpdateEvent(message);
							} catch (Exception e) {
							}
						}
						postMethod.releaseConnection();
						try {
							Thread.sleep(heartbeat);
						} catch (InterruptedException e) {
							break;
						} 
					}
				} catch (Exception e) {
					e.printStackTrace();
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
		public void post(SOAPMessage message);
	}
	
	public static void addEventListener(ISoapEventListener listener) {
		eventListeners.add(listener);
	}
	
	public static void removeEventListener(ISoapEventListener listener) {
		eventListeners.remove(listener);
	}
}
