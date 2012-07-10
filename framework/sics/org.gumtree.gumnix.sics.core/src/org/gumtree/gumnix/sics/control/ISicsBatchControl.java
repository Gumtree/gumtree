package org.gumtree.gumnix.sics.control;

import org.gumtree.gumnix.sics.io.SicsIOException;

public interface ISicsBatchControl {

//	public static final String BATCH_NAME = "gumtreeBatch.tcl";

	public enum BatchStatus {
		DISCONNECTED, IDLE, READY, RUNNING
	}

	public BatchStatus getStatus();

	// Experimental....to be removed
//	public void run(String filename) throws SicsIOException, IOException;

	public void run(String[] commands, String scriptName) throws SicsIOException;

	public void interrupt() throws SicsIOException;

//	public ISicsBatchValidator validator();

	public void addListener(IBatchListener listener);

	public void removeListener(IBatchListener listener);

}
