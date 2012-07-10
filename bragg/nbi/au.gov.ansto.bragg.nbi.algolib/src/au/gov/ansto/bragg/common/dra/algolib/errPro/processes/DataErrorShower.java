/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */
package au.gov.ansto.bragg.common.dra.algolib.errPro.processes;

/**
 * @author jgw Tel: +61 2 9717 7062 Fax: +61 2 9717 9799 Data Analysis Team,
 *         Bragg Institute,Bld.82 ANSTO PMB 1 Menai NSW 2234 AUSTRALIA
 */
public class DataErrorShower {

	/**
	 * @uml.property name="dataErrorPropagation"
	 * @uml.associationEnd inverse="dataErrorShower:process.DataErrorPropagation"
	 * @uml.association name="data export"
	 */
	private DataErrorPropagation dataErrorPropagation;

	/**
	 * Getter of the property <tt>dataErrorPropagation</tt>
	 * 
	 * @return Returns the dataErrorPropagation.
	 * @uml.property name="dataErrorPropagation"
	 */
	public DataErrorPropagation getDataErrorPropagation() {
		return dataErrorPropagation;
	}

	/**
	 * Setter of the property <tt>dataErrorPropagation</tt>
	 * 
	 * @param dataErrorPropagation
	 *            The dataErrorPropagation to set.
	 * @uml.property name="dataErrorPropagation"
	 */
	public void setDataErrorPropagation(
			DataErrorPropagation dataErrorPropagation) {
		this.dataErrorPropagation = dataErrorPropagation;
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
