/**
 * 
 */
package au.gov.ansto.bragg.nbi.service.soap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import au.gov.ansto.bragg.nbi.core.NBISystemProperties;

/**
 * @author nxi
 *
 */
public class ProposalDBSOAPService {

	private final static String SOAP_XML_PRE = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
										+ "<soap:Body xmlns:ns1=\"http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl\">"
										+ "<ns1:getProposalInfoElement><ns1:proposalId>";
	private final static String SOAP_XML_FOLLOW = "</ns1:proposalId></ns1:getProposalInfoElement></soap:Body></soap:Envelope>";
	private final static String SOAP_URL = NBISystemProperties.PORTAL_ADDRESS + "/WebServices/WebServiceAppServiceSoapHttpPort?invoke=";
//	private final static int DEFAULT_POST_THREAD_HARTBEAT = 60000;
	private static HttpClient client;
	private static PostMethod postMethod;
	private static List<ISoapEventListener> eventListeners;
//	private static int heartbeat = DEFAULT_POST_THREAD_HARTBEAT;
//	private static boolean allowSicsUpdate = false;
	private static String soapArrayString = "bookingArray";
	private static String INSTRUMENT_NODE_NAME = "instrument";
	private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("d/M/yyyy");
	
	
	public enum ProposalItems {
		ID("proposalCode"),
	    PRINCIPAL_SCIENTIST("principalSci"), 
	    PRINCIPAL_EMAIL("principalEmail"),
	    OTHER_EMAIL("otherEmail"), 
	    EXPERIMENT_TITLE("exptTitle"),
	    TEXT("text"), 
	    OTHER_SCIENTIST("otherSci"),
	    INSTRUMENT("instrument"),
	    START_DATE("startDate"), 
	    END_DATE("endDate"),
	    LOCAL_CONTACT("localContact");
		
		private String itemString;
		
		private ProposalItems(String name) {
			itemString = name;
		}
		
		public String getItemString() {
			return itemString;
		}
	}

	static{
//		try {
//			heartbeat = Integer.valueOf(System.getProperty(SOAP_HEARTBEAT_PROPERTY_ID));
//		} catch (Exception e) {
//		}
//		if (postThread == null || !postThread.isAlive()) {
//			createPostThread();
//		}

	}

//	private static void triggerUpdateEvent(SOAPMessage message) {
//		for (ISoapEventListener listener : eventListeners) {
//			listener.post(message);
//		}
//		if (allowSicsUpdate) {
//			updateSicsNode(message);
//		}
//	}

//	private static void updateSicsNode(SOAPMessage message) {
//		ISicsController sics = SicsCore.getSicsController();
//		if (sics != null) {
//			SOAPPart soapPart = message.getSOAPPart();
//			Element ele = soapPart.getDocumentElement();
////			updateController(sics, ele, "reactor_power", "reactorPower");
////			updateController(sics, ele, "cns_inlet_temp", "cnsInTemp");
////			updateController(sics, ele, "cns_outlet_temp", "cnsOutTemp");
////			updateController(sics, ele, "cns_flow", "cnsTemp");
//		}
//
//	}

//	private static void updateController(ISicsController sics, Element ele, String deviceName, String soapName) {
//		IComponentController reactorController = sics.findDeviceController(deviceName);
//		if (reactorController != null) {
//			try {
//				String reactorPower = ele.getElementsByTagName("ns1:" + soapName).item(0).getTextContent();
//				((IDynamicController) reactorController).setTargetValue(ComponentData.createData(Float.valueOf(reactorPower)));
//				((IDynamicController) reactorController).commitTargetValue(null);
//			} catch (Exception e) {
//			}
//		}
//	}

	private static String getValue(Element element, String itemString) {
		try {
			return element.getElementsByTagName("ns1:" + itemString).item(0).getTextContent();
		} catch (Exception e) {
		}
		return null;
	}
	
	private static List<Node> findNodeListByName(Element element, String name) {
		List<Node> nodeList = new ArrayList<Node>();
		Node child = element.getFirstChild();
		if (child != null) {
			nodeList.addAll(findNodeListByName(child, name));
		}
		return nodeList;
	}
	
	private static List<Node> findNodeListByName(Node node, String name) {
		List<Node> nodeList = new ArrayList<Node>();
		if (node.getNodeName().equals(name)) {
			nodeList.add(node);
		}
		Node child = node.getFirstChild();
		if (child != null) {
			nodeList.addAll(findNodeListByName(child, name));
		}
		Node next = node.getNextSibling();
		if (next != null) {
			nodeList.addAll(findNodeListByName(next, name));
		}
		return nodeList;
	}
	
