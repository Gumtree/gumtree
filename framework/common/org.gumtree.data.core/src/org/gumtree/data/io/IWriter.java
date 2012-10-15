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

package org.gumtree.data.io;

import org.gumtree.data.exception.WriterException;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

/**
 * @brief The IWriter interface provides methods of outputting CDMA object.
 * 
 * The interface of CDMA writer provides methods of outputting to a storage that 
 * has a tree structure.
 * 
 * @author nxi
 * @version 2.0
 */
public interface IWriter {

	/**
	 * Open the storage, for example, the file handler. Any output action will
	 * require the storage to be open.
	 * 
     * @throws WriterException failed to open the storage
     * @deprecated use {@link IDataset#open()}
	 */
    void open() throws WriterException;

	/**
	 * Check if the storage is open for output.
	 * 
	 * @return true or false
	 */
	boolean isOpen();

	/**
     * Add a group to the root of the storage.
	 * This has the same performance as
	 * {@link #writeToRoot(IGroup group, boolean force)}, where force is set to
	 * be false.
	 * 
     * @param group CDMA group object
     * @throws WriterException failed to write the group
	 */
    void writeToRoot(IGroup group) throws WriterException;

	/**
     * Write a group to the root of the storage. When a group with a same name 
     * already exists under the root node, write the contents of the CDMA group
     * under the target group node. For conflict data item, check the force switch.
     * If it is set to be true, overwrite the contents under the group. 
     * Otherwise raise an exception.

     * @param group CDMA group object
     * @param force if allow overwriting
     * @throws WriterException failed to write to data source
     * @see {@link #writeGroup(String, IGroup, boolean)} for more information.
	 */
    void writeToRoot(IGroup group, boolean force) throws WriterException;

	/**
	 * Write a data item to the root of the storage. If a data node with the
	 * same name already exists, raise an exception.
	 * 
     * @param dataItem CDMA data item
     * @throws WriterException failed to write
	 */
    void writeToRoot(IDataItem dataItem) throws WriterException;

	/**
	 * Write a data item to the root of the storage. If force is true, overwrite
	 * the conflicting node.
	 * 
     * @param dataItem CDMA data item
     * @param force if allow overwriting
     * @throws WriterException failed to write
	 */
	void writeToRoot(IDataItem dataItem, boolean force)
            throws WriterException;

	/**
	 * Write an attribute to the root of the storage. If an attribute node
	 * already exists in the root of the storage, raise an exception.
	 * 
     * @param attribute CDMA attribute
     * @throws WriterException failed to write
	 */
    void writeToRoot(IAttribute attribute) throws WriterException;

	/**
	 * Write an attribute to the root of the storage. If an attribute node
	 * already exists, check the force switch. If it is true, overwrite the
	 * node. Otherwise raise an exception.
	 * 
     * @param attribute CDMA attribute
     * @param force if allow overwriting
     * @throws WriterException failed to write
	 */
    void writeToRoot(IAttribute attribute, boolean force) throws WriterException;

	/**
	 * Write a group under the node with a given X-path. If a group node with
	 * the same name already exists, this will not overwrite any existing
	 * contents under the node. When conflicting happens, raise an exception.
	 * 
     * @param parentPath x-path as a String object
     * @param group CDMA group
     * @throws WriterException failed to write
	 */
    void writeGroup(String parentPath, IGroup group) throws WriterException;

	/**
	 * Write a group under the node of a given X-path. If a group node with the
	 * same name already exists, check the 'force' switch. If it is true,
	 * overwrite any conflicting contents under the node. Otherwise raise an
	 * exception for conflicting.
	 * 
     * @param parentPath x-path as a String object
     * @param group CDMA group
     * @param force if allow overwriting
     * @throws WriterException failed to write
	 */
    void writeGroup(String parentPath, IGroup group, boolean force) throws WriterException;

