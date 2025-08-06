package au.gov.ansto.bragg.quokka.msw.util;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExperimentDescriptionLoader {
	// finals
	private static final String BUSY_ID_NAME = ExperimentDescriptionLoader.class.getSimpleName();
	private static final AtomicInteger BUSY_ID = new AtomicInteger();
	
	private static String PORTAL_ADDRESS = System.getProperty("gumtree.portalAddress", "http://neutron.ansto.gov.au");
	
	private static final String SOAP_URL = PORTAL_ADDRESS + 
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
	private final List<IExperimentDescriptionLoaderListener> listeners;
	
	// construction
	public ExperimentDescriptionLoader(String instrument) {
		this.instrument = instrument;
		
		busy = new AtomicBoolean(false);
		listeners = new ArrayList<>();
	}

	// properties
	public boolean isBusy() {
		return busy.get();
	}
	
	// methods
	public void load(final Shell shell) {
		if (!busy.compareAndSet(false, true))
			return;

		final Integer busyId = BUSY_ID.incrementAndGet();
		shell.setData(BUSY_ID_NAME, busyId);
		shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_APPSTARTING));
		
		try {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					Map<String, String> response = null;
					try {
						response = load(instrument);
					}
					finally {
						busy.set(false); // allow load to run again
						
						final Map<String, String> tmp = response;
						shell.getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (busyId == shell.getData(BUSY_ID_NAME)) {
									shell.setCursor(null);
									shell.setData(BUSY_ID_NAME, null);
								}
								
								if (tmp != null)
									raiseOnLoaded(tmp);
							}
						});
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
	public synchronized void addListener(IExperimentDescriptionLoaderListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);
	}
	public synchronized boolean removeListener(IExperimentDescriptionLoaderListener listener) {
		return listeners.remove(listener);
	}

	// helpers
	private synchronized void raiseOnLoaded(Map<String, String> response) {
		for (IExperimentDescriptionLoaderListener listener : listeners)
			listener.onLoaded(response);
	}
	private static Map<String, String> load(String instrument) {
		boolean found = false;
		Map<String, String> result = new HashMap<>();
		
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
