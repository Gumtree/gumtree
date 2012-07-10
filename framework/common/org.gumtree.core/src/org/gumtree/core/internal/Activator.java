/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.core.internal;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.gumtree.core.service.IServiceRegistrationManager;
import org.gumtree.core.service.ServiceRegistrationManager;
import org.gumtree.util.eclipse.E4Utils;
import org.osgi.framework.BundleContext;

/**
 * Activator setup the environment for the OSGi and Eclipse libraries for the
 * framework. When this class is called by the OSGi system, make sure
 * org.eclipse.core.runtime is also available.
 * 
 * @author Tony Lam
 * @since 1.7
 * 
 */
@SuppressWarnings("restriction")
public class Activator extends Plugin {

	public static final String PLUGIN_ID = "org.gumtree.core";

	private static BundleContext context;

	private static Activator instance;

	private IServiceRegistrationManager registrationManager;

	private volatile IExtensionTracker extensionTracker;
	
	private volatile IEclipseContext eclipseContext;

	/**
	 * The constructor
	 */
	public Activator() {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		instance = this;
		context = bundleContext;
		registrationManager = new ServiceRegistrationManager(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if (eclipseContext != null) {
			eclipseContext.dispose();
			eclipseContext = null;	
		}
		if (extensionTracker != null) {
			extensionTracker.close();
			extensionTracker = null;
		}
		if (registrationManager != null) {
			registrationManager.disposeObject();
			registrationManager = null;
		}
		context = null;
		instance = null;
		super.stop(bundleContext);
	}

	public IServiceRegistrationManager getServiceRegistrationManager() {
		return registrationManager;
	}

	public IExtensionTracker getExtensionTracker() {
		if (extensionTracker == null) {
			synchronized (this) {
				if (extensionTracker == null) {
					extensionTracker = new ExtensionTracker();
				}
			}
		}
		return extensionTracker;
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
