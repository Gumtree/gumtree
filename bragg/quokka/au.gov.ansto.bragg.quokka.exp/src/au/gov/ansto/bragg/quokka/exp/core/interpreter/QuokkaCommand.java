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
package au.gov.ansto.bragg.quokka.exp.core.interpreter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QuokkaCommand {
	private String methodName = null;
	private List<String> parameters = null;
	static final String IMPORT_EXPERIMENT = "import au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment";
	static final String IMPORT_FACTORY = "import au.gov.ansto.bragg.quokka.exp.core.ExperimentFactory";


	public QuokkaCommand(String methodName, List<String> parameters){
		this.methodName = methodName;
		this.parameters = parameters;
	}

	public List<String> getBatchCommands() {
		// TODO Auto-generated method stub
		List<String> commandLines = new ArrayList<String>();
		commandLines.add(IMPORT_FACTORY);
		commandLines.add(IMPORT_EXPERIMENT);
		commandLines.addAll(analyseMethodName());
		return commandLines;
	}

	private List<String> analyseMethodName() {
		// TODO Auto-generated method stub
		List<String> commandLines = new ArrayList<String>();
		String command = "";
		if (parameters != null && parameters.size() > 0){
			command = "param = new String[]{";
			for (Iterator<?> iterator = parameters.iterator(); iterator.hasNext();) {
				String param = (String) iterator.next();
				command += "\"" + param + "\"";
				if (iterator.hasNext()) command += ",";
			}
			command += "};";
		}else command = "param = null";
		commandLines.add(command);
//		if (methodName.matches("setFunction")){
		command = "experiment = QuokkaExperiment.getInstance()";
		commandLines.add(command);
		if (methodName.equals("use")){
			command = "experiment.use(param)";
		}
		else //if (methodName.matches("scan"))
		{
			command = "experiment.runCommand(\"" + methodName + "\", param)";
		}
//		command += ((parameters != null && parameters.size() > 0)?",param);":");");
//		command += ",param)";
		commandLines.add(command);
		return commandLines;
	}
}
