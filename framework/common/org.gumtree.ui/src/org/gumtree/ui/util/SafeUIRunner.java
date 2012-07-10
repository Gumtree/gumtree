package org.gumtree.ui.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SafeUIRunner {

	private static Logger logger = LoggerFactory.getLogger(SafeUIRunner.class);
	
	public static void syncExec(final ISafeRunnable runnable) {
		syncExec(runnable, logger);
	}
	
	public static void syncExec(final ISafeRunnable runnable, final Logger logger) {
		Display display = Display.getDefault();
		if (display.isDisposed()) {
			logger.warn("Display has been disposed.");
		}
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				// This will look after error handling
				SafeRunner.run(runnable);
			}
		});
	}
	
	public static void asyncExec(final ISafeRunnable runnable) {
		asyncExec(runnable, logger);
	}
	
	public static void asyncExec(final ISafeRunnable runnable, final Logger logger) {
		Display display = Display.getDefault();
		if (display.isDisposed()) {
			logger.warn("Display has been disposed.");
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				// This will look after error handling
				SafeRunner.run(runnable);
			}
		});
	}
	
	public static void asyncExec(final ISafeRunnable runnable, int minDelay) {
		asyncExec(runnable, logger, minDelay);
	}
	
	public static void asyncExec(final ISafeRunnable runnable, final Logger logger, int minDelay) {
		Job job = new Job("SafeUIRunner") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				asyncExec(runnable);
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule(minDelay);
	}
	
	public static void asyncExecBusy(final ISafeRunnable runnable) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
					@Override
					public void run() {
						SafeRunner.run(runnable);
					}
				});
			}
		});
	}
	
	private SafeUIRunner() {
		super();
	}
	
}
