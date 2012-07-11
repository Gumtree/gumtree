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
 * Created on 27/05/2008
 */
public class EntranceApertureShape extends VirtualDevice {
	
//	private String description = "The rotary aperture shape in selection list";
	private static EntranceApertureShape instance;
	/**
	 * 
	 */
	protected EntranceApertureShape() {
		// TODO Auto-generated constructor stub
		super();
		setId("eapshape");
		setName("EApShape");
		setUnit("");
		setDescription("The entrance aperture shape in selection list");
		lookUpTable.add("circ");
		lookUpTable.add("squ");
		lookUpTable.add("open");
		lookUpTable.add("rect");
//		setSicsDevicePath(QuokkaConstants.RotApShape_PATH);
		deviceType = DeviceType.setable;
	}

	public static EntranceApertureShape getInstance(){
		if (instance == null){
			instance = new EntranceApertureShape();
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
		getQuokkaModel().getRotaryAperture().setShape(positionObject.toString());
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
//		return moveNonDrivable(QuokkaConstants.RotApShape_PATH, positionDescription);
//	}

}
