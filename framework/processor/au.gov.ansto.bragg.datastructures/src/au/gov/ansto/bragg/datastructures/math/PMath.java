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
package au.gov.ansto.bragg.datastructures.math;

import java.io.IOException;

import org.gumtree.data.math.EData;

import au.gov.ansto.bragg.datastructures.core.exception.PlotMathException;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;

/**
 * Math library for Plot objects. All calculations are accompanied with error propagations. 
 * @see EMath
 * @author nxi
 * Created on 07/07/2008
 */
public class PMath {

	/**
	 * Add two plot into one plot result. Both plots must in the same shape. Both of the plots 
	 * must share the same axes. 
	 * @param plot1 in Plot type
	 * @param plot2 in Plot type
	 * @return new Plot with a new storage
	 * @throws StructureTypeException 
	 * @see EMath#add(org.gumtree.data.gdm.core.Array, org.gumtree.data.gdm.core.Array, org.gumtree.data.gdm.core.Array, org.gumtree.data.gdm.core.Array)
	 * Created on 14/07/2008
	 */
	public static Plot add(Plot plot1, Plot plot2) throws StructureTypeException{
		return plot1.toAdd(plot2);
	}
	
	/**
	 * Add a value with variance to a plot. 
	 * @param plot in Plot type
	 * @param number a double value
 	 * @param variance a double value
	 * @return new Plot with a new storage
	 * @throws PlotMathException
	 * @see EMath#add(org.gumtree.data.gdm.core.Array, double, org.gumtree.data.gdm.core.Array, double)
	 * Created on 14/07/2008
	 */
	public static Plot add(Plot plot, double number, double variance) throws PlotMathException{
		return plot.toAdd(number, variance);
	}
	
	
	/**
	 * Check if the two plots are conformable. They must have the same shape and
	 * share the same axes to be conformable. 
	 * @param plot1 in Plot type
	 * @param plot2 in Plot type
	 * @return boolean type
	 * @throws PlotMathException
	 * Created on 14/07/2008
	 */
	public static boolean conformable(Plot plot1, Plot plot2) throws PlotMathException{
		return plot1.isComformable(plot2);
	}
	
	/**
	 * Convert the Plot to a Double type plot. Assume the plot has numeric data storage. 
	 * @param plot in Plot type
	 * @return a new Plot with new storage
	 * @throws StructureTypeException
	 * Created on 14/07/2008
	 */
	public static Plot convertToDouble(Plot plot) throws StructureTypeException{
		return plot.copyToDouble();
	}
	
	/**
	 * Find the maximum value of the Plot.
	 * @param plot in Plot type
	 * @return a double value
	 * @throws IOException
	 * Created on 14/07/2008
	 */
	public static double getMaximum(Plot plot) throws IOException{
		return plot.getMaximumValue();
	}

	/**
	 * Find the minimum value for the Plot.
	 * @param plot in Plot type
	 * @return a double value
	 * @throws IOException
	 * Created on 14/07/2008
	 */
	public static double getMinimum(Plot plot) throws IOException{
		return plot.getMinimumValue();
	}
	
	/**
	 * Sum up all the elements in the plot. 
	 * @param plot in Plot type
	 * @return EData type -- a double value with variance
	 * @throws IOException
	 * @see {@link EData}
	 * @see {@link EMath#sum(org.gumtree.data.gdm.core.Array, org.gumtree.data.gdm.core.Array)}
	 * Created on 14/07/2008
	 */
	public static EData<Double> sum(Plot plot) throws IOException{
		return plot.sum();
	}
	
	/**
	 * Do sum calculation for every slice of data on a dimension. The result will be 
	 * a pattern plot result. 
	 * @param plot 
	 * @param dimension
	 * @return
	 * @throws PlotMathException
	 * @see {@link EMath}{@link #sumForDimension(Plot, int)}
	 * Created on 14/07/2008
	 */
	public static Plot sumForDimension(Plot plot, int dimension) throws PlotMathException{
		return plot.sumForDimension(dimension);
	}

	/**
	 * Do a element-wise multiply on two plots. Xij = Aij * Bij. Both plots must have the 
	 * same shape and they share the same axes. 
	 * @param plot1 in Plot type
	 * @param plot2 in Plot type
	 * @return a new Plot object with new storage
	 * @throws PlotMathException 
	 * @see EMath#eltMultiply(org.gumtree.data.gdm.core.Array, org.gumtree.data.gdm.core.Array, org.gumtree.data.gdm.core.Array, org.gumtree.data.gdm.core.Array)
	 * Created on 14/07/2008
	 */
	public static Plot eltMultiply(Plot plot1, Plot plot2) throws PlotMathException{
		return plot1.toEltMultiply(plot2);
	}
	
