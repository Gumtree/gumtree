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

package org.gumtree.workflow.ui.models;

import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.gumtree.util.collection.IParameters;

public class ParametersObservables extends AbstractObservableValue {

	private boolean updating = false;
	
	private IParameters parameters;
	
	private String key;
	
	private PropertyChangeListener listener;
	
	public ParametersObservables(Realm realm, IParameters parameters, String key) {
		super(realm);
		this.parameters = parameters;
		this.key = key;
	}
	
	protected Object doGetValue() {
		return parameters.get(key);
	}

	protected void doSetValue(Object value) {
		updating = true;
		parameters.put(key, value);
		updating = false;
	}
	
	public Object getValueType() {
		if (parameters.get(key) != null) {
			return parameters.get(key).getClass();
		}
		return null;
	}
	
	protected void firstListenerAdded() {
		listener = new PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent event) {
				if (!updating) {
					final ValueDiff diff = Diffs.createValueDiff(event.getOldValue(),
											event.getNewValue());
					getRealm().exec(new Runnable(){
						public void run() {
							fireValueChange(diff);
						}});
				}
			}
		};
		parameters.addPropertyChangeListener(key, listener);
	}
	
	public synchronized void dispose() {
		if (listener != null) {
			parameters.remove(listener);
			listener = null;
		}
		super.dispose();
	}

	public static ParametersObservables observeValue(IParameters parameters, String key) {
		return new ParametersObservables(Realm.getDefault(), parameters, key);
	}
	
}
