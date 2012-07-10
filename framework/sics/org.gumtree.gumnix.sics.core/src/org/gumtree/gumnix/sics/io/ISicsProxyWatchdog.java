package org.gumtree.gumnix.sics.io;

public interface ISicsProxyWatchdog {

	public long getTimeoutInSecond();
	
	public void setTimeoutInSecond(long timeoutInSecond);
	
	public void notifySicsStalled();
	
	public long getNotificationSent();
	
}
