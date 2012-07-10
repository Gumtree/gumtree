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
package au.gov.ansto.bragg.nbi.dra.source;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.ISliceIterator;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.PlotMetadataType;
import au.gov.ansto.bragg.datastructures.core.Util;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.core.plot.PlotSet;
import au.gov.ansto.bragg.datastructures.core.plot.PlotUtil;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 23/09/2008
 */
public class PrepareSourcePlot extends ConcreteProcessor {

	private IGroup prepareSourcePlot_inputGroup;
	private IGroup prepareSourcePlot_outputGroup;
	private Boolean prepareSourcePlot_skip = false;
	private Boolean prepareSourcePlot_stop = false;
	private String prepareSourcePlot_metaDataNames;
	private Boolean useCorrectedData = false;
	private Boolean prepareSourcePlot_enableNormalisation = false;
	private String prepareSourcePlot_normalisationVariable = "bm1_counts";
	private DataStructureType dataStructureType = DataStructureType.plot;
	private DataDimensionType dataDimensionType = DataDimensionType.mapset;
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.nbi.dra.core.ConcreteProcessor#process()
	 */
	
	private enum NormalisationVariable{bm1_counts, bm2_counts, detector_time};
	
	public Boolean process() throws Exception {
		if (prepareSourcePlot_skip){
			prepareSourcePlot_outputGroup = prepareSourcePlot_inputGroup;
			dataDimensionType = ((Plot) prepareSourcePlot_inputGroup).getDimensionType();
			return prepareSourcePlot_stop;
		}
		if (prepareSourcePlot_inputGroup == null)
			throw new Exception("Input does not exist");
		prepareSourcePlot_outputGroup = preparePlot();
		if (useCorrectedData)
			((Plot) prepareSourcePlot_outputGroup).addProcessingLog("use corrected data");
		((Plot) prepareSourcePlot_outputGroup).reduceTo(3);
		if (prepareSourcePlot_metaDataNames != null){
			addMetaData();
		}
		if (prepareSourcePlot_enableNormalisation){
			IArray variableArray = null;
			try{
				variableArray = prepareSourcePlot_inputGroup.getDataItem(
						prepareSourcePlot_normalisationVariable).getData();
			}catch (Exception e) {
			}
			if (variableArray != null){
				boolean needNormalisation = false;
				IArrayIterator variableIterator = variableArray.getIterator();
				double currentValue = Double.NaN;
				double normalisationAverage = 0;
				if (variableIterator.hasNext()){
					currentValue = variableIterator.getDoubleNext();
					normalisationAverage += currentValue;
				}
				while(variableIterator.hasNext()){
					double nextValue = variableIterator.getDoubleNext();
					if (nextValue != currentValue)
						needNormalisation = true;
					normalisationAverage += nextValue;
				}
				if (needNormalisation){
					normalisationAverage = normalisationAverage / variableArray.getSize();
					ISliceIterator dataSliceIterator = ((Plot) ((NcGroup) prepareSourcePlot_outputGroup).findSignal()).
							findSignalArray().getSliceIterator(0);
					variableIterator = variableArray.getIterator();
					while(dataSliceIterator.hasNext() && variableIterator.hasNext()){
						IArray dataSlice = dataSliceIterator.getArrayNext();
						double nextValue = variableIterator.getDoubleNext();
						dataSlice.getArrayMath().scale(normalisationAverage / nextValue);
					}
				}
			}
		}
		dataDimensionType = ((Plot) prepareSourcePlot_outputGroup).getDimensionType();
		((NcGroup) prepareSourcePlot_outputGroup).setLocation(prepareSourcePlot_inputGroup.getLocation());
		return prepareSourcePlot_stop;
	}

	private void addMetaData() throws Exception {
		if (prepareSourcePlot_metaDataNames == null || prepareSourcePlot_metaDataNames.trim().length() == 0)
			return;
		if (prepareSourcePlot_outputGroup == null)
			throw new Exception("can not put metadata to output");
		String[] metaDataNames = prepareSourcePlot_metaDataNames.split(",");
		for (int i = 0; i < metaDataNames.length; i++) {
			IDataItem item = prepareSourcePlot_inputGroup.getDataItem(metaDataNames[i].trim());
			if (item != null){
				IDataItem cloneItem = item.clone();
				cloneItem.addStringAttribute(Util.PLOT_METADATA_ATTRIBUTE_NAME, 
						PlotMetadataType.value.name());
				prepareSourcePlot_outputGroup.addDataItem(cloneItem);
			}else{
				throw new Exception("can not find metadata: " + metaDataNames[i]);
			}
		}
	}

