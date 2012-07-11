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

import java.io.IOException;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.core.plot.PlotUtil;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 30/05/2008
 */
public class FindData extends ConcreteProcessor {

	IGroup source_groupData = null;
	String source_dataName = "data";
	IGroup source_scanData = null;
	String source_dataStructureType = "plot";
	String source_dataDimensionType = "mapset";

	public IGroup getSource_scanData() {
		return source_scanData;
	}

	public void setSource_groupData(IGroup source_groupData) {
		this.source_groupData = source_groupData;
	}

	public void setSource_dataName(String source_dataName) {
		this.source_dataName = source_dataName;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.echidna.dra.core.ConcreteProcessor#getDataDimensionType()
	 */
	public DataDimensionType getDataDimensionType() {
		// TODO Auto-generated method stub
		return DataDimensionType.valueOf(source_dataDimensionType);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.echidna.dra.core.ConcreteProcessor#getDataStructureType()
	 */
	public DataStructureType getDataStructureType() {
		// TODO Auto-generated method stub
		return DataStructureType.valueOf(source_dataStructureType);
	}

	public Boolean process() throws Exception {
		// TODO Auto-generated method stub
		try {
			source_scanData = source_groupData.getGroup(source_dataName);
			if (source_scanData == null)
				source_scanData = source_groupData.getDataItem(source_dataName).getParentGroup();			
		} catch (Exception e) {
			// TODO: handle exception
			throw new Exception("can not find the data: " + source_dataName);
		}
		if (source_scanData == null)
			throw new Exception("can not find the data: " + source_dataName);
//		try {
//		source_scanData = source_groupData.getGroup(source_dataName);
//		} catch (Exception e) {
//		// TODO: handle exception
//		source_scanData = source_groupData.getDataItem(source_dataName).getParentGroup();
//		}
//		if (dataVariable == null) dataVariable = source_groupData.getGroup("data").findSignal();
		if (source_scanData == null) source_scanData = ((NcGroup) source_groupData).findSignal().getParentGroup();
//		source_scanData.addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
//		StaticDefinition.DataStructureType.nexus.name());
		DataStructureType structureType = null;
		try{
			structureType = DataStructureType.valueOf(source_dataStructureType);
		}catch (Exception e) {
			// TODO: handle exception
			structureType = DataStructureType.undefined;
		}
		DataDimensionType dimensionType = null;
//		try{
//		dimensionType = DataDimensionType.valueOf(source_dataDimensionType);
//		}catch (Exception e) {
//		// TODO: handle exception
//		dimensionType = DataDimensionType.undefined;
//		}
		dimensionType = getDataDimensionTye(source_scanData);
		if (structureType == DataStructureType.plot){
			if (PlotUtil.getDataStructureType(source_scanData) != DataStructureType.plot){
				IGroup plotCopy = source_scanData.findGroup(source_scanData.getShortName() + "plot_copy");
				if (plotCopy == null) 
					source_scanData = PlotFactory.copyToPlot(source_scanData, 
							source_scanData.getShortName() + "plot_copy", dimensionType);
				else source_scanData = plotCopy;
//				DataItem variance = ((Plot) source_scanData).getVariance();
//				if (variance == null)
//					((Plot) source_scanData).addDataVariance(varianceArray)
			}
		} else {
			source_scanData.addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
					structureType.name());
			source_scanData.addStringAttribute(StaticDefinition.DATA_DIMENSION_TYPE, 
					dimensionType.name());
		}
		int[] shape = ((NcGroup) source_scanData).findSignal().getShape();
//		if (shape.length == 3 && shape[0] == 1){
		if (source_scanData instanceof Plot)
			((Plot) source_scanData).reduce();
		source_dataDimensionType = PlotUtil.getDimensionType(source_scanData).name();
		return false;
	}

	private DataDimensionType getDataDimensionTye(IGroup groupData) throws IOException {
		// TODO Auto-generated method stub
		IArray rawdata = ((NcGroup) groupData).findSignal().getData();
		int rank = rawdata.getRank();
		switch(rank){
		case 1:
			return DataDimensionType.pattern;
		case 2: 
			if (getDataDimensionType() == DataDimensionType.map || 
					getDataDimensionType() == DataDimensionType.patternset)
				return getDataDimensionType();
			else
				return DataDimensionType.map;
		case 3: 
			if (getDataDimensionType() == DataDimensionType.mapset || 
					getDataDimensionType() == DataDimensionType.volume)
				return getDataDimensionType();
			else
				return DataDimensionType.mapset;
		case 4:
			return DataDimensionType.mapset;
		case 5:
			return DataDimensionType.mapset;
		default:
			return DataDimensionType.undefined;
		}
	}


}
