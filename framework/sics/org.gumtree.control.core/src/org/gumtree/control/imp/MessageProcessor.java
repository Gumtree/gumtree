package org.gumtree.control.imp;

import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.model.PropertyConstants;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.json.JSONObject;

public class MessageProcessor {

	public enum MessageType{
		STATUS,
		STATE,
		UPDATE
	}
	
	public MessageProcessor() {
	}
	
	public void process(String message) {
		JSONObject json;
		try {
			json = new JSONObject(message);
			String messageTypeString = json.get(PropertyConstants.PROP_MESSAGE_TYPE).toString();
			if (messageTypeString != null) {
				MessageType messageType = MessageType.valueOf(messageTypeString);
				switch (messageType) {
				case STATUS:
					
					break;
				case STATE:
					String path = json.get(PropertyConstants.PROP_UPDATE_PATH).toString();
					String value = json.get(PropertyConstants.PROP_UPDATE_VALUE).toString();
					if (path != null && value != null) {
						updateModelState(path, value);
					}
					break;
				case UPDATE:
					path = json.get(PropertyConstants.PROP_UPDATE_PATH).toString();
					value = json.get(PropertyConstants.PROP_UPDATE_VALUE).toString();
					if (path != null && value != null) {
						updateModelValue(path, value);
					}
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateModelState(String path, String value) {
		ISicsModel model = SicsManager.getSicsModel();
		if (model != null) {
			ISicsController controller = model.findController(path);
			if (controller != null && controller instanceof IDynamicController) {
				try {
					((IDynamicController) controller).setState(ControllerState.getState(value));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void updateModelValue(String path, String value) {
		ISicsModel model = SicsManager.getSicsModel();
		if (model != null) {
			ISicsController controller = model.findController(path);
			if (controller != null && controller instanceof IDynamicController) {
				try {
					((IDynamicController) controller).updateModelValue(value);
				} catch (SicsModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
