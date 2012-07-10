package org.gumtree.gumnix.sics.io;

public interface ISicsChannelMonitor {

	public String getChannelId();
	
	public String getState();
	
	public long getLineReceived();
	
	public double getLineReceivedPerMinute();
	
	public long getMessageProcessed();
	
	public long getInvalidMessageReceived();
	
	public long getNonJSONMessage();

	public int getMessageSent();
	
	public long getUpTimeInMinute(); 
	
	public boolean isMessageDroppedEnable();
	
	public void setMessageDroppedEnable(boolean enable);
	
	public long getMessageDropped();
	
	public void resetMessageDropped();
	
}
