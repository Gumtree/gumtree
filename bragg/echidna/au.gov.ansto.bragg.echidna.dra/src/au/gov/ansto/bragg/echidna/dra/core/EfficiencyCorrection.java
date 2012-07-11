package au.gov.ansto.bragg.echidna.dra.core;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.plot.*;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.errorpropagation.*;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * This class is the concrete processor of the background correction algorithm for echidna algorithm group.
 * It uses the databag of the echidna input data object. 
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
	Double efficiencyCorrection_threshold = 0.1;
	IGroup efficiencyCorrection_output = null;
	URI efficiencyCorrection_mapFilename = null;
	private double [][] efficiency_data = null; 
	private double [][] efficiency_variance = null;
	private int [][] pixel_ok_map = null;
	private Map<String,String> toks_vals = null;   //Metadata from the efficiency file
	//private ArrayOperations ao = new ArrayOperations();
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.map;

	/* Perform an efficiency correction on the input dataset, with error propagation.
	 * We treat only 3-dimensional datasets in this implementation.
	 */
	
	public Boolean process() throws Exception{
		if (efficiencyCorrection_skip || efficiencyCorrection_scanData == null){
			efficiencyCorrection_output = efficiencyCorrection_scanData;
		}else{
			String metadata_string = "";
			IArray dataArray = ((NcGroup) efficiencyCorrection_scanData).findSignal().getData();
			IArray error_array = ((Plot) efficiencyCorrection_scanData).getVariance().getData();
			if (efficiency_data != null){
				/* turn efficiency data into an array...*/
				IArray [] eff_val_and_err = {Factory.createArray(efficiency_data),Factory.createArray(efficiency_variance)};
				int[] dataShape = dataArray.getShape();
				IArray result_array = Factory.createArray(double.class, dataShape);
				IArray result_error = Factory.createArray(double.class, dataShape);
				IIndex result_index = result_array.getIndex();
				IIndex res_err_ind = result_error.getIndex();
				IArray trim_pixok = Factory.createArray(pixel_ok_map);
				int drank = dataArray.getRank();
				/* dimension zero is step number; we must apply the correction for every step, so we loop over this */
				if (drank <= 3) {
					/* transpose our efficiency array to match data values */
					eff_val_and_err[0] = eff_val_and_err[0].getArrayUtils().transpose(1,0).getArray();
					eff_val_and_err[1] = eff_val_and_err[1].getArrayUtils().transpose(1,0).getArray();
				    /* trim efficiency array to be correct length */
					int [] eff_trim = {dataShape[drank-2],dataShape[drank-1]};
					int [] eff_origin = {0,0};
					eff_val_and_err[0] = eff_val_and_err[0].getArrayUtils().section(eff_origin,eff_trim).getArray();
					eff_val_and_err[1] = eff_val_and_err[1].getArrayUtils().section(eff_origin,eff_trim).getArray();
					trim_pixok = trim_pixok.getArrayUtils().section(eff_origin, eff_trim).getArray();
					if(drank == 3)
					for(int k=0;k<dataShape[0];k++) {
						/* With 2d efficiency data, we do a straight array multiply.  The efficiency data is in order of
						 * vertical pixel as the most rapidly varying index, whereas the data has the tube_no as the most
						 * rapidly varying index.  For this reason we transposed the Array values previously.
						 */
						IArray [] dat_with_err = {dataArray.getArrayUtils().slice(0, k).getArray(), error_array.getArrayUtils().slice(0,k).getArray()};
					    IArray [] corrected_data = ArrayOperations.multiply(dat_with_err, eff_val_and_err);
					    /* now we have the annoying problem that we have to assign all these values back into the final
					     * result array, which will preserve the multiple step structure, so we loop over all the indices
					     * again...
					     */
				    	IIndex comp_index = corrected_data[0].getIndex();
					    IIndex comp_err_ind = corrected_data[1].getIndex();
						for(int vert_pix=0;vert_pix<dataShape[1];vert_pix++) {
							for(int tube_no=0;tube_no<dataShape[2];tube_no++) {
								comp_index.set(vert_pix,tube_no);
								result_index.set(k,vert_pix,tube_no);
								res_err_ind.set(k,vert_pix,tube_no);
								comp_err_ind.set(vert_pix,tube_no);
								
								result_array.setDouble(result_index,corrected_data[0].getDouble(comp_index));
								result_error.setDouble(res_err_ind,corrected_data[1].getDouble(comp_err_ind));						
							}
						}
					}
					else {   // Only a single frame of data
						IArray [] dat_with_err = {dataArray, error_array};
						IArray [] corrected_data = ArrayOperations.multiply(dat_with_err, eff_val_and_err);
						result_array = corrected_data[0];
						result_error = corrected_data[1];
					}
					metadata_string = "Applied two-dimensional efficiency correction based on Vanadium flood field data.";
				}
				else  { /* not yet implemented */
					metadata_string = "No efficiency correction applied";
				}
				// include pixel_ok_map in output file as item name "contributors"
				String resultName = "efficiencyCorrection_result";
				efficiencyCorrection_output = PlotFactory.createPlot(efficiencyCorrection_scanData, resultName, dataDimensionType);
				((NcGroup) efficiencyCorrection_output).addLog("apply efficiency correction algorithm to get " + resultName);
				PlotFactory.addDataToPlot(efficiencyCorrection_output, resultName, result_array, "Efficiency corrected data", "Counts", result_error);
				IDataItem contribs = Factory.createDataItem(efficiencyCorrection_output, "contributors",trim_pixok);
				List<Axis> data_axes = ((Plot) efficiencyCorrection_scanData).getAxisList();
				for (Axis oneaxis : data_axes) {
					PlotFactory.addAxisToPlot(efficiencyCorrection_output, oneaxis,oneaxis.getDimensionName());
				}
				// Add information for use by visualisation software
				efficiencyCorrection_output.addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
						StaticDefinition.DataStructureType.plot.name());
				efficiencyCorrection_output.addStringAttribute(StaticDefinition.DATA_DIMENSION_TYPE, StaticDefinition.DataDimensionType.mapset.name());
				efficiencyCorrection_output.addDataItem(contribs);
				try {
				efficiencyCorrection_output.addDataItem(efficiencyCorrection_scanData.findDataItem("bad_frames"));
				} catch (Exception e) {
					//do nothing
				}
				((NcGroup) efficiencyCorrection_output).addMetadata("CIF", "_pd_proc_info_data_reduction", metadata_string, false);
				Map<String,String> current_md = efficiencyCorrection_output.harvestMetadata("CIF");
				for(Entry<String,String> e: toks_vals.entrySet()) {
					// Only add information that is not already there; we don't want the efficiency metadata to
					// replace or add to the main metadata store
					if(!(current_md.containsKey(e.getKey())))
						((NcGroup) efficiencyCorrection_output).addMetadata("CIF", e.getKey(), e.getValue());
				}
			}
			else efficiencyCorrection_output = efficiencyCorrection_scanData;
		}
		return efficiencyCorrection_stop;
	}

