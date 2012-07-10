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

import org.gumtree.core.service.IService;

public interface ILocalPersistenceManager extends IService {
	
	public <T> void persist(String key, T data);
	
	public <T> T retrieve(String key, Class<T> type);
	
	public void remove(String key);
	
	public boolean contains(String key);
	
}
