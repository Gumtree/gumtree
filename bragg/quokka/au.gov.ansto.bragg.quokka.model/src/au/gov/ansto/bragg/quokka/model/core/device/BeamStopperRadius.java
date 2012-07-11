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
import au.gov.ansto.bragg.quokka.model.core.exception.IllegalValueException;

/**
 * @author nxi
 *
 */
public class BeamStopperRadius extends VirtualDevice {

	private String description = "The device that changes the beam stopper radius.\n" +
	"Default value is 50mm.";

	protected static BeamStopperRadius instance;
	/**
	 * 
	 */
	protected BeamStopperRadius() {
//		TODO Auto-generated constructor stub
		super();
		setId("bs5");
		setName("Beam Stopper Radius");
		setDescription("The device that changes the beam stopper radius.\n" +
			"Default value is 50mm.");
		try {
			setPosition("50");
		} catch (IllegalValueException e) {
			// TODO Auto-generated catch block
		}
		deviceType = DeviceType.setable;
	}

	public static BeamStopperRadius getInstance(){
		if (instance == null){
			instance = new BeamStopperRadius();
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
		getQuokkaModel().setBeamStopperRadius(position);
	}	

}
