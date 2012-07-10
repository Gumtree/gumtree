package org.gumtree.gumnix.sics.internal.core.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gumtree.gumnix.sics.core.io.IInterestListenerGroup;
import org.gumtree.gumnix.sics.core.io.ISicsChannel;
import org.gumtree.gumnix.sics.core.io.ISicsInterestListener;
import org.gumtree.gumnix.sics.core.io.ISicsProxy;
import org.gumtree.gumnix.sics.core.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.core.io.ISycamoreResponse;
import org.gumtree.gumnix.sics.core.io.InterestListenerGroup;
import org.gumtree.gumnix.sics.core.io.SicsIOException;
import org.gumtree.gumnix.sics.core.io.ISycamoreResponse.Tag;

public class SicsChannel implements ISicsChannel {

	private Socket socket;

	private BufferedReader proxyInput;

	private PrintStream proxyOutput;

	private Thread sicsListener;
	
	private ChannelState channelState;
	
	private Map<Integer, ISicsProxyListener> proxyListeners;
	
	private Map<Integer, IInterestListenerGroup> interestLisntenerGroups;
	
	private ChannelType type;
	
	private static int transIdCount = 1;
	
	private boolean interestTransactionCompleted;
	
	private boolean interestRegistrationError;
	
	public SicsChannel(String host, int port, String login, String password, ChannelType type) throws SicsIOException {
		setChannelState(ChannelState.DISCONNECTED);
		this.type = type;
		connect(host, port);
		login(login, password);
		setSycamore();
	}
	
	private void connect(String host, int port) throws SicsIOException {
		if(getChannelState() != ChannelState.DISCONNECTED)
			throw new SicsIOException("SICS is connected");
		try {
			socket = new Socket(host, port);
			proxyInput = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
			proxyOutput = new PrintStream(socket.getOutputStream());
			sicsListener = new Thread(new SicsListener(proxyInput, this));
			sicsListener.start();
			sicsListener.setPriority(Thread.MAX_PRIORITY);
			while(true) {
				if(getChannelState() != ChannelState.LOGIN)
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				else
					break;
			}
		} catch (UnknownHostException e) {
			throw new SicsIOException(e);
		} catch (IOException e) {
			throw new SicsIOException(e);
		}
	}
	
	// login
	private void login(String login, String password) throws SicsIOException {
		internalSend(login + " " + password);
		while(true) {
			if(getChannelState() != ChannelState.CONNECTED)
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			else
				break;
		}
	}

	// set sycamore
	private void setSycamore() throws SicsIOException {
		internalSend("protocol set sycamore");
		while(true) {
			if(getChannelState() != ChannelState.SYCAMORE)
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			else
				break;
		}
	}
	
	private void internalSend(String command) throws SicsIOException {
		debug("Client sent: " + command);
		if(proxyOutput == null)
			throw new SicsIOException("Connection error");
		proxyOutput.println(command);
		// make sure buffer is emptied
		proxyOutput.flush();
	}

	public void send(String command, ISicsProxyListener proxyListener) throws SicsIOException {
		if(getChannelState() != ChannelState.SYCAMORE)
			throw new SicsIOException("SICS is not connected");
		final int transactionId = transIdCount++;
		getProxyListeners().put(transactionId, proxyListener);
		internalSend("contextdo " + transactionId + " " + command);
	}
	
	// logout
	public void logout() throws SicsIOException {
		try {
			sicsListener = null;
			proxyOutput.close();
			proxyInput.close();
			socket.close();
			setChannelState(ChannelState.DISCONNECTED);
		} catch (IOException e) {
			throw new SicsIOException(e);
		}
	}
	
//	public void addProxyListner(int transactionId, ISicsProxyListener proxyListener) {
//		getProxyListeners().put(transactionId, proxyListener);
//	}
//	
//	public void removeProxyListener(int transactionId) {
//		getProxyListeners().remove(transactionId);
//	}
	
	// return null if none
	public ISicsProxyListener getProxyListener(int transactionId) {
		return getProxyListeners().get(transactionId);
	}
	
    private void debug(String message) {
        if(System.getProperty(ISicsProxy.SYSTEM_PROPERTY_DEBUG) != null) {
            System.out.println(message);
        }
    }

	public ChannelState getChannelState() {
		return channelState;
	}

	public synchronized void setChannelState(ChannelState channelState) {
		this.channelState = channelState;
	}

	public ChannelType getChannelType() {
		return type;
	}
	
	private synchronized Map<Integer, ISicsProxyListener> getProxyListeners() {
		if(proxyListeners == null)
			proxyListeners = new HashMap<Integer, ISicsProxyListener>();
		return proxyListeners;
	}

