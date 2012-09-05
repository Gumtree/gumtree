package au.gov.ansto.bragg.pelican.workbench.internal;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.gumtree.util.eclipse.E4Utils;
import org.osgi.framework.BundleContext;

@SuppressWarnings("restriction")
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "au.gov.ansto.bragg.pelican.workbench";

	private static BundleContext context;

	private static Activator instance;

	private volatile IEclipseContext eclipseContext;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		instance = this;
		context = bundleContext;
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
