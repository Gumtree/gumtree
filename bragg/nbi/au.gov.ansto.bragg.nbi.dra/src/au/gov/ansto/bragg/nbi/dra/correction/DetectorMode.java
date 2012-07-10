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
package au.gov.ansto.bragg.nbi.dra.correction;


/**
 * @author nxi
 * Created on 02/12/2008
 */
public enum DetectorMode {
	TIME ("time"),
	DETECTOR_TOTAL ("detector counts"),
	MONITOR1 ("monitor counts 1"),
	MONITOR2 ("monitor counts 2"),
	MONITOR3 ("monitor counts 3");

	private String value;

	DetectorMode(String value){
		this.value = value;
	}
	
	public static DetectorMode getInstance(String value){
		if (value.equals(TIME.value))
			return TIME;
		if (value.equals(DETECTOR_TOTAL.value))
			return DETECTOR_TOTAL;
		if (value.equals(MONITOR1.value))
			return MONITOR1;
		if (value.equals(MONITOR1.value))
			return MONITOR2;
		if (value.equals(MONITOR1.value))
			return MONITOR3;
		return TIME;
	}

	public String getValue(){
		return value;
	}
}
