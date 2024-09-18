package org.gumtree.control.batch;


public interface IBatchListener {

	void statusChanged(BatchStatus newStatus);
	
	void scriptChanged(String scriptName);

	void charExecuted(int start, int end);

	void rangeExecuted(String rangeText);
	
	void lineExecuted(int line);
	
	void lineExecutionError(int line);

	void start();

	void stop();
	
}
