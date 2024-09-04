package org.gumtree.control.server.restlet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.gumtree.control.batch.BatchStatus;
import org.gumtree.control.batch.IBatchControl;
import org.gumtree.control.batch.BatchControl.BatchInfo;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.SicsControllerAdapter;
import org.gumtree.control.model.ModelUtils;
//import org.gumtree.util.ILoopExitCondition;
//import org.gumtree.util.LoopRunner;
//import org.gumtree.util.LoopRunnerStatus;
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

public class ControlRestlet extends Restlet {

	private static final String PART_HDB = "hdb";
	
	private static final String PART_HDBS = "hdbs";
	
	private static final String PART_DEVICES = "devices";
	
	private static final String PART_GROUP = "group";
	
	private static final String PART_BATCH = "batch";
	
	private static final String PART_STATUS = "status";
	
	private static final String QUERY_FORMAT = "format";
	
	private static final String QUERY_CALLBACK = "callback";
	
	private static final String QUERY_JSON_CALLBACK = "jsoncallback";
	
	private static final String QUERY_DEVICS = "devices";
	
	private static final String QUERY_GROUP = "path";
	
	private static final String QUERY_COMPONENTS = "components";
	
	private static final String PROP_NICK = "nick";
	
	private static final String[] PROP_COLLECTED = new String[] {PROP_NICK, "nxalias", "units"};

