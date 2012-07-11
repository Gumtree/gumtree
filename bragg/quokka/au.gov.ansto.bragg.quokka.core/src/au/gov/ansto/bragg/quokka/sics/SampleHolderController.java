package au.gov.ansto.bragg.quokka.sics;

import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.controllers.ComponentData;
import org.gumtree.gumnix.sics.control.controllers.DynamicController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.Component;

public class SampleHolderController extends DynamicController {
	
	private enum Status {
		IDLE, STOPPING, BUSY
	}
	
	private static final int TIME_OUT = 3000;
	
	private static final int TIME_INTERVAL = 10;
	
	private static final String PATH_STATUS = "/status";
	
	private IDynamicController stateController;
	
	// Used by run method only
	private boolean dirtyFlag;
	
	public SampleHolderController(Component component) {
		super(component);
	}

	@Override
	public void activate() {
		super.activate();
		// Assume we have a child node for state
		stateController = (IDynamicController) getChildController(PATH_STATUS);
		// Listen to the state
		stateController.addComponentListener(new DynamicControllerListenerAdapter() {
			public void valueChanged(IDynamicController controller, IComponentData newValue) {
				String newStatus = newValue.getStringData();
				if (newStatus.equalsIgnoreCase(Status.IDLE.name())) {
					setStatus(ControllerStatus.OK);
				} else if (newStatus.equalsIgnoreCase(Status.STOPPING.name())) {
					setStatus(ControllerStatus.RUNNING);
				} else if (newStatus.equalsIgnoreCase(Status.BUSY.name())) {
					setStatus(ControllerStatus.RUNNING);
				} else {
					// Else don't update status
				}
			}
		});
	}

	public void run(int position) throws SicsIOException {
		setTargetValue(ComponentData.createData(position));
		commitTargetValue(null);
	}
	
	public void setStatus(final ControllerStatus status) {
		dirtyFlag = true;
		super.setStatus(status);
	}
	
	// TODO: This is not thread safe on variable "dirtyFlag"
	public void drive(int position) throws SicsIOException, SicsExecutionException {
		if(getStatus() == ControllerStatus.RUNNING) {
			throw new SicsExecutionException("Device is already running.");
		}
		
		SicsCore.getSicsController().clearInterrupt();
		dirtyFlag = false;
		
		// Drive it
		setTargetValue(ComponentData.createData(position));
		commitTargetValue(null);
		
		int count = 0;
		// Ensure the device does go to run
		while(!dirtyFlag) {
			try {
				Thread.sleep(TIME_INTERVAL);
				count += TIME_INTERVAL;
				if(count > TIME_OUT) {
					throw new SicsExecutionException("Time out on running samplenum.");
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

}
