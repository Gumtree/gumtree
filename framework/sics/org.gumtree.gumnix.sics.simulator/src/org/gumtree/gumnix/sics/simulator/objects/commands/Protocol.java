package org.gumtree.gumnix.sics.simulator.objects.commands;

import java.util.List;

import org.gumtree.gumnix.sics.simulator.objects.SicsCommand;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection;
import org.gumtree.gumnix.sics.simulator.services.JSONProtocol;
import org.gumtree.gumnix.sics.simulator.services.SicsOutput;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection.Flag;

public class Protocol extends SicsCommand {

	private static final String ID = "protocol";

	public Protocol() {
		super(ID);
	}

	public void processCommand(List<String> args, ISicsConnection conn, int contextId) {
		if(args.size() == 2) {
			if(args.get(0).equals("set") && args.get(1).equals("json")) {
				conn.setProtocol(new JSONProtocol());
				conn.write(new SicsOutput(contextId, ID, Flag.status, "OK"));
			}
		}
	}

}
