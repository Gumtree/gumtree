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
package au.gov.ansto.bragg.process.common;

import au.gov.ansto.bragg.base.common.All_;
import au.gov.ansto.bragg.process.common.exception.IllegalNameSetException;
import java.beans.PropertyVetoException;

public abstract class Common_ extends All_ implements Common {

	private Long timestamp_;
	
	public Common_(){
		this(UNDEFINED_NAME); 
	}
	
	public Common_(String name){
		try{
			setName(name);
		}catch (IllegalNameSetException ex)
		{
			log().echo("Change name exception logged: "+ name);
		}
	}
	
	public int getID(){
		return (int) is().getId();
	}
	
	public String getName() {		
		return this.is().getName();
	}

	public Long getTimestamp() {
		if (timestamp_==null)
			timestamp_=System.currentTimeMillis();
		return timestamp_;
	}
	
	public void setName(final String name) throws IllegalNameSetException {
		if (name==null)
			throw new IllegalNameSetException("Invalid Name.");
		try{
			is().setName(name);
		}catch (PropertyVetoException ex)
		{
			log().error("Change name exception logged: "+ name);
			log().echo("Change name exception logged: "+ name);
		}

	}

	public void setTimestamp() {
		this.timestamp();
	}

	public void setTimestamp(final Long timestamp) throws IllegalArgumentException{
		if (timestamp==null)
			throw new IllegalArgumentException("No timestamp is given.");
		timestamp_=timestamp;
	}

	protected void timestamp(){
		timestamp_=System.currentTimeMillis();
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.ansto.bragg.base.common.All_#toString()
	 * Overriding toString method
	 */
	public String toString(){
		String result = "<name>" + getName() +"</name>\n";
		result += "<id>" + getID() + "</id>\n";
		return result;
	}

}
