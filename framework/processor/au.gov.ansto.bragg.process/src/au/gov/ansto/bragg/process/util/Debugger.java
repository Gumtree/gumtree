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
package au.gov.ansto.bragg.process.util;

/**
 * @author nxi
 * Created on 14/05/2009
 */
public class Debugger {

	public static long staticTimer = System.currentTimeMillis();
	public static long lastInterval = 0;
	public static long accumulatedTime = 0;
	
	public static long getCurrentTimer(){
		return System.currentTimeMillis() - staticTimer;
	}
	
	public static long stampOnTime(){
		long currentTime = System.currentTimeMillis();
		lastInterval = currentTime - staticTimer;
		accumulatedTime += lastInterval;
		staticTimer = currentTime;
		return accumulatedTime;
	}
	
	public static long getLastInterval(){
		return lastInterval;
	}
}
