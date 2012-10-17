package org.gumtree.gumnix.sics.io;

public interface ISicsProxyWatchdog {

	public long getTimeout();
	
	public void setTimeout(long timeoutInSecond);
	
	public void notifySicsStalled();
	
	public long getNotificationSent();
	
}
