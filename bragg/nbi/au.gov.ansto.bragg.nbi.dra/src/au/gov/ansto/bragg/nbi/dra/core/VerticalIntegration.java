package au.gov.ansto.bragg.nbi.dra.core;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.core.region.RegionUtils;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * This class is the concrete processor of the vertical integration algorithm for Wombat algorithm group.
 * If the input data are 2D, a 1D array is returned, otherwise a series of 1D arrays are returned, where the second
 * axis corresponds to the third dimension (the second dimension is vertical pixel).  An optional 
 * parameter allows the output data to be scaled such that the maximum value is 10000 counts.  This is provided to enable
 * datasets to be compared easily
 *  
 * @author nxi,jhester
 * @version 2.0
 */
public class VerticalIntegration extends ConcreteProcessor{

	IGroup verticalIntegration_input = null;
	Boolean verticalIntegration_skip = false;
	Boolean verticalIntegration_stop = false; 
//	Double verticalIntegration_minDist = 0.;
//	Double verticalIntegration_maxDist = 127.;
	IGroup verticalIntegration_region = null;
	IGroup verticalIntegration_output = null;
	Boolean vertint_scale = false;              // Scale to make max value 10000
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public DataDimensionType dataDimensionType = DataDimensionType.patternset;

	/**
	 * This method is called when the processor processes. Note that the datadimensiontype will
	 * attempt to switch itself to pattern, but currently Kakadu does not attempt to detect a
	 * changed data dimension type after initialisation.
	 * 
	 * @throws Exception 
	 */
	
	public Boolean process() throws Exception{
		IArray contrib_mask = null;
		if (verticalIntegration_skip) verticalIntegration_output = verticalIntegration_input;
		else{
			IArray data = ((NcGroup) verticalIntegration_input).findSignal().getData();
			IArray thetaArray = null;
			int drank = data.getRank();
			try{
				thetaArray = ((Plot) verticalIntegration_input).getAxis(drank-1).getData();
			}catch (Exception e){
				thetaArray = verticalIntegration_input.findDataItem("polar_angle").getData();
				System.out.print("Warning: using polar angle array for two theta values");
			}
			try {
				contrib_mask = verticalIntegration_input.findDataItem("contributors").getData();
			} catch (Exception e) {
				System.out.println("No contributor mask found: default used");
				contrib_mask = make_mask(data.getShape());
			}
		    IArray sourceData = null;
			IArray variance_array = null;
			try{
				variance_array = ((Plot) verticalIntegration_input).getVariance().getData();
			}catch (Exception e) {
				// TODO: handle exception
				variance_array = data;
			}
			if (verticalIntegration_region != null)	{
				try {
				IArray regionalDataArray = 
//					RegionSelector.applyInterestedRegion(verticalIntegration_input, 
//							verticalIntegration_region);
					RegionUtils.applyRegion(verticalIntegration_input, 
							verticalIntegration_region);
				sourceData = regionalDataArray;
				} catch (Exception e) {
					throw new Exception("Vertical integration: failed to apply mask region");
				}
			}
			else 
				sourceData = data;
			// The final result must be either a single 1D array or a sequence of them.  Therefore we will
			// sum over all higher dimensions, retaining only the two theta axis in each frame (the final
			// dimension), with the third-last dimension being the frame step dimension. The 
			// dimensions of the final array are always just the 1D dimension, with an optional 2nd
			// dimension corresponding to the third-last parameter.  For consistency we always pass a
			// rank-2 array to the vertical integration method and reduce any extra dimensions later.
			int[] initialShape = sourceData.getShape();
			int initialRank = sourceData.getRank();
			int[] finalShape = null;
			if(initialRank==2) finalShape = new int[] {1,(int) initialShape[initialRank-1]};
			   // sourceData = sourceData.reshape(new int[]{1,initialShape[0],initialShape[1]});//always at least rank 3
			   // variance_array = variance_array.reshape(new int[]{1,initialShape[0],initialShape[1]});
			else finalShape = new int[] {initialShape[initialRank-3], initialShape[initialRank-1]};   	
			IArray[] resultArrays = {Factory.createArray(double.class,finalShape),Factory.createArray(double.class, finalShape)};
			verticalIntegrate(
					sourceData, variance_array, contrib_mask, resultArrays);
			// Rescale if necessary
			if (vertint_scale) rescale(10000.0,resultArrays);
			// Get rid of any extra dimension we might have added
			resultArrays[0] = resultArrays[0].getArrayUtils().reduce().getArray();
			resultArrays[1] = resultArrays[1].getArrayUtils().reduce().getArray();
			if(initialRank==2) dataDimensionType = DataDimensionType.pattern;
			String resultName = "horizontalIntegration_result";
			verticalIntegration_output = PlotFactory.createPlot(verticalIntegration_input, resultName, dataDimensionType);
			String plot_title = ((IDataItem) verticalIntegration_input.findContainerByPath("$entry/sample/name")).getData().toString();
			PlotFactory.addDataToPlot(verticalIntegration_output, resultName, resultArrays[0], plot_title, "Counts", resultArrays[1]);
			((NcGroup) verticalIntegration_output).addLog("apply horizontal integration algorithm to get " + resultName);
			if(initialRank==2) PlotFactory.addAxisToPlot(verticalIntegration_output, "two_theta_vector", thetaArray, "Two theta", "degrees", 0);
			else if(initialRank>2) {   //need to add another axis
				PlotFactory.addAxisToPlot(verticalIntegration_output, "two_theta_vector", thetaArray, "Two theta", "degrees", 1);
				Axis frame_axis = ((Plot) verticalIntegration_input).getAxis(drank-3);
				PlotFactory.addAxisToPlot(verticalIntegration_output,frame_axis,0);
			} 
			//Debug report
			int[] outshape = resultArrays[0].getShape();
			System.out.printf("After v integration, data rank %d:", outshape.length);
			for(int j:outshape) System.out.printf("%d ",j);
			// Add information for use by visualisation software
		}
		return verticalIntegration_stop;
//		return result;
	}
	
