package org.gumtree.app.runtime.loader;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class JULBridgeLoader implements IRuntimeLoader {

	private static final Logger logger = LoggerFactory.getLogger(JULBridgeLoader.class);
	
	@Override
	public void load(BundleContext context) throws Exception {
		if (!SLF4JBridgeHandler.isInstalled()) {
			SLF4JBridgeHandler.install();
			logger.info("Installed JUL to SLF4J brigde");
		}
	}

	@Override
	public void unload(BundleContext context) throws Exception {
		if (SLF4JBridgeHandler.isInstalled()) {
			SLF4JBridgeHandler.uninstall();
			logger.info("Uninstalled JUL to SLF4J brigde");
		}
	}

}
