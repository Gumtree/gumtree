/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.catalog;

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
public class CatalogApplication extends Application {

	private IEclipseContext context;

	/**
	 * 
	 */
	public CatalogApplication() {
		super();
	}

	/**
	 * @param context
	 */
	public CatalogApplication(Context parentContext) {
		super(parentContext);
		context = Activator.getDefault().getEclipseContext()
				.createChild("catalogDocRestlet").createChild("catalogAdminRestlet");
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
		CatalogRestlet restlet = ContextInjectionFactory.make(
				CatalogRestlet.class, context);
		router.attachDefault(restlet);
		return router;
	}

}
