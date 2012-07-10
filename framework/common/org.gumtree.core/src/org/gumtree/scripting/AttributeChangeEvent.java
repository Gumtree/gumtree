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

package org.gumtree.scripting;

public class AttributeChangeEvent extends ScriptingChangeEvent {

	public enum AttributeChangeEventType {
		SET, REMOVED
	}

	private static final long serialVersionUID = 6487460912548361775L;

	private AttributeChangeEventType type;

	private String name;

	private Object value;

	private int scope;

	public AttributeChangeEvent(IObservableComponent component,
			AttributeChangeEventType type, String name, Object value, int scope) {
		super(component);
		this.type = type;
		this.name = name;
		this.value = value;
		this.scope = scope;
	}

	public AttributeChangeEventType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public int getScope() {
		return scope;
	}

}
