package org.gumtree.gumnix.sics.simulator.objects.commands;

import java.util.List;

import org.gumtree.gumnix.sics.simulator.objects.SicsCommand;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection;

public class Connection extends SicsCommand {

//	private ISicsConnection connection;

	public Connection(String id, ISicsConnection connection) {
		super(id);
//		this.connection = connection;
	}

	public void processCommand(List<String> args, ISicsConnection conn, int contextId) {
		// Do nothing
	}

}
