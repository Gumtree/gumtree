/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation,
 * Synchrotron SOLEIL and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Norman XIONG (Bragg Institute) - initial API and implementation
 *     Clément RODRIGUEZ (SOLEIL) - initial API and implementation
 *     Tony LAM (Bragg Institute) - implementation
 ******************************************************************************/

package org.gumtree.data.interfaces;

import java.util.List;

import org.gumtree.data.utils.Utilities.ModelType;

/**
 * @brief The IContainer interface is shared IGroup and IDataItem.
 * 
 * The IContainer contains behaviors a node, in a tree of data, should
 * respect.
 * 
 * @author nxi
 * 
 */
public interface IContainer extends IModelObject, Cloneable {
    
	/**
	 * Get the ModelType implemented by this object.
	 * 
     * @return ModelType of this object
	 */
	ModelType getModelType();

	/**
	 * Add an Attribute to the Group.
	 * 
     * @param attribute CDMA Attribute 
	 */
	void addOneAttribute(IAttribute attribute);

	/**
	 * A convenience method of adding a String type attribute.
	 * 
     * @param name String type object
     * @param value String type object 
	 */
	void addStringAttribute(String name, String value);

	/**
	 * Find an Attribute in this Group by its name.
	 * 
     * @param name the name of the attribute
	 * @return the attribute, or null if not found
	 */
	IAttribute getAttribute(String name);

	/**
	 * Get the set of attributes contained directly in this Group.
	 * 
	 * @return List of type Attribute; may be empty, not null.
	 */
	List<IAttribute> getAttributeList();

	/**
	 * Get the Dataset that hold the current Group.
	 * 
     * @return CDMA Dataset 
	 */
	IDataset getDataset();

	/**
	 * Get the location referenced by the Dataset.
	 * 
     * @return String type 
	 */
	String getLocation();

	/**
	 * Get the (long) name of the IObject, which contains the path information.
	 * 
     * @return String type object 
	 */
	String getName();

	/**
	 * Get its parent Group, or null if its the root group.
	 * 
     * @return CDMA group object
	 */
	IContainer getParentGroup();

	/**
	 * Get the root group of the tree that holds the current Group.
	 * 
     * @return CDMA Group 
	 */
	IContainer getRootGroup();

	/**
	 * Get the "short" name, unique within its parent Group.
	 * 
	 * @return String object
	 */
	String getShortName();

	/**
	 * Check if the Group has an Attribute with certain name and value.
	 * 
     * @param name in String type
     * @param value in String type
     * @return boolean type 
	 */
	boolean hasAttribute(String name, String value);

	/**
	 * Remove an Attribute from the Attribute list.
	 * 
     * @param attribute CDMA Attribute
     * @return boolean type 
	 */
	boolean removeAttribute(IAttribute attribute);

	/**
	 * Set the IObject's (long) name.
	 * 
     * @param name String object
	 */
	void setName(String name);

	/**
	 * Set the IObject's (short) name.
	 * 
     * @param name in String type 
	 */
	void setShortName(String name);

	/**
	 * Set the parent group.
	 * 
     * @param group IGroup object
	 */
	void setParent(IGroup group);
	

    /**
     * Clone this IContainer.
     * 
     * @return new IDataItem instance
     */
    IContainer clone() throws CloneNotSupportedException;

}
