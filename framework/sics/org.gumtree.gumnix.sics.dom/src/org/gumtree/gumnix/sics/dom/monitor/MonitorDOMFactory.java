package org.gumtree.gumnix.sics.dom.monitor;


import org.gumtree.gumnix.sics.core.SicsCore;

public class MonitorDOMFactory {

	public MonitorDOMFactory() {
		super();
	}

	public Object getDOMroot() {
		if(!SicsCore.getDefaultProxy().isConnected()) {
			throw new RuntimeException("Sics is not connected.");
		}
		return new MonitorDOM();
	}
	
	public static Object getMonitorDOM() {
		if(!SicsCore.getDefaultProxy().isConnected()) {
			throw new RuntimeException("Sics is not connected.");
		}
		return new MonitorDOM();
	}
}
