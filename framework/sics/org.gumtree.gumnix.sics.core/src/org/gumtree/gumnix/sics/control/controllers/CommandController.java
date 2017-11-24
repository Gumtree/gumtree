package org.gumtree.gumnix.sics.control.controllers;

import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Component;

public class CommandController extends ComplexController implements ICommandController {
	
	private static final String NODE_STATUS = "/feedback/status";
	
	// State transition may take up to few seconds
	protected static final int TIME_OUT = 60000;
	
	protected static final int TIME_INTERVAL = 100;
	
	protected static final int FORCE_UPDATE_TIME_INTERVAL = 60000;
	
	private static Logger logger = LoggerFactory.getLogger(CommandController.class);
	
	private IDynamicController statusController;
	
	protected boolean statusChanged;
	
	public CommandController(Component component) {
		super(component);
		SicsUtils.getDescendantComponent(component, "NODE_STATUS");
	}

	public void preInitialise() {
	}

	public void postInitialise() {
	}
	
	public void activate() {
		// Listen to the status controller if it is available
		if (SicsUtils.getDescendantComponent(getComponent(), NODE_STATUS) != null) {
			getStatusController(); 
		}
	}

	public void asyncExecute() throws SicsIOException {
		// Reset status change flag
		// [TODO] This may not be thread safe if asyncExecute() is called
		// multiple times when scan hasn't been finished
		statusChanged = false;
		// Send this to the blockable channel
		SicsCore.getDefaultProxy().send("hset " + getPath() + " start", null, ISicsProxy.CHANNEL_SCAN);
//		SicsCore.getDefaultProxy().send("hset " + getPath() + " start", null);
	}

	public void syncExecute() throws SicsIOException, SicsExecutionException {
		// Clear interrupt flag
		SicsCore.getSicsController().clearInterrupt();
		if (getStatusController() == null) {
			asyncExecute();
		} else {
			// Wait until command is available
			while (getCommandStatus().equals(CommandStatus.BUSY)) {
				sleep("Error occured while waiting for IDLE state");
			}
			// Execute
			asyncExecute();
			int counter = 0;
			while (!statusChanged) {
				sleep("Error occured while waiting for BUSY state");
				counter += TIME_INTERVAL;
				if (counter > TIME_OUT) {
					if (CommandStatus.valueOf(getStatusController().getValue(true).getStringData()) != CommandStatus.BUSY) {
						throw new SicsExecutionException("Time out on syncExecute() where status did not changed whiling execution");
					} else {
						statusChanged = true;
					}
				}
			}
//			while (getCommandStatus().equals(CommandStatus.BUSY) || getCommandStatus().equals(CommandStatus.STARTING)) {
			int timeCount = 0;
			while (!getCommandStatus().equals(CommandStatus.IDLE)) {
				sleep("Error occured while waiting for IDLE state");
				timeCount += TIME_INTERVAL;
				if (timeCount >= FORCE_UPDATE_TIME_INTERVAL) {
					try {
						getStatusController().getValue(true);
					} catch (Exception e) {
					}
					timeCount = 0;
				}
			}
		}
		// Check if this device is interrupted
		if (SicsCore.getSicsController().isInterrupted()) {
			SicsCore.getSicsController().clearInterrupt();
			throw new SicsExecutionException("Interrupted");
		}
	}
	
	public IDynamicController getStatusController() {
		if (statusController == null) {
			if (SicsUtils.getDescendantComponent(getComponent(), NODE_STATUS) == null) {
				// Feedback status node does not exist
				return null;
			}
			statusController = (IDynamicController) getChildController(NODE_STATUS);
			statusController.addComponentListener(new DynamicControllerListenerAdapter() {
				public void valueChanged(IDynamicController controller, IComponentData newValue) {
					// Mark status changed
					statusChanged = true;
					// Update controller status
					try {
						CommandStatus commandStatus = getCommandStatus();
						if (commandStatus.equals(CommandStatus.BUSY)) {
							CommandController.this.setStatus(ControllerStatus.RUNNING);
						} else {
							CommandController.this.setStatus(ControllerStatus.OK);
						}
					} catch (SicsIOException e) {
						logger.error("Failed to obtain status for command " + getPath(), e);
					}
				}
			});
		}
		return statusController;
	}

	public CommandStatus getCommandStatus() throws SicsIOException {
		if (getStatusController() == null) {
			return CommandStatus.UNKNOWN;
		}
		return CommandStatus.valueOf(getStatusController().getValue().getStringData());
	}

	public boolean getStatusDirtyFlag() {
		return statusChanged;
	}
	
	protected void sleep(String errorMessage) throws SicsExecutionException {
		try {
			Thread.sleep(TIME_INTERVAL);
		} catch (InterruptedException e) {
			logger.error(errorMessage, e);
			Thread.currentThread().interrupt();
			throw new SicsExecutionException(e.getMessage());
		}
	}
	
}
