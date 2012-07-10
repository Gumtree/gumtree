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
package au.gov.ansto.bragg.nbi.dra.correction;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.ISliceIterator;
import org.gumtree.data.utils.Utilities;
import org.gumtree.service.cli.ICommandLineOptions;

import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 10/10/2008
 */
public class BackgroundCorrection extends ConcreteProcessor{

	private Plot backgroundCorrection_inputPlot;
	private Plot backgroundCorrection_outputPlot;
	private Integer backgroundCorrection_index = -1;
	private Boolean backgroundCorrection_skip = false;
	private Boolean backgroundCorrection_stop = false;
	private String backgroundCorrection_factor = "monitor counts 1";
	private URI backgroundCorrection_mapURI;
	
	private static final String OPTION_ALGO_SET = "algoSet";
	private DetectorMode mode = DetectorMode.TIME;
	
	public Boolean process() throws Exception {
		// TODO Auto-generated method stub
		if (backgroundCorrection_skip || backgroundCorrection_mapURI == null){
			backgroundCorrection_outputPlot = backgroundCorrection_inputPlot;
			return backgroundCorrection_stop;
		}
		IGroup backgroundData = null;
		if (backgroundCorrection_mapURI != null){
			String dictionaryPath = null;
			try {
				dictionaryPath = getDictionaryPath();
				backgroundData = (IGroup) Utilities.findObject(backgroundCorrection_mapURI, dictionaryPath);
				backgroundData = NexusUtils.getNexusEntryList(backgroundData).get(0);
				backgroundData = NexusUtils.getNexusData(backgroundData);
			} catch (Exception e) {
				// TODO: handle exception
				backgroundData = NexusUtils.getNexusData(backgroundCorrection_mapURI);
			}
		}
		if (backgroundData == null){
			backgroundCorrection_outputPlot = backgroundCorrection_inputPlot;
			return backgroundCorrection_stop;
		}
//		String mapStartTime = backgroundData.getParentGroup().findDataItem("start_time").getData().toString();
//		String mapEndTime = backgroundData.getParentGroup().findDataItem("end_time").getData().toString();
//		String dataStartTime = backgroundCorrection_inputPlot.getDataItem("start_time").getData().toString();
//		String dataEndTime = backgroundCorrection_inputPlot.getDataItem("end_time").getData().toString();
//		long mapTime = MathUtils.timeDifference(mapStartTime, mapEndTime);
//		long dataTime = MathUtils.timeDifference(dataStartTime, dataEndTime);
		IArray factor;
		double mapReading = 0;
		IArray dataReading = null;
		if (mode == DetectorMode.TIME){
			try{
				mapReading = backgroundData.getDataItem("detector_time").getData().getArrayMath().getMaximum();
			}catch (Exception e) {
				try{
					mapReading = backgroundData.getParentGroup().findGroup("instrument").
						findGroup("detector").findDataItem("time").getData().getArrayMath().getMaximum();
				}catch (Exception e1) {}
			}
			try{
				dataReading = backgroundCorrection_inputPlot.getDataItem("detector_time").getData();
			}catch (Exception e) {}
		}else if (mode == DetectorMode.DETECTOR_TOTAL){
			try {
				mapReading = ((NcGroup) backgroundData).getSignalArray().getArrayMath().sum();
				dataReading = ((NcGroup) backgroundCorrection_inputPlot).getSignalArray().getArrayMath().sumForDimension(0, false).getArray();
			} catch (Exception e) {
			}
		}else if (mode == DetectorMode.MONITOR1){
			try{
				mapReading = backgroundData.getDataItem("monitor1_counts").getData().getArrayMath().getMaximum();
			}catch (Exception e) {
				try{
					mapReading = backgroundData.getParentGroup().findGroup("monitor").
						findDataItem("bm1_counts").getData().getArrayMath().getMaximum();
				}catch (Exception e1) {}
			}
			try{
				dataReading = backgroundCorrection_inputPlot.getDataItem("monitor1_counts").getData();
			}catch (Exception e) {}
		}else if (mode == DetectorMode.MONITOR2){
			try{
				mapReading = backgroundData.getDataItem("monitor2_counts").getData().getArrayMath().getMaximum();
			}catch (Exception e) {
				try{
					mapReading = backgroundData.getParentGroup().findGroup("monitor").
						findDataItem("bm2_counts").getData().getArrayMath().getMaximum();
				}catch (Exception e1) {}
			}
			try{
				dataReading = backgroundCorrection_inputPlot.getDataItem("monitor2_counts").getData();
			}catch (Exception e) {}
		}else if (mode == DetectorMode.MONITOR3){
			try{
				mapReading = backgroundData.getDataItem("monitor3_counts").getData().getArrayMath().getMaximum();
			}catch (Exception e) {
				try{
					mapReading = backgroundData.getParentGroup().findGroup("monitor").
						findDataItem("bm3_counts").getData().getArrayMath().getMaximum();
				}catch (Exception e1) {}
			}
			try{
				dataReading = backgroundCorrection_inputPlot.getDataItem("monitor3_counts").getData();
			}catch (Exception e) {}
		}
		if (mapReading == 0 || dataReading == null)
			throw new StructureTypeException("invalid time stamp in the data/map file (zero value)");
		factor = dataReading.getArrayMath().toScale(- 1.0 / mapReading).getArray();
		if (factor != null){
			IArray backgroundArray = ((NcGroup) backgroundData).getSignalArray().getArrayUtils().reduce().getArray();
			if (backgroundCorrection_inputPlot.findSingal().getRank() > 2){
				backgroundCorrection_outputPlot = backgroundCorrection_inputPlot.copyToDouble();
				ISliceIterator sliceIterator = backgroundCorrection_outputPlot.findSignalArray().getSliceIterator(0);
				IArrayIterator factorIterator = factor.getIterator();
				while (sliceIterator.hasNext() && factorIterator.hasNext())
					sliceIterator.getArrayNext().getArrayMath().add(backgroundArray.getArrayMath().toScale(factorIterator.getDoubleNext()).getArray());
			}else{
				double factorValue = 1;
				if (backgroundCorrection_index < 0)
					factorValue = factor.getArrayMath().sum() / factor.getSize();
				else
					factorValue = factor.getDouble(factor.getIndex().set(backgroundCorrection_index));
				System.out.println(backgroundCorrection_inputPlot.sum().getData());
				backgroundCorrection_outputPlot = backgroundCorrection_inputPlot.toAdd(backgroundArray.getArrayMath().toScale(
						factorValue).getArray());
				System.out.println(backgroundCorrection_outputPlot.sum().getData());
			}
			IArrayIterator dataIterator = backgroundCorrection_outputPlot.findSignalArray().getIterator();
			while(dataIterator.hasNext())
				if (dataIterator.getDoubleNext() < 0)
					dataIterator.next().setDoubleCurrent(0);
		}else 
			backgroundCorrection_outputPlot = backgroundCorrection_inputPlot;
		return backgroundCorrection_stop;
	}

