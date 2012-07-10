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
 * Tel: +61 2 9717 7062 Fax: +61 2 9717 9799 Data Analysis Team,
 *         Bragg Institute,Bld.82 ANSTO PMB 1 Menai NSW 2234 AUSTRALIA
 */
public class DataErrorPropagation {

	/**
	 * @uml.property name="processor"
	 */
	private String processor = "";

	/**
	 * Getter of the property <tt>processor</tt>
	 * 
	 * @return Returns the processor.
	 * @uml.property name="processor"
	 */
	public String getProcessor() {
		return processor;
	}

	/**
	 * Setter of the property <tt>processor</tt>
	 * 
	 * @param processor
	 *            The processor to set.
	 * @uml.property name="processor"
	 */
	public void setProcessor(String processor) {
		this.processor = processor;
	}

	/**
	 * @param inExpDat    Raw experimental data 
	 * @param  errArray,   optional parameter  in case inExpDat are corrected using other algorithms.
	 * @param thetaVect   Theta verctor for each detector tube and scan.
	 * @param emParam  emplify param 1:1 or 1:2 ....
	 * @return
	 */
	public double[][] errorProNOcorrection(double[][] inExpDat, double[][] errArray,
			double[][] thetaVect, double emParam) {

		double [][] errCal = null;
		int xPixels = inExpDat[0].length;
		int yPixels = inExpDat.length;
		
		if  (errArray != null)  errCal  = errArray;
		else {
			for( int j = 0; j <  yPixels; j++) {
				for( int k = 0; k<  xPixels; k++) {	
			
			      errCal[j][k] = Math.sqrt(inExpDat[j][k]);
			}
		}
		
		}
		
		return errCal;
	}
	/**
	 * @param inExpDat    integrated data and thetaVect  ( with error optional)
	 * @param  errArray,   optional parameter  in case inExpDat are corrected using other algorithms.
	 * @param emParam  emplify param 1:1 or 1:2 ....
	 * @param exprtForm  dat export format:
	 * 	                                  0:   with  data +- err
	 *                                    1:  with   dat + err  and data - err
	 * @return
	 */
	public double[][] errorProIntedData(double[][] IntedDat, 	double[] errArray, 
			                                                               double emParam, int exportForm) {
		double [] errCal = null;

		int xPixels = IntedDat[0].length;
		int yPixels = IntedDat.length;
		double[][] datErr = new double[3][xPixels];
		
		if  (errArray != null)  errCal  = errArray;
		else
			   errCal = IntedDat[1];
		
				for( int k = 0; k<  xPixels; k++) {	
			
			      datErr[0][k] = IntedDat[0][k] + IntedDat[1][k] ;
			      datErr[1][k] = IntedDat[0][k] - IntedDat[1][k] ;	
			      datErr[2][k] = IntedDat[2][k];
			}
	
		if(exportForm == 0) 
				return IntedDat;
		else if (exportForm == 1)
			return datErr;
		else return null;
	}
}
