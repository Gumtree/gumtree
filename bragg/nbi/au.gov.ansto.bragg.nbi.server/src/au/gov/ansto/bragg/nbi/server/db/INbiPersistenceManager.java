/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package au.gov.ansto.bragg.nbi.server.db;

import org.gumtree.core.service.IService;

public interface INbiPersistenceManager extends IService {
	
	public <T> void persist(String dbID, String key, T data);
	
	public <T> T retrieve(String dbID, String key, Class<T> type);
	
	public void remove(String dbID, String key);
	
	public boolean contains(String dbID, String key);

}
