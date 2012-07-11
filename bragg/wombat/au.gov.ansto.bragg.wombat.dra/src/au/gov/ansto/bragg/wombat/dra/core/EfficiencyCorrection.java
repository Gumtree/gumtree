package au.gov.ansto.bragg.wombat.dra.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.ISliceIterator;
import org.gumtree.data.utils.Utilities;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.errorpropagation.ArrayOperations;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;
import au.gov.ansto.bragg.wombat.dra.internal.Activator;

/**
 * This class is the concrete processor of the efficiency correction algorithm for wombat algorithm group.
 *  
 * @author nxi
 * @author jhester
 * @version 1.0
 * @since V2.2
 */
public class EfficiencyCorrection extends ConcreteProcessor {
	IGroup efficiencyCorrection_scanData = null;
	Boolean efficiencyCorrection_skip = false;
	Boolean efficiencyCorrection_stop = false; 
	Boolean eff_matchsize = false;   //merge/expand efficiency numbers as necessary
	Double efficiencyCorrection_threshold = 0.1;
	IGroup efficiencyCorrection_output = null;
	URI efficiencyCorrection_mapFilename = null;
	private double [][] efficiency_data = null; 
	private double [][] efficiency_variance = null;
	private Map<String,String> toks_vals = null;   //Metadata from the efficiency file
	private int [][] pixel_ok_map = null;
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.map;

	/* Perform an efficiency correction on the input dataset, with error propagation.
	 * We treat arbitrary-dimensional datasets in this implementation.
	 */
	
