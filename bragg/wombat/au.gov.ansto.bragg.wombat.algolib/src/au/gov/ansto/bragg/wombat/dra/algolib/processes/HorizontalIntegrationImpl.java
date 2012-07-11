package au.gov.ansto.bragg.wombat.dra.algolib.processes;

import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;
//import org.gumtree.dra.common.processes.WrapperSignal;
//import org.gumtree.vis.core.plot1d.PlotData1D;

public class HorizontalIntegrationImpl extends HIPDProcessor implements HorizontalIntegration {

	@Override
	public Signal processNew(Signal in) {
		double[][] inData = in.dataAs(double[][].class);
		double[] extremals = integrationBounds(inData, hid.beamX, hid.beamY,position);
		double[] x = new double[numSlices];
		for(int i = 0; i < x.length; i++)
			x[i] = extremals[0]+(extremals[1]-extremals[0])*(double)i/x.length;
		double[] err = new double[numSlices];
		double[] stdDev = new double[numSlices];
//		double[][] ty = findHorizontalIntegration(inData, numSlices, extremals[0], extremals[1], detector.beamX,
//				                    detector.beamY, position);
//		PlotData1D pd = new PlotData1D(x,ty,err,null,"Horizontal Integration at position y ="
//				                                      + ty +" of "+in.name());
//		return new WrapperSignal(pd,pd.name);
		return null;
	}
	
	/**
     * Makes the horizontal integration about a selected region ( mask.)
     * @param data The two D double data to be integrated.
	 * @param numSlices How many slices should be considered.
	 * @param xOrigin  The x coodinate of beam center 
	 * @param yOrigin  The y coodinate of beam center 
	 * @param pos The position from the detector to sample
	 * @param minYi  The minimum value (bottom side) for integration.
	 * @param maxYi The maximum value (top side) for integration.
	 * @param thetaVect: the detector tube 2 theta position (nScan * nTubes)
	 * @return two vector arrays totals[2][xPexels]
     *                 totals[0][xPexels]   present number of neutron in each integrated bin
     *                 totals[1][xPexels]   present value of error in each integrated bin
     *                 totals[2][numSlices]   present value of thetaVect in each integrated bin
     */
    public  double[][] findHorizontalIntegration(double[][] data, int nBins, 	double minDist,
            double maxDist, double xOrigin, double yOrigin, double pos, double[] thetaArray) {
        if (data == null) {
            return null;
        }
        int yheight = data.length;
        int xwidth = data[0].length;
        if (nBins == 0) nBins = xwidth;
        int[] nentry = new  int[xwidth];
//        System.out.println("Pixels in theta = " + xwidth);
        double[] err = new double[xwidth]; 
        double[] stdDev = new double[xwidth];   
        if (pos != 0.0) {
        	double det2sam = pos;
        }
        int x, y;
        double distStep = (double)(xwidth /nBins);
        double[] sums = new double[nBins];
        double[][] totals = new double[3][nBins];
        int i, j;
        //double dist;
        double w;
//        for (x = 0; x < totals[0].length; x++) {
//            totals[0][x] = 0;
//            totals[1][x] = 0;
//            if (err != null)
//                err[x] = 0;
//            if (stdDev != null)
//                err[x] = 0;
//        }
     for (j =0; j < yheight; j++) 
     {
        for (i = 0; i < xwidth; i++) {
            x = (int) ( i );
            y = (int) ( j );
            if (x >= 0 && x < xwidth && y >= minDist && y < maxDist) {
 //               if (y >= data.length || x >= data[0].length
 //                       || filtered(data[x][y])) {
 //                   continue;
 //               }
                //dist = (double) ((xCenter - x) + (yCenter - y));
                //dist = i;
                //dist -= minDist;
                //dist /= distStep;
                //w = 1 + (double) Math.floor(dist) - dist;
                double swidth = xwidth / nBins;
                int ns;
                w = 1;
//                for ( ns = 0; ns < numSlices; ns++)
//                {
                if (x > i*swidth || x <= (i+1)*swidth) 
                {
        	   String velem = String.valueOf(data[y][x]);
//               if(data[y][x] != Double.NaN) 
        	        if (velem.equals("NaN")){
        		          continue;		   
        	           }
        	        else  {
        		   				nentry[i]++;
                               sums[i] += w * data[y][x];             
  //              stdDev[i] += w * data[y][x] * data[y][x];
                //w = dist - (double) Math.floor(dist);
                    }
                }
            }
        }
     }
 // 	System.out.println("thetaVect.length; nBins  : " + thetaArray.length + "; " + nBins);

            for (x = 0; x < nBins; x++) {
            	if (nentry[x] != 0) totals[0][x] = sums[x];
            	else                     totals[0][x] = Double.NaN;
               totals[1][x] =  Math.sqrt(Math.abs(totals[0][x]));
               totals[2][x] = thetaArray[x];
//              	System.out.println("x and totals[0][x], Totals[1][x] = " + x + ",  " + totals[0][x] + ",  " + totals[1][x]);   
            }

//            for (x = 0; x < numSlices; x++) {
//                stdDev[x] = (double) Math.sqrt(stdDev[x]-totals[0][x]*totals[0][x]);
//            }
        
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
    public static double[] integrationBounds(double[][] data, double xCenter,
            double yCenter, double posi) {
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
            x = (int) ( i + xCenter);
            y = (int) ( i + yCenter);
            if (x >= 0 && x < width && y >= 0 && y < height) {
                if (x > data.length || y > data[0].length
                        || filtered(data[x][y])) {
                    continue;
                }
                dist = (double) ((xCenter - x) - (yCenter - y));
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
		return "Directional Average";
	}
	
	public String getDescription() {
		return "Takes a directional average. Set the angle using the spinbox.\n"+
			   "Zero angle is right, positive is anticlockwise.\n"+
			   "The position of the center determines zero distance.";
	}


}
