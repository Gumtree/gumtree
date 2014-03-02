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
package au.gov.ansto.bragg.nbi.dra.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Formatter;
import java.util.NoSuchElementException;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.interfaces.ISliceIterator;
import org.gumtree.data.utils.Utilities;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 01/12/2008
 */
public class XYSigmaExporter extends ConcreteProcessor {

	URI outputFolderName = null;
	Plot inputdata = null;
	Plot outputdata = null;
	Boolean xyexport_skip_Flag = false;
	Boolean xyexport_stop_flag = false;
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.patternset;
	String devices = "";

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.processor.ConcreteProcessor#process()
	 */
	@Override
	public Boolean process() throws Exception {
		outputdata = inputdata;
		if (xyexport_skip_Flag){
			return xyexport_stop_flag;
		}
		IArray outputnumbers = ((NcGroup) inputdata).getSignalArray();
		int[] dataShape = outputnumbers.getShape();
		int dataRank = outputnumbers.getRank();
		String base_comment = "Raw nexus file: " + inputdata.getLocation();
		// For each dimension in the data, write a file
		IArray tth_points = ((Plot) inputdata).getAxis(dataRank-1).getData();
		int tth_rank = tth_points.getRank();
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
		IArray timeOfFlight = null;
		IArray monitor1 = null;
		IArray monitor2 = null;
		IArray monitor3 = null;
		IArrayIterator timeIterator = null;
		IArrayIterator timeOfFlightIterator = null;
		IArrayIterator bm1Iterator = null;
		IArrayIterator bm2Iterator = null;
		IArrayIterator bm3Iterator = null;
		try {
			timeArray = inputdata.findDataItem("detector_time").getData();
			timeIterator = timeArray.getIterator();
		} catch (Exception e) {
			timeArray = inputdata.getRootGroup().getDataItem("detector_time").getData();
			timeIterator = timeArray.getIterator();
		}
		try {
			timeOfFlight = inputdata.findDataItem("time_of_flight").getData();
			timeOfFlightIterator = timeOfFlight.getIterator();
		} catch (Exception e) {
			timeOfFlight = inputdata.getRootGroup().getDataItem("time_of_flight").getData();
			timeOfFlightIterator = timeOfFlight.getIterator();
		}
		try {
			monitor1 = inputdata.findDataItem("bm1_counts").getData();
			bm1Iterator = monitor1.getIterator();
		} catch (Exception e) {
			monitor1 = inputdata.getRootGroup().getDataItem("bm1_counts").getData();
			bm1Iterator = monitor1.getIterator();
		}
		try {
			monitor2 = inputdata.findDataItem("bm2_counts").getData();
			bm2Iterator = monitor2.getIterator();
		} catch (Exception e) {
			monitor2 = inputdata.getRootGroup().getDataItem("bm2_counts").getData();
			bm2Iterator = monitor2.getIterator();
		}
		try {
			monitor3 = inputdata.findDataItem("bm3_counts").getData();
			bm3Iterator = monitor3.getIterator();
		} catch (Exception e) {
			monitor3 = inputdata.getRootGroup().getDataItem("bm3_counts").getData();
			bm3Iterator = monitor3.getIterator();
		}

		IArray second_axis = null;
		String ax_name = "Run";
		if(dataRank==2) {                                                     //get other axis
			Axis scnd_ax = ((Plot) inputdata).getAxis(0);
			second_axis = scnd_ax.getData();
			ax_name = scnd_ax.getTitle();
		} else {
			second_axis = Factory.createArray(double.class, new int[] {1});
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
			boolean isDuplicate = false;
			if (second_axis.getSize() == 2){
				if (second_axis.getArrayMath().getMaximum() - second_axis.getArrayMath().getMinimum() <= second_axis.getArrayMath().getMaximum() * 1E-6)
					isDuplicate = true;
			}
			int frame_ct = 0;                                                      //be sure that file is unique
			//			if(!xyexport_sep_flag)
			File folder = null;
			try{
				folder = new File(outputFolderName);
				if (!folder.exists())
					folder.mkdirs();
			}catch (Exception e) {
				folder = new File(".");
			}
			String rawFileName = (new File(inputdata.getLocation())).getName();
			rawFileName = rawFileName.substring(0, rawFileName.indexOf("."));
			File newFile = new File(folder.getAbsolutePath() + "/" + rawFileName + getProcessInfo() + ".xyd");
			System.out.println("Now outputting 2theta-intensity file named " + newFile.getAbsolutePath());
			outputfile = new PrintWriter(new FileWriter(newFile));
			outputfile.format("# %-79s%n",base_comment);
			outputfile.append("#\t" + getDeviceInfo("experiment_title", null) + "\t" + getDeviceInfo("sample_name", null) + "\r\n");
			outputfile.append("#\t" + getDeviceInfo("sample_description", null) + "\r\n");
			String userName = "";
			try {
				userName = inputdata.findDataItem("user_name").getData().toString();				
			} catch (Exception e) {
				userName = inputdata.getRootGroup().getDataItem("user_name").getData().toString();
			}
			String fileTime = ""; 
			try {
				fileTime = ((IAttribute) inputdata.findContainer("file_time")).getStringValue();				
			} catch (Exception e) {}
			String detectorSetting = "DETECTOR resolution=(421,421)";
			if (userName.length() > 0)
				userName = "user_name=" + userName;
			if (fileTime.length() > 0)
				fileTime = "file_time=" + fileTime;
			outputfile.append("#\t" + userName + "\t" + fileTime + "\r\n");
			outputfile.append("# " + detectorSetting  + "\t" + getDeviceInfo("active_width", "%.1f") + 
					"\t" + getDeviceInfo("mode", null) + 
					"\t" + getDeviceInfo("preset", "%.0f") + "\r\n");
			String[] deviceArray = devices.split(",");
			for (int i = 0; i < deviceArray.length; i++) {
				
			}
			outputfile.append("# SAMPLE environment\r\n");
			outputfile.append("#\t" + getDeviceInfo("sx", "%.5f") + "\t" + getDeviceInfo("sy", "%.5f") + 
					 "\t" + getDeviceInfo("sz", "%.5f") + "\r\n");
			outputfile.append("#\t" + getDeviceInfo("som", "%.5f") + "\t" + getDeviceInfo("stth", "%.5f") + "\r\n");
			outputfile.append("# MONOCHROMATOR environment\r\n");
			outputfile.append("#\t" + getDeviceInfo("mphi", "%.5f") + "\t" + getDeviceInfo("mchi", "%.5f") + "\r\n"); 
			outputfile.append("#\t" + getDeviceInfo("mx", "%.5f") + "\t\t" + getDeviceInfo("my", "%.5f") + "\r\n");
			outputfile.append("#\t" + getDeviceInfo("mom", "%.5f") + "\t" + getDeviceInfo("mtth", "%.5f") + "\r\n");
			outputfile.append("#\t" + getDeviceInfo("mf1", "%.5f") + "\t" + getDeviceInfo("mf2", "%.5f") + "\r\n");
			outputfile.append("# SLITS environment\r\n");
			outputfile.append("#\t" + getDeviceInfo("psp", "%.5f") + "\t" + getDeviceInfo("psw", "%.5f") + 
					 "\t" + getDeviceInfo("psho", "%.5f") + "\r\n");
			outputfile.append("#\t" + getDeviceInfo("ssp", "%.5f") + "\t" + getDeviceInfo("ssw", "%.5f") + 
					 "\t" + getDeviceInfo("ssho", "%.5f") + "\r\n");
			String processingLog = "# " + inputdata.getProcessingLog().replaceAll("\n", "\r\n# ") + "\r\n";
			outputfile.append(processingLog);
			try {
				outputfile.append("# Title: " + inputdata.findDataItem("title").getData().toString() + "\n");
			} catch (Exception e) {
				outputfile.append("# Title: " + inputdata.getRootGroup().getDataItem("title").getData().toString() + "\n");
			}
			outputfile.append("#\r\n#\r\n");
			while(out_iter.hasNext()) {
				IArray tth_row = null;
				try {
					tth_row = tth_iter.getArrayNext();
				} catch(NoSuchElementException e) {  //This might happen if horizontal coordinate is fixed for all frames
					tth_iter = tth_points.getSliceIterator(1);   //re-initialise
					tth_row = tth_iter.getArrayNext();
				}
				double scnd_loc = scnd_iter.getDoubleNext();
				IArray data_row = out_iter.getArrayNext();
				IArray err_row = err_iter.getArrayNext();
				if (data_row.getDouble(data_row.getIndex().set(0)) < -1e4) {
					if (timeIterator != null && timeIterator.hasNext()) {
						timeIterator.next();
					}
					continue;
				}
				IArrayIterator row_iter = tth_row.getIterator();
				IArrayIterator data_iter = data_row.getIterator();
				IArrayIterator error_iter = err_row.getIterator();
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
				String topcomment = "Scan variable: " + ax_name + "=" + (new Formatter()).format("%.5f", scnd_loc);
				outputfile.format("# %-79s%n", topcomment);
				String scanInfo = "";
				try {
					if (timeIterator != null && timeIterator.hasNext()) {
						scanInfo += " time=" + (new Formatter()).format("%.1f", timeIterator.getDoubleNext()) + " seconds";
					} else {
						IIndex timeOfFlightIndex = timeOfFlight.getIndex();
						double time = timeOfFlight.getDouble(timeOfFlightIndex.set(1)) 
							- timeOfFlight.getDouble(timeOfFlightIndex.set(0));
						scanInfo += " time=" + (new Formatter()).format("%.1f", time) + " seconds";
					}
					if (bm1Iterator != null && bm1Iterator.hasNext()) {
						double bm1Value = bm1Iterator.getDoubleNext();
						if (bm1Value >= 0) {
							scanInfo += " bm1_counts=" + (new Formatter()).format("%.0f", bm1Value);	
						}
					} else {
						try{
							double counts = monitor1.getArrayMath().getMaximum();
							scanInfo += " bm1_counts=" + (new Formatter()).format("%.0f", counts);
						} catch (Exception e) {
							double counts = 0;
							scanInfo += " bm1_counts=" + (new Formatter()).format("%.0f", counts);
						}
					}

				} catch (Exception e) {
					// TODO: handle exception
				}
//				if (bm1Iterator != null && bm2Iterator.hasNext() && bm2Iterator.getDoubleNext() >= 0)
//					scanInfo += " bm2_counts=" + (new Formatter()).format("%.0f", bm2Iterator.getDoubleCurrent());
//				if (bm1Iterator != null && bm3Iterator.hasNext() && bm3Iterator.getDoubleNext() >= 0)
//					scanInfo += "\tbm3_counts=" + (new Formatter()).format("%.0f", bm3Iterator.getDoubleCurrent());
				if (scanInfo.length() > 0)
					outputfile.append("#" + scanInfo + "\r\n");
				outputfile.format("# " + ((Plot) inputdata).getAxis(inputdata.getAxisArrayList().size() - 1).getTitle() + "         " + 
						((Plot) inputdata).findSingal().getTitle() + "         sigma\r\n");
				while(row_iter.hasNext()) {
					outputfile.format("%10.5f %15.5f %15.5f%n",row_iter.getDoubleNext(),data_iter.getDoubleNext(),
							Math.sqrt(error_iter.getDoubleNext()));
				}
				outputfile.format("%n");  //one blank line between histograms - this is good for Gnuplot
				//				if(xyexport_sep_flag) outputfile.close();
				if (isDuplicate)
					break;
			}
			outputfile.close();
		}catch (Exception e) {
			if (outputfile != null)
				outputfile.close();
			e.printStackTrace();
		}
		return xyexport_stop_flag;
	}

