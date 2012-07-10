package org.gumtree.dae.server.restlet;

import org.gumtree.util.jmx.ExtendedMBeanExporter;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class DaeRestletApplication extends Application {

	public DaeRestletApplication(Context parentContext) {
		super(parentContext);
	}

	/**
	 * Creates a root Restlet that will receive all incoming calls.
	 */
	@Override
	public synchronized Restlet createRoot() {
		// Create a router Restlet that routes each call to a
		// new instance of HelloWorldResource.
		Router router = new Router(getContext());

		// Defines only one route
		final DaeRestlet restlet = new DaeRestlet();
		router.attachDefault(restlet);

		// Register MBean in asynchronously
		Thread registerThread = new Thread(new Runnable() {
			public void run() {
				ExtendedMBeanExporter exporter = new ExtendedMBeanExporter();
				exporter.addBeanProvider(restlet);
				exporter.afterPropertiesSet();
			}
		});
		registerThread.start();

		return router;
	}

}
