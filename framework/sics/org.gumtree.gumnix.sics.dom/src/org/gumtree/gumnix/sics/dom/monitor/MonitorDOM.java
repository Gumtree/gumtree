package org.gumtree.gumnix.sics.dom.monitor;

import org.gumtree.gumnix.sics.control.controllers.ComponentData;
import org.gumtree.gumnix.sics.control.controllers.ICommandController;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;

public class MonitorDOM {

	private static final int TIME_OUT = 1000;
	
	private static final int TIME_INTERVAL = 10;
	
	// used by count methods only
	private boolean dirtyFlag;
	
	public MonitorDOM() {
		super();
	}
	
	// timer
	public synchronized boolean timerCount(int second) throws SicsExecutionException {
		IComponentController controller = SicsCore.getSicsController().findComponentController("/commands/monitor/count");
		if(controller != null && controller instanceof ICommandController) {
			try {
				IDynamicController mode = (IDynamicController)controller.getChildController("/mode");
				mode.setTargetValue(ComponentData.createStringData("timer"));
				mode.commitTargetValue(null);
				IDynamicController present = (IDynamicController)controller.getChildController("/preset");
				present.setTargetValue(ComponentData.createIntData(second));
				present.commitTargetValue(null);
				
				
				IDynamicController status = (IDynamicController)controller.getChildController("/feedback/status");
				IDynamicControllerListener statusListener = new DynamicControllerListenerAdapter() {
					public void valueChanged(IDynamicController controller, IComponentData newValue) {
						if(newValue.getStringData().equals("BUSY")) {
							dirtyFlag = true;
						}
					}
				};
				status.addComponentListener(statusListener);
					
				dirtyFlag = false;
				((ICommandController)controller).asyncExecute();
				int count = 0;
				
				// Ensure counter is started
				while(!dirtyFlag) {
					try {
						Thread.sleep(TIME_INTERVAL);
						count += TIME_INTERVAL;
						if(count > TIME_OUT) {
							status.removeComponentListener(statusListener);
							throw new SicsExecutionException("Time out on starting monitor count");
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						status.removeComponentListener(statusListener);
						throw new SicsExecutionException("Interrupted Exception", e);
					}
				}
				
				// Wait while it is running
				while(true) {
					try {
						if(status.getValue().getStringData().equals("IDLE")) {
							return true;
						}
						Thread.sleep(TIME_INTERVAL);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						status.removeComponentListener(statusListener);
						throw new SicsExecutionException("Interrupted Exception", e);
					}
				}
				
			} catch (SicsIOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	// monitor
	public synchronized boolean monitorCount(int count) {
		return false;
	}
}
