/*******************************************************************************
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package org.gumtree.data.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

import org.gumtree.data.IDatasource;
import org.gumtree.data.IFactory;
import org.gumtree.data.dictionary.ILogicalGroup;
import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.dictionary.IPathParamResolver;
import org.gumtree.data.dictionary.IPathParameter;
import org.gumtree.data.dictionary.impl.Key;
import org.gumtree.data.dictionary.impl.Path;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcArray;
import org.gumtree.data.impl.netcdf.NcAttribute;
import org.gumtree.data.impl.netcdf.NcDataItem;
import org.gumtree.data.impl.netcdf.NcDataset;
import org.gumtree.data.impl.netcdf.NcDictionary;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.impl.netcdf.NcIndex;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.interfaces.IKey;
import org.gumtree.data.utils.Utilities;
import org.gumtree.data.utils.Utilities.ParameterType;

/**
 * The Factory class in gumtree data model is a tools to create GDM objects. The
 * factory can take a URI as a parameter to read data in as GDM object, or it
 * can create an empty GDM object to hold data in a future time. <br>
 * Abbreviation: Gumtree data model -- GDM
 * 
 * @author nxi
 * @version 1.1
 */
public class NcFactory implements IFactory {

	public static final String NAME = "ncFactory";
	
	public static final String LABEL = "NetCDF implemenetation of CDMA";
	
	private NcDatasource detector;
	
	/**
	 * Hide default constructor.
	 */
	public NcFactory() {
		detector = new NcDatasource();
	}

	/**
	 * Retrieve the dataset referenced by the URI.
	 * 
	 * @param uri
	 *            URI object
	 * @return GDM Dataset
	 * @throws FileAccessException
	 *             Created on 18/06/2008
	 */
	public IDataset openDataset(final URI uri)
			throws FileAccessException {
		Object rootGroup = Utilities.findObject(uri, null, this);
		if (rootGroup != null) {
			return ((IGroup) rootGroup).getDataset();
		} else {
			return null;
		}
	}

	// public Group getGroup(URI uri){
	// return null;
	// }
	//
	// public DataItem getVariable(URI uri){
	// return null;
	// }
	//
	// public Array getArray(URI uri){
	// return null;
	// }
	//
	// public Group newGroup(URI uri){
	// return null;
	// }
	//
	// public DataItem newVariable(URI uri){
	// return null;
	// }
	//
	// public Array newArray(URI uri){
	// return null;
	// }

	/**
	 * Create an index of Array by given a shape of the Array.
	 * 
	 * @param shape
	 *            java array of integer
	 * @return GDM Array Index
	 * @deprecated it is recommended to use Array.getIndex() instead.
	 * @see IArray#getIndex()
	 */
	public IIndex createIndex(final int[] shape) {
		return new NcIndex(shape);
	}

	/**
	 * Create an empty Array with a certain data type and certain shape.
	 * 
	 * @param clazz
	 *            Class type
	 * @param shape
	 *            java array of integer
	 * @return GDM Array Created on 18/06/2008
	 */
	public IArray createArray(final Class<?> clazz, final int[] shape) {
		NcArray array = new NcArray(ucar.ma2.Array.factory(clazz, shape));
		return array;
	}

	// public static Array createStringArray(String value){
	// NcArray array = new NcArray(ucar.ma2.Array.factory(Character.TYPE, ));
	// }

	/**
	 * Create an Array with a given data type, shape and data storage.
	 * 
	 * @param clazz
	 *            in Class type
	 * @param shape
	 *            java array of integer
	 * @param storage
	 *            a 1D java array in the type reference by clazz
	 * @return GDM Array Created on 18/06/2008
	 */
	public IArray createArray(final Class<?> clazz, final int[] shape,
			final Object storage) {
		NcArray array = new NcArray(ucar.ma2.Array.factory(clazz, shape,
				storage));
		return array;
	}

	/**
	 * Create an Array from a java array. A new 1D java array storage will be
	 * created. The new GDM Array will be in the same type and same shape as the
	 * java array. The storage of the new array will be a COPY of the supplied
	 * java array.
	 * 
	 * @param javaArray
	 *            one to many dimensional java array
	 * @return GDM Array Created on 18/06/2008
	 */
	public IArray createArray(final Object javaArray) {
		NcArray array = new NcArray(ucar.ma2.Array.factory(javaArray));
		return array;
	}

	/**
	 * Create an Array of String storage. The rank of the new Array will be 2
	 * because it treat the Array as 2D char array.
	 * 
	 * @param string
	 *            String value
	 * @return new Array object
	 */
	public IArray createStringArray(final String string) {
		return createArray(String.class, new int[] { 1 }, new String[] { string
				.toString() });
	}

	/**
	 * Create a double type Array with a given single dimensional java double
	 * storage. The rank of the generated Array object will be 1.
	 * 
	 * @param javaArray
	 *            java double array in one dimension
	 * @return new Array object Created on 10/11/2008
	 */
	public IArray createDoubleArray(final double[] javaArray) {
		return createArrayNoCopy(javaArray);
	}

