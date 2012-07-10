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

import java.io.IOException;
import java.util.List;

import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;

import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;

/**
 * @author nxi
 *
 */
public interface Data extends IDataItem {

	/**
	 * Return the Variance object of the data.
	 * @return Variance object
	 * Created on 05/03/2008
	 */
	public Variance getVariance();
	
	/**
	 * Return the list of axes objects for the data
	 * @return List of Axis objects
	 * Created on 06/03/2008
	 */
	public List<Axis> getAxisList();
	
	/**
	 * Return the title attribute of the data
	 * @return String object
	 * Created on 06/03/2008
	 */
	public String getTitle();
	
	/**
	 * Add an Axis object to the data. This method put a reference of the axis' 
	 * name to the DataItem's 'axes' attribute.
	 * @param axis object
	 * @param dimension in integer type
	 * Created on 06/03/2008
	 */
	public void addAxis(Axis axis, int dimension);
	
	/**
	 * Remove an axis from the DataItem. This will remove the axis reference from the 
	 * 'axes' attribute of the data.
	 * @param axis Axis object
	 * Created on 06/03/2008
	 */
	public void removeAxis(Axis axis);
	
	/**
	 * Add all the axes reference to the data at once. This will create a new
	 * attribute 'axes=axis_names'.
	 * @param axes one to many Axis objects 
	 * Created on 12/03/2008
	 */
	public void addAxes(Axis... axes);

	/**
	 * Add a variance object for this data to the parent Plot group. 
	 * @param varianceArray GDM Array type
	 * @throws InvalidArrayTypeException
	 * Created on 18/04/2008
	 */
	public void addVariance(IArray varianceArray) throws InvalidArrayTypeException;

	/**
	 * Add a variance object for this data to the parent Plot group. 
	 * @param varianceArray GDM Array type
	 * @param shortName in String type
	 * @throws InvalidArrayTypeException
	 * Created on 18/04/2008
	 */
	public void addVariance(IArray varianceArray, String shortName) 
	throws InvalidArrayTypeException;

	/**
	 * Reduce the rank of the Array by remove the dimensions that have a size of 1.
	 * @throws IOException
	 * Created on 17/06/2008
	 */
	public void reduce() throws PlotFactoryException;

	/**
	 * Reduce the rank of the Data to at least certain value, by remove the dimensions 
	 * that have a size of 1. The removal sequence will start from the very outer dimension. 
	 * If the rank reach the target value, stop further removing even if there is still 
	 * dimension that has a size of 1. 
	 * @param rank integer value
	 * @throws PlotFactoryException
	 * Created on 17/12/2008
	 */
	public void reduceTo(int rank) throws PlotFactoryException;

	/**
	 * Set the plot data title.
	 * @param title in String type
	 * Created on 18/12/2008
	 */
	public void setTitle(String title);
	
	/**
	 * Set the units of the data.
	 * @param units in String type
	 */
	public void setUnits(String units);
}
