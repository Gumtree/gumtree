/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package org.gumtree.data.nexus;

import java.io.IOException;
import java.util.List;

import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;

/**
 * This class represents the data items used in the NeXus format.
 * 
 * @author nxi
 * 
 */
public interface INXDataItem extends IDataItem {

	/**
	 * Get the title as a string. Usually it is an attribute of the data item, with the name as 'title'
	 * @return String object
	 */
	String getTitle();
	
	/**
	 * Set the title as an attribute of the data item, with the name as 'title'
	 * @param title String object
	 */
	void setTitle(String title);

	
	/**
	 * Read the data with tolerance. Jump away from errors when loading the data.
	 * @param withTolerance
	 * @return array
	 * @throws IOException 
	 */
	IArray getData(boolean withTolerance) throws IOException;
	
	/**
	 * Set the isFlawed flag
	 * @param isFlawed
	 */
	void setFlawed(boolean isFlawed);
	
	/**
	 * Return if error has been detected when loading the data
	 * @return true or false
	 */
	boolean isFlawed();
	
	/**
	 * Return the list that contains the index of the flawed data
	 * @return List object
	 */
	List<Integer> getFlawedIndexList();
}
