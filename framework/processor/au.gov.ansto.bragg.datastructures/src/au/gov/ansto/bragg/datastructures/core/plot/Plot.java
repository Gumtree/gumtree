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
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.math.EData;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.common.Log;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.exception.PlotMathException;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;

/**
 * @author nxi
 * Created on 12/03/2008
 */
public interface Plot extends IGroup {

	
	/**
	 * Return the Array data in the signal of the Plot
	 * @return GDM Array object
	 * Created on 05/03/2008
	 * @throws IOException 
	 */
	public IArray findSignalArray() throws IOException;
	
	/**
	 * Return the list of Axes of the signal.
	 * @return List of Axis
	 * Created on 05/03/2008
	 */
	public List<Axis> getAxisList();
	
	/**
	 * Return the Array data in the Axes of the signal.
	 * @return List of Array
	 * Created on 05/03/2008
	 * @throws SignalNotAvailableException 
	 */
	public List<IArray> getAxisArrayList() throws SignalNotAvailableException;
	
	/**
	 * Return the variances of the axes in List type.
	 * @return List of Variances
	 * Created on 05/03/2008
	 */
	public List<Variance> getAxisVarianceList();
	
	/**
	 * Return the Log DataItem of the Plot
	 * @return Log type object
	 * Created on 05/03/2008
	 */
	public Log getLogDataItem();
	
	/**
	 * Return the Log as a String
	 * @return String type
	 * Created on 05/03/2008
	 */
	public String getLogString();
	
	/**
	 * Return the description as String
	 * @return String object
	 * Created on 05/03/2008
	 */
	public String getDescription();
	
	/**
	 * Return user comments as DataItem object.
	 * @return String type of object.
	 * Created on 05/03/2008
	 */
	public String getUserComments();
	
	/**
	 * Append contents to the description attribute.
	 * @param description
	 * Created on 05/03/2008
	 */
	public void addDescription(String description);
	
	/**
	 * Append log information to the Log attribute.
	 * @param log in String type
	 * @param userInfo in String type
	 * Created on 05/03/2008
	 */
	public void addLog(String log, String userInfo);
	
	/**
	 * Append user comments to the Comment attribute.
	 * @param comment in String type
	 * @param userName in String type
	 * Created on 05/03/2008
	 */
	public void addComment(String comment, String userName);
	
	/**
	 * Find the signal as Data type object. Clue is to find the Data
	 * that has signal='1' attribute.
	 * @return Data type object
	 * Created on 06/03/2008
	 */
	public Data findSingal();
	
	/**
	 * Get the variance from the plot. 
	 * @return Variance type object
	 * Created on 06/03/2008
	 */
	public Variance getVariance();
	

	/**
	 * Create a plotable Data object and add it to the Plot.
	 * @param shortName a short name of the data in String type
	 * @param array GDM Array object
	 * @param title in String type
	 * @param units in String type
	 * Created on 12/03/2008
	 * @throws InvalidArrayTypeException 
	 */
	public void addData(String shortName, IArray array, String title, String units) 
	throws InvalidArrayTypeException;
	
	/**
	 * Create a plotable Data object and add it to the Plot.
	 * @param shortName a short name of the data in String type
	 * @param array GDM Array object
	 * @param title in String type
	 * @param units in String type
	 * @param varianceArray in GDM Array type
	 * Created on 12/03/2008
	 * @throws InvalidArrayTypeException 
	 */
	public void addData(String shortName, IArray array, String title, String units, 
			IArray varianceArray) 
	throws InvalidArrayTypeException;
	
	/**
	 * Create a Variance object and add it to the Plot.
	 * @param shortName a short name of the data in String type
	 * @param varianceArray GDM Array object
	 * Created on 12/03/2008
	 * @throws InvalidArrayTypeException 
	 */
	public void addDataVariance(String shortName, IArray varianceArray) 
	throws InvalidArrayTypeException;

	/**
	 * Create a Variance object and add it to the Plot.
	 * @param varianceArray GDM Array object
	 * Created on 12/03/2008
	 * @throws InvalidArrayTypeException 
	 */
	public void addDataVariance(IArray varianceArray) 
	throws InvalidArrayTypeException;

