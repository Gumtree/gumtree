/******************************************************************************* 
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 *******************************************************************************/
package org.gumtree.data.nexus.ui.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.BreakIterator;
import java.util.Formatter;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.ISliceIterator;
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.utils.NexusUtils;
import org.gumtree.data.utils.Utilities;
import org.gumtree.vis.gdm.io.AbstractExporter;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYErrorSeries;
import org.gumtree.vis.nexus.dataset.Hist2DNXDataset;
import org.gumtree.vis.nexus.dataset.NXDatasetSeries;


/**
 * @author nxi,jxh
 * This outputs a pdCIF-conformant file, which can be imagined as an XY sigma file
 * with a standard header.  See International Tables for Crystallography, Vol G for
 * information on pdCIF data items. The processor blocks must leave instrument-specific
 * metadata in the Plot object in order for this to provide instrument-specific information.
 * 
 * Created on 16/09/2008
 */

public class PdCIFExporter extends AbstractExporter {

	@Override
	public void export(File file, IDataset signal) throws IOException {
		if (signal instanceof IXYErrorDataset) {
			List<IXYErrorSeries> seriesList = ((IXYErrorDataset) signal).getSeries();
			if (seriesList.size() > 1) {
				int index = 0;
				for (IXYErrorSeries series : seriesList) {
					if (series instanceof NXDatasetSeries) {
						INXDataset nxDataset = ((NXDatasetSeries) series).getNxDataset();
						File subFile = new File(file.getAbsolutePath() + "/" + 
								nxDataset.getTitle() + "_" + (index++) + "." + getExtensionName());
						INXdata data = NexusUtils.getNXdata(nxDataset);
						signalExport(subFile, data);
					}
				}				
			} else if (seriesList.size() > 0) {
				INXdata data = NexusUtils.getNXdata(((NXDatasetSeries) seriesList.get(0)).getNxDataset());
				signalExport(file, data);
			}
		} else if (signal instanceof Hist2DNXDataset) {
			INXdata data = NexusUtils.getNXdata(((Hist2DNXDataset) signal).getNXDataset());
			signalExport(file, data);
		}
		
	}

