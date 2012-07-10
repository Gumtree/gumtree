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

package org.gumtree.service.notification.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.gumtree.core.service.IServiceManager;
import org.gumtree.service.notification.INotification;
import org.gumtree.service.notification.INotificationProvider;
import org.gumtree.service.notification.INotificationService;
import org.gumtree.service.notification.NotificationConstants;
import org.gumtree.service.persistence.IObjectContainerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db4o.ext.ExtObjectContainer;

public class DefaultNotificationService implements INotificationService {

	private static final Logger logger = LoggerFactory
			.getLogger(DefaultNotificationService.class);

	// Service manager for finding providers
	private IServiceManager serviceManager;

	// Database for persistence
	private IObjectContainerManager objectContainerManager;

	private ExtObjectContainer db;

	// Queue for storing notification to dispatch
	private BlockingQueue<INotification> notificationQueue;

	// Thread for notification dispatch
	private ExecutorService dispatchExecutor;

	// Thread for long operation
	private ExecutorService longOperationExecutor;

	// Cache for storing preferred provider of a protocol
	private Map<String, String> defaultProviderMap;

	private Lock dbLock;

	public DefaultNotificationService() {
		// Initialise
		defaultProviderMap = new HashMap<String, String>();
		notificationQueue = new ArrayBlockingQueue<INotification>(10);
		// Initialise persistence

		// Initialise thread
		dbLock = new ReentrantLock();
		dispatchExecutor = Executors.newFixedThreadPool(1);
		dispatchExecutor.execute(new Runnable() {
			public void run() {
				runDispatch();
			}
		});
		longOperationExecutor = Executors.newFixedThreadPool(1);
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public IObjectContainerManager getObjectContainerManager() {
		return objectContainerManager;
	}

	public void setObjectContainerManager(IObjectContainerManager manager) {
		objectContainerManager = manager;
	}

	public IServiceManager getServiceManager() {
		return serviceManager;
	}

	public void setServiceManager(IServiceManager manager) {
		serviceManager = manager;
	}

	/*************************************************************************
	 * Dispatch related methods
	 *************************************************************************/
	public void send(INotification notification) {
		dbLock.lock();
		// Discard duplicated entry on the waiting list
		if (!notificationQueue.contains(notification)) {
			notificationQueue.add(notification);
		}
		dbLock.unlock();
	}

	protected void runDispatch() {
		while (true) {
			// Pull notification
			INotification notification = null;
			try {
				notification = notificationQueue.take();
			} catch (InterruptedException e) {
				logger.warn(
						"Failed to retrieve notification from blocking queue.",
						e);
			}

			// Loop protection
			if (notification == null) {
				continue;
			}

			// Dispatch
			dispatchNotification(notification);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void dispatchNotification(final INotification notification) {
		if (notification.getProtocol() == null
				&& notification instanceof CompositeNotification) {
			for (INotification subNotification : ((CompositeNotification) notification)
					.getNotifications()) {
				dispatchNotification(subNotification);
			}
		} else {
			try {
				// Find provider
				final INotificationProvider provider = getProvider(notification
						.getProtocol());
				// Dispatch
				if (provider != null) {
					// Persist before process
					persist(notification);
					if (provider.isLongOperation(notification)) {
						// Queue in the long operation thread if this is a time
						// consuming delivery
						longOperationExecutor.execute(new Runnable() {
							public void run() {
								try {
									provider.handleNotification(notification);
								} catch (Exception e) {
									logger.warn(
											"Failure detected in the long notification dispatch",
											e);
								}
							}
						});
					} else {
						// Process the notification immediately
						provider.handleNotification(notification);
					}
				} else {
					logger.warn("Provider not found for protocol "
							+ notification.getProtocol());
				}
			} catch (Exception e) {
				// Trap exception to ensure this thread runs forever
				logger.warn("Failure detected in the notification dispatch", e);
			}
		}
	}

	/*************************************************************************
	 * Persistence related methods
	 *************************************************************************/
	protected void persist(INotification notification) {
		dbLock.lock();
		if (db == null) {
			db = (ExtObjectContainer) getObjectContainerManager()
					.createObjectContainer("notification", true);
		}
		try {
			if (db.isClosed()) {
				return;
			}
			db.store(notification);
			db.commit();
		} catch (Exception e) {
			logger.error("Failed to store data.", e);
		}
		dbLock.unlock();
	}

	/*************************************************************************
	 * Providers related methods
	 *************************************************************************/

	public void setDefaultProvider(String protocol, String providerId) {
		defaultProviderMap.put(protocol, providerId);
	}

	@SuppressWarnings("rawtypes")
	protected INotificationProvider<?> getProvider(String protocol) {
		// Get all providers of this protocol
		List<INotificationProvider> providers = getServiceManager()
				.getServicesNow(INotificationProvider.class,
						NotificationConstants.PROP_PROTOCOL, protocol);
		// Return null if not found
		if (providers == null || providers.isEmpty()) {
			return null;
		}
		// Assume the first one is the correct provider
		INotificationProvider<?> proposedProvider = providers.get(0);
		String defaultProviderId = defaultProviderMap.get(protocol);
		// If default provider is provided, found it
		if (providers.size() > 1 && defaultProviderId != null) {
			for (INotificationProvider<?> provider : providers) {
				if (defaultProviderId.equals(provider.getId())) {
					return provider;
				}
			}
		}
		return proposedProvider;
	}

}
