/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package au.gov.ansto.bragg.wombat.dra.algolib.processes;

/**
 * @author jgw
 *
 */
public interface HIPDCorrectEfficiency {

	/**
	 * 
	 * @param inData    Raw exp data double 2D set for Input
	 * @param effMap    Presetting 2D efficiency table from instrument scientist or anywhere else
	 * @param flag        control para for doing correction (flag=1) or not (flag=0)
	 * @param flag contrl parameter flag =1 do correction; flag=0 do nothing
	 * @param Control threshold parameter to reject very small efficiency block
	 * @param inverse   boolean control parameter 
	 *                                if detection efficience less than 100%  set it false
	 *                                if detection efficience greater than 100%  set it true
	 * @return The filtered array.
	 * @return  foo        corrected  2D double data set
	 */
    public  double[][] doSensitivity( double[][] data, double[][] sensitivity,int flag, double thresh,  boolean inverse ); 
}