	/**
	 * Create a double type Array with a given java double storage and shape.
	 * 
	 * @param javaArray
	 *            java double array in one dimension
	 * @param shape
	 *            java integer array
	 * @return new Array object Created on 10/11/2008
	 */
	public IArray createDoubleArray(final double[] javaArray,
			final int[] shape) {
		return createArray(Double.TYPE, shape, javaArray);
	}

	/**
	 * Create an IArray from a java array. A new 1D java array storage will be
	 * created. The new GDM Array will be in the same type and same shape as the
	 * java array. The storage of the new array will be the supplied java array.
	 * 
	 * @param javaArray
	 *            java primary array
	 * @return GDM array Created on 28/10/2008
	 */
	public IArray createArrayNoCopy(final Object javaArray) {
		int rank = 0;
		Class<?> componentType = javaArray.getClass();
		while (componentType.isArray()) {
			rank++;
			componentType = componentType.getComponentType();
		}
		/*
		 * if( rank_ == 0) throw new
		 * IllegalArgumentException("Array.factory: not an array"); if(
		 * !componentType.isPrimitive()) throw new
		 * UnsupportedOperationException(
		 * "Array.factory: not a primitive array");
		 */

		// get the shape
		int count = 0;
		int[] shape = new int[rank];
		Object jArray = javaArray;
		Class<?> cType = jArray.getClass();
		while (cType.isArray()) {
			shape[count++] = java.lang.reflect.Array.getLength(jArray);
			jArray = java.lang.reflect.Array.get(jArray, 0);
			cType = jArray.getClass();
		}

		// create the Array
		return createArray(componentType, shape, javaArray);
	}

	/**
	 * Create a GDM DataItem with a given parent Group, Dataset, name and GDM
	 * Array data.
	 * 
	 * @param dataset
	 *            GDM Dataset
	 * @param parent
	 *            GDM Group
	 * @param shortName
	 *            in String type
	 * @param array
	 *            GDM Array
	 * @return GDM IDataItem
	 * @throws InvalidArrayTypeException
	 *             wrong type
	 * @deprecated use {@link #createDataItem(IGroup, String, IArray)} instead
	 * @since 18/06/2008
	 */
	public IDataItem createDataItem(final IDataset dataset,
			final IGroup parent, final String shortName, final IArray array)
			throws InvalidArrayTypeException {
		if (dataset == null) {
			return new NcDataItem((NcGroup) parent, shortName, (NcArray) array);
		}
		return new NcDataItem((NcDataset) dataset, (NcGroup) parent, shortName,
				(NcArray) array);
	}

	/**
	 * Create a DataItem with a given GDM parent Group, name and GDM Array data.
	 * If the parent Group is null, it will generate a temporary Group as the
	 * parent group.
	 * 
	 * @param parent
	 *            GDM Group
	 * @param shortName
	 *            in String type
	 * @param array
	 *            GDM Array
	 * @return GDM IDataItem
	 * @throws InvalidArrayTypeException
	 *             Created on 18/06/2008
	 */
	public IDataItem createDataItem(final IGroup parent,
			final String shortName, final IArray array)
			throws InvalidArrayTypeException {
		if (parent == null) {
			try {
				return new NcDataItem((NcGroup) createGroup("temp"), shortName,
						(NcArray) array);
			} catch (IOException e) {
				throw new InvalidArrayTypeException("IO exception");
			}
		}
		return new NcDataItem((NcGroup) parent, shortName, (NcArray) array);
	}

	/**
	 * Create a GDM Group with given Dataset, parent GDM Group, name. A boolean
	 * initiate parameter tells the factory if the new group will be put in the
	 * list of children of the parent Group.
	 * 
	 * @param dataset
	 *            GDM Dataset
	 * @param parent
	 *            GDM Group
	 * @param shortName
	 *            in String type
	 * @param init
	 *            boolean type
	 * @return GDM Group
	 * @deprecated use {@link #createGroup(IGroup, String, boolean)} instead
	 *             Created on 18/06/2008
	 */
	public IGroup createGroup(final IDataset dataset,
			final IGroup parent, final String shortName, final boolean init) {
		return new NcGroup((NcDataset) dataset, (NcGroup) parent, shortName,
				init);
	}

	/**
	 * Create a GDM Group with a given parent GDM Group, name, and a boolean
	 * initiate parameter telling the factory if the new group will be put in
	 * the list of children of the parent. Group.
	 * 
	 * @param parent
	 *            GDM Group
	 * @param shortName
	 *            in String type
	 * @param updateParent
	 *            if the parent will be updated
	 * @return GDM Group Created on 18/06/2008
	 */
	public IGroup createGroup(final IGroup parent,
			final String shortName, final boolean updateParent) {
		return new NcGroup((NcDataset) parent.getDataset(), (NcGroup) parent,
				shortName, updateParent);
	}

