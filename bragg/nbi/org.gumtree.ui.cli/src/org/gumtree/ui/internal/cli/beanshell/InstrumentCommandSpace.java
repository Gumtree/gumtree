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

import java.util.Map;

public class InstrumentCommandSpace {
	
	public InstrumentCommandSpace(){
		super();
	}
	
	public String use(String ...params){
		String instrumentName = params[0];
		if (instrumentName.equals("default")){
			BeanShellCommandLineView.getInstance().setInstrumentInterpreter(new InstrumentInterpreter());
			return "default interpreter loaded";
		}
		InterpreterRegistry interpreterRegistry = InterpreterRegistry.getInstance();
		Map<String, String> interpreterMap = interpreterRegistry.getInterpreterMap();
		if (!interpreterMap.containsKey(instrumentName))
			return "can not load the specific instrument interpreter: " + instrumentName;
		else{
			try {
				String interpreterClassID = interpreterMap.get(instrumentName);
				InstrumentInterpreter interpreter = InstrumentInterpreter.getInstance(interpreterClassID);
				BeanShellCommandLineView.getInstance().setInstrumentInterpreter(interpreter);
			} catch (Exception e) {
				// TODO: handle exception
				return "can not load the specific instrument interpreter: " + instrumentName;
			}
		} 
		return instrumentName + " interpreter loaded";
	}
	
	public String runCommand(String commandName, String ...params){
		return "default command space demostrate running command " + commandName;
	}
//	public InstrumentCommand 
}
