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

package org.gumtree.util.eclipse;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.gumtree.core.internal.Activator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

@SuppressWarnings("restriction")
public final class EclipseUtils {

	public static IExtensionTracker getExtensionTracker() {
		return Activator.getDefault().getExtensionTracker();
	}
	
	/**
	 * Returns the file store handle for a particular file resource from
	 * a plugin.
	 *
	 * @param pluginId id of the plugin where file is located
	 * @param relativePath file path relative to the plugin location
	 * @return file store handle to the specific file resource
	 * @throws CoreException when plugin cannot be found
	 */
	public static IFileStore find(String pluginId, String relativePath) throws CoreException {
		Bundle bundle = Platform.getBundle(pluginId);
		if (bundle == null) {
			throw new CoreException(new Status(IStatus.ERROR,
				Activator.PLUGIN_ID, IStatus.ERROR, "Cannot find bundle.",
				null));
		}
		URL fileURL = bundle.getEntry(relativePath);
		URI fileURI = null;
		try {
			fileURI = URIUtil.toURI(FileLocator.toFileURL(fileURL).getFile());
			return EFS.getStore(fileURI);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
				Activator.PLUGIN_ID, IStatus.ERROR,
				"Cannot find file store for " + fileURL.toString(), e));
		}
	}
	
	public static IEclipseContext createEclipseContext(Bundle bundle) {
		return createEclipseContext(bundle.getBundleContext());
	}
	
	public static IEclipseContext createEclipseContext(BundleContext context) {
		IEclipseContext eclipseContext = EclipseContextFactory.getServiceContext(context);
		// Fix for running Eclipse context in non OSGi environment
		try {
			eclipseContext.get(IEventBroker.class);
		} catch (InjectionException e) {
			eclipseContext.set(Logger.class, null);
		}
		return eclipseContext;
	}

}
