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

import org.gumtree.util.bean.AbstractModelObject;

public abstract class BatchBuffer extends AbstractModelObject implements IBatchBuffer {

	protected static final String EMPTY_CONTENT = "";
	
	private String name;
	
	private Object source;
	
	public BatchBuffer(String name) {
		this.name = name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		firePropertyChange("name", oldValue, name);
	}

	public String getName() {
		return name;
	}
	
	public Object getSource() {
		return source;
	}
	
	public void setSource(Object source) {
		Object oldValue = this.source;
		this.source = source;
		firePropertyChange("source", oldValue, source);
	}
	
}
