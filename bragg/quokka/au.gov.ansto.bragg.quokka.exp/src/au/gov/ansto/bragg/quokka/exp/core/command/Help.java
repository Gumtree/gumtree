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
import au.gov.ansto.bragg.quokka.exp.core.lib.Reflection;

public class Help implements Command {

	private Class<?> topic = null;
	private QuokkaExperiment experiment = null;
	
	public Help() {
		// TODO Auto-generated constructor stub
		super();
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		String help = "HELP: Print help documents for commands. \n\n";
		help += "Usage: help [CommandName]\n\n";
		help += "[CommandName] \t a pre-defined command name.\n";
		help += "If no command name is given, it will print a list of short descriptions of "
			+ "all available commands.\n\n";
		help += "arguments listed in square bracket [] are optional.\n";
		return help;
	}

	public String getShortDescription() {
		// TODO Auto-generated method stub
		String description = "HELP: Print help documents for a command.\n";
		description += "Usage: help [CommandName]\n";
		description += "For more information, please use 'help more'.\n";
		return description;
	}

	public String run() {
		// TODO Auto-generated method stub
		String result = "";
		if (topic == null){
			try {
				List<String> commands = Reflection.findClassList(
						QuokkaExperiment.QUOKKA_COMMANDS_PACKAGE_NAME, false);
				result += "Available commands are: \n\n";
				for (Iterator<?> iterator = commands.iterator(); iterator
						.hasNext();) {
//					result += iterator.next() + "\n";
					try {
						Command command = (Command) Class.forName(
								(String) iterator.next()).newInstance();
						result += command.getShortDescription() + "\n";						
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				experiment.printlnToShell("failed to print descriptions for available commands");
			}
		}else{
			try {
				Command command = (Command) topic.newInstance();
				result = command.getHelp();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				experiment.printlnToShell("failed to print help documents for " + topic.getSimpleName());
			} 
		}
		return result;
	}

	public void setExperiment(QuokkaExperiment experiment) {
		// TODO Auto-generated method stub
		this.experiment = experiment;
	}

	public void setParameter(String... params)
			throws InitializeCommandException {
		// TODO Auto-generated method stub
		if (params == null || params.length == 0){
			return;
		}else if (params[0].equals("more")){
			throw new InitializeCommandException(getHelp());
		}else {
			try {
				topic = Class.forName(QuokkaExperiment.QUOKKA_COMMANDS_PACKAGE_NAME + "." + 
						Character.toUpperCase(params[0].charAt(0)) 
						+ params[0].substring(1).toLowerCase());
			} catch (Exception e) {
				// TODO: handle exception
				throw new InitializeCommandException("can not find the manual for command " 
						+ params[0] + "\n\n" + run());
			}
		}
	}

}
