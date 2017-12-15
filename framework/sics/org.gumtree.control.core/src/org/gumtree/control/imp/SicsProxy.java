/**
 * 
 */
package org.gumtree.control.imp;

import org.gumtree.control.core.ISicsCallback;
import org.gumtree.control.core.ISicsChannel;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.exception.SicsCommunicationException;
import org.gumtree.control.exception.SicsException;

/**
 * @author nxi
 *
 */
public class SicsProxy implements ISicsProxy {

	private String server;
	private ISicsChannel generalChannel;
	private ISicsChannel statusChannel;
	private ISicsChannel modelChannel;
	
	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#connect()
	 */
	@Override
	public boolean connect(String server) {
		this.server = server;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#disconnect()
	 */
	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#isConnected()
	 */
	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#send(java.lang.String, org.gumtree.control.core.ISicsCallback, java.lang.String)
	 */
	@Override
	public void send(String command, ISicsCallback callback, String channelName) throws SicsCommunicationException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#syncRun(java.lang.String)
	 */
	@Override
	public String syncRun(String command) throws SicsException {
		// TODO Auto-generated method stub
		return null;
	}

}