	private String getProcessInfo() {
		String processInfo = "";
		String log = inputdata.getProcessingLog();
		if (log.contains("use corrected data"))
			processInfo += "c";
		if (log.contains("efficiency correction"))
			processInfo += "e";
		if (log.contains("geometry curve correction"))
			processInfo += "g";
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

	private String getDeviceInfo(String deviceName, String numericFormat) {
		String result = "";
		if (deviceName != null){
			try {
				Object item = inputdata.findContainer(deviceName);
				if (item == null)
					item = inputdata.findContainer("old_" + deviceName);
				if (item == null)
					item = inputdata.getRootGroup().getContainer(deviceName);
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

	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}

	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

	/**
	 * @return the outputdata
	 */
	public Plot getOutputdata() {
		return outputdata;
	}

	/**
	 * @param outputFolderName the outputFolderName to set
	 */
	public void setOutputFolderName(URI outputFolderName) {
		this.outputFolderName = outputFolderName;
	}

	/**
	 * @param inputdata the inputdata to set
	 */
	public void setInputdata(Plot inputdata) {
		this.inputdata = inputdata;
	}

	/**
	 * @param xyexport_Skip_Flag the xyexport_Skip_Flag to set
	 */
	public void setXyexport_skip_Flag(Boolean xyexport_skip_Flag) {
		this.xyexport_skip_Flag = xyexport_skip_Flag;
	}

	/**
	 * @param xyexport_stop_flag the xyexport_stop_flag to set
	 */
	public void setXyexport_stop_flag(Boolean xyexport_stop_flag) {
		this.xyexport_stop_flag = xyexport_stop_flag;
	}

	/**
	 * @param devices the devices to set
	 */
	public void setDevices(String devices) {
		this.devices = devices;
	}


}
