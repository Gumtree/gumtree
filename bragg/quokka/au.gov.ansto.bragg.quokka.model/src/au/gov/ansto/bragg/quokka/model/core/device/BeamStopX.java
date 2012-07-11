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
package au.gov.ansto.bragg.quokka.model.core.device;

import au.gov.ansto.bragg.quokka.model.core.exception.DriveDeviceFailedException;

/**
 * @author nxi
 *
 */
public class BeamStopX extends VirtualDevice {

//	private String description = "The device that moves beam stop in the X direction.\n" +
//	"Default position is 0mm.";

	protected static BeamStopX instance;
	/**
	 * 
	 */
	protected BeamStopX() {
//		TODO Auto-generated constructor stub
		super();
		setId("bsx");
		setName("Beam Stop X");
		setDescription("The device that moves beam stop in the X direction.\n" +
			"Default position is 0mm.");
		deviceType = DeviceType.moveable;
	}

	public static BeamStopX getInstance(){
		if (instance == null){
			instance = new BeamStopX();
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
		double position = 0;
		try {
			position = Double.valueOf(positionObject.toString());
		} catch (Exception e) {
			// TODO: handle exception
			throw new DriveDeviceFailedException("wrong data type");
		}
		getQuokkaModel().moveBeamStopX(position);
	}	
}