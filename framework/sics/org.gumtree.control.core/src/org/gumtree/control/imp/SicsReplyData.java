package org.gumtree.control.imp;

import org.gumtree.control.core.ISicsReplyData;
import org.gumtree.control.core.SicsCommunicationConstants.JSONTag;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SicsReplyData implements ISicsReplyData {

	private JSONObject data;
	public final static String COMMAND_REPLY_DEFERRED = "DEFERRED";
	public final static String COMMAND_REPLY_RUNNING = "RUNNING";

	public SicsReplyData(JSONObject data) {
		this.data = data;
	}

	public String getString() {
		try {
			return data.getString(JSONTag.REPLY.getText());
		} catch (JSONException e) { }
		return null;
	}

	public Boolean getBoolean() {
		try {
			return data.getBoolean(JSONTag.REPLY.getText());
		} catch (JSONException e) { }
		return null;
	}

	public Integer getInteger() {
		try {
			return data.getInt(JSONTag.REPLY.getText());
		} catch (JSONException e) { }
		return null;
	}

	public Double getDouble() {
		try {
			return data.getDouble(JSONTag.REPLY.getText());
		} catch (JSONException e) { }
		return null;
	}

	public Long getLong() {
		try {
			return data.getLong(JSONTag.REPLY.getText());
		} catch (JSONException e) { }
		return null;
	}

	public JSONArray getArray() {
		try {
			return data.getJSONArray(JSONTag.REPLY.getText());
		} catch (JSONException e) { }
		return null;
	}

	public JSONObject getJSONObject() {
		try {
			return data.getJSONObject(JSONTag.REPLY.getText());
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
