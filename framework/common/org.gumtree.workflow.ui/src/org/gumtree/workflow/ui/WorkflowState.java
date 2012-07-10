package org.gumtree.workflow.ui;

public enum WorkflowState {

	/**
	 * Unscheduled
	 */
	NEW,
	
	/**
	 * Running
	 */
	RUNNING,
	
	/**
	 * Scheduled to run
	 */
	SCHEDULED,
	
	/**
	 * Interrupted during run
	 */
	STOPPING,
	
	/**
	 * Pausing during run
	 */
	PAUSING,
	
	/**
	 * Paused during run
	 */
	PAUSED,
	
	/**
	 * Successfully completed
	 */
	FINISHED

}
