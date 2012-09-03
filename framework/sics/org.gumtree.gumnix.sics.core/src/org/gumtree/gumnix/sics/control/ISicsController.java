package org.gumtree.gumnix.sics.control;


import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.ISicsObjectController;
import org.gumtree.gumnix.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.SICS;


public interface ISicsController {

//	public static final String EVENT_TOPIC_SERVER_STATUS = "org/gumtree/gumnix/sics/controller/serverstatus";
//	
//	public static final String EVENT_PROP_VALUE = "value";
	
	/**
	 * Returns the direct child component controllers under the SICS controller.
	 *
	 * @return
	 */
	public IComponentController[] getComponentControllers();

	public IComponentController findComponentController(String path);

	public IComponentController findComponentController(Component component);

	public IComponentController findComponentController(IComponentController controller, String relativePath);

	public IComponentController findParentController(IComponentController controller);
	
	public ISicsObjectController[] getSicsObjectControllers();
	
	public ISicsObjectController getSicsObjectController(String id);
	
	public ControllerStatus getStatus();
	
	public ServerStatus getServerStatus();
	
	/**
	 * Finds the device controller of a SICS device.
	 * 
	 * @param deviceId SICS device ID
	 * @return the corresponding controller of the device; null if device does not exist or no hipadaba component associate with this device 
	 */
	public IComponentController findDeviceController(String deviceId);
	
	public SICS getSICSModel();

	public void addControllerListener(ISicsControllerListener listener);
	
	public void removeControllerListener(ISicsControllerListener listener);
	
	public void addStateMonitor(String sicsObject, IStateMonitorListener listener);

	public void removeStateMonitor(String sicsObject, IStateMonitorListener listener);
	
	public IComponentData getValue(String path) throws SicsIOException;
	
	public void setValue(String path, IComponentData newData) throws SicsIOException;

	public void interrupt() throws SicsIOException;
	
	public boolean isInterrupted();
	
	public void clearInterrupt();

}
