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
 * Created on 26/05/2008
 */
public class RotaryAperture extends VirtualDevice {

//	private String description = "The device that describe the entrace aperture.";
	private static RotaryAperture instance;
	
	/**
	 * 
	 */
	protected RotaryAperture() {
		// TODO Auto-generated constructor stub
		super();
		setId("RotAp");
		setName("Rotary Aperture");
		setUnit("");
		setDescription("The device that describe the entrace aperture.");
		setSicsDevicePath(QuokkaConstants.RotAp_PATH);
		addSubdevice(RotaryApertureAngle.getInstance());
		addSubdevice(EntranceApertureShape.getInstance());
		deviceType = DeviceType.command;
	}
	
	public static RotaryAperture getInstance(){
		if (instance == null){
			instance = new RotaryAperture();
		}
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#changeModel(java.lang.Object)
	 */
	@Override
	protected void changeModel(Object position)
			throws DriveDeviceFailedException {
		// TODO Auto-generated method stub
	}

//	/* (non-Javadoc)
//	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#getDescription()
//	 */
//	@Override
//	public String getDescription() {
//		// TODO Auto-generated method stub
//		return description;
//	}
//
//	/* (non-Javadoc)
//	 * @see au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice#moveTo(java.util.List)
//	 */
//	@Override
//	public String moveTo(List<String> positionDescription) {
//		// TODO Auto-generated method stub
//		String result = "";
//		List<String> angleString = new ArrayList<String>();
//		angleString.add(positionDescription.get(0));
//		RotaryApertureAngle apertureAngle = RotaryApertureAngle.getInstance();
//		result += apertureAngle.moveTo(angleString);
//		
//		List<String> shapeString = new ArrayList<String>();
//		shapeString.add(positionDescription.get(1));
//		RotaryApertureShape apertureShape = RotaryApertureShape.getInstance();
//		result += apertureShape.moveTo(shapeString);
//		
//		result += runAsCommand(QuokkaConstants.RotAp_PATH);
//		this.positionDescription = positionDescription;
//		return result;
//	}

}
