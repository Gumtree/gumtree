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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.gumtree.data.dictionary.ILogicalGroup;
import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.interfaces.IKey;
import org.gumtree.data.utils.FactoryManager;
import org.gumtree.data.utils.IFactoryManager;

/**
 * @brief The Core factory is the entry point of the CDMA API
 * 
 * The Factory class in common data model is a tools to create CDMA objects.. It manages all plug-ins 
 * instances.
 * <p>
 * According to an URI, it will detect which plug-in is relevant to that data source.
 * It can take an URI as a parameter to instantiate a plug-in in order to get an
 * access of the targeted data source using CDMA objects.
 * <p>
 * Abbreviation: Common Data Model Access -- CDMA
 * 
 * @author XIONG Norman
 * @contributor RODRIGUEZ Clément
 * @version 1.1
 */
public final class Factory {

	private static volatile IFactoryManager manager;
    private static String CDM_EXPERIMENT = "";
    private static final String DICO_PATH_PROP = "CDM_DICTIONARY_PATH";
	
    /**
     * Create a CDMA Dataset that can read the given URI.
     * 
     * @param uri URI object
     * @return CDMA Dataset
     * @throws Exception
     */
    public static IDataset openDataset(URI uri ) throws Exception {
        return openDataset(uri, false);
    }
    
	/**
     * Create a CDMA Dataset that can read the given URI and use optionally the Extended Dictionary
     * mechanism.
	 * 
     * @param uri URI object
     * @param useProducer only 
     * @return CDMA Dataset
     * @throws Exception
	 */
	public static IDataset openDataset(URI uri, boolean useDictionary) throws Exception {
        // [2012-05-02][ANSTO][TONY][TODO] Test this code in ANSTO
		IFactory factory = detectPlugin(uri);
        IDataset dataset = null;
        if( factory != null ) {
            IDatasource source = factory.getPluginURIDetector();
            if( ! useDictionary || source.isProducer( uri ) ) {
                dataset = factory.openDataset(uri);
            }
        }
        return dataset;
//		Object rootGroup = Utilities.findObject(uri, null);
//		if (rootGroup != null) {
//			return ((IGroup) rootGroup).getDataset();
//		} else {
//			return null;
//		}
	}
	

    /**
     * Set the name of the current view (e.q experiment) that will be active for
     * the Extended Dictionary mechanism.
     * 
     * @param experiment name
     */
	public static void setActiveView(String experiment) {
		CDM_EXPERIMENT = experiment;
	}
	
    /**
     * Returns the name of the current view (e.q experiment) that is active for
     * the Extended Dictionary mechanism.
     * 
     * @return experiment name
     */
	public static String getActiveView() {
		return CDM_EXPERIMENT;
	}
	
	/**
	 * According to the currently defined experiment, this method will return the path
	 * to reach the declarative dictionary. It means the file where
	 * is defined what should be found in a IDataset that fits the experiment.
	 * It's a descriptive file.
	 * 
	 * @return the path to the standard declarative file
	 */
	public static String getKeyDictionaryPath() {
		String sDict = getDictionariesFolder();
		String sFile = ( getActiveView() + "_view.xml" ).toLowerCase();
		
        return sDict + File.separator + sFile;
	}
	
	/**
	 * According to the given factory this method will return the path to reach
	 * the folder containing mapping dictionaries. This file associate entry 
	 * keys to paths that are plug-in dependent.
	 * 
	 * @param factory of the plug-in instance from which we want to load the dictionary
	 * @return the path to the plug-in's mapping dictionaries folder  
	 */
	public static String getMappingDictionaryFolder(IFactory factory) {
		String sDict = getDictionariesFolder();

        return sDict + File.separator + factory.getName() + File.separator;
	}
	
	/**
	 * Set the folder path where to search for key dictionary files.
	 * This folder should contains all dictionaries that the above application needs.
     * 
	 * @param path targeting a folder
	 */
	public static void setDictionariesFolder(String path) {
		System.setProperty(DICO_PATH_PROP, path);
	}
	
