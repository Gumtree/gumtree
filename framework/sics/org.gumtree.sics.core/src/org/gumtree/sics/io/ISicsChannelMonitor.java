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

package org.gumtree.sics.io;

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
