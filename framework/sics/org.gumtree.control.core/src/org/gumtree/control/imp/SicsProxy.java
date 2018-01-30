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

	private String serverAddress;
	private String publisherAddress;
	private ISicsChannel channel;
	
	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#connect()
	 */
	@Override
	public boolean connect(String serverAddress, String publisherAddress) {
		this.serverAddress = serverAddress;
		this.publisherAddress = publisherAddress;
		channel = new SicsChannel();
		try {
			channel.connect(serverAddress, publisherAddress);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#disconnect()
	 */
	@Override
	public void disconnect() {
		if (channel != null) {
			channel.disconnect();
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#isConnected()
	 */
	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return channel.isConnected();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#send(java.lang.String, org.gumtree.control.core.ISicsCallback, java.lang.String)
	 */
	@Override
	public void send(String command, ISicsCallback callback, String channelName) throws SicsException {
		if (channel != null) {
			channel.send(command);
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#syncRun(java.lang.String)
	 */
	@Override
	public String syncRun(String command) throws SicsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISicsChannel getSicsChannel() {
		// TODO Auto-generated method stub
		return channel;
	}

	
}
