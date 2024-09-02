package org.gumtree.control.batch;

import org.gumtree.control.batch.BatchControl.BatchInfo;
import org.gumtree.control.exception.SicsException;

public interface IBatchControl {

	public BatchStatus getStatus();

	// Experimental....to be removed
//	public void run(String filename) throws SicsIOException, IOException;

	public void run(String[] commands, String scriptName) throws SicsException;

	public void interrupt() throws SicsException;

//	public ISicsBatchValidator validator();

	public void addListener(IBatchListener listener);

	public void removeListener(IBatchListener listener);

	void fireBatchEvent(String type, String value);

	public void parseState(String state, String substring);
	
	public BatchInfo getBatchInfo();
	
	public String getBatchRange();
	
	public String getBatchText();
	
	public String getBatchName();
	
	public String getBatchId();

	public void resetStatus();
}