	public void signalExport(File file, INXdata signal) throws IOException {
//		Plot signal = null;
//		if (signal instanceof Plot)
//			signal = (Plot) signal;
		IArray outputnumbers;
		try {
			outputnumbers = ((NcGroup) signal).getSignalArray();
		} catch (SignalNotAvailableException e1) {
			throw new IOException(e1);
		}
		int[] dataShape = outputnumbers.getShape();
		int dataRank = outputnumbers.getRank();
		String block_name = null;
		if (signal.getLocation() != null) {
			block_name = new File(signal.getLocation()).getName();
		} 
		if (block_name == null) {
			block_name = signal.getRootGroup().getDataset().getTitle();
		}
		if (block_name == null) {
			block_name = signal.getShortName();
		}
		// For each dimension in the data, write a file
		IArray tth_points = signal.getAxisList().get(dataRank-1).getData();
		int tth_rank = tth_points.getRank();
//		String log = signal.getLogString();
		boolean isDuplicated = false;
//		if (dataRank == 2 && dataShape[0] == 2 && log.contains("duplicate 1D pattern"))
//			isDuplicated = true;
		IArray err_array = null;
		try{
			err_array = signal.getVariance().getData();
		}catch (Exception e) {
			err_array = outputnumbers;
		}
		if(tth_points.getShape()[tth_rank-1]==dataShape[dataRank-1]+1) {      //do bin boundary processing
			try{
				tth_points = toCentres(tth_points);
			}catch (Exception e) {
			}
		}
		// Do some double-checking of two theta lengths...
		if(tth_points.getShape()[tth_rank-1]!=dataShape[dataRank-1]) {
			throw new IOException(String.format("Horizontal coordinate length %d does not match data length %d",
					tth_points.getShape()[tth_rank-1],dataShape[dataRank-1]));
		}
		IArray timeArray = null;
		IArray monitor1 = null;
		IArray monitor3 = null;
		IArrayIterator timeIterator = null;
		IArrayIterator bm1Iterator = null;
		IArrayIterator bm3Iterator = null;
		try {
			timeArray = signal.getDataItem("detector_time").getData();
			timeIterator = timeArray.getIterator();
		} catch (Exception e) {}
		try {
			monitor1 = signal.getDataItem("bm1_counts").getData();
			bm1Iterator = monitor1.getIterator();
		} catch (Exception e) {}
		try {
		} catch (Exception e) {}
		try {
			monitor3 = signal.getDataItem("bm3_counts").getData();
			bm3Iterator = monitor3.getIterator();
		} catch (Exception e) {}

		IArray second_axis = null;
		String ax_name = "Run";
		if(dataRank==2) {                                                     //get other axis
			IAxis scnd_ax = signal.getAxisList().get(0);
			second_axis = scnd_ax.getData();
			ax_name = scnd_ax.getShortName();
		} else {
			IDataItem scan_ax = null;
			try{
				scan_ax = signal.findDataItem("scanVariable");
				ax_name = signal.getShortName();
				second_axis = scan_ax.getData();
			}catch (Exception e) {
				second_axis = Factory.createArray(double.class, new int[] {1});
			}			
		}
		String shortName = Utilities.getKeyFromValue(signal, ax_name);
		if (shortName != null)
			ax_name = shortName;
		//Note that tth may be 2 dimensional but may also be single-dimensional
		PrintWriter outputfile = null;
		try{
			ISliceIterator tth_iter = tth_points.getSliceIterator(1);
			ISliceIterator out_iter = outputnumbers.getSliceIterator(1);
			ISliceIterator err_iter = err_array.getSliceIterator(1);
			IArrayIterator scnd_iter = second_axis.getIterator();                   //iterate over other axis
			int frame_ct = 0;                                                      //be sure that file is unique
			String processInfo = getProcessInfo(signal);
			System.out.println("Now outputting Powder CIF file named " + file.getAbsolutePath());
			outputfile = new PrintWriter(new FileWriter(file));
			outputfile.format("#CIF\\1.1%n");
			outputfile.format("############################################################%n");
			outputfile.format("#                                                          #%n");
			outputfile.format("#   Powder CIF file.  For details on this format           #%n");
			outputfile.format("#   see International Tables for Crystallography,          #%n");
			outputfile.format("#   Volume G                                               #%n#%n");
			outputfile.format("############################################################%n");
			// use the current time as a unique block identifier
			String current_time = String.format("%1$tFT%1$tT", System.currentTimeMillis());
			outputfile.format("data_%s%s%n", block_name,current_time);
			// Create a unique block id
			String userName = "?";
			try {
				userName = ((IDataItem) signal.findContainer("user_name")).getData().toString();				
			} catch (Exception e) {} 
			// Time has a special format in CIF files
			String start_time = ((IDataItem) signal.findContainer("start_time")).getData().toString();
			String end_time = ((IDataItem) signal.findContainer("end_time")).getData().toString();
			start_time = start_time.replace(" ", "T");
			end_time = end_time.replace(" ", "T");
			outputfile.format("_pd_block_id \t%s|%s|%s%n",block_name,current_time,sanitise_string(userName));
			String prog_details = extract_metadata(signal,"program_name") + " " + extract_metadata(signal,"sics_release");
			outputfile.format("_computing_data_collection \t%s%n", prepare_string(prog_details));
			outputfile.format("_computing_data_reduction \t%s%n", "'ANSTO Gumtree'");
			outputfile.format("_audit_creation_date \t%s%n", current_time);
			outputfile.format("_audit_creation_method \t%s%n", "'Automatically generated from raw NeXuS data file by Gumtree routines'");
			outputfile.format("loop_%n _audit_conform_dict_name%n  _audit_conform_dict_version%n _audit_conform_dict_location%n");
			outputfile.format(" cif_core.dic  2.3.1 ftp://ftp.iucr.org/pub/cifdics/cif_core_2.3.1.dic%n");
			outputfile.format(" cif_pd.dic    1.0.1 ftp://ftp.iucr.org/pub/cifdics/cif_pd_1.0.1.dic%n%n");
			outputfile.format("_pd_spec_special_details \t%s%n",extract_metadata(signal, "sample_name"));
			outputfile.format("_[local]_data_collection_description \t%s%n",extract_metadata(signal, "sample_description"));
			outputfile.format("_pd_meas_datetime_initiated \t%s%n", start_time);
			outputfile.format("_[local]_datetime_completed \t%s%n", end_time);
			outputfile.format("_pd_meas_info_author_name \t%s%n", prepare_string(userName));
			outputfile.format("_pd_meas_info_author_email \t%s%n", extract_metadata(signal, "user_email"));
			outputfile.format("_pd_meas_info_author_phone \t%s%n", extract_metadata(signal, "user_phone"));
			outputfile.format("_pd_instr_2theta_monochr_pre \t%s%n",extract_metadata(signal, "takeoff_angle"));
			outputfile.format("_pd_instr_dist_mono/spec \t%s%n", extract_metadata(signal, "mono_sample_mm","%.1f",1.0));
			outputfile.format("_pd_instr_dist_spec/detc \t%s%n",extract_metadata(signal,"radius","%.1f",1.0));
			// Note: we cannot state much about the collimation as that is instrument-dependent
			// Source power value is a bit flaky - we may need to add some logic on this one
			outputfile.format("_diffrn_source_power \t%s%n", extract_metadata(signal,"reactor_power","%.2f",1000));
			outputfile.format("_diffrn_radiation_probe   \tneutron%n");
			// Now get the instrument-specific information
			TreeMap<String,String> spec_metadata = new TreeMap<String, String>(signal.harvestMetadata("CIF"));
			for(Entry<String,String> e: spec_metadata.entrySet()) {
				outputfile.format("%s \t",e.getKey());
				outputfile.format("%s%n",prepare_string(e.getValue()));
			}
			while(out_iter.hasNext()) {
				IArray tth_row = null;
				try {
					tth_row = tth_iter.getArrayNext();
				} catch(NoSuchElementException e) {  //This might happen if horizontal coordinate is fixed for all frames
					tth_iter = tth_points.getSliceIterator(1);   //re-initialise
					tth_row = tth_iter.getArrayNext();
				}
				IArray data_row = out_iter.getArrayNext();
				IArray err_row = err_iter.getArrayNext();
				IArrayIterator row_iter = tth_row.getIterator();
				IArrayIterator data_iter = data_row.getIterator();
				IArrayIterator error_iter = err_row.getIterator();
				double scnd_loc = scnd_iter.getDoubleNext();
				frame_ct++;
				if (dataRank > 1){
					String topcomment = "Scan variable: " + ax_name + "=" + (new Formatter()).format("%.5f", scnd_loc);
					outputfile.format("# %-79s%n", topcomment);
					String scanInfo = "";
					if (timeIterator != null && timeIterator.hasNext())
						scanInfo += " time=" + (new Formatter()).format("%.1f", timeIterator.getDoubleNext()) + "seconds";
					if (bm1Iterator != null && bm3Iterator.hasNext()) {
						double bm3Value = bm3Iterator.getDoubleNext();
						if (bm3Value > 0) {
							scanInfo += "\tbm3_counts=" + (new Formatter()).format("%.0f", bm3Value);
						}
					}
						
					if (scanInfo.length() > 0)
						outputfile.append("#" + scanInfo + "\r\n");
				}
				outputfile.format("# " + signal.getAxisList().get(signal.getAxisList().size() - 1).getShortName() + "         " + 
						signal.getSignal().getShortName() + "         sigma\r\n");
				outputfile.format("loop_%n _pd_proc_2theta_corrected%n _pd_proc_intensity_net%n _pd_proc_intensity_net_esd%n");
				while(row_iter.hasNext()) {
					double errorValue = error_iter.getDoubleNext();
					if(errorValue != 0) {   //otherwise is masked out
					outputfile.format("%10.5f %15s %15.5f%n",row_iter.getDoubleNext(),
							format_esd(data_iter.getDoubleNext(),
							Math.sqrt(errorValue)),Math.sqrt(errorValue));
					} else {
						row_iter.getDoubleNext();data_iter.getDoubleNext();  //unused
					}
				}
				outputfile.format("%n");  //one blank line between histograms - this is good for Gnuplot
				//				if(xyexport_sep_flag) outputfile.close();
				if (isDuplicated)
					break;
			}
			outputfile.close();
		}catch (Exception e) {
			if (outputfile != null)
				outputfile.close();
			e.printStackTrace();
		}
	}

	

