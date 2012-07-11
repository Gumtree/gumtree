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

import java.util.Iterator;
import java.util.List;

import au.gov.ansto.bragg.quokka.exp.core.Command;
import au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment;
import au.gov.ansto.bragg.quokka.exp.core.exception.InitializeCommandException;

/**
 * @author nxi
 * Created on 21/04/2008
 */
public class History implements Command {

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#getHelp()
	 */
	public String getHelp() {
		// TODO Auto-generated method stub
		String help = "HISTORY: Print the history commands.\n";
		help += "Usage: history\n";
		help += "This command requires no argument.\n";
		return help;	
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#getShortDescription()
	 */
	public String getShortDescription() {
		// TODO Auto-generated method stub
		String description = "HISTORY: Print the history commands.\n";
		description += "Usage: history\n";
		description += "For more information, please use 'help history'.\n";
		return description;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.Command#run()
	 */
	public String run() {
		// TODO Auto-generated method stub
		String result = "";
		List<String> history = QuokkaExperiment.getHistory();
		if (history == null || history.size() == 0) return "none";
		for (Iterator iterator = history.iterator(); iterator.hasNext();) {
			result += iterator.next() + "\n";
		}
		return result;
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
