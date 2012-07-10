/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
*/
package au.gov.ansto.bragg.common.dra.algolib.errPro.processes;

/**
 * @author jgw
 * Tel: +61 2 9717 7062  Fax: +61 2 9717 9799
 * Data Analysis Team, Bragg Institute,Bld.82
 * ANSTO PMB 1 Menai NSW 2234 AUSTRALIA

 */
public class LibErrorPropagation {
 

	/** Method id developed for error process  in  two variables addtions u = x + y
	 * @param  xData   input x value
	 * @param  inxError   input  error of x value
	 * @param  yData    input y value
	 * @param  inyError     input  error of y value
	 * @return  du error in return  du = dx + dy
	 */
	public double errorEdditionPropagation(double xData, double inxError, double yData, double inyError) {
			
		return  inxError + inyError;				
	}
	      /*
	       * 
	       */
	/** Method id developed for error  process in  two variables multiple calculation  u = x * y
	 * @param  xData   input x value
	 * @param  inxError   input  error of x value
	 * @param  yData    input y value
	 * @param  inyError     input  error of y value
	 * @return  du error in return  du = ydx + xdy
	 */
	public double errorMultiplePropagation(double xData, double inxError, double yData, double inyError) {		

		return  yData*inxError + xData*inyError;				
	}


	/** Method id developed for error  process in  two variables in devise calculation  u = x / y
	 * @param  xData   input x value
	 * @param  inxError   input  error of x value
	 * @param  yData    input y value
	 * @param  inyError     input  error of y value
	 * @return  du error in return  du = (ydx + xdy)/ y*y
	 */
public double errorDevisePropagation(double xData, double inxError, double yData, double inyError) {
		
		return (yData*inxError + xData*inyError)/yData;				
	}
/** Method id developed for error  process in  two variables in power calculation  u = y^x
 * @param  xData   input x value
 * @param  inxError   input  error of x value
 * @param  yData    input y value
 * @param  inyError     input  error of y value
 * @return  du error in return  du = xln(y)*dx + xy^(x-1)dy
 */
public double errorPowerPropagation(double xData, double inxError, double yData, double inyError) {
	
	return (xData*(Math.log(yData))*inxError + xData*(Math.log(yData))*inyError)/yData;				
}	

/** Method id developed for error  process in  two variables in power calculation  u = f(x1,x2,...xn)
 * @param  xData   input x value
 * @param  inxError   input  error of x value
 * @param  yData    input y value
 * @param  inyError     input  error of y value
 * @return  du error in return  du = (df/dx1)dx1  + (df/dx2)dx2 + ...  + (df/dxn)dxn
 * 
 */
public double errorMultiVariablePropagation(double xData, double inxError, double[] dfdx, double[] inxErr) {
	double totErr = 0.0;
	int nElem = dfdx.length;
	for(int i = 0; i <  nElem; i++)
		totErr +=  (dfdx[i])*inxErr[i];
	
	return 	totErr;			
	
	
}	
/** Method id developed for error  process in  two variables in power calculation  u = f(x1,x2,...xn)
 * @param  xData   input x value
 * @param  inxError   input  error of x value
 * @param  yData    input y value
 * @param  inyError     input  error of y value
 * @return  du error  in return standard error
 *                     du^2 = (df/dx1)^2dx1^2  + (df/dx2)^2dx2^2 + ...  + (df/dxn)^2dxn^2
 * 
 */
public double errorStandPropagation(double xData, double inxError, double[] dfdx, double[] inxErr) {
	double totErr = 0.0;
	
	int nElem = dfdx.length;
	for(int i = 0; i <  nElem; i++)
		totErr +=  (dfdx[i])*(dfdx[i])*inxErr[i]*inxErr[i];
	double stdErr = Math.sqrt(totErr);
	return 	stdErr;			
	
	
}	
}
