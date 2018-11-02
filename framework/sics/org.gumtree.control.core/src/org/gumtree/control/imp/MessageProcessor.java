package org.gumtree.control.imp;

import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.model.PropertyConstants;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.control.model.PropertyConstants.MessageType;
import org.json.JSONObject;

public class MessageProcessor {

//	public enum MessageType{
//		STATUS,
//		STATE,
//		VALUE, 
//		BATCH,
//		UNKNOWN;
//		
//		public static MessageType fromString(String text) {
//			try {
//				return MessageType.valueOf(text.toUpperCase());
//			} catch (Exception e) {
//				return MessageType.UNKNOWN;
//			}
//		}
//	}
	
	public MessageProcessor() {
	}
	
	public void process(String message) {
		JSONObject json;
		try {
			json = new JSONObject(message);
			String messageTypeString = json.get(PropertyConstants.PROP_UPDATE_TYPE).toString();
			if (messageTypeString != null) {
				MessageType messageType = MessageType.parseString(messageTypeString);
				switch (messageType) {
				case STATUS:
					
					break;
				case STATE:
					String path = json.get(PropertyConstants.PROP_UPDATE_VALUE).toString();
					String value = json.get(PropertyConstants.PROP_UPDATE_NAME).toString();
					if (path != null && value != null) {
						updateModelState(path, value);
					}
					break;
				case VALUE:
					path = json.get(PropertyConstants.PROP_UPDATE_NAME).toString();
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
		System.out.println(path + ", " + value);
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
		System.out.println(path + ", " + value);
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
