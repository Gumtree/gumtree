package au.gov.ansto.bragg.echidna.dra.core;

import java.net.URI;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcArray;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * This class is the concrete processor of the geometry correction algorithm for echidna.
 *  
 * @author nxi
 * @author jhester
 * @version 2.0
 * @since V2.2
 */
public class GeometryCorrection extends ConcreteProcessor {
	IGroup geometryCorrection_scanData = null;
	URI geometryCorrection_mapFilename = null;
	Boolean geometryCorrection_skip = false;
	Boolean geometryCorrection_stop = false; 
	IGroup geometryCorrection_output = null;
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.map;

	private double[] verticalOffset = null;
	private double[][] decurved_data = null;
	private double[][] decurved_variance = null;
	private int[][] contributor_mask = null;     // Which parts of this array correspond to imaged pixels
	private double[] thetagrid = null;           // New two-theta axis

	/*
	 * This method is called when the processor processes. 
	 */
	public Boolean process() throws Exception{
		if (geometryCorrection_skip || geometryCorrection_scanData == null){
			geometryCorrection_output = geometryCorrection_scanData;
		}else{
			// load up necessary information
			IArray out_array = null;
			IArray out_variance = null;
			IArray dataArray = ((NcGroup) geometryCorrection_scanData).findSignal().getData();
			IArray thetaArray = null;
			try{
				thetaArray = ((Plot) geometryCorrection_scanData).getAxis(1).getData();
			}catch (Exception ex){
				ex.printStackTrace();
				thetaArray = geometryCorrection_scanData.getDataItem("thetaVector").getData();
			}
			double radius = 0.0;
			try {
				radius = geometryCorrection_scanData.getDataItem("radius").readScalarDouble();
			} catch (Exception e) {
				String errorstring = "Geometry correction fails: unable to find detector radius";
				System.out.print(errorstring);
				throw new Exception(errorstring);
			}
			IArray verticalOffsetArray = null;
			try {
				verticalOffsetArray = ((Plot) geometryCorrection_scanData).getAxis(0).getData();
				verticalOffset = ConverterLib.get1DDouble(verticalOffsetArray);			
			} catch (Exception e) {
				String errorstring = "Geometry correction fails: no vertical pixel location data in file";
				throw new Exception(errorstring);
			}
			IArray variance_array = null;
			try {
				variance_array = ((Plot) geometryCorrection_scanData).getVariance().getData();
			} catch (Exception e) {
				String errorstring = "No variance available for geometry correction";
				System.out.print(errorstring);
				throw new Exception(errorstring);
			} 
			IArray contribs_array = null;
			try {
				contribs_array = geometryCorrection_scanData.findDataItem("contributors").getData();
			} catch (Exception e) {
					int [] dataShape = dataArray.getShape();
					int [] revShape = {dataShape[2],dataShape[1]};  /* contributor map is tube first index, vertical pixel second index */
					contribs_array = Factory.createArray(int.class, revShape);
					for (IArrayIterator j = contribs_array.getIterator();j.hasNext();j.next()) j.setIntCurrent(1);
				    System.out.println("No contribution map available for geometry correction: all pixels contribute");
			}
			/* now correct for the detector geometry */
			correctGeometry(ConverterLib.get2DDouble(dataArray), radius,
						ConverterLib.get1DDouble(thetaArray), verticalOffset,
						ConverterLib.get2DDouble(variance_array),
						(int [][]) contribs_array.getArrayUtils().copyToNDJavaArray());
			/* pack everything up for transfer to the next step */
		    out_array = Factory.createArray(decurved_data);
		    out_variance = Factory.createArray(decurved_variance);
		    IArray out_contribs = Factory.createArray(contributor_mask);
			String resultName = "geometryCorrection_result";
			//Transfer theta axis values to Array datastructure
			IIndex thindex = thetaArray.getIndex();
			for(int i=0;i<thetagrid.length;i++) {
				thindex.set(i);
				thetaArray.setDouble(thindex, thetagrid[i]*180.0/Math.PI);
			}
			geometryCorrection_output = PlotFactory.createPlot(geometryCorrection_scanData, resultName, dataDimensionType);
			PlotFactory.addDataToPlot(geometryCorrection_output, resultName, out_array, "Straightened data", "Counts", out_variance);
			((NcGroup) geometryCorrection_output).addLog("apply geometry correction algorithm to get " + resultName);
			IDataItem contribs = Factory.createDataItem(null,geometryCorrection_output, "contributors",out_contribs);
			PlotFactory.addAxisToPlot(geometryCorrection_output, "verticalOffset", verticalOffsetArray, "Vertical offset", "Channel", 0);
			PlotFactory.addAxisToPlot(geometryCorrection_output, "two_theta_axis", thetaArray, "Two theta","degrees",1);
			// Add information for use by visualisation software
//			geometryCorrection_output.addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
	//				StaticDefinition.DataStructureType.plot.name());
		//	geometryCorrection_output.addStringAttribute(StaticDefinition.DATA_DIMENSION_TYPE, StaticDefinition.DataDimensionType.map.name());
			geometryCorrection_output.addDataItem(contribs);
		}
		// as the instance of this object remains for as long as the processor chain exists, we null out any fields that are taking up
		// too much room.  This will only give a benefit if the data to which they point has been copied to the output group (rather than
		// the output group holding a reference to the data).
		verticalOffset = null;
		decurved_data = null;
		decurved_variance = null;
		contributor_mask = null;     // Which parts of this array correspond to imaged pixels
		return geometryCorrection_stop;
	}

