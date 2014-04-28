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
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.gumtree.service.persistence.ILocalPersistenceManager;
import org.gumtree.util.bean.AbstractModelObject;
import org.gumtree.util.bean.PropertyList;

public class BatchBufferQueue extends PropertyList<IBatchBuffer> {

	@Inject
	private ILocalPersistenceManager persistenceManager;
	
	public BatchBufferQueue(AbstractModelObject modelObject,
			String propertyName) {
		super(modelObject, propertyName, new CopyOnWriteArrayList<IBatchBuffer>());		
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
		boolean result = super.add(e);
		persist();
		return result;
	}
	
	public void add(int index, IBatchBuffer element) {
		add(index, element);
		persist();
	}
	
	public boolean addAll(Collection<? extends IBatchBuffer> c) {
		boolean result = super.addAll(c);
		persist();
		return result;
	}
	
	public boolean addAll(int index, Collection<? extends IBatchBuffer> c) {
		boolean result = super.addAll(index, c);
		persist();
		return result;
	}
	
	public IBatchBuffer remove(int index) {
		IBatchBuffer result = super.remove(index);
		persist();
		return result;
	}
	
	public boolean remove(Object o) {
		boolean result = super.remove(o);
		persist();
		return result;
	}
	
	public boolean removeAll(Collection<?> c) {
		boolean result = super.removeAll(c);
		persist();
		return result;
	}
	
	public IBatchBuffer set(int index, IBatchBuffer element) {
		IBatchBuffer result = super.set(index, element);
		persist();
		return result;
	}
	
	public void clear() {
		super.clear();
		persist();
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
	
}
