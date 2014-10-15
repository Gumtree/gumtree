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

package au.gov.ansto.bragg.nbi.server.db;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.gumtree.core.management.IManageableBean;
import org.gumtree.core.service.IServiceRegistrationManager;
import org.gumtree.service.persistence.IObjectContainerManager;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.server.internal.Activator;

import com.db4o.ObjectContainer;
import com.db4o.osgi.Db4oService;
import com.db4o.osgi.Db4oServiceImpl;

public class DbContainerManager implements IObjectContainerManager {

	private static final Logger logger = LoggerFactory
			.getLogger(DbContainerManager.class);

	protected Map<String, ObjectContainerContext> contexts;

	private Db4oService db4oService;

	private IServiceRegistrationManager serviceRegistrationManager;

	public DbContainerManager() {
		contexts = new HashMap<String, ObjectContainerContext>(2);
	}

	@Override
	public ObjectContainer getObjectContainer(String databaseId) {
		ObjectContainerContext context = contexts.get(databaseId);
		if (context != null) {
			return context.objectContainer;
		} else {
			return createObjectContainer(databaseId, false);
		}
	}

	@Override
	public ObjectContainer createObjectContainer(String databaseId,
			boolean overwrite) {
		// Find the cached container if possible
		// Get database file
		File databaseFile = InternalPlatform.getDefault().getStateLocation(Activator.getDefault().getContext().getBundle(), true)
				.append("/" + databaseId + ".yap").toFile();
		// Delete old one
		if (databaseFile.exists() && overwrite) {
			databaseFile.delete();
		}
		// Create database
		ObjectContainer container = getDb4oService().openFile(databaseFile.toString());
		// Register to the registry
		ObjectContainerContext context = new ObjectContainerContext();
		context.databaseId = databaseId;
		context.databaseFile = databaseFile;
		context.objectContainer = container;
		contexts.put(databaseId, context);
		return container;
	}

	@Override
	public void removeObjectContainer(String databaseId, boolean deleteFile) {
		// Check if the database is managed by this manager
		if (!contexts.containsKey(databaseId)) {
			return;
		}
		// Stop the database
		ObjectContainerContext context = contexts.get(databaseId);
		try {
			context.objectContainer.commit();
			context.objectContainer.close();
		} catch (Exception e) {
			logger.warn("Error occurred when shutting down database "
					+ databaseId, e);
		}
		// Delete file if required
		if (deleteFile) {
			try {
				context.databaseFile.delete();
			} catch (Exception e) {
				logger.warn("Error occurred when deleting database file "
						+ context.databaseFile, e);
			}
		}
		// Unregister
		contexts.remove(databaseId);
	}

	public void disposeObject() {
		if (contexts != null) {
			for (ObjectContainerContext context : contexts.values()) {
				// Stop the database
				try {
					context.objectContainer.commit();
					context.objectContainer.close();
				} catch (Exception e) {
					logger.warn("Error occurred when shutting down database "
							+ context.databaseId, e);
				}
			}
			contexts.clear();
			contexts = null;
		}
		if (serviceRegistrationManager != null) {
			serviceRegistrationManager.disposeObject();
			serviceRegistrationManager = null;
		}
		db4oService = null;
	}

	
	/*************************************************************************
	 * Components
	 *************************************************************************/

	public Db4oService getDb4oService() {
		if (db4oService != null) {
			return db4oService;
		} else {
			db4oService = new Db4oServiceImpl(Activator.getContext().getBundle());
			return db4oService;
		}
	}
	
	public void setDb4oService(Db4oService db4oService) {
		this.db4oService = db4oService;
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	class ObjectContainerContext {
		private String databaseId;
		private File databaseFile;
		private ObjectContainer objectContainer;
		private ServiceRegistration<?> registration;
	}

	@Override
	public IManageableBean[] getManageableBeans() {
		return null;
	}

}
