package org.gumtree.gumnix.sics.simulator.objects.commands;

import java.util.List;

import org.gumtree.gumnix.sics.simulator.objects.SicsCommand;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection.Flag;
import org.gumtree.gumnix.sics.simulator.services.SicsOutput;

public class Status extends SicsCommand {

	private static final String ID = "status";

	public Status() {
		super(ID);
	}

	public void processCommand(List<String> args, ISicsConnection conn, int contextId) {
		if(args.size() == 0) {
			conn.write(new SicsOutput(contextId, ID, Flag.status, "Eager to execute"));
		}
	}

}