	/**
	 * Create an Axis object and add it to the Plot.
	 * @param shortName a short name of the axis in String type
	 * @param array GDM Array object
	 * @param title in String type
	 * @param units in String type
	 * Created on 12/03/2008
	 * @throws InvalidArrayTypeException 
	 * @throws PlotFactoryException 
	 */
	public void addAxis(String shortName, IArray array, String title, String units, int dimension) 
	throws InvalidArrayTypeException, PlotFactoryException;

	/**
	 * Create an Axis object and add it to the Plot.
	 * @param shortName a short name of the axis in String type
	 * @param array GDM Array object
	 * @param title in String type
	 * @param units in String type
	 * @param axisArray in GDM Array type
	 * Created on 12/03/2008
	 * @throws InvalidArrayTypeException 
	 * @throws PlotFactoryException 
	 */
	public void addAxis(String shortName, IArray array, String title, String units, int dimension,
			IArray axisArray) 
	throws InvalidArrayTypeException, PlotFactoryException;
	
	/**
	 * Return the axis in the specified dimension. 
	 * @param dimension in int type
	 * @return Axis object
	 * Created on 13/03/2008
	 */
	public Axis getAxis(int dimension);

	/**
	 * Add an existing axis to the plot, which will represent the given dimension.
	 * @param axis Axis object
	 * @param dimension in int type
	 * Created on 14/03/2008
	 * @throws IOException 
	 * @throws PlotFactoryException 
	 * @throws InvalidArrayTypeException 
	 */
	public void addAxis(Axis axis, int dimension) throws InvalidArrayTypeException, PlotFactoryException, IOException;
	
	/**
	 * Return the creation time of this plot. If the time stamp is not
	 * available, it will return 0.
	 * @return timestamp in long type
	 * Created on 17/03/2008
	 */
	public long getCreationTimeStamp();
	
	/**
	 * Return the last modification time stamp of the plot. If the time stamp is not
	 * available, it will return 0.
	 * @return timestamp in long type
	 * Created on 17/03/2008
	 */
	public long getLastModificationTimeStamp();

	/**
	 * Reduce the rank of the Plot group data by remove the dimension that has a size of 1. 
	 * The axis of that dimension will also be removed. 
	 * @throws IOException
	 * Created on 17/06/2008
	 */
	public void reduce() throws PlotFactoryException;
	
	/**
	 * Do a transpose if the plot is a matrix.
	 * @return Plot object
	 * @throws StructureTypeException
	 * Created on 10/07/2008
	 */
	public Plot matrixTranspose() throws StructureTypeException;
	
	/**
	 * Add a plot to this one element by element. 
	 * @param plot Plot object
	 * @return Plot object
	 * Created on 10/07/2008
	 * @throws StructureTypeException 
	 */
	public Plot add(Plot plot) throws StructureTypeException;
	public Plot toAdd(Plot plot) throws StructureTypeException;
	
	/**
	 * Create a new Plot using same backing store as this plot, by
	 * fixing the specified dimension at the specified index value. This reduces rank by 1.
	 * @param dimension in which dimension it will slice
	 * @param value the index of the slice in the given dimension 
	 * @return new Plot object
	 * @throws StructureTypeException
	 * Created on 10/07/2008
	 */
	public Plot slice(int dimension, int value) throws StructureTypeException;
	
	/**
	 * Do a matrix multiply on the two plots. The shape of the two plots must 
	 * match matrix multiply requirement. 
	 * @param plot in Plot type
	 * @return new Plot object
	 * Created on 10/07/2008
	 */
	public Plot matMultiply(Plot plot);
	
	/**
	 * Do a element wise multiply on the two plots, in which make Fij = Xij x Yij. 
	 * @param plot Plot object
	 * @return new Plot object
	 * Created on 10/07/2008
	 * @throws PlotMathException 
	 */
	public Plot eltMultiply(Plot plot) throws PlotMathException;
	public Plot toEltMultiply(Plot plot) throws PlotMathException;
	
