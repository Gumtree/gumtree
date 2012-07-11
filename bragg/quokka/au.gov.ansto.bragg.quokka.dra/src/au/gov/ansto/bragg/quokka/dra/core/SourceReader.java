/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong  (based on Class PrepareSourcePlot)
*    Paul Hathaway (7/05/2009)
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

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

public class SourceReader extends ConcreteProcessor {

	/* Fields for audit trail support */
	private static final String processClass = "SourceReader"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009050701; 

	/* Fields to support client-side processing */
	private static DataStructureType dataStructureType = DataStructureType.plot;
	private static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = true;

	private IGroup sourceReader_inputGroup;
	private IGroup sourceReader_outputGroup;
	private IGroup sourceReader_source;
	private Boolean sourceReader_skip = false;
	private Boolean sourceReader_stop = false;
	private String sourceReader_metaDataNames;
	private Boolean sourceReader_enableNormalisation = false;
	private String sourceReader_normalisationVariable = "bm1_counts";
	
	private enum NormalisationVariable{bm1_counts, bm2_counts, detector_time};
	
	public Boolean process() throws Exception {
		sourceReader_source = sourceReader_inputGroup;
		if (sourceReader_skip) {
			sourceReader_outputGroup = sourceReader_inputGroup;
			dataDimensionType = ((Plot) sourceReader_inputGroup).getDimensionType();
			return sourceReader_stop;
		}
		if (sourceReader_inputGroup == null)
			throw new Exception("Input Group not detected");
		
		sourceReader_outputGroup = preparePlot();
		
		((Plot) sourceReader_outputGroup).reduceTo(3);
		((Plot) sourceReader_source).reduceTo(3);
		
		if (sourceReader_metaDataNames != null){
			addMetaData(sourceReader_outputGroup);
			addMetaData(sourceReader_source);
		}
		dataDimensionType = ((Plot) sourceReader_outputGroup).getDimensionType();
		((NcGroup) sourceReader_outputGroup).setLocation(sourceReader_inputGroup.getLocation());
		return sourceReader_stop;
	}

	private void addMetaData(IGroup group) throws Exception {
		if (sourceReader_metaDataNames == null || 
				sourceReader_metaDataNames.trim().length() == 0)
			return;
		if (group == null)
			throw new Exception("Cannot add metadata to plot group");
		String[] metaDataNames = sourceReader_metaDataNames.split(",");
		for (int i = 0; i < metaDataNames.length; i++) {
			IDataItem item = sourceReader_inputGroup.getDataItem(metaDataNames[i].trim());
			if (item != null){
				IDataItem cloneItem = item.clone();
				cloneItem.addStringAttribute(Util.PLOT_METADATA_ATTRIBUTE_NAME, 
						PlotMetadataType.value.name());
				group.addDataItem(cloneItem);
			}else{
				throw new Exception("Cannot find metadata: " + metaDataNames[i]);
			}
		}
	}

