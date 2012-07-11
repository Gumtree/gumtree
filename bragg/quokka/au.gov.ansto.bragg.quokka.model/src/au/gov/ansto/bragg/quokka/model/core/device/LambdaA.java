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
 * Created on 23/06/2008
 */
public class LambdaA extends VirtualDevice {

	protected static LambdaA instance;
	/**
	 * 
	 */
	public LambdaA() {
		// TODO Auto-generated constructor stub
		setId("lambdaa");
		setName("Neutron Wavelength");
		setUnit("A");
		setDescription("The wave length parameter");
		setSicsDevicePath(QuokkaConstants.LambdA);
		deviceType = DeviceType.setable;
	}
	
	public static LambdaA getInstance(){
		if (instance == null){
			instance = new LambdaA();
		}
		return instance;
	}
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#changeModel(java.lang.Object)
	 */
	@Override
	protected void changeModel(Object value)
			throws DriveDeviceFailedException {
		// TODO Auto-generated method stub
		double wavelength = 0;
		try {
			wavelength = Double.valueOf(value.toString());
		} catch (Exception e) {
			// TODO: handle exception
			throw new DriveDeviceFailedException("wrong data type");
		}
		getQuokkaModel().setWaveLength(wavelength);
	}

}
