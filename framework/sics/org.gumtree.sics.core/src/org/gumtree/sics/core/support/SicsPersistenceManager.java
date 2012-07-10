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

package org.gumtree.sics.core.support;

import javax.annotation.PreDestroy;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.persistence.IObjectContainerManager;
import org.gumtree.sics.core.ISicsPersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db4o.ext.DatabaseClosedException;
import com.db4o.ext.Db4oIOException;
import com.db4o.ext.ExtObjectContainer;

public class SicsPersistenceManager implements ISicsPersistenceManager {

	private static final Logger logger = LoggerFactory
			.getLogger(SicsPersistenceManager.class);

	// private static final int NUMBER_OF_THREADS = 1;
	//
	// // Thread pool
	// private static ExecutorService executor =
	// Executors.newFixedThreadPool(NUMBER_OF_THREADS);

	// Database manager
	private IObjectContainerManager manager;
	
	// Database
	private volatile ExtObjectContainer db;

	public SicsPersistenceManager() {
		super();
	}

	public void store(final Object data) {
		if (db == null) {
			synchronized (this) {
				if (db == null) {
					// Initialise
					getContainerManager();			
				}
			}
		}
		if (db != null) {
			synchronized (db) {
				try {
					// Do not persist if db is closed (due to application shutdown)
					if (db.isClosed()) {
						return;
					}
					db.store(data);
					db.commit();
				} catch (DatabaseClosedException e) {
					logger.warn("Dataabase was closed, potentially due to system shutdown.");
				} catch (Exception e) {
					logger.error("Failed to store data.", e);
				}		
			}
		}
	}
	
	public IObjectContainerManager getContainerManager() {
		if (manager == null) {
			setContainerManager(ServiceUtils.getService(IObjectContainerManager.class));
		}
		return manager;
	}
	
	public void setContainerManager(IObjectContainerManager manager) {
		if (db != null) {
			synchronized (db) {
				db.close();
			}
		}
		this.manager = manager;
		db = (ExtObjectContainer) manager.createObjectContainer("sics_new", true);
	}

	@Override
	@PreDestroy
	public void disposeObject() {
		if (db != null) {
			try {
				db.close();
			} catch (Db4oIOException e) {
				logger.warn("Failed to close database");
			}
			db = null;
		}
		manager = null;
	}

}
