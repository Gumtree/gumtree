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
import au.gov.ansto.bragg.quokka.model.core.QuokkaModel;

/**
 * @author nxi
 *
 */
public class Model implements Command {

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#getHelp()
	 */
	public String getHelp() {
		String help = "MODEL: Check the status of the Quokka instrument model.\n";
		help += "Usage: model\n";
		help += "This command requires no argument.\n";
		return help;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#getShortDescription()
	 */
	public String getShortDescription() {
		// TODO Auto-generated method stub
		String description = "MODEL: Check the status of the Quokka instrument model.\n";
		description += "Usage: model\n";
		description += "For more information, please use 'help model'.\n";
		return description;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#run()
	 */
	public String run() {
		// TODO Auto-generated method stub
		QuokkaModel model = QuokkaModel.getInstance();
		String result = model.syncWithSics();
		return result + "\n" + model.getStatus();
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
