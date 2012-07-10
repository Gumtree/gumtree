package org.gumtree.gumnix.sics.simulator.services;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONProtocol implements ISicsPrototocol {

	public String formatOutput(int connectionId, ISicsOutput output) {
		JSONObject json = new JSONObject();
		try {
			json.put("con", connectionId);
			json.put("trans", output.getContextId());
			json.put("object", output.getObjectId());
			json.put("flag", output.getFlag().toString());
			if(output.getOutputObject() instanceof JSONObject) {
				json.put("data", (JSONObject)output.getOutputObject());
			} else {
				json.put("data", output.getOutputObject().toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

}
