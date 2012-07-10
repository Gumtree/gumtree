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

import au.gov.ansto.bragg.datastructures.core.exception.IndexOutOfBoundException;

/**
 * @author nxi
 * Created on 07/07/2008
 */
public class Point {

	private PlotIndex index;
	private Plot plot;
	
	public Point(Plot plot, PlotIndex index){
		this.plot = plot;
		this.index = index;
	}
	
	/**
	 * Find the coordinate of the point in the plot.
	 * @return array of double values
	 * @throws IndexOutOfBoundException
	 * Created on 15/07/2008
	 */
	public double[] getCoordinate() throws IndexOutOfBoundException {
		List<Axis> axisList = plot.getAxisList(); 
		double[] coordinates = new double[axisList.size()];
		int dimension = 0;
		for (Axis axis : axisList){
			try {
				coordinates[dimension] = axis.getData().getDouble(index.getAxisIndex(dimension));
			} catch (IOException e) {
				throw new IndexOutOfBoundException(e);
			}
			dimension ++;
		}
		return coordinates;
	}

	/**
	 * Find the coordinate in the specific dimension. 
	 * @param dimension a integer value
	 * @return a double value
	 * @throws IndexOutOfBoundException
	 * Created on 15/07/2008
	 */
	public double getCoordinate(int dimension) throws IndexOutOfBoundException{
		double coordinate = 0;
		try {
			coordinate = plot.getAxisList().get(dimension).getData().getDouble(index.getAxisIndex(dimension));
		} catch (Exception e) {
			throw new IndexOutOfBoundException(e);
		}
		return coordinate;
	}
	
	/**
	 * Find the units of the value of the point.
	 * @return a String object
	 * Created on 15/07/2008
	 */
	public String getUnits() {
		return plot.getDataUnits();
	}
	
	/**
	 * Find the units of the axis of the point.  
	 * @return array of String objects
	 * Created on 15/07/2008
	 */
	public String[] getAxisUnits(){
		return plot.getAxisUnits();
	}
	
	/**
	 * Return the plot that this point belongs to. 
	 * @return Plot object
	 * Created on 15/07/2008
	 */
	public Plot getPlot(){
		return plot;
	}
	
	/**
	 * Get the value of the point. 
	 * @return a double value
	 * @throws IOException
	 * Created on 15/07/2008
	 */
	public double getValue() throws IOException{
		return plot.findSignalArray().getDouble(index.getDataIndex());
	}
	
	/**
	 * Find the plot index of the current point. 
	 * @return a PlotIndex object
	 * Created on 15/07/2008
	 */
	public PlotIndex getIndex(){
		return index;
	}
	
	/**
	 * Find the variance value of the point. 
	 * @return a double value
	 * @throws IOException
	 * Created on 15/07/2008
	 */
	public double getVariance() throws IOException{
		return plot.getVariance().getData().getDouble(index.getDataIndex());
	}
}