	private synchronized void cleanupCompletedListeners() {
//		long startTime = System.currentTimeMillis();
		List<Integer> redundantIds = new ArrayList<Integer>();
		for(Entry<Integer, ISicsProxyListener> entry : getProxyListeners().entrySet()) {
			if(entry.getValue().isListenerCompleted())
				redundantIds.add(entry.getKey());
		}
		for(Integer id : redundantIds) {
			getProxyListeners().remove(id);
		}
//		long endTime = System.currentTimeMillis();
//		System.out.println("Cleanup time: " + (endTime - startTime) + "ms");
	}
	
	public void handleResponse(ISycamoreResponse response) {
		if(response.getTag().equalsIgnoreCase(Tag.EVENT.name())) {
			IInterestListenerGroup group = getInterestLisntenerGroups().get(response.getTransactionId());
			if(group != null) {
				for(ISicsInterestListener listener : group.getSicsInterestListeners()) { 
					listener.receiveEvent(response);
				}
			}
			// hack!
//			return;
		}
		// do a clean up on completed listeners
		cleanupCompletedListeners();
		
		ISicsProxyListener proxyListener = getProxyListener(response.getTransactionId());
		if(proxyListener == null) {
			return;
		}
		if(response.getTag().equalsIgnoreCase(Tag.ERROR.name())) {
			proxyListener.receiveError(response);
		} else if(response.getTag().equalsIgnoreCase(Tag.WARNING.name())) {
			proxyListener.receiveWarning(response);
		} else if(response.getTag().equalsIgnoreCase(Tag.FINISH.name())){
			proxyListener.receiveFinish(response);
		} else {
			proxyListener.receiveReply(response);
		}
//		System.out.println("Listener buffer size: " + getProxyListeners().size());
	}

	private Map<Integer, IInterestListenerGroup> getInterestLisntenerGroups() {
		if(interestLisntenerGroups == null)
			interestLisntenerGroups = new HashMap<Integer, IInterestListenerGroup>();
		return interestLisntenerGroups;
	}
	
	public synchronized void addInterestListner(String sicsObjectId, ISicsInterestListener interestListener) throws SicsIOException{
		for(IInterestListenerGroup group : getInterestLisntenerGroups().values()) {
			if(group.getSicsObjectId().equals(sicsObjectId)) {
				group.addSicsInterestListener(interestListener);
				return;
			}
		}
		final int transactionId = transIdCount++;
		try {
			setInterestTransactionCompleted(false);
			setInterestRegistrationError(false);
			final int TIME_OUT = 500;
			final int TIME_WAIT = 50;
			ISicsProxyListener listener = new SicsProxyListenerAdapter() {
				public void receiveReply(ISycamoreResponse response) {
					if(SycamoreResponseUtil.getValueAt(response, 0).equals("OK")) {
						setInterestTransactionCompleted(true);
						setListenerCompleted(true);
					}
				}
				public void receiveError(ISycamoreResponse response) {
					setInterestRegistrationError(true);
				}
			};
			getProxyListeners().put(transactionId, listener);
			internalSend("contextdo " + transactionId + " " + sicsObjectId + " interest");
			int timeCount = 0;
			while(!isInterestTransactionCompleted()) {
				if(isInterestRegistrationError()) {
					throw new SicsIOException("fail to add interest for " + sicsObjectId);
				}
				if(timeCount >= TIME_OUT) {
					throw new SicsIOException("fail to add interest for " + sicsObjectId);
				}
				try {
					wait(TIME_WAIT);
					timeCount += TIME_WAIT;
				} catch (InterruptedException ex) {
					break;
				}
			}
		} catch (SicsIOException e) {
			throw e;
		}
		IInterestListenerGroup group = new InterestListenerGroup(sicsObjectId, transactionId);
		getInterestLisntenerGroups().put(transactionId, group);
		group.addSicsInterestListener(interestListener);
	}

	public void removeInterestListener(String sicsObjectId, ISicsInterestListener interestListener) throws SicsIOException{
		for(IInterestListenerGroup group : getInterestLisntenerGroups().values()) {
			if(group.getSicsObjectId().equals(sicsObjectId)) {
				group.removeSicsInterestListener(interestListener);
//				if(group.getSicsInterestListeners().length == 0) {
//					try {
//						internalSend(sicsObjectId + " uninterest");
//					} catch (SicsIOException e) {
//						throw e;
//					}
//					getInterestLisntenerGroups().remove(group.getTransactionId());
//				}
			}
		}
	}

	private boolean isInterestRegistrationError() {
		return interestRegistrationError;
	}

	private void setInterestRegistrationError(boolean interestRegistrationError) {
		this.interestRegistrationError = interestRegistrationError;
	}

	private boolean isInterestTransactionCompleted() {
		return interestTransactionCompleted;
	}

	private void setInterestTransactionCompleted(boolean interestTransactionCompleted) {
		this.interestTransactionCompleted = interestTransactionCompleted;
	}

}
