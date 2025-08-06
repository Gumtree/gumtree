package au.gov.ansto.bragg.nbi.service.soap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import au.gov.ansto.bragg.nbi.core.NBISystemProperties;

public class CurrentProposalSOAPService {
	// finals
	
	public interface IServiceListener {
		// methods
		public void onLoaded(Map<String, String> response);
	}
	
	public static final String BUSY_ID_NAME = CurrentProposalSOAPService.class.getSimpleName();
	public static final AtomicInteger BUSY_ID = new AtomicInteger();
	private static final Logger logger = LoggerFactory.getLogger(CurrentProposalSOAPService.class);
	
	private static final String SOAP_URL = NBISystemProperties.PORTAL_ADDRESS + 
			"/WebServices/WebServiceAppServiceSoapHttpPort?invoke=";
	
	private static final String SOAP_XML =
			"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
			"  <soap:Body xmlns:ns1=\"http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl\">" +
			"    <ns1:getBraggInfoElement/>" +
			"  </soap:Body>" +
			"</soap:Envelope>";
	
	// fields
	private final String instrument;
	private final AtomicBoolean busy;
	// listeners
	private final List<IServiceListener> listeners;
	
	// construction
	public CurrentProposalSOAPService(String instrument) {
		this.instrument = instrument;
		
		busy = new AtomicBoolean(false);
		listeners = new ArrayList<IServiceListener>();
	}

	// properties
	public boolean isBusy() {
		return busy.get();
	}
	
	// methods
	public void load() {
		if (!busy.compareAndSet(false, true))
			return;

		final Integer busyId = BUSY_ID.incrementAndGet();
		
		try {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					Map<String, String> response = null;
					try {
						response = load(instrument);
						logger.info(response.toString());
						raiseOnLoaded(response);
					}
					finally {
						busy.set(false); // allow load to run again
						
//						final Map<String, String> tmp = response;
					}
				}
			});
			thread.start();
		}
		catch (Exception e) {
			busy.set(false);
		}
	}
	// listeners
	public synchronized void addListener(IServiceListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);
	}
	public synchronized boolean removeListener(IServiceListener listener) {
		return listeners.remove(listener);
	}

	// helpers
	private synchronized void raiseOnLoaded(Map<String, String> response) {
		for (IServiceListener listener : listeners)
			listener.onLoaded(response);
	}
	private static Map<String, String> load(String instrument) {
		boolean found = false;
		Map<String, String> result = new HashMap<String, String>();
		
		try {
			PostMethod postMethod = new PostMethod(SOAP_URL);
			try {
				postMethod.setDoAuthentication(false);
				postMethod.setRequestEntity(new StringRequestEntity(SOAP_XML, "text/xml", "ISO-8859-1"));
	
				HttpClient client = new HttpClient();
	
				if (HttpStatus.SC_OK == client.executeMethod(postMethod)) {
	
					SOAPMessage message = MessageFactory.newInstance().createMessage();  
					SOAPPart soapPart = message.getSOAPPart();
					
					soapPart.setContent(new StreamSource(postMethod.getResponseBodyAsStream()));

					Element element = soapPart.getDocumentElement();
					
					NodeList experimentInfoNodes = element.getElementsByTagName("ns1:experimentInfo");

					for (int i = 0; (i != experimentInfoNodes.getLength()) && !found; i++) {
						result.clear();
						Node experimentInfoNode = experimentInfoNodes.item(i);
						NodeList subNodes = experimentInfoNode.getChildNodes();
						for (int j = 0; j != subNodes.getLength(); j++) {
							Node infoNode = subNodes.item(j);
							
							String name = infoNode.getLocalName();
							String value = infoNode.getTextContent();
							
							result.put(name, value);

							found |= "instrName".equals(name) && instrument.equals(value.toUpperCase());
						}
					}
				}
			}
			finally {
				postMethod.releaseConnection();
			}
		}
		catch (Exception exception) {
		}

		if (!found)
			result.clear();
		
		return result;
	}
}
