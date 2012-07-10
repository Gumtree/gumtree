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

package org.gumtree.service.eventbus;

import org.gumtree.core.service.IService;

/**
 * Event bus is a centralised event dispatching system based on the
 * publish-subscribe pattern.
 * 
 */
public interface IEventBus extends IService {

	/**
	 * Returns the id of this bus. It is likely that a system may have a number
	 * of event bus at runtime, so this is required to uniquely identity a bus.
	 * 
	 * @return unique id of the bus
	 */
	public String getId();
	
	/**
	 * Asynchronously dispatch an event.
	 *  
	 * @param event
	 */
	public void postEvent(IEvent event);

	/**
	 * Synchronously dispatch an event.
	 *  
	 * @param event
	 */
	public void sendEvent(IEvent event);
	
	/**
	 * Register to listen to all events specific in the event handler type.
	 * 
	 * @param <T>
	 * @param subscriber
	 */
	public <T extends IEvent> void subscribe(IEventHandler<T> subscriber);
	
	/**
	 * Subscribe to listen events from a particular publisher.
	 * 
	 * @param <T>
	 * @param publisher
	 * @param subscriber
	 */
	public <T extends IEvent> void subscribe(Object publisher, IEventHandler<T> subscriber);
	
	/**
	 * Stop listening events of a particular type.
	 * 
	 * @param <T>
	 * @param subscriber
	 */
	public <T extends IEvent> void unsubscribe(IEventHandler<T> subscriber);
	
	/**
	 * Stop listening events from a particular publisher.
	 * 
	 * @param <T>
	 * @param publisher
	 * @param subscriber
	 */
	public <T extends IEvent> void unsubscribe(Object publisher, IEventHandler<T> subscriber);
	
	/**
	 * Clears all subscribers who listen to the supplied publisher in this bus.
	 * 
	 * @param publisher
	 */
	public void clearSubscribers(Object publisher);
	
}
