package org.gumtree.gumnix.sics.control.controllers;

import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.IStateMonitorListener;
import org.gumtree.gumnix.sics.control.ServerStatus;
import org.gumtree.gumnix.sics.control.events.DynamicControllerCallbackAdapter;
import org.gumtree.gumnix.sics.core.PropertyConstants.PropertyType;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.io.ISicsCallback;
import org.gumtree.gumnix.sics.io.ISicsReplyData;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.LoopRunnerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Component;

public class DrivableController extends DynamicController implements IDrivableController, IStateMonitorListener {
	
	// 3 second state transition time out
	private static final int TIME_OUT = 3000;
	
	private static final int DRIVING_TIME_OUT = 60000;
	
	private static final int TIME_INTERVAL = 10;
	
	private static final Logger logger = LoggerFactory.getLogger(DrivableController.class);
	
	private String deviceName = "--";
	
	// Used by run method only
	private boolean dirtyFlag;
	
	private Object driveLock = new Object();
	
	public DrivableController(Component component) {
		super(component);
	}

	public void activate() {
		super.activate();
		deviceName = SicsUtils.getPropertyFirstValue(getComponent(), PropertyType.SICS_DEV);
		if(getDeviceName() != null) {
			// Need to register path and device name to state monitor because SICS can either
			// notify using device id (for scan and run command) and path (for hset)
			SicsCore.getSicsManager().monitor().addStateMonitor(getDeviceName(), this);
			SicsCore.getSicsManager().monitor().addStateMonitor(getPath(), this);
		}
//		if(getTargetController() != null) {
//			getTargetController().addComponentListener(new DynamicControllerListenerAdapter() {
//				public void valueChanged(IDynamicController controller, IComponentData newValue) {
//					setTargetValue(newValue);
//				}
//			});
//		}
	}

//	protected void setStatus(ComponentStatus status) {
//		super.setStatus(status);
//	}

	public void drive(float value) throws SicsIOException, SicsExecutionException {
		synchronized (driveLock) {
			logger.warn("Start driving " + getPath() + " in the synchronized mode (value=" + value + ", status=" + getStatus().toString() + ").");
			// [GUMTREE-558] Give more time to make the device settle
			if(getStatus() == ControllerStatus.RUNNING) {
				logger.warn("Device " + getPath() + " still busy, we will wait at most 2 seconds.");
				LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
					public boolean getExitCondition() {
						return getStatus() != ControllerStatus.RUNNING;
					}
				}, 4000, 10);
				if (status.equals(LoopRunnerStatus.TIMEOUT)) {
					forceRefresh();
				}
				status = LoopRunner.run(new ILoopExitCondition() {
					public boolean getExitCondition() {
						return getStatus() != ControllerStatus.RUNNING;
					}
				}, 4000, 10);
				if (status.equals(LoopRunnerStatus.TIMEOUT)) {
					throw new SicsExecutionException("time out in starting the run command");
				}
			}
			dirtyFlag = false;
			SicsCore.getSicsController().clearInterrupt();
			setTargetValue(ComponentData.createFloatData(value));
			final String[] errorMessages = new String[1];
			// [GUMTREE-556] Use run instead of hset
			if (deviceName != null) {
				SicsCore.getDefaultProxy().send("run " + deviceName + " " + value, new SicsCallbackAdapter() {
					public void receiveError(ISicsReplyData data) {
						errorMessages[0] = data.getString();
					}
				});
			} else {
				commitTargetValue(new DynamicControllerCallbackAdapter() {
					public void handleOperationError(IDynamicController controller,
							String errorMessage) {
						errorMessages[0] = errorMessage;
					}
				});
			}
			int count = 0;
			
