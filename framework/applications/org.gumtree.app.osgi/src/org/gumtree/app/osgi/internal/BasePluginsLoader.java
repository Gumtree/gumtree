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

package org.gumtree.app.osgi.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * A runtime loader for pre-loading plugins that are specified in the system
 * properties.
 * 
 * @author Tony Lam
 * 
 */
public class BasePluginsLoader {

	private BundleContext context;

	public void load(BundleContext context) throws Exception {
		this.context = context;

		// Activate custom bundle list
		String bundleList = System.getProperty(
				"gumtree.osgi.activateBundles", "");
		for (String rawBundleId : bundleList.split(",")) {
			final String bundleId = rawBundleId.trim();
			if (bundleId.length() == 0) {
				continue;
			}
			activateBundle(bundleId);
		}

	}

	public void unload(BundleContext context) {
	}

	private void activateBundle(String bundleId) {
		for (Bundle bundle : context.getBundles()) {
			if (bundle.getSymbolicName().equals(bundleId)) {
				if (bundle.getState() != Bundle.ACTIVE) {
					try {
						bundle.start();
					} catch (BundleException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
