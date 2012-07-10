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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.core.internal.Activator;
import org.gumtree.core.management.IManageableBean;
import org.gumtree.core.service.IServiceManager;
import org.gumtree.core.service.IServiceRegistrationManager;
import org.gumtree.core.service.ServiceRegistrationManager;
import org.gumtree.service.persistence.IObjectContainerManager;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db4o.ObjectContainer;
import com.db4o.osgi.Db4oService;

public class ObjectContainerManager implements IObjectContainerManager {

	private static final Logger logger = LoggerFactory
			.getLogger(ObjectContainerManager.class);

	protected Map<String, ObjectContainerContext> contexts;

	private ObjectContainerManagerMonitor monitor;

	private Db4oService db4oService;

	private IServiceManager serviceManager;

	private IServiceRegistrationManager serviceRegistrationManager;

	public ObjectContainerManager() {
		contexts = new HashMap<String, ObjectContainerContext>(2);
	}

	@Override
	public ObjectContainer getObjectContainer(String databaseId) {
		// Find object container from the service registery
		List<ObjectContainerContext> contexts = getServiceManager()
				.getServicesNow(ObjectContainerContext.class);
		for (ObjectContainerContext context : contexts) {
			if (databaseId.equals(context.databaseId)) {
				return context.objectContainer;
			}
		}
		return null;
	}

	@Override
	public ObjectContainer createObjectContainer(String databaseId,
			boolean overwrite) {
		// Find the cached container if possible
		ObjectContainer container = getObjectContainer(databaseId);
		if (container != null) {
			return container;
		}
		// Get database file
		File databaseFile = Activator.getDefault().getStateLocation()
				.append("/" + databaseId + ".yap").toFile();
		// Delete old one
		if (databaseFile.exists() && overwrite) {
			databaseFile.delete();
		}
		// Create database
		container = getDb4oService().openFile(databaseFile.toString());
		// Register to the registry
		ObjectContainerContext context = new ObjectContainerContext();
		context.databaseId = databaseId;
		context.databaseFile = databaseFile;
		context.objectContainer = container;
		context.registration = getServiceRegistrationManager().registerService(
				ObjectContainerContext.class, context);
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
		getServiceRegistrationManager().unregisterService(context.registration);
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
		serviceManager = null;
		monitor = null;
	}

	@Override
	public IManageableBean[] getManageableBeans() {
		if (monitor == null) {
			monitor = new ObjectContainerManagerMonitor(this);
		}
		return new IManageableBean[] { monitor };
	}

	public IServiceRegistrationManager getServiceRegistrationManager() {
		if (serviceRegistrationManager == null) {
			serviceRegistrationManager = new ServiceRegistrationManager();
		}
		return serviceRegistrationManager;
	}
	
	/*************************************************************************
	 * Components
	 *************************************************************************/

	public IServiceManager getServiceManager() {
		return serviceManager;
	}

	public void setServiceManager(IServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	public Db4oService getDb4oService() {
		return db4oService;
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

}
