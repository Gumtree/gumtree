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

import org.gumtree.sics.io.ISicsChannel;
import org.gumtree.sics.io.ISicsChannelMonitor;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.util.SicsCoreProperties;
import org.gumtree.util.messaging.EventBuilder;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

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

	protected EventBuilder createEventBuilder(String topic) {
		EventBuilder builder = new EventBuilder(topic);
		builder.append(ISicsProxy.EVENT_PROP_CHANNEL, getChannelId());
		if (getSicsProxy() != null) {
			builder.append(ISicsProxy.EVENT_PROP_PROXY, getSicsProxy().getId());
		}
		return builder;
	}

	protected ToStringHelper createToStringHelper() {
		ToStringHelper toStringHelper = Objects.toStringHelper(this);
		toStringHelper.add("id", getChannelId());
		if (getSicsProxy() != null) {
			toStringHelper.add("proxy", getSicsProxy().toString());
		} else {
			toStringHelper.add("proxy", null);
		}
		return toStringHelper;
	}
	
}
