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

package au.gov.ansto.bragg.wombat.dra.algolib.processes;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

//import org.eclipse.swt.SWT;
//import org.eclipse.swt.layout.FillLayout;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Shell;
import au.gov.ansto.bragg.common.dra.algolib.math.GeometryCorrecter;
import au.gov.ansto.bragg.common.dra.algolib.math.GeometryCorrecter.FPoint;
import au.gov.ansto.bragg.common.dra.algolib.processes.Processor;
import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;
import au.gov.ansto.bragg.common.dra.algolib.processes.WrapperSignal;
//import org.gumtree.vis.core.plot1d.PlotData1D;
//import org.gumtree.vis.ui.twodplot.TwoDVis;
//import org.gumtree.vis.ui.twodplot.VisualiseTwoD;
import au.gov.ansto.bragg.wombat.dra.algolib.entity.HIPDDataSet;
import au.gov.ansto.bragg.wombat.dra.algolib.entity.HIPDDetector;
/**
 *OPAL Neutron Scttering software package designed to 
 *  make online data reduction.
 *
 * @author J.G.Wang
 *
 */
public class HIPDGeometryCorrectImpl extends Processor implements HIPDGeometryCorrect {

//	protected int xPixels = 128;
//	protected int yPixels = 512;
//	protected int nScan = 10;
//	protected static double detarc = 158.75;
	protected HIPDDetector detector = new HIPDDetector();
	protected GeometryCorrecter ac = null;
	protected float pos = 0;
	protected double dy = detector.pixelHeight;
	public Signal process(Signal in) {
		HIPDDataSet data = in.dataAs(HIPDDataSet.class);
		//float[][] inData = in.dataAs(float[][].class);
		//HIPDGeometryCorrection metry = new HIPDGeometryCorrection();
		//float[] err = new float[xPixels];
		//float[] dev = new float[xPixels];
		if(data.corrected != null && data.detector == detector)
			return new WrapperSignal(data.corrected, data.name);
		if(data == null)
			throw new NullPointerException("ProcessNew given null data!");
		try{
		boolean newGeom = false;
		float detectArch = 0;
		double[] thetaVect = null;
		//float[][] corrected = correctGeometry(data, err, dev,  pos);
		double[][] corrected = correctGeometry(data.sample, data.geometry,  detectArch,  thetaVect,  newGeom, null );
		data.corrected = corrected;
		data.detector = detector;
		return new WrapperSignal(corrected, data.name);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new WrapperSignal(data, "Error - Uncorrected Data");
		}
	}

	

//    public double[][] correctGeometry(HIPDDataSet data, double[] err,
//			double[] stdDev, double pos) {

