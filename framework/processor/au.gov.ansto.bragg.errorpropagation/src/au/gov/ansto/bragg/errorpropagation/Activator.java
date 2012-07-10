package au.gov.ansto.bragg.errorpropagation;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "au.gov.ansto.bragg.errorpropagation";

	private static Activator instance;

	private BundleContext context;

	public void start(BundleContext context) throws Exception {
		this.context = context;
		instance = this;
	}

	public void stop(BundleContext context) throws Exception {
		instance = null;
		this.context = null;
	}

	public BundleContext getContext() {
		return context;
	}

	public static Activator getDefault() {
		return instance;
	}

}
