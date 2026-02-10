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
public class NBPageRedirectApplication extends Application {

	private IEclipseContext context;

	/**
	 * 
	 */
	public NBPageRedirectApplication() {
		super();
	}

	/**
	 * @param context
	 */
	public NBPageRedirectApplication(Context parentContext) {
		super(parentContext);
		context = Activator.getDefault().getEclipseContext()
				.createChild("NBPageRedirectRestlet");
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
		NBPageRedirectRestlet redirectRestlet = ContextInjectionFactory.make(
				NBPageRedirectRestlet.class, context);
		router.attachDefault(redirectRestlet);
		return router;
	}

}
