package au.gov.ansto.bragg.quokka.depr;

import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.IStateMonitorListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsReplyData;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.quokka.core.internal.QuokkaCoreProperties;

public class BeamStopController {

	private static final String DEVICE_BS = "bs";
	
	private static final int TIME_OUT = 3000;
	
	private static final int TIME_INTERVAL = 10;
	
	private static final Logger logger = LoggerFactory.getLogger(BeamStopController.class);
	
	private String deviceId;
	
	// Used by run method only
	private boolean dirtyFlag;
	
	// Used by getPosition method only
	private volatile BeamStopPosition position = BeamStopPosition.unknown;
	
	private volatile ControllerStatus status;
	
	private Object statusLock = new Object();
	
	public BeamStopController(int id) {
		deviceId = DEVICE_BS + id;
		setStatus(ControllerStatus.OK);
		if (!SicsCore.getDefaultProxy().isConnected()) {
			SicsCore.getDefaultProxy().addProxyListener(new SicsProxyListenerAdapter() {
				public void proxyConnected() {
					setupStateMonListener();
				}
			});
		} else {
			setupStateMonListener();
		}
	}

	public ControllerStatus getStatus() {
		return status; 
	}

	private void setStatus(ControllerStatus status) {
		synchronized (statusLock) {
			dirtyFlag = true;
			this.status = status;
			logger.info(deviceId + " status: " + status.name());
		}
	}

	public void up() throws SicsIOException, SicsExecutionException {
		run("up");
	}

	public void down() throws SicsIOException, SicsExecutionException {
		run("down");
	}

	private void run(String command) throws SicsIOException, SicsExecutionException {
		SicsCore.getSicsController().clearInterrupt();
		
		// Return straight away under simulation mode
		if (QuokkaCoreProperties.SICS_SIMULATION_MODE.getBoolean()) {
			return;
		}

		if(getStatus() == ControllerStatus.RUNNING) {
			throw new SicsExecutionException(deviceId + " is already running.");
		}
		
		dirtyFlag = false;
		
		// Drive it
		System.out.println("Sending action " + deviceId + " " + command);
		SicsCore.getDefaultProxy().send("action " + deviceId + " " + command, null);

		int count = 0;
		// Ensure the device does go to run
		while(!dirtyFlag) {
			try {
				Thread.sleep(TIME_INTERVAL);
				count += TIME_INTERVAL;
				if(count > TIME_OUT) {
					throw new SicsExecutionException("Time out on running " + deviceId);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new SicsExecutionException("Interrupted Exception", e);
			}
		}
		
		// Wait while it is running
		while(getStatus() == ControllerStatus.RUNNING) {
			try {
				Thread.sleep(TIME_INTERVAL);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new SicsExecutionException("Interrupted Exception", e);
			}
		}
		// Check interrupt
		if (SicsCore.getSicsController().isInterrupted()) {
			SicsCore.getSicsController().clearInterrupt();
			throw new SicsExecutionException("Interrupted");
		}
	}

	// NOTE: This may not be thread safe!!!
	public BeamStopPosition getPosition() throws SicsIOException {
		// Return straight away under simulation mode
		if (QuokkaCoreProperties.SICS_SIMULATION_MODE.getBoolean()) {
			return BeamStopPosition.unknown;
		}
		synchronized (position) {
			position = BeamStopPosition.unknown;
			SicsCore.getDefaultProxy().send(deviceId + " status", new SicsCallbackAdapter() {
				public void receiveReply(ISicsReplyData data) {
					String[] result = data.getString().split("=");
					if (result.length == 2) {
						position = BeamStopPosition.valueOf(result[1].trim());
					}
					setCallbackCompleted(true);
				}
			});
			// 5 sec time out
			LoopRunner.run(new ILoopExitCondition() {
				public boolean getExitCondition() {
					return !position.equals(BeamStopPosition.unknown);
				}
			}, 5000);
		}
		return position;
	}
	
	private void setupStateMonListener() {
		IStateMonitorListener stateMonListener = new IStateMonitorListener() {
			public void stateChanged(SicsMonitorState state, String infoMessage) {
				if (state.isRunning()) {
					setStatus(ControllerStatus.RUNNING);
				} else {
					setStatus(ControllerStatus.OK);
				}
			}
		};
		SicsCore.getSicsController().addStateMonitor(deviceId, stateMonListener);
	}

}
