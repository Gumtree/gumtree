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

package org.gumtree.gumnix.sics.batch.ui.definition;

import org.gumtree.workflow.ui.models.AbstractModelObject;

public class CommandArg extends AbstractModelObject implements ICommandArg {

	private String name;
	
	private ArgType type;
	
	private Object value;
	
	public CommandArg(String name, ArgType type) {
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}

	public ArgType getType() {
		return type;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		Object oldValue = this.value;
		this.value = value;
		firePropertyChange("value", oldValue, value);
	}

}
