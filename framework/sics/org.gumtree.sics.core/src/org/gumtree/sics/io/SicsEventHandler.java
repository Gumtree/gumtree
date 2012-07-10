package org.gumtree.sics.io;

import org.gumtree.util.messaging.EventHandler;
import org.osgi.service.event.Event;

public abstract class SicsEventHandler extends EventHandler {

	private String proxyId;
	
	public SicsEventHandler(String topic) {
		this(topic, null);
	}
	
	public SicsEventHandler(String topic, String proxyId) {
		super(topic);
		this.proxyId = proxyId;
	}

	@Override
	public void handleEvent(Event event) {
		if (getString(event, ISicsProxy.EVENT_PROP_PROXY).equals(getProxyId())) {
			handleSicsEvent(event);
		}
	}
	
	public abstract void handleSicsEvent(Event event);

	public String getProxyId() {
		return proxyId;
	}

	public SicsEventHandler setProxyId(String proxyId) {
		this.proxyId = proxyId;
		return this;
	}

}
