package au.gov.ansto.bragg.nbi.server.login;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import au.gov.ansto.bragg.nbi.server.internal.Activator;

public class UserHomeApplication extends Application {

	private IEclipseContext context;

	/**
	 * 
	 */
	public UserHomeApplication() {
		super();
	}

	/**
	 * @param context
	 */
	public UserHomeApplication(Context parentContext) {
		super(parentContext);
		context = Activator.getDefault().getEclipseContext()
				.createChild("userHomeRestlet");
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
		UserHomeRestlet runnerRestlet = ContextInjectionFactory.make(
				UserHomeRestlet.class, context);
		router.attachDefault(runnerRestlet);
		return router;
	}

}