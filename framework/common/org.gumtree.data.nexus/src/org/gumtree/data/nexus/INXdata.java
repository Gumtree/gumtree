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
 * GDM representative of NXdata class.
 * 
 * @author nxi
 * 
 */
public interface INXdata extends INXGroup {

	/**
	 * Find the NeXus signal from the NeXus data group.
	 * 
	 * @return ISignal object
	 */
	ISignal getSignal();

	/**
	 * Find the NeXus axes from the data group as a list.
	 * 
	 * @return List of IAxis objects
	 */
	List<IAxis> getAxisList();
	
	/**
	 * Set NeXus signal to the group
	 * @param signal ISignal object
	 */
	void setSignal(ISignal signal);
	
	/**
	 * Set axes to the group
	 * @param axes List of IAxis objects
	 */
	void setAxes(List<IAxis> axes);
	
	/**
	 * Set axes to the group
	 * @param axes List of IAxis objects
	 */
	void setMultipleAxes(IAxis ... axes);
	
	/**
	 * Get variance of the NeXus data
	 * @return IVariance object
	 */
	IVariance getVariance();
	
	/**
	 * Set variance to the group
	 * @param variance IVariance object
	 */
	void setVariance(IVariance variance);
}
