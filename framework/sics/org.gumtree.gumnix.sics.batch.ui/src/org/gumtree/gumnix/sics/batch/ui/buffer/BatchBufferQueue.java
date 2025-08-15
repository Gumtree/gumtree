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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.gumtree.service.persistence.ILocalPersistenceManager;
import org.gumtree.util.bean.AbstractModelObject;
import org.gumtree.util.bean.PropertyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchBufferQueue extends PropertyList<IBatchBuffer> {

	private static final Logger logger = LoggerFactory.getLogger(BatchBufferQueue.class);
	
	@Inject
	private ILocalPersistenceManager persistenceManager;
	private List<IQueueEventListener> queueEventListeners;
	
	public BatchBufferQueue(AbstractModelObject modelObject,
			String propertyName) {
		super(modelObject, propertyName, new CopyOnWriteArrayList<IBatchBuffer>());
		queueEventListeners = new ArrayList<BatchBufferQueue.IQueueEventListener>();
	}

	@PostConstruct
	@SuppressWarnings("unchecked")
	public void activate() {
		if (getPersistenceManager() != null) {
			try {
				ArrayList<IBatchBuffer> previousQueue = getPersistenceManager()
						.retrieve(BatchBufferQueue.class.getName(),
								ArrayList.class);
				if (previousQueue != null) {
					// Restore
					addAll(previousQueue);
				}
			} catch (Exception e) {
			}
		}
	}
	
	public boolean add(IBatchBuffer e) {
		logger.warn("adding buffer " + e.getName());
		boolean result = super.add(e);
		persist();
		triggerQueueEvent();
		logger.warn("finished adding buffer " + e.getName());
		return result;
	}
	
	public void add(int index, IBatchBuffer element) {
		logger.warn("adding buffer " + element.getName());
		super.add(index, element);
		persist();
		triggerQueueEvent();
		logger.warn("finished adding buffer " + element.getName());
	}
	
	public boolean addAll(Collection<? extends IBatchBuffer> c) {
		logger.warn("adding multiple buffer files: " + c.size());
		boolean result = super.addAll(c);
		persist();
		triggerQueueEvent();
		logger.warn("finished adding multiple buffer files: " + c.size());
		return result;
	}
	
	public boolean addAll(int index, Collection<? extends IBatchBuffer> c) {
		logger.warn("adding multiple buffer files: " + c.size());
		boolean result = super.addAll(index, c);
		persist();
		triggerQueueEvent();
		logger.warn("finished adding multiple buffer files: " + c.size());
		return result;
	}
	
	public IBatchBuffer remove(int index) {
		logger.warn("removing buffer file at index: " + index);
		IBatchBuffer result = super.remove(index);
		persist();
		triggerQueueEvent();
		logger.warn("finished removing buffer file at index: " + index);
		return result;
	}
	
	public boolean remove(Object o) {
		logger.warn("removing selected buffer file");
		boolean result = super.remove(o);
		persist();
		triggerQueueEvent();
		logger.warn("finished removing selected buffer file");
		return result;
	}
	
	public boolean removeAll(Collection<?> c) {
		logger.warn("removing all buffer files");
		boolean result = super.removeAll(c);
		persist();
		triggerQueueEvent();
		logger.warn("finished removing all buffer files");
		return result;
	}
	
	public IBatchBuffer set(int index, IBatchBuffer element) {
		IBatchBuffer result = super.set(index, element);
		persist();
		triggerQueueEvent();
		return result;
	}
	
	public void clear() {
		logger.warn("clear buffer queue");
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
				getPersistenceManager().persist(BatchBufferQueue.class.getName(),
						new ArrayList<IBatchBuffer>(this));
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
