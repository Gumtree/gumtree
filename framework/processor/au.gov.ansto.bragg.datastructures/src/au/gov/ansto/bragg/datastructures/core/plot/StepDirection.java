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
package au.gov.ansto.bragg.datastructures.core.plot;

/**
 * Enum type of Object that used for describing direction.
 * @author nxi
 * Created on 13/10/2008
 */

public class StepDirection{

	public enum StepDirectionType {
	forward,
	backward,
	holding
	}
	
	StepDirectionType type;
	
	/**
	 * 
	 * @param typeName
	 */
	public StepDirection(String typeName){
		type = StepDirectionType.valueOf(typeName);
	}
	
	public StepDirection(StepDirectionType type){
		this.type = type;
	}
	
	public StepDirectionType getDirectionType(){
		return type;
	}
	
	public String toString(){
		return type.name();
	}
}