	/**
	 * Apply for geometry correction for HIPD curvered detector
	 * @param iSample  input 2D array data set after stitching (nTubes*nScan)*yPixels 
	 * @param geometry  in case new detector setup with some new geometery parameters
	 *                   if there is no change, just input "null"
	 * @param thetaVect   OneD array theta vector which presents ditector tube position with " degree"
	 * @param Geom    Always true!
	 * @param Zpvertic  Two D position array.  Vertical z position for each element.
	 * @param detectArc Curved detecter arch length with degree, ex. HIPD detector detectArch = 158.75;
	 * @return Two dimensional data table
	 */
    public double[][] correctGeometry(double[][] iSample,  double[][] geometry, double detectArch, double[] thetaVect, boolean Geom, double[][] Zpvertic)
    {   		
//		Display d = Display.getDefault();
//		Shell s = new Shell(d);
        int verPixels = iSample.length;
        int horiPixels = iSample[0].length;
 //       int nTubes = thetaVect.length;
 //       int nScan = horiPixels / nTubes;
//		s.setLayout(new FillLayout());
//		VisualiseTwoD v = new TwoDVis(s,SWT.NONE);
 
    	int mScanxPixels = horiPixels;
 //   	double mdtheta = detectArch / mScanxPixels;
    	int thlen = thetaVect.length;
  //  	double[] dtheta = new double [thlen];
    	double dtheta = 0.0;
 
    	for(int n = 0; n < thlen-1; n++)
    	   dtheta += Math.abs((thetaVect[n + 1] -thetaVect[n])) / (thlen-1);
    	
    	double mdtheta = dtheta * ((Math.PI)/180.0);
 
    	System.out.println("Delta theta = " + dtheta);
    	double dist = detector.distance;
		int mNewPixels = 0;
		double[][] cor2theta = new double[verPixels-1][mScanxPixels];
		double[][] corGeom = new double[verPixels-1][mScanxPixels];
		double invz =0.0;
		GeometryCorrecter ac;
		if(Zpvertic != null)
       		   ac= new GeometryCorrecter(dist,new FPoint(0.0,0.0),	
       				  new FPoint(0.0,0.0),
    				  true,false,
    				  new FPoint(0.01f,0.01f),
    				  1);
       		else 
       			ac= new GeometryCorrecter(dist,new FPoint(0,150.0),      			
				        new FPoint(0,150.0),
				        true,false,
				        new FPoint(0.01,0.01),
				        1);

		for(int i = 0; i < mScanxPixels; i++)
		{
			for(int j = 0; j < verPixels-1; j++)
			{
//				double inTheta = i*mdtheta;
				double inTheta = 	thetaVect[i] * (Math.PI)/180;
				double inTheta0 = thetaVect[0] * (Math.PI)/180;
				if (Zpvertic != null) invz = Zpvertic[j][i];
				else	invz = detector.heightCurv  - (j*dy);
//				 mdtheta[i] = (thetaVect[j] - thetaVect[j-1])* Math.PI/180;
				cor2theta[j][i] = ac.getAngle2theta(inTheta,invz);
				if (thetaVect[0] < thetaVect[mScanxPixels -1] ) 
				     {
				               if (inTheta < Math.PI/2.0) mNewPixels =  (int) ((cor2theta[j][i] / mdtheta) -inTheta0/mdtheta );
				               else                       mNewPixels =  (int) ((cor2theta[j][i] / mdtheta) -inTheta0/mdtheta  );
				     } else {
			                  if (inTheta < Math.PI/2.0) mNewPixels =  (int) ( inTheta0/mdtheta - (cor2theta[j][i] / mdtheta)  );
			                  else                       mNewPixels =  (int) ( inTheta0/mdtheta - (cor2theta[j][i] / mdtheta) );		    	 
				     }
				if(mNewPixels < mScanxPixels)
				corGeom[j][mNewPixels] =iSample[j][i];
//				if(j == 50&&i <100) System.out.println(" mNewPixels and data =  "+mNewPixels +"; " + iSample[j][i]);
			}
		}

//		if (thetaVect[0] < thetaVect[mScanxPixels]) 
//	     {
//	               if (inTheta < Math.PI/2.0) mNewPixels =  (int) ((cor2theta[j][i] / mdtheta) -inTheta0/mdtheta + 0.5);
//	               else                       mNewPixels =  (int) ((cor2theta[j][i] / mdtheta) -inTheta0/mdtheta  - 0.5);
//	     } else {
//                 if (inTheta < Math.PI/2.0) mNewPixels =  (int) ( inTheta0/mdtheta - (cor2theta[j][i] / mdtheta) - 0.5);
//                 else                       mNewPixels =  (int) ( inTheta0/mdtheta - (cor2theta[j][i] / mdtheta)  + 0.5);		    	 
//	     }		
//		v.setPlotData(ac.transpose(arr),false);
//		s.open();
//		try{
//		StreamTokenizer st = new StreamTokenizer(new InputStreamReader(System.in));
//        while (!s.isDisposed()) {
//            if (!d.readAndDispatch())
//                d.sleep();
//        }
//        boolean printing = true;
//		while(Geom)
//		{
//			if(st.nextToken() == StreamTokenizer.TT_NUMBER)
//			{
//				double x = (double)st.nval;
//				if(st.nextToken() == StreamTokenizer.TT_NUMBER)
//				{
//					double y =(double)st.nval;
//					
////					System.out.println("point = "+ac.flatDetectorCorrect(x,y));
//				}
//			}
//		}	
//		}
//		catch(IOException e)
//		{
//			e.printStackTrace();
//		}
		return corGeom;
	}

	


	
}
