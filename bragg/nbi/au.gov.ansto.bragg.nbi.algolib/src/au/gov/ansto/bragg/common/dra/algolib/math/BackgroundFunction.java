/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
*/
package au.gov.ansto.bragg.common.dra.algolib.math;

/**
 * @author jgw
 * Tel: +61 2 9717 7062  Fax: +61 2 9717 9799
 * Data Analysis Team, Bragg Institute,Bld.82
 * ANSTO PMB 1 Menai NSW 2234 AUSTRALIA

 */
public class BackgroundFunction {
	/**
	 * The first data element.
	 */
	public float aFunc;
	/**
	 * The second data element.
	 */
	public float bFunc;
	/**
	 * Creates and initialises a new BackgroundFunction.
	 *  y = a + b * x
	 * @param xv The initial value of x.
	 * @param yv The initial value of y.
	 */
	public  BackgroundFunction(float av, float bv)
	{
		this.aFunc = av;
		this.bFunc = bv;
	}
	/**
	 * Creates and initialises a new BackgroundFunction.
	 * 	 *  y = a + b * x
	 * @param xv The initial double value of x.
	 * @param yv The initial double value of y.
	 */
	public  BackgroundFunction(double av,double bv)
	{
		this.aFunc = (float)av;
		this.bFunc = (float)bv;
	}
}
