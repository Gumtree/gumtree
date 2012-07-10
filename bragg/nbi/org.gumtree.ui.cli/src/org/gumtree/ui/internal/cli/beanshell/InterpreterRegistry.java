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

import static org.gumtree.ui.internal.cli.beanshell.InterpreterRegistryConstants.INTERPRETER_EXTENSION_CLASSID_ATTRIBUTE_NAME;
import static org.gumtree.ui.internal.cli.beanshell.InterpreterRegistryConstants.INTERPRETER_EXTENSION_NAME_ATTRIBUTE_NAME;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;

public class InterpreterRegistry {

	private static final String DEFAULT_INTERPRETER_CLASS = "org.gumtree.ui.internal.cli.beanshell.InstrumentInterpreter";
//	private static final String INTERPRETER_METHOD_NAME = "interpret";
//	private static final String OPTION_SICS_INSTRUMENT = "sicsInstr";
	private static final String PROP_SICS_INSTRUMENT = "gumtree.instrument.id";
//	public static List<String> interpreterIDList;
	public static String defaultInterpreterClassID;
	public static String defaultInterpreterClassName;
	public static Map<String, String> interpreterMap;
	
	public static InterpreterRegistry getInstance(){
		return new InterpreterRegistry();
	}

	public Map<String, String> getInterpreterMap() {
		return interpreterMap;
	}

	protected InterpreterRegistry(){
		if (interpreterMap == null){
			interpreterMap = new HashMap<String, String>();
			InterpreterExtensionReader extensionReader = 
				new InterpreterExtensionReader(this);
			extensionReader.readCicadaConfigurationExtensions();
			setDefaultInterpreter();
		}
	}

	private void setDefaultInterpreter() {
		// TODO Auto-generated method stub
		String sicsIntrument = System.getProperty(PROP_SICS_INSTRUMENT);
		if(sicsIntrument != null) {
			Set<String> keySet = interpreterMap.keySet();
			for (Iterator<?> iterator = keySet.iterator(); iterator.hasNext();) {
				String interpreterClassName = (String) iterator.next();
				String interpreterClassID = interpreterMap.get(interpreterClassName);
				if(interpreterClassID.contains(sicsIntrument)){
					defaultInterpreterClassName = interpreterClassName;
					defaultInterpreterClassID = interpreterClassID;
				}
			}
		}else
			defaultInterpreterClassID = DEFAULT_INTERPRETER_CLASS;
	}

	public void addInterpreterClass(IConfigurationElement element) {
		// TODO Auto-generated method stub
		String interpreterClassID = element.getAttribute(INTERPRETER_EXTENSION_CLASSID_ATTRIBUTE_NAME);
		String interpreterName = element.getAttribute(INTERPRETER_EXTENSION_NAME_ATTRIBUTE_NAME);
		interpreterMap.put(interpreterName, interpreterClassID);
	}

	public String getDefaultInterpreterClassID() {
		return defaultInterpreterClassID;
	}

	public String getDefaultInterpreterName() {
		return defaultInterpreterClassName;
	}
}
