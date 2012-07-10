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

import javax.script.SimpleScriptContext;

import org.gumtree.scripting.AttributeChangeEvent.AttributeChangeEventType;
import org.gumtree.util.messaging.IListenerManager;
import org.gumtree.util.messaging.ListenerManager;
import org.gumtree.util.messaging.SafeListenerRunnable;


public class ObservableScriptContext extends SimpleScriptContext implements IObservableComponent {
	
	private IListenerManager<IScriptingListener> listenerManager;
	
	public ObservableScriptContext() {
	}
	
	public void setAttribute(String name, Object value, int scope) {
		super.setAttribute(name, value, scope);
		final AttributeChangeEvent event = new AttributeChangeEvent(this, AttributeChangeEventType.SET, name, value, scope);
		getListenerManager().invokeListeners(new SafeListenerRunnable<IScriptingListener>() {
			public void run(IScriptingListener listener) throws Exception {
				listener.handleChange(event);
			}			
		});
	}
	
	 public Object removeAttribute(String name, int scope) {
		 Object returnObject = super.removeAttribute(name, scope);
		 final AttributeChangeEvent event = new AttributeChangeEvent(this, AttributeChangeEventType.REMOVED, name, null, scope);
		 getListenerManager().invokeListeners(new SafeListenerRunnable<IScriptingListener>() {
			public void run(IScriptingListener listener) throws Exception {
				listener.handleChange(event);
			}			
		});
		return returnObject;
	}

	public void addListener(IScriptingListener listener) {
		getListenerManager().addListenerObject(listener);
	}

	public void removeListener(IScriptingListener listener) {
		getListenerManager().removeListenerObject(listener);
	}
	
	public IListenerManager<IScriptingListener> getListenerManager() {
		if (listenerManager == null) {
			synchronized (this) {
				if (listenerManager == null) {
					listenerManager = new ListenerManager<IScriptingListener>();
				}
			}
		}
		return listenerManager;
	}

	
}
