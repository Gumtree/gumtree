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
package au.gov.ansto.bragg.quokka.exp.core.command;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import au.gov.ansto.bragg.quokka.exp.core.Command;
import au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment;
import au.gov.ansto.bragg.quokka.exp.core.ScanResult;
import au.gov.ansto.bragg.quokka.exp.core.exception.InitializeCommandException;
import au.gov.ansto.bragg.quokka.exp.core.lib.Reflection;
import au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice;
import au.gov.ansto.bragg.quokka.model.core.instrument.QuokkaInstrument;

/**
 * @author nxi
 *
 */
public class Device implements Command {

	QuokkaExperiment experiment;
	String motorId;
//	ComponentType type;
//	public enum ComponentType{motor, parameter};
	/**
	 * 
	 */
	public Device() {
		// TODO Auto-generated constructor stub
		super();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#getHelp()
	 */
	public String getHelp() {
		// TODO Auto-generated method stub
		String help = "DEVICE: Check device functionality.\n\n";
		help += "Usage: device [<SICSComponentName>]\n\n";
		help += "This generic command will print the description of the SICSComponent referred by "
			+ "the name.\n\n";
		help += "<SICSComponentName> \t exact name of component in the SICS instrument control tree. "
			+ "It is an optional argument.\n\n";
		help += "If no device name is provided, the command will list the names of all the " +
		"available devices.\n";
		return help;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#getShortDescription()
	 */
	public String getShortDescription() {
		// TODO Auto-generated method stub
		String description = "DEVICE: Check device description.\n";
		description += "Usage: device [<SICSComponentName>]\n";
		description += "For more information, please use 'help device'.\n";
		return description;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#run()
	 */
	public String run() {
		// TODO Auto-generated method stub
		String result = "";
		if (motorId == null) {
//			result += printFunctionList();
//			return result;
			result = printAvailableDevices();
		}else{
			try{
				VirtualDevice device = QuokkaInstrument.getDevice(motorId);
				result += device.getDescription() + "\nDevice type: " + device.getDeviceType() +
				"\nCurrent position: " + device.getPosition() + device.getUnit() + "\n";
				ScanResult scanResult = experiment.findResultForSameDevice(device);
				if (scanResult != null)
					result += scanResult.toString();
			}catch (Exception ex){
				return "Failed to find the motor with ID = " + motorId + "\n";
			}
		}
//		result += print();
		return result;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#setExperiment(au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment)
	 */
	public void setExperiment(QuokkaExperiment experiment) {
		// TODO Auto-generated method stub
		this.experiment = experiment;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#setParameter(java.lang.String[])
	 */
	public void setParameter(String... params)
	throws InitializeCommandException {
		// TODO Auto-generated method stub
		if (params != null){
			if (params.length == 0) return;
			if (params.length > 1) 
				throw new InitializeCommandException("can not match parameters (expecting 2 parameters)");
			try {
				motorId = params[0];
			} catch (Exception e) {
				// TODO: handle exception
				throw new InitializeCommandException("failed to define the motor: " + params[0]);
			}
		}
	}

	public String printAvailableDevices(){
		String result = "";
		try {
			Set<String> deviceNames = Reflection.findClassNames(
					VirtualDevice.class.getPackage().getName(), false);
			result += "Available SICS devices are: \n";
			for (Iterator<?> iterator = deviceNames.iterator(); iterator
			.hasNext();) {
//				result += iterator.next() + "\n";
				String className = (String) iterator.next();
				if (className.contains(".VirtualDevice")) continue;
				try {
					Class<?> deviceClass = Class.forName(className);
					Method method = deviceClass.getMethod("getInstance", new Class[]{});
					VirtualDevice device = (VirtualDevice) method.invoke(deviceClass, new Object[]{});
//					if (device.getDeviceType() == DeviceType.moveable){
						result += "\n" + device.getId() + " (" + device.getName() + "): " + device.getDescription();
						result += "\nDevice type is " + device.getDeviceType(); 
						result += "\n" + "current posiont is : " + device.getPosition();
						ScanResult scanResult = experiment.findResultForSameDevice(device);
						if (scanResult != null)
							result += "\n" + scanResult.toString();
						result += "\n";
//					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			experiment.printlnToShell("failed to print descriptions for available SICS devices");
		}
		return result;
	}
}
