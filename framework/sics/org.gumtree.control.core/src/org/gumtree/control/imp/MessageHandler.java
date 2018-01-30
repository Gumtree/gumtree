package org.gumtree.control.imp;

import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.model.PropertyConstants;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageHandler {

	private ISicsModel model;
	
	public MessageHandler() {
	}
	
	public void process(JSONObject json) {
		try {
			String type = json.getString(PropertyConstants.PROP_MESSAGE_TYPE);
			if (type.equals(PropertyConstants.PROP_TYPE_UPDATE)) {
				processUpdate(json.getString(PropertyConstants.PROP_UPDATE_PATH), 
						json.getString(PropertyConstants.PROP_UPDATE_VALUE));
			}
		} catch (JSONException e) {
//			e.printStackTrace();
		}
	}
	
	public void processUpdate(String path, String value) {
		ISicsController controller = getModel().findControllerByPath(path);
		((IDynamicController) controller).updateValue(value);
	}

	private ISicsModel getModel() {
		if (model == null) {
			model = SicsManager.getSicsModel();
		}
		return model;
	}
}
