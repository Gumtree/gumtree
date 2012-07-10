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
import java.util.Iterator;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.ISliceIterator;
import org.gumtree.data.utils.Utilities;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.Util;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.common.Log;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.internal.NcPlot;
import au.gov.ansto.bragg.datastructures.core.plot.internal.NcPlotSet;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;

/**
 * @author nxi
 * Created on 05/03/2008
 */
public class PlotFactory {

	public static final int MIN_YSIZE = 1;
	/**
	 * Create a PlotSet type of data that is in the Group container. 
	 * @param parent Group object that will be the parent group of the new one
	 * @param shortName in String type
	 * @return Group object, which actually is a PlotSet object
	 * Created on 19/03/2008
	 */
	public static PlotSet createPlotSet(IGroup parent, String shortName){
		return new NcPlotSet(parent, shortName);
	};

	/**
	 * Create a Plot type of data that is in the Group container. A DataDimensionType
	 * need to be provided when calling.
	 * @param plotSet Group object
	 * @param shortName in String type
	 * @param dimensionType DataDimensionType enum type
	 * @return Group object
	 * Created on 19/03/2008
	 */
	public static Plot createPlot(IGroup plotSet, String shortName, 
			DataDimensionType dimensionType){
		Plot plot = new NcPlot(plotSet, shortName, dimensionType);
//		plotSet.addSubgroup(plot);
		return plot;
	}

	/**
	 * Create a plot with provided short name and dimension type. The new plot will use a 
	 * new data set as the holder.
	 * @param shortName in String type
	 * @param dimensionType DataDimensionType enum type 
	 * @return GDM Group object
	 * Created on 17/12/2008
	 */
	public static Plot createPlot(String shortName, DataDimensionType dimensionType){
		Plot plot = null;
		try {
			plot = createPlot(Factory.createEmptyDatasetInstance().getRootGroup(), shortName, dimensionType);
		} catch (IOException e) {
		}
		return plot;
	}
	
	/**
	 * Add a signal data to the Plot group. Here the signal is contained in a GDM
	 * Array type. With the name, title and units provided, it will create a new
	 * DataItem in the Plot group, which follows the Data data structure type 
	 * schema.
	 * @param group in Group type, the parent of the DataItem to be created
	 * @param shortName in String type
	 * @param array GDM Array type
	 * @param title in String type
	 * @param units in String type
	 * @throws PlotFactoryException
	 * Created on 20/03/2008
	 */
	public static void addDataToPlot(IGroup group, String shortName, 
			IArray array, String title, String units) 
	throws PlotFactoryException{
		if (Util.matchStructureType(group, StaticDefinition.DataStructureType.plot)){
			try {
				((Plot) group).addData(shortName, array, title, units);
			} catch (InvalidArrayTypeException e) {
				throw new PlotFactoryException(e.getClass().getSimpleName() + 
						" - " + e.getMessage());
			}
		}else throw new PlotFactoryException("expecting Plot structure type");
	}

	/**
	 * Add a variance data to the Plot group. Here the variance is contained in a GDM
	 * Array type. It will create a new DataItem in the Plot group, which follows the 
	 * Variance data structure type schema.
	 * @param group in Group type, the parent of the DataItem to be created
	 * @param shortName in String type
	 * @param varianceArray GDM Array type
	 * @throws PlotFactoryException
	 * Created on 18/04/2008
	 */
	public static void addDataVarianceToPlot(IGroup group, String shortName, 
			IArray varianceArray) 
	throws PlotFactoryException{
		if (Util.matchStructureType(group, StaticDefinition.DataStructureType.plot)){
			try {
				((Plot) group).addDataVariance(shortName, varianceArray);
			} catch (InvalidArrayTypeException e) {
				throw new PlotFactoryException(e.getClass().getSimpleName() + 
						" - " + e.getMessage());
			}
		}else throw new PlotFactoryException("expecting Plot structure type");
	}

