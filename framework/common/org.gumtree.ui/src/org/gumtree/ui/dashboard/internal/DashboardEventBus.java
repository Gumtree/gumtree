package org.gumtree.ui.dashboard.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.service.eventbus.IEvent;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.service.eventbus.support.EventBus;

public class DashboardEventBus extends EventBus {

	private Map<Object, String> publisherMap;
	
	private Map<String, List<IEventHandler<?>>> subscriberMap;
	
	public DashboardEventBus() {
		publisherMap = new HashMap<Object, String>();
		subscriberMap = new HashMap<String, List<IEventHandler<?>>>();
	}
	
	public void registerPublisherWidget(Object widget, String publisherId) {
		publisherMap.put(widget, publisherId);
	}
	
	public void registerSubscriberWidger(String publisherId, IEventHandler<?> subscriber) {
		List<IEventHandler<?>> subscribers = subscriberMap.get(publisherId);
		if (subscribers == null) {
			subscribers = new ArrayList<IEventHandler<?>>(2);
			subscriberMap.put(publisherId, subscribers);
		}
		subscribers.add(subscriber);
	}
	
	public void clearWidgetRegistry() {
		publisherMap.clear();
		subscriberMap.clear();
	}
	
	public void syncDispatchEvent(IEvent event) {
		String publisherId = publisherMap.get(event.getPublisher());
		if (publisherId != null) {
			List<IEventHandler<?>> subscribers = subscriberMap.get(publisherId);
			syncDispatchEvent(event, subscribers);
		}
	}
	
	public void dispose() {
		if (publisherMap != null) {
			publisherMap.clear();
			publisherMap = null;
		}
		if (subscriberMap != null) {
			subscriberMap.clear();
			subscriberMap = null;
		}
		super.dispose();
	}
	
}
