package org.gumtree.control.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public interface ISicsOutputData {
	String getString() throws JSONException ;

	JSONArray getArray();

	JSONObject getFullData();
	
}
