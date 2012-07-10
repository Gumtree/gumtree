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

package org.gumtree.gumnix.sics.internal.core;

import org.gumtree.gumnix.sics.core.ISicsPersistenceManager;
import org.gumtree.service.persistence.IObjectContainerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db4o.ext.DatabaseClosedException;
import com.db4o.ext.ExtObjectContainer;

public class SicsPersistenceManager implements ISicsPersistenceManager {

	private static final Logger logger = LoggerFactory
			.getLogger(SicsPersistenceManager.class);

	// private static final int NUMBER_OF_THREADS = 1;
	//
	// // Thread pool
	// private static ExecutorService executor =
	// Executors.newFixedThreadPool(NUMBER_OF_THREADS);

	private IObjectContainerManager objectContainerManager;

	// Database
	private ExtObjectContainer db;

	public SicsPersistenceManager() {
		super();
	}

	public void activate() {
		db = (ExtObjectContainer) getObjectContainerManager()
				.createObjectContainer("sics", true);
	}

	public void store(final Object data) {
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

	public IObjectContainerManager getObjectContainerManager() {
		return objectContainerManager;
	}

	public void setObjectContainerManager(
			IObjectContainerManager objectContainerManager) {
		this.objectContainerManager = objectContainerManager;
	}

}
