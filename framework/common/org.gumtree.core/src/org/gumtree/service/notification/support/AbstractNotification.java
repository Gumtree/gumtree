/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.service.notification.support;

import java.util.Date;

import org.gumtree.service.notification.INotification;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public abstract class AbstractNotification implements INotification {

	private static final long serialVersionUID = -5848032376470317707L;

	private Date timestamp;

	protected AbstractNotification() {
		// Set default timestamp
		// The final timestamp may change if it is grouped
		// under the composite notification object
		this.timestamp = new Date(System.currentTimeMillis());
	}

	public Date getTimestamp() {
		return timestamp;
	}

	protected void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractNotification other = (AbstractNotification) obj;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

	protected ToStringHelper getToStringHelper() {
		ToStringHelper toStringHelper = Objects.toStringHelper(this)
				.add("timestamp", getTimestamp())
				.add("protocol", getProtocol());
		return toStringHelper;
	}
	
	@Override
	public String toString() {
		return getToStringHelper().toString();
	}
	
}
