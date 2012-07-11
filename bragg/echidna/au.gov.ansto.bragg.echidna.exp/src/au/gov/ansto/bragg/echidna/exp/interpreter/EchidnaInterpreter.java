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
package au.gov.ansto.bragg.echidna.exp.interpreter;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.ui.internal.cli.beanshell.InstrumentCommandSpace;
import org.gumtree.ui.internal.cli.beanshell.InstrumentInterpreter;

import au.gov.ansto.bragg.echidna.exp.core.EchidnaExperiment;

public class EchidnaInterpreter extends InstrumentInterpreter{

	public EchidnaInterpreter(){
		super();
	}
	
	@Override
	public List<String> interpret(String command){
		List<String> output = null;
		String[] words = null;
		if (command.contains(",")){
			words = command.split(",");
		}else
		words = command.split(" ");
		output = createMethodLines(words);
		return output;
	}

	private List<String> createMethodLines(String[] words) {
		// TODO Auto-generated method stub
		String methodName = null;
		List<String> parameters = new ArrayList<String>();
		int beginOfParameter = 0;
		for (int i = 0; i < words.length; i++) {
			if (words[i].length() > 0){
				methodName = words[i];
				beginOfParameter = i + 1;
				break;
			}
		}
		for (int i = beginOfParameter; i < words.length; i++) {
			if (words[i].length() > 0){
				parameters.add(words[i]);
			}
		}
		EchidnaCommand quokkaCommand = new EchidnaCommand(methodName, parameters);
		
		return quokkaCommand.getBatchCommands();
	}
	
	@Override
	public InstrumentCommandSpace getCommandSpace(){
		return EchidnaExperiment.getInstance();
	}
}
