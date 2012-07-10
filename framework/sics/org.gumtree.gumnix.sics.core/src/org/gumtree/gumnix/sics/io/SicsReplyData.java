package org.gumtree.gumnix.sics.io;

import org.gumtree.gumnix.sics.internal.io.SicsCommunicationConstants.JSONTag;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SicsReplyData implements ISicsReplyData {

	private JSONObject data;

	public SicsReplyData(JSONObject data) {
		this.data = data;
	}

	public String getString() {
		try {
			return data.getString(JSONTag.DATA.getText());
		} catch (JSONException e) { }
		return null;
	}

	public Boolean getBoolean() {
		try {
			return data.getBoolean(JSONTag.DATA.getText());
		} catch (JSONException e) { }
		return null;
	}

	public Integer getInteger() {
		try {
			return data.getInt(JSONTag.DATA.getText());
		} catch (JSONException e) { }
		return null;
	}

	public Double getDouble() {
		try {
			return data.getDouble(JSONTag.DATA.getText());
		} catch (JSONException e) { }
		return null;
	}

	public Long getLong() {
		try {
			return data.getLong(JSONTag.DATA.getText());
		} catch (JSONException e) { }
		return null;
	}

	public JSONArray getArray() {
		try {
			return data.getJSONArray(JSONTag.DATA.getText());
		} catch (JSONException e) { }
		return null;
	}

	public JSONObject getJSONObject() {
		try {
			return data.getJSONObject(JSONTag.DATA.getText());
		} catch (JSONException e) {
//			logger.error("Cannot get data from json object", e);
		}
		return null;
	}

	public JSONObject getFullReply() {
		return data;
	}

	public String toString() {
		return "SicsReplyData: " + data.toString();
	}
	
}