	private static Logger logger = LoggerFactory.getLogger(ControlRestlet.class);
	
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
        	if (pathTokens[0].equals(PART_HDB)) {
        		// Single hdb request
        		String hdbPath = path.substring(pathTokens[0].length());
        		handleHdbRequest(request,response, hdbPath, queryForm);
        	} else if (pathTokens[0].equals(PART_HDBS)) {
        		// Multi hdb requests
        		handleHdbRequests(request, response, queryForm);
        	} else if (pathTokens[0].equals(PART_DEVICES)) {
        		// Devices request
        		handleDevicesRequests(request, response, queryForm);
        	} else if (pathTokens[0].equals(PART_GROUP)) {
        		// Devices request
        		handleHdbGroupRequest(request, response, queryForm);
        	} else if (pathTokens[0].equals(PART_BATCH)) {
        		// Devices request
        		handleBatchRequest(request, response, queryForm);
        	} else if (pathTokens[0].equals(PART_STATUS)) {
        		// Specific sics request
        		handleSicsRequest(request, response, queryForm);
        	} else if (pathTokens[0].length() == 0) {
        		// General sics request
        		handleSicsConnectionRequest(request, response, queryForm);
        	}
        } else {
        	// General sics request
        	handleSicsRequest(request, response, queryForm);
        }
    }
	
	private void handleHdbRequest(Request request, Response response,
			String hdbPath, Form queryForm) {
		// Find the controller from the path
		ISicsController controller = SicsManager.getSicsModel().findController(hdbPath);
		if (controller != null) {
			try {
				JSONObject result = createComponentJSONRepresentation(request, controller, true);
				// Write result
				writeJSONObject(response, queryForm, result);
			} catch (Exception e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
			}
		} else {
			// Controller not found
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}
	
	private void handleHdbGroupRequest(Request request, Response response, Form queryForm) {
		JSONArray array = new JSONArray();
		
		// Get devices
		String devicesQuery = queryForm.getValues(QUERY_GROUP);
		if (devicesQuery != null) {
			ISicsController controller = SicsManager.getSicsModel().findController(devicesQuery);
			List<ISicsController> childList = controller.getChildren();
			Collections.sort(childList, new Comparator<ISicsController>(){

				@Override
				public int compare(ISicsController o1, ISicsController o2) {
					return o1.getId().compareTo(o2.getId());
				}
			});
			for (ISicsController child : childList){
				try {
					JSONObject controllerValues = createComponentJSONRepresentationWithProp(
							request, child, false);
					array.put(controllerValues);
				} catch (Exception e) {
					logger.error(
							"Failed to get JSON representation for device "
									+ devicesQuery, e);
				}
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

	private void handleBatchRequest(Request request, Response response, Form queryForm) {
		
		// Get devices
		JSONObject result = new JSONObject();
		IBatchControl bufferManager = SicsManager.getBatchControl();
//		BatchStatus bufferStatus = bufferManager.getStatus();
		try {
//			result.put("status", bufferStatus);
//			if (bufferStatus == BatchStatus.RUNNING) {
//				String bufferName = bufferManager.getRunningBuffername();
//				String runningText = bufferManager.getRunningText();
//				String bufferRange = bufferManager.getRunningBufferRangeString();
//				result.put("name", bufferName);
//				result.put("text", runningText);
//				result.put("range", bufferRange);
//				try {
//					String bufferContent = bufferManager.getRunningBufferContent();
//					result.put("content", bufferContent);
//				} catch (Exception ex) {
//					result.put("content", ex.toString());
//				}
//			}
			BatchInfo batchInfo = bufferManager.getBatchInfo();
			result.put("status", batchInfo.getStatus());
			result.put("name", batchInfo.getBatchName());
			result.put("text", batchInfo.getBatchText());
			result.put("range", batchInfo.getBatchRange());
			result.put("content", batchInfo.getBatchText());
			writeJSONObject(response, queryForm, result);
		} catch (JSONException e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		}

//		String devicesQuery = queryForm.getValues(QUERY_GROUP);
//		if (devicesQuery != null) {
//			ISicsController controller = getSicsManager()
//					.getServerController().findChild(devicesQuery);
//			ISicsController[] children = controller.getChildren();
//			for (ISicsController child : children){
//				try {
//					JSONObject controllerValues = createComponentJSONRepresentation(
//							request, child, false);
//					array.put(controllerValues);
//				} catch (Exception e) {
//					logger.error(
//							"Failed to get JSON representation for device "
//									+ devicesQuery, e);
//				}
//			}
//		}
		
		
		// Write result
	}
	
	private void handleHdbRequests(Request request, Response response, Form queryForm) {
		JSONArray array = new JSONArray();
		
		// Get devices
		String devicesQuery = queryForm.getValues(QUERY_DEVICS);
		if (devicesQuery != null) {
			String[] deviceIds = devicesQuery.split(",");
			// Find data for each device query
			for (String deviceId : deviceIds) {
				ISicsController controller = SicsManager.getSicsModel().findControllerById(deviceId);
				if (controller != null) {
					try {
						JSONObject controllerValues = createComponentJSONRepresentation(
								request, controller, false);
						array.put(controllerValues);
					} catch (Exception e) {
						logger.error(
								"Failed to get JSON representation for device "
										+ deviceId, e);
					}
				}
			}
		}
		
		// Get components
		String componentsQuery = queryForm.getValues(QUERY_COMPONENTS);
		if (componentsQuery != null) {
			String[] paths = componentsQuery.split(",");
			// Find data for each device query
			for (String path : paths) {
				ISicsController controller = SicsManager.getSicsModel().findControllerByPath(path);
				if (controller != null) {
					try {
						JSONObject controllerValues = createComponentJSONRepresentation(
								request, controller, false);
						array.put(controllerValues);
					} catch (Exception e) {
						logger.error(
								"Failed to get JSON representation for component "
										+ path, e);
					}
				}
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
	
	private void handleDevicesRequests(Request request, Response response,
			Form queryForm) {
		JSONArray array = new JSONArray();
		ISicsController[] deviceControllers = SicsManager.getSicsModel().getSicsControllers();
		for (ISicsController controller : deviceControllers) {
			try {
				JSONObject controllerValues = new JSONObject();
				controllerValues.put("id", controller.getId());
				controllerValues.put("deviceId", controller.getDeviceId());
				controllerValues.put("path", controller.getPath());
				array.put(controllerValues);
			} catch (Exception e) {
				logger.error(
						"Failed to get JSON representation for component "
								+ controller.getPath(), e);
			}
		}
		
		// Write result
		try {
			JSONObject result = new JSONObject();
			result.put("devices", array);
			writeJSONObject(response, queryForm, result);
		} catch (JSONException e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		}
	}
	
	// We only export proxy status at this stage
	private void handleSicsRequest(Request request, Response response, Form queryForm) {
		JSONObject result = new JSONObject();
		try {
			ServerStatus status = SicsManager.getSicsProxy().getServerStatus();
			status = (status == null) ? ServerStatus.UNKNOWN : status;
			result.put("status", status.getText());
			result.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(Calendar.getInstance().getTime()));
			writeJSONObject(response, queryForm, result);
		} catch (JSONException e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		}
	}
	
	// We only export proxy status at this stage
	private void handleSicsConnectionRequest(Request request, Response response, Form queryForm) {
		JSONObject result = new JSONObject();		
		try {
			result.put("status", SicsManager.getSicsProxy().getServerStatus().toString());
			result.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(Calendar.getInstance().getTime()));
			writeJSONObject(response, queryForm, result);
		} catch (JSONException e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		}
	}
	
	private JSONObject createComponentJSONRepresentation(Request request,
			ISicsController controller, boolean detailed) throws Exception {
		JSONObject result = new JSONObject();
		result.put("id", controller.getId());
		result.put("path", controller.getPath());
		result.put("status", controller.getState().toString());
		result.put("deviceId", controller.getDeviceId());
		if (detailed) {
			result.put("url", request.getResourceRef().getBaseRef() + "/hdb"
					+ controller.getPath());
			// Parent Info
//			if (controller.getParent() != null) {
//				JSONObject parentResult = new JSONObject();
//				parentResult.put("id", controller.getParent().getId());
//				parentResult.put("path", controller.getParent().getPath());
//				parentResult.put("url", request.getResourceRef().getBaseRef()
//						+ "/hdb" + controller.getParent().getPath());
//				result.put("parent", parentResult);
//			}
			// Children info
			JSONArray childrenResult = new JSONArray();
			for (ISicsController child : controller.getChildren()) {
				JSONObject childResult = new JSONObject();
				childResult.put("id", child.getId());
				childResult.put("path", child.getPath());
				childResult.put("url", request.getResourceRef().getBaseRef()
						+ "/hdb" + child.getPath());
				childrenResult.put(childResult);
			}
			result.put("children", childrenResult);
		}
		if (controller instanceof IDynamicController) {
			IDynamicController dynamicController = (IDynamicController) controller;
			// Get device id
			if (dynamicController.getDeviceId() != null) {
				result.put("dataType", dynamicController.getDeviceId());
			}
			// Get data type
			if (dynamicController.getModel() != null) {
				result.put("dataType", dynamicController.getControllerDataValue().getDataType().toString());
			}
			// Get data
//			final ISicsData[] dataHolder = new ISicsData[1];
//			dynamicController.getCurrentValue(new ControllerCallbackAdapter() {
//				public void getCurrentValue(ISicsData data) {
//					dataHolder[0] = data;
//				}
//			});
//			LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
//				@Override
//				public boolean getExitCondition() {
//					return dataHolder[0] != null;
//				}
//			}, 2000);
			Object val = dynamicController.getValue();
			if (val != null) {
				result.put("value", val.toString());
			} else {
				result.put("value", "ERROR");
			}
		}
		// Time stamp
		result.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(Calendar.getInstance().getTime()));
		return result;
	}
	
	private JSONObject createComponentJSONRepresentationWithProp(Request request,
			ISicsController controller, boolean detailed) throws Exception {
		JSONObject result = createComponentJSONRepresentation(request, controller, detailed);
		for (String name : PROP_COLLECTED) {
			String value = null;
			List<String> valueList = controller.getPropertyValue(name);
			if (valueList != null && valueList.size() > 0) {
				value = valueList.get(0);
			}
			if (value != null) {
				result.put(name, value.trim());
			}
		}
		ISicsController nickController = ModelUtils.getNicknameController(controller);
		if (nickController != null) {
			if (nickController instanceof IDynamicController) {
				result.put(PROP_NICK, ((IDynamicController) nickController).getValue());
			}
		}

//		ISicsController nickController = SicsModelUtils.getNicknameController(getSicsManager(), controller);
//		if (nickController != null) {
//			if (nickController instanceof IDynamicController) {
//				((IDynamicController) nickController).addChild(child);
//			}
//		}
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
	
}
