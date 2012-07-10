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
 * This class maps to NeXus NXentry class.
 * 
 * @author nxi
 * 
 */
public interface INXentry extends INXGroup {

	/**
	 * Find the NXdata object under the entry.
	 * 
	 * @return NXdata object
	 */
	INXdata getData();

	/**
	 * Find the NXinstrument object under the entry.
	 * 
	 * @return NXinstrument object
	 */
	INXinstrument getInstrumentGroup();

	/**
	 * Find the signal under the entry.
	 * 
	 * @return ISingal object
	 */
	ISignal getSignal();

	/**
	 * Find the axes objects under the entry as a list.
	 * 
	 * @return List of IAxis objects
	 */
	List<IAxis> getAxes();

	/**
	 * Find the NXuser object under the entry.
	 * 
	 * @return NXuser object
	 */
	INXuser getUserGroup();

	/**
	 * Find the NXmonitor object under the entry.
	 * 
	 * @return NXmonitor object
	 */
	INXmonitor getMonitorGroup();

	/**
	 * Find the NXsample object under the entry.
	 * 
	 * @return NXsample object
	 */
	INXsample getSampleGroup();

	/**
	 * Find the NXnote object under the entry.
	 * 
	 * @return NXnote object
	 */
	INXnote getNoteGroup();
}
