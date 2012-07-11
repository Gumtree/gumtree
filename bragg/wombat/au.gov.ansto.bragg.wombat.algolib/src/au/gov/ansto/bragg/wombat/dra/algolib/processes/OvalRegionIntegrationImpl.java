package au.gov.ansto.bragg.wombat.dra.algolib.processes;
/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */


import au.gov.ansto.bragg.common.dra.algolib.math.FPoint;
import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;
//import au.gov.ansto.bragg.common.dra.algolib.processes.WrapperSignal;
//import org.gumtree.vis.core.plot1d.PlotData1D;

/**
 * @author jgw
 *
 */
public class OvalRegionIntegrationImpl extends HIPDProcessor implements OvalRegionIntegration {


	/**
     * Finds the oval region through pixel by pixel..
     * @param data The data to be used.
     * Points where data = 0 are considered to be masked.
	 * @param numSlices How many slices should be considered.
	 * @param startpoint The atart point to consider.
	 * @param endpoint  the end point to consider.
	 * @param xCenter The beam center X coordinate.
	 * @param yCenter The beam center Y coordinate.
	 * @param  pos   the distance from sample to detector
	 * @param thetaVect  two theta vector for wombat detectors.
     * @return two vector arrays totals[2][xPexels]
     *                 totals[0][xPexels]   present number of neutron in each integrated bin
     *                 totals[1][xPexels]   present value of error in each integrated bin
     */
    public  double[][] ovalMaskRegion (double[][] data, int numSlices,	FPoint startpoint,
            FPoint endpoint, double xCenter,   double yCenter,  double pos, double[] thetaVect) {
        if (data == null) {
            return null;
        }
        double minDist =Math.min(startpoint.x,endpoint.x);
        double maxDist =Math.max(startpoint.x,endpoint.x);
 
        double distStep = (maxDist-minDist)/(numSlices-1);
        double[][] totals = new double[2][numSlices];
        double[] weight = new double[numSlices];
        double[] ybound = new double[1];
        int height = data.length;
        int width = data[0].length;
        double[] err = new double[width]; 
        double[] stdDev = new double[width];       
        int x, y;
        double iterate;
        double dist;
        double w;
        for (x = 0; x < totals.length; x++) {
            totals[0][x] = 0;
            totals[1][x] = 0;
            weight[x] = 0;
            if (err != null)
                err[x] = 0;
            if (stdDev != null)
                err[x] = 0;
        }
        for (iterate = minDist; iterate < maxDist; iterate += distStep) {
            x = (int) xCenter;
            y = (int) yCenter;
            if (x >= 0 && x < width && y >= 0 && y < height) {
                if (x >= data.length || y >= data[0].length
                        || filtered(data[x][y])) {
                    continue;
                }
                dist = (double) ((xCenter - x)  + (yCenter - y) );
                dist += iterate;
                dist -= minDist;
                dist /= distStep;
//                w = 1 + (double) Math.floor(dist) - dist;
                w = 1 ;
                for (x=0; x<width; x++)
                {
                	if(x > (minDist + iterate * distStep) && x <  (minDist +( iterate+1) * distStep))
                	{
                		ybound = MaskOvalAreaBounds(x, startpoint, endpoint, xCenter,  yCenter, thetaVect) ;
                		double  yb1 = ybound[0];
                		double  yb2 = ybound[1];
                		
                	for (y=0; y<height; y++) {
                		if (y >yb1 && y<= yb2)
                	     totals[0][(int)iterate] += w * data[x][y];
                	}
                stdDev[(int) Math.floor(dist)] += w * data[x][y] * data[x][y];
                w = dist - (double) Math.floor(dist);
                weight[(int) Math.min(Math.ceil(dist), numSlices - 1)] += w;
                stdDev[(int) Math.min(Math.ceil(dist), numSlices - 1)] += w
                * data[x][y]* data[x][y];
                }
                }
            }
        }
        if (err != null)
            for (x = 0; x < err.length; x++) {
                totals[1][x] = (double) Math.sqrt(Math.abs(totals[0][x]));
            }
        inPlaceArrayDivide(totals[0], weight);
        if (err != null)
            inPlaceArrayDivide(err, weight);
		if(stdDev != null)
		{
	        inPlaceArrayDivide(stdDev, weight);
            for (x = 0; x < err.length; x++) {
                stdDev[x] = (double) Math.sqrt(stdDev[x] - totals[0][x]*totals[0][x] );
            }
		}
        return totals;
    }
    
    /**
     * Finds the min and max values at which the oval area will be
     * in the data set and not masked.
     * @param data The xPosi set, horisontal position in oval area
     * @param xCenter The X coordinate of the center to take the cross section
     * through
     * @param yCenter The Y coordinate of the center to take the cross section
     * through
     * @param dir The direction to take the cross section.
     * @return An array containing  {min, max}
     */
    public static double[] MaskOvalAreaBounds(double xPosi, FPoint startpoint,   FPoint endpoint, 
    		double xCenter,  double yCenter, double[] thetaVect) {
   
        double min = Float.POSITIVE_INFINITY;
        double max = Float.NEGATIVE_INFINITY;
  
    
        
        double   xOvalCenter  = (endpoint.x  + startpoint.x)/2;
        double   yOvalCenter  = (endpoint.y  + startpoint.y)/2;       

        double   xAhalf = Math.abs(endpoint.x - startpoint.x)/2;
        double   yBhalf = Math.abs(endpoint.y - startpoint.y)/2;
        
        min = yOvalCenter - (double)Math.sqrt( xAhalf*xAhalf*yBhalf*yBhalf 
        																	- (xPosi - xOvalCenter) * (xPosi - xOvalCenter) );
        
        max = yOvalCenter  +  (double)Math.sqrt( xAhalf*xAhalf*yBhalf*yBhalf 
				- (xPosi - xOvalCenter) * (xPosi - xOvalCenter) );
        
            
        return new double[] {min,max};
    }
    
	@Override
	public String getName() {
		return "Mask Integration";
	}
	
	public String getDescription() {
		return "Takes a mask section. Set the area using the quatroangle.\n"+
		       "Zero  is center, positive is in the right.\n"+
		       "The position of the center determines zero distance.";
	}

	@Override
	protected Signal processNew(Signal in) {
		// TODO Auto-generated method stub
		return null;
	}



}
