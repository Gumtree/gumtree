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

package org.gumtree.service.dataaccess;

import java.net.URI;
import java.util.Map;

import org.gumtree.core.management.IManageableBeanProvider;
import org.gumtree.core.service.IService;

public interface IDataAccessManager extends IService, IManageableBeanProvider {

	/**
	 * Gets a resource with a given URI address and representation.
	 * 
	 * @param <T>
	 * @param uri
	 * @param representation
	 * @return
	 * @throws DataAccessException
	 */
	public <T> T get(URI uri, Class<T> representation)
			throws DataAccessException;
	
	/**
	 * Gets a resource with a given URI address, representation and supporting properties.
	 * Usually we do not recommend the use properties because it against the ReST principle.
	 * In theory, properties are usually encoded in the URI string, but in security reason,
	 * password and user id should use the properties argument.
	 * 
	 * @param <T>
	 * @param uri
	 * @param representation
	 * @param properties
	 * @return
	 * @throws DataAccessException
	 */
	public <T> T get(URI uri, Class<T> representation,
			Map<String, Object> properties) throws DataAccessException;
	
	/**
	 * Gets a resource in an asynchronously.
	 * 
	 * @param <T>
	 * @param uri
	 * @param representation
	 * @param callback
	 */
	public <T> void get(URI uri, Class<T> representation,
			IDataHandler<T> callback);
	
	/**
	 * Gets a resource in an asynchronously with properties supplied.
	 * 
	 * @param <T>
	 * @param uri
	 * @param representation
	 * @param callback
	 * @param properties
	 */
	public <T> void get(URI uri, Class<T> representation,
			IDataHandler<T> callback, Map<String, Object> properties);
	
	/**
	 * Adds a data change listener.
	 * 
	 * @param listener
	 */
	public void addDataChangeListener(IDataChangeListener listener);
	
	/**
	 * Removes a data change listener.
	 * 
	 * @param listener
	 */
	public void removeDataChangeListener(IDataChangeListener listener);
	
}