	/**
	 * Add a signal data to the Plot group. Here the signal is contained in a GDM
	 * Array type. With the name, title and units provided, it will create a new
	 * DataItem in the Plot group, which follows the Data data structure type 
	 * schema.
	 * @param group in Group type, the parent of the DataItem to be created
	 * @param shortName in String type
	 * @param array GDM Array type
	 * @param title in String type
	 * @param units in String type
	 * @param varianceArray in GDM Array type, the variance of the data
	 * @throws PlotFactoryException
	 * Created on 20/03/2008
	 */
	public static void addDataToPlot(IGroup group, String shortName, 
			IArray array, String title, String units, IArray varianceArray) 
	throws PlotFactoryException{
		if (Util.matchStructureType(group, StaticDefinition.DataStructureType.plot)){
			try {
				((Plot) group).addData(shortName, array, title, units, varianceArray);
			} catch (InvalidArrayTypeException e) {
				throw new PlotFactoryException(e.getClass().getSimpleName() + 
						" - " + e.getMessage());
			}
		}else throw new PlotFactoryException("expecting Plot structure type");
	}

	/**
	 * Add calculation result data to a plot. The plot will carry on the calculation result as
	 * DataItem objects. 
	 * @param group the GDM Group object that will carry the calculation data
	 * @param shortName in String type
	 * @param array GDM Array object, which hold the data storage
	 * @param title in String type, the UI title of the data
	 * @param units in String type
	 * @param varianceArray GDM Array object
	 * @throws PlotFactoryException
	 * Created on 17/12/2008
	 */
	public static void addCalculationDataToPlot(IGroup group, String shortName, 
			IArray array, String title, String units, IArray varianceArray) 
	throws PlotFactoryException{
		if (Util.matchStructureType(group, StaticDefinition.DataStructureType.plot)){
			try {
				((Plot) group).addCalculationData(shortName, array, title, units, varianceArray);
			} catch (InvalidArrayTypeException e) {
				throw new PlotFactoryException(e.getClass().getSimpleName() + 
						" - " + e.getMessage());
			}
		}else throw new PlotFactoryException("expecting Plot structure type");
	}
	
	/**
	 * Add an axis information to the Plot group. Here the axis information is contained in
	 * a GDM Array object. With the name, title units and dimension information provided, 
	 * it will create a DataItem in the Plot group, which follows the Axis data structure type
	 * schema.
	 * @param group in Group type, the parent of the DataItem to be created
	 * @param shortName in String type
	 * @param array GDM Array type
	 * @param title in String type
	 * @param units in String type
	 * @param dimension int type
	 * @throws PlotFactoryException
	 * Created on 20/03/2008
	 */
	public static void addAxisToPlot(IGroup group, String shortName, 
			IArray array, String title, String units, int dimension) 
	throws PlotFactoryException{
		if (Util.matchStructureType(group, StaticDefinition.DataStructureType.plot)){
			try {
				((Plot) group).addAxis(shortName, array, title, units, dimension);
			} catch (InvalidArrayTypeException e) {
				e.printStackTrace();
				throw new PlotFactoryException(e.getClass().getSimpleName() + 
						" - " + e.getMessage());
			}
		}else throw new PlotFactoryException("expecting Plot structure type");

	}

	/**
	 * Add an axis information to the Plot group. Here the axis information is contained in
	 * a GDM Array object. With the name, title units and dimension information provided, 
	 * it will create a DataItem in the Plot group, which follows the Axis data structure type
	 * schema.
	 * @param group in Group type, the parent of the DataItem to be created
	 * @param shortName in String type
	 * @param array GDM Array type
	 * @param title in String type
	 * @param units in String type
	 * @param dimension int type
	 * @param axisVariance in GDM Array type, variance of the axis data
	 * @throws PlotFactoryException
	 * Created on 20/03/2008
	 */
	public static void addAxisToPlot(IGroup group, String shortName, 
			IArray array, String title, String units, int dimension, IArray axisVariance) 
	throws PlotFactoryException{
		if (Util.matchStructureType(group, StaticDefinition.DataStructureType.plot)){
			try {
				((Plot) group).addAxis(shortName, array, title, units, dimension, axisVariance);
			} catch (InvalidArrayTypeException e) {
				e.printStackTrace();
				throw new PlotFactoryException(e.getClass().getSimpleName() + 
						" - " + e.getMessage());
			}
		}else throw new PlotFactoryException("expecting Plot structure type");
	}

