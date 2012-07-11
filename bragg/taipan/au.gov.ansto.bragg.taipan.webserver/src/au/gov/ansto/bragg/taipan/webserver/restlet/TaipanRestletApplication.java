package au.gov.ansto.bragg.taipan.webserver.restlet;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import au.gov.ansto.bragg.taipan.webserver.internal.Activator;

@SuppressWarnings("restriction")
public class TaipanRestletApplication extends Application {

	private IEclipseContext context;

	public TaipanRestletApplication(Context parentContext) {
		super(parentContext);
		context = Activator.getDefault().getEclipseContext()
				.createChild("taipanRestlet");
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
		TaipanRestlet restlet = ContextInjectionFactory.make(
				TaipanRestlet.class, context);

		router.attachDefault(restlet);

		return router;
	}

}
