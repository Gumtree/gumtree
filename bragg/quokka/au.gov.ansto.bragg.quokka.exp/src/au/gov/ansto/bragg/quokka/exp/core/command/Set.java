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

/**
 * @author nxi
 * Created on 23/06/2008
 */
public class Set implements Command {

	private String parameterName;
	private String value;
	private QuokkaExperiment experiment;
	
	public Set() {
		// TODO Auto-generated constructor stub
		super();
//		positionDescription= new ArrayList<String>();
	}

	public String run() {
		// TODO Auto-generated method stub
		String result = "";
		try {
			result += QuokkaInstrument.set(QuokkaExperiment.getQuokkaModel(), 
					parameterName, QuokkaExperiment.getSics(), value);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "failed to set the parameter: " + parameterName + "\ncaused by: " + e.getMessage() + "\n";
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
			parameterName = params[0].toLowerCase();
		} catch (Exception e) {
			// TODO: handle exception
			throw new InitializeCommandException("failed to define the motor: " + params[0]);
		}
		try {
			value= params[1];
		} catch (Exception e) {
			// TODO: handle exception
			throw new InitializeCommandException("failed to recognise target position: " + params[1]);
		}
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		String help = "Set: Set the parameter in the Sics model.\n\n";
		help += "Usage: set <SICSParameterID> <value>\n\n";
		help += "This generic command will send instructions to SICS server to set a "
			+ "parameter to a specific value.\n\n";
		help += "<SICSParameterID> \t exact id of device in the SICS instrument control tree. "
			+ "It is not an optional argument.\n\n";
		help += "<position> \t the target value for the parameter. It is not " 
			+ "an optional argument.\n";
		return help;
	}

	public String getShortDescription() {
		// TODO Auto-generated method stub
		String description = "Set: Set the parameter in the Sics model.\n";
		description += "Usage: set <SICSParameterID> <value>\n";
		description += "For more information, please use 'help set'.\n";
		return description;
	}

	

}
