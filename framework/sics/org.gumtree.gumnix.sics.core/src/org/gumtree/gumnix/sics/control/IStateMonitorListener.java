package org.gumtree.gumnix.sics.control;

public interface IStateMonitorListener {

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
	
	public void stateChanged(SicsMonitorState state, String infoMessage);

}
