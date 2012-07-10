package org.gumtree.gumnix.sics.simulator.objects;

import java.util.List;

import org.gumtree.gumnix.sics.simulator.services.ISicsConnection;

public interface ISicsObject {

	public String getId();

	public void processCommand(List<String> args, ISicsConnection conn, int contextId);

}