	private static Map<ProposalItems, String> processMessage(Element element, String instrumentId) {
		Map<ProposalItems, String> itemMap = new LinkedHashMap<ProposalDBSOAPService.ProposalItems, String>();
		itemMap.put(ProposalItems.ID, getValue(element, ProposalItems.ID.getItemString()));
		itemMap.put(ProposalItems.PRINCIPAL_SCIENTIST, getValue(element, ProposalItems.PRINCIPAL_SCIENTIST.getItemString()));
		itemMap.put(ProposalItems.PRINCIPAL_EMAIL, getValue(element, ProposalItems.PRINCIPAL_EMAIL.getItemString()));
		itemMap.put(ProposalItems.OTHER_EMAIL, getValue(element, ProposalItems.OTHER_EMAIL.getItemString()));
		itemMap.put(ProposalItems.EXPERIMENT_TITLE, getValue(element, ProposalItems.EXPERIMENT_TITLE.getItemString()));
		itemMap.put(ProposalItems.TEXT, getValue(element, ProposalItems.TEXT.getItemString()));
		itemMap.put(ProposalItems.OTHER_SCIENTIST, getValue(element, ProposalItems.OTHER_SCIENTIST.getItemString()));
		List<Node> list = findNodeListByName(element, "ns1:" + soapArrayString);
		Node instrumentNode = findInstrumentNode(list, instrumentId);
		if (instrumentNode != null) {
			Node startNode = findChildNodeByName(instrumentNode, "ns1:" + ProposalItems.START_DATE.getItemString());
			if (startNode != null) {
				itemMap.put(ProposalItems.START_DATE, startNode.getTextContent());
			}
			Node endNode = findChildNodeByName(instrumentNode, "ns1:" + ProposalItems.END_DATE.getItemString());
			if (endNode != null) {
				itemMap.put(ProposalItems.END_DATE, endNode.getTextContent());
			}
			Node contactNode = findChildNodeByName(instrumentNode, "ns1:" + ProposalItems.LOCAL_CONTACT.getItemString());
			if (contactNode != null) {
				itemMap.put(ProposalItems.LOCAL_CONTACT, contactNode.getTextContent());
			}
		}
		return itemMap;
	}
	
	private static Node findInstrumentNode(List<Node> list, String instrumentId) {
		Node closestNode = null;
		Node exactNode = null;
		for (Node node : list) {
			Node instrumentChild = findChildNodeByName(node, "ns1:" + INSTRUMENT_NODE_NAME);
			if (instrumentChild != null && instrumentChild.getTextContent().toLowerCase().equals(instrumentId.toLowerCase())){
				closestNode = node;
				Node startChild = findChildNodeByName(node, "ns1:" + ProposalItems.START_DATE.getItemString());
				Node endChild = findChildNodeByName(node, "ns1:" + ProposalItems.END_DATE.getItemString());
				Date current = new Date();
				try {
					if (startChild != null) {
						Date startDate = DATE_FORMATTER.parse(startChild.getTextContent());
						if (current.after(startDate)) {
							if (endChild != null) {
								Date endDate = DATE_FORMATTER.parse(endChild.getTextContent());
								if (current.before(endDate) || current.equals(endDate)) {
									exactNode = node;
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (exactNode != null) {
			return exactNode;
		}
		return closestNode;
	}
	
	private static Node findChildNodeByName(Node node, String name) {
//		NodeList children = node.getChildNodes();
//		for (int j = 0; j < children.getLength(); j++) {
//			Node child = children.item(j);
//			if (child.getNodeName().equals(name)) {
//				return child;
//			}
//		}
//		return null;
		Node child = node.getFirstChild();
		if (child != null) {
			List<Node> found = findNodeListByName(child, name);
			if (found.size() > 0) {
				return found.get(0);
			}
		}
		return null;
	}
	
	public static Map<ProposalItems, String> getProposalInfo(int proposalId, String instrumenId) {
		postMethod = new PostMethod(SOAP_URL);
		client = new HttpClient();
		postMethod.setDoAuthentication(false);
		RequestEntity entity = null;
		try {
			entity = new StringRequestEntity(SOAP_XML_PRE + proposalId + SOAP_XML_FOLLOW, "text/xml", "ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		postMethod.setRequestEntity(entity);
		eventListeners = new ArrayList<ISoapEventListener>();
		
		int statusCode = 0;
		try {
			statusCode = client.executeMethod(postMethod);
		} catch (HttpException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		Map<ProposalItems, String> proposalMap = null;
		if (statusCode != HttpStatus.SC_OK) {
			System.err.println("HTTP GET failed: " + postMethod.getStatusLine());
		} else {
			try{
				SOAPMessage message = MessageFactory.newInstance().createMessage();  
				SOAPPart soapPart = message.getSOAPPart();  
				soapPart.setContent(new StreamSource(postMethod.getResponseBodyAsStream()));
				Element element = soapPart.getDocumentElement();
				proposalMap = processMessage(element, instrumenId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		postMethod.releaseConnection();
		return proposalMap;
	}

	/**
	 * 
	 */
	public ProposalDBSOAPService() {
		synchronized (ProposalDBSOAPService.class) {
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
