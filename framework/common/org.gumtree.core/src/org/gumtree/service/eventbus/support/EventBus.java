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

package org.gumtree.service.eventbus.support;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gumtree.service.eventbus.IEvent;
import org.gumtree.service.eventbus.IEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBus extends AbstractEventBus {

	private static final int NUMBER_OF_THREADS = 1;
	
	private static Logger logger = LoggerFactory.getLogger(EventBus.class);

	private ExecutorService executor;

	private Object dispatchLock = new Object();
	
	public EventBus() {
		this(null);
	}
	
	public EventBus(String id) {
		super(id);
		executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
	}
	
	@Override
	public void asyncDispatchEvent(final IEvent event) {
		// Stop dispatch when disposed
		if (executor == null) {
			return;
		}
		executor.execute(new Runnable() {
			public void run() {
				syncDispatchEvent(event);
			}
		});
	}

	@Override
	public void syncDispatchEvent(IEvent event) {
		syncDispatchEvent(event, getSubscribers(event));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void syncDispatchEvent(IEvent event, List<IEventHandler<?>> subscribers) {
		// Stop dispatch when disposed
		if (executor == null || subscribers == null) {
			return;
		}
		synchronized (dispatchLock) {
			for (IEventHandler subscriber : subscribers) {
				try {
					subscriber.handleEvent(event);
				} catch (Throwable e) {
					logger.warn("Subscriber " + subscriber.toString() +
							" has failed to handle event " + event.toString(), e);
				}
			}			
		}
	}

	public void dispose() {
		if (executor != null) {
			executor.shutdown();
			executor = null;
		}
		super.dispose();
	}
	
}
