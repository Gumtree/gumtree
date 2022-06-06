package org.gumtree.control.imp.client;

import org.json.JSONObject;

public interface IClientListener {

	public void processMessage(JSONObject json);

}
