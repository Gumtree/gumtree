package au.gov.ansto.bragg.dingo.workbench.internal;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.gumtree.util.eclipse.E4Utils;
import org.osgi.framework.BundleContext;


@SuppressWarnings("restriction")
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "au.gov.ansto.bragg.dingo.workbench";

	private static BundleContext context;

	private static Activator instance;

	private volatile IEclipseContext eclipseContext;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		this.context = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (eclipseContext != null) {
			eclipseContext.dispose();
			eclipseContext = null;
		}
		if (InternalImage.isInstalled()) {
			InternalImage.dispose();
		}
		context = null;
		instance = null;
		super.stop(context);
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