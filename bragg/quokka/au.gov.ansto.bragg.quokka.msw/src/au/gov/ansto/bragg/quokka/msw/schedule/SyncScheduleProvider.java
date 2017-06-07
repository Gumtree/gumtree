package au.gov.ansto.bragg.quokka.msw.schedule;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.msw.schedule.execution.IScheduleProvider;
import org.gumtree.msw.schedule.execution.ScheduleStep;

public class SyncScheduleProvider implements IScheduleProvider {
	// fields
	private final Shell shell;
	private final Display display;
	private final IScheduleProvider provider;
	//
	private final Lock lock;
	private final Condition condition;
	private boolean completed;
	private boolean successful;
	
	// construction
	public SyncScheduleProvider(Shell shell, Display display, IScheduleProvider provider) {
		this.shell = shell;
		this.display = display;
		this.provider = provider;
		
		lock = new ReentrantLock();
		condition = lock.newCondition();
	}

	// methods
	@Override
	public boolean initiate() {
		final Boolean[] result = new Boolean[] { false };
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				result[0] = provider.initiate();
			}
		};
		
		return syncExec(runnable) ? result[0] : false;
	}
	@Override
	public void cleanUp() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				provider.cleanUp();
			}
		};
		
		syncExec(runnable);
	}
	@Override
	public ScheduleStep firstStep() {
		final ScheduleStep[] result = new ScheduleStep[] { null };
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				result[0] = provider.firstStep();
			}
		};
		
		return syncExec(runnable) ? result[0] : null;
	}
	@Override
	public ScheduleStep nextStep() {
		final ScheduleStep[] result = new ScheduleStep[] { null };
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				result[0] = provider.nextStep();
			}
		};
		
		return syncExec(runnable) ? result[0] : null;
	}
	
	// helpers
	private boolean syncExec(final Runnable runnable) {
		// this is a workaround for display.syncExec(...) which
		// blocks when shell is disposed during a run
		
		lock.lock();
		try {
			completed = false;
			successful = false;

			if (shell.isDisposed() || (shell.getDisplay() != display))
				return false;
			
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					lock.lock();
					try {
						try {
							runnable.run();
							successful = true;
						}
						finally {
							completed = true;
							condition.signal();
						}
					}
					finally {
						lock.unlock();
					}
				}
			});
			
			while (!completed) {
				if (shell.isDisposed() || (shell.getDisplay() != display))
					return false;

				try {
					condition.await(10, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException e) {
					throw new Error(e.getMessage());
				}
			}

			return successful;
		}
		finally {
			lock.unlock();
		}
	}
}
