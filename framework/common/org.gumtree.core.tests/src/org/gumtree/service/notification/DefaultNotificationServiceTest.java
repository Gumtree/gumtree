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

package org.gumtree.service.notification;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.gumtree.core.service.IServiceRegistrationManager;
import org.gumtree.core.service.ServiceManager;
import org.gumtree.core.service.ServiceRegistrationManager;
import org.gumtree.core.tests.internal.Activator;
import org.gumtree.service.notification.support.AbstractNotification;
import org.gumtree.service.notification.support.AbstractNotificationProvider;
import org.gumtree.service.notification.support.DefaultNotificationService;
import org.gumtree.service.persistence.support.ObjectContainerManager;
import org.gumtree.util.collection.CollectionUtils;
import org.junit.Test;

import com.db4o.osgi.Db4oServiceImpl;

public class DefaultNotificationServiceTest {

	@Test
	public void testNotificationService() throws InterruptedException {
		// Create notification server
		DefaultNotificationService service = new DefaultNotificationService();
		ServiceManager serviceManager = new ServiceManager();
		service.setServiceManager(serviceManager);
		ObjectContainerManager objectContainerManager = new ObjectContainerManager();
		objectContainerManager.setServiceManager(serviceManager);
		objectContainerManager.setDb4oService(new Db4oServiceImpl());
		service.setObjectContainerManager(objectContainerManager);


		// Inject provider
		IServiceRegistrationManager manager = new ServiceRegistrationManager(
				Activator.getDefault().getBundle().getBundleContext());
		final DummyNotificationProvider provider = new DummyNotificationProvider();
		manager.registerService(INotificationProvider.class, provider,
				CollectionUtils.createMap(NotificationConstants.PROP_PROTOCOL,
						DummyNotification.PROTOCOL));
		DummyNotification notification = new DummyNotification();

		// Dispatch
		service.send(notification);

		// Wait
		INotification processedNotification = provider.getQueue().take();

		// Test if the notification has been dispatched
		assertEquals(notification, processedNotification);
	}

}

class DummyNotification extends AbstractNotification {

	private static final long serialVersionUID = -2394456739789700003L;

	protected static final String PROTOCOL = "dummy";

	public String getProtocol() {
		return PROTOCOL;
	}

}

class DummyNotificationProvider extends
		AbstractNotificationProvider<DummyNotification> {

	private BlockingQueue<INotification> queue;

	public DummyNotificationProvider() {
		queue = new ArrayBlockingQueue<INotification>(2);
	}

	public void handleNotification(DummyNotification notification) {
		try {
			queue.put(notification);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public BlockingQueue<INotification> getQueue() {
		return queue;
	}

	public boolean isLongOperation(DummyNotification notification) {
		return false;
	}

}