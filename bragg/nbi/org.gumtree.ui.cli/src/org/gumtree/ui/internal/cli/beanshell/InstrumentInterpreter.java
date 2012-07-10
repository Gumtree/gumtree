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
package org.gumtree.ui.internal.cli.beanshell;

import java.util.LinkedList;
import java.util.List;

public class InstrumentInterpreter {
	
	public static InstrumentInterpreter getInstance(String interpreterClassID) 
	throws CreateInterpreterInstanceFailedException{
		InstrumentInterpreter interpreter = new InstrumentInterpreter();
		if (interpreterClassID != null){
			try {
//				Class<?> interpreterClass = Class.forName(interpreterClassID);
				ClassLoader loader = InstrumentInterpreter.class.getClassLoader();
				Class<?> interpreterClass = loader.loadClass(interpreterClassID);
				interpreter = (InstrumentInterpreter) interpreterClass.newInstance();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new CreateInterpreterInstanceFailedException(
						"can not create the specific interpreter instance, " +
						"use default interpreter instead");
			}
		}
		return interpreter;
	}
	
	public InstrumentInterpreter(){
		super();
	}
	
	public List<String> interpret(String command){
		command = command.trim();
		List<String> output = new LinkedList<String>();
		if (command.contains("use ")){
			InstrumentCommandSpace instrumentCommand = new InstrumentCommandSpace();
			String result = instrumentCommand.use(command.substring(
					command.indexOf("use ") + 4));
			command = "\"" + result + "\";";
		}
		output.add(command);
		return output;
	}
	
	public InstrumentCommandSpace getCommandSpace(){
		return new InstrumentCommandSpace();
	}
}
