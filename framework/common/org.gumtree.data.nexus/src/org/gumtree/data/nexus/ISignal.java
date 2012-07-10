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
 * This class maps to NeXus signal objects.
 * 
 * @author nxi
 * 
 */
public interface ISignal extends INXDataItem {

	/**
	 * Find the axes of the NXdata as a list.
	 * 
	 * @return List of IAxis objects
	 */
	List<IAxis> findAxes();

}
