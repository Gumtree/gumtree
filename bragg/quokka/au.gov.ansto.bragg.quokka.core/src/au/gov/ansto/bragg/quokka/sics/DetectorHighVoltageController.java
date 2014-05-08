package au.gov.ansto.bragg.quokka.sics;

import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.IStateMonitorListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.gumnix.sics.io.ISicsReplyData;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;

import au.gov.ansto.bragg.quokka.core.internal.QuokkaCoreProperties;

public class DetectorHighVoltageController {

	private static final String DEVICE_DHV1 = "dhv1";
	
	private volatile ControllerStatus status;
	
	private volatile Float value;
	
	public DetectorHighVoltageController() {
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
		synchronized (status) {
			this.status = status;
		}
	}
	
	public void reset() throws SicsIOException {
		SicsCore.getDefaultProxy().send(DEVICE_DHV1 + " reset", null);
	}
	
	public float getValue() throws SicsIOException {
		if (QuokkaCoreProperties.SICS_SIMULATION_MODE.getBoolean()) {
			return 0.0f;
		}
		value = null;
		SicsCore.getDefaultProxy().send(DEVICE_DHV1, new SicsCallbackAdapter() {
			public void receiveReply(ISicsReplyData data) {
				String[] result = data.getString().split("=");
				if (result.length == 2) {
					value = Float.parseFloat(result[1].trim());
				}
				setCallbackCompleted(true);
			}
		});
		// 5 sec time out
		LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return value != null;
			}
		}, 5000);
		return value.floatValue();
	}
	
	public void up() throws SicsIOException, SicsExecutionException {
		run("up");
	}
	
	public void down() throws SicsIOException, SicsExecutionException {
		run("down");
	}
	
	private void run(String command) throws SicsIOException, SicsExecutionException {
//		System.out.println("Start to run dhv1 " + command);
		
		SicsCore.getSicsController().clearInterrupt();
		
		// Return straight away under simulation mode
		if (QuokkaCoreProperties.SICS_SIMULATION_MODE.getBoolean()) {
			return;
		}
		
		// Wait for 1 sec to stablise
		LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				System.out.println("Loop 1: " + getStatus().name());
				return getStatus().equals(ControllerStatus.OK);
			}
		}, 1000);
//		System.out.println("Sending dhv1 " + command);
		// This command is blockable
		SicsCore.getDefaultProxy().send(DEVICE_DHV1 + " " + command, null, ISicsProxy.CHANNEL_SCAN);
//		SicsCore.getDefaultProxy().send(DEVICE_DHV1 + " " + command, null);
		// Wait for 1 sec to run
		LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return getStatus().equals(ControllerStatus.RUNNING);
			}
		}, 1000);
//		System.out.println("dhv1 is running");
		// Can't get into the next waiting loop too quick
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		// Wait until it has finished
		LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return getStatus().equals(ControllerStatus.OK);
			}
		}, LoopRunner.NO_TIME_OUT);
		if (SicsCore.getSicsController().isInterrupted()) {
			SicsCore.getSicsController().clearInterrupt();
			throw new SicsExecutionException("Interrupted");
		}
		System.out.println("dhv1 is done");
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
		SicsCore.getSicsController().addStateMonitor(DEVICE_DHV1, stateMonListener);
	}
	
}
