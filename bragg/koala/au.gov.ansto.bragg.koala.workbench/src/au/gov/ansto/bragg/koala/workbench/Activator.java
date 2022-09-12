package au.gov.ansto.bragg.koala.workbench;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.gumtree.util.eclipse.E4Utils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "au.gov.ansto.bragg.koala.workbench"; //$NON-NLS-1$
	private static final String ENABLE_VERTUAL_SERVER = "gumtree.koala.virtualServer";

	private static BundleContext context;

	private static Activator instance;

	private volatile IEclipseContext eclipseContext;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		instance = this;
		context = bundleContext;
		boolean virtualServer = false;
		try {
			virtualServer = Boolean.valueOf(System.getProperty(ENABLE_VERTUAL_SERVER));
		} catch (Exception e) {
		}
		if (virtualServer) {
			KoalaServer server = new KoalaServer(ConstantSetup.LOCAL_SERVER_ADDRESS, ConstantSetup.LOCAL_PUBLISHER_ADDRESS);
	    	server.run();
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		if (eclipseContext != null) {
			eclipseContext.dispose();
			eclipseContext = null;
		}
		context = null;
		instance = null;
		super.stop(bundleContext);
	}

	public IEclipseContext getEclipseContext() {
		if (eclipseContext == null) {
			synchronized (this) {
				if (eclipseContext == null) {
					eclipseContext = E4Utils.createEclipseContext(getContext());
				}
			}
		}
		return eclipseContext;
	}

	public static BundleContext getContext() {
		return context;
	}

	public static Activator getDefault() {
		return instance;
	}


}
