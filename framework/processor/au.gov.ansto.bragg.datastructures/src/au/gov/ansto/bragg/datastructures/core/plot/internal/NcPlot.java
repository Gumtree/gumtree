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
package au.gov.ansto.bragg.datastructures.core.plot.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gumtree.data.DataType;
import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcDataset;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.math.EData;
import org.gumtree.data.math.EMath;
import org.gumtree.data.utils.Utilities;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.common.Log;
import au.gov.ansto.bragg.datastructures.core.common.internal.NcLog;
import au.gov.ansto.bragg.datastructures.core.exception.IndexOutOfBoundException;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.exception.PlotMathException;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Data;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.core.plot.PlotIndex;
import au.gov.ansto.bragg.datastructures.core.plot.PlotIterator;
import au.gov.ansto.bragg.datastructures.core.plot.PlotUtil;
import au.gov.ansto.bragg.datastructures.core.plot.Point;
import au.gov.ansto.bragg.datastructures.core.plot.Variance;

/**
 * @author nxi
 * Created on 06/03/2008
 */
public class NcPlot extends NcGroup implements Plot {

	public NcPlot(IGroup parent, String shortName, StaticDefinition.DataDimensionType dimensionType) {
		super((NcDataset) parent.getDataset(), (NcGroup) parent, shortName, true);
		addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
				StaticDefinition.DataStructureType.plot.name());
		//		DataDimensionType dimensionTypeValue = DataDimensionType.valueOf(dimensionType);
		//		if (dimensionTypeValue == null) dimensionTypeValue = DataDimensionType.undefined;
		addStringAttribute(StaticDefinition.DATA_DIMENSION_TYPE, dimensionType.name());
		addLog("Created");
		copyProcessingLog(parent);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Plot#addComment(java.lang.String)
	 */
	public void addComment(String comment, String userName) {
		IAttribute commentAttribute = getAttribute("comments");
		if (commentAttribute == null)
			commentAttribute = Factory.createAttribute("comments", comment + " -- by " + userName);
		else commentAttribute.setStringValue(commentAttribute.getStringValue() + "\n\n" 
				+ comment + " -- by " + userName);
		addLog("Commented");
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Plot#addDescription(java.lang.String)
	 */
	public void addDescription(String description) {
		addStringAttribute("description", description);
		addLog("Add description");
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Plot#addLog(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addLog(String log, String userInfo) {
		Log ncLog = (NcLog) getDataItemWithAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
				StaticDefinition.DataStructureType.log.name());
		String userInfoString = "";
//		if (userInfo != null && userInfo.length() > 0)
//			userInfoString = " - " + userInfo;
		if (ncLog != null) ncLog.appendLog(log + userInfoString, false);
		else{
			Log newLog = null;
			try {
				newLog = new NcLog(this, "log", log + userInfoString);
			} catch (InvalidArrayTypeException e) {
				//				e.printStackTrace();
			}
			addDataItem(newLog);
		}
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Plot#addLog(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addLog(String log) {
		addLog(log, "default user");
	}

	public void addProcessingLog(String log){
		addLog(Log.PROCESSING_LOG_PREFIX + " " + log, "default user");
	}
	
