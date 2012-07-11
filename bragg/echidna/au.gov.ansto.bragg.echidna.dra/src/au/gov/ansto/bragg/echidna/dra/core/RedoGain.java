package au.gov.ansto.bragg.echidna.dra.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.DivideByZeroException;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.interfaces.ISliceIterator;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class RedoGain extends ConcreteProcessor {
	/**
	 * This class recalculates the 1D gain of Echidna tubes in those cases where tube scans have
	 * overlapped.  It should be run prior to stitching.  It will calculate new gains, and apply
	 * those gains to the 2D data.
	 *  
	 * @author James Hester
	 * @version 1.0
	 */
	IGroup gain_input = null;
	Boolean gain_skip = false;
	Boolean gain_stop = false;
	IGroup gain_output = null;
	Boolean gain_reversed = true;
	Double gain_limit = 0.0005;       // Give up refinement after this value
	Integer gain_steps = 1000;         // Give up refinement after this many steps
	Boolean ang_correct = false;      // Apply angular corrections
	public IArray weighted_data = null;
	public IArray variance_data = null;
	private double [] correction_data = null;
	private IArray outarray = null; // For storing corrected data
	private IArray gain_variance = null; // For storing error in estimated gain
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.map;
	@Override
	public Boolean process() throws Exception {
		/* The basis of this algorithm is an old paper on scaling datasets by Monahan, Schiffer and Schiffer,
		 * Acta Cryst. (1967) 22, p322.  Scale factors between datasets are iteratively refined according to a simple
		 * procedure.  Note also an alternative Maximum Entropy approach given by Collins (1984) Acta Cryst. A40,
		 * p705-708.
		 * 
		 * Our adaptation of the Monahan et al procedure is to define the full range of 1D points as F_h.  
		 * Each tube's observation
		 * of these points is given by F_hl, where l indexes the tube (a tube is now analogous to the
		 * X-ray film of the original paper), and G_l is the inverse gain for each tube.
		 * 
		 * A wrinkle in this scheme is the fact that our tubes do not perfectly overlap when scanned due to non-ideal
		 * tube spacing.  We can correct for this by first interpolating the 1D data onto a regular grid.  Prototyping
		 * in Python indicates that there is a significant improvement in chi squared when this interpolation is done.
		 * 
		 * */
		if(gain_skip || gain_input == null) {
			gain_output = gain_input;
			return false;
		}
		//Initial setup; get data dimensions
		String metadata_string = "";
		IArray inDat = ((Plot) gain_input).findSignalArray();
		int [] dataShape = inDat.getShape();
		int drank = inDat.getRank();
		IArray inVar = ((Plot) gain_input).getVariance().getData();
		IDataItem cbs = gain_input.findDataItem("contributors");
		IArray contribs = null;
		if(cbs != null) {
			contribs = cbs.getData();
		} else   // fill with ones
		{
			int [] revShape = {dataShape[2],dataShape[1]};  /* contributor map is tube first index, vertical pixel second index */
			contribs = Factory.createArray(int.class, revShape);
			for (IArrayIterator j = contribs.getIterator();j.hasNext();j.next().setIntCurrent(1)) {};
		}
		// Obtain angles using NeXuS conventions
		IArray scanStep = null;
		IArray array2theta = null;
		IArray vOffset = null;
		String tube_units = "degrees";
		List<Axis> axes_di = ((Plot) gain_input).getAxisList();
		double radius = gain_input.findDataItem("radius").readScalarDouble();
		if(radius<0.1) radius = 1250.0; //for one week in April 2009 it was zero
		if(axes_di.size()!=3) {       //we have bad input NeXuS data
			scanStep = gain_input.findDataItem("scanStep").getData();
			array2theta = gain_input.findDataItem("xoffset").getData();
			tube_units = "mm";        //We just know this, unit attribute isn't set
			vOffset = gain_input.findDataItem("verticalOffset").getData();
		} else {
			// The step positions will be the most slowly-varying element, which is earliest in the array
			scanStep = ((Plot) gain_input).getAxis(0).getData();
			// The tube positions at each step we assume to be the fastest varying element
			array2theta = ((Plot) gain_input).getAxis(2).getData();
			// Units may be degrees or mm depending on when they were generated...
			tube_units = ((Plot) gain_input).getAxis(2).getUnitsString();
			vOffset = ((Plot) gain_input).getAxis(1).getData(); //vertical pixel: 2nd dimension
		}
		IIndex index = array2theta.getIndex();
		index.set(0);
		Double twotheta0 = array2theta.getDouble(index); //first value of left/rightmost tube
		double[] scanStepData = ConverterLib.get1DDouble(scanStep);
		double roughbinSize = Math.abs(scanStepData[0]-scanStepData[scanStepData.length-1])/(scanStepData.length-1);//assume monotonic increasing
		System.out.println("First tube, first position: "+ (twotheta0+scanStepData[0]));
		/* extract the tube separation from the data.*/
		int nTubes = dataShape[drank-1];
		double tube_sep = Math.abs(twotheta0-array2theta.getDouble(index.set(nTubes-1)))/(array2theta.getSize()-1);
		int non_overlap_points = (int) Math.round(tube_sep/roughbinSize);
		System.out.printf("Tubes are notionally separated by %d steps%n",non_overlap_points);
		// Exit early if there is no overlap or incorrect overlap
		if(dataShape[0]/non_overlap_points < 2) {
			System.out.printf("No refinement of gain: %d steps for %d step tube separation%n",dataShape[0],non_overlap_points);
			gain_output = gain_input;
			return false;
		} 
		System.out.printf("Proceeding with gain calculation, overlap factor %d",dataShape[0]/non_overlap_points);
		/* Now we have all the auxilliary data, we start actually processing it... */
		//Initialise the gain array with ones
		IArray start_gain = Factory.createArray(double.class, new int[] {dataShape[drank-1]});
		IArrayIterator sgi = start_gain.getIterator();
		while(sgi.hasNext()) sgi.next().setDoubleCurrent(1.0);
		//Step one: vertically integrate
		IArray one_d_data = inDat.getArrayUtils().integrateDimension(1,false).getArray();
		variance_data = inVar.getArrayUtils().integrateDimension(1, false).getArray();
		//Now weight the data
		weighted_data = Factory.createArray(double.class, one_d_data.getShape());
		IArrayIterator odi = one_d_data.getIterator();
		IArrayIterator vdi = variance_data.getIterator();
		IArrayIterator wdi = weighted_data.getIterator();
		while(wdi.hasNext()) {//weight and avoid zero variance
			double thisvar = vdi.getDoubleNext();
			vdi.setDoubleCurrent(Math.max(thisvar, 1.0));  //get rid of 0 values
			wdi.next().setDoubleCurrent(odi.getDoubleNext()/thisvar); 
		}
		//Some debugging output for our edification.  Characterise the signal that we are trying to
		//fit by printing out the sum from all points of each tube
		double[] summed_tubes = (double []) one_d_data.getArrayUtils().integrateDimension(0, false).copyTo1DJavaArray();
		System.out.printf("Sum signal for each tube%n");
		int i=0;
		for(double qq:summed_tubes) { System.out.printf("%d:%f ", i,qq); i++;}
		//Our routines are written such that the first index refers to the tube number.  We
		//need to transpose.
		weighted_data = weighted_data.getArrayUtils().transpose(1, 0).getArray();
		variance_data = variance_data.getArrayUtils().transpose(1,0).getArray();
		//Count along one further than the last all-zero tube.  We rely on the efficiency correction
		//to have set the efficiency to zero for blocked tubes.  We could also be a bit more sophisticated
		//in using partially covered tubes, but we leave that for later.  TODO: allow partially covered
		//tubes to be included.
		//Note we always drop the first tube in this algorithm, as we don't know if it
		//is partially blocked.
		int dropped = 0;
		for(dropped=0;dropped<10;dropped++) if(summed_tubes[dropped]>1.0) break;
		weighted_data = weighted_data.getArrayUtils().section(new int[] {dropped+1,0}, new int[] {nTubes-dropped-1,dataShape[0]}).getArray();
		variance_data = variance_data.getArrayUtils().section(new int[] {dropped+1,0}, new int[] {nTubes-dropped-1,dataShape[0]}).getArray();
		IArray gain_array = null;
		gain_array = find_gain(start_gain,non_overlap_points,false);
		IArray old_gain = gain_array.copy();
		double gain_shift = gain_array.getArrayMath().toAdd(-1.0).getNorm();
		int iter_counter = 0;
		while(gain_shift>gain_limit & iter_counter<gain_steps) {
			gain_array = find_gain(gain_array,non_overlap_points,false);
			gain_shift = gain_array.getArrayMath().toAdd(old_gain.getArrayMath().scale(-1.0)).getNorm();
			old_gain = gain_array;
			iter_counter++;
//			System.out.printf("%d: Gain shift now: %f%n", iter_counter,gain_shift);
		}
		// Do a final run to set variances.  We have to adjust our 1d data to do this.
		IArray dropped_1d_data = one_d_data.getArrayUtils().transpose(1, 0).getArray();
		dropped_1d_data = dropped_1d_data.getArrayUtils().section(new int[] {dropped+1,0}, new int[] {nTubes-dropped-1,dataShape[0]}).getArray();
		calc_variance(dropped_1d_data,outarray,gain_array,non_overlap_points);
		// Debugging printout
		double[] g = (double[]) gain_array.getArrayUtils().copyTo1DJavaArray();
		double[] ge= (double[]) gain_variance.getArrayUtils().copyTo1DJavaArray();
		i=0;
		for(double gg:g) { System.out.printf("%d:%f(%f) ",i,gg,Math.sqrt(ge[i]));i++;}
		//Now apply the gains across the entire 3D dataset, including error propagation
		IArray full_out_array = Factory.createArray(double.class, new int[] {dataShape[2],dataShape[1],dataShape[0]});
		IArray full_out_variance = Factory.createArray(double.class, new int[] {dataShape[2],dataShape[1],dataShape[0]});
		//We have a (nsteps,nvert,ntubes) array where we want to iterate over tubes;
		ISliceIterator foai = full_out_array.getSliceIterator(2);
		ISliceIterator fovi = full_out_variance.getSliceIterator(2);
		IArray transpose_inDat = inDat.getArrayUtils().transpose(2, 0).getArray();
		IArray transpose_inVar = inVar.getArrayUtils().transpose(2, 0).getArray();
		ISliceIterator in_iterator = transpose_inDat.getSliceIterator(2);
		ISliceIterator var_iterator = transpose_inVar.getSliceIterator(2);
		//First copy the tubes that we haven't refined without change
		for(i=0;i<dropped+1;i++) {
			IArray thisslice = foai.getArrayNext();
			IArray thisvariance = fovi.getArrayNext();
			in_iterator.getArrayNext().getArrayUtils().copyTo(thisslice);
			var_iterator.getArrayNext().getArrayUtils().copyTo(thisvariance);
		}
		//Now copy the ones we have refined
		IArrayIterator gi = gain_array.getIterator();
		IArrayIterator gei = gain_variance.getIterator();  //not this is error, not variance
		while(foai.hasNext()){
			double giValue = gi.getDoubleNext();
			IArray thisslice = foai.getArrayNext();
			IArray thisvar = fovi.getArrayNext();
			in_iterator.getArrayNext().getArrayUtils().copyTo(thisslice);
			var_iterator.getArrayNext().getArrayUtils().copyTo(thisvar);
			thisslice.getArrayMath().scale(1.0/giValue);
			thisvar.getArrayMath().add(thisslice.getArrayMath().toScale(1.0/giValue).power(2.0).scale(gei.getDoubleNext()));
			thisvar.getArrayMath().scale(1.0/(giValue*giValue));                    //variance of A/B is var(A)*1/B^2 + var(B)*(A/B^2)^2
		}
		
	String resultName = "redo_gain_result";
	gain_output = PlotFactory.createPlot(gain_input, resultName, dataDimensionType);
	((Plot) gain_output).addLog("apply 1D gain correction algorithm to get " + resultName, null);
	PlotFactory.addDataToPlot(gain_output, resultName, full_out_array.getArrayUtils().transpose(2,0).getArray(), "Data after gain refinement", "Counts", full_out_variance.getArrayUtils().transpose(2, 0).getArray());
	PlotFactory.addAxisToPlot(gain_output, "verticalOffset", vOffset, "Vertical pixel offset", "mm", 1);
	PlotFactory.addAxisToPlot(gain_output, "two_theta_vector", array2theta, "Two theta", "degrees", 2);
	PlotFactory.addAxisToPlot(gain_output, "scanstep", scanStep, "Scan step", "Step number", 0);
	IDataItem pixel_ok = Factory.createDataItem(gain_output, "contributors", contribs);
	gain_output.addDataItem(pixel_ok);
	metadata_string = String.format("Relative tube gains were recorrected by comparing 1D signals from overlapping%n");
	metadata_string += String.format("scans.  The first %d tubes were not included. The adjusted gains are tabulated below.%n",dropped+1);
	metadata_string += String.format("Tube     Gain(Error)%n");
	for(i=0;i<g.length;i++) {
		metadata_string += String.format("%d: %f(%f)%n", i+dropped,g[i],Math.sqrt(ge[i]));
	}
	((NcGroup) gain_output).addMetadata("CIF", "_pd_proc_info_data_reduction", metadata_string, false);
    return gain_stop;
	}
		
	/*A single iterative step to find the gain.  We use class variables weighted_data
	 * If final_run is set, additional diagnostic and error values are set
	*/
	public IArray find_gain(IArray old_gain, int steps_per_tube, boolean final_run) 
	throws InvalidRangeException, ShapeNotMatchException, DivideByZeroException {
		IArray new_gain = null;
		IArray scaled_data = Factory.createArray(double.class, weighted_data.getShape());
		// Scale the data;     G_l(rho-1) * wd) in paper
		ISliceIterator wdi = weighted_data.getSliceIterator(1);
		ISliceIterator sdi = scaled_data.getSliceIterator(1);
		IArrayIterator gdi = old_gain.getIterator();
		while(gdi.hasNext()) {
			IArray oneslice = wdi.getArrayNext();
			IArray outslice = sdi.getArrayNext(); 
			outslice.getArrayMath().add(oneslice);
			outslice.getArrayMath().scale(gdi.getDoubleNext());
		}
		IArray summed_data = shift_tube_add(scaled_data,steps_per_tube);
		// Scale the variance
		gdi = old_gain.getIterator();   //reset
		ISliceIterator vdi = variance_data.getSliceIterator(1);
		IArray scaled_variance = Factory.createArray(double.class, variance_data.getShape());
		ISliceIterator svi = scaled_variance.getSliceIterator(1);
		while(gdi.hasNext()) {
			IArray oneslice = vdi.getArrayNext();
			IArray outslice = svi.getArrayNext();
			outslice.getArrayMath().add(oneslice);
			outslice.getArrayMath().scale(1.0/Math.pow(gdi.getDoubleNext(),2));
			outslice.getArrayMath().eltInverse();
		}
		IArray summed_denominator = shift_tube_add(scaled_variance,steps_per_tube);
		outarray = summed_data.getArrayMath().toEltDivide(summed_denominator).getArray();  //Best estimate of actual intensity
		// Now calculate the next round of gains
		summed_data = shift_mult_tube_add(weighted_data,outarray,steps_per_tube,false);
		summed_denominator = shift_mult_tube_add(variance_data.getArrayMath().toEltInverse().getArray(),
				outarray,steps_per_tube,true);
		new_gain = summed_data.getArrayMath().toEltDivide(summed_denominator).getArray();
		// If the last time we calculate esds on the gain
		return new_gain;
	}
	
	/* Calculate the variance in the gain calculation
	 * 
	 * The principle is to get the raw data array in data, and a model array calculated by the main gain calculation.  These two arrays can
	 * be used to find the predicted gain at each point, which in the ideal world would be identical for each tube.  The deviation from
	 * identity can be used to find the error.  Note that the model array is flat, as it is just an estimate of the true intensity as a
	 * function of angle, whereas the data array is a series of 1D slices, each slice corresponding to the data from a single tube.
	 *
	 * Note that this routine is unweighted
	 */
	public IArray calc_variance(IArray data, IArray model, IArray gain, int steps_per_tube) 
	    throws ShapeNotMatchException, InvalidRangeException {
		gain_variance = Factory.createArray(double.class, gain.getShape());
		ISliceIterator oneslice = data.getSliceIterator(1);
		IArrayIterator gei = gain_variance.getIterator();
		for(int tubeno=0;tubeno<gain.getSize();tubeno++) {
			// Calculate position in output array
			IArray outsection = model.getArrayUtils().section(new int[] {steps_per_tube*tubeno}, new int[] {data.getShape()[1]}).getArray();
			// Calculate apparent gain
			IArray point_gain = oneslice.getArrayNext().getArrayMath().toEltDivide(outsection).getArray();
			double ave_gain = point_gain.getArrayMath().sum()/point_gain.getSize();
			double ms_dev = point_gain.getArrayMath().scale(-1.0).add(ave_gain).power(2).sum()/point_gain.getSize();
			gei.next().setDoubleCurrent(ms_dev);
		}
		return gain_variance;
	}
	
	public IArray shift_tube_add(IArray indata, int steps_per_overlap) throws ShapeNotMatchException, InvalidRangeException{
		/* A utility routine which creates a 1D array out of a series of 2D arrays, where each member 
		 * of the 2D array is shifted by steps_per_overlap relative to the previous member.
		 * A positive steps_per_overlap means that step i in the n+1th slice is added to pixel 
		 * (i-tube_offset) in the nth slice.
       This corresponds to the detector scanning in positive 2th direction. The result
       array is expanded by offset*notubes.Note that this means only positive offsets make sense.
       If pixel_mask is set, it will have a 1 for pixel positions that are valid, and 0 otherwise
		 */
		int[] incoming_shape = indata.getShape();
		IArray return_array = Factory.createArray(double.class, 
				new int[] {incoming_shape[0]*steps_per_overlap + incoming_shape[1]});
		ISliceIterator oneslice = indata.getSliceIterator(1);
		for(int tubeno=0;tubeno<incoming_shape[0];tubeno++) {
			int[] origin = new int[] {tubeno*steps_per_overlap};
			IArray rta = return_array.getArrayUtils().section(origin, new int[] {incoming_shape[1]}).getArray();
			rta.getArrayMath().add(oneslice.getArrayNext());
		}
		return return_array;
	}
	
	public IArray shift_mult_tube_add(IArray fixed_array, IArray sliding_vector, int steps_per_overlap, boolean squareit) throws ShapeNotMatchException, InvalidRangeException {
		/* A utility routine that in some sense calculates the inverse of shift_tube_add.Fixed array is a
       set of tube scans, and sliding_array is a single line of intensity values
       produced by the shift_tube_add routine. We multiply the two
       vectors, shifting sliding vector by offset each time it is multiplied by a section 
       of fixed_array.  Points in sliding vector 
       that are beyond the end of fixed_array are ignored. If squareit is true,
       the result of the multiplication is squared before summing.  The result
       will be a vector with length corresponding to the number of tubes
		 */
		int numtubes = fixed_array.getShape()[0];
		int scanlen = fixed_array.getShape()[1];
		IArray result = Factory.createArray(double.class, new int[] {numtubes});
		IIndex rsi = result.getIndex();
		ISliceIterator fsi = fixed_array.getSliceIterator(1);
		   for(int tubeno=0;tubeno<numtubes;tubeno++) {
			   rsi.set(tubeno);
			   IArray window = sliding_vector.getArrayUtils().section(new int[] {tubeno*steps_per_overlap}, new int[] {scanlen}).getArray();
			   IArray fixed_window = fsi.getArrayNext();
			   IArray outarray = window.getArrayMath().toEltMultiply(fixed_window).getArray();
			   if(squareit) outarray.getArrayMath().eltMultiply(window); //square of window values
			   result.setDouble(rsi, outarray.getArrayMath().sum());
		   }
		   return result;
	}
	
	/* A routine to convert from bin boundaries to bin centres.  Proper resting place would be not in this
	 * concrete processor but in a general library.
	 */
	private IArray findBinCentres(IArray bin_borders) {
		double bin_lowedge = 0;
		int[] out_len = {bin_borders.getShape()[0]-1};
		IArray centre_array = Factory.createArray(double.class, out_len);
		IIndex c_index = centre_array.getIndex();
		IIndex o_index = bin_borders.getIndex();
		double bin_highedge = 0;                       //allocate memory outside the loop
		o_index.set(0);
		bin_lowedge = bin_borders.getDouble(o_index);
		for(int c_i=0;c_i<out_len[0];c_i++) {
			c_index.set0(c_i);
			o_index.set0(c_i+1);
			bin_highedge = bin_borders.getDouble(o_index);
			centre_array.setDouble(c_index, bin_lowedge + (bin_highedge-bin_lowedge)/2.0);
			bin_lowedge = bin_highedge;                 //set ready for next time through the loop
		}
		return centre_array;
	}
		/* Apply angular corrections to the polar angle and stth arrays.  Correcting all 6400
		 * entries is not optimal, as the stitching routine only looks at a few particular
		 * angular values.  However, doing it this way guards against future changes in the 
		 * stitching routine failing to account for angular correction
		 */
		private IArray do_angular_correction(IArray polar_angles, double [] corrections) {
			return polar_angles;
		}

		/* The angular correction file is a sequence of numbers corresponding to a correction
		 * to be applied to the positions held in the input file under 'polar angle' and 'stth'.  
		 */
		private double[] read_correction_file(File ang_file) {
			double [] correction_data=new double[200];
			BufferedReader getdata;
			try {
				getdata = new BufferedReader(new FileReader(ang_file));
			} catch (FileNotFoundException e) {
				return correction_data;   //all zero at this stage
			}
			String next_line;
			String [] these_numbers;
			int current=0;           //pointer to current position in double array
			try {
				// Find the next line with data, ignoring empty lines and comments
				for(next_line=getdata.readLine();next_line!=null;) {
					if (next_line.equals("")) {   /* watch out for blank lines */
						next_line=getdata.readLine();
						continue;
					}
					int comment_start = next_line.indexOf("#");   /* anything after a hash is ignored */
					if(comment_start>0) {
						next_line = next_line.substring(0, comment_start);
					} else if (comment_start==0) {                /* nothing on this line at all */
						next_line=getdata.readLine();
						continue;
					}
					these_numbers = next_line.trim().split(" +");
					for(String this_number: these_numbers) {
						correction_data[current++]=Double.parseDouble(this_number);
					}
					next_line=getdata.readLine();
				}
			} catch (IOException e) {
				return correction_data;
			}
			return correction_data;
		}

		public IGroup getGain_output() {
			return gain_output;
		}

		public void setGain_input(IGroup gainInput) {
			gain_input = gainInput;
		}

		public void setGain_skip(Boolean gainSkip) {
			gain_skip = gainSkip;
		}

		public void setGain_stop(Boolean gainStop) {
			gain_stop = gainStop;
		}

		public void setGain_reversed(Boolean gainReversed) {
			gain_reversed = gainReversed;
		}

		public void setGain_correct(Boolean angCorrect) {
			ang_correct = angCorrect;
		}

		public Double setGain_limit() {
			return gain_limit;
		}

		public void setGain_steps(Integer gainSteps) {
			gain_steps = gainSteps;
		}


}