	/**
	 * Create an empty GDM Group with a given name. The factory will create an
	 * empty GDM Dataset first, and create the new Group under the root group of
	 * the Dataset.
	 * 
	 * @param shortName
	 *            in String type
	 * @return GDM Group
	 * @throws IOException
	 *             Created on 18/06/2008
	 */
	public IGroup createGroup(final String shortName)
	throws IOException {
		NcDataset dataset = (NcDataset) createEmptyDatasetInstance();
		return new NcGroup(dataset, dataset.getRootGroup(), shortName, true);
	}

	/**
	 * Create a GDM Attribute with given name and value.
	 * 
	 * @param name
	 *            in String type
	 * @param value
	 *            in String type
	 * @return GDM Attribute Created on 18/06/2008
	 */
	public IAttribute createAttribute(final String name,
			final Object value) {
		if (value == null) {
			return new NcAttribute(name);
		}
		if (value instanceof String) {
			return new NcAttribute(name, (String) value);
		}
		if (value instanceof NcArray) {
			return new NcAttribute(name, (NcArray) value);
		}
		if (value instanceof Boolean) {
			return new NcAttribute(name, (Boolean) value);
		}
		if (value instanceof Number) {
			return new NcAttribute(name, (Number) value);
		} else {
			return new NcAttribute(name, value.toString());
		}
	}


	/* (non-Javadoc)
	 * @see org.gumtree.data.IModelFactory#createDatasetInstance(java.net.URI)
	 */
	public IDataset createDatasetInstance(final URI uri) throws Exception {
		IDataset dataset = NcDataset.createDataset(new File(uri).getPath());
		dataset.open();
		return dataset;
	}

	/**
	 * Create a GDM Dataset in memory only. The dataset is not open yet. It is
	 * necessary to call dataset.open() to access the root of the dataset.
	 * 
	 * @return a GDM Dataset
	 * @throws IOException
	 *             I/O error Created on 18/06/2008
	 */
	public IDataset createEmptyDatasetInstance() throws IOException {
		IDataset dataset = NcDataset.createDataset();
		dataset.open();
		return dataset;
	}

	public String getName() {
		return NAME;
	}

	@Override
	public String getPluginLabel() {
		return LABEL;
	}

	/**
	 * Create a temporary Dataset. It will use a URI of the Temp folder of the
	 * platform as reference.
	 * 
	 * @return GDM Dataset
	 * @throws Exception
	 * @deprecated since V2.0 Use createDataset() instead.
	 */
	// public static IDataset createTempDataset() throws Exception {
	// if (randomGenerator == null) randomGenerator = new Random();
	// String tempdir = System.getProperty("java.io.tmpdir");
	// tempdir = tempdir.replace('\\', '/');
	// String filename = "temp" + randomGenerator.nextInt(100000) + ".hdf";
	// URI tempUri = null;
	// tempUri = URI.create("file:/" + tempdir + filename);
	// IDataset dataset = createDataset(tempUri);
	// return dataset;
	// }

	/**
	 * Import data from a IGor ASCII file, and create a GDM Group
	 * 
	 * @param uri
	 *            URI object, the location of the ASCII file
	 * @param dictionaryPath
	 *            in String type
	 * @param importHeaderFile
	 *            a file that hold the keywords for parsing the ASCII file
	 * @return GDM Group
	 * @throws Exception
	 * @Deprecated on 24/03/2009 Created on 18/06/2008
	 */
	// public static IGroup importIgorData(URI uri, String dictionaryPath,
	// String importHeaderFile) throws Exception{
	// igorImporter igorImporter = new IgorImporter();
	// IGroup rootGroup = igorImporter.importData(uri.getPath(),
	// importHeaderFile);
	// rootGroup.initialiseDictionary(dictionaryPath);
	// return rootGroup;
	// }

	// TODO [SOLEIL][clement]
	// Add those two methods to fit new mechanism of dictionary. 
	// TODO END
	
	@Override
	public IKey createKey(String keyName) {
		return new Key(this, keyName);
	}

	@Override
	public IPath createPath(String path) {
		return new Path(this, path);
	}

	@Override
	public ILogicalGroup createLogicalGroup(IDataset dataset, IKey key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPathParameter createPathParameter(ParameterType type, String name,
			Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPathParamResolver createPathParamResolver(IPath path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPathSeparator() {
		return "/";
	}

	@Override
	public IDictionary openDictionary(URI uri) throws FileAccessException {
		return openDictionary(uri.getPath());
	}
	
	@Override
	public IDictionary openDictionary(String filepath) throws FileAccessException {
		if (filepath == null)
			throw new FileAccessException("null file exception");
		IDictionary dictionary = new NcDictionary();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(filepath)));
			while(br.ready()){
				String line = br.readLine().trim();
				if (!line.startsWith("#") && !line.isEmpty()) {
					String[] temp = line.split("=");
					if (0<(temp[0].length())) {
						dictionary.addEntry(temp[0].trim(), temp[1].trim());
					}

				}
			}
		} catch (Exception ex){
			ex.printStackTrace();
			throw new FileAccessException(ex);
		}
		return dictionary;
	}

	@Override
	public IDictionary createDictionary() {
		return new NcDictionary();
	}

	@Override
	public IDatasource getPluginURIDetector() {
		return detector;
	}
	
}