	private Plot preparePlot() throws PlotFactoryException, StructureTypeException{
		if (prepareSourcePlot_inputGroup instanceof Plot){
			return (Plot) prepareSourcePlot_inputGroup;
		}
		DataStructureType dataStructure = Util.getDataStructureType(prepareSourcePlot_inputGroup);
		switch (dataStructure) {
		case nexusRoot:
			IGroup nexusData = null;
			try{
				if (useCorrectedData)
					nexusData = NexusUtils.getCorrectedNexusData(NexusUtils.getNexusEntryList(
							prepareSourcePlot_inputGroup).get(0));
				else
					nexusData = NexusUtils.getNexusData(NexusUtils.getNexusEntryList(
							prepareSourcePlot_inputGroup).get(0));
			}catch (Exception e) {
				break;
			}
			if (nexusData != null)
				try{
					return (Plot) PlotFactory.copyToPlot(nexusData, nexusData.getShortName() + "_plotcopy", 
						PlotUtil.getDimensionType(nexusData));
				}catch (PlotFactoryException e) {
					throw new PlotFactoryException("the data read from Nexus file is invalid. \n" +
							"Please use hdf reader to check the data first; " + e.getMessage());
				}
		case nexusEntry:
			nexusData = null;
			try{
				if (useCorrectedData)
					nexusData = NexusUtils.getCorrectedNexusData(prepareSourcePlot_inputGroup);
				else
					nexusData = NexusUtils.getNexusData(prepareSourcePlot_inputGroup);
			}catch (Exception e) {
				break;
			}
			if (nexusData != null)
				return (Plot) PlotFactory.copyToPlot(nexusData, nexusData.getShortName() + "_plotcopy", 
						PlotUtil.getDimensionType(nexusData));
		case nexusData:
			return (Plot) PlotFactory.copyToPlot(prepareSourcePlot_inputGroup, 
					prepareSourcePlot_inputGroup.getShortName() + "_plotcopy", 
					PlotUtil.getDimensionType(prepareSourcePlot_inputGroup));
		case plotset:
			return ((PlotSet) prepareSourcePlot_inputGroup).getPlotList().get(0);
		default:
			break;
		}
		throw new StructureTypeException("can not prepare plot from this type of data");
	}

	/**
	 * @return the prepareSourcePlot_outputGroup
	 */
	public IGroup getPrepareSourcePlot_outputGroup() {
		return prepareSourcePlot_outputGroup;
	}

	/**
	 * @param prepareSourcePlot_inputGroup the prepareSourcePlot_inputGroup to set
	 */
	public void setPrepareSourcePlot_inputGroup(IGroup prepareSourcePlot_inputGroup) {
		this.prepareSourcePlot_inputGroup = prepareSourcePlot_inputGroup;
	}

	/**
	 * @param prepareSourcePlot_skip the prepareSourcePlot_skip to set
	 */
	public void setPrepareSourcePlot_skip(Boolean prepareSourcePlot_skip) {
		this.prepareSourcePlot_skip = prepareSourcePlot_skip;
	}

	/**
	 * @param prepareSourcePlot_stop the prepareSourcePlot_stop to set
	 */
	public void setPrepareSourcePlot_stop(Boolean prepareSourcePlot_stop) {
		this.prepareSourcePlot_stop = prepareSourcePlot_stop;
	}

	/**
	 * @param prepareSourcePlot_metaDataNames the prepareSourcePlot_metaDataNames to set
	 */
	public void setPrepareSourcePlot_metaDataNames(
			String prepareSourcePlot_metaDataNames) {
		this.prepareSourcePlot_metaDataNames = prepareSourcePlot_metaDataNames;
	}
	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

	/**
	 * @param prepareSourcePlot_enableNormalisation the prepareSourcePlot_enableNormalisation to set
	 */
	public void setPrepareSourcePlot_enableNormalisation(
			Boolean prepareSourcePlot_enableNormalisation) {
		this.prepareSourcePlot_enableNormalisation = prepareSourcePlot_enableNormalisation;
	}

	/**
	 * @param prepareSourcePlot_normalisationVariable the prepareSourcePlot_normalisationVariable to set
	 */
	public void setPrepareSourcePlot_normalisationVariable(
			String prepareSourcePlot_normalisationVariable) {
		this.prepareSourcePlot_normalisationVariable = prepareSourcePlot_normalisationVariable;
	}

	/**
	 * @param useCorrectedData the useCorrectedData to set
	 */
	public void setUseCorrectedData(Boolean useCorrectedData) {
		this.useCorrectedData = useCorrectedData;
	}	
	
}
