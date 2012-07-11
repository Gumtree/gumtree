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
public class SampleAperture extends VirtualDevice {

	private static SampleAperture instance;
	
	/**
	 * 
	 */
	public SampleAperture() {
		// TODO Auto-generated constructor stub
		setId("SAp");
		setName("Sample Aperture");
		setUnit("");
		setDescription("The sample aperture");
		setSicsDevicePath(QuokkaConstants.SAp_PATH);
		addSubdevice(SampleApertureSize.getInstance());
		addSubdevice(SampleApertureShape.getInstance());
		deviceType = DeviceType.command;
	}

	public static SampleAperture getInstance(){
		if (instance == null){
			instance = new SampleAperture();
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

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#moveTo(java.util.List)
	 */
	@Override
	public String moveTo(List<String> positionDescription) {
		// TODO Auto-generated method stub
//		String result = "";
//		List<String> sizeString = new ArrayList<String>();
//		sizeString.add(positionDescription.get(0));
//		SampleApertureSize apertureSize = SampleApertureSize.getInstance();
//		result += apertureSize.moveTo(sizeString);
//		
//		List<String> shapeString = new ArrayList<String>();
//		shapeString.add(positionDescription.get(1));
//		SampleApertureShape apertureShape = SampleApertureShape.getInstance();
//		result += apertureShape.moveTo(shapeString);
//		
//		result += runAsCommand(QuokkaConstants.SAp_PATH);
//		this.positionDescription = positionDescription;
//		return result;
		return updateSubdevice(QuokkaConstants.SAp_PATH, positionDescription);
	}

}
