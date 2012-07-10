package org.gumtree.workflow.ui.internal.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.WorkflowState;
import org.gumtree.workflow.ui.internal.Workflow;
import org.gumtree.workflow.ui.util.IWorkflowExecutor;
import org.gumtree.workflow.ui.util.IWorkflowRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowExecutor implements IWorkflowExecutor {

	private static final int poolSize = 1;

	private static final int maxPoolSize = 1;
	
	// Do we need this?
	private static final long keepAliveTime = 10;
	
	private static Logger logger = LoggerFactory.getLogger(WorkflowExecutor.class);
	
	private volatile ThreadPoolExecutor threadPool;
	
	private ArrayBlockingQueue<Runnable> queue;
	
	private Map<IWorkflow, IWorkflowRunnable> runnableMap;
	
	public WorkflowExecutor() {
		queue = new ArrayBlockingQueue<Runnable>(5);
		runnableMap = new HashMap<IWorkflow, IWorkflowRunnable>();
	}
	
	public IWorkflowRunnable[] getScheduledWorkflow() {
		return queue.toArray(new IWorkflowRunnable[queue.size()]);
	}

	// TODO: proper error handling on the Runnable
	public synchronized void schedule(final IWorkflow workflow) {
		IWorkflowRunnable runnable = new WorkflowRunnable(workflow);
		synchronized (runnableMap) {
			runnableMap.put(workflow, runnable);
		}
		((Workflow) workflow).setWorkflowState(WorkflowState.SCHEDULED, "Workflow scheduled");
		getThreadPool().execute(runnable);
	}

	public void stop(IWorkflow workflow) {
		synchronized (runnableMap) {
			IWorkflowRunnable runnable = runnableMap.get(workflow);
			if (runnable != null) {
				// Remove it from the queue
				boolean isRemovedFromQueue = getThreadPool().remove(runnable);
				if (!isRemovedFromQueue) {
					// Stop it directly if it has been executed
					workflow.stop();
				} else {
					((Workflow) workflow).setWorkflowState(WorkflowState.FINISHED, "Workflow completed");
				}
				runnableMap.remove(workflow);
			}
		}
	}
	
	private ThreadPoolExecutor getThreadPool() {
		if (threadPool == null) {
			synchronized (this) {
				if (threadPool == null) {
					threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize,
							keepAliveTime, TimeUnit.SECONDS, queue);
				}
			}
		}
		return threadPool;
	}
	
	// This method is used by Spring to dispose this bean.
	public void destroy() {
		if (threadPool != null) {
			threadPool.shutdown();
			threadPool = null;
		}
	}
	
	private class WorkflowRunnable implements IWorkflowRunnable {

		private IWorkflow workflow;
		
		private WorkflowRunnable(IWorkflow workflow) {
			this.workflow = workflow;
		}
		
		public IWorkflow getWorkflow() {
			return workflow;
		}

		public void run() {
			try {
				getWorkflow().run();
			} catch (Exception e) {
				logger.error("Error occured during workflow execution", e);
			} finally {
				synchronized (runnableMap) {
					runnableMap.remove(getWorkflow());
				}
			}
		}
		
	}
	
}
