package org.gumtree.control.imp;

import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.events.ThreadPool;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.model.PropertyConstants;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.control.model.PropertyConstants.MessageType;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageHandler {

	private static Logger logger = LoggerFactory.getLogger(MessageHandler.class);
	private ISicsModel model;
	private ThreadPool threadPool;
	private SicsProxy sicsProxy;
	
	public MessageHandler(SicsProxy sicsProxy) {
		threadPool = new ThreadPool();
		this.sicsProxy = sicsProxy;
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
				process(json);
			}
		});
		sicsProxy.fireMessageEvent(json.toString());
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
					logger.info("UPDATE status to " + status);
					sicsProxy.setServerStatus(ServerStatus.parseStatus(status));
				} else if (type.equalsIgnoreCase(MessageType.VALUE.getId())) {
					String name = json.getString(PropertyConstants.PROP_UPDATE_NAME);
					String value = json.getString(PropertyConstants.PROP_UPDATE_VALUE);
					logger.info("UPDATE value " + name + " = " + value);
					processUpdate(name, value);
				} else if (type.equalsIgnoreCase(MessageType.STATE.getId())) {
					String name = json.getString(PropertyConstants.PROP_UPDATE_VALUE);
					String state = json.getString(PropertyConstants.PROP_UPDATE_NAME);
					logger.info("UPDATE state of " + name + " to " + state);
					processState(name, state);
				} else if (type.equalsIgnoreCase(MessageType.BATCH.getId())) {
					processBatch(json);
				}
			} else if (json.has(PropertyConstants.PROP_COMMAND_CMD)) {
				logger.info("PROCESS command message");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void processBatch(final JSONObject json) {
		try {
			if (json.has(PropertyConstants.PROP_BATCH_NAME)) {
				sicsProxy.getBatchControl().fireBatchEvent(PropertyConstants.PROP_BATCH_NAME, 
						json.getString(PropertyConstants.PROP_BATCH_NAME));
			}
			if (json.has(PropertyConstants.PROP_BATCH_RANGE)) {
				sicsProxy.getBatchControl().fireBatchEvent(PropertyConstants.PROP_BATCH_RANGE, 
						json.getString(PropertyConstants.PROP_BATCH_RANGE));
			}
			if (json.has(PropertyConstants.PROP_BATCH_TEXT)) {
				sicsProxy.getBatchControl().fireBatchEvent(PropertyConstants.PROP_BATCH_TEXT, 
						json.getString(PropertyConstants.PROP_BATCH_TEXT));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void processUpdate(String path, String value) {
		ISicsController controller = getModel().findController(path);
		try {
			((IDynamicController) controller).updateModelValue(value);
		} catch (SicsModelException e) {
		}
	}

	public void processState(String path, String state) {
		ISicsController controller = getModel().findController(path);
		controller.setState(ControllerState.getState(state));
	}

	private ISicsModel getModel() {
		if (model == null) {
			model = sicsProxy.getSicsModel();
		}
		return model;
	}
}
