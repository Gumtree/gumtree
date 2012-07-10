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
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.gumtree.core.internal.Activator;
import org.osgi.framework.Bundle;

/**
 * BundleFileSystem provides an Eclipse File System (EFS) extenion "bundle" to
 * the runtime.
 * 
 * The expected URI for this extension:
 * bunle://<bundle_id>/<relation_path_to_the_bundle>
 * 
 * @author Tony Lam
 * 
 */
public class BundleFileSystem extends FileSystem {

	public BundleFileSystem() {
		super();
	}

	@Override
	public IFileStore getStore(URI uri) {
		try {
			String bundleId = uri.getAuthority();
			String path = uri.getPath();
			return find(bundleId, path);
		} catch (CoreException e) {
			return EFS.getNullFileSystem().getStore(uri);
		}
	}

	public IFileStore find(String pluginId, String relativePath)
			throws CoreException {
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

}
