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
package au.gov.ansto.bragg.cicada.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Formatter;
import java.util.NoSuchElementException;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.ISliceIterator;
import org.gumtree.data.utils.Utilities;

import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Data;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;

/**
 * @author nxi
 * Created on 16/09/2008
 */
public class XYSigmaExport extends FormatedExport {

	/**
	 * 
	 */
	public XYSigmaExport() {

	}

	@Override
	public void resultExport(URI fileURI, IGroup signal) throws IOException {
		Plot inputdata = null;
		if (signal instanceof Plot)
			inputdata = (Plot) signal;
		IArray outputnumbers;
		try {
			outputnumbers = ((NcGroup) inputdata).getSignalArray();
		} catch (SignalNotAvailableException e1) {
			throw new IOException(e1);
		}
		int[] dataShape = outputnumbers.getShape();
		int dataRank = outputnumbers.getRank();
		String base_comment = "Raw nexus file: " + inputdata.getLocation();
		// For each dimension in the data, write a file
		IArray tth_points = ((Plot) inputdata).getAxis(dataRank-1).getData();
		int tth_rank = tth_points.getRank();
		String log = inputdata.getLogString();
		boolean isDuplicated = false;
		if (dataRank == 2 && dataShape[0] == 2 && log.contains("duplicate 1D pattern"))
			isDuplicated = true;
		IArray err_array = null;
		try{
			err_array = ((Plot) inputdata).getVariance().getData();
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
		IArray monitor2 = null;
		IArray monitor3 = null;
		IArrayIterator timeIterator = null;
		IArrayIterator bm1Iterator = null;
		IArrayIterator bm2Iterator = null;
		IArrayIterator bm3Iterator = null;
		try {
			timeArray = inputdata.getDataItem("detector_time").getData();
			timeIterator = timeArray.getIterator();
		} catch (Exception e) {}
		try {
			monitor1 = inputdata.getDataItem("bm1_counts").getData();
			bm1Iterator = monitor1.getIterator();
		} catch (Exception e) {}
		try {
			monitor2 = inputdata.getDataItem("bm2_counts").getData();
			bm2Iterator = monitor2.getIterator();
		} catch (Exception e) {}
		try {
			monitor3 = inputdata.getDataItem("bm3_counts").getData();
			bm3Iterator = monitor3.getIterator();
		} catch (Exception e) {}

		IArray second_axis = null;
		String ax_name = "Run";
		if(dataRank==2) {                                                     //get other axis
			Axis scnd_ax = ((Plot) inputdata).getAxis(0);
			second_axis = scnd_ax.getData();
			ax_name = scnd_ax.getTitle();
		} else {
			IDataItem scan_ax = null;
			try{
				scan_ax = inputdata.findDataItem("scanVariable");
				ax_name = ((Data) scan_ax).getTitle();
				second_axis = scan_ax.getData();
			}catch (Exception e) {
				second_axis = Factory.createArray(double.class, new int[] {1});
			}			
		}
		String shortName = Utilities.getKeyFromValue(inputdata, ax_name);
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
			//			if(!xyexport_sep_flag)
//			File folder = null;
//			try{
//				folder = new File(fileURI).getParentFile();
//				if (!folder.exists())
//					folder.mkdirs();
//			}catch (Exception e) {
//				folder = new File(".");
//			}
//			String rawFileName = (new File(inputdata.getLocation())).getName();
//			rawFileName = rawFileName.substring(0, rawFileName.indexOf("."));
//			File newFile = new File(folder.getAbsolutePath() + "/" + rawFileName + ".xyd");
			String processInfo = getProcessInfo(signal);
			String extensionName = ".xyd";
			if (processInfo.contains("i") || processInfo.contains("f"))
				extensionName = ".xid";
			
			if (fileURI == null || fileURI.getPath().isEmpty())
				return;
			String filename = fileURI.getPath();
			if (!filename.endsWith(".xyd") && !filename.endsWith(".xid")){
				filename = filename + processInfo + extensionName;
			}
			File newFile = new File(filename);
			System.out.println("Now outputting 2theta-intensity file named " + newFile.getAbsolutePath());
			outputfile = new PrintWriter(new FileWriter(newFile));
			outputfile.format("# %-79s%n",base_comment);
			outputfile.append("#\t" + getDeviceInfo(inputdata, "experiment_title", null) + "\t" 
					+ getDeviceInfo(inputdata, "sample_name", null) + "\r\n");
			outputfile.append("#\t" + getDeviceInfo(inputdata, "sample_description", null) + "\r\n");
			String userName = "";
			try {
				userName = inputdata.getDataItem("user_name").getData().toString();				
			} catch (Exception e) {}
			String fileTime = ""; 
			try {
				fileTime = ((IAttribute) inputdata.getContainer("file_time")).getStringValue();				
			} catch (Exception e) {}
			String detectorSetting = "DETECTOR resolution=(421,421)";
			if (userName.length() > 0)
				userName = "user_name=" + userName;
			if (fileTime.length() > 0)
				fileTime = "file_time=" + fileTime;
			outputfile.append("#\t" + userName + "\t" + fileTime + "\r\n");
			outputfile.append("# " + detectorSetting  + "\t" + getDeviceInfo(inputdata, "mode", null) + 
					 "\t" + getDeviceInfo(inputdata, "preset", "%.0f") + "\r\n");
//			String[] deviceArray = devices.split(",");
//			for (int i = 0; i < deviceArray.length; i++) {
//				
//			}
			outputfile.append("# SAMPLE environment\r\n");
			outputfile.append("#\t" + getDeviceInfo(inputdata, "sx", "%.5f") + "\t" 
					+ getDeviceInfo(inputdata, "sy", "%.5f") + 
					 "\t" + getDeviceInfo(inputdata, "sz", "%.5f") + "\r\n");
			outputfile.append("#\t" + getDeviceInfo(inputdata, "som", "%.5f") + "\t" + getDeviceInfo(inputdata, "stth", "%.5f") + "\r\n");
			outputfile.append("# MONOCHROMATOR environment\r\n");
			outputfile.append("#\t" + getDeviceInfo(inputdata, "mphi", "%.5f") + "\t" + getDeviceInfo(inputdata, "mchi", "%.5f") + "\r\n"); 
			outputfile.append("#\t" + getDeviceInfo(inputdata, "mx", "%.5f") + "\t\t" + getDeviceInfo(inputdata, "my", "%.5f") + "\r\n");
			outputfile.append("#\t" + getDeviceInfo(inputdata, "mom", "%.5f") + "\t" + getDeviceInfo(inputdata, "mtth", "%.5f") + "\r\n");
			outputfile.append("#\t" + getDeviceInfo(inputdata, "mf1", "%.5f") + "\t" + getDeviceInfo(inputdata, "mf2", "%.5f") + "\r\n");
			outputfile.append("# SLITS environment\r\n");
			outputfile.append("#\t" + getDeviceInfo(inputdata, "psp", "%.5f") + "\t" + getDeviceInfo(inputdata, "psw", "%.5f") + 
					 "\t" + getDeviceInfo(inputdata, "psho", "%.5f") + "\r\n");
			outputfile.append("#\t" + getDeviceInfo(inputdata, "ssp", "%.5f") + "\t" + getDeviceInfo(inputdata, "ssw", "%.5f") + 
					 "\t" + getDeviceInfo(inputdata, "ssho", "%.5f") + "\r\n");
			String processingLog = "# " + inputdata.getProcessingLog().replaceAll("\n", "\r\n# ") + "\r\n";
			outputfile.append(processingLog);
			outputfile.append("#\r\n#\r\n");
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
				//				if(xyexport_sep_flag) 
				//					outputfile = new PrintWriter(new FileWriter(new File(fileURI.getRawPath()+"_frame_"+String.valueOf(frame_ct)+".xyd")));
//				String topcomment = String.format(" "+ax_name+" value %f", scnd_loc);
				//			   outputfile.format("# Two theta         Intensity          Sigma%n");
				String varianceName = null;
				try {
					varianceName = ((Plot) inputdata).getVariance().getShortName();
				} catch (Exception e) {
					varianceName = ((Plot) inputdata).findSingal().getTitle() + " variance";
				}
				if (dataRank > 1){
					String topcomment = "Scan variable: " + ax_name + "=" + (new Formatter()).format("%.5f", scnd_loc);
					outputfile.format("# %-79s%n", topcomment);
					String scanInfo = "";
					if (timeIterator != null && timeIterator.hasNext())
						scanInfo += " time=" + (new Formatter()).format("%.1f", timeIterator.getDoubleNext()) + "seconds";
					//				if (bm1Iterator != null && bm1Iterator.hasNext() && bm1Iterator.getDoubleNext() > 0)
					//					scanInfo += " bm1_counts=" + (new Formatter()).format("%.0f", bm1Iterator.getDoubleCurrent());
					//				if (bm1Iterator != null && bm2Iterator.hasNext() && bm2Iterator.getDoubleNext() > 0)
					//					scanInfo += " bm2_counts=" + (new Formatter()).format("%.0f", bm2Iterator.getDoubleCurrent());
					if (bm1Iterator != null && bm3Iterator.hasNext()) {
						 double bm3Value = bm3Iterator.getDoubleNext();
						 if (bm3Value > 0) {
							 scanInfo += "\tbm3_counts=" + (new Formatter()).format("%.0f", bm3Value);
						 }
					}
					if (scanInfo.length() > 0)
						outputfile.append("#" + scanInfo + "\r\n");
				}
				outputfile.format("# " + ((Plot) inputdata).getAxis(inputdata.getAxisArrayList().size() - 1).getTitle() + "         " + 
						((Plot) inputdata).findSingal().getTitle() + "         sigma\r\n");
				while(row_iter.hasNext()) {
					outputfile.format("%10.5f %15.5f %15.5f%n",row_iter.getDoubleNext(),data_iter.getDoubleNext(),
							Math.sqrt(error_iter.getDoubleNext()));
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

	private String getDeviceInfo(Plot inputdata, String deviceName, String numericFormat) {
		String result = "";
		if (deviceName != null){
			try {
				Object item = inputdata.getContainer(deviceName);
				IArray signal = null;
				String units = "";
				if (item instanceof IDataItem){
					signal = ((IDataItem) item).getData();
					units = ((IDataItem) item).getUnitsString();
				}
				else if (item instanceof IAttribute)
					signal = ((IAttribute) item).getValue();
				if (signal.getElementType() == Character.TYPE)
					result = deviceName + "=" + signal.toString();
				else{
					double signalMax = signal.getArrayMath().getMaximum();
					double signalMin = signal.getArrayMath().getMinimum();
					result = deviceName + "=";
					if (numericFormat == null)
						numericFormat = "%.5f";
					if (signalMax == signalMin)
						result += (new Formatter()).format(numericFormat, signalMax) + " " + units;
					else
						result += (new Formatter()).format(numericFormat, signal.getDouble(
								signal.getIndex().set(0))) + " " + units;
				}
			} catch (Exception e) {
			}
		}
		return result;
	}
	@Override
	public void signalExport(URI fileURI, Object signal) throws IOException {
		if (signal instanceof IGroup)
			resultExport(fileURI, (IGroup) signal);
	}

	@Override
	public void signalExport(URI fileURI, Object signal, String title)
	throws IOException {
		signalExport(fileURI, signal);
	}

	@Override
	public void signalExport(URI fileURI, Object signal, boolean isTranspose)
	throws IOException {
		signalExport(fileURI, signal);
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

	private String getProcessInfo(IGroup groupData) {
		String processInfo = "";
		Plot inputdata = null;
		if (groupData instanceof Plot)
			inputdata = (Plot) groupData;
		else 
			return processInfo;
		String log = inputdata.getProcessingLog();
		if (log.contains("efficiency correction"))
			processInfo += "e";
		if (log.contains("geometry curve correction"))
			processInfo += "g";
		if (log.contains("regional integration"))
			processInfo += "i";
		if (log.contains("fitted with"))
			processInfo += "f"; 
		if (processInfo.length() > 0)
			processInfo = "_" + processInfo;
		if (log.contains("y in [")){
			String yInfo = log.substring(log.indexOf("y in ["));
			yInfo = yInfo.substring(6, yInfo.indexOf("]"));
			yInfo = yInfo.replace(",", "_");
			processInfo += "_" + yInfo;
		}
		
		return processInfo;
	}
}
