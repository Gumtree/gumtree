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

package org.gumtree.util.bean;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class AbstractModelObject {

	protected static final String EMPTY_STRING = "";
	
	@XStreamOmitField
	private transient PropertyChangeSupport changeSupport;
	
	private PropertyChangeSupport getChangeSupport() {
		if (changeSupport == null) {
			synchronized (AbstractModelObject.class) {
				if (changeSupport == null) {
					changeSupport = new PropertyChangeSupport(this);
				}
			}
		}
		return changeSupport;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		getChangeSupport().addPropertyChangeListener(listener);
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		getChangeSupport().addPropertyChangeListener(propertyName, listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		getChangeSupport().removePropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		getChangeSupport().removePropertyChangeListener(propertyName, listener);
	}
	
	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		getChangeSupport().firePropertyChange(propertyName, oldValue, newValue);
	}
	
}
