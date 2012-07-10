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
package au.gov.ansto.bragg.process.port;

/**
 * @author nxi
 * Created on 23/10/2008
 */
public class ConcreteField<T> {
	
	private String name;
	private T field;
	
	public ConcreteField(String name, T field){
		this.name = name;
		this.field = field;
	}
	
	public String getName(){
		return name;
	}
	
	public T getField(){
		return field;
	}
	
	public void setField(T field){
		this.field = field;
	}
}
