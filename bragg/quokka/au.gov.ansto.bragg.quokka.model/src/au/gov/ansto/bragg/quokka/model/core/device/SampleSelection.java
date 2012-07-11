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
public class SampleSelection extends VirtualDevice{

//	private String description = "The sample selector";
	private static SampleSelection instance;
	/**
	 * 
	 */
	protected SampleSelection() {
		// TODO Auto-generated constructor stub
		super();
		setId("sampleselection");
		setName("Sample Selector");
		setUnit("");
		setDescription("The sample selector");
		addSubdevice(SampleID.getInstance());
		setSicsDevicePath(QuokkaConstants.SampleSelect_PATH);
		deviceType = DeviceType.command;
	}
	
	public static SampleSelection getInstance(){
		if (instance == null){
			instance = new SampleSelection();
		}
		return instance;
	}
	
	@Override
	protected void changeModel(Object positionObject)
			throws DriveDeviceFailedException {
		// TODO Auto-generated method stub
		
	}

//	@Override
//	public String getDescription() {
//		// TODO Auto-generated method stub
//		return description;
//	}

//	@Override
//	public String moveTo(List<String> positionDescription) {
//		// TODO Auto-generated method stub
//		String result = "";
//		SampleID sampleId = SampleID.getInstance();
//		result += sampleId.moveTo(positionDescription);
//		
//		result += runAsCommand(QuokkaConstants.SampleSelect_PATH);
//		this.positionDescription = positionDescription;
//		return result;
//	}

}
