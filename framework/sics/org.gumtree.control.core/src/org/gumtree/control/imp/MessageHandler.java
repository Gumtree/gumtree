package org.gumtree.control.imp;

import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ThreadPool;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.model.PropertyConstants;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.control.model.PropertyConstants.MessageType;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageHandler {

	private ISicsModel model;
	private ThreadPool threadPool;
	
	public MessageHandler() {
		threadPool = new ThreadPool();
	}
	
	public void delayedProcess(JSONObject json) {
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
		SicsManager.getSicsProxy().fireMessageEvent(json.toString());
	}

	public void process(JSONObject json) {
//		System.out.println("process " + json);
		try {
			if (json.has(SicsChannel.JSON_KEY_STATUS)) {
				String status = json.getString(SicsChannel.JSON_KEY_STATUS);
				SicsManager.getSicsProxy().setServerStatus(ServerStatus.parseStatus(status));
			}
		} catch (JSONException e) {
		}
		try {
			String type = json.getString(PropertyConstants.PROP_MESSAGE_TYPE);
			if (type.equals(MessageType.UPDATE.getId())) {
				processUpdate(json.getString(PropertyConstants.PROP_UPDATE_PATH), 
						json.getString(PropertyConstants.PROP_UPDATE_DATA));
			} else if (type.equals(MessageType.STATE.getId())) {
				processState(json.getString(PropertyConstants.PROP_UPDATE_PATH), 
						json.getString(PropertyConstants.PROP_UPDATE_DATA));
			} else if (type.equals(MessageType.BATCH.getId())) {
				processBatch(json);
			}
		} catch (JSONException e) {
		}
	}

	public void processBatch(JSONObject json) {
		try {
			if (json.has(PropertyConstants.PROP_BATCH_NAME)) {
				SicsManager.getBatchControl().fireBatchEvent(PropertyConstants.PROP_BATCH_NAME, 
						json.getString(PropertyConstants.PROP_BATCH_NAME));
			}
			if (json.has(PropertyConstants.PROP_BATCH_RANGE)) {
				SicsManager.getBatchControl().fireBatchEvent(PropertyConstants.PROP_BATCH_RANGE, 
						json.getString(PropertyConstants.PROP_BATCH_RANGE));
			}
			if (json.has(PropertyConstants.PROP_BATCH_TEXT)) {
				SicsManager.getBatchControl().fireBatchEvent(PropertyConstants.PROP_BATCH_TEXT, 
						json.getString(PropertyConstants.PROP_BATCH_TEXT));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void processUpdate(String path, String value) {
		ISicsController controller = getModel().findControllerByPath(path);
		try {
			((IDynamicController) controller).updateModelValue(value);
		} catch (SicsModelException e) {
		}
	}

	public void processState(String path, String state) {
		ISicsController controller = getModel().findControllerByPath(path);
		controller.setState(ControllerState.valueOf(state));
	}

	private ISicsModel getModel() {
		if (model == null) {
			model = SicsManager.getSicsModel();
		}
		return model;
	}
}
