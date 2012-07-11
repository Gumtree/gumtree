package au.gov.ansto.bragg.echidna.dra.algolib.processes;
/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */
/**
 * @author J. G. WANG
 *
 */
import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;
//import au.gov.ansto.bragg.common.dra.algolib.processes.WrapperSignal;
//import org.gumtree.vis.core.plot1d.PlotData1D;

public class SpecialRegionIntegrationImpl extends HRPDProcessor  implements SpecialRegionIntegration{
	
	@Override
	public Signal processNew(Signal in) {
		double[][] inData = in.dataAs(double[][].class);
		double[] extremals = MaskSectionBounds(inData, detector.beamX, detector.beamY,position);
		double[] x = new double[numSlices];
		for(int i = 0; i < x.length; i++)
			x[i] = extremals[0]+(extremals[1]-extremals[0])*(double)i/x.length;
		double[] err = new double[numSlices];
		double[] stdDev = new double[numSlices];
		double[][] y = findMaskArea(inData,  numSlices, extremals[0], extremals[1], detector.beamX, detector.beamY, position);
//		PlotData1D pd = new PlotData1D(x,y,err,null,"Cross Section at "+position+" of "+in.name());
//		return new WrapperSignal(pd,pd.name);
		return null;
	}

	/**
     * Finds the cross section through a point.
     * @param data The data to be used.
     * Points where data = 0 are considered to be masked.
     * @param numSlices How many slices should be considered.
     * @param minDist The minimum distance to consider.
     * @param maxDist The maximum distance to consider.
     * @param xCenter The beam center X coordinate.
     * @param yCenter The beam center Y coordinate.
     * @param dir The angle from the vertical to be considered as X for the output.
    * @return two vector arrays totals[2][xPexels]
     *                 totals[0][xPexels]   present number of neutron in each integrated bin
     *                 totals[1][xPexels]   present value of error in each integrated bin
     *                 totals[2][numSlices]   present value of thetaVect in each integrated bin
     */
    public  double[][] findMaskArea(double[][] data,
            int numSlices, double minDist, double maxDist, double xCenter,
            double yCenter, double dir) {
        if (data == null) {
            return null;
        }
        double distStep = (maxDist-minDist)/(numSlices-1);
        double[][] totals = new double[2][numSlices];
        double[] weight = new double[numSlices];
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
                w = 1 + (double) Math.floor(dist) - dist;
                weight[(int) Math.floor(dist)] += w;
                totals[0][(int) Math.floor(dist)] += w * data[x][y];
                stdDev[(int) Math.floor(dist)] += w * data[x][y] * data[x][y];
                w = dist - (double) Math.floor(dist);
                weight[(int) Math.min(Math.ceil(dist), numSlices - 1)] += w;
                totals[0][(int) Math.min(Math.ceil(dist), numSlices - 1)] += w
                        * data[x][y];
                stdDev[(int) Math.min(Math.ceil(dist), numSlices - 1)] += w
                * data[x][y]* data[x][y];
            }
        }
        if (err != null)
            for (x = 0; x < numSlices; x++) {
                totals[1][x] = (double) Math.sqrt(Math.abs(totals[0][x]));
            }
        inPlaceArrayDivide(totals[0], weight);
        if (err != null)
            inPlaceArrayDivide(err, weight);
		if(stdDev != null)
		{
	        inPlaceArrayDivide(stdDev, weight);
            for (x = 0; x < numSlices; x++) {
                stdDev[x] = (double) Math.sqrt(stdDev[x]-totals[0][x]*totals[0][x]);
            }
		}
        return totals;
    }
    
    /**
     * Finds the min and max values at which the cross section will be
     * in the data set and not masked.
     * @param data The data set, with Float.NaN representing masked points
     * @param xCenter The X coordinate of the center to take the cross section
     * through
     * @param yCenter The Y coordinate of the center to take the cross section
     * through
     * @param dir The direction to take the cross section.
     * @return An array containing  {min, max}
     */
    public static double[] MaskSectionBounds(double[][] data, double xCenter,
            double yCenter, double dir) {
        if (data == null) {
            return null;
        }
        double min = Float.POSITIVE_INFINITY;
        double max = Float.NEGATIVE_INFINITY;
        int height = data.length;
        int width = data[0].length;
        int x, y, i;
        double dist;
        for (i = -data.length*2; i < data.length*2; i += 1) {
            x = (int) (Math.cos(dir) * i + xCenter);
            y = (int) (-Math.sin(dir) * i + yCenter);
            if (x >= 0 && x < width && y >= 0 && y < height) {
                if (x > data.length || y > data[0].length
                        || filtered(data[x][y])) {
                    continue;
                }
                dist = (double) ((xCenter - x)  - (yCenter - y));
                if(dist < min)
                {
                    min = dist;
                }
                if(dist > max)
                {
                    max = dist;
                }
            }
        }
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
}
