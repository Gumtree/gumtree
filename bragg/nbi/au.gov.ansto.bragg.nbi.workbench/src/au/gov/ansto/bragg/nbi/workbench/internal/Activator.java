package au.gov.ansto.bragg.nbi.workbench.internal;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.gumtree.util.eclipse.E4Utils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

@SuppressWarnings("restriction")
public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "au.gov.ansto.bragg.nbi.workbench";
	
	private static BundleContext context;

	private static Activator instance;
	
	private volatile IEclipseContext eclipseContext;
	
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		instance = this;
		context = bundleContext;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		if (InternalImage.isInstalled()) {
			InternalImage.dispose();
		}
		context = null;
		instance = null;
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