	/**
	 * Write a data item under a group node with a given X-path. If a data item
	 * node already exists there, raise an exception.
	 * 
     * @param parentPath x-path as a String object
     * @param dataItem CDMA data item
     * @throws WriterException failed to write
	 */
    void writeDataItem(String parentPath, IDataItem dataItem) throws WriterException;

	/**
	 * Write a data item under a group node with a given X-path. If a data item
	 * node already exists there, check the 'force' switch. If it is true,
	 * overwrite the node. Otherwise raise an exception.
	 * 
     * @param parentPath String value
     * @param dataItem IDataItem object
     * @param force true or false
     * @throws WriterException failed to write
	 */
	void writeDataItem(String parentPath, IDataItem dataItem, boolean force)
            throws WriterException;

	/**
	 * Write an attribute under a node with a given X-path. The parent node can
	 * be either a group node or a data item node. If an attribute node with the
	 * same name already exists, raise an exception.
	 * 
     * @param parentPath x-path as a String object
     * @param attribute CDMA attribute
     * @throws WriterException failed to write
	 */
    void writeAttribute(String parentPath, IAttribute attribute) throws WriterException;

	/**
	 * Write an attribute to the node with a given X-path. The node can be
	 * either a group node or a data item node. If an attribute with an existing
	 * name already exists, raise an exception.
	 * 
     * @param parentPath x-path as a String object
     * @param attribute CDMA attribute
     * @param force if allowing overwriting
     * @throws WriterException failed to write
	 */
	void writeAttribute(String parentPath, IAttribute attribute, boolean force)
            throws WriterException;

	/**
	 * Write an empty group under a group node with a given X-path. If a group
	 * node with the same name already exists, check the 'force' switch. If it
	 * is true, remove all the contents of the group node.
	 * 
     * @param xPath as a String object
     * @param groupName short name as a String object
     * @param force if allow overwriting
     * @throws WriterException failed to write
	 */
	void writeEmptyGroup(String xPath, String groupName, boolean force)
            throws WriterException;

	/**
	 * Remove a group with a given X-path from the storage.
	 * 
     * @param groupPath as a String object
	 */
	void removeGroup(String groupPath);

	/**
	 * Remove a data item with a given X-path from the storage.
	 * 
     * @param dataItemPath as a String object
     * @throws WriterException failed to write
	 */
    void removeDataItem(String dataItemPath) throws WriterException;

	/**
	 * Remove an attribute with a given X-path from the storage.
	 * 
     * @param attributePath x-path as a String object
     * @throws WriterException failed to write
	 */
    void removeAttribute(String attributePath) throws WriterException;

	/**
	 * Check if a group exists in a given X-path.
	 * 
     * @param xPath as a String object
	 * @return true or false
	 */
	boolean isGroupExist(String xPath);

	/**
	 * Check if a group exists under certain group node with a given X-path.
	 * 
     * @param parentPath the X-path of the parent group
     * @param groupName the name of the target group
	 * @return true or false
	 */
	boolean isGroupExist(String parentPath, String groupName);

	/**
	 * Check if a data item exists with a given X-path.
	 * 
     * @param xPath as a String object
	 * @return true or false
	 */
	boolean isDataItemExist(String xPath);

	/**
	 * Check if a data item exists under a parent group with a given X-path.
	 * 
     * @param parentPath x-path of the parent group as a String object
     * @param dataItemName name of the target data item
	 * @return true or false
	 */
	boolean isDataItemExist(String parentPath, String dataItemName);

	/**
	 * Check if an attribute exist with a given xpath.
	 * 
     * @param xPath x-path as a String object
	 * @return true or false
	 */
	boolean isAttributeExist(String xPath);

	/**
	 * Check if the attribute with a given name already exists.
	 * 
     * @param parentPath String object
     * @param attributeName String object
	 * @return true or false
	 */
	boolean isAttributeExist(String parentPath, String attributeName);

	/**
	 * Close the file handler. Unlock the file.
     * @deprecated use {@link IDataset#close()}
	 */
	void close();
}