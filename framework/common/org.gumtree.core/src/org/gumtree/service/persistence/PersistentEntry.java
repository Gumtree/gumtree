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

import java.io.Serializable;

public class PersistentEntry implements Serializable {

	private static final long serialVersionUID = 9037271131846514836L;

	private String key;
	
	private Object data;
	
	public PersistentEntry() {
		this(null, null);
	}
	
	public PersistentEntry(String key) {
		this(key, null);
	}
	
	public PersistentEntry(String key, Object data) {
		this.key = key;
		this.data = data;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
}
