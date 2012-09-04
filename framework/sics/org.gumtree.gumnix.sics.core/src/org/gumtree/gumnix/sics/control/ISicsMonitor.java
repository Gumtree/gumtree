package org.gumtree.gumnix.sics.control;

public interface ISicsMonitor {

//	public static final String EVENT_TOPIC_HNOTIFY = "org/gumtree/gumnix/sics/monitor/hnotify";
//	
//	public static final String EVENT_PROP_VALUE = "value";
	
	public void addListener(String path, IHipadabaListener listener);
	
	public void removeListener(String path, IHipadabaListener listener);
	
	public void addStateMonitor(String sicsObject, IStateMonitorListener listener);
	
	public void removeStateMonitor(String sicsObject, IStateMonitorListener listener);
	
	public void addSicsListener(ISicsListener sicsListener);
	
	public void removeSicsListener(ISicsListener sicsListener);
	
}
