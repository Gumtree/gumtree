package org.gumtree.gumnix.sics.ui.internal.transaction;

public interface ITransactionListener {

	ITransactionListener INSTANCE = TransactionListener.getDefault();
	
	public void start();
	
	public void stop();
	
	public ISicsTransaction[] getNewTransactions();
	
}
