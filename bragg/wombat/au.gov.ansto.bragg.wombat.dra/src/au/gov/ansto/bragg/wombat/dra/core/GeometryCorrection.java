package au.gov.ansto.bragg.wombat.dra.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.interfaces.ISliceIterator;

import au.gov.ansto.bragg.common.dra.algolib.math.BinTypeChange;
import au.gov.ansto.bragg.common.dra.algolib.math.GeometryCorrecter;
import au.gov.ansto.bragg.common.dra.algolib.math.GeometryCorrecter.FPoint;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;

/**
 * This class is the concrete processor of the geometry correction algorithm for echidna.
 *  
 * @author nxi
 * @author jhester
 * @version 2.0
 * @since V2.2
 */
public class GeometryCorrection implements ConcreteProcessor {
	IGroup geometryCorrection_scanData = null;
	URI geometryCorrection_mapFilename = null;
	Boolean geometryCorrection_skip = false;
	Boolean geometryCorrection_stop = false; 
	IGroup geometryCorrection_output = null;
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.map;

	private double[] verticalOffset = null;
	private IArray decurved_data = null;
	private IArray decurved_variance = null;
	private int[][] contributor_mask = null;     // Which parts of this array correspond to imaged pixels

	/*
	 * This method is called when the processor processes. 
	 */
	public Boolean process() throws Exception{
		if (geometryCorrection_skip){
			geometryCorrection_output = geometryCorrection_scanData;
		}else{
			// load up necessary information
			IArray out_array = null;
			IArray out_variance = null;
			IArray dataArray = ((Plot) geometryCorrection_scanData).findSignalArray();
			int[] dshape = dataArray.getShape();
			int drank = dshape.length;
			// Get two theta axis: this will be the largest-numbered axis
			IArray thetaArray = null;
			try{
				thetaArray = ((Plot) geometryCorrection_scanData).getAxis(drank-1).getData();
			}catch (Exception ex){
				ex.printStackTrace();
				thetaArray = geometryCorrection_scanData.getDataItem("thetaVector").getData();
			}
			// Make sure theta Array is 1D
			thetaArray = thetaArray.getArrayUtils().reduce().getArray();
			// And also make sure that we are dealing with bin centres
			int[] thshape = thetaArray.getShape();
			if(thshape[thshape.length-1]==dshape[drank-1]+1) thetaArray = BinTypeChange.ToCentres(thetaArray);
			// Check for dodgy data - it has happened, people!
			thshape = thetaArray.getShape(); //refresh
			if(thshape[thshape.length-1]!= dshape[drank-1]) {
				String errorstring = String.format("Bad data in file: data length %d does not match theta axis length %d",
						dshape[drank-1],thshape[thshape.length-1]);
				throw new Exception(errorstring);
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
				verticalOffsetArray = ((Plot) geometryCorrection_scanData).getAxis(drank-2).getData();

			} catch (Exception e) {
				String errorstring = "Geometry correction fails: no vertical pixel location data in file";
				throw new Exception(errorstring);
			}
			// Make sure that we have pixel centres, not boundaries
			if(verticalOffsetArray.getSize()==dshape[drank-2]+1) 
				verticalOffsetArray = BinTypeChange.ToCentres(verticalOffsetArray);
		    //Get the detector active height to convert to height relative to central position
		    double det_height = geometryCorrection_scanData.getRootGroup().getDataItem("active_height").readScalarDouble();
		    IIndex vo_index = verticalOffsetArray.getIndex();
		    for(int vi=0;vi<verticalOffsetArray.getSize();vi++) {
		    	vo_index.set(vi);
		    	verticalOffsetArray.setDouble(vo_index, verticalOffsetArray.getDouble(vo_index)-det_height/2.0);
		    	}
			//Convert to Java array - a historical hangover
			//TODO: use Arrays everywhere
			verticalOffset = ConverterLib.get1DDouble(verticalOffsetArray);			
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
					int dlength = dataArray.getRank();
					contribs_array = Factory.createArray(int.class, dataShape);
					for (IArrayIterator j = contribs_array.getIterator();j.hasNext();) j.next().setIntCurrent(1);
				    System.out.println("No contribution map available for geometry correction: all pixels contribute");
			}
			//Check that theta axis is increasing, and if not reverse all relevant arrays
			IIndex tth_index = thetaArray.getIndex();
			tth_index.setDim(thshape.length-1, 0);  // "first" value
			double lowang = thetaArray.getDouble(tth_index);
			tth_index.setDim(thshape.length-1,thshape[thshape.length-1]-1);
			if(lowang>thetaArray.getDouble(tth_index)) {  //two-theta axis is reversed: now reverse everything
				dataArray = dataArray.getArrayUtils().flip(drank-1).getArray();
				variance_array = variance_array.getArrayUtils().flip(drank-1).getArray();
				thetaArray = thetaArray.getArrayUtils().flip(thshape.length-1).getArray();
			}
			correctGeometry(dataArray, radius,
						thetaArray, verticalOffset,
						variance_array,
						(int [][]) contribs_array.getArrayUtils().copyToNDJavaArray());
			/* pack everything up for transfer to the next step */
		    IArray out_contribs = Factory.createArray(contributor_mask);
			String resultName = "geometryCorrection_result";
			geometryCorrection_output = PlotFactory.createPlot(geometryCorrection_scanData, resultName, dataDimensionType);
			PlotFactory.addDataToPlot(geometryCorrection_output, resultName, decurved_data, "Straightened data", "Counts", decurved_variance);
			((NcGroup) geometryCorrection_output).addLog("apply geometry correction algorithm to get " + resultName);
			IDataItem contribs = Factory.createDataItem(null,geometryCorrection_output, "contributors",out_contribs);
			for(int data_axis = 0;data_axis < drank-2; data_axis++) {
				Axis one_axis = ((Plot) geometryCorrection_scanData).getAxis(data_axis);
				PlotFactory.addAxisToPlot(geometryCorrection_output,one_axis,one_axis.getDimensionName());
			}
			// We have altered last two axes
			PlotFactory.addAxisToPlot(geometryCorrection_output, "verticalOffset", verticalOffsetArray, "Vertical offset", "Channel", drank-2);
			PlotFactory.addAxisToPlot(geometryCorrection_output, "two_theta_axis", thetaArray, "Two theta","degrees",drank-1);
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

	/**
	 * Apply geometry correction for HIPD/HRPD curved detectors. This method will set the return data and variance
	 * Java arrays, as well as a region array describing which parts of the return array can be included in any
	 * horizontal integration.  Once the region datastructure becomes more versatile (currently only rectilinear
	 * descriptions are supported) we can use that API instead of the current adhoc approach.
	 * @param iSample  input arbitrary dimensional array data set 
	 * @param thetaVect   OneD array theta vector in degrees
	 * @param Zpvertic  Offset in z position at each 2-theta value
	 * @param contribs  2D Pixel ok map
	 * @param radius Curved detector radius
	 */
	public void correctGeometry (IArray iSample, double radius, IArray thetaVect, double[] Zpvertic, IArray variance,
			int [][] contribs) throws InvalidRangeException,ShapeNotMatchException,Exception
			{   		
		int[] dataShape = iSample.getShape();
		IIndex dindex = iSample.getIndex();
		int dlength = iSample.getRank();
		int verPixels = dataShape[dlength-2];  //least rapidly varying index
		int horiPixels = dataShape[dlength-1]; //most rapidly varying index
		int mScanxPixels = horiPixels;  
		// If there are multiple extra dimensions theta will be a multi-dimensional array:
		// In fact, there should be dlength-1 dimensions of theta
		if(thetaVect.getRank()!=dlength-1) {
			String errorstring = String.format("Geometry correction fails: theta array has rank %d for data of rank %d",
					thetaVect.getRank(),dlength);
			throw new Exception(errorstring);
		}
		int mNewPixels = 0;
		double cor2theta = 0.0;    // corrected value
		double invz =0.0;
		contributor_mask = new int [verPixels][mScanxPixels];
		//set all values to 1
		for(int j=0;j<verPixels;j++)
			for(int k=0;k<mScanxPixels;k++) contributor_mask[j][k]=1;
		/* The geometry corrector class produces an object which will convert a pixel
		 * value to an angular value.  We will use this for interpolation later
		 */
		GeometryCorrecter ac;
		if(Zpvertic != null)
			/* A detector with centre point 0,0 in both pixel and
			 * world coordinates, curved in X direction only and with
			 * pixels separated by 0.01 units.  For our purposes
			 * only the y centre point in pixel units and the
			 * detector radius are used.
			 */
			ac= new GeometryCorrecter(radius,new FPoint(0.0,0.0),	
					new FPoint(0.0,0.0),
					true,false,
					new FPoint(0.01f,0.01f),
					1);
		else 
			/* No vertical pixels available: centre point at 0,150 in both
			 * pixel and world coordinates, 0.01 size pixels. We should never use this
			 */
			ac= new GeometryCorrecter(radius,new FPoint(0,150.0),      			
					new FPoint(0,150.0),
					true,false,
					new FPoint(0.01,0.01),
					1);

		/* It is possible that we have unevenly-spaced theta bins, and we are not proposing to respace
		 * them evenly in this algorithm.  At the same time, we want it to be as easy as possible to correct
		 * for any action that we take here.  So, when searching for the appropriate bin in which to put a
		 * recalculated theta value, we simply search in the appropriate direction until we find something 
		 * suitable.
		 */

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
		 * Note that at 2theta = 90 this is one everywhere, as it is for vertical offset = 0
		 * 
		 * The efficiency correction (which is more correctly a flood field correction)
		 * will have corrected for the geometrical effect of smaller solid angle per pixel
		 * away from the equatorial plane, in that the flood field data will be weaker and thus produce a 
		 * larger efficiency value.  They will be weaker by a factor of the Jacobian, so multiplication
		 * by the Jacobian should not be necessary.
		 */
		decurved_variance = Factory.createArray(double.class, dataShape); //new variances
		decurved_data = Factory.createArray(double.class, dataShape);      //output data
		// double inTheta0 = thetaVect.[0] * (Math.PI)/180; //start angle, radians
		double Jacobian = 0.0;  // for storing the Jacobian
		// loop over input pixel array
		double maxJac = 0.0;     //because we can
		double minJac = 5.0;
		ISliceIterator data_iter = iSample.getSliceIterator(2);
		ISliceIterator out_iter = decurved_data.getSliceIterator(2);
		ISliceIterator out_var_iter = decurved_variance.getSliceIterator(2);
		ISliceIterator var_iter = variance.getSliceIterator(2);
		ISliceIterator tth_iter = thetaVect.getSliceIterator(1);
		// Now loop over images, looping over our twotheta array as well
		while(data_iter.hasNext()) {
			double ncontr[][] = new double [verPixels][mScanxPixels]; //number of contributions
			IArray this_tthvect = tth_iter.getArrayNext();

			// values for working out pixel location in our array. 
			// We assume that the 2theta value corresponds to the centre of the pixel.  Therefore, we expect 
			// thlen-1 bins from thlen points.

			IIndex tth_index = this_tthvect.getIndex();
			double dtheta = (this_tthvect.getDouble(tth_index.set(mScanxPixels-1)) -this_tthvect.getDouble(tth_index.set(0)))
			/ (mScanxPixels-1);  //negative if order is reversed
			double mdtheta = dtheta * Math.PI/180.0; // 
			List<Double> thetagrid = new ArrayList<Double>((int) this_tthvect.getSize());
			IArrayIterator ai = this_tthvect.getIterator();
			// Copy this tth array to a hashable list in radians
			while(ai.hasNext()) thetagrid.add(ai.getDoubleNext()*Math.PI/180.0);
			// Now loop over each point in each image
			IArrayIterator twod_data_iter = data_iter.getArrayNext().getIterator();
			IArrayIterator twod_var_iter = var_iter.getArrayNext().getIterator();
			IArray twod_outd_arr = out_iter.getArrayNext();
			IArray twod_outv_arr = out_var_iter.getArrayNext();
			IIndex twod_outv_ind = twod_outv_arr.getIndex();
			IIndex twod_outd_ind = twod_outd_arr.getIndex();
			while(twod_data_iter.hasNext())
				// for(int i = 0; i < mScanxPixels; i++)  
			{
				//	for(int j = 0; j < verPixels; j++)
				//	{
				double twod_data_value = twod_data_iter.getDoubleNext();
				int[] twod_pos = twod_data_iter.getCounter();  //[vert,horiz] is standard for data
				double inTheta = 	this_tthvect.getDouble(tth_index.set(twod_pos[1])) * (Math.PI)/180; // convert to radians
				if (Zpvertic != null) invz = Zpvertic[twod_pos[0]]; //vertical offset for this vertical pixel coordinate
				cor2theta = ac.getAngle2theta(inTheta,invz); //actual 2 theta value in radians
				// We calculate the Jacobian, but it is not presently used
				Jacobian = 1.0/Math.sqrt(1.0-Math.pow(Math.cos(cor2theta),2))*Math.sin(inTheta);
				if(Jacobian>maxJac) maxJac = Jacobian;
				if(Jacobian<minJac) minJac = Jacobian;
				// find out where this pixel would sit on the transformed grid.
				// We do a binary search on our thetagrid, which will return either an exact match or else
				// the insertion point, defined as the position that this value would be in the list.  Which
				// means we have to decide either to assign this value to the previous 2 theta coordinate,
				// or else the next one.  We take the halfway point between them as the boundary
				// Collections.sort(thetagrid);
				mNewPixels = Collections.binarySearch(thetagrid, cor2theta);  //get (-insertion point) - 1
				// if(invz>=0 && (i%1000==0 || i%1000==1)) System.out.printf("IP %d ",mNewPixels);
				// For reference, the value on an even grid
				int mOldPixels = (int) Math.round((cor2theta-(this_tthvect.getDouble(tth_index.set(0))*Math.PI/180))/mdtheta);
				if (mNewPixels < 0)              //no exact match, as will be the case away from equator
				{
					mNewPixels = (-1*mNewPixels)-1;            // now equal to insertion point
					if(mNewPixels > 0 && mNewPixels < mScanxPixels) {    // otherwise no point in the following calculation
						// adjust for the nearest neighbour
						double dist = cor2theta - thetagrid.get(mNewPixels-1);
						double local_span = thetagrid.get(mNewPixels) - thetagrid.get(mNewPixels-1);
						if (dist < local_span/2) mNewPixels--;
						// Some debugging output
						//if(invz>=0 &&(i%1000 == 0 || i%1000 == 1)) {
						//   System.out.printf("%f %d -> %f %f, assigned to %f *%f* %f (bin %d, dist %f, span %f)%n",inTheta,j,cor2theta,invz,
						//		   mNewPixels >0 ? thetagrid.get(mNewPixels-1):0.0, thetagrid.get(mNewPixels),thetagrid.get(mNewPixels+1),
						//		   mNewPixels, dist, local_span);
						// }
					}
					// due to rounding error we could conceive of getting an insertion point beyond the last element.  We assume that
					// it would be assigned to the last bin in any case
					if (mNewPixels == mScanxPixels) mNewPixels--;   //assign to last element
				}
				// Check in with our pixelok map. A pixel which is not OK will not contribute, and the next section
				// of code will take care of constructing the new pixel ok map. 
				if(contribs[twod_pos[0]][twod_pos[1]]==1) {
					ncontr[twod_pos[0]][mNewPixels]++;	
					//Now create a view of the array where the final two dimensions are fixed, then iterate over it
					//setting the data and variance values
					/* Array loop_array = iSample.sectionNoReduce(origin_list, range_list, null);
					Array loop_err_array = variance.sectionNoReduce(origin_list, range_list, null);
					Array out_array = decurved_data.sectionNoReduce(origin_list, range_list, null);
					Array out_var_array = decurved_variance.sectionNoReduce(origin_list, range_list, null);
					ArrayIterator oa_iter = out_array.getIterator();
					ArrayIterator oae_iter = out_var_array.getIterator();
					ArrayIterator la_iter = loop_array.getIterator();
					ArrayIterator lae_iter = loop_err_array.getIterator();*/
					// Add contribution of this pixel to the appropriate place

					twod_outd_ind.set(twod_pos[0],mNewPixels);
					twod_outd_arr.setDouble(twod_outd_ind,twod_outd_arr.getDouble(twod_outd_ind)+twod_data_value);
					twod_outv_ind.set(twod_pos[0],mNewPixels);
					twod_outv_arr.setDouble(twod_outv_ind,twod_outv_arr.getDouble(twod_outv_ind)+twod_var_iter.getDoubleNext());
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
			int nonzero = 0; // count nonzero pixels
			for(int i=0;i<mScanxPixels;i++)
			{
				//	System.out.printf("%d: ",i);
				for(int j=1;j<verPixels-1;j++) {
					/* look for the edges */
					if (ncontr[j][i] > 0 && ncontr[j-1][i] > 0 && ncontr[j+1][i]>0) {
						contributor_mask[j][i] &= 1;   //All slices must have a contributor for this to count
						nonzero++;
						continue;
					}
					if (ncontr[j][i] > 0 && ncontr[j-1][i] == 0) {  // found a rising edge, discard this value in this frame
						twod_outd_ind.set(j,i);
						twod_outd_arr.setDouble(twod_outd_ind,0);
						twod_outv_ind.set(j,i);
						twod_outv_arr.setDouble(twod_outv_ind,0);
						j++;                                        // skip the new real edge value
						contributor_mask[j][i]&=1;                          // but it does contribute
						nonzero++;
						continue;                                   
					}
					if (ncontr[j][i] == 0 && ncontr[j-1][i] > 0) {   // a falling edge
						twod_outd_ind.set(j-1,i);
						twod_outd_arr.setDouble(twod_outd_ind,0);
						twod_outv_ind.set(j-1,i);
						twod_outv_arr.setDouble(twod_outv_ind,0);
					}
				}  // end of loop over one vertical strip
				/* Handle the vertical edges */
				if (ncontr[0][i]>0) { 
					contributor_mask[0][i]&=1; nonzero++;
				}
				if (ncontr[verPixels-1][i]>0) {
					contributor_mask[verPixels-1][i]&=1; nonzero++;
				}
			}  //end of loop over all horizontal coordinates
			System.out.printf("Nonzero pixels in mask for this frame: %d%n",nonzero);
		}  //end of loop over all frames
		System.out.println("Geometry transformation minimum, maximum Jacobian: "+minJac +","+maxJac);
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

