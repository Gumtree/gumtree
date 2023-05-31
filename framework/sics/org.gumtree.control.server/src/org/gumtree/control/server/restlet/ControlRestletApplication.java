package org.gumtree.control.server.restlet;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.gumtree.control.server.Activator;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class ControlRestletApplication extends Application {

	private IEclipseContext context;

	public ControlRestletApplication(Context parentContext) {
		super(parentContext);
		context = Activator.getDefault().getEclipseContext()
				.createChild("sicsRestlet");
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
		ControlRestlet restlet = ContextInjectionFactory.make(ControlRestlet.class,
				context);
		
//		// Temp: login here
//		try {
//			restlet.getSicsManager()
//					.getProxy()
//					.login(new SicsConnectionContext("ics1-test.nbi.ansto.gov.au", 60002,
//							SicsRole.SPY, "007"));
//		} catch (SicsIOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SicsExecutionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		router.attachDefault(restlet);

		return router;
	}

}
