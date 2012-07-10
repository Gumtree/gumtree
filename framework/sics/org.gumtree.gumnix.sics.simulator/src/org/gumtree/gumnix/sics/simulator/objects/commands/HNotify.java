package org.gumtree.gumnix.sics.simulator.objects.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gumtree.gumnix.sics.simulator.objects.IHNotify;
import org.gumtree.gumnix.sics.simulator.objects.SicsCommand;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection;
import org.gumtree.gumnix.sics.simulator.services.SicsOutput;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection.Flag;

public class HNotify extends SicsCommand implements IHNotify {

	private Map<ISicsConnection, Integer> callbackConnection;

	public HNotify() {
		super(ID);
		callbackConnection = new HashMap<ISicsConnection, Integer>();
	}

	public void processCommand(List<String> args, ISicsConnection conn, int contextId) {
		if(args.size() == 2) {
			callbackConnection.put(conn, contextId);
			conn.write(new SicsOutput(contextId, ID, Flag.status, "OK"));
		}
	}

	public synchronized void callback(Object data) {
		for(Entry<ISicsConnection, Integer> entry : callbackConnection.entrySet()) {
			entry.getKey().write(new SicsOutput(entry.getValue(), ID, Flag.hdbevent, data));
		}
	}

}
