package au.gov.ansto.bragg.quokka.sics;

import org.gumtree.control.core.ISicsReplyData;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsCallback;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsExecutionException;
import org.gumtree.control.model.PropertyConstants.ControllerState;
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
	
	private volatile ControllerState state;
	
	private Object statusLock = new Object();
	
	public BeamStopController(int id) {
		deviceId = DEVICE_BS + id;
		setState(ControllerState.IDLE);
		if (!SicsManager.getSicsProxy().isConnected()) {
			SicsManager.getSicsProxy().addProxyListener(new SicsProxyListenerAdapter() {
				
				@Override
				public void modelUpdated() {
//					setupStateMonListener();
				}
			});
		} else {
			setupStateMonListener();
		}
	}

	public ControllerState getState() {
		return state; 
	}

	private void setState(ControllerState state) {
		synchronized (statusLock) {
			dirtyFlag = true;
			this.state = state;
			logger.info(deviceId + " state: " + state.name());
		}
	}

	public void up() throws SicsException {
		run("up");
	}

	public void down() throws SicsException {
		run("down");
	}

	private void run(String command) throws SicsException {
		SicsManager.getSicsProxy().clearInterruptFlag();
		
		// Return straight away under simulation mode
		if (QuokkaCoreProperties.SICS_SIMULATION_MODE.getBoolean()) {
			return;
		}

		if(getState() == ControllerState.BUSY) {
			throw new SicsExecutionException(deviceId + " is already running.");
		}
		
		dirtyFlag = false;
		
		// Drive it
		System.out.println("Sending action " + deviceId + " " + command);
		SicsManager.getSicsProxy().asyncRun("action " + deviceId + " " + command, null);

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
				throw new SicsExecutionException("Interrupted Exception");
			}
		}
		
		// Wait while it is running
		while(getState() == ControllerState.BUSY) {
			try {
				Thread.sleep(TIME_INTERVAL);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new SicsExecutionException("Interrupted Exception");
			}
		}
		// Check interrupt
		if (SicsManager.getSicsProxy().isInterrupted()) {
			SicsManager.getSicsProxy().clearInterruptFlag();
			throw new SicsExecutionException("Interrupted");
		}
	}

	// NOTE: This may not be thread safe!!!
	public BeamStopPosition getPosition() throws SicsException {
		// Return straight away under simulation mode
		if (QuokkaCoreProperties.SICS_SIMULATION_MODE.getBoolean()) {
			return BeamStopPosition.unknown;
		}
		synchronized (position) {
			position = BeamStopPosition.unknown;
			SicsManager.getSicsProxy().asyncRun(deviceId + " status", new ISicsCallback() {
				
				@Override
				public void setError(boolean error) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void setCallbackCompleted(boolean completed) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void receiveWarning(ISicsReplyData data) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void receiveReply(ISicsReplyData data) {
					String[] result = data.getString().split("=");
					if (result.length == 2) {
						position = BeamStopPosition.valueOf(result[1].trim());
					}
					setCallbackCompleted(true);
				}
				
				@Override
				public void receiveRawData(Object data) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void receiveFinish(ISicsReplyData data) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void receiveError(ISicsReplyData data) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public boolean isCallbackCompleted() {
					// TODO Auto-generated method stub
					return false;
				}
				
				@Override
				public boolean hasError() {
					// TODO Auto-generated method stub
					return false;
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
		SicsManager.getSicsModel().findControllerById(deviceId).addControllerListener(new ISicsControllerListener() {
			
			@Override
			public void updateValue(Object oldValue, Object newValue) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void updateTarget(Object oldValue, Object newValue) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void updateState(ControllerState oldState, ControllerState newState) {
				setState(newState);
			}
			
			@Override
			public void updateEnabled(boolean isEnabled) {
				// TODO Auto-generated method stub
				
			}
		});
	}

}