	/**
     * Makes the vertical integration about a selected region.  Masked out areas will have values of Double.NaN.
     * Set private fields rather than return structured arrays.  We accept a 'mask' argument for more complicated
     * masks that are not handled by the current Region datastructures.
     * 
     * @param data The multi-dimensional (at least 2 dimensions) double data to be integrated.
     * @param variances The multi-dimensional (at least 2 dimensions) variances corresponding to first argument
	 * @param minDist  The minimum value (bottom side) for integration.
	 * @param maxDist The maximum value (top side) for integration.
	 * @param mask    A 2-dimensional integer mask with 1 at included pixels
	 * @param returned_data A 2-element array with each element a 2-dimensional Array for the data and variances
     */

	public void verticalIntegrate(IArray data, IArray variances,
			IArray mask, IArray[] returned_data) throws InvalidRangeException {

		// We add over all dimensions except the last and third-last, if the
		// latter exists.  We do this by creating a view containing all of the
		// data to be summed at each two-theta value
		int[] dataShape = data.getShape();
		int dataRank = data.getRank();
		int yheight = dataShape[dataRank-2];   //maximum y pixel value
		//Prepare output array indices
		IIndex o_index = returned_data[0].getIndex();
		IIndex ov_index = returned_data[1].getIndex();
		IIndex c_index = mask.getIndex();
		int[] oarray_shape = returned_data[0].getShape();
		// sanity check for minimum,maximum limits
		double minDist = 0, maxDist = yheight; 
//		if (minDist < 0) minDist = 0;
//		if (maxDist >= yheight) maxDist = yheight;
		int[] origin_list = new int[dataRank];
		int[] range_list = dataShape.clone();
		// Loop over all two theta values
		for(int ttheta=0;ttheta<dataShape[dataRank-1];ttheta++) {
			// Initialise our array views
			// System.out.printf("Theta step=%d:",ttheta);
			origin_list[dataRank-1]= ttheta;
			range_list[dataRank-1]=1;     //ie theta range is always last
			IArray ttheta_step = data.getArrayUtils().sectionNoReduce(origin_list, range_list,null).getArray();
			IArray variance_step = variances.getArrayUtils().sectionNoReduce(origin_list,range_list,null).getArray();
			// Now we can iterate over our view in parallel with our returned data
			IArrayIterator tth_iter = ttheta_step.getIterator();
			IArrayIterator v_iter = variance_step.getIterator();
			int nentry[] = new int[oarray_shape[0]];   // initialise counter for contributors
			// initialise indices
			c_index.setDim(1, ttheta);  //contributor mask is 2D with 2-theta the second index
			o_index.setDim(1, ttheta);  //final index for output data and variance is 2-theta
			ov_index.setDim(1,ttheta);
			//loop over all pixels
			while(tth_iter.hasNext())   //iterate over all pixels at this two-theta value
			{
				double tth_iter_value = tth_iter.getDoubleNext();
				String velem = String.valueOf(tth_iter_value);
				int[] position = tth_iter.getCounter();
				//set indices of output arrays etc. to current value
				c_index.setDim(0, position[position.length-2]);  //Second last position value is the vertical direction, as we
				//used sectionnoReduce for ttheta_step
				if (velem.equals("NaN") || (mask != null && mask.getInt(c_index)==0)) continue;   //This value is masked out	   
				int extra_dim = 0;
				if(position.length>2) extra_dim = position[position.length-3];
				// if(ttheta==50) {System.out.printf("Pos %d++ at step 50 (pos %d-%d)%n",extra_dim,position[0],position[1]);}
				nentry[extra_dim]++;            // count contributions
				o_index.setDim(0, extra_dim);
				ov_index.setDim(0, extra_dim);
				returned_data[0].setDouble(o_index, returned_data[0].getDouble(o_index)+tth_iter_value);
				returned_data[1].setDouble(o_index, returned_data[1].getDouble(ov_index)+v_iter.getDoubleNext());
				//if(ttheta==50) System.out.printf("Pos %d %d now %f%n", extra_dim,ttheta,returned_data[0].getDouble(o_index));
			}
			// Finished single theta loop: now normalise to number of pixels vertically. This is
			// convenient for a quick estimate of pixel error based on total counts.  We choose
			// to normalise to the maximum number of possible vertical pixels for simplicity.
			for(int i=0;i<oarray_shape[0];i++) {
				o_index.setDim(0,i);
				ov_index.setDim(0,i);
				// System.out.printf("Nentry[%d]=%d%n",i,nentry[i]);
				if(nentry[i] == 0) returned_data[0].setDouble(o_index, 0);
				else { 
					returned_data[0].setDouble(o_index, returned_data[0].getDouble(o_index)*yheight/nentry[i]);
					returned_data[1].setDouble(ov_index, returned_data[1].getDouble(ov_index)*yheight*yheight/(nentry[i]*nentry[i]));
				}
			} 
		}
	}

