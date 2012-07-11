package au.gov.ansto.bragg.echidna.dra.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.echidna.dra.internal.Activator;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class VerticalOffset extends ConcreteProcessor {
	URI vert_file = null;
	IGroup voffset_in = null;
	IGroup voffset_out = null;
	Boolean voffset_skip = false;
	Boolean voffset_stop = false;
	private int [] offset_data = null; 
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.map;

	/* This concrete processor applies an integral pixel offset to the input data.  This
	 * involves shifting the data array by an integral number of steps, as well as shifting
	 * the error array and contributor array, if one exists.  If no contributor array is
	 * present, we create one.
	 */
	
	public Boolean process() throws Exception {
		Boolean make_contribs = false;
		IArray contribs = null;
		if (voffset_skip || voffset_in == null){
			voffset_out = voffset_in;
			return voffset_stop;
		}else{
			IArray dataArray = ((Plot) voffset_in).findSignalArray();
			IArray error_array = ((Plot) voffset_in).getVariance().getData();
			if (dataArray != null){
				//We cycle through the tubes, shifting pixels up and down.  We can't shift
				//in place as the Java array is fixed storage, so we create a whole new
				//data structure.  Note that the contribs array is only 2D, as each step
				//will have an identical pixel ok map, so we don't need the third dimension
				int[] dataShape = dataArray.getShape();
				int drank = dataArray.getRank();
				try {
					contribs = voffset_in.findDataItem("contributors").getData();
				} catch (NullPointerException e) {
					System.out.print("Vertical offset: no contributors found, assuming all contribute");
					make_contribs = true;  //we will fill with ones as we go
					int[] proper_shape = {dataShape[drank-1],dataShape[drank-2]};
					contribs = Factory.createArray(int.class,proper_shape);
				}
				int[] conShape = contribs.getShape();
				IArray shifted_data = Factory.createArray(double.class, dataShape);
				IArray shifted_errs = Factory.createArray(double.class, dataShape);
				IArray new_contribs = Factory.createArray(int.class,conShape);
				IIndex new_index = shifted_data.getIndex();
				IIndex err_index = shifted_errs.getIndex();
				IIndex con_index = new_contribs.getIndex();
				IArrayIterator di = dataArray.getIterator();
				IArrayIterator ei = error_array.getIterator();
				IIndex old_c_i = contribs.getIndex();
				int conval = 1;  // default is for an unshifted pixel to contribute
				while (di.hasNext()) {
					double thisval = di.getDoubleNext();
					double errval = ei.getDoubleNext();
					int[] shift_location = di.getCounter(); //current location in input array
					//data structure is [step,vertical pixel,tube]
					//move the data in each tube up by specified value
					//pixel ok map data structure is tubeno,vertical pixel, i.e. reversed
					if(!make_contribs){
						old_c_i.set(shift_location[drank-1],shift_location[drank-2]);
						conval = contribs.getInt(old_c_i);  //move the contribs around
					}
					shift_location[drank-2]+= offset_data[shift_location[drank-1]]; //shift up by value of offset data
					if ((shift_location[drank-2]>=0) && (shift_location[drank-2]<dataShape[drank-2])) {
						//copy the data into the proper place
						//only need to do the contribs once
						new_index.set(shift_location);
						shifted_data.setDouble(new_index, thisval);
						shifted_errs.setDouble(new_index, errval); //note we use same Index for error array
				        if(drank==2 || (drank==3 && shift_location[0]==0)) {  //ie make contrib array the first time only
						    con_index.set(shift_location[drank-1],shift_location[drank-2]);
						    new_contribs.setInt(con_index, conval);
				        }
					}
				}
				//Now pack it all up as usual
				String resultName = "verticalcorrection_result";
				voffset_out = PlotFactory.createPlot(voffset_in, resultName, dataDimensionType);
				((NcGroup) voffset_out).addLog("apply vertical offset correction algorithm to get " + resultName);
				PlotFactory.addDataToPlot(voffset_out, resultName, shifted_data, "Vertically corrected data", "Counts", shifted_errs);
				IDataItem pixok = Factory.createDataItem(voffset_out, "contributors",new_contribs);
				List<Axis> data_axes = ((Plot) voffset_in).getAxisList();
				for (Axis oneaxis : data_axes) {
					PlotFactory.addAxisToPlot(voffset_out, oneaxis,oneaxis.getDimensionName());
				}
				// Add information for use by visualisation software
				voffset_out.addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
						StaticDefinition.DataStructureType.plot.name());
				voffset_out.addStringAttribute(StaticDefinition.DATA_DIMENSION_TYPE, StaticDefinition.DataDimensionType.mapset.name());
				voffset_out.addDataItem(pixok);
				try {
					voffset_out.addDataItem(voffset_in.findDataItem("bad_frames"));
					} catch (Exception e) {
						//do nothing
					}
				((NcGroup) voffset_out).addMetadata("CIF", "_pd_proc_info_data_reduction", 
						"Vertical coordinates of data from each tube shifted to correct for variations" +
						" in individual tube vertical position.", false);
			}
		}
		return voffset_stop;
	}

	/*
	 * Read in an efficiency correction file.  The file format is:
	 * 1. Any line starting with '#' is a comment
	 * 2. Other lines are tube number, space, integer offset where a positive number
	 * means that the tube is physically offset by that many pixels vertically, meaning
	 * that the array of data for this tube should be shifted up by this amount.
	 */
	public void setVertical_Filename(URI vertical_filename) 
	throws Exception {
		this.vert_file = vertical_filename;
		URI uri = vertical_filename;
		File vertfile = null;
		try {
			vertfile = new File(vertical_filename);				
		} catch (Exception e) {
			// TODO: handle exception
			vertfile = null;
		}
		if (vertfile == null || ! vertfile.exists()){
			System.out.println("loading default vertical offsets map");
			vertfile = ConverterLib.findFile(Activator.getDefault().PLUGIN_ID, "data/echidna_vertical_offsets.txt");
			uri = ConverterLib.path2URI(vertfile.getPath());
		}
		readOffsetsFile(vertfile);
	}

	/* Read ascii data in format: tube_no offset 
	 * Tube no is an integer, and offset may be non-integral(it will be rounded to an
	 * integer).
	 * May fail in vaguely pathological cases e.g. a comment on an empty last line.
	 * The offset describes the physical displacement of the tube from the ideal value of 
	 * 63.5 in pixel units, so in order to correct for this effect the data should be
	 * shifted in the same direction to the figure given.
	 */
	private void readOffsetsFile(File off_data) throws Exception {
		BufferedReader getdata;
		try {
			getdata = new BufferedReader(new FileReader(off_data));
		} catch (FileNotFoundException e) {
			return;
		}
		String next_line;
		String [] these_numbers;

		offset_data = new int [200];  /* only need 128 for Echidna as of 2008 */
		try {
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
				//Note the -1 to correct for 0-numbering rather than 1-numbering
				offset_data[Integer.parseInt(these_numbers[0])-1]=(int) Math.round(Double.parseDouble(these_numbers[1]));
				next_line=getdata.readLine();
			}
		} catch (IOException e) {
			throw new Exception("Syntax error in vertical offset file");
		}
	}

	public DataDimensionType getDataDimensionType() {
		// TODO Auto-generated method stub
		return null;
	}

	public DataStructureType getDataStructureType() {
		// TODO Auto-generated method stub
		return null;
	}

	public IGroup getVoffset_out() {
		return voffset_out;
	}

	public void setVoffset_in(IGroup voffset_in) {
		this.voffset_in = voffset_in;
	}

	public void setVoffset_skip(Boolean voffset_skip) {
		this.voffset_skip = voffset_skip;
	}

	public void setVoffset_stop(Boolean voffset_stop) {
		this.voffset_stop = voffset_stop;
	}


}