	private void copyProcessingLog(IGroup parent){
		if (parent instanceof Plot){
			String processingLog = ((Plot) parent).getProcessingLog();
			addLog(processingLog);
		}
	}
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Plot#findSignalArray()
	 */
	public IArray findSignalArray() throws IOException {
		return findSignal().getData();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Plot#getAxisArrayList()
	 */
	public List<IArray> getAxisArrayList() throws SignalNotAvailableException {
		List<IArray> arrayList = new ArrayList<IArray>();
		List<IDataItem> axesDataItems = findAxes();
		if (axesDataItems.size() > 0){
			for (Iterator<?> iterator = axesDataItems.iterator(); iterator
			.hasNext();) {
				try {
					IArray axesArray = ((IDataItem) iterator.next()).getData();
					arrayList.add(axesArray);
				} catch (IOException e) {
					throw new SignalNotAvailableException("axes array is not reachable " + 
							e.getMessage());
				}
			}
		}
		return arrayList;	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Plot#getAxisList()
	 */
	public List<Axis> getAxisList() {
		Data signal = findSingal();
		if (signal == null) 
			return null;
		IAttribute axesAttribute = signal.getAttribute("axes");
		if (axesAttribute == null) 
			return null;
		String[] axesNames = axesAttribute.getStringValue().split(":");
		List<Axis> axes = new ArrayList<Axis>();

//		ListIterator iter = axes.listIterator();
//
//		Axis axis;
//
//		while (iter.hasNext()) {
//			axis = (Axis) 
//		}
		for (int i = 0; i < axesNames.length; i ++){
			Axis axis = (Axis) findVariable(axesNames[i]);
			if (axis != null) axes.add(axis);
		}
		return axes;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Plot#getAxisVarianceList()
	 */
	public List<Variance> getAxisVarianceList() {
		return null;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Plot#getDescription()
	 */
	public String getDescription() {
		IAttribute attribute = getAttribute("description");
		if (attribute != null) return attribute.getStringValue();
		return null;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Plot#getLogDataItem()
	 */
	public NcLog getLogDataItem() {
		return (NcLog) getDataItemWithAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
				StaticDefinition.DataStructureType.log.name());
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Plot#getLogString()
	 */
	public String getLogString() {
		Log log = (Log) getLogDataItem();
		if (log != null) {
			IArray logContent = null;
			try {
				logContent = log.getData();
			} catch (IOException e) {
			}
			if (logContent != null) return logContent.toString();
		}
		return null;
	}

	public String getProcessingLog(){
		String logString = getLogString();
		String[] sentences = logString.split("\n");
		String result = "";
		boolean isBegining = true;
		for (int i = 0; i < sentences.length; i++) {
			String sentence = sentences[i];
			if (sentence.contains(Log.PROCESSING_LOG_PREFIX)){
				if (isBegining){
					result += sentence;
					isBegining = false;
				}else
					result += "\n" + sentence;
			}
		}
		return result;
	}
	
	public String getCopyingLog(){
		String logString = getLogString();
		String[] sentences = logString.split("\n");
		String result = "";
		boolean isBegining = true;
		for (int i = 0; i < sentences.length; i++) {
			String sentence = sentences[i];
			if (sentence.contains(Log.COPYING_LOG_PREFIX)){
				if (isBegining){
					result += sentence;
					isBegining = false;
				}else
					result += "\n" + sentence;
			}
		}
		return result;
	}
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Plot#getUserComments()
	 */
	public String getUserComments() {
		IAttribute attribute = getAttribute("comments");
		if (attribute != null) return attribute.getStringValue();
		return null;
	}

	public Data findSingal() {
		//      after fixing duplicate name of GroupData method
		return (Data) super.findSignal();
	}

//	public Data findSignal() {
//		return (Data) super.findSignal();
//	}

	public void addData(String shortName, IArray array, String title,
			String units) throws InvalidArrayTypeException {
		Data data = new NcData(this, shortName, array, title, units);
		addDataItem(data);
		addLog("data added");
	}

	public void addAxis(String shortName, IArray array, String title,
			String units, int dimension)
	throws InvalidArrayTypeException, PlotFactoryException {
		if (isDataExists()){
			Axis axis = new NcAxis(this, shortName, array, title, units, dimension);
			addDataItem(axis);
			findSingal().addAxis(axis, dimension);
			addLog("axis added");			
		}else throw new PlotFactoryException("no data in the plot, can not add axis");
	}

	private boolean isDataExists() {
		if (findSignal() != null) return true;
		else return false;
	}

	public void addAxis(Axis axis, int dimension) 
	throws InvalidArrayTypeException, PlotFactoryException, IOException {
		addAxis(axis.getShortName(), axis.getData(), axis.getTitle(), axis.getUnitsString(), dimension);
	}

	public Axis getAxis(int dimension) {
		List<Axis> axes = getAxisList();
		Axis axis = null;
		try{
			axis = axes.get(dimension);
		}catch (Exception e) {
		}
		return axis;
	}

	public long getCreationTimeStamp() {
		long timeStamp = 0;
		try {
			Log log = getLogDataItem();
			String stamp = log.getCreationTimeStamp();
			timeStamp = Long.valueOf(stamp);
		} catch (Exception e) {
			return 0;
		}
		return timeStamp;
	}

	public long getLastModificationTimeStamp() {
		long timeStamp = 0;
		try {
			Log log = getLogDataItem();
			String stamp = log.getLastModificationTimeStamp();
			timeStamp = Long.valueOf(stamp);
		} catch (Exception e) {
			return 0;
		}
		return timeStamp;
	}

	public void addDataVariance(String shortName, IArray varianceArray)
	throws InvalidArrayTypeException {
		Data data = findSingal();
		data.addVariance(varianceArray, shortName);
		addLog("variance added");		
	}

	public void addDataVariance(IArray varianceArray)
	throws InvalidArrayTypeException {
		Data data = findSingal();
		data.addVariance(varianceArray);
		addLog("variance added");		
	}

	public Variance getVariance() {
		Data data = findSingal();
		if (data == null) return null;
		return data.getVariance();
	}

	public void addData(String shortName, IArray array, String title,
			String units, IArray varianceArray) throws InvalidArrayTypeException {
		Data data = new NcData(this, shortName, array, title, units, varianceArray);
		addDataItem(data);
		addLog("data added");		
	}

	public void addAxis(String shortName, IArray array, String title,
			String units, int dimension, IArray axisArray)
	throws InvalidArrayTypeException, PlotFactoryException {
		if (isDataExists()){
			Axis axis = new NcAxis(this, shortName, array, title, units, dimension, axisArray);
			addDataItem(axis);
			findSingal().addAxis(axis, dimension);
			addLog("axis added");			
		}else throw new PlotFactoryException("no data in the plot, can not add axis");
	}

	public void removeAxis(int dimension){
		List<Axis> axes = getAxisList();
		if (axes.size() > dimension){
			Axis axis = axes.get(dimension);
			Data data = findSingal();
			if (data != null){
				data.removeAxis(axis);
			}
		}
	}

	public void reduce() throws PlotFactoryException {
		Data data = findSingal();
		if (data != null){
			data.reduce();
		}
//		DataDimensionType oldType = PlotUtil.getDimensionType(this);
		DataDimensionType newType;
		int rank = data.getRank();
		newType = getReducedType(rank);
		IAttribute dimensionType = getAttribute(StaticDefinition.DATA_DIMENSION_TYPE);
		dimensionType.setStringValue(newType.name());
		addLog("Dimensionality reduced");
	}

	public void reduceTo(int rank) throws PlotFactoryException{
		if (getRank() <= rank)
			return;
		Data data = findSingal();
		if (data != null){
			data.reduceTo(rank);
		}
		DataDimensionType newType;
		newType = getReducedType(rank);
		IAttribute dimensionType = getAttribute(StaticDefinition.DATA_DIMENSION_TYPE);
		dimensionType.setStringValue(newType.name());
		addLog("Dimensionality reduced");
	}
	
	private DataDimensionType getReducedType(int newRank){
		DataDimensionType oldType = PlotUtil.getDimensionType(this);
		DataDimensionType newType;
		switch (oldType) {
		case volumeset:
			if (newRank == 1)
				newType = DataDimensionType.pattern;
			else if (newRank == 2) 
				newType = DataDimensionType.map;
			else if (newRank == 3)
				newType = DataDimensionType.volume;
			else 
				newType = DataDimensionType.volumeset;
			break;
		case volume:
			if (newRank == 1)
				newType = DataDimensionType.pattern;
			else if (newRank == 2) 
				newType = DataDimensionType.map;
			else
				newType = DataDimensionType.volume;
			break;
		case mapset:
			if (newRank == 1)
				newType = DataDimensionType.pattern;
			else if (newRank == 2) 
				newType = DataDimensionType.map;
			else
				newType = DataDimensionType.mapset;
			break;
		case map:
			if (newRank == 1)
				newType = DataDimensionType.pattern;
			else 
				newType = DataDimensionType.map;
			break;
		case patternset:
			if (newRank == 1)
				newType = DataDimensionType.pattern;
			else 
				newType = DataDimensionType.patternset;
			break;
		default:
			newType = DataDimensionType.undefined;
		break;
		}
		return newType;
	}

	public Plot add(Plot plot) throws StructureTypeException {
		Data data1 = findSingal();
		Variance variance1 = getVariance();
		Data data2 = plot.findSingal();
		Variance variance2 = plot.getVariance();
		IArray varianceArray1 = null;
		IArray varianceArray2 = null;
		try {
			varianceArray1 = variance1.getData();
			varianceArray2 = variance2.getData();
		} catch (Exception e) {
		}
//		EData<Array> sum = null;
		try {
			EMath.add(data1.getData(), data2.getData(), varianceArray1, varianceArray2);
		} catch (Exception e) {
			throw new StructureTypeException(e);
		} 
		return this;
	}

	public Plot toAdd(Plot plot) throws StructureTypeException {
		Plot newPlot = (Plot) PlotFactory.createPlot(getParentGroup(), "combined_group", getDimensionType());
		Data data1 = findSingal();
		Variance variance1 = getVariance();
		Data data2 = plot.findSingal();
		Variance variance2 = plot.getVariance();
		IArray varianceArray1 = null;
		IArray varianceArray2 = null;
		try {
			varianceArray1 = variance1.getData();
			varianceArray2 = variance2.getData();
		} catch (Exception e) {
		}
		EData<IArray> sum = null;
		try {
			sum = EMath.toAdd(data1.getData(), data2.getData(), varianceArray1, varianceArray2);
			newPlot.addData("combined_data", sum.getData(), "Combined Data", data1.getUnitsString(), sum.getVariance());
			for (Axis axis : getAxisList()){
				newPlot.addAxis(axis);
			}
		} catch (Exception e) {
			throw new StructureTypeException(e);
		} 
		return newPlot;
	}

	public Plot copyToDouble() throws StructureTypeException {
		Data data = findSingal();
		if (data.getType() == Double.TYPE)
			return this;
		Plot doublePlot = (Plot) PlotFactory.createPlot(getShortName(), getDimensionType());
		try {
			doublePlot.addData(data.getShortName(), Utilities.copyToDoubleArray(data.getData()), 
					data.getTitle(), data.getUnitsString(), Utilities.copyToDoubleArray(
							getVariance().getData()));
		} catch (Exception e) {
			throw new StructureTypeException(e);
		} 
		int dimension = 0;
		for (Axis axis : getAxisList())
			try {
				doublePlot.addAxis(axis, dimension);
			} catch (Exception e) {
				throw new StructureTypeException(e);
			} 
			return doublePlot;
	}

	public Plot eltMultiply(Plot plot) throws PlotMathException {
		try{
			IArray dataArray1 = findSignalArray();
			IArray varianceArray1 = getVariance().getData();
			IArray dataArray2 = plot.findSignalArray();
			IArray varianceArray2 = plot.getVariance().getData();
			EMath.eltMultiply(dataArray1, varianceArray1, dataArray2, varianceArray2);
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return this;
	}

	public Plot toEltMultiply(Plot plot) throws PlotMathException {
		Plot newPlot = (Plot) PlotFactory.createPlot(getShortName() + "_eltMultiply", getDimensionType());
		try{
			IArray dataArray1 = findSignalArray();
			IArray varianceArray1 = getVariance().getData();
			IArray dataArray2 = plot.findSignalArray();
			IArray varianceArray2 = plot.getVariance().getData();
			EData<IArray> result = EMath.toEltMultiply(dataArray1, varianceArray1, 
					dataArray2, varianceArray2);
			newPlot.addData(findSignal().getShortName() + "_multiply", result.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), result.getVariance());
			newPlot.copyAxes(getAxisList());
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return newPlot;
	}

	public Plot eltMultiply(IArray array) throws PlotMathException{
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = findVarianceArray();
			EMath.eltMultiply(dataArray, array, varianceArray, array);
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return this;
	}

	public Plot toEltMultiply(IArray array) throws PlotMathException{
		Plot plot = (Plot) PlotFactory.createPlot(this, getShortName() + "_eltMultiply", getDimensionType());
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = findVarianceArray();
			EData<IArray> result = EMath.toEltMultiply(dataArray, array, varianceArray, array);
			plot.addData(findSignal().getShortName() + "_multiply", result.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), result.getVariance());
			plot.copyAxes(getAxisList());
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return plot;
	}

	public Plot toEltDivide(IArray array, IArray variance) throws PlotMathException{
		Plot plot = (Plot) PlotFactory.createPlot(this, getShortName() + "_eltDivide", getDimensionType());
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = findVarianceArray();
			EData<IArray> result = EMath.toEltDivideSkipZero(dataArray, array, varianceArray, variance);
			plot.addData(findSignal().getShortName() + "_multiply", result.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), result.getVariance());
			plot.copyAxes(getAxisList());
		}catch (Exception e) {
			e.printStackTrace();
			throw new PlotMathException(e);
		}
		return plot;
	}

	public Plot add(IArray array) throws PlotMathException{
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EMath.add(dataArray, array, varianceArray, array);
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return this;
	}

	public Plot toAdd(IArray array) throws PlotMathException{
		Plot plot = (Plot) PlotFactory.createPlot(getShortName() + "_add", getDimensionType());
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EData<IArray> result = EMath.toAdd(dataArray, array, varianceArray, array);
			plot.addData(findSignal().getShortName() + "_add", result.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), result.getVariance());
			plot.copyAxes(getAxisList());
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return plot;
	}

	public PlotIndex getIndex() throws SignalNotAvailableException {
		IIndex index;
		try {
			index = findSignalArray().getIndex();
		} catch (IOException e1) {
			throw new SignalNotAvailableException(e1);
		}
		List<IIndex> axisIndexList = new ArrayList<IIndex>();
		for (IArray axisArray : getAxesArrayList()){
			axisIndexList.add(axisArray.getIndex());
		}
		return new PlotIndex(index, axisIndexList);
	}

	public Point getMaximumPoint() throws PlotMathException {
		Point maxPoint = null;
		double max = Double.MIN_VALUE;
		PlotIterator iterator = null;
		try {
			iterator = new PlotIterator(this);
			while (iterator.hasNext()) {
				Point currentPoint = iterator.getPointNext();
				if (!Double.isNaN(currentPoint.getValue()) && currentPoint.getValue() > max){
					max = currentPoint.getValue();
					maxPoint = currentPoint;
				}
			}
		} catch (Exception e) {
			throw new PlotMathException("Can not iterate the plot");
		}
		return maxPoint;
	}

	public List<Point> getMaximumPointList() {
		return null;
	}

	public double getMaximumValue() throws IOException {
		return findSignalArray().getArrayMath().getMaximum();
	}

	public Point getMinimumPoint() throws PlotMathException {
		Point minPoint = null;
		double min = Double.MAX_VALUE;
		PlotIterator iterator = null;
		try {
			iterator = new PlotIterator(this);
			while (iterator.hasNext()) {
				Point currentPoint = iterator.getPointNext();
				if (!Double.isNaN(currentPoint.getValue()) && currentPoint.getValue() < min){
					min = currentPoint.getValue();
					minPoint = currentPoint;
				}
			}
		} catch (Exception e) {
			throw new PlotMathException("Can not iterate the plot");
		}
		return minPoint;
	}

	public List<Point> getMinimumPointList() {
		return null;
	}

	public double getMinimumValue() throws IOException {
		return findSignalArray().getArrayMath().getMinimum();
	}

	public Point getPoint(PlotIndex index) {
		return new Point(this, index);
	}

	public double getDoubleValue(PlotIndex index) throws IOException{
		return getPoint(index).getValue();
	}

	public double[] getCoordinate(PlotIndex index) throws IndexOutOfBoundException{
		return getPoint(index).getCoordinate();
	}

	public Plot matMultiply(Plot plot) {
		return null;
	}

	public Plot section(int[] reference, int[] shape) throws StructureTypeException {
		Plot section = (Plot) PlotFactory.createPlot(getShortName() + "_section", 
				PlotUtil.getDimensionType(this));
		Data data = findSingal();
		try {
			section.addData(data.getShortName() + "_section", data.getData().getArrayUtils().section(
					reference, shape).getArray(), data.getTitle() + " section", data.getUnitsString(), 
					getVariance().getData().getArrayUtils().section(reference, shape).getArray());
		} catch (Exception e) {
			throw new StructureTypeException(e);
		} 
		int oldDimension = 0;
		int newDimension = 0;
		for (Axis axis : getAxisList()){
			if (shape[oldDimension ++] != 1){
				try {
					section.addAxis(axis, newDimension ++);
				} catch (Exception e) {
					throw new StructureTypeException(e);
				}
			}
		}
		return section;
	}

	public Plot section(int[] reference, int[] shape, int[] stride) 
	throws StructureTypeException {
		Plot section = (Plot) PlotFactory.createPlot(getShortName() + "_section", 
				PlotUtil.getDimensionType(this));
		Data data = findSingal();
		try {
			long[] longStride = new long[stride.length];
			for (int i = 0; i < longStride.length; i++) {
				longStride[i] = stride[i];
			}
			section.addData(data.getShortName() + "_section", data.getData().getArrayUtils().section(
					reference, shape, longStride).getArray(), data.getTitle() + " section", data.getUnitsString(), 
					getVariance().getData().getArrayUtils().section(reference, shape, longStride).getArray());
		} catch (Exception e) {
			throw new StructureTypeException(e);
		} 
		int oldDimension = 0;
		int newDimension = 0;
		for (Axis axis : getAxisList()){
			if (shape[oldDimension ++] != 1){
				try {
					section.addAxis(axis, newDimension ++);
				} catch (Exception e) {
					throw new StructureTypeException(e);
				}
			}
		}
		return section;
	}

	public Plot slice(int dimension, int value) throws StructureTypeException {
		int rank = getRank();
		if (rank < 2) 
			throw new StructureTypeException("can not get slice, rank < 2");
		Plot slice = (Plot) PlotFactory.createPlot(getShortName() + "_slice", getReducedType(rank - 1));
		Data data = findSingal();
		try {
			slice.addData(data.getShortName()+ "_slice_" + dimension + "_" + value, 
					data.getData().getArrayUtils().slice(dimension, value).getArray(), data.getTitle() + " slice " + value, 
					data.getUnitsString(), getVariance().getData().getArrayUtils().slice(dimension, value).getArray());
		} catch (Exception e) {
			throw new StructureTypeException(e);
		} 
		int oldIndex = 0;
		int newIndex = 0;
		Axis sliceAxis = getAxis(dimension);
		for (Axis axis : getAxisList()){
			if (oldIndex ++ != dimension)
				if (axis.getRank() > 1){
					if (axis.getShape()[0] == sliceAxis.getSize()){
						try {
							IArray sliceArray = axis.getData().getArrayUtils().slice(0, value).getArray();
							slice.addAxis(axis.getShortName()+"_" + value, sliceArray, 
									axis.getTitle(), axis.getUnitsString(), newIndex ++);
						} catch (Exception e) {
							throw new StructureTypeException(e);
						}
					}
				}else{
					try {
						slice.addAxis(axis, newIndex ++);
					} catch (Exception e) {
						throw new StructureTypeException(e);
					} 
				}
		}
		
		return slice;
	}

	public Plot transpose(int dimension1, int dimension2) throws StructureTypeException 
	{
		if (getRank() < dimension1 || getRank() < dimension2)
			throw new StructureTypeException("illegal dimension");
		Plot transpose = (Plot) PlotFactory.createPlot(getShortName() + "_transpose", 
				PlotUtil.getDimensionType(this));
		Data data = findSingal();
		try {
			transpose.addData(data.getShortName() + "_transpose", data.getData().getArrayUtils().transpose(
					dimension1, dimension2).getArray(), data.getTitle(), data.getUnitsString(), 
					getVariance().getData().getArrayUtils().transpose(dimension1, dimension2).getArray());
		} catch (Exception e) {
			throw new StructureTypeException(e);
		} 
		for (int i = 0; i < getAxisList().size(); i ++){
			try{
				if (i == dimension1)
					transpose.addAxis(getAxis(dimension2), i);
				else if (i == dimension2)
					transpose.addAxis(getAxis(dimension1), i);
				else 
					transpose.addAxis(getAxis(i), i);
			}catch (Exception e) {
				throw new StructureTypeException(e);
			}
		}
		return transpose;
	}

	public Plot matrixTranspose() throws StructureTypeException{
		if (getRank() > 2)
			throw new StructureTypeException("plot is not a matrix");
		return transpose(0, 1);
	}

	public void setDouble(PlotIndex index, double value) throws SignalNotAvailableException {
		getSignalArray().setDouble(index.getDataIndex(), value);
	}

	public String getDataUnits(){
		return findSingal().getUnitsString();
	}

	public String[] getAxisUnits(){
		List<Axis> axisList = getAxisList(); 
		String[] units = new String[axisList.size()];
		int dimension = 0;
		for (Axis axis : axisList)
			units[dimension ++] = axis.getUnitsString();
		return units;
	}

	public int getRank(){
		return findSingal().getRank();
	}

	public DataDimensionType getDimensionType(){
		return PlotUtil.getDimensionType(this);
	}

	public Plot eltInverse() throws PlotMathException {
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EMath.eltInverse(dataArray, varianceArray);
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return this;
	}

	public Plot toEltInverse() throws PlotMathException {
		Plot plot = (Plot) PlotFactory.createPlot(getShortName() + "_inverse", getDimensionType());
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EData<IArray> inverseResult = EMath.toEltInverse(dataArray, varianceArray);
			plot.addData(findSignal().getShortName() + "_inverse", inverseResult.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), inverseResult.getVariance());
			plot.copyAxes(getAxisList());
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return plot;
	}

	public Plot add(double number, double variance) throws PlotMathException {
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EMath.add(dataArray, number, varianceArray, variance);
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return this;
	}

	public Plot toAdd(double number, double variance) throws PlotMathException {
		Plot plot = (Plot) PlotFactory.createPlot(getShortName() + "_add", getDimensionType());
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EData<IArray> result = EMath.toAdd(dataArray, number, varianceArray, variance);
			plot.addData(findSignal().getShortName() + "_inverse", result.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), result.getVariance());
			plot.copyAxes(getAxisList());
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return plot;
	}

