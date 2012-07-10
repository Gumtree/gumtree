package org.gumtree.sics.core;

public enum SicsMonitorState {

	STARTED, FINISH, BUSY, IDLE;
	
	public boolean isRunning() {
		if (this.equals(STARTED) || this.equals(BUSY)) {
			return true;
		} else {
			return false;
		}
	}
	
}
