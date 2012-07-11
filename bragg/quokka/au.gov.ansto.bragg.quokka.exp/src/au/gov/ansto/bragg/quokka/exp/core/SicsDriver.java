/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.quokka.exp.core;

import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDrivableController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;

public class SicsDriver {

	private static final int TIME_OUT = 5000;
	
	private static final int TIME_INTERVAL = 10;
	
	// used by save method only
	private boolean saveFlag;

//	public static getInstance(){
//		
//	}
	public static void driveMoter(String moterName, float position){
		
	}
	
	public IComponentController getDevice(String deviceId) {
		return SicsCore.getSicsController().findDeviceController(deviceId);
	}

	public boolean run(String deviceId, float position) {
		IComponentController controller = getDevice(deviceId);
		if(controller instanceof IDrivableController) {
			IDrivableController drivable = (IDrivableController)controller;
			try {
				drivable.drive(position);
				return true;
			} catch (SicsIOException e) {
				e.printStackTrace();
			} catch (SicsExecutionException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public String saveScratch() throws SicsIOException, SicsExecutionException {
		// Hardcode to BEAM_MONITOR ... to be changed later
		SicsCore.getDefaultProxy().send("newfile BEAM_MONITOR scratch", null);
		IDynamicController filename = (IDynamicController)SicsCore.getSicsController().findDeviceController("datafilename");
		saveFlag = false;
		filename.addComponentListener(new DynamicControllerListenerAdapter() {
			public void valueChanged(IDynamicController controller, IComponentData newValue) {
				System.out.println("changed");
				saveFlag = true;
			}
		});
		SicsCore.getDefaultProxy().send("save 0", null);
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
}
