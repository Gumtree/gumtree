package au.gov.ansto.bragg.echidna.dra.algolib.processes;

import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;
import au.gov.ansto.bragg.common.dra.algolib.processes.WrapperSignal;
//import org.gumtree.vis.core.plot1d.PlotData1D;

import au.gov.ansto.bragg.echidna.dra.algolib.entity.HRPDDetector;

public class GlobalIntegrationImpl extends HRPDProcessor implements GlobalIntegration {

	@Override
	public Signal processNew(Signal in) {
		double[][] inData = in.dataAs(double[][].class);
		double[] extremals = integrationBounds(inData, detector.beamX, detector.beamY,position);
		double[] x = new double[numSlices];
		for(int i = 0; i < x.length; i++)
			x[i] = extremals[0]+(extremals[1]-extremals[0])*(float)i/x.length;
		float[] err = new float[numSlices];
		float[] stdDev = new float[numSlices];
//		double[][] dind = (double)inData;
//		float[][] ty = findGlobalIntegration(inData, numSlices);
//		PlotData1D pd = new PlotData1D(x,ty,err,null,"Horizontal Integration at position y ="+ ty +" of "+in.name());
//		return new WrapperSignal(pd,pd.name);
		return null;
	}
	
	/**
     * Finds the global integration about a point.
     * @param data The data to be integrated.
	 * @param numSlices How many slices should be considered.
	 * @return two vector arrays totals[2][xPexels]
     *                 totals[0][xPexels]   present number of neutron in each integrated bin
     *                 totals[1][xPexels]   present value of error in each integrated bin
     *                 totals[2][numSlices]   present value of thetaVect in each integrated bin
     */
    public  double[][] findGlobalIntegration(double[][] data, int numSlices, double[] thetaVect ) {
        if (data == null) {
            return null;
        }
        HRPDDetector hd = new HRPDDetector();
        int yheight = data.length;
        int xwidth = data[0].length;
        int nTubes = hd.count;
        int maxDist = xwidth;      
        int minDist = 0;
        float[] err = new float[xwidth]; 
        float[] stdDev = new float[xwidth];     
        if (numSlices == 0 ) numSlices = xwidth;
        float distStep  = (maxDist-minDist)/numSlices;
        double[][] totals = new double[3][numSlices];
        float xOrigin = 0.0F;
        float yOrigin = 0.0F;
        int x, y;
        int i,j;
        //float dist;
        float w;
        for (x = 0; x < totals.length; x++) {
            totals[0][x] = 0;
            totals[1][x] = 0;
            totals[2][x] = 0;
            if (err != null)
                err[x] = 0;
            if (stdDev != null)
                err[x] = 0;
        }

        for (i = minDist; i < maxDist; i+=distStep) {
            x = (int) ( i + xOrigin);
            y = (int) ( i + yOrigin);
//            if (y >= 0 && y < yheight ) {
//                if (x > xwidth  || filtered(data[x][y])) {
//                    continue;
//                }
                //dist = (float) ((xCenter - x) + (yCenter - y));
                //dist = i;
                //dist -= minDist;
                //dist /= distStep;
                //w = 1 + (float) Math.floor(dist) - dist;
                float swidth =  xwidth / numSlices;
                int ns;
                w = 1;
//                for ( ns = 0; ns < numSlices; ns++)
//                {
                if (i >= i*swidth && i < (i+1)*swidth) 
                    for (j = 0; j < yheight; j++)
                    {               	
                totals[0][i] += w * data[y][x];
 //               stdDev[i] += w * data[y][x] * data[y][x];
                //w = dist - (float) Math.floor(dist);
  //              }
  //                 }
              }
         }
        if (err != null ) 
            for (x = 0; x < err.length; x++) {
                totals[1][x] =  Math.sqrt(Math.abs(totals[0][x]));
                totals[2][x] =  thetaVect[x];
            }

//            for (x = 0; x < err.length; x++) {
//                stdDev[x] = (float) Math.sqrt(stdDev[x]-totals[0][x]*totals[0][x]);
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
