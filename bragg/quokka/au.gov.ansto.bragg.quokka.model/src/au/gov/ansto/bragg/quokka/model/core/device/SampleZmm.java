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
 * Created on 21/08/2008
 */
public class SampleZmm extends VirtualDevice {

	private static SampleZmm instance;

	/**
	 * 
	 */
	public SampleZmm() {
		// TODO Auto-generated constructor stub
		super();
		setId("samz");
		setName("SampleStageZ");
		setUnit("");
		setDescription("Sample stage in Z direction");
		deviceType = DeviceType.moveable;
	}

	public static SampleZmm getInstance(){
		if (instance == null){
			instance = new SampleZmm();
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
