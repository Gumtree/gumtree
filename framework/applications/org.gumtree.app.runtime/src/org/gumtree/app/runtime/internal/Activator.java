package org.gumtree.app.runtime.internal;

import java.util.Stack;

import org.gumtree.app.runtime.RuntimeProperties;
import org.gumtree.app.runtime.loader.EclipseLogListenerLoader;
import org.gumtree.app.runtime.loader.IRuntimeLoader;
import org.gumtree.app.runtime.loader.PluginsLoader;
import org.gumtree.app.runtime.loader.PropertiesLoader;
import org.gumtree.app.runtime.loader.RuntimeInfoLoader;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.gumtree.app.runtime";

	// Logger
	private static final Logger logger = LoggerFactory
			.getLogger(Activator.class);

	private static BundleContext context;

	// Stack of registered runtime loaders
	private Stack<IRuntimeLoader> loaders;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		context = bundleContext;
		loaders = new Stack<IRuntimeLoader>();

		// Load new config environment based properties
		load(new PropertiesLoader(), context);

		// Load runtime info
		load(new RuntimeInfoLoader(), context);

		// Eclipse log listener
		load(new EclipseLogListenerLoader(), context);
		
		// JUL to SLF4J
//		load(new JULBridgeLoader(), context);

		// Plugins (load Spring OSGi extender even if we haven't specified this
		// in config)
		load(new PluginsLoader(), context);

		// [GUMTREE-550] Kill system on unexpected activation error
		context.addFrameworkListener(new FrameworkListener() {
			public void frameworkEvent(FrameworkEvent event) {
				if (event.getThrowable() != null
						&& event.getType() == FrameworkEvent.ERROR) {
					if (Boolean.getBoolean(RuntimeProperties.SHUTDOWN_ON_ERROR)) {
						logger.error(
								"Framework error occured, it needs to be shutdown.",
								event.getThrowable());
						System.exit(1);
					}
					// final AbstractBundle systemBundle = (AbstractBundle)
					// getBundle().getBundleContext().getBundle(0);
					// if (systemBundle != null) {

					// This is the proper way to kill OSGi, but unfortunately it
					// does not work
					// in this thread.
					// bundle.getFramework().close();
					// System.exit(1);
					// } else {
					// logger.error("Framework error occured, but we have failed to shutdown.",
					// event.getThrowable());
					// }
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		// Manually unload loaders in a LIFO way
		if (loaders != null) {
			while (!loaders.isEmpty()) {
				loaders.pop().unload(context);
			}
			loaders = null;
		}
		context = null;
	}

	public static BundleContext getContext() {
		return context;
	}

	private void load(IRuntimeLoader loader, BundleContext context) {
		try {
			loader.load(context);
			loaders.push(loader);
			logger.info("Loaded " + loader.getClass().getSimpleName());
		} catch (Exception e) {
			logger.error("Error has occured on loading "
					+ loader.getClass().getSimpleName() + ".", e);
		}
	}

}