	private IArray toCentres(IArray inarray) throws InvalidRangeException {
		int[] centre_shape = inarray.getShape();  //final shape
		int centre_rank = inarray.getRank();      //final rank
		centre_shape[centre_rank-1]--;
		IArray centre_array = Factory.createArray(double.class, centre_shape);
		//Create an iterator over the higher dimensions
		int[] range_list = new int[centre_rank];
		for(int i=0;i<centre_rank-1;i++) range_list[i] = centre_shape[i];
		range_list[centre_rank-1]=1;
		IArray high_dim_array = inarray.getArrayUtils().sectionNoReduce(new int[centre_rank],range_list, null).getArray();
		//Now repurpose the range_list for the internal array
		for(int i=0;i<centre_rank-1;i++) range_list[i] = 1;
		range_list[centre_rank-1] = centre_shape[centre_rank-1]+1;
		IArrayIterator high_dim_iter = high_dim_array.getIterator();
		while(high_dim_iter.hasNext()) {
			high_dim_iter.next();
			//Iterate over the lowest dimensional array
			int[] origin_list = high_dim_iter.getCounter();
			IArray this_scan = inarray.getArrayUtils().section(origin_list,range_list,null).getArray();
			range_list[centre_rank-1]--;
			IArray c_scan = centre_array.getArrayUtils().section(origin_list,range_list,null).getArray();
			range_list[centre_rank-1]++;  //for next time
			IArrayIterator o_iter = this_scan.getIterator();
			IArrayIterator c_iter = c_scan.getIterator();
			double bin_lowedge = o_iter.getDoubleNext();    //We get o_iter one extra time
			//note we rely on canonical behaviour, that is, that the iterator will loop over the fastest index first
			double bin_highedge = 0;
			while(c_iter.hasNext()) {
				bin_highedge = o_iter.getDoubleNext();
				c_iter.next().setDoubleCurrent(bin_lowedge + (bin_highedge-bin_lowedge)/2.0);
				bin_lowedge = bin_highedge;                 //set ready for next time through the loop
			}
		}
		return centre_array;
	}

