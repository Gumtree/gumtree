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
public class NBManagerRedirectApplication extends Application {

	private IEclipseContext context;

	/**
	 * 
	 */
	public NBManagerRedirectApplication() {
		super();
	}

	/**
	 * @param context
	 */
	public NBManagerRedirectApplication(Context parentContext) {
		super(parentContext);
		context = Activator.getDefault().getEclipseContext()
				.createChild("NBRedirectRestlet");
	}

	/**
	 * Creates a root Restlet that will receive all incoming calls.
	 */
	@Override
	public synchronized Restlet createInboundRoot() {
		// Create a router Restlet that routes each call to a
		// new instance of HelloWorldResource.
		Router router = new Router(getContext());

		// Defines only one route
		NBManagerRedirectRestlet redirectRestlet = ContextInjectionFactory.make(
				NBManagerRedirectRestlet.class, context);
		router.attachDefault(redirectRestlet);
		return router;
	}

}