	/**
	 * This method will add an existing Axis object to the Plot group. By providing
	 * the dimension information, it will be added to represent the specific dimension 
	 * of the plot Data.
	 * @param group in Group type
	 * @param axis in Axis type
	 * @param dimension int type
	 * @throws InvalidArrayTypeException
	 * @throws PlotFactoryException
	 * @throws IOException
	 * Created on 20/03/2008
	 */
	public static void addAxisToPlot(IGroup group, IDataItem axis, int dimension) 
	throws InvalidArrayTypeException, PlotFactoryException, IOException{
		if (!(axis instanceof Axis)) 
			throw new PlotFactoryException("expecting Axis structure type");
		if (Util.matchStructureType(group, StaticDefinition.DataStructureType.plot)){
			((Plot) group).addAxis((Axis) axis, dimension);
		}else throw new PlotFactoryException("expecting Plot structure type");
	}

	/**
	 * @param parent
	 * @param group
	 * @param shortName
	 * @param dimensionType
	 * @return
	 * @throws PlotFactoryException
	 * Created on 05/12/2008
	 */
	public static Plot copyToPlot(IGroup parent, IGroup group, String shortName, DataDimensionType dimensionType) 
	throws PlotFactoryException{
		if (dimensionType == DataDimensionType.mapset)
			return copyToMapSet(group, shortName);
		Plot plot = createPlot(parent, shortName, dimensionType);
		((NcGroup) plot).addLog(Log.COPYING_LOG_PREFIX + " " + parent.getLocation());
//		DataItem item = group.findSignal();
		IDataItem item = null;
		try{
			item = NexusUtils.getNexusSignal(group);
		}catch (StructureTypeException e) {
			throw new PlotFactoryException(e);
		}
		if (item == null) throw new PlotFactoryException("can not copy to Plot structure");
		IDataItem variance = findVariance(group, item);
		if (variance == null)
			variance = item;
		List<IDataItem> axes = null;
		try {
			axes = NexusUtils.getNexusAxis(group);
		} catch (StructureTypeException e1) {
			throw new PlotFactoryException(e1);
		}
		try {
			addDataToPlot(plot, item.getShortName(), Utilities.copyToDoubleArray(item.getData()), 
					item.getShortName(), item.getUnitsString(), Utilities.copyToDoubleArray(variance.getData()));
		} catch (IOException e) {
//			e.printStackTrace();
			throw new PlotFactoryException("failed to read data from source, " + e.getMessage());
		}
		if (axes != null && axes.size() > 0){
			int dimension = 0;
			for (Iterator<?> iterator = axes.iterator(); iterator.hasNext();) {
				IDataItem axis = (IDataItem) iterator.next();
				try {
					addAxisToPlot(plot, axis.getShortName(), Utilities.copyToDoubleArray(axis.getData()), 
							axis.getShortName(), axis.getUnitsString(), dimension);
				} catch (IOException e) {
//					e.printStackTrace();
					throw new PlotFactoryException("failed to read axis from source, " + e.getMessage());
				}
				dimension ++;
			}
		}else{
//			int dimension = item.getRank();
			int[] shape = item.getShape();
			for (int i = 0; i < shape.length; i++) {
				double[] axisStorage = new double[shape[i]];
				for (int j = 0; j < axisStorage.length; j++) {
					axisStorage[j] = j;
				}
				IArray axis = Factory.createArray(Double.TYPE, new int[]{shape[i]}, axisStorage);
				try {
					((Plot) plot).addAxis("axis" + i, axis, "Axis " + i , "bin", i);
				} catch (InvalidArrayTypeException e) {
					throw new PlotFactoryException(e);
				}
			}
			
		}
//		group.addSubgroup(plot);
		return plot;
	}
	
