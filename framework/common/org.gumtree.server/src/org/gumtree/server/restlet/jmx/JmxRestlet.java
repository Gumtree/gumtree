package org.gumtree.server.restlet.jmx;

import java.util.Calendar;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.MBeanServerNotFoundException;
import org.springframework.jmx.support.JmxUtils;

public class JmxRestlet extends Restlet {

	private static final String QUERY_FORMAT = "format";

	private static final Logger logger = LoggerFactory.getLogger(JmxRestlet.class);
	
	private MBeanServer server;
	
	public void handle(Request request, Response response) {
		long startTime = System.currentTimeMillis();
		String path = request.getResourceRef().getRemainingPart();
		// Take the first '/' out
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		// Process query
        Form queryForm = request.getResourceRef().getQueryAsForm();
        // Get pure path
        if (queryForm.size() != 0) {
        	path = path.substring(0, path.indexOf('?'));
        }
        ObjectName objectName = null;
		if (path.length() != 0) {
			try {
				objectName = new ObjectName(path);
			} catch (Exception e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
			}
		}
        // Get MBeans
		Set<ObjectName> mbeans = getServer().queryNames(objectName, null);
		JSONObject output = new JSONObject();
		// Process MBeans
		for (ObjectName mbean : mbeans) {
			processMBean(mbean, output);
		}
		// Reply
		JSONObject result = new JSONObject();
		try {
			result.put("JMX", output);
			result.put("processTime", System.currentTimeMillis() - startTime + "ms");
			result.put("time", Calendar.getInstance().getTime());
			writeJSONObject(response, queryForm, result);
		} catch (JSONException e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		}
	}
	
	private void processMBean(ObjectName mbean, JSONObject output) {
		try {
			MBeanAttributeInfo[] attributes = getServer().getMBeanInfo(mbean).getAttributes();
			JSONObject mbeanData = new JSONObject();
			for (MBeanAttributeInfo attribute : attributes) {
				try {
					Object data = getServer().getAttribute(mbean, attribute.getName());
					if (data instanceof CompositeData) {
						mbeanData.put(attribute.getName(), processCompositeData((CompositeData) data));
					} else if (data instanceof TabularData) {
						mbeanData.put(attribute.getName(), processTabularData((TabularData) data));
					} else if (data.getClass().isArray()) {
						mbeanData.put(attribute.getName(), processArrayData(data));
					} else {
						mbeanData.put(attribute.getName(), data);
					}
				} catch (Exception e) {
					try {
						mbeanData.put(attribute.getName(), "Unavailable");
					} catch (JSONException e1) {
					}
				}
			}
			output.put(mbean.toString(), mbeanData);
		} catch (Exception e) {
			try {
				output.put(mbean.toString(), "Unavailable");
			} catch (JSONException e1) {
			}
		}
	}
	
	private JSONObject processCompositeData(CompositeData data) {
		JSONObject result = new JSONObject();
		for (String key : data.getCompositeType().keySet()) {
			try {
				Object value = data.get(key);
				if (value instanceof CompositeData) {
					result.put(key, processCompositeData((CompositeData) value));
				} else if (value instanceof TabularData) {
					result.put(key, processTabularData((TabularData) value));
				} else if (value.getClass().isArray()) {
					result.put(key, processArrayData(value));
				} else {
					result.put(key, value);
				}
			} catch (JSONException e) {
				try {
					result.put(key, "Unavailable");
				} catch (JSONException e1) {
				}
			}
		}
		return result;
	}
	

	private JSONArray processTabularData(TabularData data) {
		JSONArray result = new JSONArray();
		for (Object tabularDataRow : data.values()) {
			if (tabularDataRow instanceof CompositeData) {
				result.put(processCompositeData((CompositeData) tabularDataRow));
			}
		}
		return result;
	}

	private JSONArray processArrayData(Object data) {
		JSONArray array = new JSONArray();
		if (data instanceof CompositeData[]) {
			for (CompositeData compositeData : (CompositeData[]) data) {
				array.put(processCompositeData(compositeData));
			}
		} else if (data instanceof String[]) {
			for (String stringData : (String[]) data) {
				array.put(stringData);
			}
		} else if (data instanceof Object[]) {
			for (Object objectData : (Object[]) data) {
				array.put(objectData);
			}
		} else if (data instanceof int[]) {
			for (int number : (int[]) data) {
				array.put(number);
			}
		} else if (data instanceof long[]) {
			for (long number : (long[]) data) {
				array.put(number);
			}
		} else if (data instanceof float[]) {
			for (float number : (float[]) data) {
				try {
					array.put(number);
				} catch (JSONException e) {
					array.put("Invalid Number");
				}
			}
		} else if (data instanceof double[]) {
			for (double number : (double[]) data) {
				try {
					array.put(number);
				} catch (JSONException e) {
					array.put("Invalid Number");
				}
			}
		} else if (data instanceof boolean[]) {
			for (boolean booleanData : (boolean[]) data) {
				array.put(booleanData);
			}
		} else {
			array.put(data);
		}
		return array;
	}
	
	private MBeanServer getServer() {
		if (server == null) {
			try {
				server = JmxUtils.locateMBeanServer();
			} catch (MBeanServerNotFoundException e) {
				logger.error("Failed to load MBean Server");
			}
		}
		return server;
	}
	
	private void writeJSONObject(Response response, Form queryForm, JSONObject jsonObject) {
		// Use content-type in header to resolve representation (see http://restlet.tigris.org/issues/show_bug.cgi?id=385)
	    // TODO: fix this after Restlet 1.1
	    String outputValue = queryForm.getValues(QUERY_FORMAT);
	    // Set response
	    if ("json".equals(outputValue)) {
	    	response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
	    } else {
	    	response.setEntity(jsonObject.toString(), MediaType.TEXT_PLAIN);
	    }
	}
}
