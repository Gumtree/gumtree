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

import java.util.List;

/**
 * This class maps to NeXus NXroot class.
 * 
 * @author nxi
 * 
 */
public interface INXroot extends INXGroup {

	/**
	 * Find the first NXentry object under the root.
	 * 
	 * @return NXentry object
	 */
	INXentry getFirstEntry();

	/**
	 * Find all the NXentry objects under the entry as a list.
	 * 
	 * @return list of NXentry objects
	 */
	List<INXentry> getEntryList();

	/**
	 * Get the NeXus version of the file.
	 * 
	 * @return String value
	 */
	String getNexusVersion();

	/**
	 * Get the HDF version used to write the NeXus file.
	 * 
	 * @return String value
	 */
	String getHDFVersion();

	/**
	 * Get the time that this file is last updated.
	 * 
	 * @return String value
	 */
	String getFileTime();

	/**
	 * Get the default NXentry of the file.
	 * 
	 * @return NXentry object
	 */
	INXentry getDefaultEntry();

	/**
	 * Get the NXdata from the default NXentry.
	 * 
	 * @return NXdata object
	 */
	INXdata getDefaultData();

	/**
	 * Get the signal from the default NXentry.
	 * 
	 * @return ISignal object
	 */
	ISignal getDefaultSignal();

}
