package org.gumtree.control.batch;

import org.gumtree.control.events.ISicsMessageListener;

public abstract class SicsMessageAdapter implements ISicsMessageListener {
	
	private boolean isEnabled;

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	} 
	
	
}
