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
			/*correctGeometry(dataArray, radius,
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
	 * @param stepsize The angular step between each two-theta bin, in degrees.
	 */
	public void correctGeometry (IArray iSample, double radius, double stepsize, IArray thetaVect, IArray Zpvertic, IArray variance,
			int bottom, int top,
			/* output */
            IArray contributor_mask, IArray decurved_data, IArray decurved_variance) throws InvalidRangeException,ShapeNotMatchException,Exception
			{   		
		int[] dataShape = iSample.getShape();
		IIndex dindex = iSample.getIndex();
		int dlength = iSample.getRank();
		int verPixels = dataShape[dlength-2];  //least rapidly varying index
		int horiPixels = dataShape[dlength-1]; //most rapidly varying index
		
		// If there are multiple extra dimensions theta will be a multi-dimensional array: if
		// the detector is stationary theta dimension will be lower
		
		boolean stationary = (thetaVect.getRank()==1);
		
		int mNewPixels = 0;
		double cor2theta = 0.0;    // corrected value
		double invz =0.0;
		double radsq = radius * radius;
		double mdtheta = stepsize * Math.PI/180;
		
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

		ISliceIterator data_iter = iSample.getSliceIterator(2);
		ISliceIterator out_iter = decurved_data.getSliceIterator(2);
		ISliceIterator out_var_iter = decurved_variance.getSliceIterator(2);
		ISliceIterator var_iter = variance.getSliceIterator(2);
		ISliceIterator contrib_iter = contributor_mask.getSliceIterator(2);
		ISliceIterator tth_iter = null;
		
		if (!stationary) tth_iter = thetaVect.getSliceIterator(1);
		
		/* Not clear what the best strategy here is for multiple frames. If we loop over
		 vertical, then horizontal, then frame, we minimise floating-point calculations,
		 but we will skip around the data structure and lose the benefit of caching.
		 
		 If we simply iterate over every point, we will perform some calculations at least 100 * nframes
		 times more often than necessary. So we loop over frames, but within a single frame we loop
		 over height, then angle.
		
		*/
		IIndex zpind = Zpvertic.getIndex();
		IArray this_tthvect = thetaVect;
		
		// Now loop over images
		while(data_iter.hasNext()) {
			double ncontr[][] = new double [verPixels][horiPixels]; //number of contributions
			if (!stationary) {
			    this_tthvect = tth_iter.getArrayNext();
			}
			
			// values for working out pixel location in our array. 
			// We assume that the 2theta value corresponds to the centre of the pixel.

			IIndex tth_index = this_tthvect.getIndex();
			double start_theta = this_tthvect.getDouble(tth_index.set(0)) * Math.PI/180.0;
			
			// Now set things up for this frame
			
			IArray twod_data = data_iter.getArrayNext();
			IIndex twod_d_ind = twod_data.getIndex();
			
			IArray twod_var = var_iter.getArrayNext();
			IIndex twod_v_ind = twod_var.getIndex();
			
			IArray twod_outd_arr = out_iter.getArrayNext();
			IArray twod_outv_arr = out_var_iter.getArrayNext();
			IIndex twod_outv_ind = twod_outv_arr.getIndex();
			IIndex twod_outd_ind = twod_outd_arr.getIndex();
			IArray contrib_arr = contrib_iter.getArrayNext();
			IIndex contrib_ind = contrib_arr.getIndex();
			
			// Loop over vertical position
			for(int j = bottom; j < top; j++)
			{
				invz = Zpvertic.getDouble(zpind.set(j)); //vertical offset for this vertical pixel coordinate
				double xFactor = radius/Math.sqrt(radsq+invz*invz);

				for(int i = 0; i < horiPixels; i++) {  //loop over raw two theta grid
					double inTheta = 	this_tthvect.getDouble(tth_index.set(i)) * (Math.PI)/180; // convert to radiansraw
					double oldcos = Math.cos(inTheta);
					cor2theta = Math.acos(xFactor*oldcos);    //potential sign issues?
				
					// find out where this pixel would sit on the transformed grid.
					
					double grid_pos = (cor2theta-start_theta)/mdtheta;
					mNewPixels = (int) Math.round(grid_pos);
					double ideal_pos = start_theta + mNewPixels * mdtheta;
				
					// proportion of intensity to assign
				
					double shift = (cor2theta - ideal_pos)/mdtheta;
				
				/* Use offset as a proxy for proportion of the pixel to move. If shift is positive, the ideal position is to the right
				 * and the next pixel right will get shift*intensity added to it. If shift is negative, the ideal position is back to
				 * the left of centre and that position will get abs(shift)* intensity added to it. In both cases the ideal pixel gets
				 * the remainder of the intensity. 
				 */
					int partial = (int) (mNewPixels + Math.signum(shift));
					shift = Math.abs(shift);
					
					if (mNewPixels >= horiPixels) {
						mNewPixels--;   //assign to last element
						shift = 0;
						partial = -1;
					}	
				
					// Count contributions to this pixel
				
					ncontr[j][mNewPixels]+=(1-shift);
					
					
					// Add contribution of this pixel to the appropriate place

					twod_v_ind.set(j,i);
					twod_d_ind.set(j, i);
					twod_outv_ind.set(j,mNewPixels);
					twod_outd_ind.set(j,mNewPixels);
					
					twod_outd_arr.setDouble(twod_outd_ind,twod_outd_arr.getDouble(twod_outd_ind)+twod_data.getDouble(twod_d_ind) * (1 - shift));
					twod_outv_arr.setDouble(twod_outv_ind,twod_outv_arr.getDouble(twod_outv_ind)+twod_var.getDouble(twod_v_ind) * (1 - shift));
					
					// Now the remainder that overlaps the neighbour
					
					if (partial >= 0 && partial < horiPixels) {
					    ncontr[j][partial] += shift;
					    twod_outd_ind.set(j, partial);
					    twod_outv_ind.set(j, partial);
						twod_outd_arr.setDouble(twod_outd_ind,twod_outd_arr.getDouble(twod_outd_ind)+twod_data.getDouble(twod_d_ind) * shift);
						twod_outv_arr.setDouble(twod_outv_ind,twod_outv_arr.getDouble(twod_outv_ind)+twod_var.getDouble(twod_v_ind) * shift);
					}
			}  // horizontal coord
		}      // vertical coord
			
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
			for(int i=0;i<horiPixels;i++)
			{
				//	System.out.printf("%d: ",i);
				for(int j=1;j<verPixels-1;j++) {
					/* look for the edges */
					if (ncontr[j][i] > 0 && ncontr[j-1][i] > 0 && ncontr[j+1][i]>0) {
						contrib_ind.set(j,i);
					    contrib_arr.setInt(contrib_ind, 1);
						nonzero++;
						continue;
					}
					if (ncontr[j][i] > 0 && ncontr[j-1][i] == 0) {  // found a rising edge, discard this value in this frame
						twod_outd_ind.set(j,i);
						twod_outd_arr.setDouble(twod_outd_ind,0);
						twod_outv_ind.set(j,i);
						twod_outv_arr.setDouble(twod_outv_ind,0);
						j++;
						contrib_ind.set(j,i);
						contrib_arr.setInt(contrib_ind, 1);                          // but it does contribute
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
					contrib_ind.set(0,i);
					contrib_arr.setInt(contrib_ind, 1); nonzero++;
				}
				if (ncontr[verPixels-1][i]>0) {
					contrib_ind.set(verPixels-1,i);
					contrib_arr.setInt(contrib_ind, 1); nonzero++;
				}
			}  //end of loop over all horizontal coordinates
			System.out.printf("Nonzero pixels in mask for this frame: %d%n",nonzero);
		}  //end of loop over all frames
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

