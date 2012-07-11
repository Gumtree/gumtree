package au.gov.ansto.bragg.quokka.sics;

import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.IStateMonitorListener;
import org.gumtree.gumnix.sics.control.controllers.CommandController;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.Component;

public class BeamStopCommandController extends CommandController {

	public BeamStopCommandController(Component component) {
		super(component);
		// Register statemon listener to change status on "selbs"
		// This is a temporary solution because the feedback node is working
		SicsCore.getSicsManager().monitor().addStateMonitor("selbs", new IStateMonitorListener() {
			public void stateChanged(SicsMonitorState state, String infoMessage) {
				statusChanged = true;
				if (state.isRunning()) {
					setStatus(ControllerStatus.RUNNING);
				} else {
					setStatus(ControllerStatus.OK);
				}
			}
		});
	}
	
	public void syncExecute() throws SicsIOException, SicsExecutionException {
		SicsCore.getSicsController().clearInterrupt();
//		if (getStatusController() == null) {
//			asyncExecute();
//		} else {
			// Wait until command is available
//			while (getCommandStatus().equals(CommandStatus.BUSY)) {
			while (getStatus().equals(ControllerStatus.RUNNING)) {
				sleep("Error occured while waiting for IDLE state");
			}
			// Execute
			asyncExecute();
			int counter = 0;
			while (!getStatusDirtyFlag()) {
				sleep("Error occured while waiting for BUSY state");
				counter += TIME_INTERVAL;
				if (counter > TIME_OUT) {
					throw new SicsExecutionException("Time out on syncExecute() where status did not changed whiling execution");
				}
			}
//			while (getCommandStatus().equals(CommandStatus.BUSY) || getCommandStatus().equals(CommandStatus.STARTING)) {
			while (getStatus().equals(ControllerStatus.RUNNING)) {
				if (SicsCore.getSicsController().isInterrupted()) {
					break;
				}
				sleep("Error occured while waiting for IDLE state");
			}
//		}
		// Check if this device is interrupted
		if (SicsCore.getSicsController().isInterrupted()) {
			SicsCore.getSicsController().clearInterrupt();
			throw new SicsExecutionException("Interrupted");
		}
	}
}
