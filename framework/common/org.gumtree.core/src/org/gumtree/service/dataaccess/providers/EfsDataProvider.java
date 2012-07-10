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

package org.gumtree.service.dataaccess.providers;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.gumtree.service.dataaccess.DataAccessException;
import org.gumtree.service.dataaccess.InvalidResourceException;

public class EfsDataProvider extends AbstractDataProvider<IFileStore> {
	
	public <T> T get(URI uri, Class<T> representation, Map<String, Object> properties) throws DataAccessException {
		// Convert resource to URI
		// see: http://wiki.eclipse.org/EFS_for_Platform_Committers
		if (uri.getScheme().equalsIgnoreCase("resource")) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			try {
				IResource resource = root.getProject(uri.getHost()).findMember(uri.getPath());
				if (resource != null) {
					uri = resource.getLocationURI();
				}
			} catch (Exception e) {
				throw new InvalidResourceException(e);
			}
		}
		
		// Load file store
		IFileStore fileStore = null;
		try {
			fileStore = EFS.getStore(uri);
		} catch (Exception e) {
			throw new InvalidResourceException(e);
		}
		if (fileStore == null) {
			throw new InvalidResourceException("Resource does not exist");
		}
		return convert(fileStore, representation, null);
	}

}
