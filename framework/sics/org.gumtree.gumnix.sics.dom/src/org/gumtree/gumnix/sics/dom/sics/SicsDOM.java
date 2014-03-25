package org.gumtree.gumnix.sics.dom.sics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.gumnix.sics.control.ISicsControllerListener;
import org.gumtree.gumnix.sics.control.controllers.ComponentData;
import org.gumtree.gumnix.sics.control.controllers.ComponentDataFormatException;
import org.gumtree.gumnix.sics.control.controllers.ICommandController;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDrivableController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.internal.io.SicsProxy;
import org.gumtree.gumnix.sics.io.ISicsCallback;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.gumnix.sics.io.ISicsReplyData;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.DataType;
import org.gumtree.gumnix.sics.dom.sics.SicsConstant;

public class SicsDOM {

	private static final int TIME_OUT = 5000;
	
	private static final int TIME_INTERVAL = 10;
	
	// used by save method only
	private boolean saveFlag;
	
	boolean dirtyFlag = false;
	
	public SicsDOM() {
		super();
		SicsCore.getSicsController();
	}
	
	public IComponentController getDevice(String deviceId) {
		return SicsCore.getSicsController().findDeviceController(deviceId);
	}

	public IComponentController getController(String componentPath){
		return SicsCore.getSicsController().findComponentController(componentPath);
	}
	
	public float getPosition(String deviceId) throws SicsDOMException{
		IComponentController controller = getDevice(deviceId);
		
		if(controller instanceof IDynamicController) {
			IDynamicController drivable = (IDynamicController)controller;
			try {
				IComponentData data = drivable.getValue();
				return data.getFloatData();
			} catch (ComponentDataFormatException e) {
				// TODO: handle exception
				throw new SicsDOMException(e);
			} catch (SicsIOException e) {
				// TODO Auto-generated catch block
				throw new SicsDOMException(e);
			}
		}else throw new SicsDOMException("device does not have a numeric position");
	}
		
	public String getValue(String idOrPath) throws SicsDOMException{
		IComponentController controller = null;
		try{
			controller = getDevice(idOrPath);
		}catch (Exception e) {
			// TODO: handle exception
			controller = getController(idOrPath);
		}
		if (controller == null)
			controller = getController(idOrPath);
		if (controller == null)
			throw new SicsDOMException("device does not exist: " + idOrPath);
		if (controller instanceof IDynamicController) {
			IDynamicController dynamicController = (IDynamicController) controller;
			try {
				IComponentData data = dynamicController.getValue();
				return data.getSicsString();
			} catch (Exception e) {
				// TODO: handle exception
				throw new SicsDOMException(e);
			}
		}
		throw new SicsDOMException("device does not have an status available");
	}
	
	public void setValue(String componentPath, String SicsString) throws SicsDOMException{
		IComponentController controller = getController(componentPath);
		
		if (controller instanceof IDynamicController) {
			IDynamicController dynamicController = (IDynamicController) controller;
			try {
				DataType dataType = dynamicController.getDataType();
				IComponentData data = new ComponentData(SicsString, dataType);
				dynamicController.setTargetValue(data);
				dynamicController.commitTargetValue(null);
				return;
			} catch (Exception e) {
				// TODO: handle exception
				throw new SicsDOMException(e);
			}
		}
		throw new SicsDOMException("device does not have an status available");
	}
	
	public void setParameterValue(String parameterId, String SicsString) throws SicsDOMException{
		IComponentController controller = getDevice(parameterId);
		if (controller instanceof IDynamicController) {
			IDynamicController dynamicController = (IDynamicController) controller;
			try {
				DataType dataType = dynamicController.getDataType();
				IComponentData data = new ComponentData(SicsString, dataType);
				dynamicController.setTargetValue(data);
				dynamicController.commitTargetValue(null);
				return;
			} catch (Exception e) {
				// TODO: handle exception
				throw new SicsDOMException(e);
			}
		}
		throw new SicsDOMException("device does not have an status available");
	}
	
	private List<IComponentController> getSubDynamicControllerList(IComponentController controller){
		
		List<IComponentController> controllerList = new ArrayList<IComponentController>();
		if (controller != null){
			IComponentController[] subcontrollers = controller.getChildControllers();
			for (int i = 0; i < subcontrollers.length; i++) {
				if (subcontrollers[i] instanceof IDynamicController)
					controllerList.add(subcontrollers[i]);
				else
					controllerList.addAll(getSubDynamicControllerList(subcontrollers[i]));
			}
		}
		return controllerList;
	}
	
	public List<IComponentController> getSubDynamicControllerList(String componentPath){
		IComponentController controller = getController(componentPath);
		return getSubDynamicControllerList(controller);
	}
	
