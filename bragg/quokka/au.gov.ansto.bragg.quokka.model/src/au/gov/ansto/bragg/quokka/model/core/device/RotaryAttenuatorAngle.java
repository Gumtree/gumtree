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

import au.gov.ansto.bragg.quokka.model.core.QuokkaConstants;
import au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice.DeviceType;
import au.gov.ansto.bragg.quokka.model.core.exception.DriveDeviceFailedException;

/**
 * @author nxi
 * Created on 27/05/2008
 */
public class RotaryAttenuatorAngle extends VirtualDevice {

	private static RotaryAttenuatorAngle instance;
	/**
	 * 
	 */
	public RotaryAttenuatorAngle() {
		// TODO Auto-generated constructor stub
		setId("attrotdeg");
		setName("Rotary Attenuator Angle");
		setUnit("degree");
		setDescription("The rotary attenuator angle");
		for (int i = 0; i < 12; i++) {
			Double angle = 360. / 12 * i;
			lookUpTable.add(String.valueOf(angle));
		}
//		setSicsDevicePath(QuokkaConstants.AttRotDeg_PATH);
		deviceType = DeviceType.macro;
	}

	public static RotaryAttenuatorAngle getInstance(){
		if (instance == null){
			instance = new RotaryAttenuatorAngle();
		}
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#changeModel(java.lang.Object)
	 */
	@Override
	protected void changeModel(Object positionObject)
			throws DriveDeviceFailedException {
		// TODO Auto-generated method stub
		double position = 0;
		try {
			position = Double.valueOf(positionObject.toString());
		} catch (Exception e) {
			// TODO: handle exception
			throw new DriveDeviceFailedException("wrong data type");
		}
		getQuokkaModel().getRotaryAttenuator().setAngle(position);
	}

//	/* (non-Javadoc)
//	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#moveTo(java.util.List)
//	 */
//	@Override
//	public String moveTo(List<String> positionDescription) {
//		// TODO Auto-generated method stub
//		return moveNonDrivable(QuokkaConstants.AttRotDeg_PATH, positionDescription);
//	}

}
