package org.gumtree.gumnix.sics.core;

import org.gumtree.core.management.IManageableBean;

public interface ISicsProxyMonitor extends IManageableBean {

	public String getProxyStatus();
	
	public String getCurrentRole();
	
}