	/**
	 * Do a element wise multiplying on the plot and an array, in which make Fij = Xij x Yij.
	 * Assume the variance is the same as the array. 
	 * @param array GDM Array object
	 * @return new Plot object
	 * Created on 10/10/2008
	 * @throws PlotMathException 
	 */
	public Plot eltMultiply(IArray array) throws PlotMathException;
	public Plot toEltMultiply(IArray array) throws PlotMathException;
	public Plot toEltDivide(IArray array, IArray variance) throws PlotMathException;
	
	/**
	 * Do a element wise add on the plot and an array, in which make Fij = Xij + Yij.
	 * Assume the variance is the same as the array. 
	 * @param array GDM Array object
	 * @return new Plot object
	 * Created on 10/07/2008
	 * @throws PlotMathException 
	 */
	public Plot add(IArray array) throws PlotMathException;
	public Plot toAdd(IArray array) throws PlotMathException;
	
	/**
	 * Return the section of the plot as a new plot. The new plot will use the same
	 * data storage as the current one. 
	 * @param reference the origin of the section
	 * @param shape java integer array
	 * @return new Plot object
	 * @throws StructureTypeException
	 * Created on 14/07/2008
	 */
	public Plot section(int[] reference, int[] shape) throws StructureTypeException;
	
	/**
	 * Return the section of the plot that is referred by the reference, shape and stride. 
	 * @param reference the origin of the section in java integer array
	 * @param shape java integer array
	 * @param stride java integer array
	 * @return new Plot object
	 * @throws StructureTypeException
	 * Created on 14/07/2008
	 */
	public Plot section(int[] reference, int[] shape, int[] stride) throws StructureTypeException;
	
	/**
	 * Copy the plot into a new plot with double type storage. Assume the plot has only numeric data
	 * @return new Plot object
	 * @throws StructureTypeException
	 * Created on 14/07/2008
	 */
	public Plot copyToDouble() throws StructureTypeException;
	
	/**
	 * Return the point in the plot referred by an index. 
	 * @param index in PlotIndex type
	 * @return new Plot object
	 * @see PlotIndex
	 * Created on 14/07/2008
	 */
	public Point getPoint(PlotIndex index);
	
	/**
	 * Create an index from the plot. 
	 * @return PlotIndex object
	 * @throws SignalNotAvailableException
	 * Created on 14/07/2008
	 */
	public PlotIndex getIndex() throws SignalNotAvailableException;
	
	/**
	 * Set a double value at the position referred by index in the plot.  
	 * @param index in PlotIndex type
	 * @param value a double value
	 * @throws SignalNotAvailableException
	 * Created on 14/07/2008
	 */
	public void setDouble(PlotIndex index, double value) throws SignalNotAvailableException;
	
	/**
	 * Get the point of the plot with a maximum value. 
	 * @return a point in Point type
	 * Created on 14/07/2008
	 * @throws PlotMathException 
	 */
	public Point getMaximumPoint() throws PlotMathException;
	
	/**
	 * Get the maximum value of the plot.
	 * @return double value
	 * @throws IOException
	 * Created on 14/07/2008
	 */
	public double getMaximumValue() throws IOException;
	
	/**
	 * Get the point of the plot with a minimum value.
	 * @return a point in Point type
	 * Created on 14/07/2008
	 * @throws PlotMathException 
	 */
	public Point getMinimumPoint() throws PlotMathException;
	
	/**
	 * Get the minimum value of the plot.
	 * @return double value
	 * @throws IOException
	 * Created on 14/07/2008
	 */
	public double getMinimumValue() throws IOException;
	
	/**
	 * Find the list of the points that have the maximum value of the plot. 
	 * @return return a list of Point objects
	 * Created on 14/07/2008
	 */
	public List<Point> getMaximumPointList();
	
	/**
	 * Find the list of the points that have the minimum value of the plot.
	 * @return return a list of Point objects
	 * Created on 14/07/2008
	 */
	public List<Point> getMinimumPointList();
	
	/**
	 * Return the units of the data of the plot. 
	 * @return String object
	 * Created on 14/07/2008
	 */
	public String getDataUnits();
	
