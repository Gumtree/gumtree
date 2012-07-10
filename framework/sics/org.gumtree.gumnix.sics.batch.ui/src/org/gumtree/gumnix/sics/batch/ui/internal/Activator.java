/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package org.gumtree.gumnix.sics.batch.ui.internal;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.gumtree.util.eclipse.E4Utils;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.gumtree.gumnix.sics.batch.ui";

	private static Activator instance;

	private static BundleContext context;

	private IEclipseContext eclipseContext;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		this.context = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (InternalImage.isInstalled()) {
			InternalImage.dispose();
		}
		if (eclipseContext != null) {
			eclipseContext.dispose();
			eclipseContext = null;
		}
		this.context = null;
		instance = null;
		super.stop(context);
	}

	public IEclipseContext getEclipseContext() {
		if (eclipseContext == null) {
			synchronized (this) {
				if (eclipseContext == null) {
					eclipseContext = E4Utils.createEclipseContext(getContext());
				}
			}
		}
		return eclipseContext;
	}

	public static BundleContext getContext() {
		return context;
	}

	public static Activator getDefault() {
		return instance;
	}

}
