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
package au.gov.ansto.bragg.datastructures.core.plot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;

/**
 * @author nxi
 * Created on 13/03/2008
 */
public class PlotUtil {

	/**
	 * Get a list of slices of data from the original data. Given conditions the original data has 
	 * more than one dimension. This method will slice the given data at the first dimension 
	 * (dimension 0), wrap each slice with a new Plotable Group, and put them into a list. 
	 * @param plot a plotable data, in Group type 
	 * @return List of Group objects
	 * @throws StructureTypeException
	 * @throws PlotFactoryException
	 * Created on 19/03/2008
	 */
	public static List<IGroup> slice(IGroup plot) 
	throws StructureTypeException, PlotFactoryException{
		if (plot instanceof PlotSet){
			List<IGroup> plotList = new ArrayList<IGroup>();
			for (Plot item : ((PlotSet) plot).getPlotList()){
				plotList.add(item);
			}
			return plotList;
		}
		DataDimensionType type = getDimensionType(plot);
		List<IGroup> plotList = new ArrayList<IGroup>();
		if (type.name().endsWith("set")){
			String typeName = type.name();
			DataDimensionType newType;
			try{
				newType = DataDimensionType.valueOf(typeName.substring(0, 
						typeName.lastIndexOf("set")));
			}catch (Exception e) {
				throw new StructureTypeException("not a sliceable plot set");
			}
			Plot plotable = (Plot) plot;
			
			Data plotData = plotable.findSingal();
			int[] shape = plotData.getShape();
			IArray dataArray = null;
			IArray varianceArray = null;
			try {
				dataArray = plotData.getData();
				varianceArray = plotData.getVariance().getData();
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < shape[0]; i++) {
				Plot subGroup = (Plot) PlotFactory.createPlot(plotable, 
						plotable.getShortName() + "_item_" + i, newType);
				PlotFactory.addDataToPlot(subGroup, plotData.getShortName() + "_item_" + i, 
						dataArray.getArrayUtils().slice(0, i).getArray(), plotData.getTitle() + " Item " + i, 
						plotData.getUnitsString(), varianceArray == null ? null : varianceArray.getArrayUtils().slice(0, i).getArray());
				for(int j = 1; j < shape.length; j ++)
					try {
						subGroup.addAxis(plotable.getAxis(j), j - 1);
					} catch (Exception e) {
//						e.printStackTrace();
						throw new PlotFactoryException(e);
					} 
				plotList.add(subGroup);
			}
		}
//		else throw new StructureTypeException("not a sliceable plot set");
		else{
			plotList.add(plot);
		}
		return plotList;
	}
	
	/**
	 * Find the dimension type of the Group data. It will find the attribute with name of
	 * 'dataDimensionType', and convert the value to enum type. 
	 * @param plot in Group type
	 * @return DataDimensionType enum type
	 * @throws StructureTypeException
	 * Created on 19/03/2008
	 */
	public static DataDimensionType getDimensionType(IGroup plot) {
//		DataStructureType structureType = getDataStructureType(plot);
//		if (structureType != DataStructureType.plot) 
//			throw new StructureTypeException("data structure is not plot");
		IAttribute dataDimensionAttribute = plot.getAttribute(StaticDefinition.DATA_DIMENSION_TYPE);
		if (dataDimensionAttribute == null) 
			return analysisRawDataDimensionType(plot);
		String dataDimensionTypeName = dataDimensionAttribute.getStringValue();
		DataDimensionType type = DataDimensionType.valueOf(dataDimensionTypeName);
		if (type == null) return DataDimensionType.undefined;
		return type;
	}
	
	public static DataDimensionType analysisRawDataDimensionType(IGroup group){
		IArray dataArray = null;
		int[] shape = null;
		try {
			dataArray = ((NcGroup) group).getSignalArray();
			shape = dataArray.getArrayUtils().reduce().getArray().getShape();
		} catch (Exception e) {
			return DataDimensionType.undefined;
		}
		int rank = shape.length;
		switch (rank) {
		case 1:
			return DataDimensionType.pattern;
		case 2:
			if (shape[0] < 30)
				return DataDimensionType.patternset;
			else
				return DataDimensionType.map;
		case 3:
			return DataDimensionType.mapset;
		case 4:
			return DataDimensionType.volumeset;
		default:
			return DataDimensionType.undefined;
		}
	}
	/**
	 * Find the data structure type of the Group data. It will find the attribute with name of
	 * 'dataStructureType', and convert the value to enum type. 
	 * @param group in GDM Group type
	 * @return DataStructureType enum type
	 * Created on 19/03/2008
	 */
	public static DataStructureType getDataStructureType(IGroup group){
		IAttribute dataStructureAttribute = group.getAttribute(StaticDefinition.DATA_STRUCTURE_TYPE);
		if (dataStructureAttribute == null) return DataStructureType.undefined;
		String dataStructureTypeName = dataStructureAttribute.getStringValue();
		DataStructureType type = DataStructureType.valueOf(dataStructureTypeName);
		if (type == null) return DataStructureType.undefined;
		return type;
	}
	
	/**
	 * Convert a String type object to DataStructureType enum type. Note here the argument
	 * is in Object type. But the object.toString() method will be called to first convert
	 * the object into a String type object.
	 * @param typeValue any Object
	 * @return DataStructureType enum type
	 * Created on 19/03/2008
	 */
	public static DataStructureType readDataStructureType(Object typeValue){
		if (typeValue == null) return DataStructureType.undefined;
		String string = typeValue.toString();
		DataStructureType type;
		try {
			type = DataStructureType.valueOf(string);
		} catch (Exception e) {
			return DataStructureType.undefined;
		}
		return type;
	}
	
	/**
	 * Convert a String type object to DataDimensionType enum type. Note here the argument
	 * is in Object type. But the object.toString() method will be called to first convert
	 * the object into a String type object.
	 * @param typeValue any Object
	 * @return DataDimensionType enum type
	 * Created on 19/03/2008
	 */	
	public static DataDimensionType readDataDimensionType(Object typeValue){
		if (typeValue == null) return DataDimensionType.undefined;
		String string = typeValue.toString();
		DataDimensionType type;
		try {
			type = DataDimensionType.valueOf(string);
		} catch (Exception e) {
			return DataDimensionType.undefined;
		}
		return type;
	}
//	public static 

	public static void removeNaN(Plot plot) {
		try {
			IArray data = plot.findSignalArray();
			IArrayIterator dataIter = data.getIterator();
			while(dataIter.hasNext()){
				if (Double.isNaN(dataIter.getDoubleNext()))
					dataIter.setDoubleCurrent(0);
			}
		} catch (IOException e) {
		}
		try {
			IArray variance = plot.getVariance().getData();
			IArrayIterator varianceIter = variance.getIterator();
			while(varianceIter.hasNext()){
				if (Double.isNaN(varianceIter.getDoubleNext()))
					varianceIter.setDoubleCurrent(0);
			}
		} catch (IOException e) {
		}
	}

	public static void removeZeroVariance(Plot plot) {
		try {
			IArray variance = plot.getVariance().getData();
			IArrayIterator varianceIter = variance.getIterator();
			while(varianceIter.hasNext()){
				varianceIter.setDoubleCurrent(varianceIter.getDoubleNext() + 1);
			}
		} catch (IOException e) {
		}		
	}
	
}
