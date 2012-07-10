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

import java.lang.management.ManagementFactory;

import org.osgi.framework.BundleContext;

/**
 * A runtime loader for putting process information into the system property.
 * 
 * @author Tony Lam
 * @since 1.5
 * 
 */
public class RuntimeInfoLoader implements IRuntimeLoader {

	public static final String PROP_GUMTREE_RUNTIME = "gumtree.runtime";

	public void load(BundleContext context) throws Exception {
		// [GUMTREE-537] Reads the runtime information string and push into the
		// system property
		String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
		if (runtimeName != null) {
			System.setProperty(PROP_GUMTREE_RUNTIME, runtimeName);
		}
	}

	public void unload(BundleContext context) throws Exception {
	}

}