	 /* Apply geometry correction for HRPD curved detector onto an even 2-theta grid. An even grid is reasonable
	  * given that the only points for which an uneven grid will produce exact results are those on the 
	  * equatorial plane.
	 * This method will set the return data and variance
	 * Java arrays, as well as a region array describing which parts of the return array can be included in any
	 * horizontal integration.  Once the region datastructure becomes more versatile (currently only rectilinear
	 * descriptions are supported) we can use that API instead of the current adhoc approach. 
	 * @param iSample  input 2D array data set after stitching (nTubes*nScan)*yPixels 
	 * @param thetaVect   OneD array theta vector in degrees
	 * @param Zpvertic  Offset in z position at each 2-theta value
	 * @param contribs  Pixel ok map
	 * @param radius Curved detector radius
	 */
	public void correctGeometry(double[][] iSample, double radius, double[] thetaVect, double[] Zpvertic, double[][] variance,
			                    int [][] contribs)
	{   		
		int verPixels = iSample.length;
		int horiPixels = iSample[0].length;
		int mScanxPixels = horiPixels;
		int thlen = thetaVect.length;

		int mNewPixels = 0;
		double cor2theta = 0.0;    // corrected value
		double invz =0.0;
		/* Respace the theta bins into an even grid.  We find out the total length and number of bins,
		 * then use the average width for our grid.
		 */
		
		// values for working out pixel location in our array. As discussed in the stitching algorithm,
		// we assume that the 2theta value corresponds to the centre of the pixel.  Therefore, we expect 
		// thlen-1 bins from thlen points.

		double dtheta = (thetaVect[thlen-1] -thetaVect[0]) / (thlen-1);  //negative if order is reversed
		double mdtheta = dtheta * Math.PI/180.0; // in radians
		System.out.printf("Theta grid regularised to spacing %f\n", mdtheta*180.0/Math.PI);

		/* our strategy is to calculate the true two-theta value of each pixel, and insert the intensity
		 * into our array of (y, true 2-theta)-indexed intensities.  It is quite possible that several
		 * source pixels might map onto a single target pixel, so we keep track of this as we go. Unlike
		 * D20B, we do not attempt to work out an interpolated value for intensity and variance, as this
		 * would introduce statistical correlation between neighbouring points in the final 1D data. Note
		 * that the Jacobian of our transformation is in this case simply
		 * d 2theta_new/d 2theta (as the derivatives with respect to pixel height are 0 and 1 respectively)
		 * which is
		 * 1/sqrt(1-cos^2 2theta_new) * sin 2theta
		 * 
		 * Note that at 2theta = 90 this is 1 everywhere, as it is for vertical offset = 0
		 * 
		 * The efficiency correction (which is more correctly a flood field correction)
		 * will have corrected for the geometrical effect of smaller solid angle per pixel
		 * away from the equatorial plane, in that the flood field data will be weaker and thus produce a 
		 * larger efficiency value.  They will be weaker by a factor of the Jacobian, so multiplication
		 * by the Jacobian should not be necessary.
		 */
		double ncontr[][] = new double [verPixels][mScanxPixels]; //number of contributions
		decurved_variance = new double [verPixels][mScanxPixels]; //new variances
		decurved_data = new double[verPixels][mScanxPixels];      //output data
		double inTheta0 = thetaVect[0] * (Math.PI)/180; //start angle, radians
		double Jacobian = 0.0;  // for storing the Jacobian
		// loop over input pixel array
		double maxJac = 0.0;     //because we can
		double minJac = 5.0;
		thetagrid = new double[thlen];
		for(int i=0;i<thlen;i++) thetagrid[i]=inTheta0+(mdtheta*i);  //initialise theta grid
		double radsq = radius*radius;   //precalculate for efficiency
		for(int i = 0; i < mScanxPixels; i++)
		{
			double inTheta = 	thetaVect[i] * (Math.PI)/180; // convert to radians
			double oldcos = Math.cos(inTheta);  //precalculate for efficiency
			for(int j = 0; j < verPixels; j++)
			{
				if (Zpvertic != null) invz = Zpvertic[j]; //vertical offset for this vertical pixel coordinate
				double xFactor = radius/Math.sqrt(radsq+invz*invz);
				cor2theta = Math.acos(xFactor*oldcos);    //potential sign issues?
				// We calculate the Jacobian, but it is not presently used
				Jacobian = 1.0/Math.sqrt(1.0-Math.pow(Math.cos(cor2theta),2))*Math.sin(inTheta);
				if(Jacobian>maxJac) maxJac = Jacobian;
				if(Jacobian<minJac) minJac = Jacobian;
				// find out where this pixel would sit on the transformed grid.
				mNewPixels = (int) Math.round((cor2theta-inTheta0)/mdtheta);
				// Check in with our pixelok map. A pixel which is not OK will not contribute, and the next section
				// of code will take care of constructing the new pixel ok map
				if(contribs[j][i]==1) {
				ncontr[j][mNewPixels]++;	
				decurved_data[j][mNewPixels] += iSample[j][i];
				decurved_variance[j][mNewPixels] += variance[j][i];
				}
			}
		}
		/* When this dataset is vertically integrated, we need to avoid accessing the regions that were
		 not accessible to the cylindrical detector but after straightening transformation are now included
		 in the output array.  We thus create a 'mask' array with 0 in non-contributor positions and '1' in
		 contributor positions. To avoid edge
		 effects we throw away data within one pixel of the boundaries.  The following heuristic may fail for pathological
		 cases, for example when only two adjacent vertical pixels are contributors: the first is detected,
		 and the second is skipped over.  Note that we do not normalise by the number of contributions: the
		 efficiency (or more properly flood-field) correction has taken care of the compression effects already.
		 */
		
		contributor_mask = new int [verPixels][mScanxPixels];
		int nonzero = 0; // count nonzero pixels
		for(int i=0;i<mScanxPixels;i++)
		{
		//	System.out.printf("%d: ",i);
			for(int j=1;j<verPixels-1;j++) {
				/* look for the edges */
				if (ncontr[j][i] > 0 && ncontr[j-1][i] > 0 && ncontr[j+1][i]>0) {
					contributor_mask[j][i] = 1;
					nonzero++;
					continue;
				}
				if (ncontr[j][i] > 0 && ncontr[j-1][i] == 0) {  // found a rising edge, discard this value
					decurved_data[j][i] = 0.0;
					decurved_variance[j][i] = 0.0;
					j++;                                        // skip the new real edge value
					contributor_mask[j][i]=1;                          // but it does contribute
					nonzero++;
					continue;                                   
				}
				if (ncontr[j][i] == 0 && ncontr[j-1][i] > 0) {   // a falling edge
					decurved_data[j-1][i] = 0.0;
					decurved_variance[j-1][i] = 0.0;
				}
			}
			/* Handle the vertical edges */
			if (ncontr[0][i]>0) { 
				contributor_mask[0][i]=1; nonzero++;
			}
			if (ncontr[verPixels-1][i]>0) {
				contributor_mask[verPixels-1][i]=1; nonzero++;
			}
		}
		System.out.println("Geometry transformation minimum, maximum Jacobian: "+minJac +","+maxJac);
		System.out.printf("Nonzero pixels in mask: %d%n",nonzero);
	}

	public IGroup getGeometryCorrection_output() {
		return geometryCorrection_output;
	}


	public void setGeometryCorrection_scanData(IGroup geometryCorrection_scanData) {
		this.geometryCorrection_scanData = geometryCorrection_scanData;
	}


	public void setGeometryCorrection_skip(Boolean geometryCorrection_skip) {
		this.geometryCorrection_skip = geometryCorrection_skip;
	}


	public void setGeometryCorrection_stop(Boolean geometryCorrection_stop) {
		this.geometryCorrection_stop = geometryCorrection_stop;
	}

	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

}

