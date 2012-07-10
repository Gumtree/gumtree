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
package au.gov.ansto.bragg.cicada.core.extension;

import org.gumtree.data.exception.FileAccessException;

import au.gov.ansto.bragg.datastructures.util.ConverterLib;

public class AlgorithmSet{
	String id = null;
	String name = null;
	String version = null;
	private boolean available = true;
	private boolean isDefault = false;
	
	public AlgorithmSet(String id, String name, String version){
		this.id = id;
		this.name = name;
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}
	
	public void setAvailability(boolean available){
		this.available = available;
	}
	
	public boolean isAvailable(){
		return available;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
	
	public String getPath() throws FileAccessException{
		return ConverterLib.path2URI(ConverterLib.findFile(id, "/xml").getPath()).getPath();

	}
	
	public String toString(){
		String result = "<algorithm_set>\n";
		result += "<id>" + getId() +"</id>\n";
		result += "<name>" + getName() + "</name>\n";
		result += "<version>" + getVersion() + "</version>\n";
		result += "</algorithm_set>";
		return result;
	}
}