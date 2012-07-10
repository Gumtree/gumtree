package org.gumtree.gumnix.sics.simulator.objects.commands;

import java.util.List;

import org.gumtree.gumnix.sics.simulator.objects.ISicsObject;
import org.gumtree.gumnix.sics.simulator.objects.SicsCommand;
import org.gumtree.gumnix.sics.simulator.server.SicsSimulationServer;
import org.gumtree.gumnix.sics.simulator.services.ISicsConnection;

public class ContextDo extends SicsCommand {

	private static final String ID = "contextdo";

	public ContextDo() {
		super(ID);
	}

	public void processCommand(List<String> args, ISicsConnection conn, int contextId) {
		if(args.size() >= 2) {
			int newContextId = Integer.parseInt(args.get(0));
			String objectId = args.get(1);
			ISicsObject sicsObject = SicsSimulationServer.getDefault().getObjectLibrary().getSicsObject(objectId);
			if(sicsObject != null) {
				args.remove(0);
				args.remove(0);
				SicsSimulationServer.getDefault().getLogger().debug("Passed input from contextdo to object " + sicsObject.getId());
				sicsObject.processCommand(args, conn, newContextId);
			}
		}
	}

}
