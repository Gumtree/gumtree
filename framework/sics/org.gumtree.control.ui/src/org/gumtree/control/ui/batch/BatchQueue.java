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

package org.gumtree.control.ui.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.gumtree.control.batch.IBatchScript;
import org.gumtree.service.persistence.ILocalPersistenceManager;
import org.gumtree.util.bean.AbstractModelObject;
import org.gumtree.util.bean.PropertyList;

public class BatchQueue extends PropertyList<IBatchScript> {

	@Inject
	private ILocalPersistenceManager persistenceManager;
	private List<IQueueEventListener> queueEventListeners;
	
	public BatchQueue(AbstractModelObject modelObject,
			String propertyName) {
		super(modelObject, propertyName, new CopyOnWriteArrayList<IBatchScript>());
		queueEventListeners = new ArrayList<BatchQueue.IQueueEventListener>();
	}

	@PostConstruct
	@SuppressWarnings("unchecked")
	public void activate() {
		if (getPersistenceManager() != null) {
			try {
				ArrayList<IBatchScript> previousQueue = getPersistenceManager()
						.retrieve(BatchQueue.class.getName(),
								ArrayList.class);
				if (previousQueue != null) {
					// Restore
					addAll(previousQueue);
				}
			} catch (Exception e) {
			}
		}
	}
	
	public boolean add(IBatchScript e) {
		boolean result = super.add(e);
		persist();
		triggerQueueEvent();
		return result;
	}
	
	public void add(int index, IBatchScript element) {
		add(index, element);
		persist();
		triggerQueueEvent();
	}
	
	public boolean addAll(Collection<? extends IBatchScript> c) {
		boolean result = super.addAll(c);
		persist();
		triggerQueueEvent();
		return result;
	}
	
	public boolean addAll(int index, Collection<? extends IBatchScript> c) {
		boolean result = super.addAll(index, c);
		persist();
		triggerQueueEvent();
		return result;
	}
	
	public IBatchScript remove(int index) {
		IBatchScript result = super.remove(index);
		persist();
		triggerQueueEvent();
		return result;
	}
	
	public boolean remove(Object o) {
		boolean result = super.remove(o);
		persist();
		triggerQueueEvent();
		return result;
	}
	
	public boolean removeAll(Collection<?> c) {
		boolean result = super.removeAll(c);
		persist();
		triggerQueueEvent();
		return result;
	}
	
	public IBatchScript set(int index, IBatchScript element) {
		IBatchScript result = super.set(index, element);
		persist();
		triggerQueueEvent();
		return result;
	}
	
	public void clear() {
		super.clear();
		persist();
		triggerQueueEvent();
	}
	
	public ILocalPersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

	public void setPersistenceManager(ILocalPersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}

	private void persist() {
		if (getPersistenceManager() != null) {
			try {
				getPersistenceManager().persist(BatchQueue.class.getName(),
						new ArrayList<IBatchScript>(this));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public interface IQueueEventListener{
		public void queueChanged();
	}
	
	public void addQueueEventListener(IQueueEventListener listener){
		queueEventListeners.add(listener);
	}
	
	public void removeQueueEventListener(IQueueEventListener listener) {
		queueEventListeners.remove(listener);
	}
	
	public void triggerQueueEvent() {
		for (IQueueEventListener listener : queueEventListeners) {
			listener.queueChanged();
		}
	}
}
