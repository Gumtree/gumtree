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

package org.gumtree.data;

import java.io.IOException;
import java.net.URI;

import org.gumtree.data.dictionary.ILogicalGroup;
import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.dictionary.IPathParamResolver;
import org.gumtree.data.dictionary.IPathParameter;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IKey;
import org.gumtree.data.utils.Utilities.ParameterType;

/**
 * @brief The plug-in factory is the entry point of the CDMA plug-in.
 * 
 * The IFactory interface is implemented by each plug-in. It permits to instantiate the IDataset and 
 * all the CDMA plug-in's object that will be used during the process.
 */
public interface IFactory {

	/**
	 * Retrieve the dataset referenced by the URI.
	 * 
     * @param uri URI object
     * @return CDMA Dataset
	 * @throws FileAccessException
	 */
	public IDataset openDataset(final URI uri) throws FileAccessException;

    /**
     * Instantiate a IDictionary with the given URI. Its loading can be done later.
     * 
     * @param uri of the dictionary
     * @return a new instance of the dictionary
     * @throws FileAccessException
     */
	public IDictionary openDictionary(final URI uri) throws FileAccessException;
	
    /**
     * Instantiate a IDictionary with the given file path. Its loading can be done later.
     * 
     * @param path of the dictionary file
     * @return a new instance of the dictionary
     * @throws FileAccessException
     */
	public IDictionary openDictionary(final String filepath) throws FileAccessException;
	
	/**
     * Create an empty IArray with a certain data type and certain shape.
	 * 
     * @param clazz Class type
     * @param shape java array of integer
     * @return CDMA IArray 
	 */
	public IArray createArray(final Class<?> clazz, final int[] shape);

	/**
     * Create an IArray with a given data type, shape and data storage.
	 * 
     * @param clazz in Class type
     * @param shape java array of integer
     * @param storage a 1D java array in the type reference by clazz
     * @return CDMA IArray 
	 */
	public IArray createArray(final Class<?> clazz, final int[] shape,
			final Object storage);

	/**
     * Create an IArray from a java array. A new 1D java array storage will be
     * created. The new CDMA IArray will be in the same type and same shape as the
	 * java array. The storage of the new array will be a COPY of the supplied
	 * java array.
	 * 
     * @param javaArray one to many dimensional java array
     * @return CDMA IArray 
	 */
	public IArray createArray(final Object javaArray);

	/**
     * Create an IArray of String storage. The rank of the new IArray will be 2
     * because it treat the IArray as 2D char array.
	 * 
     * @param string String value
     * @return new IArray object
	 */
	public IArray createStringArray(final String string);

	/**
     * Create a double type IArray with a given single dimensional java double
     * storage. The rank of the generated IArray object will be 1.
	 * 
     * @param javaArray java double array in one dimension
     * @return new IArray object 
	 */
	public IArray createDoubleArray(final double[] javaArray);

	/**
     * Create a double type IArray with a given java double storage and shape.
	 * 
     * @param javaArray java double array in one dimension
     * @param shape java integer array
     * @return new IArray object 
	 */
	public IArray createDoubleArray(final double[] javaArray, final int[] shape);

	/**
	 * Create an IArray from a java array. A new 1D java array storage will be
     * created. The new CDMA IArray will be in the same type and same shape as the
	 * java array. The storage of the new array will be the supplied java array.
	 * 
     * @param javaArray java primary array
     * @return CDMA array 
	 */
	public IArray createArrayNoCopy(final Object javaArray);

	/**
     * Create a IDataItem with a given CDMA parent Group, name and CDMA IArray data.
	 * If the parent Group is null, it will generate a temporary Group as the
	 * parent group.
	 * 
     * @param parent CDMA Group
     * @param shortName in String type
     * @param array CDMA IArray
     * @return CDMA IDataItem
	 * @throws InvalidArrayTypeException
	 */
	public IDataItem createDataItem(final IGroup parent,
			final String shortName, final IArray array)
			throws InvalidArrayTypeException;

	/**
     * Create a CDMA Group with a given parent CDMA Group, name, and a boolean
	 * initiate parameter telling the factory if the new group will be put in
	 * the list of children of the parent. Group.
	 * 
     * @param parent CDMA Group
     * @param shortName in String type
     * @param updateParent if the parent will be updated
     * @return CDMA Group 
	 */
	public IGroup createGroup(final IGroup parent, final String shortName,
			final boolean updateParent);

	/**
     * Create an empty CDMA Group with a given name. The factory will create an
     * empty CDMA Dataset first, and create the new Group under the root group of
	 * the Dataset.
	 * 
     * @param shortName in String type
     * @return CDMA Group
	 * @throws IOException
	 */
	public IGroup createGroup(final String shortName) throws IOException;

	/**
     * Create an empty CDMA Logical Group with a given key. 
	 * 
     * @param dataset an IDataset that this group will belong to
     * @param key an IKey that this group will correspond to
     * @return CDMA Logical Group
	 * @throws IOException
	 */
	public ILogicalGroup createLogicalGroup(IDataset dataset, IKey key);
	
	/**
     * Create a CDMA Attribute with given name and value.
	 * 
     * @param name in String type
     * @param value in String type
     * @return CDMA Attribute 
	 */
	public IAttribute createAttribute(final String name, final Object value);

	/**
     * Create a CDMA Dataset with a URI reference. If the file exists, it will
	 * 
     * @param uri URI object
     * @return CDMA Dataset
	 * @throws Exception
	 */
	public IDataset createDatasetInstance(final URI uri) throws Exception;

	/**
     * Create a CDMA Dataset in memory only. The dataset is not open yet. It is
	 * necessary to call dataset.open() to access the root of the dataset.
	 * 
     * @return a CDMA Dataset
	 * @throws IOException
     *             I/O error 
	 */
	public IDataset createEmptyDatasetInstance() throws IOException;

    /**
     * Create a IKey having the given name.
     * 
     * @param name of the key  
     * @return a new IKey
     */
    public IKey createKey(String name);
	
    /**
     * Create a IPath having the given value.
     * 
     * @param path interpreted by the plug-in  
     * @return a new IPath
     */
	public IPath createPath( String path );
	
    /**
     * Create a IPathParameter that will permit selection of a specific branch while browsing.
     * 
     * @param type of parameter 
     * @param name of the parameter
     * @param value of the parameter
     * @return a new IPathParameter
     */
	public IPathParameter createPathParameter(ParameterType type, String name, Object value);
	
    /**
     * Create a new IPathParamResolver which is used to resolve a IPath.
     * 
     * @param path the resolver will use 
     * @return a new IPathParamResolver
     */
	public IPathParamResolver createPathParamResolver(IPath path);
	
	/**
	 * Return the symbol used by the plug-in to separate nodes in a string path
	 * @return 
	 * @note <b>EXPERIMENTAL METHOD</b> do note use/implements
	 */
	public String getPathSeparator();
	
	/**
	 * The factory has a unique name that identifies it.
	 * @return the factory's name
	 */
	public String getName();

	/**
	 * The plug-in has a label, which describe the institute it comes from
	 * and / or the data source it is supposed to read / write: a human friendly
	 * information of which plug-in is working.
	 * @return the plug-in's label
	 */
	public String getPluginLabel();

    /**
     * Returns the URI detector of the instantiated plug-in. 
     * @return IPluginURIDetector
     */
    public IDatasource getPluginURIDetector();

    /**
     * Create an empty CDMA IDictionary
     * 
     * @return a CDMA IDictionary
     */
	public IDictionary createDictionary();
}