	/**
	 * Find the units of the axes of the plot. 
	 * @return array of String objects
	 * Created on 14/07/2008
	 */
	public String[] getAxisUnits();
	
	/**
	 * Find the rank of the plot.
	 * @return a integer value
	 * Created on 14/07/2008
	 */
	public int getRank();
	
	/**
	 * Do a element-wise inverse on a plot -- Yij = 1 / Xij.
	 * @return new Plot object
	 * @throws PlotMathException
	 * Created on 14/07/2008
	 */
	public Plot eltInverse() throws PlotMathException;
	public Plot toEltInverse() throws PlotMathException;

	/**
	 * Sum up all the elements in the plot. 
	 * @return a EData object with result and variance.
	 * @throws IOException
	 * Created on 14/07/2008
	 */
	public EData<Double> sum() throws IOException;

	/**
	 * Do sum calculation for every slice of data on a dimension. The result will be 
	 * a pattern plot result. 
	 * @param dimension
	 * @return new Plot object
	 * @throws PlotMathException
	 * Created on 14/07/2008
	 */
	public Plot sumForDimension(int dimension) throws PlotMathException;

	/**
	 * Do sum calculation for every slice of data on a dimension. The result will be 
	 * a pattern plot result. The result will not be normalised against the size of that
	 * dimension. 
	 * @param dimension
	 * @return new Plot object
	 * @throws PlotMathException
	 * Created on 14/07/2008
	 */
	public Plot enclosedSumForDimension(int dimension) throws PlotMathException;

	/**
	 * Check if the two plots are conformable. They must have the same shape and
	 * share the same axes to be conformable. 
	 * @param plot in Plot type
	 * @return true or false
	 * @throws PlotMathException
	 * Created on 14/07/2008
	 */
	public boolean isComformable(Plot plot) throws PlotMathException;

	/**
	 * Add a value with variance to a plot. 
	 * @param number a double value
 	 * @param variance a double value
	 * @return new Plot object
	 * @throws PlotMathException
	 * Created on 14/07/2008
	 */
	public Plot add(double number, double variance) throws PlotMathException;
	public Plot toAdd(double number, double variance) throws PlotMathException;

	/**
	 * Scale the plot with a double value with certain variance. 
	 * @param value a double value
	 * @param variance a double value
	 * @return new Plot object
	 * @throws PlotMathException
	 * Created on 14/07/2008
	 */
	public Plot scale(double value, double variance) throws PlotMathException;
	public Plot toScale(double value, double variance) throws PlotMathException;

	/**
	 * Do a matrix inverse of the plot. The plot must comply the matrix inverse requirement, e.g., 
	 * it must be a squire matrix and the det is not zero
	 * @return new Plot object
	 * @throws PlotMathException
	 * Created on 14/07/2008
	 */
	public Plot matInverse() throws PlotMathException;

	/**
	 * Do an element-wise power calculation of the plot. Yij = Xij ^ power. 
	 * @param value a double value
	 * @return new Plot object
	 * @throws PlotMathException
	 * Created on 14/07/2008
	 */
	public Plot power(double value) throws PlotMathException;
	public Plot toPower(double value) throws PlotMathException;

	/**
	 * Do an element-wise e raised to the power of double values in the plot.
	 * @return new Plot object
	 * @throws PlotMathException
	 * Created on 14/07/2008
	 */
	public Plot exp() throws PlotMathException;
	public Plot toExp() throws PlotMathException;

	/**
	 * Do an element-wise natural logarithm (base e) of values in the plot.
	 * @return new Plot object
	 * @throws PlotMathException
	 * Created on 14/07/2008
	 */
	public Plot ln() throws PlotMathException;
	public Plot toLn() throws PlotMathException;

	/**
	 * Do an element-wise logarithm (base 10) of values in the plot.
	 * @return new Plot object
	 * @throws PlotMathException
	 * Created on 14/07/2008
	 */
	public Plot log10() throws PlotMathException;
	public Plot toLog10() throws PlotMathException;

