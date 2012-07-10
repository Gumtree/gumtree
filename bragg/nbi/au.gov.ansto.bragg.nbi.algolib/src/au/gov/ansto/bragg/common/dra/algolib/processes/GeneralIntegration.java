/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */
/**
 * @author J. G. WANG
 */

package au.gov.ansto.bragg.common.dra.algolib.processes;


import au.gov.ansto.bragg.common.dra.algolib.math.GeometryProcessor;

/**
 *OPAL Neutron Scttering software package designed to 
 *  make online data reduction.
 *
 * @author J.G.Wang
 *
 */
public class GeneralIntegration extends GeometryProcessor {
	
	
	/**
     * Finds the horizontal integration about a point.
     * @param data The data to be integrated.
     * Points where data = 0 are considered to be masked.
     * @param err An array to put the error values into.
     * @param stdDev An array to put the standard deviation values into.
     * @param numSlices How many slices should be considered.
     * @param minYi The minimum distance to consider.
     * @param maxYi The maximum distance to consider.
     * @param xOrigin
     * @param yOrigin
     * @param posi The position from the vertical to be considered as Y for the output.
     * @return The position integration.
     */
    public static float[] findGeneralIntegration(float[][] data, float[] err,
			float[] stdDev,
            int numSlices, float minDist, float maxDist, float xOrigin,
            float yOrigin, float pos) {
        if (data == null) {
            return null;
        }
        float distStep = (maxDist-minDist)/(numSlices-1);
        float[] totals = new float[numSlices];
        int height = data.length;
        int width = data[0].length;
        int x, y;
        float i;
        //float dist;
        float w;
        for (x = 0; x < totals.length; x++) {
            totals[x] = 0;
 
            if (err != null)
                err[x] = 0;
            if (stdDev != null)
                err[x] = 0;
        }
        for (i = minDist; i < maxDist; i += distStep) {
            x = (int) ( i + xOrigin);
            y = (int) ( i + yOrigin);
            if (x >= 0 && x < width && y >= 0 && y < height) {
                if (x >= data.length || y >= data[0].length
                        || filtered(data[x][y])) {
                    continue;
                }

                float swidth = height / numSlices;
                int ns;
                w = 1;
                for ( ns = 0; ns < numSlices; ns++)
                {
                if (x < ns*swidth || x >= (ns+1)*swidth) 
                	continue;
                totals[ns] += w * data[x][y];
                stdDev[ns] += w * data[x][y] * data[x][y];
                //w = dist - (float) Math.floor(dist);
                }
            }
        }
        if (err != null ) 
            for (x = 0; x < err.length; x++) {
                err[x] = (float) Math.sqrt(Math.abs(totals[x]));
            }

            for (x = 0; x < err.length; x++) {
                stdDev[x] = (float) Math.sqrt(stdDev[x]-totals[x]*totals[x]);
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
    public static float[] integrationBounds(float[][] data, float xCenter,
            float yCenter, float posi) {
        if (data == null) {
            return null;
        }
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        int height = data.length;
        int width = data[0].length;
        int x, y, i;
        float dist;
        for (i = -data.length*2; i < data.length*2; i += 1) {
            x = (int) ( i + xCenter);
            y = (int) ( i + yCenter);
            if (x >= 0 && x < width && y >= 0 && y < height) {
                if (x > data.length || y > data[0].length
                        || filtered(data[x][y])) {
                    continue;
                }
                dist = (float) ((xCenter - x) - (yCenter - y));
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
        return new float[] {min,max};
    }

	@Override
	public String getName() {
		return "Directional Average";
	}
	
	public String getDescription() {
		return "Takes a directional average. Set the angle using the spinbox.\n"+
			   "Zero angle is right, positive is anticlockwise.\n"+
			   "The position of the center determines zero distance.";
	}

	@Override
	protected Signal processNew(Signal in) {
		// TODO Auto-generated method stub
		return null;
	}
}
	
	
	
