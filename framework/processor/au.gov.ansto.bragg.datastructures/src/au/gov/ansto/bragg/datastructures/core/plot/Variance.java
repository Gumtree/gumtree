/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.datastructures.core.plot;

import org.gumtree.data.interfaces.IDataItem;

import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;

/**
 * @author nxi
 * Created on 05/03/2008
 */
public interface Variance extends IDataItem {

	/**
	 * Reduce the rank of the variance by remove the dimensions that have a size of 1.
	 * @throws PlotFactoryException
	 * Created on 17/12/2008
	 */
	public void reduce() throws PlotFactoryException;

	/**
	 * Reduce the rank of the variance to at least a value by remove enough number of dimensions
	 * that have a size of 1. The reduction sequence start evaluating from the very first dimension.
	 * @param rank integer
	 * @throws PlotFactoryException
	 * Created on 17/12/2008
	 */
	public void reduceTo(int rank) throws PlotFactoryException;
}
