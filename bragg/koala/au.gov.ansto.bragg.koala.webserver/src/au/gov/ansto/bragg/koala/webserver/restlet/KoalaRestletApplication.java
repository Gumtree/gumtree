package au.gov.ansto.bragg.koala.webserver.restlet;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import au.gov.ansto.bragg.koala.webserver.Activator;

@SuppressWarnings("restriction")
public class KoalaRestletApplication extends Application {

	private IEclipseContext context;

	public KoalaRestletApplication(Context parentContext) {
		super(parentContext);
		context = Activator.getDefault().getEclipseContext()
				.createChild("koalaRestlet");
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
		KoalaRestlet restlet = ContextInjectionFactory.make(
				KoalaRestlet.class, context);

		router.attachDefault(restlet);

		return router;
	}

}
