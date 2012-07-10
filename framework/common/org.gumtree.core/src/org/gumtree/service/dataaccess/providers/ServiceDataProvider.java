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

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.dataaccess.InvalidResourceException;
import org.gumtree.service.dataaccess.RepresentationNotSupportedException;

public class ServiceDataProvider extends AbstractDataProvider<Object> {

	@SuppressWarnings("unchecked")
	public <T> T get(URI uri, Class<T> representation, Map<String, Object> properties) {
		try {
			return (T) ServiceUtils.getService(Class.forName(uri.getHost()));
		} catch (ClassNotFoundException e) {
			throw new InvalidResourceException(e);
		} catch (Exception e) {
			throw new RepresentationNotSupportedException();
		}
	}
	
}
