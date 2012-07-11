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
 * Created on 27/06/2008
 */
public class RotaryApertureZ extends VirtualDevice {

	private static RotaryApertureZ instance;

	/**
	 * 
	 */
	public RotaryApertureZ() {
		// TODO Auto-generated constructor stub
		super();
		setId("rotapzmm");
		setName("RotApZmm");
		setUnit("mm");
		setDescription("The rotary aperture size in Z direction");
		deviceType = DeviceType.setable;
	}

	public static RotaryApertureZ getInstance(){
		if (instance == null){
			instance = new RotaryApertureZ();
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
