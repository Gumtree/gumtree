package org.gumtree.gumnix.sics.ui.internal.transaction;

import java.util.List;
import java.util.Vector;

import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;

public class TransactionListener implements ITransactionListener {

	private static TransactionListener singleton;
	
	private ISicsProxyListener proxyListener;
	
	private Vector<ISicsTransaction> newTransaction;
	
	private TransactionListener() {
		super();
		newTransaction = new Vector<ISicsTransaction>();
	}
	
	public static ITransactionListener getDefault() {
		if(singleton == null) {
			singleton = new TransactionListener();
		}
		return singleton;
	}
	
	public void start() {
		SicsCore.getDefaultProxy().addProxyListener(getProxyListener());
	}
	
	public void stop() {
		SicsCore.getDefaultProxy().removeProxyListener(getProxyListener());
	}
	
	public ISicsTransaction[] getNewTransactions() {
		synchronized (newTransaction) {
			ISicsTransaction[] result =  newTransaction.toArray(new ISicsTransaction[newTransaction.size()]);
			newTransaction.clear();
			return result;
		}
	}
	
	private ISicsProxyListener getProxyListener() {
		if(proxyListener == null) {
			proxyListener = new SicsProxyListenerAdapter() {
				public void messageSent(final String message, String channelId) {
					ISicsTransaction trans = new SicsTransaction(message, channelId, TransactionType.SENT);
					synchronized (newTransaction) {
						newTransaction.add(trans);
					}
				}
				public void messageReceived(final String message, String channelId) {
					ISicsTransaction trans = new SicsTransaction(message, channelId, TransactionType.RECEIVED);
					synchronized (newTransaction) {
						newTransaction.add(trans);
					}
				}
			};
		}
		return proxyListener;
	}
	
}
