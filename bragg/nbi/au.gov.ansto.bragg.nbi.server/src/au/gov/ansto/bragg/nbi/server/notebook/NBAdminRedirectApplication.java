/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.notebook;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import au.gov.ansto.bragg.nbi.server.internal.Activator;

/**
 * @author nxi
 *
 */
public class NBAdminRedirectApplication extends Application {

	private IEclipseContext context;

	/**
	 * 
	 */
	public NBAdminRedirectApplication() {
		super();
	}

	/**
	 * @param context
	 */
	public NBAdminRedirectApplication(Context parentContext) {
		super(parentContext);
		context = Activator.getDefault().getEclipseContext()
				.createChild("NBAdminRedirectRestlet");
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
		NBAdminRedirectRestlet redirectRestlet = ContextInjectionFactory.make(
				NBAdminRedirectRestlet.class, context);
		router.attachDefault(redirectRestlet);
		return router;
	}

}
