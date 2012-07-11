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

import au.gov.ansto.bragg.quokka.exp.core.Command;
import au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment;
import au.gov.ansto.bragg.quokka.exp.core.exception.InitializeCommandException;
import au.gov.ansto.bragg.quokka.model.core.instrument.QuokkaInstrument;

public class Move implements Command {

	private String motorName;
	private String position;
	private QuokkaExperiment experiment;
	
	public Move() {
		// TODO Auto-generated constructor stub
		super();
//		positionDescription= new ArrayList<String>();
	}

	public String run() {
		// TODO Auto-generated method stub
		String result = "";
		try {
			result += QuokkaInstrument.move(QuokkaExperiment.getQuokkaModel(), 
					motorName, QuokkaExperiment.getSics(), position);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "failed to drive the motor: " + motorName + "\n";
		}
		return result + "done\n";
	}

	public void setExperiment(QuokkaExperiment experiment) {
		// TODO Auto-generated method stub
		this.experiment = experiment;
	}

	public void setParameter(String... params)
			throws InitializeCommandException {
		// TODO Auto-generated method stub
		if (params == null) 
			throw new InitializeCommandException("can not match parameters (expecting 2 parameters)");
		if (params.length != 2) 
			throw new InitializeCommandException("can not match parameters (expecting 2 parameters)");
		try {
			motorName = params[0].toLowerCase();
		} catch (Exception e) {
			// TODO: handle exception
			throw new InitializeCommandException("failed to define the motor: " + params[0]);
		}
		try {
				position = params[1];
		} catch (Exception e) {
			// TODO: handle exception
			throw new InitializeCommandException("failed to recognise target position: " + params[1]);
		}
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		String help = "MOVE: Drive a SICS device to a target position.\n\n";
		help += "Usage: move <SICSDeviceID> <position>\n\n";
		help += "This generic command will send instructions to SICS server to drive a "
			+ "device to a specific position.\n\n";
		help += "<SICSDeviceID> \t exact id of device in the SICS instrument control tree. "
			+ "It is not an optional argument.\n\n";
		help += "<position> \t the target position for the device to go. It is not " 
			+ "an optional argument.\n";
		return help;
	}

	public String getShortDescription() {
		// TODO Auto-generated method stub
		String description = "MOVE: Drive a SICS device to a target position.\n";
		description += "Usage: move <SICSDeviceID> <position>\n";
		description += "For more information, please use 'help move'.\n";
		return description;
	}

}
