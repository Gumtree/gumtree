package au.gov.ansto.bragg.wombat.dra.algolib.processes;

import au.gov.ansto.bragg.common.dra.algolib.math.FPoint;
import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;
//import org.gumtree.dra.common.processes.WrapperSignal;
//import org.gumtree.vis.core.plot1d.PlotData1D;

public class SquareRegionIntegrationImpl extends HIPDProcessor implements SquareRegionIntegration {

	@Override
	public Signal processNew(Signal in) {
		SquareRegionIntegrationImpl ssm = new SquareRegionIntegrationImpl();
		double[][] inData = in.dataAs(double[][].class);
		 FPoint startpoint=null,endpoint = null;
		double[] extremals = integrationBounds(inData, hid.beamX, hid.beamY,position);
		double[] x = new double[numSlices];
		for(int i = 0; i < x.length; i++)
			x[i] = extremals[0]+(extremals[1]-extremals[0])*(double)i/x.length;
		double[] err = new double[numSlices];
		double[] stdDev = new double[numSlices];
		double[][] ty = ssm.findSquareMask(inData, numSlices, startpoint, endpoint, null );
//		PlotData1D pd = new PlotData1D(x,ty,err,null,"Horizontal Integration at position y ="+ ty +" of "+in.name());
//		return new WrapperSignal(pd,pd.name);
		return in;
	}
	
	/**
     * Finds the square integration about a mask.
     * @param data The data to be integrated.
	 * @param numSlices How many slices should be considered.
	 * @param startpoint The atart point to consider with two d point (x1,y1).
	 * @param endpoint  the end point to consider  with two d point (x2,y2).
	 *
	 * @param thetaVect: the theta vector for each detector tube.
	 * @return two vector arrays totals[2][xPexels]
     *                 totals[0][xPexels]   present number of neutron in each integrated bin
     *                 totals[1][xPexels]   present value of error in each integrated bin
     *                 totals[2][numSlices]   present value of thetaVect in each integrated bin
     */
    public  double[][] findSquareMask(double[][] data, int numSlices,
			FPoint startpoint,FPoint endpoint, double[] thetaVect   ) {
        if (data == null) {
            return null;
        }

        int yheight = data.length;
        int xwidth = data[0].length;
        double xOrigin = 0.0;
        double yOrigin = 0.0;
        int minX  = (int)Math.min(startpoint.x,endpoint.x);
        int maxX = (int) Math.max(startpoint.x,endpoint.x);
        int minY  = (int) Math.min(startpoint.y,endpoint.y);
        int maxY =  (int) Math.max(startpoint.y,endpoint.y);    
        int   xBins =numSlices;
        int distStep = (maxX-minX)/(xBins-1);
        double[][] totals = new double[3][xwidth];
        double[] err = new double[xwidth]; 
        double[] stdDev = new double[xwidth];   
        double[] sums  = new double[xwidth];   
        int[]  nentry = new int[xwidth];   
 //       System.out.println("Input Data  and Theta Vector  length: " + xwidth + "; " + thetaVect.length);
        int x, y;
//        int  i,j;
        //double dist;
        double w;
        for (int l = 0; l < totals[0].length; l++) {                  //horizontal loop
            totals[0][l] = 0;
 
            if (err != null)
                err[l] = 0;
            if (stdDev != null)
                err[l] = 0;
        }
        
 
//        for (int i = minX; i < maxX; i++) {
//            x = (int) ( i + xOrigin);
//            y = (int) ( i + yOrigin);
//            if (y >= 0 && y < yheight ) {
//                if (x >= data[0].length  || filtered(data[x][y])) {
//                    continue;
//                }
                //dist = (double) ((xCenter - x) + (yCenter - y));
                //dist = i;
                //dist -= minX;
                //dist /= distStep;
                //w = 1 + (double) Math.floor(dist) - dist;
                double swidth =  (maxX-minX) / xBins;
                int n;
                w = 1;

    // integration data to xBins ....
//                for ( ns = 0; ns < xBins; ns++)
//                {
//                if (x < minX+ns*swidth || x >= minX+(ns+1)*swidth)   //select xBin
//               	continue;
       System.out.println( "(minX, minY); (maxX, maxY)=   (" + minX + "," + minY+ "); (" +maxX +", "+maxY+")");    
  
       for (int k = 0; k < xwidth; k++ ) {                   //horizontal loop
 //                 x    =k  + (int)xOrigin;
           
           if (k >= minX && k < maxX )
         { 
               for ( int j = 0; j < yheight; j++) 		//Vertical loop
                  {
//            	To limit integration in the required box
//                   y   =  j  + (int)yOrigin;   	
 
 //                  System.out.println( "x, y = " + x +", " + y);  
 //                  System.out.println( "(minX, minY); (minX, minY)=   (" + minX + "," + minY+ "); (" +minX +", "+minY+")"); 
                   
            if (j >= minY && j < maxY )
                {       
         	   String velem = String.valueOf(data[j][k]);
//             if(data[y][x] != Double.NaN) 
      	      if (velem.equals("NaN")){
      		          continue;		   
      	         }
      	        else  {
		   				nentry[k]++;
                        sums[k] += w * data[j][k];    
                        stdDev[k] += w * data[j][k] * data[j][k];  
                         }
                  } 
               }
         }
         else  {
                    totals[0][k] = Double.NaN;
                    stdDev[k] = Double.NaN;  
                      
                }
                //w = dist - (double) Math.floor(dist);
//                }

//            }
//        }
      
   }

            for (x = 0; x < thetaVect.length; x++) {
            	if (nentry[x] != 0) totals[0][x] = sums[x];
            	else                     totals[0][x] = Double.NaN;
                totals[1][x] =  Math.sqrt(Math.abs(totals[0][x]));
  //              int l = minX + x;
                totals[2][x] =thetaVect[x];
            }

            for (x = 0; x < thetaVect.length; x++) {
                stdDev[x] =  Math.sqrt(stdDev[x]-totals[0][x]*totals[0][x]);
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
