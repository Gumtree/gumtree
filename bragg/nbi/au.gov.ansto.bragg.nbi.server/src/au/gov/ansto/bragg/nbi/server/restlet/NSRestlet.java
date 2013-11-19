package au.gov.ansto.bragg.nbi.server.restlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
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
import org.gumtree.core.object.IDisposable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class NSRestlet extends Restlet implements IDisposable {

	private static final String PART_HDBS = "hdbs";
	
	private static final String QUERY_FORMAT = "format";
	
	private static final String QUERY_CALLBACK = "callback";
	
	private static final String QUERY_JSON_CALLBACK = "jsoncallback";
	
	private static final String QUERY_DEVICS = "devices";
	
	private static Logger logger = LoggerFactory.getLogger(NSRestlet.class);
	
	private final static String SOAP_XML = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
			+ "<soap:Body xmlns:ns1=\"http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl\">"
			+ "<ns1:getReactorDisplayElement/></soap:Body></soap:Envelope>"; 
	private final static int POST_THREAD_HARTBEAT = 60000;
	private static final String[] QUERY_DEVICE_IDS = new String[]{"reactorPower", "cnsInTemp", "cnsTemp", 
		"cnsOutTemp", "timeStamp", "tg123Status", "tg4Status", "cg4Status", "cg123Status"};
	
	
	
	private static HttpClient client;
	private PostMethod postMethod;
	private Thread postThread;
	private static Map<String, String> devices;
	private static String serverStatus;
	
	
	public void handle(Request request, Response response) {
		
		if (postThread == null) {
			synchronized (NSRestlet.class) {
				clearValues();
				try {
					createPostThread();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		
		// Get path + query (everything after http://.../sics)
		String path = request.getResourceRef().getRemainingPart();
		// Take the first '/' out
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		// Get query
        Form queryForm = request.getResourceRef().getQueryAsForm();
        // Get pure path
        if (queryForm.size() != 0) {
        	path = path.substring(0, path.indexOf('?'));
        }
        // Get path tokens
        String[] pathTokens = path.split("/");
        if (pathTokens.length > 0) {
        	if (pathTokens[0].equals(PART_HDBS)) {
        		// Multi hdb requests
        		handleHdbRequests(request, response, queryForm);
        	} 
        } 
    }
		
	private void handleHdbRequests(Request request, Response response, Form queryForm) {
		JSONArray array = new JSONArray();
		
		// Get devices
		String devicesQuery = queryForm.getValues(QUERY_DEVICS);
		if (devicesQuery != null) {
			String[] deviceIds = devicesQuery.split(",");
			// Find data for each device query
			for (String deviceId : deviceIds) {
				
				if (devices.containsKey(deviceId)) {
					try {
						JSONObject controllerValues = createComponentJSONRepresentation(
								request, deviceId, devices.get(deviceId));
						array.put(controllerValues);
					} catch (Exception e) {
						logger.error(
								"Failed to get JSON representation for device "
										+ deviceId, e);
					}
				}
			}
			try {
				JSONObject controllerValues = createComponentJSONRepresentation(
						request, "NSStatus", devices.get(serverStatus));
				array.put(controllerValues);
			} catch (Exception e) {
				logger.error(
						"Failed to get JSON representation for status", e);
			}
		}
		
		
		// Write result
		try {
			JSONObject result = new JSONObject();
			result.put("hdbs", array);
			writeJSONObject(response, queryForm, result);
		} catch (JSONException e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		}
	}
	
	private void createPostThread() throws UnsupportedEncodingException {
		postMethod = new PostMethod("http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort?invoke=");
		postMethod.setDoAuthentication(false);
		RequestEntity entity = new StringRequestEntity(SOAP_XML, "text/xml", "ISO-8859-1");
		postMethod.setRequestEntity(entity);
		postThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					while (true) {
						updateValue();
						try {
							Thread.sleep(POST_THREAD_HARTBEAT);
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

	private void updateValue() throws HttpException, IOException  {
        // consult documentation for your web service
//		postMethod.setRequestHeader("SOAPAction", strSoapAction);
		int statusCode = getClient().executeMethod(postMethod);
		if (statusCode != HttpStatus.SC_OK) {
			System.err.println("HTTP GET failed: " + postMethod.getStatusLine());
//			postMethod.releaseConnection();
			clearValues();
		} else {
			try{
				SOAPMessage message = MessageFactory.newInstance().createMessage();  
				SOAPPart soapPart = message.getSOAPPart();  
				soapPart.setContent(new StreamSource(postMethod.getResponseBodyAsStream()));
				//        NodeList items = soapPart.getElementsByTagName("ns0:getReactorPowerResponseElement");
				//        Element item = soapPart.getElementById("ns1:value");
				final Element ele = soapPart.getDocumentElement();
				String value;
				for (String device : devices.keySet()) {
					value = "";
					try {
						value = ele.getElementsByTagName("ns1:" + device).item(0).getTextContent();
					} catch (Exception e) {
					}
					devices.put(device, value);
				}
				serverStatus = "OK";
			} catch (Exception e) {
				clearValues();
			}
		}
		postMethod.releaseConnection();
	}
	
	private void clearValues() {
		if (devices == null) {
			devices = new HashMap<String, String>();
		}
		for (String ids : QUERY_DEVICE_IDS) {
			devices.put(ids, "");
		}
		serverStatus = "ERROR";
	}

	private HttpClient getClient() {
		if (client == null) {
			synchronized (NSRestlet.class) {
				if (client == null) {
					client = new HttpClient();
				}
			}
		}
		return client;
	}
	
	private JSONObject createComponentJSONRepresentation(Request request,
			String id, String value) throws Exception {
		JSONObject result = new JSONObject();
		result.put("id", id);
		result.put("value", value);
		result.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(Calendar.getInstance().getTime()));
		return result;
	}
	
	private void writeJSONObject(Response response, Form queryForm, JSONObject jsonObject) {
		// Use content-type in header to resolve representation (see http://restlet.tigris.org/issues/show_bug.cgi?id=385)
	    // TODO: fix this will Restlet 1.1
	    String outputValue = queryForm.getValues(QUERY_FORMAT);
	    String callback = queryForm.getValues(QUERY_CALLBACK);
	    if (callback == null) {
	    	callback = queryForm.getValues(QUERY_JSON_CALLBACK);
	    }
		// Set response
		if (callback != null) {
			response.setEntity(callback + "(" + jsonObject.toString() + ")",
					MediaType.APPLICATION_JAVASCRIPT);
		} else {
			if ("json".equals(outputValue)) {
				response.setEntity(jsonObject.toString(),
						MediaType.APPLICATION_JSON);
			} else {
				response.setEntity(jsonObject.toString(), MediaType.TEXT_PLAIN);
			}
		}
	}

	@Override
	public void disposeObject() {
		postMethod.releaseConnection();
	}
	
	
}
