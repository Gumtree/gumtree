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

package org.gumtree.control.events;

import org.gumtree.control.core.ISicsReplyData;

/**
 * Interface for sics proxy callback. Proxy callback receives corresponding
 * outputs if sics delivers them to the client.
 *
 * @since 1.0
 */
public interface ISicsCallback {

	/**
	 * This method is called from client receives output with tag "OUT" and
	 * "STATUS".
	 *
	 * @param response
	 *            output from sics in json format
	 */
	public void receiveReply(ISicsReplyData data);

	/**
	 * This method is called from client receives output with tag "ERROR".
	 *
	 * @param response
	 *            output from sics in json format
	 */
	public void receiveError(ISicsReplyData data);

	/**
	 * This method is called from client receives output with tag "WARNING".
	 *
	 * @param response
	 *            output from sics in json format
	 */
	public void receiveWarning(ISicsReplyData data);

	/**
	 * This method is called from client receives output with tag "FINISH".
	 *
	 * @param response
	 *            output from sics in json format
	 */
	public void receiveFinish(ISicsReplyData data);

	/**
	 * This method is called from client receives raw data output (usually non-JSON data)
	 * @param data
	 */
	public void receiveRawData(Object data);

	/**
	 * Returns whether this callback is completed or not. When a listener is not
	 * completed, it will continue to receive corresponding response from sics
	 * if there is any.
	 *
	 * @return true if listener is completed; false otherwise
	 */
	public boolean isCallbackCompleted();

	/**
	 * If listener is no longer interest in receiving response from sics,
	 * setListnerCompleted should be set to true.
	 *
	 * @param completed
	 */
	public void setCallbackCompleted(boolean completed);

	/**
	 * Returns whether this callback has encountered any error callback.
	 *
	 * @return true if callback received error
	 */
	public boolean hasError();

	/**
	 * If receiveError() is called, class which implements this interface should call
	 * this method to indicate error.
	 *
	 * @param error true for error; false otherwise
	 */
	public void setError(boolean error);

}
