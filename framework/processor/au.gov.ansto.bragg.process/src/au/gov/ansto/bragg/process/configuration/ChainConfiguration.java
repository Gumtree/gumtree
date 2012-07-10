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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author nxi
 * Created on 23/04/2008
 */
public class ChainConfiguration extends Configuration_ {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4178986246992936041L;
	private String recipeID;
	private String algorithmName;
	private Map<String, String> tunerConfigurations;
	private String defaultSinkName;
	/**
	 * 
	 */
	public ChainConfiguration() {
		// TODO Auto-generated constructor stub
		super();
		tunerConfigurations = new HashMap<String, String>();
	}

	/**
	 * @param name
	 */
	public ChainConfiguration(String name, String recipeID, String algorithmName) {
		super(name);
		this.recipeID = recipeID;
		this.algorithmName = algorithmName;
		tunerConfigurations = new HashMap<String, String>();
		// TODO Auto-generated constructor stub
	}

	public void addConfiguration(String tunerName, String value){
		tunerConfigurations.put(tunerName, value);
	}

	public String getConfiguration(String tunerName){
		return tunerConfigurations.get(tunerName);
	}

	public void removeConfiguration(String tunerName){
		tunerConfigurations.remove(tunerName);
	}

	public void setConfiguration(String tunerName, String value){
		if (tunerConfigurations.containsKey(tunerName))
			tunerConfigurations.remove(tunerName);
		addConfiguration(tunerName, value);
	}

	public Set<String> getTunerNameSet(){
		return tunerConfigurations.keySet();
	}

	public String toString(){
		String result = "";
		Set<String> keySet = tunerConfigurations.keySet();
		if (keySet.size() > 0){
			result += "<tuners>\n";
			for (String key : keySet){
				String value = tunerConfigurations.get(key).toString();
				result += "<tuner name=\"" + value + "\"/>\n";
			}
			result += "</tuners>\n";
		}
		if (defaultSinkName != null)
			result += "<defaultSink name=\"" + defaultSinkName +"\"/>\n";
		return result;
	}

	/**
	 * @return the algorithmName
	 */
	public String getAlgorithmName() {
		return algorithmName;
	}

	/**
	 * @return the recipeID
	 */
	public String getRecipeID() {
		return recipeID;
	}

	/**
	 * @return the defaultSinkName
	 */
	public String getDefaultSinkName() {
		return defaultSinkName;
	}

	/**
	 * @param defaultSinkName the defaultSinkName to set
	 */
	public void setDefaultSinkName(String defaultSinkName) {
		this.defaultSinkName = defaultSinkName;
	}
}