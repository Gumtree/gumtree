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
public class RotaryAttenuator extends VirtualDevice {

	private static RotaryAttenuator instance;
	/**
	 * 
	 */
	public RotaryAttenuator() {
		// TODO Auto-generated constructor stub
		setId("AttRot");
		setName("Rotary Attenuator");
		setUnit("");
		setDescription("The rotary attenuator");
		setSicsDevicePath(QuokkaConstants.AttRot_PATH);
		addSubdevice(RotaryApertureAngle.getInstance());
		deviceType = DeviceType.command;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#changeModel(java.lang.Object)
	 */
	@Override
	protected void changeModel(Object positionObject)
			throws DriveDeviceFailedException {
		// TODO Auto-generated method stub

	}
	
	public static RotaryAttenuator getInstance(){
		if (instance == null){
			instance = new RotaryAttenuator();
		}
		return instance;
	}
	
//	/* (non-Javadoc)
//	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#moveTo(java.util.List)
//	 */
//	@Override
//	public String moveTo(List<String> positionDescription) {
//		// TODO Auto-generated method stub
//		String result = "";
//		RotaryAttenuatorAngle attenuatorAngle = RotaryAttenuatorAngle.getInstance();
//		result += attenuatorAngle.moveTo(positionDescription);
//		
//		result += runAsCommand(QuokkaConstants.AttRot_PATH);
//		this.positionDescription = positionDescription;
//		return result;
//	}

}
