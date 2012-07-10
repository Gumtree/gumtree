package org.gumtree.gumnix.sics.simulator.server;

import org.gumtree.gumnix.sics.simulator.services.ISicsObjectLibrary;
import org.json.JSONObject;
import org.slf4j.Logger;

public interface ISicsSimulationServer {

	public JSONObject getProperties();

	public ISicsObjectLibrary getObjectLibrary();

	public Logger getLogger();

}
