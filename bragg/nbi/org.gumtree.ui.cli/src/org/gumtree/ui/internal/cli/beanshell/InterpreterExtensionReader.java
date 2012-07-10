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

import static org.gumtree.ui.internal.cli.beanshell.InterpreterRegistryConstants.INTERPRETER_EXTENSION_ELEMENT;
import static org.gumtree.ui.internal.cli.beanshell.InterpreterRegistryConstants.INTERPRETER_EXTENSION_POINT;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.gumtree.ui.internal.cli.Activator;
import org.gumtree.util.eclipse.ExtensionRegistryReader;

public class InterpreterExtensionReader extends ExtensionRegistryReader{

	InterpreterRegistry interpreterRegistry = null;
	
	protected InterpreterExtensionReader(InterpreterRegistry interpreterRegistry) {
		super(Activator.getDefault());
		this.interpreterRegistry = interpreterRegistry;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean readElement(IConfigurationElement element) {
		if(element.getName().equals(INTERPRETER_EXTENSION_ELEMENT)) {
			readFactory(element);
			return true;
		}
		return false;
	}

	private void readFactory(IConfigurationElement element) {
		interpreterRegistry.addInterpreterClass(element);
	}

	public void readCicadaConfigurationExtensions() {
		IExtensionRegistry in = Platform.getExtensionRegistry();
		readRegistry(in, getPlugin().getBundle().getSymbolicName(), INTERPRETER_EXTENSION_POINT);
	}

}