	/* Rescale the data and variance arrays such that the maximum value in the data array is newmax */
	
	public void rescale (double newmax, IArray [] results) {
		// Find the current maximum 
		double curmax = results[0].getArrayMath().getMaximum();
		// Calculate the necessary scale factor
		double rescale_fact = newmax/curmax;
		System.out.printf("Maximum value %f; rescaling by %f",curmax,rescale_fact);
		// Apply the scale factor to the totals
		IArrayIterator a_iter = results[0].getIterator();
		IArrayIterator av_iter = results[1].getIterator();
		while(a_iter.hasNext()) {
			double curval = a_iter.getDoubleNext();
			double curvar = av_iter.getDoubleNext();
			a_iter.setDoubleCurrent(curval*rescale_fact);
			av_iter.setDoubleCurrent(curvar*rescale_fact*rescale_fact);
		}
	}
		    
	// We make a simple mask of all 1s.  It will be two-dimensional regardless
	// of the input rank
	private IArray make_mask(int[] shape) {
		int rank = shape.length;
		int[] new_shape = {shape[rank-2],shape[rank-1]};
		IArray new_mask = Factory.createArray(int.class, new_shape);
		IArrayIterator nmi = new_mask.getIterator();
		while(nmi.hasNext()) nmi.next().setIntCurrent(1);
		return new_mask;
	}
	
	public IGroup getVerticalIntegration_output() {
		return verticalIntegration_output;
	}

	public void setVerticalIntegration_input(IGroup verticalIntegration_input) {
		this.verticalIntegration_input = verticalIntegration_input;
	}

	public void setVerticalIntegration_skip(Boolean verticalIntegration_skip) {
		this.verticalIntegration_skip = verticalIntegration_skip;
	}

	public void setVerticalIntegration_stop(Boolean verticalIntegration_stop) {
		this.verticalIntegration_stop = verticalIntegration_stop;
	}

//	public void setVerticalIntegration_minDist(
//			Double verticalIntegration_minDist) {
//		this.verticalIntegration_minDist = verticalIntegration_minDist;
//	}
//
//	public void setVerticalIntegration_maxDist(
//			Double verticalIntegration_maxDist) {
//		this.verticalIntegration_maxDist = verticalIntegration_maxDist;
//	}

	public void setVerticalIntegration_region(IGroup verticalIntegration_region) {
		this.verticalIntegration_region = verticalIntegration_region;
	}
	
	public void setVertInt_scale (Boolean newval) {
		this.vertint_scale = newval;
	}
	
	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

}
