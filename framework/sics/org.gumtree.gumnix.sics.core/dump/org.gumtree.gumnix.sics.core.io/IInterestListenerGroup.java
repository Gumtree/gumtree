package org.gumtree.gumnix.sics.core.io;

public interface IInterestListenerGroup {
	public String getSicsObjectId();

	public int getTransactionId();

	public void addSicsInterestListener(ISicsInterestListener listener);

	public void removeSicsInterestListener(ISicsInterestListener listener);

	public ISicsInterestListener[] getSicsInterestListeners();
}
