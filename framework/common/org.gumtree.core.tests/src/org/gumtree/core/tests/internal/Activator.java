package org.gumtree.core.tests.internal;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.gumtree.util.eclipse.E4Utils;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

	public static final String PLUGIN_ID = "org.gumtree.core.tests";

	private static BundleContext bundleContext;

	private static Activator instance;

	private IEclipseContext eclipseContext;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		bundleContext = context;
		eclipseContext = E4Utils.createEclipseContext(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (eclipseContext != null) {
			eclipseContext.dispose();
			eclipseContext = null;
		}
		bundleContext = null;
		instance = null;
		super.stop(context);
	}

	public static BundleContext getContext() {
		return bundleContext;
	}

	public static Activator getDefault() {
		return instance;
	}

	public IEclipseContext getEclipseContext() {
		return eclipseContext;
	}

}