	/**
	 * Copy a list of Axis objects to the current plot. All of the current Axis of the plot
	 * will be replaced. 
	 * @param axisList
	 * @throws PlotFactoryException
	 * Created on 14/07/2008
	 */
	public void copyAxes(List<Axis> axisList) throws PlotFactoryException;

	/**
	 * Add extra dataItem to the plot. 
	 * @param shortName a String value
	 * @param array the storage in GDM Array type
	 * @param title a String value
	 * @param units a String value
	 * @param varianceArray the storage for variance in GDM Array type
	 * @throws InvalidArrayTypeException
	 * Created on 08/08/2008
	 */
	public void addCalculationData(String shortName, IArray array, String title,
			String units, IArray varianceArray) throws InvalidArrayTypeException;
	
	/* pvh 09mar2009*/
	public void addCalculationData(String shortName, IArray array, String title,
			String units) throws InvalidArrayTypeException;

	/**
	 * Find the extra data with the given name.
	 * @param shortName a String value
	 * @return a Data type object
	 * Created on 08/08/2008
	 */
	public Data findCalculationData(String shortName);

	/**
	 * Add an axis after the last dimension that has an axis. 
	 * @param axis Axis object
	 * Created on 28/08/2008
	 * @throws IOException 
	 * @throws PlotFactoryException 
	 * @throws InvalidArrayTypeException 
	 */
	public void addAxis(Axis axis) throws InvalidArrayTypeException, PlotFactoryException, IOException;
	
	/**
	 * Return the data dimension type of the data.
	 * @return enum type object
	 * Created on 30/09/2008
	 */
	public DataDimensionType getDimensionType();
	
	/**
	 * Integrate on the given dimension. Return a new Plot which is one dimension less than the given 
	 * plot. 
	 * @param dimension in integer type
	 * @return new Plot object
	 * Created on 30/09/2008
	 * @throws PlotMathException 
	 */
	public Plot integrateDimension(int dimension) throws PlotMathException;
	
	/**
	 * Integrate on the given dimension. Return a new Plot which is one dimension less than the given 
	 * plot. 
	 * @param dimension in integer type
	 * @return new Plot object
	 * Created on 30/09/2008
	 * @throws PlotMathException 
	 */
	public Plot enclosedIntegrateDimension(int dimension) throws PlotMathException;
	
	/**
	 * Reduce the rank of the plot to at least specified value. If the rank of the current
	 * plot is smaller than the given value, do nothing.
	 * @param rank in integer type
	 * @throws PlotFactoryException
	 * Created on 20/10/2008
	 */
	public void reduceTo(int rank) throws PlotFactoryException;
	
	/**
	 * Return the variance array of the plot. If the variance does not exist, return null;
	 * @return Array object
	 * Created on 23/10/2008
	 */
	public IArray findVarianceArray();
	
	/**
	 * Get the calculation data in a list.
	 * @return List of Data Object
	 * Created on 19/11/2008
	 */
	public List<Data> getCalculationData();

	/**
	 * Add processing information to the log. The processing log will start with clause: "Processed with: ".
	 * @param string String type
	 * Created on 04/12/2008
	 */
	public void addProcessingLog(String string);

	/**
	 * Get the processing log as a String. The processing log is the log that starts with clause: 
	 * "Processed with: ".
	 * @return String object
	 * Created on 04/12/2008
	 */
	public String getProcessingLog();

	/**
	 * Set the title for the plot. 
	 * @param title
	 * Created on 18/12/2008
	 */
	public void setTitle(String title);
	
	/**
	 * Get the log when this plot is copied from a original Group object.
	 * @return String object
	 * Created on 17/03/2009
	 */
	public String getCopyingLog();
	
	/**
	 * Get the title of the plot.
	 * @return String object
	 * Created on 18/03/2009
	 */
	public String getTitle();

	/**
	 * Calculate the memory size that allocated for the plot object. 
	 * @return long value
	 * Created on 06/04/2009
	 * @throws IOException 
	 */
	public long calculateMemorySize() throws IOException;
	
	/**
	 * Clear the data, the variance and the axis data items in the plot.  
	 * 
	 * Created on 15/04/2009
	 */
	public void clearData();
}
