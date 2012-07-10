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

package org.gumtree.service.persistence.support;

import org.gumtree.service.persistence.ILocalPersistenceManager;
import org.gumtree.service.persistence.IObjectContainerManager;
import org.gumtree.service.persistence.PersistentEntry;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Query;

public class LocalPersistenceManager implements ILocalPersistenceManager {
	
	private static final String ID_DATABASE = "local";
	
	private IObjectContainerManager objectContainerManager;
	
	public LocalPersistenceManager() {
	}
	
	public <T> void persist(String key, T data) {
		// Update entry if entry has already existed
		// otherwise create a new entry
		PersistentEntry entry = retrieveEntry(key);
		if (entry == null) {
			entry = new PersistentEntry(key);
		}
		entry.setData(data);
		// Store to object database
		getObjectContainer().store(entry);
		getObjectContainer().commit();
		
		// Get
		Query query = getObjectContainer().query();
		query.constrain(PersistentEntry.class);
		query.descend("key").constrain(key);
		ObjectSet<PersistentEntry> result = query.execute();
		if (result.size() > 0) {
			System.out.println(result.get(0));
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T retrieve(String key, Class<T> type) {
		PersistentEntry entry = retrieveEntry(key);
		if (entry != null) {
			return (T) entry.getData();
		}
		return null;
	}
	
	public void remove(String key) {
		PersistentEntry entry = retrieveEntry(key);
		if (entry != null) {
			getObjectContainer().delete(entry);
			getObjectContainer().commit();
		}
	}
	
	public boolean contains(String key) {
		return retrieveEntry(key) != null;
	}

	private PersistentEntry retrieveEntry(String key) {
		// Use SODA query over Native Query for better performance
		Query query = getObjectContainer().query();
		query.constrain(PersistentEntry.class);
		query.descend("key").constrain(key);
		ObjectSet<PersistentEntry> result = query.execute();
		if (result.size() > 0) {
			return result.get(0);
		}
		return null;
	}
	
	/*************************************************************************
	 * Components
	 *************************************************************************/
	
	public IObjectContainerManager getObjectContainerManager() {
		return objectContainerManager;
	}

	public void setObjectContainerManager(
			IObjectContainerManager objectContainerManager) {
		this.objectContainerManager = objectContainerManager;
	}
	
	/*************************************************************************
	 * Utilities
	 *************************************************************************/
	
	public ObjectContainer getObjectContainer() {
		ObjectContainer container = getObjectContainerManager()
				.getObjectContainer(ID_DATABASE);
		if (container == null) {
			synchronized (this) {
				if (container == null) {
					container = getObjectContainerManager().createObjectContainer(
							ID_DATABASE, false);
				}
			}
		}
		return container;
	}
	
}
