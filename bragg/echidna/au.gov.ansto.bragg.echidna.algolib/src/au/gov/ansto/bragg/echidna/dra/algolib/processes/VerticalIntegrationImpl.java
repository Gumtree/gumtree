package au.gov.ansto.bragg.echidna.dra.algolib.processes;

import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;

public class VerticalIntegrationImpl extends HRPDProcessor implements VerticalIntegration {

	@Override
	public Signal processNew(Signal in) {
		double[][] inData = in.dataAs(double[][].class);
		double[] extremals = verticalBoxBounds(inData, detector.beamX, detector.beamY, position);
		double[] x = new double[numSlices];
		for(int i = 0; i < x.length; i++)
			x[i] = extremals[0]+(extremals[1]-extremals[0])*(double)i/x.length;
		double[] err = new double[numSlices];
		double[] stdDev = new double[numSlices];
		double[][] y = findVerticalIntegration(inData, numSlices, extremals[0], extremals[1], detector.beamX,
				                  detector.beamY, position, null);
//		PlotData1D pd = new PlotData1D(x,y,err,null,"Vertical integration at "+ position
//				                        + " of "+in.name());
//		return new WrapperSignal(pd,pd.name);
		return null;
	}
	
	/**
     * Finds the directional averages about a point.
     * @param data The data to be averaged.
	 * @param numSlices How many slices should be considered.
	 * @param minDist The minimum distance to consider.
	 * @param maxDist The maximum distance to consider.
	 * @param xOrigin
	 * @param yOrigin
	 * @param dir The angle from the vertical to be considered as X for the output.
	 * @return two vector arrays totals[2][xPexels]
     *                 totals[0][xPexels]   present number of neutron in each integrated bin
     *                 totals[1][xPexels]   present value of error in each integrated bin
     *                 totals[2][numSlices]   present value of thetaVect in each integrated bin
     */
    public double[][] findVerticalIntegration(double[][] data, int numSlices,
			double minDist,
            double maxDist, double xOrigin, double yOrigin, double pos, double[] thetaVect) {
        if (data == null) {
            return null;
        }
        int yheight = data.length;
        int xwidth = data[0].length;
        if (numSlices == 0) numSlices = xwidth;
        double xStepvalue = (maxDist-minDist)/(numSlices-1);
        int[] nentry =  new  int[xwidth];       
        double[] sums = new double[xwidth];       
        double[][] totals = new double[3][xwidth];
 

        double[] err = new double[xwidth]; 
        double[] stdDev = new double[xwidth];       
        int x, y;
//        int i,j;
        double dist;
        double w;
        int leftValue = (int) minDist;
        for (x = 0; x < xwidth; x++) {
            totals[0][x] = 0;
            totals[1][x] = 0;
            totals[2][x] = 0;
            if (err != null)
                err[x] = 0;
            if (stdDev != null)
                err[x] = 0;
        }
        for (int j =0; j < yheight; j++) 
        {
           for (int n  = 0; n < xwidth; n++) {
               x = (int) ( n + xOrigin);
               y = (int) ( j + yOrigin);
   
            	   
 /*              if (y >= data.length || x >= data[0].length
  *  //                       || filtered(data[x][y])) {
  *  //                   continue;
  *  //               }
  *                 //dist = (double) ((xCenter - x) + (yCenter - y));
  *                 //dist = i;
  *                 //dist -= minDist;
  *                 //dist /= distStep;
  *                 //w = 1 + (double) Math.floor(dist) - dist;
  * 
  */

//                   int ns;
                  w = 1;
//                   for ( ns = 0; ns < numSlices; ns++)
//                   {
//                   if (x < leftValue + ns*xStepvalue || x >=  leftValue + (ns+1)*xStepvalue) 
//                   	continue;
//                   totals[0][ns] += w * data[y][x];
//                   stdDev[ns] += w * data[y][x] * data[y][x];
//                   //w = dist - (double) Math.floor(dist);
//                   }
                   if ((n >= minDist && n < maxDist ) )
                   {
                	   String velem = String.valueOf(data[y][x]);
//                     if(data[y][x] != Double.NaN) 
              	       if (velem.equals("NaN")){
              		          continue;		   
              	          }
              	        else{
                         totals[0][n] += w * data[j][n];
                         stdDev[n] += w * data[j][n] * data[j][n];
                               } 
                   }
                   else  {
                       totals[0][n] = Double.NaN;
                       stdDev[n] = Double.NaN;
                   }

           }
        }
 
            for (x = 0; x < xwidth; x++) {
            	if (nentry[x] != 0) totals[0][x] = sums[x];
            	else                     totals[0][x] = Double.NaN;
                totals[1][x] = (double) Math.sqrt(Math.abs(totals[0][x]));
 //               int l = leftValue + x;
                totals[2][x] =thetaVect[x];
            }

            for (x = 0; x < xwidth; x++) {
                stdDev[x] = (double) Math.sqrt(stdDev[x]-totals[0][x]*totals[0][x]);
            }
		
        return totals;
    }
 /*   
    private static double inParallelSlice(double x1, double y1, double x2, double y2, double ox, double oy)
    {
    	double sx = -oy;
    	double sy = ox;

    	double s1 = underLine(x1,y1,x2,y2,sx,sy);
    	double s2 = underLine(x1-ox,y1-oy,x2-ox,y2-oy,sx,sy);
    	return Math.abs(s1-s2);
    }
    
    /**
     * Finds the min and max values at which the directional average will be
     * in the data set and not masked.
     * @param data The data set, with Float.NaN representing masked points
     * @param xCenter The X coordinate of the center to take the average
     * through
     * @param yCenter The Y coordinate of the center to take the average
     * through
     * @param dir The direction to take the average.
     * @return An array containing  {min, max}
     */
    public static double[] verticalBoxBounds(double[][] data, double xCenter,
            double yCenter, double posi) {
        if (data == null) {
            return null;
        }
        double min = Float.POSITIVE_INFINITY;
        double max = Float.NEGATIVE_INFINITY;
        int height = data.length;
        int width = data[0].length;
        int x, y;
        double dist;
        for (x = 0; x < width; x++) {
            for (y = 0; y < height; y++) {
                if (x >= data.length || y >= data[0].length
                        || filtered(data[x][y])) {
                    continue;
                }
                dist = -(double) ((x - xCenter) + (yCenter - y));
                if(dist > max)
                {
                    max = dist;
                }
                if(dist < min)
                {
                    min = dist;
                }
            }
        }
        return new double[] {min,max};
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
}
