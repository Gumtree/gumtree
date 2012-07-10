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

package org.gumtree.service.persistence;

import org.gumtree.core.object.IDisposable;
import org.gumtree.core.service.IService;
import org.gumtree.core.management.IManageableBeanProvider;

import com.db4o.ObjectContainer;

public interface IObjectContainerManager extends IService, IDisposable,
		IManageableBeanProvider {

	public ObjectContainer getObjectContainer(String databaseId);

	public ObjectContainer createObjectContainer(String databaseId,
			boolean overwrite);

	public void removeObjectContainer(String databaseId,
			boolean deleteFile);

}
