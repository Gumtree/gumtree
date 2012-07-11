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

import java.util.List;

import au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice.DeviceType;
import au.gov.ansto.bragg.quokka.model.core.exception.DriveDeviceFailedException;
import au.gov.ansto.bragg.quokka.model.core.instrument.QuokkaInstrument;

/**
 * @author nxi
 *
 */
public class SamplePhi extends VirtualDevice {

//	private String description = "The device that moves sample stage in the Y direction.\n" +
//	"Positive value means forwards, negative value means backwards.\nDefault position is 0m.";

	protected static SamplePhi instance;
	/**
	 * 
	 */
	protected SamplePhi() {
		super();
		setId("samphi");
		setName("Sample Phi");
		setUnit("deg");
		setDescription("The device that moves sample phi.\n" +
		"Default position is 0 degrees.");
		deviceType = DeviceType.moveable;
	}

	public static SamplePhi getInstance(){
		if (instance == null){
			instance = new SamplePhi();
		}
		return instance;
	}
//	/* (non-Javadoc)
//	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#getDescription()
//	 */
//	@Override
//	public String getDescription() {
//		return description;
//	}

//	/* (non-Javadoc)
//	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#moveTo(double)
//	 */
//	@Override
//	public String moveTo(List<String> positionDescription) {
//		return moveDrivable(positionDescription);
//	}

	@Override
	protected void changeModel(Object position) throws DriveDeviceFailedException {
	}


}