	/**
	 * Scale the plot with a double value with certain variance. 
	 * @param plot in Plot type
	 * @param value a double value
	 * @param variance a double value
	 * @return new Plot with new storage
	 * @throws PlotMathException
	 * @see {@link EMath}{@link #scale(Plot, double, double)}
	 * Created on 14/07/2008
	 */
	public static Plot scale(Plot plot, double value, double variance) throws PlotMathException{
		return plot.toScale(value, variance);
	}
	
	/**
	 * Do a matrix multiply on two plots. The two plots must comply with matrix multiply requirement.  
	 * @param plot1 in Plot type
	 * @param plot2 in Plot type
	 * 
	 * @return new Plot with new storage type
	 * @see EMath#matMultiply(org.gumtree.data.gdm.core.Array, org.gumtree.data.gdm.core.Array, org.gumtree.data.gdm.core.Array, org.gumtree.data.gdm.core.Array)
	 * Created on 14/07/2008
	 */
	public static Plot matMultiply(Plot plot1, Plot plot2){
		return plot1.matMultiply(plot2);
	}

	/**
	 * Do a element-wise inverse on a plot -- Yij = 1 / Xij
	 * @param plot in Plot type
	 * @return new Plot object
	 * @throws PlotMathException
	 * @see {@link EMath}{@link #eltInverse(Plot)}
	 * Created on 14/07/2008
	 */
	public static Plot eltInverse(Plot plot) throws PlotMathException{
		return plot.toEltInverse();
	}
	
	/**
	 * Do a matrix inverse of the plot. The plot must comply the matrix inverse requirement, e.g., 
	 * it must be a squire matrix and the det is not zero
	 * @param plot in Plot type
	 * @return new Plot
	 * @throws PlotMathException
	 * @see {@link EMath}{@link #eltInverse(Plot)}
	 * Created on 14/07/2008
	 */
	public static Plot matInverse(Plot plot) throws PlotMathException{
		return plot.matInverse();
	}
	
	/**
	 * Do an element-wise power calculation of the plot. Yij = Xij ^ power. 
	 * 
	 * @param plot in Plot type
	 * @param value in integer type
	 * @return new Plot type
	 * @throws PlotMathException
	 * @see {@link EMath}{@link #power(Plot, int)}
	 * Created on 14/07/2008
	 */
	public static Plot power(Plot plot, double value) throws PlotMathException{
		return plot.toPower(value);
	}
	
	/**
	 * Do an element-wise e raised to the power of double values in the plot.
	 * @param plot in Plot type
	 * @return new Plot with new storage
	 * @throws PlotMathException
	 * @see {@link EMath}{@link #power(Plot, double)}
	 * Created on 14/07/2008
	 */
	public static Plot exp(Plot plot) throws PlotMathException{
		return plot.toExp();
	}
	
	/**
	 * Do an element-wise natural logarithm (base e) of values in the plot.
	 * @param plot in Plot type
	 * @return new Plot object with new storage
	 * @throws PlotMathException
	 * @see {@link EMath}{@link #exp(Plot)}
	 * Created on 14/07/2008
	 */
	public static Plot ln(Plot plot) throws PlotMathException{
		return plot.toLn();
	}
	
	/**
	 * Do an element-wise logarithm (base 10) of values in the plot.
	 * @param plot in Plot type
	 * @return new Plot object with new storage.
	 * @throws PlotMathException
	 * @see {@link EMath}{@link #log10(Plot)}
	 * Created on 14/07/2008
	 */
	public static Plot log10(Plot plot) throws PlotMathException{
		return plot.toLog10();
	}
	
	/**
	 * Integrate on the given dimension. Return a new Plot which is one dimension less than the given 
	 * plot. 
	 * @param plot a Plot object 
	 * @param dimension in integer type
	 * @return new Plot object
	 * Created on 30/09/2008
	 * @throws PlotMathException 
	 */
	public static Plot integrateDimension(Plot plot, int dimension) throws PlotMathException{
		return plot.integrateDimension(dimension);
	}
}
