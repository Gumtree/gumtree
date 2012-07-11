/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package au.gov.ansto.bragg.quokka.model.core.device;

import au.gov.ansto.bragg.quokka.model.core.exception.DriveDeviceFailedException;

/**
 * @author nxi
 *
 */
public class SampleTheta extends VirtualDevice {

//	private String description = "The device that moves beam stop in the X direction.\n" +
//	"Default position is 0mm.";

	protected static SampleTheta instance;
	/**
	 * 
	 */
	protected SampleTheta() {
//		TODO Auto-generated constructor stub
		super();
		setId("samthet");
		setName("Sample Theta");
		setDescription("The device that rotate sample theta.\n" +
			"Default position is 0 degree.");
		deviceType = DeviceType.moveable;
	}

	public static SampleTheta getInstance(){
		if (instance == null){
			instance = new SampleTheta();
		}
		return instance;
	}
//	/* (non-Javadoc)
//	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#getDescription()
//	 */
//	@Override
//	public String getDescription() {
////		TODO Auto-generated method stub
//		return description;
//	}
//
//	/* (non-Javadoc)
//	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#moveTo(double)
//	 */
//	@Override
//	public String moveTo(List<String> positionDescription) {
////		TODO Auto-generated method stub
//		return moveDrivable(positionDescription);
//	}

	protected void changeModel(Object positionObject) throws DriveDeviceFailedException {
		// TODO Auto-generated method stub
	}	
}