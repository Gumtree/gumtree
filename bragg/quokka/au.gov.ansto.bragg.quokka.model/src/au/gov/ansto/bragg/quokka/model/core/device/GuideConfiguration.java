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

import au.gov.ansto.bragg.quokka.model.core.QuokkaConstants;
import au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice.DeviceType;
import au.gov.ansto.bragg.quokka.model.core.exception.DriveDeviceFailedException;

/**
 * @author nxi
 * Created on 30/06/2008
 */
public class GuideConfiguration extends VirtualDevice {

	protected static GuideConfiguration instance;
	/**
	 * 
	 */
	public GuideConfiguration() {
		// TODO Auto-generated constructor stub
		setId("guideconfiguration");
		setName("Beam guide configuration");
		setUnit("");
		setDescription("The wave length resolution percentage parameter");
		setSicsDevicePath(QuokkaConstants.GuideConfiguration_PATH);
		deviceType = DeviceType.setable;
	}

	public static GuideConfiguration getInstance(){
		if (instance == null){
			instance = new GuideConfiguration();
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

	}

}
