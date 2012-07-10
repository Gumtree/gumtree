package org.gumtree.data.util.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		context = bundleContext;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		context = null;
	}
	
	public static BundleContext getContext() {
		return context;
	}

}