	/**
	 * @param parent
	 * @param group
	 * @param shortName
	 * @param dimensionType
	 * @return
	 * @throws PlotFactoryException
	 * Created pvhathaway 15/07/2009
	 */
	public static Plot copyToPlotNoRead(
			IGroup parent, 
			IGroup group, 
			String shortName, 
			DataDimensionType dimensionType) 
	throws PlotFactoryException {
		Plot plot = createPlot(parent, shortName, dimensionType);
		IDataItem item = group.getDataItemWithAttribute(Util.NEXUS_SIGNAL_ATTRIBUTE_NAME,"1");
		if (null==item) {
			throw new PlotFactoryException("Cannot locate data item for Plot structure");
		}
		if (null!=item) {
			IDataItem variance = findVariance(group,item);
			if (null==variance) { variance = item; }			
			plot.addDataItem(item);
			plot.addDataItem(variance);
			
			List<IDataItem> axes = null;
			try {
				axes = NexusUtils.getNexusAxis(group);
				if (null!=axes) {
					if (0<axes.size()) {
						int dimension = 0;
						for (Iterator<?> iterator = axes.iterator(); iterator.hasNext();) {
							IDataItem axis = (IDataItem) iterator.next();
							addAxisToPlot(plot, 
									axis.getShortName(), 
									axis.getData(),
									axis.getShortName(), 
									axis.getUnitsString(), 
									dimension);
							dimension++;
						}
					} else {
						int[] shape = item.getShape();
						for (int i = 0; i < shape.length; i++) {
							double[] axisStorage = new double[shape[i]];
							for (int j = 0; j < axisStorage.length; j++) {
								axisStorage[j] = j;
							}
							IArray axis = Factory.createArray(
									Double.TYPE, 
									new int[]{shape[i]}, 
									axisStorage);
							addAxisToPlot(plot, 
									"axis"+i, 
									axis,
									"Axis"+i,
									"bin",
									i);
						}
					}
				}
			} catch (StructureTypeException e1) {
				throw new PlotFactoryException(e1);
			} catch (IOException e) {
				throw new PlotFactoryException("failed to read axis from source, " + e.getMessage());
			}
		}
		return plot;
	}

	private static IDataItem findVariance(IGroup group, IDataItem data) {
		IAttribute varianceAttribute = data.getAttribute(StaticDefinition.DATA_VARIANCE_REFERENCE_NAME);
		if (varianceAttribute == null)
			return null;
		return group.findDataItem(varianceAttribute.getStringValue());
	}

	/**
	 * Convert a undefined type of Group object into a Plot type of data structure.
	 * The method will return a new Group object, which will follow the Plot data
	 * structure type.
	 * @param group GDM Group object
	 * @param dimensionType int type
	 * @return new Group object
	 * @throws PlotFactoryException
	 * Created on 20/03/2008
	 */
	public static Plot copyToPlot(IGroup group, String shortName, DataDimensionType dimensionType) 
	throws PlotFactoryException{
		return copyToPlot(group, group, shortName, dimensionType);
	}

