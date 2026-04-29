/**
 * 
 */
package org.gumtree.control.ui.batch;

import org.gumtree.control.batch.BatchStatus;

/**
 * @author nxi
 *
 */
public class BatchManagerListenerAdapter implements IBatchManagerListener {

	@Override
	public void statusChanged(BatchStatus newStatus, String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scriptChanged(String scriptName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rangeChanged(String rangeText) {
		// TODO Auto-generated method stub

	}

	@Override
	public void lineExecutionError(int line) {
		// TODO Auto-generated method stub

	}

	@Override
	public void lineExecuted(int line) {
		// TODO Auto-generated method stub

	}

	@Override
	public void charExecuted(int start, int end) {
		// TODO Auto-generated method stub

	}

	@Override
	public void queueStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queueStopped() {
		// TODO Auto-generated method stub

	}

	@Override
	public void outputReceived(String outputText) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scriptFinished(String replyText) {
		// TODO Auto-generated method stub

	}

}
