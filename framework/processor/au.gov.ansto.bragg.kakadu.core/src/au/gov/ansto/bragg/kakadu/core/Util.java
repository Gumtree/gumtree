/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.exception.PlotMathException;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Data;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotSet;
import au.gov.ansto.bragg.datastructures.core.plot.PlotUtil;
import au.gov.ansto.bragg.kakadu.core.data.DataType;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.processor.Sink;

/**
 * 
 * @author Danil Klimontov (dak)
 */
public class Util {
	
	protected Util(){
		
	}

	public static boolean areEqual(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	/**
	 * Gets data type of the data object by parsing of inner structure and attributes.
	 * 
	 * @param data data object
	 * @return data type
	 * @see DataType
	 */
	public static DataType getDataType(IGroup groupData) {
		if (groupData == null) {
			return DataType.Undefined;
		}
		
		if (groupData instanceof PlotSet){
			PlotSet plotSet = (PlotSet) groupData;
			List<Plot> plotList = plotSet.getPlotList();
			if (plotList.size() > 0){
				switch (plotList.get(0).getDimensionType()) {
				case map:
					return DataType.MapSet;
				case pattern:
					return DataType.PatternSet;
				case mapset:
					return DataType.MapSet;
				case patternset:
					return DataType.PatternSet;
				default:
					break;
				}
			}
				
		}
	
		//Schema data type detection
		final IAttribute signalAttribute = groupData.getAttribute("signal");
		if (signalAttribute != null) {
			final String signalAttributeValue = signalAttribute.getStringValue();
			if (signalAttributeValue.equalsIgnoreCase("calculation")) {
				//this groupData object is calculation type
				return DataType.Calculation;
			}
		}
	
		//Assume groupData object contains plotable data
		final IDataItem signal = ((NcGroup) groupData).findSignal();
		final int[] shape = signal.getShape();
		if (shape.length == 1) {
			return DataType.Pattern;
		} else if (shape.length == 2) {
			return DataType.Map; 
		} else if (shape.length == 3) {
			return DataType.MapSet;
		}
		
		
		return DataType.Undefined; 
	}

	/**
	 * Gets a list of elements to present sets of data.
	 * Used to parse PatternSet as List of Pattern elements
	 * and MapSet as a List of Map elements.
	 * @param plotData data to be parsed.
	 * @return a list of sub elements or empty list if sub elements are not available.
	 * @throws PlotFactoryException 
	 * @throws StructureTypeException 
	 */
	public static List<IGroup> getSubGroups(IGroup plotData) throws StructureTypeException, PlotFactoryException {
		if (plotData != null) {
			return PlotUtil.slice(plotData);
		} else {
			return new ArrayList<IGroup>();
		}
	}

	/**
	 * Gets DataType to be used for child elements.
	 * @param dataType
	 * @return child element data type or the same data type as original.
	 */
	public static DataType getChildDataType(DataType dataType) {
		switch (dataType) {
		case PatternSet:
		case Pattern:
			return DataType.Pattern;
		case MapSet:
		case Map:
			return DataType.Map;
	
		default:
			return DataType.Undefined;
		}
	}
	
	public static DataType getDataType(Sink sink) throws ProcessorChainException {
		StaticDefinition.DataStructureType structureType = null;
		try{
			Object structure = sink.getProperty(StaticDefinition.DATA_STRUCTURE_TYPE);
			structureType = PlotUtil.readDataStructureType(structure);
		}catch (Exception e) {
			structureType = StaticDefinition.DataStructureType.plot;
		}
		switch (structureType) {
		case plot:
			StaticDefinition.DataDimensionType dimensionType = null;
			try{
				Object dimension = sink.getProperty(StaticDefinition.DATA_DIMENSION_TYPE);
				dimensionType = PlotUtil.readDataDimensionType(dimension);
			}catch (Exception e) {
				dimensionType = StaticDefinition.DataDimensionType.undefined;
			}
			return getDataType(dimensionType);

		case calculation:
			return DataType.Calculation;
		}

		return DataType.Undefined;
	}

	/**
	 * @param dimensionType
	 */
	public static DataType getDataType(DataDimensionType dimensionType) {
		switch (dimensionType) {
		case pattern:
			return DataType.Pattern;
		case patternset:
			return DataType.PatternSet;
		case map:
			return DataType.Map;
		case mapset:
			return DataType.MapSet;
		case volume:
			return DataType.Volume;
		case volumeset:
			return DataType.VolumeSet;
		}
		return DataType.Undefined;
	}

	public static void checkData(IGroup groupData) throws StructureTypeException{
		if (groupData instanceof Plot){
			Data data = ((Plot) groupData).findSingal();
			if (data == null)
				throw new StructureTypeException("can not find data");
			IArray dataArray = null;
			try {
				dataArray = data.getData();
			} catch (IOException e) {
				throw new StructureTypeException("can not read data");
			}
			if(!isValid(dataArray))
				throw new StructureTypeException("there is no valid value in the data");
		}
	}

	private static boolean isValid(IArray dataArray) {
		IArrayIterator iterator = dataArray.getIterator();
		
		while(iterator.hasNext()){
			double value = iterator.getDoubleNext();
			if (!Double.isNaN(value) && !Double.isInfinite(value))
				return true;
		}
		return false;
	}
}
