package org.gumtree.jython.core.internal;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.gumtree.jython.core.OsgiPackageLoader;
import org.gumtree.util.eclipse.E4Utils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

@SuppressWarnings("restriction")
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.gumtree.jython.core";
	
	private static BundleContext context;
	
	private static Activator instance;

	private OsgiPackageLoader packageLoader;

	private volatile IEclipseContext eclipseContext;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		instance = this;
		context = bundleContext;
		packageLoader = new OsgiPackageLoader();
		packageLoader.load();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if (eclipseContext != null) {
			eclipseContext.dispose();
			eclipseContext = null;	
		}
		packageLoader = null;
		context = null;
		instance = null;
	}
	
	public static BundleContext getContext() {
		return context;
	}
	
	public static Activator getDefault() {
		return instance;
	}
	
	public boolean isPackageLoaderInitialised() {
		if (packageLoader != null) {
			return packageLoader.isInitialised();
		}
		// Assume true if loader is missing
		return true;
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

}