			// Ensure the device does go to run
			logger.warn("Start waiting for " + getPath() + " state transition (dirtyFlag=" + dirtyFlag + ", status=" + getStatus().toString() + ").");
			while(!dirtyFlag) {
				try {
					if (errorMessages[0] != null) {
						throw new SicsExecutionException(errorMessages[0]);
					}
					Thread.sleep(TIME_INTERVAL);
					count += TIME_INTERVAL;
					if(count > getStateChangeTimeout()) {
						throw new SicsExecutionException("Time out on running device " + getDeviceName());
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new SicsExecutionException("Interrupted Exception", e);
				}
			}
			
			count = 0;
			int repeat = 0;
			// Wait while it is running
			logger.warn("Start running " + getPath() + " (status=" + getStatus().toString() + ").");
			while(getStatus() == ControllerStatus.RUNNING) {
				try {
					Thread.sleep(TIME_INTERVAL);
					if (count > DRIVING_TIME_OUT) {
						logger.warn("timeout reached, checking current value ...");
						IComponentController precision = getChildController("/precision");
						if (precision != null && precision instanceof IDynamicController) {
							try {
								double precisionValue = ((IDynamicController) precision).getValue().getFloatData();
								double currentValue = getValue(true).getFloatData();
								logger.warn("check if current value is within the tolerance: TARGET=" + value + 
										", CURRENT=" + currentValue + ", PRECISION=" + precisionValue);
								if (Math.abs(value - currentValue) < precisionValue || (precisionValue < 1e-4 && Math.abs(value - currentValue) < 1e-4)) {
//									repeat++;
//									logger.warn("target reached, repeat=" + repeat);
//									if (repeat >= 3) {
									logger.error("ERROR: DRIVING HANGS WITH TARGET REACHED, BREAKING THE WAITING LOOP.");
									SicsCore.getDefaultProxy().send("stopexe " + deviceName, null);
									Thread.sleep(1000);
									setStatus(ControllerStatus.OK);
									break;
//									}
								} else {
									SicsCore.getSicsController().refreshServerStatus();
									if (ServerStatus.EAGER_TO_EXECUTE.equals(SicsCore.getSicsController().getServerStatus())) {
										logger.error("resend the drive command");
										if (deviceName != null) {
											SicsCore.getDefaultProxy().send("run " + deviceName + " " + value, new SicsCallbackAdapter() {
												public void receiveError(ISicsReplyData data) {
													errorMessages[0] = data.getString();
												}
											});
										} else {
											commitTargetValue(new DynamicControllerCallbackAdapter() {
												public void handleOperationError(IDynamicController controller,
														String errorMessage) {
													errorMessages[0] = errorMessage;
												}
											});
										}
										break;
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						count = 0;
					}
					count += TIME_INTERVAL;
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new SicsExecutionException("Interrupted Exception", e);
				}
			}
			logger.warn("Exit from running " + getPath() + "(status=" + getStatus().toString() + ").");
			// Check if this device is interrupted
			if (SicsCore.getSicsController().isInterrupted()) {
				SicsCore.getSicsController().clearInterrupt();
				throw new SicsExecutionException("Interrupted");
			}
		}
	}

	public void forceRefresh() throws SicsIOException {
		if (deviceName != null && !deviceName.equals("--")) {
			SicsCore.getDefaultProxy().send(deviceName + " status", new ISicsCallback() {

				@Override
				public void receiveReply(ISicsReplyData data) {
					String val = data.getString();
					if (val.contains("=")) {
						String[] pair = val.split("=");
						if (pair.length == 2) {
							String status = pair[1].trim();
							if ("HWIdle".equalsIgnoreCase(status)) {
								setStatus(ControllerStatus.OK);
							} else if ("HWBusy".equalsIgnoreCase(status)) {
								setStatus(ControllerStatus.RUNNING);
							} else {
								setStatus(ControllerStatus.ERROR);
							}
						}
					}
				}

				@Override
				public void receiveError(ISicsReplyData data) {
					
				}

				@Override
				public void receiveWarning(ISicsReplyData data) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void receiveFinish(ISicsReplyData data) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void receiveRawData(Object data) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public boolean isCallbackCompleted() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void setCallbackCompleted(boolean completed) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public boolean hasError() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void setError(boolean error) {
					// TODO Auto-generated method stub
					
				}
				
			});
			
		}
		
	}
	
	protected int getStateChangeTimeout() {
		return TIME_OUT;
	}
	
	protected void setDirty(boolean flag) {
		dirtyFlag = flag;
	}
	
	public void stateChanged(SicsMonitorState state, String infoMessage) {
		logger.info("State changed (old_status=" + getStatus().toString() + ", sics_state=" + state.toString() + ").");
		if(state.isRunning()) {
			setDirty(true);
			setStatus(ControllerStatus.RUNNING);
			logger.info("Device " + getPath() + " state set to " + getStatus().toString());
		} else {
			setStatus(ControllerStatus.OK);
			logger.info("Device " + getPath() + " state set to " + getStatus().toString());
		}
	}

	public String getDeviceName() {
		return deviceName;
	}
	
}
