/*******************************************************************************
 * Copyright (c) 2004  Australian Nuclear Science and Technology Organisation.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * GumTree Platform is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * Contributors:
 *     Hugh Rayner (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package au.gov.ansto.bragg.common.dra.algolib.plot;

import java.awt.Color;


/**
 * A simple class used to store 1D plot data.
 * @author hrz
 *
 */
public class PlotData1D extends PlotData {
	/**
	 * The x coordinates.
	 */
    public double[] x;
    /**
     * The y coordinates.
     */
	public double[] y;
	/**
	 * The y error.
	 */
	public double[] err;
	/**
	 * The complex part of the Y values (significant for FFT).
	 */
	public double[] imaginary;
	/**
	 * The color of the plot.
	 */
	public Color color;
	/**
	 * The color of the plot's marks.
	 */
	public Color markColor;
	/**
	 * The type of marks to use.
	 */
	public int markType;
	/**
	 * The type of stroke to use.
	 */
	public int strokeType;
	/**
	 * The type of error bars to use.
	 */
	public int errorBars;
	/**
	 * The name of the plot.
	 */
	public String name;
	
	/**
	 * Creates a new PlotData1D
	 * @param x2 The x coordinates.
	 * @param y2 The y coordinates.
	 * @param e The error values.
	 * @param color The color of the plot.
	 * @param name The name of the plot.
	 */
	public PlotData1D(double[] x2, double[] y2, double[] e, Color color, String name)
	{
		super();
		this.x = x2;
		this.y = y2;
		this.err = e;
		this.color = color;
		this.name = name;
		this.strokeType = 0;
		this.markColor = color;
		this.markType = 0;
		this.errorBars = 0;
	}
	
	public PlotData1D(PlotData1D in)
	{
		super();
		this.x = in.x;
		this.y = in.y;
		this.err = in.err;
		this.color = in.color;
		this.name = in.name;
		this.strokeType = in.strokeType;
		this.markColor = in.markColor;
		this.markType = in.markType;
		this.errorBars = in.errorBars;
	}
	
	/**
	 * Creates a new PlotData1D
	 * @param x The x coordinates.
	 * @param y The y coordinates.
	 * @param err The error values.
	 * @param color The color of the plot.
	 * @param name The name of the plot.
	 */
	public PlotData1D(double[] x, double[] y, double[] err, Color color, int strokeType, Color markColor, int markType, int errorBars, String name)
	{
		super();
		this.x = x;
		this.y = y;
		this.err = err;
		this.color = color;
		this.name = name;
		this.strokeType = strokeType;
		this.markColor = markColor;
		this.markType = markType;
		this.errorBars = errorBars;
	}
	
	private String showArrayErr(double[] xa, double[] ya, double[] err)
	{
		if(err == null)
			return showArrayPairs(xa,ya);
		if(xa.length != ya.length)
		{
			return "X = "+showArray(xa)+"; Y = "+showArray(ya)+"; err = "+showArray(err);
		}
		String rval = "{";
		for(int j = 0; j < xa.length; j++)
		{
			rval = rval + "(" + xa[j]+", "+ya[j]+"+-"+err[j]+")";
			if(j < xa.length - 1)
				rval = rval +", ";
		}
		rval = rval +"}";
		return rval;
	}
	
	private String showArrayPairs(double[] xa, double[] ya)
	{
		if(xa.length != ya.length)
		{
			return "X = "+showArray(xa)+"; Y = "+showArray(ya);
		}
		String rval = "{";
		for(int j = 0; j < xa.length; j++)
		{
			rval = rval + "(" + xa[j]+", "+ya[j]+")";
			if(j < xa.length - 1)
				rval = rval +", ";
		}
		rval = rval +"}";
		return rval;
	}
	
	private String showArray(double[] xa)
	{
	    String rval = "{";
		for(int j = 0; j < xa.length; j++)
		{
			rval = rval + xa[j];
			if(j < xa.length - 1)
				rval = rval +", ";
		}
		rval = rval +"}";
		return rval;
	}
	
	public String toString()
	{
		return "PlotData1D ("+showArrayErr(x,y,err)+"; color = "+color+"; name = "+name;
	}
}
