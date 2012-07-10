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

public interface IDataChangeListener {

	public String getScheme();
	
	public boolean matchUri(URI uri);
	
	public Class<?> getRepresentation(URI uri);
	
	public void handleDataChange(URI uri, Class<?> representation, Object data);
	
}
