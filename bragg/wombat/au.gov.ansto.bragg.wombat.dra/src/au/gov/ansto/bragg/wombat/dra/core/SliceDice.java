package au.gov.ansto.bragg.wombat.dra.core;

import java.io.IOException;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.math.EData;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.errorpropagation.ArrayOperations;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/*
 * @author jrh
 * This class allows selection and/or summation of the third dimension in
 * a dataset, if it exists.  The syntax is given in the entry field - see parsespec method.
 * 
 * 
 */

public class SliceDice extends ConcreteProcessor {

	String slicespec = "0";    //The slices to produce
	IGroup slice_input = null;
	IGroup slice_output = null;
	Boolean skip_slice = false;
	String scan_coord = "xxx";    //The variable that is scanning
	private int[][] spec_array = new int[200][2];
	private int no_slices = 0;
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.map;
	@Override
	public Boolean process() throws Exception {
		if(skip_slice) {
			slice_output = slice_input;
			return false;
		}
		// Run through our slice specification, first choosing the appropriate region
		// of the data array, then compacting.
		IArray data_array = ((NcGroup) slice_input).getSignalArray();
		IArray variance_array = ((Plot) slice_input).findVarianceArray();
		int[] initial_dims = data_array.getShape();
		if(initial_dims[0]==1) {
			System.out.print("Cannot slice and dice a single exposure!");
			slice_output=slice_input;
			return false;
		}
		int initial_rank = data_array.getRank();
		System.out.print("dims: ");
		for(int i=0;i<initial_rank;i++) System.out.printf(" %d ", initial_dims[i]);
		System.out.print("\n");
		parse_slicespec(slicespec,initial_dims[0]);
		IArray scan_list = find_scanarray(scan_coord);
		if(scan_list.getRank()!=1) {
			System.out.printf("Nominated scan variable has incorrect rank;ignored");
		}
		if(scan_list.getSize()!=initial_dims[0]) {
			System.out.printf("Nominated scan variable has incorrect length(%d):ignored",scan_list.getSize());
		}
		//Finally, we need the monitor counts.  We borrow a nice routine from the Efficiency creation processor
		EData<IArray> norm_array = getNormalisation();
		if(norm_array==null) {
			int [] j_array = new int[initial_dims[0]];
			for(int i=0;i<initial_dims[0];i++) j_array[i]=1;
			norm_array=new EData<IArray>(Factory.createArray(j_array),Factory.createArray(double.class, new int[] {initial_dims[0]}));
		}
		/*Now work out the normalisation values for the result slices.  the normalisation arrays will
	    have dimensions n less than the data dimensions, where n is the number of detector dimensions.
	    We are collapsing the most rapidly varying dimension.
		 */
		double[] part_n_array = new double[no_slices];
		double[] part_nv_array = new double[no_slices];
		// Find the axis that we are dealing with
		Axis sliced_axis = getfirstAxis();
		IArray axis_data = sliced_axis.getData();
		IArray out_axis_array = Factory.createArray(double.class, new int [] {no_slices});
		IIndex out_axis_index = out_axis_array.getIndex();
		for(int slice_ind=0;slice_ind<no_slices;slice_ind++) {
			//Set up a section of the normalisation array.  Some of the following is overkill for a 1-D structure,
			//but is needed if we have a 2D or greater normalisation array (which is not yet supporteD).
			int [] norm_array_dims = norm_array.getData().getShape();  //expect 1D in real life...
			int [] origin = norm_array_dims.clone();
			for(int oi=1;oi<norm_array_dims.length;oi++) origin[oi]=0;
			int [] shape = norm_array_dims.clone();
			origin[0]=spec_array[slice_ind][0];
			shape[0]=spec_array[slice_ind][1]-spec_array[slice_ind][0];
			System.out.printf("%d: From %d to %d%n", slice_ind, origin[0],origin[0]+shape[0]);
			part_n_array[slice_ind] = norm_array.getData().getArrayUtils().sectionNoReduce(origin,shape,null).getArray().getArrayMath().sum();
			part_nv_array[slice_ind] = norm_array.getVariance().getArrayUtils().sectionNoReduce(origin, shape,null).getArray().getArrayMath().sum();
			//part array and part_v_array now contain the summed normalised values corresponding to the final slice
			//We choose the maximum value as the normalisation value
			// Set the new axis value for this dimension
			IArray axis_vals = axis_data.getArrayUtils().section(new int[] {origin[0]}, new int[] {shape[0]}).getArray();
			out_axis_index.set(slice_ind);
			out_axis_array.setDouble(out_axis_index, axis_vals.getArrayMath().sum()/axis_vals.getSize());
		}
		IArray pna = Factory.createArray(part_n_array);
		IArray pnva = Factory.createArray(part_nv_array);
		double norm_val = pna.getArrayMath().getMaximum();//Now generate the data slices.  We have to take monitor counts and axis values as well
		System.out.printf("Normalising slices to %f%n",norm_val);
		int[] final_dims = initial_dims.clone();
		final_dims[0] = no_slices;
		IArray out_array = Factory.createArray(double.class, final_dims);
		IArray out_v_array = Factory.createArray(double.class, final_dims);
		IIndex norm_index = pna.getIndex();
		IIndex norm_v_index = pnva.getIndex();
		for(int slice_ind=0;slice_ind<no_slices;slice_ind++) {
			//Get the appropriate piece of the normalisation array.  We assume a 1D array here, but
			//this could be extended to higher dimensional arrays with use of sectioning and iteration
			norm_index.set(slice_ind);
			norm_v_index.set(slice_ind);
			double slice_norm = pna.getDouble(norm_index);
			double norm_var = pnva.getDouble(norm_v_index);
			//For each slice, we create a section which we sum, then normalise with error propagation
			int [] origin = initial_dims.clone();
			for(int oi=1;oi<initial_rank;oi++) origin[oi]=0;
			int [] shape = initial_dims.clone();
			origin[0]=spec_array[slice_ind][0];
			shape[0]=spec_array[slice_ind][1]-spec_array[slice_ind][0];
			IArray part_array = data_array.getArrayUtils().sectionNoReduce(origin,shape,null).enclosedIntegrateDimension(0, false).getArray();
			IArray part_v_array = variance_array.getArrayUtils().sectionNoReduce(origin, shape,null).enclosedIntegrateDimension(0, true).getArray();
			// Work out the normalised values
			double norm_ratio = norm_val/slice_norm;
			// Now reuse origin and shape for the outgoing arrays
			origin[0]=slice_ind;shape[0]=1;
			IArray out_array_section = out_array.getArrayUtils().section(origin, shape).getArray();
			IArray out_v_array_sect = out_v_array.getArrayUtils().section(origin, shape).getArray();
			double monerr = 0;           //in case we have no error in monitor value (e.g. time)
			if(norm_var>0) monerr = norm_val*norm_val/(norm_var*norm_var*norm_var);
			IArray[] out_with_var = {out_array_section,out_v_array_sect};
			IArray [] in_with_var = {part_array, part_v_array};
			// First create a scalar for normalisation of each frame
			double[] norm_with_err = {norm_ratio,monerr};
			// This will fill in our out array for us
			ArrayOperations.multiplyByScalar(in_with_var,norm_with_err,out_with_var);
		}
		//Now wrap it all up
		String metadata_string = "Data grouped using slice specification " + slicespec;
		String resultName = "slicedice_result";
		slice_output = PlotFactory.createPlot(slice_input, resultName, dataDimensionType);
		((NcGroup) slice_output).addLog("Apply slice specification" + slicespec + " to get " + resultName);
		PlotFactory.addDataToPlot(slice_output, resultName, out_array, "Sliced data", "Normalised counts",out_v_array);
		// Copy axes across from previous data
		List<Axis> data_axes = ((Plot) slice_input).getAxisList();
		for (int ano=0;ano<data_axes.size();ano++) {
			if(ano==0) PlotFactory.addAxisToPlot(slice_output, 
					sliced_axis.getShortName(),out_axis_array, sliced_axis.getTitle(),
					sliced_axis.getUnitsString(), 0);
			else PlotFactory.addAxisToPlot(slice_output, data_axes.get(ano),data_axes.get(ano).getDimensionName());
		}
		((NcGroup) slice_output).addMetadata("CIF","_pd_proc_info_data_reduction", metadata_string,false);
		// We need the value to which everything has been normalised to be available to
		// subsequent processing steps
		IAttribute our_norm = Factory.createAttribute("normalised_to_val",norm_val);
		// normalised_data.buildResultGroup(signal, stthVector, channelVector, twoThetaVector); 
		slice_output.addOneAttribute(our_norm);
		return false;
	}

