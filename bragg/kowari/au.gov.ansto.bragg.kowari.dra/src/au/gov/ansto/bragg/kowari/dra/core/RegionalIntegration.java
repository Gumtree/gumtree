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

package au.gov.ansto.bragg.kowari.dra.core;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.region.RegionUtils;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 31/07/2008
 */
public class RegionalIntegration extends ConcreteProcessor {

	IGroup regionalIntegration_input;
	Boolean regionalIntegration_skip = false;
	Boolean regionalIntegration_stop = false;
	IGroup regionalIntegration_region;
	String regionalIntegration_resultName = "regional_intensity";
	IGroup regionalIntegration_output;
	
	public static DataStructureType dataStructureType = DataStructureType.plot;
	public static DataDimensionType dataDimensionType = DataDimensionType.pattern;

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
//		regionalIntegration_input.getSignalArray().sumForDimension(0);
		if (regionalIntegration_region != null){
			regionalIntegration_input = RegionUtils.applyRegionToGroup(regionalIntegration_input, 
					regionalIntegration_region);
		}
		if (regionalIntegration_input instanceof Plot){
			Plot inputPlot = (Plot) regionalIntegration_input;
			regionalIntegration_output = inputPlot.sumForDimension(0);
			regionalIntegration_output.setShortName(regionalIntegration_resultName);
		}
		return regionalIntegration_stop;
	}

	/**
	 * @return the regionalIntegration_output
	 */
	public IGroup getRegionalIntegration_output() {
		return regionalIntegration_output;
	}

	/**
	 * @param regionalIntegration_input the regionalIntegration_input to set
	 */
	public void setRegionalIntegration_input(IGroup regionalIntegration_input) {
		this.regionalIntegration_input = regionalIntegration_input;
	}

	/**
	 * @param regionalIntegration_skip the regionalIntegration_skip to set
	 */
	public void setRegionalIntegration_skip(Boolean regionalIntegration_skip) {
		this.regionalIntegration_skip = regionalIntegration_skip;
	}

	/**
	 * @param regionalIntegration_stop the regionalIntegration_stop to set
	 */
	public void setRegionalIntegration_stop(Boolean regionalIntegration_stop) {
		this.regionalIntegration_stop = regionalIntegration_stop;
	}

	/**
	 * @param regionalIntegration_region the regionalIntegration_region to set
	 */
	public void setRegionalIntegration_region(IGroup regionalIntegration_region) {
		this.regionalIntegration_region = regionalIntegration_region;
	}

	/**
	 * @param regionalIntegration_resultName the regionalIntegration_resultName to set
	 */
	public void setRegionalIntegration_resultName(
			String regionalIntegration_resultName) {
		this.regionalIntegration_resultName = regionalIntegration_resultName;
	}

	public static DataStructureType getDataStructureType() {
		return dataStructureType;
	}

	public static void setDataStructureType(DataStructureType dataStructureType) {
		RegionalIntegration.dataStructureType = dataStructureType;
	}

	public static DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

	public static void setDataDimensionType(DataDimensionType dataDimensionType) {
		RegionalIntegration.dataDimensionType = dataDimensionType;
	}


}
