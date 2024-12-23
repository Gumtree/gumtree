package au.gov.ansto.bragg.kookaburra.webserver.restlet;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import au.gov.ansto.bragg.kookaburra.webserver.internal.Activator;


@SuppressWarnings("restriction")
public class KookaburraRestletApplication extends Application {

	private IEclipseContext context;

	public KookaburraRestletApplication(Context parentContext) {
		super(parentContext);
		context = Activator.getDefault().getEclipseContext()
				.createChild("kookaburraRestlet");
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
		KookaburraRestlet restlet = ContextInjectionFactory.make(
				KookaburraRestlet.class, context);

		router.attachDefault(restlet);

		return router;
	}

}
