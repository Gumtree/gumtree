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
 *
 */
public class QTwoThetaConvention {

	/**
	 * 
	 * @param data         One D array input data
	 * @param lambda    Wavelenght  related to input oneD array data[i]
	 * @param theta0      initial theta0 for first detector tube
	 * @param dtheta      delta theta between two detectors
	 * @return  neutq      A return varible which contain two verctors
	 *                                neutq[0][i]  give Q verctor
	 *                                neutq[1][i]  give  number of neutrons respect to  Q[i] value
	 */
	public double [][] theta2QConvertion (double[] data, double  lambda, double theta0, double dtheta ) {
		double[][] neutq  = new double [2] [data.length];
		double[] xpixels  = new double[data.length];
		double[] xTheta  = new double[data.length];		

	try {
		for (int i = 0; i < data.length; i++ ) {
			 xpixels[i] = (double) i;
			 xTheta[i] = (theta0 + xpixels[i] * dtheta)/2;
			 neutq[0][i] = qCalculator(lambda, xTheta[i]);
			 neutq[1][i] = data[i];	
		  }
	}catch(Exception e) {
			e.printStackTrace();
		   }
  
		return neutq;
		
	}
	
private  static double  qCalculator(double  lambda, double theta) {
	  
	  double qvalu =0.0;
	  double rtheta = theta * Math.PI/180;
	try {  
	    if (lambda < 10E-10 )
	    	return 0;
	        qvalu = 4.0 * Math.PI * Math.sin(rtheta) / lambda;
	}catch (Exception e) {
		e.printStackTrace();
	}
	  return qvalu;
   } 

 /**
	 * 
	 * @param data            One D array input data
	 * @param lambda       Wavelenght  related to input oneD array data[i]
	 * @param thetavect    theta vector, input vector values. the dimension should be same as data.length
	 * @return  neutq         A return varible which contain two vectors
	 *                                    neutq[0][i]  give Q vector
	 *                                    neutq[1][i]  give  number of neutrons respect to  Q[i] value
	 */ 
	public double [][] theta2QConvertion (double[] data, double  lambda,  double[] thetavect ) {
		double[][] neutq  = new double [2] [data.length];
		double[] xpixels  = new double[data.length];
		double[] xTheta  = new double[data.length];		

	try {
		for (int i = 0; i < data.length; i++ ) {
			 xpixels[i] = (double) i;

			 xTheta[i] = thetavect[i]/2;
			 neutq[0][i] = qCalculator(lambda, xTheta[i]);
			 neutq[1][i] = data[i];	
		  }
	}catch(Exception e) {
			e.printStackTrace();
		   }
  
		return neutq;
		
	}
	
	/**
	 * 
	 * Theta to Q calculation without data set. Input with lambda and theta vector
	 * @param lambda       Wavelenght  related to input oneD array data[i]
	 * @param thetavect    theta vector, input vector values. the dimension should be same as data.length
	 * @return  neutq         A return varible which contain two verctors
	 *                                    neutq[0][i]  give Q verctor
	 *                                    neutq[1][i]  give  number of neutrons respect to  Q[i] value
	 */ 
	public double [] theta2QConvertion ( double  lambda,  double[] thetavect ) {

		double[] xpixels  = new double[thetavect.length];
		double[] xTheta  = xpixels;		
		double[] neutq  = new double [thetavect.length];
	try {
		for (int i = 0; i < thetavect.length ; i++ ) {
			 xpixels[i] = (double) i;

			 xTheta[i] = thetavect[i]/2;
			 neutq[i] = qCalculator(lambda, xTheta[i]);

		  }
	}catch(Exception e) {
			e.printStackTrace();
		   }
  
		return neutq;
		
	}
}

