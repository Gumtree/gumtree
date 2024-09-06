package org.gumtree.control.ui.batch;

import org.gumtree.control.batch.BatchStatus;

public interface IBatchManagerListener {

	void statusChanged(BatchStatus newStatus);
	
	void scriptChanged(String scriptName);
	
	void lineExecutionError(int line);
	
	void lineExecuted(int line);
	
	void charExecuted(int start, int end);

	void queueStarted();

	void queueStopped();
	
}
