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
package au.gov.ansto.bragg.datastructures.core.plot.internal;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcDataItem;
import org.gumtree.data.impl.netcdf.NcDataset;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.Variance;

/**
 * @author nxi
 * Created on 06/03/2008
 */
public class NcVariance extends NcDataItem implements Variance {

	/**
	 * @param group
	 * @param shortName
	 * @param array
	 * @throws InvalidArrayTypeException
	 */
	public NcVariance(IGroup group, String shortName,
			IArray array, IDataItem data) throws InvalidArrayTypeException {
		super((NcDataset) group.getDataset(), (NcGroup) group, shortName, array);
		addOneAttribute(Factory.createAttribute("signal", 
				StaticDefinition.DATA_VARIANCE_REFERENCE_NAME));
		addOneAttribute(Factory.createAttribute(StaticDefinition.VARIANCE_DATA_REFERENCE_NAME, 
				data.getShortName()));
	}

	public void reduce() throws PlotFactoryException {
		try {
			setCachedData(getData().getArrayUtils().reduce().getArray(), false);
			shape = getData().getShape();
		} catch (Exception e) {
			throw new PlotFactoryException(e);
		} 
		
	}

	public void reduceTo(int rank) throws PlotFactoryException {
		try{
			setCachedData(getData().getArrayUtils().reduceTo(rank).getArray(), false);
			shape = getData().getShape();
		}catch (Exception e) {
			throw new PlotFactoryException(e);
		}
	}


}
