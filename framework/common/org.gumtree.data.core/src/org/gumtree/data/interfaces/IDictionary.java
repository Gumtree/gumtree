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

import java.net.URI;
import java.util.List;

import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.exception.FileAccessException;

/**
 * @brief The IDictionary interface defines a direct association of a keyword and a path.
 * 
 * A dictionary interface for used in CDMA Standard Dictionary mechanism.
 * The IGroup model references a path with a key in String type and should list
 * all available node in the IDataset.
 * The targeted object in the path can be either a IGroup or a IDataItem.
 * 
 * @author nxi
 * 
 */
public interface IDictionary extends IModelObject, Cloneable {

	/**
	 * Return all keys referenced in the dictionary.
	 * 
	 * @return a list of String objects
	 */
	List<IKey> getAllKeys();
	
	/**
	 * Get the path referenced by the key. If there are more than one paths are
	 * referenced by the path, get the default one.
	 * 
     * @param key key object
	 * @return String object
	 */
	IPath getPath(IKey key);

	/**
	 * Return all paths referenced by the key.
	 * 
     * @param key key object
	 * @return a list of String objects
	 */
	List<IPath> getAllPaths(IKey key);

	/**
	 * Add an entry of key and path.
	 * 
     * @param key key object
     * @param path String object
	 */
	void addEntry(String key, String path);
	
	void addEntry(String key, IPath path);

	/**
	 * Read dictionary entries from a file.
	 * 
     * @param uri URI object
	 * @throws FileAccessException
	 *             I/O file access exception
	 */
	void readEntries(URI uri) throws FileAccessException;

	/**
	 * Read dictionary entries from a file.
	 * 
     * @param path String object
	 * @throws FileAccessException
	 *             I/O file access exception
	 */
	void readEntries(String path) throws FileAccessException;

	/**
	 * Remove a path from an entry. If there is only one path associated with
	 * the key, then remove the entry as well.
	 * 
     * @param key key object
     * @param path String object
	 */
	void removeEntry(String key, String path);

	/**
	 * Remove an entry from the dictionary.
	 * 
     * @param key key object
	 */
	void removeEntry(String key);

	/**
     * @param key key object
	 * @return true or false
	 */
	boolean containsKey(String key);

	/**
	 * Clone the dictionary in a new object.
	 * 
	 * @return new Dictionary object
	 * @throws CloneNotSupportedException
	 *             failed to clone
	 */
	IDictionary clone() throws CloneNotSupportedException;

}
