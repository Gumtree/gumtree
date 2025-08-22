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

	public boolean isActive = true;
	
	/* (non-Javadoc)
	 * @see org.gumtree.control.events.ISicsProxyListener#connect()
	 */
	@Override
	public void connect() {
	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.events.ISicsProxyListener#disconnect()
	 */
	@Override
	public void disconnect() {
	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.events.ISicsProxyListener#modelUpdated()
	 */
	@Override
	public void modelUpdated() {
	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.events.ISicsProxyListener#interrupt(boolean)
	 */
	@Override
	public void interrupt(boolean isInterrupted) {
	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.events.ISicsProxyListener#setStatus(org.gumtree.control.core.ServerStatus)
	 */
	@Override
	public void setStatus(ServerStatus newStatus) {
	}

	@Override
	public void proxyConnectionReqested() {
	}
	
	@Override
	public void activate() {
		isActive = true;
	}
	
	@Override
	public void deactivate() {
		isActive = false;
	}
	
	@Override
	public boolean isActive() {
		return isActive;
	}
}
