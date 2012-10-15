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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.exception.NoResultException;
import org.gumtree.data.exception.SignalNotAvailableException;

/**
 * @brief The IGroup interface in a IDataset forms a hierarchical tree, like directories on a disk.
 * 
 * The IGroup has a name, contains one or mode IDataItem and one or more IGroup.
 * This can have optionally a set of IAttribute objects containing its metadata.
 * <br>
 * It used to browse the dataset following its physical structure.
 * 
 * @author nxi
 * 
 */
public interface IGroup extends IContainer {

	/**
	 * Add a data item to the group.
	 * 
     * @param item IDataItem object
	 */
    void addDataItem(IDataItem item);

	/**
     * 
     */
    Map<String, String> harvestMetadata(final String mdStandard) throws IOException;

	/**
     * Get its parent Group, or null if this is the root group.
	 * 
     * @return CDMA group object
	 */
	@Override
	IGroup getParentGroup();

	/**
     * Get the root group of the IDataset that holds the current Group.
	 * 
     * @return CDMA Group 
	 */
	@Override
	IGroup getRootGroup();

	/**
     * Add a shared IDimension.
	 * 
     * @param dimension CDMA IDimension object
	 */
	void addOneDimension(IDimension dimension);

	/**
	 * Add a nested Group.
	 * 
     * @param group CDMA IGroup object
	 */
	void addSubgroup(IGroup group);

	/**
     * Get the IDataItem with the specified (short) name in this group.
	 * 
     * @param shortName short name of IDataItem within this group.
     * @return the IDataItem, or null if not found
	 */
	IDataItem getDataItem(String shortName);

	/**
     * Find the IDataItem corresponding to the given key in the dictionary.
	 * 
     * @param key entry name of the dictionary
	 * 
     * @return the first encountered IDataItem that match the key
	 */
	IDataItem findDataItem(IKey key);

	/**
     * Find the IDataItem that has the specific attribute, with given name and
     * value.
	 * 
     * @param name in String type
     * @param value in String type
     * @return IDataItem object 
	 */
	IDataItem getDataItemWithAttribute(String name, String value);

	/**
     * Find the IDataItem corresponding to the given key in the dictionary. The
     * data item will be filtered by the attribute's name and value it must have.
	 * 
     * @param key key to look for in the dictionary
     * @param name name of the attribute the key should have
     * @param value the attribute value
     * @return IDataItem object 
	 */
    IDataItem findDataItemWithAttribute(IKey key, String name, String attribute) throws NoResultException;

	/**
	 * Find the Group corresponding to the given key in the dictionary. The
     * group will be filtered by the attribute's name and value it must have.
	 * 
     * @param key key to look for in the dictionary
     * @param name name of the attribute the group must have
     * @param value the attribute value
	 */
	IGroup findGroupWithAttribute(IKey key, String name, String value);

	/**
     * Retrieve a IDimension using its (short) name. If it does not exist in this
	 * group, recursively look in parent groups.
	 * 
     * @param name dimension's name.
     * @return the dimension, or null if not found
	 */
	IDimension getDimension(String name);

	/**
     * Retrieve the IContainer that has the given short name. The object can be
	 * either a group or a data item.
	 * 
     * @param shortName as String object
     * @return CDMA group or data item
	 */
	IContainer getContainer(String shortName);

	/**
     * Get the IGroup with the specified (short) name as a sub-group of the
	 * current group.
	 * 
     * @param shortName short name of the nested group you are looking for.
     * @return the IGroup, or null if not found
	 */
	IGroup getGroup(String shortName);

	/**
     * Get the IGroup that has the specific attribute, with the name and
	 * value given.
	 * 
     * @param attributeName String object
     * @param value in String type
     * @return Group object 
	 */
	IGroup getGroupWithAttribute(String attributeName, String value);

	/**
     * Get the IDataItem by searching the path in the dictionary with the given
     * name. The target IDataItem is not necessary to be under the current data
	 * item. If there are more than one paths associated with the same key word,
	 * use the order of their appearance in the dictionary to find the first not
	 * null object. If there is an entry wildcard, it will return the data item
	 * in the current entry.
	 * 
     * @param shortName in String type
     * @return CDMA IDataItem 
	 */
	IDataItem findDataItem(String shortName);

	/**
     * Get a list of IDataItem contained directly in this group.
	 * 
     * @return List of type IDataItem; may be empty, not null.
	 */
	List<IDataItem> getDataItemList();

	/**
     * Get the Dataset that holds the current Group.
	 * 
     * @return CDMA IDataset 
	 */
	IDataset getDataset();

	/**
     * Get the dimensions contained directly in this group.
	 * 
     * @return List of IDimension; may be empty, not null.
	 */
	List<IDimension> getDimensionList();

	/**
     * Get the IGroup by searching the path in the dictionary with the given
	 * name. The target Group is not necessary to be under the current Group. If
	 * there are more than one paths associated with the key word, find the
	 * first not null group in these paths.
	 * 
     * @param shortName in String type
     * @return CDMA Group 
	 */
	IGroup findGroup(String shortName);

