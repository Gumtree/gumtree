/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.core.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.kakadu.core.UIAlgorithmManager;

/**
 * The DataItem class it is a bean for experiment data.
 * Attributes provides additional information about experiment.
 * 
 * 
 * @author Danil Klimontov (dak)
 */
public class DataItem {
	private Map<String, Object> attributes = new LinkedHashMap<String, Object>();
	private String dataObjectPath;
	private String filePath;
	private String name;


	/**
	 * Creates a new DataItem instance.
	 * @param name displayable name of DataItem
	 */
	public DataItem(String name) {
		this.name = name;
	}
	
	public DataItem(String name, IGroup dataObject) {
		this(name);
		this.dataObjectPath = dataObject.getName();
		this.filePath = dataObject.getLocation();
	}

	/**
	 * Gets displayable name of DataItem. 
	 * @return string presentation of DataItem's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets attribute value.
	 * @param attributeName name of attribute
	 * @return attribute value or null if the attribute is not defined.
	 */
	public Object getAttribute(String attributeName) {
		return attributes.get(attributeName);
	}
	
	/**
	 * Sets the attribute value.
	 * @param attributeName name of attribute
	 * @param attributeValue value for attribute
	 */
	public void setAttribute(String attributeName, Object attributeValue) {
		attributes.put(attributeName, attributeValue);
	}
	
	/**
	 * Sets all attributes from a map. 
	 * @param attributesMap attributes map (key - attribute name (String), value - attribute value (Object)) 
	 */
	public void setAttributes(Map<String, Object> attributesMap) {
		attributes.putAll(attributesMap);
	}
	
	/**
	 * Gets attribute names added to DataItem.
	 * @return attribute names.
	 */
	public List<String> getArrributeNames() {
		return new ArrayList<String>(attributes.keySet());
	}
	
	/**
	 * Gets count of attributes for the DataItem.
	 * @return attribute count number.
	 */
	public int getAttributeCount() {
		return attributes.size();
	}

	/**
	 * Gets data object which reference to one data entry in a file (DataItem).
	 * @return data object or null if the object was not set.
	 */
	public IGroup getDataObject() {
		IGroup rootGroup = null;
		try {
			rootGroup = UIAlgorithmManager.getAlgorithmManager().loadDataFromFile(
					ConverterLib.path2URI(filePath));
			return (IGroup) rootGroup.findContainerByPath(dataObjectPath);
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Sets data object associated with the DataItem.
	 * @param dataObject data object
	 */
	public void setDataObject(IGroup dataObject) {
		this.dataObjectPath = dataObject.getName();
		this.filePath = dataObject.getLocation();
	}

}
