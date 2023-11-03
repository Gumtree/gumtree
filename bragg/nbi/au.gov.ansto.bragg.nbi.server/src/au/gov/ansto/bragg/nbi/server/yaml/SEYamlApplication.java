/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.yaml;

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
public class SEYamlApplication extends Application {

	private IEclipseContext context;

	/**
	 * 
	 */
	public SEYamlApplication() {
		super();
	}

	/**
	 * @param context
	 */
	public SEYamlApplication(Context parentContext) {
		super(parentContext);
		context = Activator.getDefault().getEclipseContext()
				.createChild("SEYamlRestlet");
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
		SEYamlRestlet restlet = ContextInjectionFactory.make(
				SEYamlRestlet.class, context);
		router.attachDefault(restlet);
		return router;
	}

}
