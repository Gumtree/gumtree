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

import java.util.Map;

import org.gumtree.core.service.IContributionService;

public interface IDataConverter<K> extends IContributionService {

	public <T> T convert(K rawData, Class<T> representation,
			Map<String, Object> properties)
			throws RepresentationNotSupportedException;
	
//	public List<Class<?>> getSupportedRepresentations();
	
}