    /**
     * Get the folder path where to search for key dictionary files (e.q: view or experiment).
     * This folder should contains all dictionaries that the above application needs.
     * 
     * @return path targeting a folder
     */
	public static String getDictionariesFolder() {
		return System.getProperty(DICO_PATH_PROP, System.getenv(DICO_PATH_PROP));
	}

	/**
     * Create an index of IArray by given a shape of the IArray.
	 * 
     * @param shape java array of integer
     * @return CDMA IArray Index
     * @deprecated it is recommended to use Array#getIndex() instead.
	 * @see IArray#getIndex()
	 */
	public static IIndex createIndex(int[] shape) {
		throw new UnsupportedOperationException();
	}

	/**
     * Create an empty IArray with a certain data type and certain shape.
	 * 
     * @param clazz Class type
     * @param shape java array of integer
     * @return CDMA IArray 
     * @deprecated it is recommended to use {@link IFactory#createArray(Class, int[])}
	 */
	public static IArray createArray(Class<?> clazz, int[] shape) {
		return getFactory().createArray(clazz, shape);
	}

	/**
     * Create an IArray with a given data type, shape and data storage.
	 * 
     * @param clazz in Class type
     * @param shape java array of integer
     * @param storage a 1D java array in the type reference by clazz
     * @return CDMA IArray
     * @deprecated it is recommended to use {@link IFactory#createArray(Class, int[], Object)}
	 */
	public static IArray createArray(Class<?> clazz, int[] shape,
			final Object storage) {
		return getFactory().createArray(clazz, shape, storage);
	}

	/**
     * Create an IArray from a java array. A new 1D java array storage will be
     * created. The new CDMA IArray will be in the same type and same shape as the
	 * java array. The storage of the new array will be a COPY of the supplied
	 * java array.
	 * 
     * @param javaArray one to many dimensional java array
     * @return CDMA IArray 
     * @deprecated it is recommended to use {@link IFactory#createArray(Object)}
	 */
	public static IArray createArray(final Object javaArray) {
		return getFactory().createArray(javaArray);
	}

	/**
     * Create an IArray of String storage. The rank of the new IArray will be 2
     * because it treat the IArray as 2D char array.
	 * 
     * @param string String value
     * @return new IArray object
	 */
	public static IArray createStringArray(String string) {
		return createArray(String.class, new int[] { 1 }, new String[] { string
				.toString() });
	}

	/**
     * Create a double type IArray with a given single dimensional java double
     * storage. The rank of the generated IArray object will be 1.
	 * 
     * @param javaArray java double array in one dimension
     * @return new IArray object 
     * @deprecated it is recommended to use {@link IFactory#createDoubleArray(double[])}
	 */
	public static IArray createDoubleArray(double[] javaArray) {
		return createArrayNoCopy(javaArray);
	}

	/**
     * Create a double type IArray with a given java double storage and shape.
	 * 
     * @param javaArray java double array in one dimension
     * @param shape java integer array
     * @return new IArray object 
     * @deprecated it is recommended to use {@link IFactory#createDoubleArray(double[], int[])}
	 */
	public static IArray createDoubleArray(double[] javaArray,
			final int[] shape) {
		return createArray(Double.TYPE, shape, javaArray);
	}

	/**
	 * Create an IArray from a java array. A new 1D java array storage will be
     * created. The new CDMA IArray will be in the same type and same shape as the
	 * java array. The storage of the new array will be the supplied java array.
	 * 
     * @param javaArray java primary array
     * @return CDMA array 
     * @deprecated it is recommended to use {@link IFactory#createArrayNoCopy(Object)}
	 */
	public static IArray createArrayNoCopy(Object javaArray) {
		int rank = 0;
		Class<?> componentType = javaArray.getClass();
		while (componentType.isArray()) {
			rank++;
			componentType = componentType.getComponentType();
		}

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

        // create the IArray
		return createArray(componentType, shape, javaArray);
	}

