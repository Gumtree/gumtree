package org.gumtree.sics.core;

import org.gumtree.core.object.IDisposable;
import org.gumtree.sics.io.ISicsProxy;

public interface ISicsMonitor extends IDisposable {

	public static final String EVENT_TOPIC_HNOTIFY = "org/gumtree/sics/monitor/hnotify";
	
	public static final String EVENT_TOPIC_STATEMON = "org/gumtree/sics/monitor/statemon";
	
	public static final String EVENT_TOPIC_INTERRUPT = "org/gumtree/sics/monitor/interrupt";
	
	public static final String EVENT_TOPIC_SERVER = "org/gumtree/sics/monitor/server";
	
	public static final String EVENT_PROP_INTERRUPT_LEVEL = "interruptLevel";
	
	public static final String EVENT_PROP_VALUE = "value";
	
	public static final String EVENT_PROP_STATUS = "status";
	
	public static final String EVENT_PROP_STATE = "state";
	
	public static final String EVENT_PROP_MESSAGE = "message";
	
	public ISicsProxy getProxy();
	
	public void setProxy(ISicsProxy proxy);
	
}
