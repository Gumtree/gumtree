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
 * Created on 27/05/2008
 */
public class SampleApertureSize extends VirtualDevice{

	private static SampleApertureSize instance;
	/**
	 * 
	 */
	public SampleApertureSize() {
		// TODO Auto-generated constructor stub
		setId("SApSize");
		setName("Sample Aperture Size");
		setUnit("mm");
		setDescription("The sample aperture size");
		lookUpTable.add(String.valueOf("25"));
		lookUpTable.add(String.valueOf("50"));
		setSicsDevicePath(QuokkaConstants.SApPosXmm_PATH);
		deviceType = DeviceType.setable;
	}

	public static SampleApertureSize getInstance(){
		if (instance == null){
			instance = new SampleApertureSize();
		}
		return instance;
	}
	
	@Override
	protected void changeModel(Object positionObject)
			throws DriveDeviceFailedException {
		// TODO Auto-generated method stub
		double position = 0; 
		try {
			position = Double.valueOf(positionObject.toString());
		} catch (Exception e) {
			// TODO: handle exception
			throw new DriveDeviceFailedException("not a numeric value");
		}
		getQuokkaModel().getSampleAperture().setSize(position);
	}

//	@Override
//	public String moveTo(List<String> positionDescription) {
//		// TODO Auto-generated method stub
//		String result = "";
//		result += moveNonDrivable(QuokkaConstants.SApPosXmm_PATH, positionDescription);
////		result += moveNonDrivable(QuokkaConstants.SApPosZmm_PATH, positionDescription);
//		return result;
//	}

}
