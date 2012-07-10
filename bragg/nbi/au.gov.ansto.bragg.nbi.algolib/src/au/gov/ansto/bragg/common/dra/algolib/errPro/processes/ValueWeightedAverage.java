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
public class ValueWeightedAverage {
	
	double meanValue;
	double weightedAverage;
	double weightError;
	
	 /**
	  * This method id used to calculate average raw  measurement  to have mean of raw measurement
	  * @param xMeas    input number of measurements
	  * @return   reutrn the mean of  number of measurement
	  */
	public double meanOfRawMeasurement(double[] xMeas){
		    int nMeasure = xMeas.length;
		    double meanValue= 0.0; 
		     for ( int n = 0; n < nMeasure; n++)  meanValue += xMeas[n]/nMeasure;
		return meanValue;
	}
	/**
	*
	 * @param xErrors
	 * @return
	 */
	public  double weightErrorCal(double[] xErrors){
		double reviseError = 0.0;
		int nError = xErrors.length;
		for ( int n = 0; n < nError; n++)  reviseError  += 1/(xErrors[n] * xErrors[n]);
		weightError = 1.0 / Math.sqrt(reviseError);
		return weightError;
	}
   /**
    * 	 The method is designed to have weighted valuse merging. This method follows math.
	 *    u_mean = (sum_(1 -> n) w_n * x_n) / w
	 *    w_n  = 1 / xErrors* xErrors 
	 *    w = sum w_n
	 *    Mean of xErrors = 1/ sqrt(w)
    * @param xValues
    * @param xErrors
    * @return   merged weight value of measurement.
    */
	 public double  weightedMultivalueMerge (double[] xValues, double[] xErrors) {

		 		 int nMeasure = xValues.length;
		 		 double totals = 0.0;
		 		 if (xErrors.length != nMeasure) return  Double.NaN;
		 		 for (int n = 0; n< nMeasure; n ++) 
		 			 totals += xValues[n] / (xErrors[n]*xErrors[n]);
		 		 double var = weightErrorCal(xErrors);
		 		    weightedAverage = totals * (var*var);
		 		 
		 return weightedAverage;
	 }
}
