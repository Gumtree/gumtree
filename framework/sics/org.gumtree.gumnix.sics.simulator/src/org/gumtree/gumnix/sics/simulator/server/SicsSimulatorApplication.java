package org.gumtree.gumnix.sics.simulator.server;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class SicsSimulatorApplication implements IApplication {

	public Object start(IApplicationContext context) throws Exception {
		SicsSimulationServer.main(new String[0]);
		return IApplication.EXIT_OK;
	}

	public void stop() {
	}

}
