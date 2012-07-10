package org.gumtree.gumnix.sics.simulator.services;

public class NormalProtocol implements ISicsPrototocol {

	public String formatOutput(int connectionId, ISicsOutput output) {
		return output.getOutputObject().toString();
	}

}
