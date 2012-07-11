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
package au.gov.ansto.bragg.kowari.exp.python;

import org.gumtree.scripting.ScriptExecutor;

/**
 * @author nxi
 * Created on 29/10/2008
 */
public class Histmem {

	private ScriptExecutor executor;
		
	public Histmem(ScriptExecutor executor){
		this.executor = executor;
	}

	public void run(String cmd, String mode, String preset){
		String script = "from gumpy.commons import sics\n";
		script += "sics.histmem('" + cmd + "', '" + mode + "', '" + preset + "')";
		PythonExecutor.runScript(executor, script, false);
	}
	
	public void runSilently(String cmd, String mode, String preset){
		String script = "from gumpy.commons import sics\n";
		script += "sics.histmem('" + cmd + "', '" + mode + "', '" + preset + "')";
		PythonExecutor.runScript(executor, script, true);
	}
	/**
	 * @param executor the executor to set
	 */
	public void setExecutor(ScriptExecutor executor) {
		this.executor = executor;
	}

	
}
