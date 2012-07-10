package org.gumtree.sics.io;

import org.gumtree.util.messaging.EventBuilder;

public class SicsEventBuilder extends EventBuilder {

	private String proxyId;
	public SicsEventBuilder(String topic, String proxyId) {
		super(topic);
		this.proxyId = proxyId;
	}
	
	public void post() {
		append(ISicsProxy.EVENT_PROP_PROXY, proxyId);
		super.post();
	}
	
	public void send() {
		append(ISicsProxy.EVENT_PROP_PROXY, proxyId);
		super.send();
	}
	
}
