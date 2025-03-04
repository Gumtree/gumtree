package org.gumtree.control.imp.client;

import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.events.ThreadPool;
import org.gumtree.control.imp.SicsProxy;
import org.gumtree.control.model.PropertyConstants;
import org.gumtree.control.model.PropertyConstants.MessageType;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientMessageHandler {

	private static Logger logger = LoggerFactory.getLogger(ClientMessageHandler.class);
	private ISicsModel model;
	private ThreadPool threadPool;
	private ISicsProxy sicsProxy;
	
	public ClientMessageHandler(ISicsProxy sicsProxy) {
		this.sicsProxy = sicsProxy;
		threadPool = new ThreadPool();
	}
	
	public void delayedProcess(final JSONObject json) {
		threadPool.run(new Runnable() {
			
			@Override
			public void run() {
//				try {
//					if (json.has(SicsChannel.JSON_KEY_STATUS)) {
//						String status = json.getString(SicsChannel.JSON_KEY_STATUS);
//						SicsManager.getSicsProxy().setServerStatus(ServerStatus.parseStatus(status));
//					}
//				} catch (JSONException e) {
//				}
//				try {
//					String type = json.getString(PropertyConstants.PROP_MESSAGE_TYPE);
//					System.out.println("subscribe " + json);
//					if (type.equals(MessageType.UPDATE.getId())) {
//						processUpdate(json.getString(PropertyConstants.PROP_UPDATE_PATH), 
//								json.getString(PropertyConstants.PROP_UPDATE_DATA));
//					} else if (type.equals(MessageType.STATE.getId())) {
//						processState(json.getString(PropertyConstants.PROP_UPDATE_PATH), 
//								json.getString(PropertyConstants.PROP_UPDATE_DATA));
//					} 
//				} catch (JSONException e) {
//				}
//				System.out.println("delayed");
				logger.warn(json.toString());
				process(json);
			}
		});
		sicsProxy.fireMessageEvent(json);
	}

	public void process(final JSONObject json) {
//		System.out.println("process " + json);
//		try {
//			if (json.has(SicsChannel.JSON_KEY_STATUS)) {
//				String status = json.getString(SicsChannel.JSON_KEY_STATUS);
//				sicsProxy.setServerStatus(ServerStatus.parseStatus(status));
//			}
//		} catch (JSONException e) {
//		}
		try {
			if (json.has(PropertyConstants.PROP_UPDATE_TYPE)) {
				String type = json.getString(PropertyConstants.PROP_UPDATE_TYPE);
				if (type.equalsIgnoreCase(MessageType.STATUS.getId())) {
					String status = json.getString(PropertyConstants.PROP_UPDATE_VALUE);
//					logger.info("UPDATE status to " + status);
				} else if (type.equalsIgnoreCase(MessageType.VALUE.getId())) {
					String name = json.getString(PropertyConstants.PROP_UPDATE_NAME);
//					String value = json.getString(PropertyConstants.PROP_UPDATE_VALUE);
//					logger.info("UPDATE value " + name + " = " + value);
//					processUpdate(name, value);
				} else if (type.equalsIgnoreCase(MessageType.STATE.getId())) {
					String name = json.getString(PropertyConstants.PROP_UPDATE_VALUE);
					String state = json.getString(PropertyConstants.PROP_UPDATE_NAME);
//					logger.info("UPDATE state of " + name + " to " + state);
//					processState(name, state);
				} else if (type.equalsIgnoreCase(MessageType.BATCH.getId())) {
//					processBatch(json);
				} else {
					logger.error(json.toString());
				}
			} else if (json.has(PropertyConstants.PROP_COMMAND_CMD)) {
				logger.info("PROCESS command message");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

//	public void processBatch(final JSONObject json) {
//		try {
//			if (json.has(PropertyConstants.PROP_BATCH_NAME)) {
//			}
//			if (json.has(PropertyConstants.PROP_BATCH_RANGE)) {
//			}
//			if (json.has(PropertyConstants.PROP_BATCH_TEXT)) {
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
//	public void processUpdate(String path, String value) {
//		ISicsController controller = getModel().findController(path);
//		try {
//			((IDynamicController) controller).updateModelValue(value);
//		} catch (SicsModelException e) {
//			logger.error("failed to update value of " + path + " " + value);
//		}
//	}

//	public void processState(String path, String state) {
//		ISicsController controller = getModel().findController(path);
//		try {
//			controller.setState(ControllerState.getState(state));
//		} catch (Exception e) {
//			logger.error("failed to set state for " + path + " " + state);
//		}
//	}

//	private ISicsModel getModel() {
//		if (model == null) {
//			model = sicsProxy.getSicsModel();
//		}
//		return model;
//	}
}
