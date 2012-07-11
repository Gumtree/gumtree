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
public class SampleApertureShape extends VirtualDevice {

	private static SampleApertureShape instance;
	/**
	 * 
	 */
	public SampleApertureShape() {
		// TODO Auto-generated constructor stub
		setId("sapshape");
		setName("SApShape");
		setUnit("");
		setDescription("The sample aperture shape");
		lookUpTable.add("circ");
		lookUpTable.add("squ");
		lookUpTable.add("open");
		lookUpTable.add("rect");
//		setSicsDevicePath(QuokkaConstants.SApShape_PATH);
		deviceType = DeviceType.setable;
	}

	public static SampleApertureShape getInstance(){
		if (instance == null){
			instance = new SampleApertureShape();
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
		getQuokkaModel().getSampleAperture().setShape(positionObject.toString());
	}

//	/* (non-Javadoc)
//	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#moveTo(java.util.List)
//	 */
//	@Override
//	public String moveTo(List<String> positionDescription) {
//		// TODO Auto-generated method stub
//		return moveNonDrivable(QuokkaConstants.SApShape_PATH, positionDescription);
//	}

}
