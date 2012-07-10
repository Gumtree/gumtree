package au.gov.ansto.bragg.nbi.dra.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.ISliceIterator;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.errorpropagation.ArrayOperations;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * This class is the concrete processor of the efficiency correction algorithm for wombat algorithm group.
 *  
 * @author nxi
 * @author jhester
 * @version 1.0
 * @since V2.2
 */
public class EfficiencyCorrection extends ConcreteProcessor {
	IGroup efficiencyCorrection_inputPlot = null;
	Boolean efficiencyCorrection_skip = false;
	Boolean efficiencyCorrection_stop = false; 
	Boolean eff_matchsize = false;   //merge/expand efficiency numbers as necessary
	Double efficiencyCorrection_threshold = 0.1;
	IGroup efficiencyCorrection_output = null;
	URI efficiencyCorrection_mapURI = null;
	private double [][] efficiency_data = null; 
	private double [][] efficiency_variance = null;
	private int [][] pixel_ok_map = null;
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.map;

	/* Perform an efficiency correction on the input dataset, with error propagation.
	 * We treat arbitrary-dimensional datasets in this implementation.
	 */
	
	public Boolean process() throws Exception{
		if (efficiencyCorrection_skip){
			efficiencyCorrection_output = efficiencyCorrection_inputPlot;
		}else{
			IArray dataArray = ((Plot) efficiencyCorrection_inputPlot).findSignalArray();
			IArray variance_array = ((Plot) efficiencyCorrection_inputPlot).getVariance().getData();
			if (efficiency_data == null){    //File reading failed.  Need to advise user of this
				System.out.print("No efficiency correction data:Fail");
				throw new Exception("No efficiency correction data found: skip efficiency correction or change file location");
			}

			/* turn efficiency data into an array...*/
			IArray [] eff_val_and_var = {Factory.createArray(efficiency_data),Factory.createArray(efficiency_variance)};
			int[] dataShape = dataArray.getShape();
			int dlength = dataShape.length;
			IArray trim_pixok = Factory.createArray(pixel_ok_map);
			// Now we do the actual calculation.  We need to create a new Array to hold the result
			IArray outarray = Factory.createArray(double.class, dataShape);
			IArray out_variance = Factory.createArray(double.class,dataShape);
			/* transpose our efficiency array to match data values */
			eff_val_and_var[0] = eff_val_and_var[0].getArrayUtils().transpose(1,0).getArray();
			eff_val_and_var[1] = eff_val_and_var[1].getArrayUtils().transpose(1,0).getArray();
			trim_pixok = trim_pixok.getArrayUtils().transpose(1,0).getArray();
			// Check that the dimensions match the data dimensions
			int[] effdim = eff_val_and_var[0].getShape();
			if(effdim[0]!=dataShape[dlength-2]||effdim[1]!=dataShape[dlength-1]) {
				String errorstring = String.format("Efficiency correction file dimensions %d x %d do not match data dimensions %d x %d",
						 effdim[0],effdim[1],dataShape[dlength-2],dataShape[dlength-1]);
				throw new Exception(errorstring);
			}
			/* trim efficiency array to be correct length */
			int [] eff_trim = {dataShape[dlength-2],dataShape[dlength-1]};
			int [] eff_origin = {0,0};
			eff_val_and_var[0] = eff_val_and_var[0].getArrayUtils().section(eff_origin,eff_trim).getArray();
			eff_val_and_var[1] = eff_val_and_var[1].getArrayUtils().section(eff_origin,eff_trim).getArray();
			trim_pixok = trim_pixok.getArrayUtils().section(eff_origin, eff_trim).getArray();
			// Iterate over 2-dimensional frames
			ISliceIterator higher_dim_iter = dataArray.getSliceIterator(2);
			ISliceIterator high_dim_out_it = outarray.getSliceIterator(2);
			ISliceIterator var_array_iter = variance_array.getSliceIterator(2);
			ISliceIterator var_out_arr_it = out_variance.getSliceIterator(2);
			while (higher_dim_iter.hasNext()){
				IArray[] out_with_var = new IArray[] {high_dim_out_it.getArrayNext(),var_out_arr_it.getArrayNext()};
				IArray [] in_with_var = new IArray[] {higher_dim_iter.getArrayNext(),var_array_iter.getArrayNext()};
				// Now we have some 2D arrays to operate on, lets go to it!
				// First create a scalar for normalisation of each background value
				ArrayOperations.multiply(eff_val_and_var,in_with_var,out_with_var);
			}
			// include pixel_ok_map in output file as item name "contributors"
			String resultName = "efficiencyCorrection_result";
			efficiencyCorrection_output = PlotFactory.createPlot(efficiencyCorrection_inputPlot, resultName, dataDimensionType);
			((NcGroup) efficiencyCorrection_output).addLog("apply efficiency correction algorithm to get " + resultName);
			PlotFactory.addDataToPlot(efficiencyCorrection_output, resultName, outarray, "Efficiency corrected data", "Counts", out_variance);
			IDataItem contribs = Factory.createDataItem(null, efficiencyCorrection_output, "contributors",trim_pixok);
			List<Axis> data_axes = ((Plot) efficiencyCorrection_inputPlot).getAxisList();
			for (Axis oneaxis : data_axes) {
				PlotFactory.addAxisToPlot(efficiencyCorrection_output, oneaxis,oneaxis.getDimensionName());
			}
			efficiencyCorrection_output.addDataItem(contribs);
		}
		return efficiencyCorrection_stop;
	}

/*
 * Read in an efficiency correction file.  The file format is:
 * 1. Any line starting with '#' is a comment
 * 2. Other lines are, for 1D-data: 3 space-delimited values: tube_no   efficiency    error in efficiency
 *    For 2D data: a sequence of efficiency, variance values for each tube
 * The efficiency is a number that must be multiplied by an observed value in order to find the result.
 * This means that the larger an efficiency number, the more the signal from that tube is dampened
 * 
 */
	public void setEfficiencyCorrection_mapURI(URI mapURI) 
	throws Exception {
		this.efficiencyCorrection_mapURI = mapURI;
		URI uri = mapURI;
		File efficiencyFile = null;
		try {
				efficiencyFile = new File(mapURI);				
			} catch (Exception e) {
				// TODO: handle exception
				efficiencyFile = null;
			}
			if (efficiencyFile == null || ! efficiencyFile.exists()){
				String filename = System.getProperty("dav.efficiencies.default","");
				IFileStore fileStore = EFS.getStore(new URI(filename));
				efficiencyFile = fileStore.toLocalFile(EFS.NONE, new NullProgressMonitor());
				System.out.println("loading default efficiency map from "+efficiencyFile.toString());
			}
		    read2DEfficiencyFile(efficiencyFile);
		}

