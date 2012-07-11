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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import au.gov.ansto.bragg.quokka.exp.core.Command;
import au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment;
import au.gov.ansto.bragg.quokka.exp.core.exception.InitializeCommandException;
import au.gov.ansto.bragg.quokka.exp.core.lib.Reflection;
import au.gov.ansto.bragg.quokka.exp.core.scanfunction.Function;

public class Scanfunctionlist implements Command {

	private QuokkaExperiment experiment = null;
	private Action action;
	private List<String> functionList = null;

	private enum Action{Add, Remove, Use};

	public Scanfunctionlist() {
		// TODO Auto-generated constructor stub
		super();
	}

	public String run() {
		// TODO Auto-generated method stub
		String result = "";
		if (functionList == null || functionList.size() == 0){
//			result += printFunctionList();
//			return result;
		}else if (action == Action.Add){
			for (Iterator<?> iterator = functionList.iterator(); iterator.hasNext();){
				String function = (String) iterator.next();
				QuokkaExperiment.addFunction(function);
				result += "add function " + function + " successfully\n";
//				result += printFunctionList();
			}
		}else if (action == Action.Remove){
			for (Iterator<?> iterator = functionList.iterator(); iterator.hasNext();){ 
				String function = (String) iterator.next();
				QuokkaExperiment.removeFunction(function);
				result += "remove function " + function + " successfully\n";
//				result += printFunctionList();
			}
		}else{
			QuokkaExperiment.setFunctionList(functionList);
			result += "set function list successfully\n";
		}
		result += printFunctionList();
		return result;
	}

	private String printFunctionList() {
		// TODO Auto-generated method stub
		String result = "";
		List<Function> functions = QuokkaExperiment.getFunctionList();
		for (Iterator<?> iterator = functions.iterator(); iterator.hasNext();) {
			Function function = (Function) iterator.next();
			result += function.toString() + "\n";
//			experiment.printlnToShell(iterator.next().toString() + "\n");
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
//		if (params.length != 2) 
//		throw new InitializeCommandException("can not match parameters (expecting 2 parameters)");
		if (params != null){
			try {
				action = Action.Use;
				functionList = new ArrayList<String>();
				for (int i = 0; i < params.length; i ++){
					if (params[i].length() == 1){
						if (params[i].equals("\\+")){
							action = Action.Add;
							continue;
						}
						if (params[i].matches("\\-")){
							action = Action.Remove;
							continue;
						}					
					}
//					String function = params[i];
					functionList.add(params[i]);
				}
//				motorName = MotorName.valueOf(params[0]);
			} catch (Exception e) {
				// TODO: handle exception
				throw new InitializeCommandException("failed to define scan function: " + params[0]);
			}
		}
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		String help = "SCANFUNCTIONLIST: Set the pre-defined scan function list to the experiment. \n"
			+ "Once the scan function list has been set, the functions in the list "
			+ "will be calculated by quokka DRA component and will be plot "
			+ "during the scan operation.\n\n";
		help += "Usage: scanfunctionlist [+/-] [<ScanFunctionName> ...]\n\n";
		help += "+ \t\t is optional. If exists must be followed by at least one scan function name. \n"
			+ "The functions will be added to the function list instead of replace the list.\n\n";
		help += "- \t\t is optional. If exists must be followed by at least one scan function name. \n"
			+ "The functions will be removed from the function list instead of replace the list.\n\n";
		help += "<ScanFunctionName> \t the pre-defined scan function names, can be 0 or more.\n\n"
			+ "If no name is provided, it will list current function list "
			+ "(a default list will be set at initialisation).\n\n"
			+ "If no option (+/-) is provided, the current function list will be replaced by "
			+ "the provided function names. \n\n";
		help += printAvailableFunctions() + "\n";
		help += "See also: scan\n";
		return help;
	}

	public String getShortDescription() {
		// TODO Auto-generated method stub
		String description = "SCANFUNCTIONLIST: Set scan function list. \n";
		description += "Usage: scanfunctionlist [+/-] [<ScanFunctionName> ...]\n";
		description += "For more information, please use 'help scanfunctionlist'.\n";
		return description;
	}

	public String printAvailableFunctions(){
		String result = "";
		try {
			Set<String> functionNames = Reflection.findClassNames(
					QuokkaExperiment.QUOKKA_FUNCTIONS_PACKAGE_NAME, false);
			result += "Available scan functions are: \n";
			for (Iterator<?> iterator = functionNames.iterator(); iterator
					.hasNext();) {
//				result += iterator.next() + "\n";
				String className = (String) iterator.next();
				if (className.contains(".Function")) continue;
				try {
					Class<?> functionClass = Class.forName(className);
					Method method = functionClass.getMethod("getInstance", new Class[]{});
					Function function = (Function) method.invoke(functionClass, new Object[]{});
					result += function.getShortDescription();
//					Function function = (Function) Class.forName(className).newInstance();
//					result += function.getShortDescription() + "\n";						
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			experiment.printlnToShell("failed to print descriptions for available scan functions");
		}
		return result;
	}
}
