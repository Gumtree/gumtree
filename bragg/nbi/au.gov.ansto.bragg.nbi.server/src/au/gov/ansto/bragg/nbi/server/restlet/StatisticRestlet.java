package au.gov.ansto.bragg.nbi.server.restlet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.gumtree.core.object.IDisposable;
import org.gumtree.sics.io.SicsLogManager;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticRestlet extends Restlet implements IDisposable {

	private static final String PART_STATUS = "status";
	private static final String PART_GETSTART = "getStart";
	
	private static final String QUERY_FROM = "from";
	private static final String QUERY_TO = "to";
	
	private static final String QUERY_FORMAT = "format";
	
	private static final String QUERY_CALLBACK = "callback";
	
	private static final String QUERY_JSON_CALLBACK = "jsoncallback";
	
	private static Logger logger = LoggerFactory.getLogger(StatisticRestlet.class);
	
	
	
	public StatisticRestlet() {
		super();
	}
	
	public void handle(Request request, Response response) {
		
		
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
        	if (pathTokens[0].equals(PART_STATUS)) {
        		// Multi hdb requests
        		handleStatusRequests(request, response, queryForm);
        	} else if (pathTokens[0].equals(PART_GETSTART)){
        		handleGetStartRequests(request, response, queryForm);
        	}
        } 
    }
		
	private void handleStatusRequests(Request request, Response response, Form queryForm) {
		
	    String fromValue = queryForm.getValues(QUERY_FROM);
	    String toValue = queryForm.getValues(QUERY_TO);

	    SicsLogManager logManager = SicsLogManager.getInstance();
	    
	    try {
		    Date start = logManager.fileDateFormat.parse(fromValue);
		    Date end = logManager.fileDateFormat.parse(toValue);
		    Map<String, Long> logCounts = logManager.processLog(start, end);
			JSONObject result = new JSONObject();
			long total = logCounts.get("TOTAL");
			for (Entry<String, Long> entry : logCounts.entrySet()) {
				if ("TOTAL".equals(entry.getKey())){
					result.put("TOTAL", String.format("%.1fh", entry.getValue() / 3600000.));
				} else if ("STATUS.EAGER TO EXECUTE".equals(entry.getKey())){
					result.put("STATUS.IDLE", String.format("%.5g", ((float) entry.getValue()) / total));
				} else {
					result.put(entry.getKey(), String.format("%.5g", ((float) entry.getValue()) / total));
				}
			}
			writeJSONObject(response, queryForm, result);
		} catch (Exception e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		}
	    
		
	}
	
	private void handleGetStartRequests(Request request, Response response, Form queryForm) {
		
	    SicsLogManager logManager = SicsLogManager.getInstance();
	    
	    try {
			JSONObject result = new JSONObject();
			result.put("start", logManager.getStartDate());
			writeJSONObject(response, queryForm, result);
		} catch (Exception e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		}
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
	}
	
	
}
