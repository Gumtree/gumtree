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
public class DatabaseApplication extends Application {

	private IEclipseContext context;

	/**
	 * 
	 */
	public DatabaseApplication() {
		super();
	}

	/**
	 * @param context
	 */
	public DatabaseApplication(Context parentContext) {
		super(parentContext);
		context = Activator.getDefault().getEclipseContext()
				.createChild("databaseRestlet");
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
		DatabaseRestlet runnerRestlet = ContextInjectionFactory.make(
				DatabaseRestlet.class, context);
		router.attachDefault(runnerRestlet);
		return router;
	}

}
