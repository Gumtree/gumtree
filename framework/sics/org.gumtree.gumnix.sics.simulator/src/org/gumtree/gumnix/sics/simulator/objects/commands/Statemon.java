package org.gumtree.gumnix.sics.simulator.objects.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gumtree.gumnix.sics.simulator.objects.IStatemon;
import org.gumtree.gumnix.sics.simulator.objects.SicsCommand;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection;
import org.gumtree.gumnix.sics.simulator.services.SicsOutput;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection.Flag;

public class Statemon extends SicsCommand implements IStatemon {

	private Map<ISicsConnection, Integer> callbackConnection;

	private Map<ISicsConnection, Integer> hdbCallbackConnection;

	public Statemon() {
		super(ID);
		callbackConnection = new HashMap<ISicsConnection, Integer>();
		hdbCallbackConnection = new HashMap<ISicsConnection, Integer>();
	}

	public void processCommand(List<String> args, ISicsConnection conn, int contextId) {
		if(args.size() == 1) {
			if(args.get(0).equals("interest")) {
				callbackConnection.put(conn, contextId);
			} else if(args.get(0).equals("hdbinterest")) {
				hdbCallbackConnection.put(conn, contextId);
			}
			conn.write(new SicsOutput(contextId, ID, Flag.status, "OK"));
		}
	}

	public synchronized void interestCallback(Object data) {
		for(Entry<ISicsConnection, Integer> entry : callbackConnection.entrySet()) {
			entry.getKey().write(new SicsOutput(entry.getValue(), ID, Flag.warning, data));
		}
	}

	public synchronized void hdbInterestCallback(Object data) {
		for(Entry<ISicsConnection, Integer> entry : hdbCallbackConnection.entrySet()) {
			entry.getKey().write(new SicsOutput(entry.getValue(), ID, Flag.warning, data));
		}
	}

}
