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

package org.gumtree.gumnix.sics.control.controllers;

import java.util.Calendar;
import java.util.Date;

public class ComponentRecord implements IComponentRecord {

	private String path;
	
	private Date timestamp;
	
	private IComponentData data;
	
	public ComponentRecord(String path, IComponentData data) {
		super();
		this.timestamp = Calendar.getInstance().getTime();
		this.path = path;
		this.data = data;
	}

	public String getPath() {
		return path;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public IComponentData getData() {
		return data;
	}

}
