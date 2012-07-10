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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gumtree.service.eventbus.IEvent;
import org.gumtree.service.eventbus.IEventBus;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.service.eventbus.IFilteredEventHandler;

public abstract class AbstractEventBus implements IEventBus {

	private static final Object ALL_PUBLISHER = new Object();

	// Event bus id
	private String id;
	
	// Cache of all handlers, categorised by event class type
	// Map<EventType, Map<Publisher, Handlers>>
	private Map<Class<?>, Map<Object, List<IEventHandler<?>>>> eventTable;
	
	public AbstractEventBus() {
		this(null);
	}
	
	public AbstractEventBus(String id) {
		this.id = id;
		// Use a thread safe collection
		eventTable = new ConcurrentHashMap<Class<?>, 
			Map<Object, List<IEventHandler<?>>>>(2);
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.core.eventbus.IEventBus#getId()
	 */
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	protected Map<Class<?>, Map<Object, List<IEventHandler<?>>>> getEventTable() {
		return eventTable;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.gumtree.core.eventbus.IEventBus#subscribe(org.gumtree.core.eventbus
	 * .IEventHandler)
	 */
	public <T extends IEvent> void subscribe(IEventHandler<T> subscriber) {
		subscribe(ALL_PUBLISHER, subscriber);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.core.eventbus.IEventBus#subscribe(java.lang.Object, org.gumtree.core.eventbus.IEventHandler)
	 */
	public <T extends IEvent> void subscribe(Object publisher, IEventHandler<T> subscriber) {
		Class<?> type = findEventType(subscriber);
		if (type == null) {
			return;
		}
		Map<Object, List<IEventHandler<?>>> handlerTable = eventTable.get(type);
		if (handlerTable == null) {
			synchronized (eventTable) {
				if (eventTable.get(type) == null) {
					handlerTable = new ConcurrentHashMap<Object, List<IEventHandler<?>>>(2);
					eventTable.put(type, handlerTable);
				}
			}
		}
		List<IEventHandler<?>> handlers = handlerTable.get(publisher);
		if (handlers == null) {
			synchronized (eventTable) {
				if (handlerTable.get(publisher) == null) {
					handlers = new CopyOnWriteArrayList<IEventHandler<?>>(); 
					handlerTable.put(publisher, handlers);
				}
			}
		}
		handlers.add(subscriber);
	}

	public <T extends IEvent> void unsubscribe(IEventHandler<T> subscriber) {
		unsubscribe(ALL_PUBLISHER, subscriber);
	}

	public <T extends IEvent> void unsubscribe(Object publisher, IEventHandler<T> subscriber) {
		Class<?> type = findEventType(subscriber);
		if (type == null) {
			return;
		}
		Map<Object, List<IEventHandler<?>>> handlerTable = eventTable.get(type);
		if (handlerTable != null) {
			List<IEventHandler<?>> handlers = handlerTable.get(publisher);
			if (handlers != null) {
				handlers.remove(subscriber);
				// Clean up
				if (handlers.size() == 0) {
					synchronized (eventTable) {
						handlerTable.remove(publisher);
						if (handlerTable.size() == 0) {
							eventTable.remove(type);
						}
					}
				}
			}
		}
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.gumtree.core.eventbus.IEventBus#postEvent(org.gumtree.core.eventbus
	 * .IEvent)
	 */
	public void postEvent(final IEvent event) {
		// Dispatch event asynchronously
		asyncDispatchEvent(event);
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.gumtree.core.eventbus.IEventBus#sendEvent(org.gumtree.core.eventbus
	 * .IEvent)
	 */
	public void sendEvent(IEvent event) {
		// Dispatch event synchronously
		syncDispatchEvent(event);
	}
	
	public void clearSubscribers(Object publisher) {
		if (publisher == null) {
			return;
		}
		// Loop for all class key and find all possible hander tables
		for (Entry<Class<?>, Map<Object, List<IEventHandler<?>>>> entry : eventTable.entrySet()) {
			Map<Object, List<IEventHandler<?>>> handlerTable = entry.getValue();
			synchronized (handlerTable) {
				if (handlerTable.containsKey(publisher)) {
					synchronized (handlerTable.get(publisher)) {
						handlerTable.get(publisher).clear();	
					}
					handlerTable.remove(publisher);
				}
			}	
		}
	}
	
	/**
	 * Retrieves subscribers of a given event from the cache.'
	 * 
	 * @param event
	 * @return
	 */
	protected List<IEventHandler<?>> getSubscribers(IEvent event) {
		List<IEventHandler<?>> subscribers = new ArrayList<IEventHandler<?>>();
		// Check initial condition
		if (event == null || event.getPublisher() == null) {
			return subscribers;
		}
		
		// Loop for all class key and find all possible hander tables
		for (Entry<Class<?>, Map<Object, List<IEventHandler<?>>>> entry : eventTable.entrySet()) {
			if (entry.getKey().isAssignableFrom(event.getClass())) {
				Map<Object, List<IEventHandler<?>>> handlerTable = entry.getValue();
				// Specific publisher
				List<IEventHandler<?>> handlers = handlerTable.get(event.getPublisher());
				if (handlers != null) {
					filterEventHandler(event, handlers, subscribers);
				}
				// All publishers
				handlers = handlerTable.get(ALL_PUBLISHER);
				if (handlers != null) {
					filterEventHandler(event, handlers, subscribers);
				}
			}
		}
		
		return subscribers;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void filterEventHandler(IEvent event, List<IEventHandler<?>> rawList, List<IEventHandler<?>> resultList) {
		for (IEventHandler<?> handler : rawList) {
			if (handler instanceof IFilteredEventHandler) {
				if (((IFilteredEventHandler) handler).isDispatchable(event)) {
					resultList.add(handler);
				}
			} else {
				resultList.add(handler);
			}
		}
	}
	
	/**
	 * Discover the generic type of an event handler.
	 * 
	 * @param <T>
	 * @param subscriber
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected static <T extends IEvent> Class findEventType(
			IEventHandler<? super T> subscriber) {
		if (subscriber == null) {
			return null;
		}
		
		// Get generic information
		Type[] types = subscriber.getClass().getGenericInterfaces();
		// We only support single generic type
		if (types.length == 1 && types[0] instanceof ParameterizedType) {
			// Get the actual class object
			Type[] actualTypes = ((ParameterizedType) types[0])
					.getActualTypeArguments();
			if (actualTypes.length == 1) {
				return (Class) actualTypes[0];
			}
		}
		
		return null;
	}
	
	public void dispose() {
		if (eventTable != null) {
			eventTable.clear();
			eventTable = null;
		}
	}
	
	/*************************************************************************
	 * Subclass methods
	 *************************************************************************/
	
	/**
	 * Handle event dispatch in an asynchronous way
	 * 
	 * @param event
	 */
	protected abstract void asyncDispatchEvent(IEvent event);
	
	/**
	 * Handle event dispatch in a synchronous way
	 * 
	 * @param event
	 */
	protected abstract void syncDispatchEvent(IEvent event);
	
}