	/**
	 * Parse the slice specification.  We produce two simple java arrays containing the
	 * beginning and ending of each slice.
	 * 
	 * @author jrh
	 * @param instring    slice specification (see below)
	 * @param size        size of the array in the sliced dimension
	 *  
	 * 1-3,4-8,2-6: accumulate 1-3 in a single 2D array, 4-8 in a further 2D array etc.
 * If an out-of-range number is entered, the largest possible slice is chosen.  If a single
 * out of range number is entered (or 0) all slices are added together.  To leave all
 * slices separate, skip this processor block.  "/n" will divide the dataset into pieces
 * of n slices each. "~n" will divide the dataset into a total of about n pieces
 * 
	 */
	
	private void parse_slicespec(String instring,int size) {
		//Remove all spaces, unnecessary characters
		String no_space = instring.replaceAll("[^0-9,;/~-]", "");
		System.out.print("Proper string "+no_space + "\n");
		String[] slice_specs = no_space.split(",");
		no_slices = 0;      //initialise
		for(String one_slice: slice_specs) {
			//Check for special characters
			if(one_slice.charAt(0)=='/') {
				int group_size = Integer.parseInt(one_slice.substring(1));
				for(int i=0;i<size;i+=group_size){
					spec_array[no_slices][0] = i;
					spec_array[no_slices][1] = i+group_size;
					no_slices++;
				}
				continue;
			} 
			if(one_slice.charAt(0)=='~') {
				int tot_size = Integer.parseInt(one_slice.substring(1));
				int group_size = (int) Math.ceil((double) size/(double)tot_size);
				for(int i=0;i<size;i+=group_size){
					spec_array[no_slices][0] = i;
					spec_array[no_slices][1] = i+group_size;
					no_slices++;
				}
				continue;
			}
			String[] bounds = one_slice.split("-");
			if(bounds.length < 1) continue;  //ignore bad one
			spec_array[no_slices][0]=Integer.parseInt(bounds[0]);
			if(bounds.length==1) {  //single number, single slice
				spec_array[no_slices][1] = spec_array[no_slices][0]+1;
			} else {
				spec_array[no_slices][1] = Integer.parseInt(bounds[1]);
			}
			no_slices++;             //finished this slice
		}
		System.out.printf("Found %d slices%n", no_slices);
		//Clean up the array.  Note that the ranges are not inclusive
		for(int spec_no=0;spec_no<no_slices;spec_no++) {
			if(spec_array[spec_no][0]>size) spec_array[spec_no][0]=size-1;
			if(spec_array[spec_no][1]>size) spec_array[spec_no][1]=size;
			if(spec_array[spec_no][0]<0) spec_array[spec_no][0]=0;
			if(spec_array[spec_no][1]<1) spec_array[spec_no][1]=1;
			if(spec_array[spec_no][0]>spec_array[spec_no][1]) // must be increasing
				spec_array[spec_no][1] = spec_array[spec_no][0]+1;
		}
	}
	