	public Plot scale(double value, double variance) throws PlotMathException {
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EMath.scale(dataArray, value, varianceArray, variance);
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return this;
	}

	public Plot toScale(double value, double variance) throws PlotMathException {
		Plot plot = (Plot) PlotFactory.createPlot(getShortName() + "_scale", getDimensionType());
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EData<IArray> result = EMath.toScale(dataArray, value, varianceArray, variance);
			plot.addData(findSignal().getShortName() + "_inverse", result.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), result.getVariance());
			plot.copyAxes(getAxisList());
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return plot;
	}

	public boolean isComformable(Plot plot) throws PlotMathException {
		try {
			return findSignalArray().getArrayUtils().isConformable(plot.findSignalArray());
		} catch (IOException e) {
			throw new PlotMathException(e);
		}
	}

	@SuppressWarnings("deprecation")
	public Plot matInverse() throws PlotMathException {
		Plot plot = (Plot) PlotFactory.createPlot(getShortName() + "_metInverse", getDimensionType());
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EData<IArray> result = EMath.matInverse(dataArray, varianceArray);
			plot.addData(findSignal().getShortName() + "_inverse", result.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), result.getVariance());
			plot.copyAxes(getAxisList());
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return plot;
	}

	public EData<Double> sum() throws IOException {
		return EMath.sum(findSignalArray(), getVariance().getData());
	}

	public Plot sumForDimension(int dimension) throws PlotMathException {
		Plot plot = (Plot) PlotFactory.createPlot(this, getShortName() + "_sum", DataDimensionType.pattern);
		try{
			IArray dataArray = findSignalArray();
			Variance variance = getVariance();
			IArray varianceArray = null;
			if (variance != null)
				varianceArray = variance.getData();
			EData<IArray> result = EMath.sumForDimension(dataArray, dimension, varianceArray);
			plot.addData(findSignal().getShortName() + "_inverse", result.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), result.getVariance());
			plot.addAxis(getAxisList().get(dimension), 0);
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return plot;
	}

	public Plot enclosedSumForDimension(int dimension) throws PlotMathException {
		Plot plot = (Plot) PlotFactory.createPlot(this, getShortName() + "_sum", DataDimensionType.pattern);
		try{
			IArray dataArray = findSignalArray();
			Variance variance = getVariance();
			IArray varianceArray = null;
			if (variance != null)
				varianceArray = variance.getData();
			EData<IArray> result = EMath.enclosedSumForDimension(dataArray, dimension, varianceArray);
			plot.addData(findSignal().getShortName() + "_inverse", result.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), result.getVariance());
			plot.addAxis(getAxisList().get(dimension), 0);
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return plot;
	}

	public Plot exp() throws PlotMathException {
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EMath.exp(dataArray, varianceArray);
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return this;
	}

	public Plot toExp() throws PlotMathException {
		Plot plot = (Plot) PlotFactory.createPlot(getShortName() + "_metInverse", getDimensionType());
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EData<IArray> result = EMath.toExp(dataArray, varianceArray);
			plot.addData(findSignal().getShortName() + "_inverse", result.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), result.getVariance());
			plot.copyAxes(getAxisList());
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return plot;
	}

	public Plot ln() throws PlotMathException {
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EMath.ln(dataArray, varianceArray);
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return this;
	}

	public Plot toLn() throws PlotMathException {
		Plot plot = (Plot) PlotFactory.createPlot(getShortName() + "_metInverse", getDimensionType());
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EData<IArray> result = EMath.toLn(dataArray, varianceArray);
			plot.addData(findSignal().getShortName() + "_inverse", result.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), result.getVariance());
			plot.copyAxes(getAxisList());
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return plot;
	}

	public Plot log10() throws PlotMathException {
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EMath.log10(dataArray, varianceArray);
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return this;
	}

	public Plot toLog10() throws PlotMathException {
		Plot plot = (Plot) PlotFactory.createPlot(getShortName() + "_metInverse", getDimensionType());
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EData<IArray> result = EMath.toLog10(dataArray, varianceArray);
			plot.addData(findSignal().getShortName() + "_inverse", result.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), result.getVariance());
			plot.copyAxes(getAxisList());
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return plot;
	}

	public Plot power(double value) throws PlotMathException {
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EMath.power(dataArray, value, varianceArray);
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return this;
	}

	public Plot toPower(double value) throws PlotMathException {
		Plot plot = (Plot) PlotFactory.createPlot(getShortName() + "_metInverse", getDimensionType());
		try{
			IArray dataArray = findSignalArray();
			IArray varianceArray = getVariance().getData();
			EData<IArray> result = EMath.toPower(dataArray, value, varianceArray);
			plot.addData(findSignal().getShortName() + "_inverse", result.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), result.getVariance());
			plot.copyAxes(getAxisList());
		}catch (Exception e) {
			throw new PlotMathException(e);
		}
		return plot;
	}

	public void copyAxes(List<Axis> axisList) throws PlotFactoryException 
	{
		for (int i = 0; i < axisList.size(); i++) {
			try {
				addAxis(axisList.get(i), i);
			} catch (Exception e) {
				throw new PlotFactoryException("can not copy axis " + 
						axisList.get(i).getShortName());
			} 
		}
	}

	/* mod pvh 10sep2009 */
	public void addCalculationData(String shortName, IArray array, String title,
			String units, IArray varianceArray) throws InvalidArrayTypeException {
		Data data = new NcData(this, shortName, array, title, units, varianceArray);
		data.addStringAttribute("signal","calculation");
		addDataItem(data);
		addLog("Add calculated data item: " + shortName);	
	}

	/* add pvh 9mar2009 */
	/* mod pvh 10sep2009 */
	public void addCalculationData(
			String shortName,  // data item name 
			IArray array,       // data item value
			String title,      // data item title attribute
			String units)      // data item units attribute
	throws InvalidArrayTypeException 
	{
		Data data = new NcData(this, shortName, array, title, units);
		data.addStringAttribute("signal","calculation");
		addDataItem(data);
		addLog("Add calculated data item: " + shortName);	
	}

	public Data findCalculationData(String shortName) {
		IDataItem data = findDataItem(shortName);
		if (data != null && data instanceof Data)
			return (Data) data;
		return null;
	}

	public List<Data> getCalculationData() {
		List<Data> dataList = new ArrayList<Data>();
		List<IDataItem> itemList = getDataItemList();
		for (IDataItem item : itemList){
			if (item instanceof Data){
				if (((Data) item).hasAttribute("signal", "calculation"))
					dataList.add((Data) item);
			}
		}
		return dataList;
	}

	public void addAxis(Axis axis) throws InvalidArrayTypeException, PlotFactoryException, IOException {
		int dimension;
		List<Axis> axisList = getAxisList();
		if (axisList == null || axisList.size() == 0)
			dimension = 0;
		else
			dimension = getAxisList().size();		
		addAxis(axis, dimension);
	}

	public Plot integrateDimension(int dimension) throws PlotMathException {
		DataDimensionType dimensionType;
		if (getRank() > 2)
			dimensionType = DataDimensionType.patternset;
		else 
			dimensionType = DataDimensionType.pattern;
		Plot plot = (Plot) PlotFactory.createPlot(this, getShortName() + "_integrate_" + dimension, 
				dimensionType);
		try{
			IArray dataArray = findSignalArray();
			Variance variance = getVariance();
			IArray varianceArray = null;
			if (variance != null)
				varianceArray = variance.getData();
			EData<IArray> result = EMath.integrateDimension(dataArray, dimension, varianceArray);
			plot.addData(findSignal().getShortName() + "_integrate", result.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), result.getVariance());
			int index = 0;
			for (Axis axis : getAxisList()){
				if (index < dimension)
					plot.addAxis(axis, index);
				else if (index > dimension)
					plot.addAxis(axis, index - 1);
				index ++;
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			throw new PlotMathException(e);
		}
		return plot;
	}
	
	public Plot enclosedIntegrateDimension(int dimension) throws PlotMathException {
		DataDimensionType dimensionType;
		if (getRank() > 2)
			dimensionType = DataDimensionType.patternset;
		else 
			dimensionType = DataDimensionType.pattern;
		Plot plot = (Plot) PlotFactory.createPlot(this, getShortName() + "_integrate_" + dimension, 
				dimensionType);
		try{
			IArray dataArray = findSignalArray();
			Variance variance = getVariance();
			IArray varianceArray = null;
			if (variance != null)
				varianceArray = variance.getData();
			EData<IArray> result = EMath.enclosedIntegrateDimension(dataArray, dimension, varianceArray);
			plot.addData(findSignal().getShortName() + "_integrate", result.getData(), 
					findSingal().getTitle(), findSingal().getUnitsString(), result.getVariance());
			int index = 0;
			for (Axis axis : getAxisList()){
				if (index < dimension)
					plot.addAxis(axis, index);
				else if (index > dimension)
					plot.addAxis(axis, index - 1);
				index ++;
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			throw new PlotMathException(e);
		}
		return plot;
	}
	
	/**
	 * Return the variance array as a GDM Array object.
	 * @return GDM Array object
	 */
	public IArray findVarianceArray(){
		IArray varianceArray = null;
		try{
			varianceArray = getVariance().getData();
		}catch (Exception e) {
		}
		return varianceArray;
	}

	public void setTitle(String title){
		findSingal().setTitle(title);
	}

	public String getTitle() {
		return findSingal().getTitle();
	}

	public long calculateMemorySize() throws IOException {
		long size = findSignalArray().getSize();
		Class<?> type = findSignalArray().getElementType();
		DataType dataType = DataType.getType(type);
		return size * dataType.getSize();
	}

	public void clearData() {
		getDataItemList().remove(getVariance());
		getDataItemList().removeAll(getAxisList());
		getDataItemList().remove(findSingal());
	}
}
