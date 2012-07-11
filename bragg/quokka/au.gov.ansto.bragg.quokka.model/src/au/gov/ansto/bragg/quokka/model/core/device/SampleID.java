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
import au.gov.ansto.bragg.quokka.model.core.exception.DriveDeviceFailedException;

/**
 * @author nxi
 * Created on 27/05/2008
 */
public class SampleID extends VirtualDevice {

//	private String description = ;
	private static SampleID instance;
	/**
	 * 
	 */
	public SampleID() {
		// TODO Auto-generated constructor stub
		super();
		setId("samplenum");
		setName("Sample ID");
		setUnit("");
		setDescription("The sample ID");
		setRange(8, 0);
//		setSicsDevicePath(QuokkaConstants.SampleNum_PATH);
		setRadonly(true);
		deviceType = DeviceType.setable;
	}

	public static SampleID getInstance(){
		if (instance == null){
			instance = new SampleID();
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
			throw new DriveDeviceFailedException("not a numeric value");
		}
		getQuokkaModel().getCurrentSample().setSampleNum(position);
	}

//	/* (non-Javadoc)
//	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#moveTo(java.util.List)
//	 */
//	@Override
//	public String moveTo(List<String> positionDescription) {
//		// TODO Auto-generated method stub
//		return moveNonDrivable(QuokkaConstants.SampleNum_PATH, positionDescription);
//	}

}