	public void runCommand(String componentPath) throws SicsDOMException{
		IComponentController controller = getController(componentPath);
		if (controller instanceof ICommandController) {
			ICommandController commandController = (ICommandController) controller;
			try {
				commandController.asyncExecute();
				return;
			} catch (Exception e) {
				// TODO: handle exception
				throw new SicsDOMException(e);
			}
		}
		throw new SicsDOMException("device is not a command component");
	}
	
	public void runRawCommand(String rawCommand) throws SicsDOMException{
		ISicsProxy sicsProxy = SicsCore.getDefaultProxy();
		ISicsCallback callback =  new SicsCallbackAdapter() {
			public void receiveReply(ISicsReplyData response) {
//				System.out.println(response.getObject().toString());
				setCallbackCompleted(true);
			};
		};
		try {
			sicsProxy.send(rawCommand, callback, SicsProxy.CHANNEL_GENERAL);
		} catch (SicsIOException e) {
			// TODO Auto-generated catch block
			throw new SicsDOMException(e);
		}
	}
	
	public boolean run(String deviceId, float position) throws SicsDOMException{
		IComponentController controller = getDevice(deviceId);
		if(controller instanceof IDrivableController) {
			IDrivableController drivable = (IDrivableController)controller;
			try {
				drivable.drive(position);
				return true;
			} catch (SicsIOException e) {
				throw new SicsDOMException(e);
//				e.printStackTrace();
			} catch (SicsExecutionException e) {
//				e.printStackTrace();
				throw new SicsDOMException(e);
			}
		}
		return false;
	}
	
