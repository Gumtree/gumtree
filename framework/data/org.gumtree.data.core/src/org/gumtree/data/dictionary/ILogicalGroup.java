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

import java.util.List;

import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IKey;
import org.gumtree.data.utils.Utilities.ModelType;

/**
 * @brief The ILogicalGroup interface is a purely <b>virtual</b> object that regroup several data.
 * 
 * <p>
 * Its existence is correlated to the IExtendedDictionary. A standard CDMA dictionary make 
 * a link between a key and a path. Now let's imagine a dictionary with keys having a tree
 * structure. This structure hierarchically organized might now have a meaning regardless
 * their physical organization. So the keys are now simple notions that can have a human
 * friendly meaning.
 * <p>
 * The ILogicalGroup permits to browse simply through those different levels
 * of key. More over the key used can be filtered according to some criteria.
 * The aim is to find a really specific node by doing a search that get narrower
 * while iterating over queries.
 * 
 * @author rodriguez
 */

public interface ILogicalGroup extends IContainer {
    /**
     * Find the IDataItem corresponding to the given key in the dictionary.
     *  
     * @param key entry of the dictionary (can carry filters)
     * @return the first encountered IDataItem that match the key, else null
     */
    IDataItem getDataItem(IKey key);
    
    /**
     * Find the IDataItem corresponding to the given key in the dictionary.
     *  
     * @param keyPath separated entries of the dictionary (can't carry filters) 
     * @return the first encountered IDataItem that match the key, else null
     * @note keyPath can contain several keys concatenated with a plug-in's separator
     */
    IDataItem getDataItem(String keyPath);
    
    /**
     * Find all IDataItems corresponding to the given key in the dictionary.
     *  
     * @param key entry of the dictionary (can carry filters)
     * @return a list of IDataItem that match the key
     */
    List<IDataItem> getDataItemList(IKey key);

    /**
     * Find all IDataItems corresponding to the given path of key in the dictionary.
     *  
     * @param keyPath separated entries of the dictionary (can't carry filters)
     * @return a list of IDataItem that match the key
     * @note keyPath can contain several keys concatenated with a plug-in's separator
     */
    List<IDataItem> getDataItemList(String keyPath);
    
    /**
     * Find the Group corresponding to the given key in the dictionary.
     *  
     * @param key entry name of the dictionary
     * @return the first encountered ILogicalGroup that matches the key, else null
     */
    ILogicalGroup getGroup(IKey key);
    
    
    /**
     * Find the Group corresponding to the given key in the dictionary.
     *  
     * @param keyPath separated entries of the dictionary (can't carry filters)
     * @return the first encountered ILogicalGroup that matches the key, else null
     * @note keyPath can contain several keys concatenated with a plug-in's separator
     */
    ILogicalGroup getGroup(String keyPath);
    
    /**
     * Get the IDataset that hold the current Group.
     * 
     * @return CDMA IDataset 
     */
    IDataset getDataset();
    
    /**
     * Return the list of key that match the given model type.
     * 
     * @param model which kind of keys (ie: IDataItem, Group, ILogical, Attribute...)
     * @return List of type Group; may be empty, not null.
     */
    List<String> getKeyNames(ModelType model);
    
    /**
     * Bind the given key with the given name, so the key can be accessed by the bind
     * 
     * @param bind value with which we can get the key
     * @param key key object to be mapped by the bind value 
     * @return the given key
     */
    IKey bindKey(String bind, IKey key);
    
	/**
	 * Get the dictionary belonging to this ILogicalGroup.
	 * 
     * @return IDictionary the dictionary currently applied to this group
	 */
	IExtendedDictionary getDictionary();
    
	/**
	 * Set a dictionary to this ILogicalGroup.
	 * 
     * @param dictionary the dictionary to set
	 */
	void setDictionary(IDictionary dictionary);
	
	/**
	 * Return the list of parameters we can set on the given key that will
	 * have an occurrence in the dataset.
     * 
     * @param key IKey for which we want the parameters values
     * @return List<IPathParameter> that can be directly applied on the key
	 * @note <b>EXPERIMENTAL METHOD</b> do note use/implements
	 * @note if the path that matches the key hold several different parameters 
	 * the method will return the FIRST undefined parameter. To know deeper parameters,
	 * user has to set some IPathParameter on the key and call again this method
	 */
	List<IPathParameter> getParameterValues(IKey key);

	/**
	 * Set the given logical group as parent of this logical group  
     * 
	 * @param group ILogicalGroup
	 */
	void setParent(ILogicalGroup group);
	
	/**
	 * This method defines the way the IExtendedDictionary will be loaded.
	 * It must manage the do the detection and loading of the key file, 
	 * and the corresponding mapping file that belongs to the plug-in.
	 * Once the dictionary has its paths targeting both key and mapping
	 * files set, the detection work is done. It just remains the loading 
	 * of those files using the IExtendedDictionary. 
     * 
	 * @return IExtendeddictionary instance, that has already loaded keys and paths
	 * @note IExtendedDictionary.readEntries() is already implemented in the core 
	 */
	public IExtendedDictionary findAndReadDictionary();
}