	/**
     * Create a CDMA IDataItem with a given parent Group, Dataset, name and GDM
     * IArray data.
	 * 
     * @param dataset CDMA Dataset
     * @param parent CDMA Group
     * @param shortName in String type
     * @param array CDMA IArray
     * @return CDMA IDataItem
	 * @throws InvalidArrayTypeException
	 *             wrong type
	 * @deprecated use {@link #createDataItem(IGroup, String, IArray)} instead
	 * @since 18/06/2008
	 */
	public static IDataItem createDataItem(IDataset dataset,
			IGroup parent, String shortName, IArray array)
			throws InvalidArrayTypeException {
		throw new UnsupportedOperationException();
	}

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
     * @deprecated it is recommended to use {@link IFactory#createDataItem(IGroup, String, IArray)}
	 */
	public static IDataItem createDataItem(IGroup parent,
			String shortName, IArray array)
			throws InvalidArrayTypeException {
		return getFactory().createDataItem(parent, shortName, array);
	}

	/**
     * Create a CDMA Group with given Dataset, parent CDMA Group, name. A boolean
	 * initiate parameter tells the factory if the new group will be put in the
	 * list of children of the parent Group.
	 * 
     * @param dataset CDMA Dataset
     * @param parent CDMA Group
     * @param shortName in String type
     * @param init boolean type
     * @return CDMA Group
	 * @deprecated use {@link #createGroup(IGroup, String, boolean)} instead
	 */
	public static IGroup createGroup(IDataset dataset,
			IGroup parent, String shortName, boolean init) {
		throw new UnsupportedOperationException();
	}

	/**
     * Create a CDMA Group with a given parent CDMA Group, name, and a boolean
	 * initiate parameter telling the factory if the new group will be put in
	 * the list of children of the parent. Group.
	 * 
     * @param parent CDMA Group
     * @param shortName in String type
     * @param updateParent if the parent will be updated
     * @return CDMA Group
     * @deprecated it is recommended to use {@link IFactory#createGroup(IGroup, String, boolean)}
	 */
	public static IGroup createGroup(IGroup parent,
			String shortName, boolean updateParent) {
		return getFactory().createGroup(parent, shortName, updateParent);
	}

	/**
     * Create an empty CDMA Group with a given name. The factory will create an
     * empty CDMA Dataset first, and create the new Group under the root group of
	 * the Dataset.
	 * 
     * @param shortName in String type
     * @return CDMA Group
	 * @throws IOException
     * @deprecated it is recommended to use {@link IFactory#createGroup(String)}
	 */
	public static IGroup createGroup(String shortName)
	throws IOException {
		return getFactory().createGroup(shortName);
	}

	/**
     * Create an empty CDMA Logical Group with a given key. 
	 * 
     * @param parent an ILogicalGroup
     * @param key an IKey that this group will correspond
     * @return CDMA Logical Group
	 * @throws IOException
     * @deprecated it is recommended to use {@link IFactory#createLogicalGroup(IDataset, IKey)}
	 */
	public static ILogicalGroup createLogicalGroup(IDataset dataset, IKey key) {
		return getFactory().createLogicalGroup(dataset, key);
	}

	/**
     * Create a CDMA Attribute with given name and value.
	 * 
     * @param name in String type
     * @param value in String type
     * @return CDMA Attribute
     * @deprecated it is recommended to use {@link IFactory#createAttribute(String, Object)}
	 */
	public static IAttribute createAttribute(String name, Object value) {
		return getFactory().createAttribute(name, value);
	}

    /**
     * Create a CDMA Dataset that can read the given URI.
     * 
     * @param uri URI object
     * @return CDMA Dataset
     * @throws Exception
     * @deprecated use openDataset(URI)
     */
    public static IDataset createDatasetInstance(URI uri ) throws Exception {
        return createDatasetInstance(uri, false);
    }
    
	/**
     * Create a CDMA Dataset that can read the given URI and use optionally the Extended Dictionary
     * mechanism.
	 * 
     * @param uri URI object
     * @param useProducer only 
     * @return CDMA Dataset
	 * @throws Exception
     * @deprecated use openDataset(URI, boolean)
	 */
    public static IDataset createDatasetInstance(URI uri, boolean useDictionary) throws Exception {
    	// [2012-05-02][ANSTO][TONY][TODO] Test this code in ANSTO
        IFactory factory = detectPlugin(uri);
        IDataset dataset = null;
        if( factory != null ) {
            IDatasource source = factory.getPluginURIDetector();
            if( ! useDictionary || source.isProducer( uri ) ) {
                dataset = factory.createDatasetInstance(uri);
            }
        }
        return dataset;
//		return getFactory().createDatasetInstance(uri);
	}

	/**
     * Create a CDMA Dataset in memory only. The dataset is not open yet. It is
	 * necessary to call dataset.open() to access the root of the dataset.
	 * 
     * @return a CDMA Dataset
	 * @throws IOException
     *             I/O error 
     * @deprecated it is recommended to use {@link IFactory#createEmptyDatasetInstance()}
	 */
	public static IDataset createEmptyDatasetInstance() throws IOException {
        return getFactory().createEmptyDatasetInstance();
	}
	
    /**
     * Create a key having the given name
     * @param keyName String
     * @return new created IKey
     * @deprecated it is recommended to use {@link IFactory#createKey(String)}
     */
	public static IKey createKey(String keyName) {
		return getFactory().createKey(keyName);
	}
	
    /**
     * Create a path having the given value
     * @param path String
     * @return new created IPath
     * @deprecated it is recommended to use {@link IFactory#createPath(String)}
     */
	public static IPath createPath(String path) {
		return getFactory().createPath(path);
	}
	
	/**
     * Create an empty CDMA IDictionary
	 * 
     * @return a CDMA IDictionary
     * @deprecated it is recommended to use {@link IFactory#createDictionary()}
	 */
	public static IDictionary createDictionary() {
		return getFactory().createDictionary();
	}
	
    /**
     * Return the singleton instance of the plug-ins factory manager
     * @return IFactoryManager unique instance
     */
	public static IFactoryManager getManager() {
		if (manager == null) {
			synchronized (Factory.class) {
				if (manager == null) {
					manager = new FactoryManager();
				}
			}
		}
		return manager;
	}
	
    /**
     * Return the IFactory of the first available plug-in that was loaded 
     * @return first loaded IFactory 
     */
	public static IFactory getFactory() {
		return getManager().getFactory();
	}
	
    /**
     * Return the plug-in's factory having the given name
     * @param name of the requested factory
     * @return IFactory instance
     */
	public static IFactory getFactory(String name) {
		return getManager().getFactory(name);
	}
	
	/**
     * Return a plug-in IFactory that is the most relevant for the given URI.
     * Try to detect factories according the following:
     * if a plug-in declares itself as the owner of the targeted data source returns its factory
     * else returns the first plug-in that is compatible with given data format
     * no plug-in is compatible returns null
     * 
     * @param uri of the data source
     * @return IFactory instance
     */
    public static IFactory getFactory(URI uri) {
        return detectPlugin(uri);
    }

    /**
     * Return a plug-in IFactory that is the most relevant for the given URI.
     * Try to detect factories according the following:
     * if a plug-in declares itself as the owner of the targeted data source returns its factory
     * else returns the first plug-in that is compatible with given data format
     * no plug-in is compatible returns null
     * 
     * @param uri of the data source
     * @return IFactory instance
     */
    public static IFactory detectPlugin(URI uri) {
        ArrayList<IFactory> reader = new ArrayList<IFactory>();
        IFactory result = null;
        IFactory plugin;
        IDatasource detector;
        IFactoryManager mngr = getManager();


        Map<String, IFactory> registry = mngr.getFactoryRegistry();

        for( Entry<String, IFactory> entry : registry.entrySet() ) {
            plugin   = entry.getValue();
            detector = plugin.getPluginURIDetector();

            if( detector.isReadable(uri) ) {
                reader.add( plugin );
                if( detector.isProducer(uri) ) {
                    result = plugin;
                    break;
                }
            }
        }

        if( result == null && reader.size() > 0 ) {
            result = reader.get(0);
        }

        return result;
    }

    /**
	 * Hide default constructor.
	 */
	private Factory() {
	}
	

}
