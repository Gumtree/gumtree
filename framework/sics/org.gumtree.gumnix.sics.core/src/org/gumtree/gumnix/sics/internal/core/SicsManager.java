package org.gumtree.gumnix.sics.internal.core;

import org.gumtree.gumnix.sics.control.ISicsControl;
import org.gumtree.gumnix.sics.control.ISicsMonitor;
import org.gumtree.gumnix.sics.core.ISicsCoreService;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.internal.control.SicsControl;
import org.gumtree.gumnix.sics.internal.control.SicsMonitor;
import org.gumtree.gumnix.sics.internal.io.SicsProxy;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsManager implements ISicsManager {

	private static final Logger logger = LoggerFactory.getLogger(SicsManager.class);

	private volatile ISicsCoreService service;

	private volatile ISicsControl control;

	private volatile ISicsProxy proxy;

	private volatile ISicsMonitor monitor;
	
	public SicsManager() {
		super();
	}

	public ISicsProxy proxy() {
		if(proxy == null) {
			synchronized (this) {
				if(proxy == null) {
					proxy = new SicsProxy();
					logger.info("Created SICS proxy.");
				}
			}
		}
		return proxy;
	}

	public ISicsControl control() {
		if(control ==  null) {
			synchronized (this) {
				if(control ==  null) {
					control = new SicsControl(this);		
				}
			}
		}
		return control;
	}

	public ISicsCoreService service() {
		if(service == null) {
			synchronized (this) {
				if(service == null) {
					service = new SicsCoreService(this);
				}
			}
		}
		return service;
	}

	public ISicsMonitor monitor() {
		if (monitor == null) {
			synchronized (this) {
				if (monitor == null) {
					monitor = new SicsMonitor(proxy());
				}
			}
		}
		return monitor;
	}
	
}
