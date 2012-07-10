package org.gumtree.workflow.ui.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class AbstractModelObject {

	@XStreamOmitField
	private volatile PropertyChangeSupport changeSupport;
	
	private PropertyChangeSupport getChangeSupport() {
		if (changeSupport == null) {
			synchronized (this) {
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
