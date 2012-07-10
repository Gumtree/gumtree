package org.gumtree.gumnix.sics.core.io;

import java.util.ArrayList;
import java.util.List;

public class InterestListenerGroup implements IInterestListenerGroup {
	private String sicsObjectId;

	private int transactionId;

	private List<ISicsInterestListener> listeners;

	public InterestListenerGroup(String sicsObjectId, int transactionId) {
		this.sicsObjectId = sicsObjectId;
		this.transactionId = transactionId;
	}

	public void addSicsInterestListener(ISicsInterestListener listener) {
		getListeners().add(listener);
	}

	public void removeSicsInterestListener(ISicsInterestListener listener) {
		getListeners().remove(listener);
	}

	public ISicsInterestListener[] getSicsInterestListeners() {
		return getListeners().toArray(
				new ISicsInterestListener[getListeners().size()]);
	}

	private List<ISicsInterestListener> getListeners() {
		if (listeners == null)
			listeners = new ArrayList<ISicsInterestListener>();
		return listeners;
	}

	public String getSicsObjectId() {
		return sicsObjectId;
	}

	public int getTransactionId() {
		return transactionId;
	}
}