/*
 * Read in an efficiency correction file.  The file format is:

 * The efficiency is a number that must be multiplied by an observed value in order to find the result.
 * This means that the larger an efficiency number, the more the signal from that tube is dampened
 * 
 */
	public void setEfficiencyCorrection_mapFilename(URI efficiencyCorrection_mapFilename) 
	throws Exception {
		this.efficiencyCorrection_mapFilename = efficiencyCorrection_mapFilename;
		URI uri = efficiencyCorrection_mapFilename;
		File efficiencyFile = null;
		try {
				efficiencyFile = new File(efficiencyCorrection_mapFilename);				
			} catch (Exception e) {
				// TODO: handle exception
				efficiencyFile = null;
			}
			if (efficiencyFile == null || ! efficiencyFile.exists()){
				String filename = System.getProperty("dav.efficiencies","");
				System.out.println("loading default efficiency map from "+filename);
				IFileStore fileStore = EFS.getStore(new URI(filename));
				efficiencyFile = fileStore.toLocalFile(EFS.NONE, new NullProgressMonitor());
			}
		    read2DEfficiencyFile(efficiencyFile);
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
		int no_vert;
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
    	efficiency_data = new double [200][200];  /* only need 128 for Echidna as of 2008 */
    	efficiency_variance = new double [200][200];
    	pixel_ok_map = new int [200][200];
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
					if(in_tokens.sval.equals(";")) {
						// process semicolon delimited string
						in_tokens.resetSyntax();
						in_tokens.wordChars(0,126);   //get it all
						in_tokens.whitespaceChars(0,0);
						in_tokens.quoteChar(';');
						in_tokens.nextToken();
						in_tokens.wordChars(33, 126);
						in_tokens.whitespaceChars(0, 32);
						in_tokens.commentChar('#');
						in_tokens.quoteChar('"');
						in_tokens.quoteChar('\'');
						toks_vals.put(this_token, in_tokens.sval);
						in_tokens.nextToken();      //this will be an isolated semicolon
					}
					else {
						toks_vals.put(this_token, in_tokens.sval);
					}
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
    	no_vert = Integer.parseInt(toks_vals.get("_[local]_efficiency_number_vertical"));
    	int vert_pix = 0; int tube_no = 0;
    	in_tokens.parseNumbers();    /* pick up numbers now */
			while(in_tokens.nextToken()!=StreamTokenizer.TT_EOF) 
			{
			    if(in_tokens.ttype!=in_tokens.TT_NUMBER) {
			    	System.out.printf("Ignoring non-number %s on line %d%n",in_tokens.sval,in_tokens.lineno());
			    	continue;
			    }
			    if (vert_pix % 2 == 0)  //even-numbered entry, therefore variance
				    efficiency_data[tube_no][vert_pix/2]=in_tokens.nval;
				else
				{
				    efficiency_variance[tube_no][vert_pix/2]=in_tokens.nval;
				     // pick up invalid pixels
				     if(in_tokens.nval > 0.0) pixel_ok_map[tube_no][vert_pix/2]= 1;
				 }
				   vert_pix++;            // move to next entry
				   if (vert_pix % (no_vert*2) == 0) {
					   tube_no++; vert_pix = 0;       //reset vertical pixel counter
				   }
				}
		} catch (Exception e) {            //Maybe it is the old format?  Interim solution
			e.printStackTrace();
		}
	   	} 
	
	private void oldread2DEfficiencyFile(File eff_data) {
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
    	efficiency_data = new double [200][200];  /* only need 128 for Echidna as of 2008 */
    	efficiency_variance = new double [200][200];
    	pixel_ok_map = new int [200][200];
	   	try {
	   		int tube_no=-1; int vert_pix = 0;  // initialise position counters
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
	    	for (String tn : these_numbers) {
	    	   if (vert_pix % 256 == 0) {
	    		   tube_no++; vert_pix = 0;       //reset vertical pixel counter
	    	   }
	    	   if (vert_pix % 2 == 0)  //even-numbered entry, therefore variance
	    	     efficiency_data[tube_no][vert_pix/2]=Double.parseDouble(tn);
	    	   else
	    	   {
	    		 double this_val = Double.parseDouble(tn);
	    	     efficiency_variance[tube_no][vert_pix/2]=this_val;
	    	     // pick up invalid pixels
	    	     if(this_val > 0.0) pixel_ok_map[tube_no][vert_pix/2]= 1;
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


	public void setEfficiencyCorrection_scanData(IGroup efficiencyCorrection_scanData) {
		this.efficiencyCorrection_scanData = efficiencyCorrection_scanData;
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

