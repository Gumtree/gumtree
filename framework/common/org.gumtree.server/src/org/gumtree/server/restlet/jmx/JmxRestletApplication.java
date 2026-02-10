package org.gumtree.server.restlet.jmx;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

@Deprecated
public class JmxRestletApplication extends Application {

	public JmxRestletApplication(Context parentContext) {
		super(parentContext);
	}

	@Override
	public Restlet createInboundRoot() {
		// Create a router Restlet that routes each call to a
		Router router = new Router(getContext());

		// Defines only one route
		router.attachDefault(new JmxRestlet());

		return router;
	}

}
