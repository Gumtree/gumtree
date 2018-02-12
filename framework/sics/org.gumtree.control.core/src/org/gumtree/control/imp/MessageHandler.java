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
				try {
					if (json.has(SicsChannel.JSON_KEY_STATUS)) {
						String status = json.getString(SicsChannel.JSON_KEY_STATUS);
						SicsManager.getSicsProxy().setServerStatus(ServerStatus.parseStatus(status));
					}
				} catch (JSONException e) {
				}
				try {
					String type = json.getString(PropertyConstants.PROP_MESSAGE_TYPE);
					System.out.println("subscribe " + json);
					if (type.equals(MessageType.UPDATE.getId())) {
						processUpdate(json.getString(PropertyConstants.PROP_UPDATE_PATH), 
								json.getString(PropertyConstants.PROP_UPDATE_VALUE));
					} else if (type.equals(MessageType.STATE.getId())) {
						processState(json.getString(PropertyConstants.PROP_UPDATE_PATH), 
								json.getString(PropertyConstants.PROP_UPDATE_VALUE));
					}
				} catch (JSONException e) {
				}
			}
		});
	}

	public void process(JSONObject json) {
		try {
			if (json.has(SicsChannel.JSON_KEY_STATUS)) {
				String status = json.getString(SicsChannel.JSON_KEY_STATUS);
				SicsManager.getSicsProxy().setServerStatus(ServerStatus.parseStatus(status));
			}
		} catch (JSONException e) {
		}
		try {
			String type = json.getString(PropertyConstants.PROP_MESSAGE_TYPE);
//			System.out.println("command " + json);
			if (type.equals(MessageType.UPDATE.getId())) {
				processUpdate(json.getString(PropertyConstants.PROP_UPDATE_PATH), 
						json.getString(PropertyConstants.PROP_UPDATE_VALUE));
			} else if (type.equals(MessageType.STATE.getId())) {
				processState(json.getString(PropertyConstants.PROP_UPDATE_PATH), 
						json.getString(PropertyConstants.PROP_UPDATE_VALUE));
			}
		} catch (JSONException e) {
		}
	}

	public void processUpdate(String path, String value) {
		ISicsController controller = getModel().findControllerByPath(path);
		try {
			((IDynamicController) controller).updateModelValue(value);
		} catch (SicsModelException e) {
			// TODO Auto-generated catch block
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