	void dumpspecarray() {
		for(int i=0;i<no_slices;i++) {
		System.out.printf("%d:%d-%d%n",i,spec_array[i][0],spec_array[i][1]);
		}
	}
	
	/*
	 * Find the variable corresponding to the scan variable; may return null in which case an
	 * axis coordinate should be used instead
	 */
	
	IArray find_scanarray(String scan_coord) throws IOException, SignalNotAvailableException{
		Object scan_array = slice_input.getContainer(scan_coord);
		if(scan_array==null) {
			try {
				scan_array = slice_input.findContainerByPath(scan_coord);
			} catch (Exception e) { }
		}
		if (scan_array instanceof IDataItem){
			scan_array = ((IDataItem) scan_array).getData();
		}
		if (scan_array==null) {//just use the first axis
			scan_array = ((NcGroup) slice_input).getAxesArrayList().get(0);
		}
		return ((IArray) scan_array).getArrayUtils().reduce().getArray();
	}
	
	/*
	 * find something that we can normalise against.  The norm_items list is a list of items to
	 * search for, in descending order of preference.  As soon as we successfully read one of them,
	 * we return.  We put detector time last as that will have a zero associated error, although
	 * of course it is likely to have the largest systematic error.
	 */
	
	private EData<IArray> getNormalisation() {
		IArray mapReading = null;
		int i=0;
		String[] norm_items = {"monitor1_counts","/entry1/bm1_counts","monitor2_counts",
				"bm2_counts","monitor3_counts","bm3_counts","detector_time"};
		for(; mapReading==null&&i<norm_items.length;i++) {
			try{
				mapReading = ((IDataItem) slice_input.findContainerByPath(norm_items[i])).getData();
			}catch (Exception e) {
			}
		}
		if(i<norm_items.length-1)
		    return new EData<IArray>(mapReading,mapReading.copy());
		if(i==norm_items.length-1)           //detector time
			return new EData<IArray>(mapReading,Factory.createArray(double.class, mapReading.getShape()));
		return null;
	}

	private Axis getfirstAxis() throws SignalNotAvailableException {
		return ((Plot) slice_input).getAxis(0);
	}
	
	public IGroup getSlice_output() {
		return slice_output;
	}
	public void setSlicespec(String slicespec) {
		this.slicespec = slicespec;
	}
	
	public void setSlice_input(IGroup slice_input) {
		this.slice_input = slice_input;
	}
	public void setSkip_slice(Boolean skip_slice) {
		this.skip_slice = skip_slice;
	}
	
	public void setScan_coord(String scan_loc){
		this.scan_coord = scan_loc;
	}

}