	private Plot preparePlot() throws PlotFactoryException, StructureTypeException {

		Plot source = null, result = null;
		
		if (sourceReader_inputGroup instanceof Plot){
			source = (Plot) PlotFactory.copyToPlot(
					sourceReader_inputGroup, 
					sourceReader_inputGroup.getShortName() + "_sourcecopy", 
					PlotUtil.getDimensionType(sourceReader_inputGroup));
			return (Plot) sourceReader_inputGroup;
		}
		
		DataStructureType dataStructure = Util.getDataStructureType(sourceReader_inputGroup);

		switch (dataStructure) {
		case nexusRoot:
			IGroup nexusData = null;
			try{
				nexusData = NexusUtils.getNexusData(NexusUtils.getNexusEntryList(
						sourceReader_inputGroup).get(0));
			}catch (Exception e) {
				throw new StructureTypeException("Cannot process nexusRoot for plot",e);
			}
			if (nexusData != null)
				try{
					source = (Plot) PlotFactory.copyToPlot(
							    nexusData, 
							    nexusData.getShortName() + "_sourcecopy", 
							    PlotUtil.getDimensionType(nexusData));
					result = (Plot) PlotFactory.copyToPlot(
								nexusData, 
								nexusData.getShortName() + "_plotcopy", 
								PlotUtil.getDimensionType(nexusData));
				}catch (PlotFactoryException e) {
					throw new PlotFactoryException(
							"Cannot process dataset from Nexus file. \n" +
							"Please use hdf reader to check the dataset. \n" + 
							e.getMessage(),e);
				}
		case nexusEntry:
			nexusData = null;
			try{
				nexusData = NexusUtils.getNexusData(sourceReader_inputGroup);
			} catch (Exception e) {
				throw new StructureTypeException("Cannot process nexusEntry for plot",e);
			}
			if (nexusData != null) {
				source = (Plot) PlotFactory.copyToPlot(
					    	nexusData, 
					    	nexusData.getShortName() + "_sourcecopy", 
					    	PlotUtil.getDimensionType(nexusData));
				result = (Plot) PlotFactory.copyToPlot(
							nexusData, 
							nexusData.getShortName() + "_plotcopy", 
						    PlotUtil.getDimensionType(nexusData));
			}
			break;
		case nexusData:
			source = (Plot) PlotFactory.copyToPlot(
					sourceReader_inputGroup, 
					sourceReader_inputGroup.getShortName() + "_sourcecopy", 
					PlotUtil.getDimensionType(sourceReader_inputGroup));
			result = (Plot) PlotFactory.copyToPlot(
					sourceReader_inputGroup, 
					sourceReader_inputGroup.getShortName() + "_plotcopy", 
					PlotUtil.getDimensionType(sourceReader_inputGroup));
			break;
		case plotset:
			result = ((PlotSet) sourceReader_inputGroup).getPlotList().get(0);
			source = (Plot) PlotFactory.copyToPlot(
						result, 
						result.getShortName() + "_sourcecopy", 
						PlotUtil.getDimensionType(result));
			break;
		default:
			throw new StructureTypeException("Cannot determine structure type of dataset");
		}
		sourceReader_source = source;
		return result;
	}

	/**
	 * @return the sourceReader_outputGroup
	 */
	public IGroup getSourceReader_outputGroup() {
		return sourceReader_outputGroup;
	}

	/**
	 * @param sourceReader_inputGroup the sourceReader_inputGroup to set
	 */
	public void setSourceReader_inputGroup(IGroup sourceReader_inputGroup) {
		this.sourceReader_inputGroup = sourceReader_inputGroup;
	}

	public IGroup getSourceReader_source() {
		return sourceReader_source;
	}

	/**
	 * @param sourceReader_skip the sourceReader_skip to set
	 */
	public void setSourceReader_skip(Boolean sourceReader_skip) {
		this.sourceReader_skip = sourceReader_skip;
	}

	/**
	 * @param sourceReader_stop the sourceReader_stop to set
	 */
	public void setSourceReader_stop(Boolean sourceReader_stop) {
		this.sourceReader_stop = sourceReader_stop;
	}

	/**
	 * @param sourceReader_metaDataNames the sourceReader_metaDataNames to set
	 */
	public void setSourceReader_metaDataNames(
			String sourceReader_metaDataNames) {
		this.sourceReader_metaDataNames = sourceReader_metaDataNames;
	}
	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

	/**
	 * @param sourceReader_enableNormalisation the sourceReader_enableNormalisation to set
	 */
	public void setSourceReader_enableNormalisation(
			Boolean sourceReader_enableNormalisation) {
		this.sourceReader_enableNormalisation = sourceReader_enableNormalisation;
	}

	/**
	 * @param sourceReader_normalisationVariable the sourceReader_normalisationVariable to set
	 */
	public void setSourceReader_normalisationVariable(
			String sourceReader_normalisationVariable) {
		this.sourceReader_normalisationVariable = sourceReader_normalisationVariable;
	}	
	
}
