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
public class SampleY extends VirtualDevice {

//	private String description = "The device that moves sample stage in the Y direction.\n" +
//	"Positive value means forwards, negative value means backwards.\nDefault position is 0m.";

	protected static SampleY instance;
	/**
	 * 
	 */
	protected SampleY() {
		super();
		setId("samy");
		setName("Sample Y");
		setUnit("m");
		setDescription("The device that moves sample stage in the Y direction.\n" +
		"Positive value means forwards, negative value means backwards.\nDefault position is 0m.");
		deviceType = DeviceType.moveable;
	}

	public static SampleY getInstance(){
		if (instance == null){
			instance = new SampleY();
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
		double positionValue = 0;
		try {
			positionValue = Double.valueOf(position.toString());
		} catch (Exception e) {
			throw new DriveDeviceFailedException("wrong data type");
		}
		getQuokkaModel().shrinkL1(positionValue);
		getQuokkaModel().shrinkL2(- positionValue);
	}


}
