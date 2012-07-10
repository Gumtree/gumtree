package org.gumtree.gumnix.sics.simulator.objects.commands;

import java.util.List;

import org.gumtree.gumnix.sics.simulator.objects.IHNotify;
import org.gumtree.gumnix.sics.simulator.objects.SicsCommand;
import org.gumtree.gumnix.sics.simulator.server.SicsSimulationServer;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection.Flag;
import org.gumtree.gumnix.sics.simulator.services.SicsOutput;
import org.json.JSONException;
import org.json.JSONObject;

public class HSet extends SicsCommand {

	private static final String ID = "hset";

	public HSet() {
		super(ID);
	}

	public void processCommand(List<String> args, ISicsConnection conn, int contextId) {
		if(args.size() == 2) {
			conn.write(new SicsOutput(contextId, ID, Flag.status, "OK"));
			IHNotify notify = (IHNotify)SicsSimulationServer.getDefault().getObjectLibrary().getSicsObject(IHNotify.ID);
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put(args.get(0), args.get(1));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// Stress test: reply 10000 times to see if GumTree can handle this
//			for(int i = 0; i < 5000; i++) {
				notify.callback(jsonObject);
//			}
		}
	}

}
