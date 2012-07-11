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
package au.gov.ansto.bragg.quokka.dra.core;

import java.awt.color.ProfileDataException;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.core.plot.PlotUtil;
import au.gov.ansto.bragg.quokka.dra.core.internal.ConcreteProcessor;


public class Source implements ConcreteProcessor{
	IGroup source_groupData = null;
	String source_dataName = "data";
	IGroup source_scanData = null;
	String source_dataStructureType = "plot";
	String source_dataDimensionType = "map";
//	public List<Object> getSource(GroupData data, String dataName, String backgroundFileName) 
	@SuppressWarnings("static-access")
	public Boolean process() throws Exception{
//		List<Object> result = new LinkedList<Object>();
//		DataItem dataVariable = null;
//		try {
//		dataVariable = source_groupData.getDataItem(source_dataName);	
//		} catch (Exception e) {
//		// TODO: handle exception
//		dataVariable = source_groupData.getGroup(source_dataName).findSignal();
//		}
//		try{
//		if (dataVariable == null) dataVariable = source_groupData.getGroup(source_dataName).findSignal();
//		} catch (Exception ex){
//		ex.printStackTrace();
//		}
		try {
			source_scanData = source_groupData.findGroup(source_dataName);
			if (source_scanData == null)
				source_scanData = source_groupData.getGroup(source_dataName);
			if (source_scanData == null)
				source_scanData = source_groupData.getDataItem(source_dataName).getParentGroup();			
		} catch (Exception e) {
			// TODO: handle exception
			throw new ProfileDataException("can not find the data: " + source_dataName);
		}
		if (source_scanData == null)
			throw new ProfileDataException("can not find the data: " + source_dataName);
//		try {
//		source_scanData = source_groupData.getGroup(source_dataName);
//		} catch (Exception e) {
//			// TODO: handle exception
//			source_scanData = source_groupData.getDataItem(source_dataName).getParentGroup();
//		}
//		if (dataVariable == null) dataVariable = source_groupData.getGroup("data").findSignal();
		if (source_scanData == null) source_scanData = ((NcGroup) source_groupData).findSignal().getParentGroup();
//		source_scanData.addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
//				StaticDefinition.DataStructureType.nexus.name());
		DataStructureType structureType = null;
		try{
			structureType = DataStructureType.valueOf(source_dataStructureType);
		}catch (Exception e) {
			// TODO: handle exception
			structureType = DataStructureType.undefined;
		}
		DataDimensionType dimensionType = null;
		try{
			dimensionType = DataDimensionType.valueOf(source_dataDimensionType);
		}catch (Exception e) {
			// TODO: handle exception
			dimensionType = DataDimensionType.undefined;
		}

		if (structureType == DataStructureType.plot){
			if (PlotUtil.getDataStructureType(source_scanData) != DataStructureType.plot){
				IGroup plotCopy = source_scanData.findGroup(source_scanData.getShortName() + "plot_copy");
				if (plotCopy == null) 
					source_scanData = PlotFactory.copyToPlot(source_scanData, 
							source_scanData.getShortName() + "plot_copy", dimensionType);
				else source_scanData = plotCopy;
				if (((Plot) source_scanData).getVariance() == null)
					PlotFactory.addDataVarianceToPlot(source_scanData, "plot_variance", 
							((NcGroup) source_scanData).getSignalArray());
			}
		} else {
			source_scanData.addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
					structureType.name());
			source_scanData.addStringAttribute(StaticDefinition.DATA_DIMENSION_TYPE, 
					dimensionType.name());
		}
		
//		Array dataArray = dataVariable.read();
//		result.add(dataGroup);
//		result.add(dataArray);
//		double[] backgroundData = null;
//		int[] dataShape = dataArray.getShape();
//		if (dataShape.length < 2 || dataShape.length > 3) 
//		throw new Exception("wrong echidna data");
//		else{
//		int row, column;
//		if (dataShape.length == 3){
//		row = dataShape[1];
//		column = dataShape[2];
//		}else{
//		row = dataShape[0];
//		column = dataShape[1];
//		}
//		backgroundData = new double[row * column];
//		for (int i = 0; i < backgroundData.length; i ++) backgroundData[i] = 5.;
//		Array backgroundArray = Array.factory(double.class, new int[]{row, column},
//		backgroundData);
//		result.add(backgroundArray);
//		}
//		Array backgroundArray = backgroundGroup.getVariable("data").read();
//		result.add(backgroundArray);
//		result.add(source_backgroundData);
//		return result;
		return false;
	}
	public IGroup getSource_groupData() {
		return source_groupData;
	}
	public void setSource_dataName(String source_dataName) {
		this.source_dataName = source_dataName;
	}
	public void setSource_scanData(IGroup source_scanData) {
		this.source_scanData = source_scanData;
	}
	public void setSource_groupData(IGroup source_groupData) {
		this.source_groupData = source_groupData;
	}
	public IGroup getSource_scanData() {
		return source_scanData;
	}

	public DataStructureType getDataStructureType() {
		return DataStructureType.valueOf(source_dataStructureType);
	}

	public DataDimensionType getDataDimensionType() {
		return DataDimensionType.valueOf(source_dataDimensionType);
	}


	public void setSource_dataStructureType(String source_dataStructureType) {
		this.source_dataStructureType = source_dataStructureType;
	}
	
	public void setSource_dataDimensionType(String source_dataDimensionType) {
		this.source_dataDimensionType = source_dataDimensionType;
	}

}
