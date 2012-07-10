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
package au.gov.ansto.bragg.process.configuration;


/**
 * @author nxi
 * Created on 23/02/2007, 2:24:30 PM
 * Last modified 23/02/2007, 2:24:30 PM
 * 
 */
public class AgentConfiguration_ extends Configuration_ implements AgentConfiguration {

	public final static long serialVersionUID = 1L;
	
	protected String pName;
	protected String name;
	protected String principal;
	protected String label;
	
	public AgentConfiguration_(String name, String principal, String pName, String label){
		super(name);
		this.principal = principal;
		this.pName = pName;
		this.label = label;
	}
	
	public String getDescription(){
		return label;
	}
	
	public String getPName(){
		return pName;
	}
	
	public String getPrincipal(){
		return principal;
	}
	
	public String toString(){
		String result = "<agent id=\"" + getID() + "\" name=\"" + getName() + "\" principal=\"" + getPrincipal() + "_" + getPName() + "\"";
		return result;
		
	}
}