	/** Read ascii data in the following format:
	 * 1. Anything after a '#' is ignored until the end of the line (ie a comment)
	 * 2. The first two values are the data dimensions (# horizontal pixels, # vertical pixels).
	 * 3. The values are given as a sequence of (efficiency, variance) numbers starting from 0,0 and
	 * varying most rapidly in the vertical direction.
	 * 
	 * The efficiency value is multiplied by the observed value to obtain the corrected value.
	 * 
	 * May fail
	 * in vaguely pathological cases e.g. a comment on an empty last line.  Note that there is an implicit
	 * pixel_ok map included in the data: any efficiency value of zero is considered to be a non-contributor.
	 * We therefore fill a separate structure with 1s and 0s as we read in the efficiency file.
	 * It may be a better use of the Cicada structure to make this pixel_ok map be a separate output of
	 * this algorithm which is read in by those routines that need one.  For now it is passed along in
	 * the "contributors" dataitem and used during geometry correction and vertical integration.
	 * 
	 * Note the first two numbers read are the data dimensions.  This allows us to economise by not assigning
	 * a larger array than necessary
	 * 
	 */
	
	private void read2DEfficiencyFile(File eff_data) {
		boolean first=true;             //First two numbers are dimensions vertical pixels x horizontal pixels
		int vert_pix_no=0;
		int horiz_pix_no=0;
		BufferedReader getdata;
		try {
		    getdata = new BufferedReader(new FileReader(eff_data));
		} catch (FileNotFoundException e) {
			return;
		}
    	String next_line;
    	String [] these_numbers;
    	/* We store efficiency data with this structure to streamline passing it to the array multiply 
    	 * function in the main correction
    	 */
	   	try {
	   		int horiz_pix=-1; int vert_pix = 0;  // initialise position counters
	   		for(next_line=getdata.readLine();next_line!=null;) {
	   			// process blanks and comments
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
	    	   // Input numbers into our storage arrays
	    	these_numbers = next_line.trim().split(" +");
	    	// First time we expect two numbers alone on a single line.  Surplus numbers are ignored
	    	if (first) {
	    		horiz_pix_no = Integer.parseInt(these_numbers[0]);
	    		vert_pix_no = Integer.parseInt(these_numbers[1]);
	    		efficiency_data = new double[horiz_pix_no][vert_pix_no];
	    		efficiency_variance = new double[horiz_pix_no][vert_pix_no];
	    		pixel_ok_map = new int[horiz_pix_no][vert_pix_no];
	    		first = false;
	    		next_line = getdata.readLine();
	    		continue;  //get next line
	    	}
	    	for (String tn : these_numbers) {
	    	   if (vert_pix % (vert_pix_no*2) == 0) {
	    		   horiz_pix++; vert_pix = 0;       //reset vertical pixel counter
	    	   }
	    	   if (vert_pix % 2 == 0)  //even-numbered entry, therefore variance
	    	     efficiency_data[horiz_pix][vert_pix/2]=Double.parseDouble(tn);
	    	   else
	    	   {
	    		 double this_val = Double.parseDouble(tn);
	    	     efficiency_variance[horiz_pix][vert_pix/2]=this_val;
	    	     // pick up invalid pixels
	    	     if(this_val > 0.0) pixel_ok_map[horiz_pix][vert_pix/2]= 1;
	    	   }
	    	   vert_pix++;            // move to next entry
	    	}
	    	next_line=getdata.readLine();
	    	}
	   	} catch (IOException e) {
	   		return;
	   	}
	}

	public IGroup getEfficiencyCorrection_output() {
		return efficiencyCorrection_output;
	}


	public void setEfficiencyCorrection_inputPlot(IGroup efficiencyCorrection_scanData) {
		this.efficiencyCorrection_inputPlot = efficiencyCorrection_scanData;
	}


	public void setEfficiencyCorrection_skip(Boolean efficiencyCorrection_skip) {
		this.efficiencyCorrection_skip = efficiencyCorrection_skip;
	}


	public void setEfficiencyCorrection_stop(Boolean efficiencyCorrection_stop) {
		this.efficiencyCorrection_stop = efficiencyCorrection_stop;
	}


	public void setEfficiencyCorrection_threshold(
			Double efficiencyCorrection_threshold) {
		this.efficiencyCorrection_threshold = efficiencyCorrection_threshold;
	}
	
	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}


}

