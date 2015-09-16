package au.gov.ansto.bragg.quokka.msw.schedule;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.msw.schedule.execution.IScheduleProvider;
import org.gumtree.msw.schedule.execution.ScheduleStep;

public class SyncScheduleProvider implements IScheduleProvider {
	// fields
	private final Shell shell;
	private final Display display;
	private final IScheduleProvider provider;
	
	// construction
	public SyncScheduleProvider(Shell shell, Display display, IScheduleProvider provider) {
		this.shell = shell;
		this.display = display;
		this.provider = provider;
	}

	// methods
	@Override
	public boolean initiate() {
		final Storage<Boolean> result = new Storage<>(false);
		if (!shell.isDisposed() && (shell.getDisplay() == display))
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					result.value = provider.initiate();
				}
			});
		return result.value;
	}
	@Override
	public void release() {
		if (!shell.isDisposed() && (shell.getDisplay() == display))
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					provider.release();
				}
			});
	}
	@Override
	public ScheduleStep firstStep() {
		final Storage<ScheduleStep> result = new Storage<>(null);
		if (!shell.isDisposed() && (shell.getDisplay() == display))
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					result.value = provider.firstStep();
				}
			});
		return result.value;
	}
	@Override
	public ScheduleStep nextStep() {
		final Storage<ScheduleStep> result = new Storage<>(null);
		if (!shell.isDisposed() && (shell.getDisplay() == display))
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					result.value = provider.nextStep();
				}
			});
		return result.value;
	}
	
	// helper
	private static class Storage<T> {
		// fields
		public T value;
		
		// construction
		public Storage(T value) {
			this.value = value;
		}
	}
}
