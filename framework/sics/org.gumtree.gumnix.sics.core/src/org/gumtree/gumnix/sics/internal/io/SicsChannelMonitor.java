package org.gumtree.gumnix.sics.internal.io;

import org.gumtree.gumnix.sics.io.ISicsChannelMonitor;

public class SicsChannelMonitor implements ISicsChannelMonitor {

	private AbstractSicsChannel channel;

	public SicsChannelMonitor(AbstractSicsChannel channel) {
		this.channel = channel;
	}

	@Override
	public String getChannelId() {
		return channel.getChannelId();
	}

	@Override
	public String getState() {
		return channel.getChannelState().name();
	}

	@Override
	public long getLineReceived() {
		return channel.lineRead;
	}

	@Override
	public double getLineReceivedPerMinute() {
		return (channel.lineRead / (double) (System.currentTimeMillis() - channel.upTime)) * 1000 * 60;
	}

	@Override
	public long getMessageProcessed() {
		return channel.messageProcessed;
	}

	@Override
	public long getInvalidMessageReceived() {
		return channel.invalidMessage;
	}

	@Override
	public long getNonJSONMessage() {
		return channel.nonJSONMessage;
	}

	@Override
	public int getMessageSent() {
		return channel.transIdCount - 1;
	}


	public long getUpTimeInMinute() {
		return (System.currentTimeMillis() - channel.upTime) / 1000 / 60;
	}

	@Override
	public boolean isMessageDroppedEnable() {
		return channel.messageDropEnable;
	}

	@Override
	public void setMessageDroppedEnable(boolean enable) {
		channel.messageDropEnable = enable;
	}

	@Override
	public long getMessageDropped() {
		return channel.messageDropped;
	}

	@Override
	public void resetMessageDropped() {
		channel.resetMessageDropped();
	}

}
