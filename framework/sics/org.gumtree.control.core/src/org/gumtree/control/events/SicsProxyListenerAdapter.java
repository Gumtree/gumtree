/**
 * 
 */
package org.gumtree.control.events;

import org.gumtree.control.core.ServerStatus;

/**
 * @author nxi
 *
 */
public abstract class SicsProxyListenerAdapter implements ISicsProxyListener {

	/* (non-Javadoc)
	 * @see org.gumtree.control.events.ISicsProxyListener#connect()
	 */
	@Override
	public void connect() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.events.ISicsProxyListener#disconnect()
	 */
	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.events.ISicsProxyListener#modelUpdated()
	 */
	@Override
	public void modelUpdated() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.events.ISicsProxyListener#interrupt(boolean)
	 */
	@Override
	public void interrupt(boolean isInterrupted) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.events.ISicsProxyListener#setStatus(org.gumtree.control.core.ServerStatus)
	 */
	@Override
	public void setStatus(ServerStatus newStatus) {
		// TODO Auto-generated method stub

	}

}
