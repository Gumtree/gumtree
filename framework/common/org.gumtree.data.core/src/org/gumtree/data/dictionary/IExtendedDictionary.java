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


package org.gumtree.data.dictionary;

import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IKey;


/**
 * @brief IExtendedDictionary interface is the logical representation of a IDataset.
 * 
 * It defines how data is logically structured and permits a standardized browsing what
 * ever the plug-in, the data source format or its structure is.
 * <br/>
 * The dictionary is compound of two element a key file that defines the representation
 * of the dataset and a mapping file that associates 
 * Association of objects is the following:
 * <br/> - IKey and IPath for a IDataItem, 
 * <br/> - IKey and IExtendedDictionary for a ILogicalGroup.  
 */

public interface IExtendedDictionary extends IDictionary {
	/**
	 * Get a sub part of this dictionary that corresponds to a key.
	 * @param IKey object
	 * @return IExtendedDictionary matching the key
	 */
	public IExtendedDictionary getDictionary(IKey key);
	
	/**
	 * Get the version number (in 3 digits default implementation) that is plug-in
	 * dependent. This version corresponds of the dictionary defining the path. It  
	 * permits to distinguish various generation of IDataset for a same institutes.
	 * Moreover it's required to select the right class when using a IClassLoader
	 * invocation. 
     * 
     * @return the string representation of the plug-in's version number
	 */
	public String getVersionNum();
	
	/**
	 * Get the plug-in implementation of a IClassLoader so invocations of external
     * code are made possible.
     * 
     * @return the plug-in's class loader
	 */
	public IClassLoader getClassLoader();
	
	/**
	 * Get the view name matching this dictionary
     * 
     * @return the name of the experimental view
	 */
	public String getView();
	
	/**
	 * Read all keys stored in the XML dictionary file
	 */
	public void readEntries() throws FileAccessException;
	
	/**
	 * Return the path to reach the key dictionary file
     * 
     * @return the path of the dictionary key file
	 */
	public String getKeyFilePath();
	
	/**
	 * Return the path to reach the mapping dictionary file
     * 
     * @return the path of the plug-in's dictionary mapping file
	 */
	public String getMappingFilePath();
}
