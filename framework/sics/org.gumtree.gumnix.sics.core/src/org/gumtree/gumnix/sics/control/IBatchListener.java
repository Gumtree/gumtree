package org.gumtree.gumnix.sics.control;

import org.gumtree.gumnix.sics.control.ISicsBatchControl.BatchStatus;

public interface IBatchListener {

	public void statusChanged(BatchStatus newStatus);

	public void charExecuted(int start, int end);

	public void lineExecuted(int line);
	
	/**
	 * <b>Experimental</b>
	 * <p>
	 * Notifies when a line has failed to execute.
	 * 
	 * @param line
	 */
	public void lineExecutionError(int line);

}
