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

import org.gumtree.scripting.ScriptBlock;
import org.gumtree.scripting.ScriptExecutor;

/**
 * @author nxi
 * Created on 29/10/2008
 */
public class PythonExecutor {
	
	public static int TIME_INTERVAL = 100;
		
	public static void runScript(ScriptExecutor executor, String script, boolean silenceMode){
		executor.runScript(script, silenceMode);
		while(true){
			try {
				Thread.sleep(TIME_INTERVAL);
			}catch (InterruptedException e) {
				// TODO: handle exception
			}
			if (!executor.isBusy())
				break;
		}
	}

	public static void runScript(ScriptExecutor executor, ScriptBlock block,
			boolean silenceMode) {
		// TODO Auto-generated method stub
		executor.runScript(block);
		while(true){
			try {
				Thread.sleep(TIME_INTERVAL);
			}catch (InterruptedException e) {
				// TODO: handle exception
			}
			if (!executor.isBusy())
				break;
		}
	}
}
