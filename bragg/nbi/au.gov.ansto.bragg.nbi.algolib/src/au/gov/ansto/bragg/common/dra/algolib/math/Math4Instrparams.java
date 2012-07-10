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
public class Math4Instrparams {
	

	public double[][] thetaVectElem (int nScan, int nTubes, double dTheta){

	double twoTheta0=0.0;
	double[][] array2theta = new double[nScan][nTubes];
	double avstepsiz =0.05;	
	
		  twoTheta0=0.0;
		for (int i = 0; i < nScan; i++){
			for (int j = 0; j < nTubes; j++){
				array2theta[i][j] =twoTheta0 + avstepsiz * i + dTheta*j;
			}
		}
	
	return array2theta;

}
	
	public double[] thetaVectArray (int nScan, int nTubes, double[][] twoD2theta,  double delTheta, 
			                                double twoTheta0, double[]stepsize){

		double deltaTheta = 0.0;
		double[] array2theta;
		double horisonCurv = 158.75;
//		double[][][] inDat3D = new double[nScan][nCount][nTubes];
		double avstepsiz =0.05;
		double binsize = 0.0;
		
//		for (int n = 0; n < nScan; n++)
//			for(int k = 0; k < nCount; k ++)
//				for (int j = 0; j < nTubes; j++)
//					inDat3D[n][k][j] = dat3DSets[n][k][j];
		
	    int   nsBin = (int)( (horisonCurv + nScan * avstepsiz)/ avstepsiz + 0.5);
	    array2theta = new double[nsBin];

			  twoTheta0=0.0;
				for (int k = 0; k < nsBin; k++)
					array2theta[k] = twoTheta0 + k * avstepsiz;	
	
		if (stepsize == null || (stepsize != null && stepsize[1]!=0.0)) {			
			stepsize = new double[nScan];	
		     for (int i = 0; i < nScan; i++) 
		    	 stepsize[i] = avstepsiz;
		}
		return array2theta;

	}
	/**
	 * /**
    * 	 The method is designed to have weighted valuse merging. This method follows math.
    *    u_mean = (sum_(1 -> n) w_n * x_n) / w
    *    w_n  = 1 / xErrors* xErrors 
    *    w = sum w_n
    *    Mean of xErrors = 1/ sqrt(w)
   
	 * @param inDat
	 * @param xyErrs
	 * @param xyWt
	 * @return   merged weight value of measurement.
	 */
	public double weightedMultivalueMerge (double[][] inDat, double[][] xyErrs, double[][] xyWt) {

		 int yPixl = inDat.length;
		 int xPixl = inDat[0].length;
		 double totals = 0.0;
		 double wt = 0.0;
		 if (xyErrs.length != inDat.length) return  Double.NaN;
		 
		 for (int j = 0; j < yPixl; j ++ ){
		         for (int n = 0; n< xPixl; n ++) {
			            totals += inDat[j][n] * xyWt[j][n] / (xyErrs[j][n]*xyErrs[j][n]);
			            wt += xyWt[j][n];
		           }
	       }
		 double var = weightErrorCal(xyErrs);
		  double   weightedAverage = totals * (var*var)/wt;
		 
return weightedAverage;
	
}


/**
 * This method id used to calculate average raw  measurement  to have mean of raw measurement
 * @param xMeas    input number of measurements
 * @return   reutrn the mean of  number of measurement
 */
public double meanOfRawMeasurement(double[][] xyMeas){
	    int yMeas = xyMeas.length;
	    int xMeas = xyMeas[0].length;	    
	    double meanValue= 0.0; 
	     for ( int n = 0; n < yMeas; n++)  {
	    	 for (int j = 0; j < xMeas; j ++) {	     
	    	 meanValue += xyMeas[n][j]/yMeas*xMeas;
	           }
	     }
	return meanValue;
}
/**
*
* @param xErrors
* @return
*/
public  double weightErrorCal(double[][] xErrors){
	double reviseError = 0.0;

	int yError = xErrors.length;
	int xError = xErrors[0].length;
	for ( int n = 0; n < yError; n++) {
		for (int j = 0; j < xError; j ++) {
		reviseError  += 1/(xErrors[n][j] * xErrors[n][j]);
        	}
	   }
	double weightError = 1.0 / Math.sqrt(reviseError);
	return weightError;
}


}