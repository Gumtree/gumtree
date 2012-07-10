package org.gumtree.gumnix.sics.simulator.objects.commands;

import java.util.List;

import org.gumtree.gumnix.sics.simulator.objects.SicsCommand;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection;
import org.gumtree.gumnix.sics.simulator.services.SicsOutput;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection.Flag;
import org.json.JSONException;
import org.json.JSONObject;

public class HGet extends SicsCommand {

	private static final String ID = "hget";

	private static float i = 0;

	public HGet() {
		super(ID);
	}

	public void processCommand(List<String> args, ISicsConnection conn, int contextId) {
		if(args.size() == 1) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put(args.get(0), i++);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			conn.write(new SicsOutput(contextId, ID, Flag.hdbevent, jsonObject));
		}
	}

}
