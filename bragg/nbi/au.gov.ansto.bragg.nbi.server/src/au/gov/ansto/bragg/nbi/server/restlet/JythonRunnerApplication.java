/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.restlet;

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
public class JythonRunnerApplication extends Application {

	private IEclipseContext context;

	/**
	 * 
	 */
	public JythonRunnerApplication() {
		super();
	}

	/**
	 * @param context
	 */
	public JythonRunnerApplication(Context parentContext) {
		super(parentContext);
		context = Activator.getDefault().getEclipseContext()
				.createChild("jythonRunnerRestlet");
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
		JythonRunnerRestlet runnerRestlet = ContextInjectionFactory.make(
				JythonRunnerRestlet.class, context);
		router.attachDefault(runnerRestlet);
		return router;
	}

}
