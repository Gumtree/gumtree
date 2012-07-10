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

package org.gumtree.sics.util;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.gumtree.util.eclipse.EclipseUtils;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.gumtree.sics.core";

	// The shared instance
	private static Activator plugin;
	
	private IEclipseContext eclipseContext;

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	public void stop(BundleContext context) throws Exception {
		eclipseContext = null;
		plugin = null;
		super.stop(context);
	}

	public IEclipseContext getEclipseContext() {
		if (eclipseContext == null) {
			eclipseContext = EclipseUtils.createEclipseContext(plugin.getBundle());
		}
		return eclipseContext;
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