	public Boolean process() throws Exception{
		if (efficiencyCorrection_skip){
			efficiencyCorrection_output = efficiencyCorrection_scanData;
		}else{
			IArray dataArray = ((Plot) efficiencyCorrection_scanData).findSignalArray();
			IArray variance_array = ((Plot) efficiencyCorrection_scanData).getVariance().getData();
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
			/* transpose our efficiency array to match data values
			eff_val_and_var[0] = eff_val_and_var[0].transpose(1,0);
			eff_val_and_var[1] = eff_val_and_var[1].transpose(1,0);
			trim_pixok = trim_pixok.transpose(1,0);*/
			// Check that the dimensions match the data dimensions
			int[] effdim = eff_val_and_var[0].getShape();
			if(effdim[0]!=dataShape[dlength-2]||effdim[1]!=dataShape[dlength-1]) {
				String errorstring = String.format("Efficiency correction file dimensions %d x %d do not match data dimensions %d x %d",
						 effdim[0],effdim[1],dataShape[dlength-2],dataShape[dlength-1]);
				throw new Exception(errorstring);
			}
			/* trim efficiency array to be correct length
			int [] eff_trim = {dataShape[dlength-2],dataShape[dlength-1]};
			int [] eff_origin = {0,0};
			eff_val_and_var[0] = eff_val_and_var[0].section(eff_origin,eff_trim);
			eff_val_and_var[1] = eff_val_and_var[1].section(eff_origin,eff_trim);
			trim_pixok = trim_pixok.section(eff_origin, eff_trim);*/
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
			efficiencyCorrection_output = PlotFactory.createPlot(efficiencyCorrection_scanData, resultName, dataDimensionType);
			((NcGroup) efficiencyCorrection_output).addLog("apply efficiency correction algorithm to get " + resultName);
			PlotFactory.addDataToPlot(efficiencyCorrection_output, resultName, outarray, "Efficiency corrected data", "Counts", out_variance);
			IDataItem contribs = Factory.createDataItem(null, efficiencyCorrection_output, "contributors",trim_pixok);
			List<Axis> data_axes = ((Plot) efficiencyCorrection_scanData).getAxisList();
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
 * We try to autodetect HDF files and use a different strategy to load our information
 * 
 */
	public void setEfficiencyCorrection_mapURI(URI efficiencyCorrection_mapFilename) 
	throws Exception {
		this.efficiencyCorrection_mapFilename = efficiencyCorrection_mapFilename;
		URI uri = efficiencyCorrection_mapFilename;
		File efficiencyFile = null;
		System.out.print("Setting efficiency correction filename "+efficiencyCorrection_mapFilename);
		try {
				efficiencyFile = new File(efficiencyCorrection_mapFilename);				
			} catch (Exception e) {
				efficiencyFile = null;
			}
			if (efficiencyFile == null || ! efficiencyFile.exists()){
				System.out.println("loading default efficiency map");
				efficiencyFile = ConverterLib.findFile(Activator.getDefault().PLUGIN_ID, "data/default_efficiencies.txt");
				// uri = ConverterLib.path2URI(efficiencyFile.getPath());
			}
			//Now try to distinguish between an HDF file and a text file
			try {
				Object test_hdf = Utilities.findObject(uri, null);
			} catch (Exception e) {
				//System.out.println("Failed to read HDF file, exception as follows:");
				//e.printStackTrace();
		        read2DEfficiencyFile(efficiencyFile);
		    return;
			}
			readhdfEfficiencyFile(efficiencyFile);
		}

	/** 
	 *  Read an efficiency file.  The file format consists of a set of header lines, each consisting of a 
	 *  token followed by a value.  The efficiency data themselves are preceeded by a 'loop_' 
	 *  keyword which signals that the efficiency/variance sets follow.
	 * Note that there is an implicit
	 * pixel_ok map included in the data: any efficiency value of zero is considered to be a non-contributor.
	 * We therefore fill a separate structure with 1s and 0s as we read in the efficiency file.
	 * It may be a better use of the Cicada structure to make this pixel_ok map be a separate output of
	 * this algorithm which is read in by those routines that need one.  For now it is passed along in
	 * the "contributors" dataitem and used during geometry correction and vertical integration. 
	 * 
	 */
	
	private void read2DEfficiencyFile(File eff_data) throws IOException {
		BufferedReader getdata;
		int no_tubes,no_vert;
		try {
		    getdata = new BufferedReader(new FileReader(eff_data));
		} catch (FileNotFoundException e) {
			return;
		}
		StreamTokenizer in_tokens = new StreamTokenizer(getdata);
		toks_vals = new HashMap<String,String>();

    	/* We store efficiency data with this structure to streamline passing it to the array multiply 
    	 * function in the main correction
    	 */
    	/*
    	 * Initialise our tokenizer; at first we don't understand numbers
    	 */
    	in_tokens.resetSyntax();
    	in_tokens.whitespaceChars(0, 32);
    	in_tokens.wordChars(33, 126);
    	in_tokens.commentChar('#');
    	in_tokens.quoteChar('\'');
    	in_tokens.quoteChar('"');
    	try {
    		// Start processing only after seeing a 'data_xxx' token 
    		while(in_tokens.nextToken()!=StreamTokenizer.TT_EOF)
    		{
    			if(in_tokens.sval.startsWith("data_")) break;
    		}
    		if(in_tokens.ttype==StreamTokenizer.TT_EOF) {
    			oldread2DEfficiencyFile(eff_data);
    			return;
    		}
			while(in_tokens.nextToken()!=StreamTokenizer.TT_EOF)
			{
				/* Our syntax is simple: we have tag-value pairs, where tags must start with '_'
				 * and values are either non-space-delimited strings, or strings enclosed inside
				 * quote characters.  After we see the 'loop_' token, we expect two tags, followed
				 * by all of our efficiency numbers.  We start the whole file with a token of the
				 * the form 'data_xxxx' where the xxxx can be anything
				 */
	   			System.out.println("Token: "+in_tokens.sval);
				if(in_tokens.sval.equals("loop_")) break;  /* extract data */
				if(in_tokens.sval.charAt(0)=='_')   /* tag */
				{
					String this_token = in_tokens.sval;
					in_tokens.nextToken();
					toks_vals.put(this_token, in_tokens.sval);
				} else 
					System.out.printf("Expected value corresponding to token %s at line %d%n", in_tokens.sval,in_tokens.lineno());
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	/* Now get two tags, and then all of the numbers */
		try {
    	in_tokens.nextToken();
    	in_tokens.nextToken();
    	/* initialise our array size */
    	no_tubes = Integer.parseInt(toks_vals.get("_[local]_efficiency_number_horizontal"));
    	no_vert = Integer.parseInt(toks_vals.get("_[local]_efficiency_number_vertical"));
    	efficiency_data = new double [no_vert][no_tubes];  /* only need 128x968 for Wombat as of 2008 */
    	efficiency_variance = new double [no_vert][no_tubes];
    	pixel_ok_map = new int [no_vert][no_tubes];
      	int vert_pix = 0; int tube_no = 0;
    	in_tokens.parseNumbers();    /* pick up numbers now */
    	// The array is stored on file with horizontal position as the most rapidly varying index. This will
    	// coincide with the way the data are stored in the NeXuS file.
			while(in_tokens.nextToken()!=StreamTokenizer.TT_EOF) 
			{
			    if(in_tokens.ttype!=in_tokens.TT_NUMBER) {
			    	System.out.printf("Ignoring non-number %s on line %d%n",in_tokens.sval,in_tokens.lineno());
			    	continue;
			    }
			    if (tube_no % 2 == 0)  //even-numbered entry, therefore variance
				    efficiency_data[vert_pix][tube_no/2]=in_tokens.nval;
				else
				{
				    efficiency_variance[vert_pix][tube_no/2]=in_tokens.nval;
				     // pick up invalid pixels
				     if(in_tokens.nval > 0.0) pixel_ok_map[vert_pix][tube_no/2]= 1;
				 }
				   tube_no++;            // move to next entry
				   if (tube_no % (no_tubes*2) == 0) {
					   vert_pix++; tube_no = 0;       //reset vertical pixel counter
				   }
				}
		} catch (Exception e) {            //Maybe it is the old format?  Interim solution
			e.printStackTrace();
		}
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
	
	private void oldread2DEfficiencyFile(File eff_data) {
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

	/*
	 * Read an HDF-format efficiency file
	 */
	private void readhdfEfficiencyFile(File eff_infile) {
		IGroup eff_hdf = null;
		IArray eff_data = null;
		IArray eff_var = null;
		System.out.print("Now reading HDF type efficiency file");
		try {
			eff_hdf = (IGroup) Utilities.findObject(eff_infile.toURI(),null);
		} catch (FileAccessException e) {  //shouldn't happen as we just checked!!
			e.printStackTrace();
		}
		try {
			eff_data = ((NcGroup) eff_hdf).getSignalArray();
			System.out.print("Successfully found efficiency data");
		} catch (SignalNotAvailableException e) {
			e.printStackTrace();
			return;
		}
		try {
		    eff_var = ((Plot) eff_hdf).findVarianceArray();
		} catch (Exception e) {
			System.out.print("No efficiency variance available");
		}
		// Now convert to expected format
		if(eff_data!=null) 
		efficiency_data = (double[][]) eff_data.getArrayUtils().copyToNDJavaArray();
		if(eff_var!=null) efficiency_variance = (double[][]) eff_var.getArrayUtils().copyToNDJavaArray();
		else {
			//TODO: fill variance with blanks
		}
	}
	
	public IGroup getEfficiencyCorrection_output() {
		return efficiencyCorrection_output;
	}


	public void setEfficiencyCorrection_inputPlot(IGroup efficiencyCorrection_scanData) {
		this.efficiencyCorrection_scanData = efficiencyCorrection_scanData;
	}


	public void setEfficiencyCorrection_skip(Boolean efficiencyCorrection_skip) {
		this.efficiencyCorrection_skip = efficiencyCorrection_skip;
	}


	public void setEfficiencyCorrection_stop(Boolean efficiencyCorrection_stop) {
		this.efficiencyCorrection_stop = efficiencyCorrection_stop;
	}
	
	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}


}

