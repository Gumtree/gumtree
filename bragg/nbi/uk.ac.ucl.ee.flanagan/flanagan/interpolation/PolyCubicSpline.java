/**********************************************************
*
*   PolyCubicSpline.java
*
*   Class for performing an interpolation on the tabulated
*   function y = f(x1,x2, x3 .... xn) using a natural cubic splines
*   Assumes second derivatives at end points = 0 (natural spines)
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	15 February 2006
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   PolyCubicSpline.html
*
*   Copyright (c) February 2006   Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.interpolation;

import java.lang.reflect.Array;

public class PolyCubicSpline{

    	private int nDimensions = 0;   	    // number of the dimensions of the tabulated points array, y=f(x1,x2,x3 . . xn), i.e. n
    	private Object fOfX = null;         // tabulated values of y = f(x1,x2,x3 . . fn)
    	                                    // as a multidemensional array of double [x1 length][x2 length] ... [xn length]
    	private Object xArrays = null;      // The variable arrays x1, x2, x3 . . . xn
    	                                    // packed as a multidemensional array of double [][]
    	                                    // where xArrays[0] = array of x1 values, xArrays[1] = array of x2 values etc
        private double yValue = 0.0D;       // returned interpolated value

    	// Constructor
    	public PolyCubicSpline(Object xArrays, Object fOfX){

    	    this.fOfX = fOfX;
    	    this.xArrays = xArrays;

    	    // Calculate fOfX array dimension number
    	    Object internalArray = fOfX;
    	    this.nDimensions = 1;
            while(!((internalArray  =  Array.get(internalArray, 0)) instanceof Double))nDimensions++;

            // Repack xArrays as 2 dimensional array if entered a single dimensioned array for a simple cubic spline
            if(this.xArrays instanceof double[] && this.nDimensions == 1){
                double[][] xArraysTemp = new double[1][];
                xArraysTemp[0] = (double[])xArrays;
                this.xArrays = (Object)xArraysTemp;
            }
            else{
               if(!(xArrays instanceof double[][]))throw new IllegalArgumentException("xArrays should be a two dimensional array of doubles");
            }
        }

    	//  Interpolation method
    	public double interpolate(double[] unknownCoord){

    	    int nUnknown = unknownCoord.length;
    	    if(nUnknown!=this.nDimensions)throw new IllegalArgumentException("Number of unknown value coordinates, " + nUnknown + ", does not equal the number of tabulated data dimensions, " + this.nDimensions);

            int kk = 0;
            double[][] xArray = (double[][])this.xArrays;
            switch(this.nDimensions){
                case 0: throw new IllegalArgumentException("data array must have at least one dimension");
                case 1: // If fOfX is one dimensional perform simple cubic spline
                        CubicSpline cs = new CubicSpline(xArray[0], (double[])this.fOfX);
                        this.yValue = cs.interpolate(unknownCoord[0]);
                        break;
                case 2: // If fOfX is two dimensional perform bicubic spline
                        BiCubicSpline bcs = new BiCubicSpline(xArray[0], xArray[1], (double[][])this.fOfX);
                        this.yValue = bcs.interpolate(unknownCoord[0], unknownCoord[1]);
                        break;
                case 3: // If fOfX is three dimensional perform tricubic spline
                        TriCubicSpline tcs = new TriCubicSpline(xArray[0], xArray[1], xArray[2], (double[][][])this.fOfX);
                        this.yValue = tcs.interpolate(unknownCoord[0], unknownCoord[1], unknownCoord[2]);
                        break;
                default:// If fOfX is greater than three dimensional, recursively call PolyCubicSpline
                        //  with, as arguments, the n1 fOfX sub-arrays, each of (number of dimensions - 1) dimensions,
                        //  where n1 is the number of x1 variables.
                        Object obj = fOfX;
                        int dimOne = Array.getLength(obj);
                        double[] csArray = new double [dimOne];
                        double[][] newXarrays= new double[this.nDimensions-1][];
                        double[] newCoord = new double[this.nDimensions-1];
                        for(int i=0; i<this.nDimensions-1; i++){
                            newXarrays[i] = xArray[i+1];
                            newCoord[i] = unknownCoord[i+1];
                        }
                        for(int i=0; i<dimOne; i++){
                            Object objT = (Object)Array.get(obj, i);
                            PolyCubicSpline pcs = new PolyCubicSpline(newXarrays, objT);
                            csArray[i] = pcs.interpolate(newCoord);
                        }

                        // Perform simple cubic spline on the array of above returned interpolates
                        CubicSpline ncs = new CubicSpline(xArray[0], csArray);
            	    	this.yValue = ncs.interpolate(unknownCoord[0]);
            }

            return this.yValue;
    	}
}

