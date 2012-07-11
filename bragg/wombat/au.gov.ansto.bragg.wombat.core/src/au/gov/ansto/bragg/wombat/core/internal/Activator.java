package au.gov.ansto.bragg.wombat.core.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.wombat.sics.IUserCommandManager;
import au.gov.ansto.bragg.wombat.sics.internal.UserCommandManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "au.gov.ansto.bragg.wombat.core";

	// The shared instance
	private static Activator plugin;
	
	private static Logger logger = LoggerFactory.getLogger(Activator.class);
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		
		// Register services
		context.registerService(IUserCommandManager.class.getName(), new UserCommandManager(), null);
		logger.info("Registered service: " + IUserCommandManager.class.getName());
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
