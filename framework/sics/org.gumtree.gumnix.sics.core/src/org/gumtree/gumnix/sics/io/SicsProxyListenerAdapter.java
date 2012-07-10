/*******************************************************************************
 * Copyright (c) 2006 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.gumnix.sics.io;

/**
 * This is an empty implementation of the ISicsProxyListener.  Client may
 * extend this class.
 *
 * @since 1.0
 */
public abstract class SicsProxyListenerAdapter implements ISicsProxyListener {

	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.io.ISicsProxyListener#proxyConnected()
	 */
	public void proxyConnected() {
	}

	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.io.ISicsProxyListener#proxyConnectionReqested()
	 */
	public void proxyConnectionReqested() {
	}

	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.io.ISicsProxyListener#proxyDisconnected()
	 */
	public void proxyDisconnected() {
	}

	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.io.ISicsProxyListener#messageReceived(java.lang.String, java.lang.String)
	 */
	public void messageReceived(String message, String channelId) {
	}

	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.io.ISicsProxyListener#messageSent(java.lang.String, java.lang.String)
	 */
	public void messageSent(String message, String channelId) {
	}

}