	/**
	 * @return the backgroundCorrection_outputPlot
	 */
	public Plot getBackgroundCorrection_outputPlot() {
		return backgroundCorrection_outputPlot;
	}

	/**
	 * @param backgroundCorrection_inputPlot the backgroundCorrection_inputPlot to set
	 */
	public void setBackgroundCorrection_inputPlot(
			Plot backgroundCorrection_inputPlot) {
		this.backgroundCorrection_inputPlot = backgroundCorrection_inputPlot;
	}

	/**
	 * @param backgroundCorrection_skip the backgroundCorrection_skip to set
	 */
	public void setBackgroundCorrection_skip(Boolean backgroundCorrection_skip) {
		this.backgroundCorrection_skip = backgroundCorrection_skip;
	}

	/**
	 * @param backgroundCorrection_stop the backgroundCorrection_stop to set
	 */
	public void setBackgroundCorrection_stop(Boolean backgroundCorrection_stop) {
		this.backgroundCorrection_stop = backgroundCorrection_stop;
	}

	/**
	 * @param backgroundCorrection_mapURI the backgroundCorrection_mapURI to set
	 * @throws StructureTypeException 
	 */
	public void setBackgroundCorrection_mapURI(URI backgroundCorrection_mapURI)
	throws StructureTypeException {
		this.backgroundCorrection_mapURI = backgroundCorrection_mapURI;
	}

	/**
	 * @param backgroundCorrection_factor the backgroundCorrection_factor to set
	 */
	public void setBackgroundCorrection_factor(String backgroundCorrection_factor) {
		this.backgroundCorrection_factor = backgroundCorrection_factor;
		mode = DetectorMode.getInstance(backgroundCorrection_factor);
	}

	private String getDictionaryPath() throws IOException, URISyntaxException{
		String pluginId = null;
		ICommandLineOptions options = ServiceUtils.getService(ICommandLineOptions.class);
		if(options.hasOptionValue(OPTION_ALGO_SET)) {
			pluginId = options.getOptionValue(OPTION_ALGO_SET);
			File dict_file = new File(ConverterLib.getDictionaryPath(pluginId));
			return dict_file.getAbsolutePath();
		}
		return null;
	}

	/**
	 * @param backgroundCorrection_index the backgroundCorrection_index to set
	 */
	public void setBackgroundCorrection_index(Integer backgroundCorrection_index) {
		this.backgroundCorrection_index = backgroundCorrection_index;
	}
	
}