	public synchronized String saveScratch() throws SicsIOException, SicsExecutionException {
		// Hardcode to BEAM_MONITOR ... to be changed later
		SicsCore.getDefaultProxy().send("newfile BEAM_MONITOR scratch", null);
		
		// Set up listener
		IDynamicController filename = (IDynamicController)SicsCore.getSicsController().findDeviceController("datafilename");
		saveFlag = false;
		filename.addComponentListener(new DynamicControllerListenerAdapter() {
			public void valueChanged(IDynamicController controller, IComponentData newValue) {
				System.out.println("changed");
				saveFlag = true;
			}
		});
		
		// Run command
		SicsCore.getDefaultProxy().send("save 0", null);
		
		// Wait
		int count = 0;
		while(!saveFlag) {
			try {
				Thread.sleep(TIME_INTERVAL);
				count += TIME_INTERVAL;
				if(count > TIME_OUT) {
					throw new SicsExecutionException("Time out on saving file");
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new SicsExecutionException("Interrupted Exception", e);
			}
		}
		return filename.getValue().getStringData();
	}

	public void addStatusListener(String componentPath,
			IDynamicControllerListener listener) throws SicsDOMException {
		// TODO Auto-generated method stub
		IComponentController controller = getController(componentPath);
		
		if (controller instanceof IDynamicController) {
			IDynamicController dynamicController = (IDynamicController) controller;
			try {
				dynamicController.addComponentListener(listener);
				return;
			} catch (Exception e) {
				// TODO: handle exception
				throw new SicsDOMException(e);
			}
		}
		throw new SicsDOMException("device does not have an status available");
	}

	public IDynamicController getDynamicController(String componentPath) throws SicsDOMException {
		// TODO Auto-generated method stub
		IComponentController controller = getController(componentPath);
		
		if (controller instanceof IDynamicController) {
			return (IDynamicController) controller;
		}
		throw new SicsDOMException("device does not have an status available");
	}
	
	public void runCommandWithFeedBack(String commandPath) throws SicsDOMException{
		String statusPath = commandPath + "/feedback/status";
//		boolean dirtyFlag = false;
		IDynamicController status = null;
		try {
			status = getDynamicController(statusPath);
		} catch (SicsDOMException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		IDynamicControllerListener statusListener = new DynamicControllerListenerAdapter() {
			public void valueChanged(IDynamicController controller, IComponentData newValue) {
				if(newValue.getStringData().equals("BUSY")) {
					dirtyFlag = true;
				}
			}
		};
		status.addComponentListener(statusListener);
		
		dirtyFlag = false;
		int count = 0;
		runCommand(commandPath);
		while(!dirtyFlag) {
			try {
				Thread.sleep(TIME_INTERVAL);
				count += TIME_INTERVAL;
				if(count > TIME_OUT) {
					status.removeComponentListener(statusListener);
					throw new SicsDOMException("Time out on starting command: " + commandPath);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				status.removeComponentListener(statusListener);
//				return e.getMessage();
				throw new SicsDOMException(e);
			}
		}
		while(true) {
			try {
				if(status.getValue().getStringData().equals("IDLE")) {
					status.removeComponentListener(statusListener);
					return;
				}
				Thread.sleep(TIME_INTERVAL);
			} catch (Exception e) {
				Thread.currentThread().interrupt();
				status.removeComponentListener(statusListener);
				throw new SicsDOMException(e);
			}
		}
	}
	
	public String HMScan(final String scanVar, final double startPosition, final double increment, 
			final double numberOfStep, final String mode, final String preset, 
			final SicsStatusListener listener) throws SicsDOMException{
		IDynamicController status = null;
		try {
			status = getDynamicController(SicsConstant.HMSCAN_STATUS_PATH);
		} catch (SicsDOMException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		IDynamicControllerListener statusListener = new DynamicControllerListenerAdapter() {
			public void valueChanged(IDynamicController controller, IComponentData newValue) {
				if(newValue.getStringData().equals("BUSY")) {
					dirtyFlag = true;
				}
			}
		};
		status.addComponentListener(statusListener);
		
		IDynamicController variableValue = null;
		try {
			variableValue = getDynamicController(SicsConstant.HMSCAN_VARIABLE_VALUE_PATH);
		} catch (SicsDOMException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		IDynamicControllerListener variableValueListener = new DynamicControllerListenerAdapter() {
			public void valueChanged(IDynamicController controller, IComponentData newValue) {
//				experiment.printlnToShell("move " + scanVar + " to " + newValue.getStringData() + "\n");
//				experiment.printlnToShell("Do a hmm scan with " + mode + "=" + preset + "\n");
				listener.getMessage("move " + scanVar + " to " + newValue.getStringData() + "\n"
						+ "Do a hmm scan with " + mode + "=" + preset + "\n");
			}
		};
		variableValue.addComponentListener(variableValueListener);
		String resultFilename = null;
		IDynamicController filename = null;
		try {
			filename = getDynamicController(SicsConstant.SCAN_FILENAME_PATH);
		} catch (SicsDOMException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		setValue(SicsConstant.HMSCAN_SCANVARIABLE_PATH, scanVar);
		setValue(SicsConstant.HMSCAN_SCANSTART_PATH, String.valueOf(startPosition));
		setValue(SicsConstant.HMSCAN_SCANINCREMENT_PATH, String.valueOf(increment));
		setValue(SicsConstant.HMSCAN_NUMBEROFPOINTS_PATH, String.valueOf(numberOfStep));
		setValue(SicsConstant.HMSCAN_MODE_PATH, mode);
		setValue(SicsConstant.HMSCAN_PRESET_PATH, String.valueOf(preset));
		setValue(SicsConstant.HMSCAN_CHANNEL_PATH, "0");
		try {
			Thread.sleep(100);
		} catch (Exception e) {
			// TODO: handle exception
		}
		dirtyFlag = false;
		int count = 0;
		runCommand(SicsConstant.HMSCAN_PATH);
		while(!dirtyFlag) {
			try {
				Thread.sleep(TIME_INTERVAL);
				count += TIME_INTERVAL;
				if(count > TIME_OUT) {
					status.removeComponentListener(statusListener);
					throw new SicsDOMException("Time out on starting monitor count.\n");
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				status.removeComponentListener(statusListener);
				variableValue.removeComponentListener(variableValueListener);
				throw new SicsDOMException(e);
			}
		}
		while(true) {
			try {
				if(status.getValue().getStringData().equals("IDLE")) {
					status.removeComponentListener(statusListener);
					variableValue.removeComponentListener(variableValueListener);
					resultFilename = filename.getValue().getStringData();
					listener.getMessage("Save scan result in file: " + resultFilename + "\n");
					if (resultFilename.equals("UNKNOWN")) 
						resultFilename = "D:/dra/echidnadata/ECH0000700.nx.hdf";
					else {
						File newFile = new File(resultFilename);
						resultFilename = "W:/commissioning/" + newFile.getName();
					}
//					experiment.setScanResultFilename(resultFilename);
//					experiment.setResultEntryList(experiment.getKakadu().addDataSourceFile(resultFilename));
					return resultFilename;
				}
				Thread.sleep(TIME_INTERVAL);
			} catch (Exception e) {
				Thread.currentThread().interrupt();
				status.removeComponentListener(statusListener);
				variableValue.removeComponentListener(variableValueListener);
				throw new SicsDOMException(e);
			}
		}

	}

	public String Runscan(final String scanVar, final double startPosition, final double stopPosition, 
			final double numberOfStep, final String mode, final String preset, 
			String datatype, String savetype, boolean force, final SicsStatusListener listener) 
	throws SicsDOMException{
		IDynamicController status = null;
		try {
			status = getDynamicController(SicsConstant.RUNSCAN_STATUS_PATH);
		} catch (SicsDOMException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		IDynamicControllerListener statusListener = new DynamicControllerListenerAdapter() {
			public void valueChanged(IDynamicController controller, IComponentData newValue) {
				if(newValue.getStringData().equals("BUSY")) {
					dirtyFlag = true;
				}
			}
		};
		status.addComponentListener(statusListener);
		
		IDynamicController variableValue = null;
		try {
			variableValue = getDynamicController(SicsConstant.RUNSCAN_VARIABLE_VALUE_PATH);
		} catch (SicsDOMException e2) {
			e2.printStackTrace();
		}
		IDynamicControllerListener variableValueListener = new DynamicControllerListenerAdapter() {
			public void valueChanged(IDynamicController controller, IComponentData newValue) {
//				experiment.printlnToShell("move " + scanVar + " to " + newValue.getStringData() + "\n");
//				experiment.printlnToShell("Do a hmm scan with " + mode + "=" + preset + "\n");
				listener.getMessage("move " + scanVar + " to " + newValue.getStringData() + "\n"
						+ "do a hmm scan with " + mode + " = " + preset + "\n");
			}
		};
		variableValue.addComponentListener(variableValueListener);
		String resultFilename = null;
		IDynamicController filename = null;
		try {
			filename = getDynamicController(SicsConstant.SCAN_FILENAME_PATH);
		} catch (SicsDOMException e2) {
			e2.printStackTrace();
		}
		
		setValue(SicsConstant.RUNSCAN_SCANVARIABLE_PATH, scanVar);
		setValue(SicsConstant.RUNSCAN_SCANSTART_PATH, String.valueOf(startPosition));
		setValue(SicsConstant.RUNSCAN_SCANSTOP_PATH, String.valueOf(stopPosition));
		setValue(SicsConstant.RUNSCAN_NUMBEROFPOINTS_PATH, String.valueOf(numberOfStep));
		setValue(SicsConstant.RUNSCAN_MODE_PATH, mode);
		setValue(SicsConstant.RUNSCAN_PRESET_PATH, String.valueOf(preset));
		setValue(SicsConstant.RUNSCAN_DATATYPE_PATH, datatype);
		setValue(SicsConstant.RUNSCAN_SAVETYPE_PATH, savetype);
		setValue(SicsConstant.RUNSCAN_FORCE_PATH, String.valueOf(force));
		try {
			Thread.sleep(100);
		} catch (Exception e) {
			// TODO: handle exception
		}
		dirtyFlag = false;
		int count = 0;
		runCommand(SicsConstant.RUNSCAN_PATH);
		try{
			while(!dirtyFlag) {
				try {
					Thread.sleep(TIME_INTERVAL);
					count += TIME_INTERVAL;
					if(count > TIME_OUT) {
						status.removeComponentListener(statusListener);
						throw new SicsDOMException("Time out on starting monitor count.\n");
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new SicsDOMException(e);
				} 
			}
			while(true) {
				try {
					if(status.getValue().getStringData().equals("IDLE")) {
//						status.removeComponentListener(statusListener);
//						variableValue.removeComponentListener(variableValueListener);
//						resultFilename = filename.getValue().getStringData();
						resultFilename = (new File(filename.getValue().getStringData())).getName();
						listener.getMessage("save scan result in file: " + resultFilename + "\n");
						if (resultFilename.equals("UNKNOWN")) 
//							resultFilename = "D:/dra/echidnadata/ECH0000700.nx.hdf";
							resultFilename = "D:/dra/quokkadata/QKK0001320.nx.hdf";
						else {
							File newFile = new File(resultFilename);
							resultFilename = "W:/commissioning/" + newFile.getName();
						}
						//					experiment.setScanResultFilename(resultFilename);
						//					experiment.setResultEntryList(experiment.getKakadu().addDataSourceFile(resultFilename));
						return resultFilename;
					}
					Thread.sleep(TIME_INTERVAL);
				} catch (Exception e) {
					Thread.currentThread().interrupt();
//					status.removeComponentListener(statusListener);
//					variableValue.removeComponentListener(variableValueListener);
					throw new SicsDOMException(e);
				}
			}
		} finally{
			status.removeComponentListener(statusListener);
			variableValue.removeComponentListener(variableValueListener);
		}
	}

	public void addSicsControlListener(SicsControlListener listener){
		SicsCore.getSicsController().addControllerListener(listener);
	}
	
	public void removeSicsControlListener(SicsControlListener listener){
		SicsCore.getSicsController().removeControllerListener(listener);
	}
}
