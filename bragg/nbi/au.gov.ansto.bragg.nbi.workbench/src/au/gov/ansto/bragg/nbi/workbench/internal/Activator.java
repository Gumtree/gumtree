package au.gov.ansto.bragg.nbi.workbench.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "au.gov.ansto.bragg.nbi.workbench";
	
	@Override
	public void start(BundleContext context) throws Exception {
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (InternalImage.isInstalled()) {
			InternalImage.dispose();
		}
	}

}
