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

import org.gumtree.data.dictionary.ILogicalGroup;
import org.gumtree.data.exception.WriterException;

/**
 * @brief The IDataset interface is used to handle a data source.
 * 
 * A IDataset is a physical storage of CDMA objects, it holds a reference
 * of a root group, which is the root of a tree of IGroup and IDataItem. It is
 * the entry point to access the data structure it represents.<br>
 * For example in case of a data file container, the IDataset should refer to
 * the file handle.
 * 
 * @author nxi
 */
public interface IDataset extends IModelObject {

	/**
	 * Close the dataset.
	 * 
	 * @throws IOException
	 */
	void close() throws IOException;

	/**
	 * Return the root group of the dataset.
	 * 
     * @return IGroup that is on top of the structure 
	 */
	IGroup getRootGroup();
	
	/**
     * Return the logical root of the dataset.
	 * 
     * @return ILogicalGroup that is on top of the logical structure 
	 */
	ILogicalGroup getLogicalRoot();

	/**
     * Return the location of the dataset. If it's a file it will return the path.
	 * 
     * @return String type 
	 */
	String getLocation();

	/**
	 * Return the title of the dataset.
	 * 
     * @return string title  
	 */
	String getTitle();

	/**
     * Set the location of the dataset.
	 * 
     * @param location in String type 
	 */
	void setLocation(String location);

	/**
	 * Set the title for the Dataset.
	 * 
     * @param title a String object 
	 */
	void setTitle(String title);

	/**
	 * Synchronize the dataset with the file reference.
	 * 
	 * @return true or false
	 * @throws IOException
	 */
	boolean sync() throws IOException;

	/**
     * Open the dataset. If it is a file should open the file,
     * if a database enable connection, etc.
	 * 
	 * @throws IOException
	 */
	void open() throws IOException;

	/**
	 * Save the contents / changes of the dataset to the file.
	 * 
     * @throws WriterException
     *             failed to write 
	 */
    void save() throws WriterException;

	/**
     * Save the contents of the dataset to a new location.
	 * 
     * @throws WriterException
     *             failed to write 
	 */
    void saveTo(String location) throws WriterException;

	/**
     * Save the specific contents / changes of the dataset.
	 * 
     * @throws WriterException failed to write 
	 */
    void save(IContainer container) throws WriterException;
	
	/**
	 * Save the attribute to the specific path of the file.
	 * 
     * @throws WriterException failed to write 
	 */	
    void save(String parentPath, IAttribute attribute) throws WriterException;

	/**
	 * Write the file with NcML format.
	 * 
     * @param os java i/o OutputStream
     * @param uri a path to the file
	 * @throws java.io.IOException
     * @deprecated [SOLEIL][clement][2012-04-18] seems to be a plug-in dependent method 
	 */
    //[SOLEIL][clement][2012-04-18] seems to be a plug-in dependent method maybe I'm wrong. I think we should remove it from the Core or rename it like write(...)
    void writeNcML(java.io.OutputStream os, java.lang.String uri) throws java.io.IOException;

	/**
	 * Check if the data set is open.
	 * 
	 * @return true or false
	 */
	boolean isOpen();
}