	/*
	 * This method will be called by copyToPlot internally if the data dimension type of the
	 * target plot is MapSet.
	 */
	private static Plot copyToMapSet(IGroup group, String shortName)
	throws PlotFactoryException{
		Plot plot = createPlot(group, shortName, DataDimensionType.mapset);
		IDataItem item = null;
		try{
			item = NexusUtils.getNexusSignal(group);
		}catch (StructureTypeException e) {
			throw new PlotFactoryException(e);
		}
		if (item == null) throw new PlotFactoryException("can not copy to Plot structure");
		int[] oldShape = item.getShape();
		int rank = item.getRank();
		if (rank < 3)
			throw new PlotFactoryException("can not copy to mapset data dimension type, no enough dimension");
		IArray newArray = null;
		if (rank == 3){
			try {
				newArray = Utilities.copyToDoubleArray(item.getData());
			} catch (IOException e) {
				throw new PlotFactoryException("failed to read data from source, " + e.getMessage());
			}
		}
		if (rank > 3){
			int[] newShape = new int[3];
			int length0 = 1;
			for (int i = 0; i < rank - 2; i++) 
				length0 *= oldShape[i];
			newShape[0] = length0;
			newShape[1] = oldShape[rank -2];
			newShape[2] = oldShape[rank -1];
			try {
				newArray = item.getData().copy().getArrayUtils().reshape(newShape).getArray();
			} catch (Exception e) {
				throw new PlotFactoryException("failed to read data from source, " + e.getMessage());
			}
		}
		IDataItem variance = findVariance(group, item);
		IArray varianceArray = null;
//		List<DataItem> axes = group.findAxes();
		List<IDataItem> axes = null;
		try{
			if (Util.getDataStructureType(group) == DataStructureType.nexusData){
				axes = NexusUtils.getNexusAxis(group);
			}else
				axes = ((NcGroup) group).findAxes();
			if (variance != null)
				varianceArray = variance.getData();
			else
				varianceArray = newArray;
			addDataToPlot(plot, item.getShortName(), newArray, item.getShortName(), item.getUnitsString(), 
					varianceArray);
		}catch (Exception e) {
		}
		if (axes.size() <=3){
			int dimension = 0;
			for (Iterator<?> iterator = axes.iterator(); iterator.hasNext();) {
				IDataItem axis = (IDataItem) iterator.next();
				try {
					addAxisToPlot(plot, axis.getShortName(), Utilities.copyToDoubleArray(axis.getData()), 
							axis.getShortName(), axis.getUnitsString(), dimension);
				} catch (IOException e) {
//					e.printStackTrace();
					throw new PlotFactoryException("failed to read axis from source, " + e.getMessage());
				}
				dimension ++;
			}
		}
		else{
			IDataItem axis0 = null;
			if (axes.size() >= 3){
//				for (int i = 0; i < axes.size() - 2; i ++)
//					if (axes.get(i).getSize() == newArray.getShape()[0])
//						axis0 = axes.get(i);
				for (int j = 0; j < oldShape.length - 2; j++) {
					if (oldShape[j] > 1)
						axis0 = axes.get(j);
				}
			}
			if (axis0 != null){
				try{
					addAxisToPlot(plot, axis0.getShortName(), Utilities.copyToDoubleArray(axis0.getData()),
							axis0.getShortName(), axis0.getUnitsString(), 0);
				} catch (Exception e) {
					throw new PlotFactoryException("failed to read axis from source, " + e.getMessage());
				}
			}else{
				int[] axis0Storage = new int[newArray.getShape()[0]];
				for (int i = 0; i < axis0Storage.length; i++) {
					axis0Storage[i] = i;
				}
				addAxisToPlot(plot, "set", Factory.createArray(axis0Storage), 
						"set index", "item", 0);
			}
			try {
				IDataItem axis1 = (IDataItem) axes.get(axes.size() - 2);
				addAxisToPlot(plot, axis1.getShortName(), Utilities.copyToDoubleArray(axis1.getData()),
						axis1.getShortName(), axis1.getUnitsString(), 1);
				IDataItem axis2 = (IDataItem) axes.get(axes.size() - 1);
				addAxisToPlot(plot, axis2.getShortName(), Utilities.copyToDoubleArray(axis2.getData()),
						axis2.getShortName(), axis2.getUnitsString(), 2);
			} catch (Exception e) {
				throw new PlotFactoryException("failed to read axis from source, " + e.getMessage());
			}
		}
		//		group.addSubgroup(plot);
		return plot;
	}
	//	public static Group makePlotable(Group group, String dimensionType) {
	//	group.addStringAttribute(StaticDefination.DATA_STRUCTURE_TYPE, 
	//	StaticDefination.DataStructureType.raw.name());
	//	}

