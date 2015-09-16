package org.gumtree.msw.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	// fields
	private static BundleContext context;
	private static Activator instance;

	// properties
	public static BundleContext getContext() {
		return context;
	}
	public static Activator getDefault() {
		return instance;
	}

	// methods
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		instance = this;
		context = bundleContext;
	}
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		context = null;
		instance = null;
	}
	// helper
	public static String getEntry(String path) {
		return "../org.gumtree.msw/" + path;
		//return "file:../org.gumtree.msw/" + path;
		
		// TODO use BundleContext
		//return getContext().getBundle().getEntry("resources/msw.xsd");
		//try {
		//	return new URL("file:../org.gumtree.msw/" + path);
		//}
		//catch (MalformedURLException e) {
		//	return null;
		//}
	}
}
