package org.gumtree.sics.control;

import org.gumtree.sics.io.SicsIOException;


public interface IServerController extends ISicsController {

	public static final String EVENT_TOPIC_SERVER_INTERRUPT = "org/gumtree/cs/sics/server/interrupt";
	
	public static final String EVENT_TOPIC_SERVER_STATUS_CHANGE = "org/gumtree/cs/sics/server/status";
	
	public static final String EVENT_PROP_SERVER_STATUS = "serverStatus";
	
	/**
	 * Returns the direct child component controllers under the SICS controller.
	 *
	 * @return
	 */
//	public IComponentController[] getComponentControllers();
//
//	public IComponentController findComponentController(String path);
//
//	public IComponentController findComponentController(Component component);
//
//	public IComponentController findComponentController(IComponentController controller, String relativePath);
//
//	public IComponentController findParentController(IComponentController controller);
//	
//	public ISicsObjectController[] getSicsObjectControllers();
//	
//	public ISicsObjectController getSicsObjectController(String id);
	
	/**
	 * Finds the device controller of a SICS device.
	 * 
	 * @param deviceId SICS device ID
	 * @return the corresponding controller of the device; null if device does not exist or no hipadaba component associate with this device 
	 */
//	public IComponentController findDeviceController(String deviceId);
//		
//	public IComponentData getValue(String path) throws SicsIOException;
//	
//	public void setValue(String path, IComponentData newData) throws SicsIOException;

	
	/*************************************************************************
	 * 
	 * Status, interrupt
	 * 
	 *************************************************************************/
	
	public ControllerStatus getStatus();
	
	public ServerStatus getServerStatus();
	
	public void interrupt() throws SicsIOException;
	
	public boolean isInterrupted();
	
	public void clearInterrupt();

	public void modelCreated();

	/*************************************************************************
	 * 
	 * Getters and setters
	 * 
	 *************************************************************************/
	
//	public SICS getSICSModel() throws SicsIOException;
	
//	public void addStateMonitor(String sicsObject, IStateMonitorListener listener);

//	public void removeStateMonitor(String sicsObject, IStateMonitorListener listener);

}