	private String sanitise_string(String bad_string) {
		String good_string = bad_string.replaceAll("\\s", "-"); //no embedded whitespace
		String better_string = good_string.replaceAll("_","-"); //no leading underscores
		return better_string;
	}
	
	/* prepare a string for CIF output; if there are any CR/LF characters, we need to
	 * output a semicolon-delimited string; otherwise, we can surround by quotes.  Of
	 * course, if there are embedded quotes then we are in trouble
	 * TODO: deal with embedded quotes
	 */
	
	private String prepare_string(String bad_string) {
		String good_string = "";
		if(bad_string.contains("\r")||bad_string.contains("\n")) {
			// Wrap the text nicely
			BreakIterator bi = BreakIterator.getLineInstance();
			bi.setText(bad_string);
			int start = bi.first();
			for(int end = bi.next();end!=BreakIterator.DONE;end=bi.next()) {
				if(end-start<75) continue; 
				good_string = good_string + bad_string.substring(start, end);
				start = end;
			}
			good_string = good_string + "\n"+ bad_string.substring(start);
			good_string = "\n;\n"+good_string+"\n;\n";
		} else {
			// Note that this is not perfect; we may have a string containing whitespace
			// immediately following a single or double quote, in which case we must use
			// the semicolon-delimited variation.  We don't check for this here, but a
			// simple regular expression should catch it
			if(bad_string.contains(" ")||bad_string.contains("\t"))
				if (bad_string.contains("\""))
					good_string = "'" + bad_string + "'";
				else good_string = "\"" + bad_string + "\"";
			else good_string = bad_string;
		}
		return good_string;
	}
	
