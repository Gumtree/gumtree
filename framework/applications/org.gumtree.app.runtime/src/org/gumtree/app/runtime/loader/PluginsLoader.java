/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.app.runtime.loader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.Platform;
import org.gumtree.app.runtime.RuntimeProperties;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A runtime loader for pre-loading plugins that are specified in the system
 * properties.
 * 
 * @author Tony Lam
 * 
 */
public class PluginsLoader implements IRuntimeLoader {

	// private static final String PLUGIN_EQUINOX_EVENT =
	// "org.eclipse.equinox.event";

	// private static final String PLUGIN_SPRING_OSGI_EXTENDER =
	// "org.springframework.osgi.extender";

	// private static final String PLUGIN_ECLIPSE_EQUINOX_DS =
	// "org.eclipse.equinox.ds";

	private static final Logger logger = LoggerFactory
			.getLogger(PluginsLoader.class);

	public void load(BundleContext context) throws Exception {
		// Activate Eclipse Equinox DS
		// activateBundle(PLUGIN_ECLIPSE_EQUINOX_DS);

		// Activate Spring OSGi extender
		// activateBundle(PLUGIN_SPRING_OSGI_EXTENDER);

		// Activate custom bundle list
		String bundleList = System.getProperty(
				RuntimeProperties.ACTIVATE_BUNDLES, "");
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		for (String rawBundleId : bundleList.split(",")) {
			final String bundleId = rawBundleId.trim();
			if (bundleId.length() == 0) {
				continue;
			}
			// Activating on a separate thread to avoid holding UI thread
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					if (!Platform.isRunning()) {
						logger.info("We are waiting for the Eclipse Platform to be ready...");
					}
					// Wait until the Eclipse Platform is ready
					while (!Platform.isRunning()) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
					}
					activateBundle(bundleId);
				}
			});
		}

	}

	public void unload(BundleContext context) {
	}

	private void activateBundle(String bundleId) {
		Bundle bundle = Platform.getBundle(bundleId);
		if (bundle != null && bundle.getState() != Bundle.ACTIVE) {
			try {
				bundle.start();
				logger.info("Maunally activated " + bundleId);
			} catch (BundleException e) {
				logger.warn("Failed to activate " + bundleId, e);
			}
		} else if (bundle == null) {
			logger.warn("Bundle " + bundleId + " is missing.");
		} else {
			logger.info("Bundle " + bundleId + " has really been activated.");
		}
	}

}
