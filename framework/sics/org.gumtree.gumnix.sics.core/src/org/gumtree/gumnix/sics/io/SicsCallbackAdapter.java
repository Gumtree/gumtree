/*******************************************************************************
 * Copyright (c) 2004  Australian Nuclear Science and Technology Organisation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * GumTree Platform is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.gumnix.sics.io;

/**
 * Default implementation of an ISicsProxyListener.
 *
 */
public abstract class SicsCallbackAdapter implements ISicsCallback {

	private boolean callbackCompleted = false;

	private boolean errorFlag = false;

	/* (non-Javadoc)
	 * @see sycamoretest.io.ISicsProxyListener#receiveReply(sycamoretest.io.ISycamoreResponse)
	 */
	public void receiveReply(ISicsReplyData data) {
	}

	/* (non-Javadoc)
	 * @see sycamoretest.io.ISicsProxyListener#receiveError(sycamoretest.io.ISycamoreResponse)
	 */
	public void receiveError(ISicsReplyData data) {
	}

	/* (non-Javadoc)
	 * @see sycamoretest.io.ISicsProxyListener#receiveWarning(sycamoretest.io.ISycamoreResponse)
	 */
	public void receiveWarning(ISicsReplyData data) {
	}

	/* (non-Javadoc)
	 * @see sycamoretest.io.ISicsProxyListener#receiveFinish(sycamoretest.io.ISycamoreResponse)
	 */
	public void receiveFinish(ISicsReplyData data) {
	}

	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.io.ISicsCallback#receiveRawData(java.lang.Object)
	 */
	public void receiveRawData(Object data) {
	}

	/* (non-Javadoc)
	 * @see sycamoretest.io.ISicsProxyListener#isListenerCompleted()
	 */
	public boolean isCallbackCompleted() {
		return callbackCompleted;
	}

	/* (non-Javadoc)
	 * @see sycamoretest.io.ISicsProxyListener#setListenerCompleted(boolean)
	 */
	public void setCallbackCompleted(boolean completed) {
		callbackCompleted = completed;
	}

	public boolean hasError() {
		return errorFlag;
	}

	public void setError(boolean error) {
		this.errorFlag = error;
	}

}
