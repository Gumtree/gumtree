package org.gumtree.control.imp;

import org.gumtree.control.core.ISicsOutputData;
import org.gumtree.control.core.SicsCommunicationConstants.Flag;
import org.gumtree.control.core.SicsCommunicationConstants.JSONTag;
import org.gumtree.control.model.PropertyConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SicsOutputData implements ISicsOutputData {

	private JSONObject data;

	public SicsOutputData(JSONObject data) {
		this.data = data;
	}

	public String getString() throws JSONException {
			String res = "";
			JSONArray arr = (JSONArray) data.get(JSONTag.OUTPUT.getText());
			for (int i = 0; i < arr.length(); i++) {
				res += arr.getString(i);
				if (i < arr.length() - 1) {
					res += "\n";
				}
			}
			return res;
	}

	public JSONArray getArray() {
		try {
			return data.getJSONArray(JSONTag.OUTPUT.getText());
		} catch (JSONException e) { }
		return null;
	}

	public JSONObject getFullData() {
		return data;
	}

	public Flag getFlag() {
		try {
			return Flag.valueOf(data.getString(PropertyConstants.PROP_COMMAND_FLAG));
		} catch (JSONException e) {
		}
		return Flag.OUT;
	}
	
	public String toString() {
		return "SicsOutputData: " + data.toString();
	}
	
}