	/**
     * Find the IGroup corresponding to the given key in the dictionary.
	 * 
     * @param key entry name of the dictionary
	 */
	IGroup findGroup(IKey key);

	/**
     * Get a list of IGroup contained directly in this Group.
	 * 
     * @return List of IGroup; may be empty, not null.
	 */
	List<IGroup> getGroupList();

	/**
     * Find the IContainer by searching the path in the dictionary with the given
     * name. The targeted IContainer is not necessary to be under the current Group.
     * It can be a IGroup or IDataItem. If there are more than one
	 * paths associated with the same key word, use the order of their
	 * appearance in the dictionary to find the first not null object. If there
	 * is an entry wildcard, it will return the object in the current entry.
	 * 
     * @param shortName in String type
     * @return IContainer 
	 */
	IContainer findContainer(String shortName);

	/**
     * Get the IContainer by searching the path in the root group. The target object
     * is not necessary to be under the current IGroup. The Object can be a IGroup
     * or a IDataItem.
	 * 
     * @param path full path of the object in String type
     * @return IContainer
	 */
	IContainer findContainerByPath(String path) throws NoResultException;

	/**
     * Get all IContainer by searching the path from the root group. Targeted
     * objects are not necessary to be directly under the current IGroup. Objects
     * can be a IGroups, IDataItems.
	 * 
     * @param path full path of objects in String type
     * @return a list of CDMA IContainer 
	 */
	List<IContainer> findAllContainerByPath(String path) throws NoResultException;

	/**
     * Remove a IDataItem from this IGroup children list.
	 * 
     * @param item IDataItem to be removed
     * @return boolean true if item has been removed
	 */
	boolean removeDataItem(IDataItem item);

	/**
     * Remove a IDataItem using its (short) name, in this group only.
	 * 
     * @param varName Variable name.
     * @return true if IDataItem has been found and removed
	 */
	boolean removeDataItem(String varName);

	/**
     * Remove a IDimension using its name, in this group only.
	 * 
     * @param name Dimension name
     * @return true if dimension has been found and removed
	 */
    boolean removeDimension(String name);

	/**
     * Remove a IGroup from the sub Group list.
	 * 
     * @param group CDMA IGroup
     * @return boolean true if IGroup has been found and removed 
	 */
	boolean removeGroup(IGroup group);

	/**
     * Remove the IGroup with a certain name in the children list.
	 * 
     * @param name in String type
     * @return boolean true if IGroup has been found and removed  
	 */
    boolean removeGroup(String name);

	/**
     * Remove a IDimension from the IDimension list.
	 * 
     * @param dimension CDMA IDimension
     * @return boolean true if IDimension has been found and removed CDMA
	 */
	boolean removeDimension(IDimension dimension);

	/**
	 * Update the data item in the location labeled by the key with a new data
	 * item. If the previous data item labeled by the key doesn't exist, it
	 * will put the data item in the location. This will also update the parent
	 * reference of the data item to the new one. If the key can not be found in
	 * the dictionary, or the parent path referred by the dictionary doesn't
	 * exist, raise an exception.
	 * 
     * @param key in String type
     * @param dataItem IDataItem object 
	 * @throws SignalNotAvailableException
	 *             no signal exception
	 */
	void updateDataItem(String key, IDataItem dataItem)
			throws SignalNotAvailableException;

	/**
	 * Set a dictionary to the root group.
	 * 
     * @param dictionary the dictionary to set
	 */
	void setDictionary(IDictionary dictionary);

	/**
	 * Get the dictionary from the root group.
	 * 
	 * @return IDictionary object
	 */
	IDictionary findDictionary();

	/**
	 * Check if this is the root group.
	 * 
	 * @return true or false
	 */
	boolean isRoot();

	/**
	 * Check if this is an entry group. Entries are immediate sub-group of the
	 * root group.
	 * 
	 * @return true or false
	 */
	boolean isEntry();

	/**
     * The CDMA dictionary allows multiple occurrences of a single key. This
     * method finds all the objects referenced by the given key string. If there
	 * is an entry wildcard, it will return the objects in the current entry.
	 * 
     * @param key Key object
     * @return a list of CDMA IContainer
	 */
	List<IContainer> findAllContainers(IKey key) throws NoResultException;

	/**
	 * Find all the occurrences of objects referenced by the first path for the
	 * given key in the dictionary. Those occurrences are from the available
	 * entries of the root group.
	 * 
     * @param key Key object
     * @return a list of CDMA IContainer
	 * @throws NoResultException 
	 */
	List<IContainer> findAllOccurrences(IKey key) throws NoResultException;

	/**
	 * Find the first occurrences of objects referenced by the given path. Those
	 * occurrences are from the available entries of the root group.
	 * 
     * @param path Path object
     * @return IContainer
	 */
	IContainer findObjectByPath(IPath path);
	
	/**
     * Returns a clone of this IGroup object. The tree structure is new. However
	 * the data items are shallow copies that share the same storages with the
	 * original ones.
	 * 
     * @return new IGroup CDMA group object 
	 */
	@Override
	IGroup clone();

}
