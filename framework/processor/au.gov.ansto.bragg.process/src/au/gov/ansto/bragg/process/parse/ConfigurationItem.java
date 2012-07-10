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
package au.gov.ansto.bragg.process.parse;

import au.gov.ansto.bragg.process.common.Common_;


public class ConfigurationItem extends Common_{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id = 0;
	private String name = null;  
	private String version = null;
	private String className = null;
	private String defaultLoaded = null;

	public ConfigurationItem(int id, String name, String version,
			String className, String defaultLoaded) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.className = className;
		this.defaultLoaded = defaultLoaded;
	}

	/**
	 * @return the domain
	 */
	public int getID() {
		return id;
	}

	/**
	 * @return the version
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the help_url
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return the short_description
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the default_id
	 */
	public boolean isDefault() {
		return Boolean.valueOf(defaultLoaded);
	}
	
	public String toString(){
		String result = "<plugin>\n";
		result += "<id>" + getID() + "</id>\n";
		result += "<name>" + getName() + "</name>\n";
		result += "<version>" + getVersion() + "</version>\n";
		result += "<default>" + isDefault() +"</default>\n";
		result += "</plugin>\n";
		return result;
	}
}
