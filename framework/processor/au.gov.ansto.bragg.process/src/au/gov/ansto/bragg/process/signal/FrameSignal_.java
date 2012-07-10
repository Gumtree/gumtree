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
package au.gov.ansto.bragg.process.signal;

import au.gov.ansto.bragg.process.common.Common_;

public class FrameSignal_ extends Common_ implements FrameSignal {

	public static final long serialVersionUID = 1L;
	
	protected Object signal;
	protected long[] dimension;
	protected String type;
	protected int id;
	protected String name;
	
	
	public long[] getDimension(){
		return dimension;
	}
	
	public Object getSignal(){
		return signal;
	}
	
	public String getType(){
		return type;
	}
	
	public void setDimension(long[] dimension){
		this.dimension = dimension;
	}
	
	public void setSignal(Object signal){
		this.signal = signal;
	}
	
	public void setType(String type){
		this.type = type;
	}
}
