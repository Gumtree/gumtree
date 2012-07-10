package org.gumtree.gumnix.sics.internal.io;

import org.gumtree.gumnix.sics.core.SicsCoreProperties;
import org.gumtree.gumnix.sics.io.ISicsChannelMonitor;




public abstract class AbstractSicsChannel implements ISicsChannel {

	protected int transIdCount;
	
	private String channelId;
	
	private ChannelState state;
	
	private SicsProxy sicsProxy;
	
	private SicsChannelMonitor monitor;
	
	// Statistics
	protected long lineRead;
	
	protected long messageProcessed;

	protected long nonJSONMessage;

	protected long invalidMessage;
	
	protected long upTime;
	
	protected long messageDropped;
	
	protected boolean messageDropEnable;
	
	protected AbstractSicsChannel(String channelId, SicsProxy sicsProxy) {
		this.channelId = channelId;
		this.sicsProxy = sicsProxy;
		state = ChannelState.DISCONNECTED;
		monitor = new SicsChannelMonitor(this);
		transIdCount = 1;
		lineRead = 0;
		nonJSONMessage = 0;
		invalidMessage = 0;
		messageProcessed = 0;
		messageDropEnable = SicsCoreProperties.MESSAGE_DROP_ENABLE.getBoolean();
		upTime = System.currentTimeMillis();
	}
	
	public String getChannelId() {
		return channelId;
	}
	
	public ChannelState getChannelState() {
		return state;
	}
	
	public ISicsChannelMonitor getMonitor() {
		return monitor;
	}
	
	protected SicsProxy getSicsProxy() {
		return sicsProxy;
	}
	
	protected void setChannelState(ChannelState state) {
		synchronized (this.state) {
			this.state = state;	
		}
	}
	
	protected void resetMessageDropped() {
		messageProcessed = 0;
	}

}
