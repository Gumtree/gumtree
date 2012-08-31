package org.gumtree.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class JobRunner {

	public static final int WAIT_TIME = 10;

	public static void run(ILoopExitCondition condition) {
		run(condition, null, WAIT_TIME);
	}

	public static void run(ILoopExitCondition condition, Runnable runnable) {
		run(condition, runnable, WAIT_TIME);
	}

	public static void run(ILoopExitCondition condition, int waitTime) {
		run(condition, null, waitTime);
	}

	public static void run(final ILoopExitCondition condition,
			final Runnable runnable, final int waitTime) {
		Job job = new Job("Loop") {
			protected IStatus run(IProgressMonitor monitor) {
				if (!condition.getExitCondition()) {
					schedule(waitTime);
					return Status.OK_STATUS;
				} else {
					if (runnable != null) {
						runnable.run();
					}
					return Status.OK_STATUS;
				}
			};
		};
		job.setSystem(true);
		job.schedule();
	}

}
