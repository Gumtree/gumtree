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

package org.gumtree.gumnix.sics.batch.ui.buffer;

import java.net.URI;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.dataaccess.IDataAccessManager;

public class ResourceBasedBatchBuffer extends BatchBuffer {
	
	public ResourceBasedBatchBuffer(String name, URI uri) {
		super(name);
		setSource(uri);
	}

	public URI getSource() {
		URI source = (URI) super.getSource();
		// Fix deserialisation problem with URI object
		if (source.getScheme() == null) {
			source = URI.create(source.toString());
			setSource(source);
		}
		return source;
	}

	public String getContent() {
		IDataAccessManager dam = ServiceUtils.getService(IDataAccessManager.class);
		return dam.get(getSource(), String.class);
	}
	
}
