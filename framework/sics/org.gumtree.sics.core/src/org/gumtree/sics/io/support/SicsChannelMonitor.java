/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.sics.io.support;

import org.gumtree.sics.io.ISicsChannelMonitor;

import com.google.common.base.Objects;

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
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("channelId", getChannelId())
				.toString();
	}
	
}
