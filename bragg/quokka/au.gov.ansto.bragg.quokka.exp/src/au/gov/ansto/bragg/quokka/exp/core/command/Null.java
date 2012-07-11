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

/**
 * @author nxi
 * Created on 21/04/2008
 */
public class Null implements Command {

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#getHelp()
	 */
	public String getHelp() {
		// TODO Auto-generated method stub
		String help = "NULL: a command that does nothing.\n\n";
		help += "Usage: null\n\n";
		help += "This command does nothing to experiment. It can be used in creating a loop.\n";
		return help;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#getShortDescription()
	 */
	public String getShortDescription() {
		// TODO Auto-generated method stub
		String description = "NULL: a command that does nothing.\n";
		description += "Usage: null\n";
		description += "For more information, please use 'help null'.\n";
		return description;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#run()
	 */
	public String run() {
		// TODO Auto-generated method stub
		return "";
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#setExperiment(au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment)
	 */
	public void setExperiment(QuokkaExperiment experiment) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#setParameter(java.lang.String[])
	 */
	public void setParameter(String... params)
			throws InitializeCommandException {
		// TODO Auto-generated method stub

	}

}
