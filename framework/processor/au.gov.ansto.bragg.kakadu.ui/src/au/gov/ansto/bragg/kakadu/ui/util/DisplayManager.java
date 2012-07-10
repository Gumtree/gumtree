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
package au.gov.ansto.bragg.kakadu.ui.util;

import org.eclipse.swt.widgets.Display;

public class DisplayManager {

	private boolean isAsyncExecEnabled = true;
	private boolean isSyncExecEnabled = true;
	private boolean isEnabled = true;
	
	private static DisplayManager instance;
	
	private DisplayManager(){	
	}
	
	public static DisplayManager getDefault(){
		if (instance == null)
			instance = new DisplayManager();
		return instance;
	}
	
	public void asyncExec(Runnable runnable){
		if (isEnabled && isAsyncExecEnabled)
			Display.getDefault().asyncExec(runnable);
	}

	public void syncExec(Runnable runnable){
		if (isEnabled && isSyncExecEnabled)
			Display.getDefault().syncExec(runnable);
	}

	public void setEnable(boolean isEnabled){
		this.isEnabled = isEnabled;
	}
	
	public void setAsyncExecEnable(boolean isEnabled){
		isAsyncExecEnabled = isEnabled;
	}

	public void setSyncExecEnable(boolean isEnabled){
		isSyncExecEnabled = isEnabled;
	}

	
}
