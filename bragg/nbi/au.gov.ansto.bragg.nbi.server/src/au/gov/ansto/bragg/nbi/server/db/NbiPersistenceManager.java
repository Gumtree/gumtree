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

package au.gov.ansto.bragg.nbi.server.db;

import org.gumtree.service.persistence.IObjectContainerManager;
import org.gumtree.service.persistence.PersistentEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Query;

public class NbiPersistenceManager implements INbiPersistenceManager {

	private static final Logger logger = LoggerFactory
			.getLogger(NbiPersistenceManager.class);

	public static final String ID_USER_DATABASE = "user";
	public static final String ID_RESET_DATABASE = "reset";
	public static final String ID_SESSION_EMAIL_DATABASE = "session";
	public static final String ID_EMAIL_SESSION_DATABASE = "esession";
	
	private IObjectContainerManager objectContainerManager;
	
	public NbiPersistenceManager() {
		objectContainerManager = new DbContainerManager();
	}
	
	public <T> void persist(String dbID, String key, T data) {
		// Update entry if entry has already existed
		// otherwise create a new entry
		PersistentEntry entry = retrieveEntry(dbID, key);
		if (entry == null) {
			entry = new PersistentEntry(key);
		}
		entry.setData(data);
		// Store to object database
		getObjectContainer(dbID).store(entry);
		getObjectContainer(dbID).commit();
		
		// Get
		Query query = getObjectContainer(dbID).query();
		query.constrain(PersistentEntry.class);
		query.descend("key").constrain(key);
		ObjectSet<PersistentEntry> result = query.execute();
		if (result.size() > 0) {
			System.out.println(result.get(0));
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T retrieve(String dbID, String key, Class<T> type) {
		PersistentEntry entry = retrieveEntry(dbID, key);
		if (entry != null) {
			return (T) entry.getData();
		}
		return null;
	}
	
	public void remove(String dbID, String key) {
		PersistentEntry entry = retrieveEntry(dbID, key);
		if (entry != null) {
			getObjectContainer(dbID).delete(entry);
			getObjectContainer(dbID).commit();
		}
	}
	
	public boolean contains(String dbID, String key) {
		return retrieveEntry(dbID, key) != null;
	}

	private PersistentEntry retrieveEntry(String dbID, String key) {
		// Use SODA query over Native Query for better performance
		Query query = getObjectContainer(dbID).query();
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
	
	public ObjectContainer getObjectContainer(String dbID) {
		ObjectContainer container = getObjectContainerManager()
				.getObjectContainer(dbID);
		if (container == null) {
			synchronized (this) {
				if (container == null) {
					container = getObjectContainerManager().createObjectContainer(
							dbID, false);
				}
			}
		}
		return container;
	}
	
}
