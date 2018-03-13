package org.gumtree.control.ui.batch;

public interface IBatchManagerListener {

	void statusChanged(BatchManagerStatus newStatus);
	
	void scriptChanged(String scriptName);
	
	void lineExecutionError(int line);
	
	void lineExecuted(int line);
	
	void charExecuted(int start, int end);

	void queueStarted();

	void queueStopped();
	
}
