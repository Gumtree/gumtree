package au.gov.ansto.bragg.quokka.msw.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gumtree.msw.schedule.execution.Summary;

public class CustomInstrumentAction {
	// finals
	public static final String DRIVE_TO_LOAD_POSITION = "DriveToLoadPosition";
	public static final String DRIVE_TO_SAMPLE_POSITION = "DriveToSamplePosition";
	public static final String TEST_DRIVE = "TestDrive";
	
	// fields
	private final AtomicBoolean busy;
	private final AtomicBoolean enabled;
	// listeners
	private final List<ICustomInstrumentActionListener> listeners;

	// construction
	public CustomInstrumentAction() {
		busy = new AtomicBoolean(false);
		enabled = new AtomicBoolean(true);
		listeners = new ArrayList<>();
	}
	
	// properties
	public boolean isBusy() {
		return busy.get();
	}
	public boolean isEnabled() {
		return enabled.get();
	}
	public void setEnabled(boolean value) {
		enabled.set(value);
	}
	
	// methods
	public boolean driveToLoadPosition(String sampleStage) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("SampleStage", sampleStage);
		
		return launch(DRIVE_TO_LOAD_POSITION, parameters);
	}
	public boolean driveToSamplePosition(String sampleStage, double position) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("SampleStage", sampleStage);
		parameters.put("Position", position);
		
		return launch(DRIVE_TO_SAMPLE_POSITION, parameters);
	}
	public boolean testDrive(String script) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("Script", script);
		
		return launch(TEST_DRIVE, parameters);
	}
	// listeners
	public synchronized void addListener(ICustomInstrumentActionListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);
	}
	public synchronized boolean removeListener(ICustomInstrumentActionListener listener) {
		return listeners.remove(listener);
	}
	
	// helpers
	private synchronized void raiseOnActionFinished(String action, Summary summary) {
		for (ICustomInstrumentActionListener listener : listeners)
			listener.onActionFinished(action, summary);
	}
	private boolean launch(final String action, final Map<String, Object> parameters) {
		if (enabled.get() && busy.compareAndSet(false, true))
			try {
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						Summary summary = null;
						try {
							summary = InstrumentActionExecuter.getDefault().customAction(action, parameters);
						}
						finally {
							busy.set(false);
						}
						raiseOnActionFinished(action, summary);
					}
				});
				thread.start();
				return true;
			}
			catch (Exception e) {
				busy.set(false);
			}
		
		return false;
	}
}