	public static Plot copyTo2DPlot(Plot plot1D) 
	throws ShapeNotMatchException, InvalidRangeException, IOException, 
	InvalidArrayTypeException, PlotFactoryException{
		Plot newPlot = createPlot(plot1D.getShortName(), DataDimensionType.map);
		Data data1D = plot1D.findSingal();
		List<Axis> axes = plot1D.getAxisList();
		if (data1D.getRank() == 2){
			int[] shape = data1D.getShape();
			int[] newShape = new int[2];
			IArray newDataArray = null;
			IArray newAxis0 = null;
			if (shape[0] < MIN_YSIZE){
				int expandFactor = (int) Math.ceil(((double) MIN_YSIZE) / shape[0]);
				newShape[0] = shape[0] * expandFactor;
				newShape[1] = shape[1];
				newDataArray = Factory.createArray(double.class, newShape);
				ISliceIterator sourceIterator = data1D.getData().getSliceIterator(1);
				ISliceIterator newDataIterator = newDataArray.getSliceIterator(1);
				while(sourceIterator.hasNext()){
					IArray sliceArray = sourceIterator.getArrayNext();
					for (int i = 0; i < expandFactor; i ++){
						IArray newSlice = newDataIterator.getArrayNext();
						sliceArray.getArrayUtils().copyTo(newSlice);
					}
				}
				if (axes.size() == 2){
					Axis axis0 = axes.get(0);
					boolean isSingleLine = false;
					if (shape[0] == 1) {
						isSingleLine = true;
					}
					if (isSingleLine) {
						newAxis0 = Factory.createArray(double.class, new int[]{expandFactor});
						IArrayIterator axisIterator = axis0.getData().getIterator();
						IArrayIterator newAxisIterator = newAxis0.getIterator();
						while(axisIterator.hasNext()){
							double axisValue = axisIterator.getDoubleNext();
							for (int i = 0; i < expandFactor / 2; i ++){
								newAxisIterator.next().setDoubleCurrent(axisValue);
							}
						}
					} else {
						newAxis0 = Factory.createArray(double.class, new int[]{(int) axis0.getSize() * expandFactor});
						IArrayIterator axisIterator = axis0.getData().getIterator();
						IArrayIterator newAxisIterator = newAxis0.getIterator();
						while(axisIterator.hasNext()){
							double axisValue = axisIterator.getDoubleNext();
							for (int i = 0; i < expandFactor; i ++){
								newAxisIterator.next().setDoubleCurrent(axisValue);
							}
						}
					}
				}else{
					newAxis0 = Factory.createArray(double.class, new int[]{shape[0] * expandFactor});
					IArrayIterator newAxisIterator = newAxis0.getIterator();
					int count = 0;
					double index = 0;
					while(newAxisIterator.hasNext()){
						if (count < expandFactor)
							count ++;
						else{
							index ++;
							count = 0;
						}
						newAxisIterator.next().setDoubleCurrent(index);
					}
				}
			}else{
				newDataArray = data1D.getData();
				if (axes.size() == 2){
					newAxis0 = axes.get(0).getData();
				}else{
					newAxis0 = Factory.createArray(double.class, new int[]{shape[0]});
					IArrayIterator axisIterator = newAxis0.getIterator();
					double index = 0;
					while(axisIterator.hasNext()){
						axisIterator.next().setDoubleCurrent(index ++);
					}
				}
			}
			IArray varianceArray = null;
			try {
				varianceArray = data1D.getVariance().getData();
			} catch (Exception e) {
			}
			
			newPlot.addData(data1D.getShortName(), newDataArray, data1D.getTitle(), data1D.getUnitsString(), 
					varianceArray);
			if (axes.size() == 2){
				Axis axis0 = axes.get(0);
				Axis axis1 = axes.get(1);
				newPlot.addAxis(axis0.getShortName(), newAxis0, axis0.getTitle(), axis0.getUnitsString(), 0);
				newPlot.addAxis(axis1.getShortName(), axis1.getData(), axis1.getTitle(), axis1.getUnitsString(), 
						1);
			}else if (axes.size() == 1){
				Axis axis1 = axes.get(0);
				newPlot.addAxis("index", newAxis0, "Index", "", 0);
				newPlot.addAxis(axis1.getShortName(), axis1.getData(), axis1.getTitle(), axis1.getUnitsString(), 
						1);
			}
			
		}
		return newPlot;
	}
	
}
