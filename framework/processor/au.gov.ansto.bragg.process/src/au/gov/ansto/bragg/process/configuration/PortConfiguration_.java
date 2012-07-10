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

public class PortConfiguration_ extends Configuration_ implements
		PortConfiguration {

	public final static long serialVersionUID = 1L;
	
	protected int dimension;
	protected String type;
	protected String parentName;
	public PortConfiguration_() {
		super();
		dimension = DEFAULT_DIMENSION;
		type = DEFAULT_TYPE; 
		parentName = DEFAULT_PARENTNAME;
		// TODO Auto-generated constructor stub
	}

	public PortConfiguration_(String name, int dimension, String type, String parentName) {
		super(name);
		this.setDimension(dimension);
		this.setType(type);
		this.setParentName(parentName);
		// TODO Auto-generated constructor stub
	}

	public int getDimension() {
		// TODO Auto-generated method stub
		
		return dimension;
	}

	public String getParentName() {
		// TODO Auto-generated method stub
		return parentName;
	}

	public String getType() {
		// TODO Auto-generated method stub
		return type;
	}

	public void setDimension(final int dimension){
		this.dimension = dimension;
	}
	
	public void setParentName(final String parentName) {
		// TODO Auto-generated method stub
		this.parentName = parentName;
	}

	public void setType(final String type) {
		// TODO Auto-generated method stub
		this.type = type;
	}
	
	public String toString(){
		String result = super.toString();
//		result += "<receipe_id>" + getReceipeID() + "</receipe_id>\n";
		result += "<dimension>" + getDimension() + "</dimension>\n";
		result += "<type>" + getType() + "</type>\n";
		result += "<parent_name>" + getParentName() + "</parent_name>\n";
		return result;
	}

}