	/* Extract the specified item into a string for output */
	private String extract_metadata(INXdata input_data, String first_location) {
		String first_string = "";
		boolean is_number = false;
		try {
			IArray rawdata = null;
			Object item = input_data.getContainer(first_location);
			if(item==null) item = input_data.findContainer(first_location);
			if (item instanceof IDataItem){
				rawdata = ((IDataItem) item).getData();
			}
			else if (item instanceof IAttribute)
				 rawdata = ((IAttribute) item).getValue();
			if (rawdata.getElementType() == Character.TYPE)
				first_string = rawdata.toString();
			else  { //Report an average value
				first_string = String.format("%f",rawdata.getArrayMath().sum()/rawdata.getSize());
				is_number = true;
			}
		} catch (Exception e) {}
		if (first_string.equalsIgnoreCase("")) return "?";  //unknown
		else {
			if (!is_number)
			    return prepare_string(first_string);
			else
				return first_string;
		}
	}
	
	/* Extract numerical metadata, with application of scalefactor and using format_string.  This should not be used for
	 * character data */
	
	private String extract_metadata(INXdata input_data, String first_location, String format_string, double scalefactor) {
		String first_string = "";
		try {
			IArray rawdata = null;
			Object item = input_data.getContainer(first_location);
			if(item==null) item = input_data.findContainer(first_location);
			if (item instanceof IDataItem){
				rawdata = ((IDataItem) item).getData();
			}
			else if (item instanceof IAttribute)
				 rawdata = ((IAttribute) item).getValue();
			first_string = String.format(format_string,rawdata.getArrayMath().sum()*scalefactor/rawdata.getSize());
		} catch (Exception e) {}
		if (first_string.equalsIgnoreCase("")) return "?";  //unknown
		else return first_string;
	}
	
	/*
	 * A routine that, given a number and its uncertainty, will format in the traditional
	 * fashion so that the output string has the error in the final digits enclosed in 
	 * round brackets.  We keep three digits of the error just in case
	 * 
	 */
	private String format_esd(Double number, Double error) {
		double err_as_int = error;
		int outsigfigs = -2;
		String flt_format = "";
		if(error<0.00000001) return String.format("%.5f(0)",number);
		for(;err_as_int<9.5;err_as_int*=10,outsigfigs++){}
		// Now outsigfigs is the number of digits after the decimal point to output
		if(outsigfigs<0) {       //Decimal point is not needed
			flt_format = "%.0f(%2d)";
		}
		else {
		    flt_format = String.format("%%.%df(%%2d)",outsigfigs);
		}
		return String.format(flt_format, number,(int) Math.rint(err_as_int));
	}
	
	private String getProcessInfo(INXdata signal) {
		String processInfo = "";
//		String log = signal.getProcessingLog();
//		if (log.contains("efficiency correction"))
//			processInfo += "e";
//		if (log.contains("geometry curve correction"))
//			processInfo += "g";
//		if (log.contains("regional integration"))
//			processInfo += "i";
//		if (log.contains("fitted with"))
//			processInfo += "f"; 
//		if (processInfo.length() > 0)
//			processInfo = "_" + processInfo;
//		if (log.contains("y in [")){
//			String yInfo = log.substring(log.indexOf("y in ["));
//			yInfo = yInfo.substring(6, yInfo.indexOf("]"));
//			yInfo = yInfo.replace(",", "_");
//			processInfo += "_" + yInfo;
//		}
//		
		return processInfo;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "pdCIF";
	}
	
	@Override
	public String getExtensionName() {
		return "cif";
	}

}
