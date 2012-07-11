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

import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotSet;
import au.gov.ansto.bragg.datastructures.core.plot.PlotUtil;
import au.gov.ansto.bragg.datastructures.core.plot.Position;
import au.gov.ansto.bragg.datastructures.core.plot.StepDirection;
import au.gov.ansto.bragg.datastructures.core.plot.StepDirection.StepDirectionType;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 13/10/2008
 */
public class ChooseSlice extends ConcreteProcessor {

	private Plot chooseSlice_mapsetInput;
	private Plot chooseSlice_mapOutput;
	private PlotSet chooseSlice_patternsetInput;
	private Plot chooseSlice_patternInput;
	private Plot chooseSlice_patternOutput;
	private Boolean chooseSlice_skip = false;
	private Boolean chooseSlice_stop = false;
	private StepDirection chooseSlice_stepDirection = new StepDirection(StepDirectionType.holding);
	private Integer chooseSlice_currentIndex = 0;
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public DataDimensionType dataDimensionType = DataDimensionType.patternset;
	private int currentMapSize = 0;
	private int currentPatternSize = 0;
	private Position chooseSlice_axisValue = new Position("NaN");
	private boolean isAxisValueChanged = false;
	private boolean isCurrentIndexChanged = false;
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.nbi.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		if (chooseSlice_skip){
			chooseSlice_mapOutput = chooseSlice_mapsetInput;
			chooseSlice_patternOutput = chooseSlice_patternsetInput.getPlotList().get(0);
			return chooseSlice_stop;
		}
		boolean isSizeChanged = false;
		int mapsetSize = 0;
		int patternsetSize = 0;
		DataDimensionType mapsetType = PlotUtil.getDimensionType(chooseSlice_mapsetInput);
		if (mapsetType.name().contains("set")){
			mapsetSize = chooseSlice_mapsetInput.findSingal().getShape()[0];
		}else{
			chooseSlice_mapOutput = chooseSlice_mapsetInput;
		}
		if (mapsetSize != currentMapSize){
			isSizeChanged = true;
			currentMapSize = mapsetSize;
		}
//		DataDimensionType patternsetType = PlotUtil.getDimensionType(chooseSlice_patternsetInput);
//		if (patternsetType.name().contains("set")){
		patternsetSize = chooseSlice_patternsetInput.getPlotList().size();
		if (patternsetSize != currentPatternSize){
			isSizeChanged = true;
			currentPatternSize = patternsetSize;
		}
		updateAxisValueOptions();
//		}else{
//			chooseSlice_patternOutput = chooseSlice_patternsetInput;
//		}
		if (isSizeChanged){
			int indexSize = mapsetSize > patternsetSize ? mapsetSize : patternsetSize;
			List<Object> indexOptions = new ArrayList<Object>();
			for (int i = 1; i <= indexSize; i++) {
				indexOptions.add(Integer.valueOf(i));
			}
			informVarOptionsChange("chooseSlice_currentIndex", indexOptions);
			if (indexOptions.size() > 0)
				setCurrentIndex(0);
		}
		if (isCurrentIndexChanged){
			setAxisIndex(chooseSlice_currentIndex);
			isCurrentIndexChanged = false;
			isAxisValueChanged = false;
		} else if (isAxisValueChanged){
			if (chooseSlice_axisValue != null && !Double.isNaN(chooseSlice_axisValue.getX())){
				List<IArray> axisList = null;
				try {
					axisList = ((NcGroup) chooseSlice_patternInput).getAxesArrayList();
					IArray axisArray = axisList.get(axisList.size() - 1);
					setCurrentIndex(findIndex(chooseSlice_axisValue.getX(), axisArray));			
				} catch (SignalNotAvailableException e) {
					e.printStackTrace();
				}
			}
			isAxisValueChanged = false;
			isCurrentIndexChanged = false;
		}else{
			if (chooseSlice_axisValue != null && !Double.isNaN(chooseSlice_axisValue.getX()))
				informVarValueChange("chooseSlice_axisValue", chooseSlice_axisValue);
		}
		switch (chooseSlice_stepDirection.getDirectionType()) {
		case forward:
			if (chooseSlice_currentIndex < mapsetSize - 1)
				setCurrentIndex(chooseSlice_currentIndex + 1);
			break;
		case backward:
			if (chooseSlice_currentIndex > 0)
				setCurrentIndex(chooseSlice_currentIndex - 1);
			break;
		default:
			if (isSizeChanged)
				setCurrentIndex(chooseSlice_currentIndex);
			break;
		}
//		if (chooseSlice_currentIndex >= mapsetSize && chooseSlice_currentIndex >= patternsetSize)
//			chooseSlice_currentIndex = 0;
		int current_mapIndex = chooseSlice_currentIndex >= mapsetSize ? 
				mapsetSize - 1 : chooseSlice_currentIndex;
		int current_patternIndex = chooseSlice_currentIndex >= patternsetSize ?
				patternsetSize - 1 : chooseSlice_currentIndex;
		if (mapsetSize > 0)
			chooseSlice_mapOutput = chooseSlice_mapsetInput.slice(0, current_mapIndex);
		if (patternsetSize > 0)
			chooseSlice_patternOutput = chooseSlice_patternsetInput.getPlotList().get(current_patternIndex);
		chooseSlice_stepDirection = new StepDirection(StepDirectionType.holding);
//		List<?> patterns = PlotUtil.slice(chooseSlice_patternOutput);
		return chooseSlice_stop;
	}
	
	private void setCurrentIndex(Integer index){
		informVarValueChange("chooseSlice_currentIndex", index + 1);
		chooseSlice_currentIndex = index;
		setAxisIndex(index);
	}
	
	private void setAxisIndex(int index){
		List<Axis> axisList = null;
		try {
			axisList = chooseSlice_patternInput.getAxisList();
			Axis axis = axisList.get(axisList.size() - 1);
			IArray axisArray = axis.getData();
			setAxisValue(String.valueOf(findAxisName(axis) + findValue(chooseSlice_currentIndex, axisArray)));
		}catch (Exception e) {
		}		
	}
	private String findAxisName(Axis axis){
		return axis.getTitle() + ": ";
	}
	
	private void setAxisValue(String value){
		chooseSlice_axisValue = new Position(value);
		informVarValueChange("chooseSlice_axisValue", chooseSlice_axisValue);
	}
	/**
	 * @return the chooseSlice_mapOutput
	 */
	public Plot getChooseSlice_mapOutput() {
		return chooseSlice_mapOutput;
	}
	/**
	 * @return the chooseSlice_patternOutput
	 */
	public Plot getChooseSlice_patternOutput() {
		return chooseSlice_patternOutput;
	}
	/**
	 * @param chooseSlice_mapsetInput the chooseSlice_mapsetInput to set
	 */
	public void setChooseSlice_mapsetInput(Plot chooseSlice_mapsetInput) {
		this.chooseSlice_mapsetInput = chooseSlice_mapsetInput;
	}
	/**
	 * @param chooseSlice_patternsetInput the chooseSlice_patternsetInput to set
	 */
	public void setChooseSlice_patternsetInput(PlotSet chooseSlice_patternsetInput) {
		this.chooseSlice_patternsetInput = chooseSlice_patternsetInput;
	}
	/**
	 * @param chooseSlice_skip the chooseSlice_skip to set
	 */
	public void setChooseSlice_skip(Boolean chooseSlice_skip) {
		this.chooseSlice_skip = chooseSlice_skip;
	}
	/**
	 * @param chooseSlice_stop the chooseSlice_stop to set
	 */
	public void setChooseSlice_stop(Boolean chooseSlice_stop) {
		this.chooseSlice_stop = chooseSlice_stop;
	}
	/**
	 * @param chooseSlice_stepDirection the chooseSlice_stepDirection to set
	 */
	public void setChooseSlice_stepDirection(StepDirection chooseSlice_stepDirection) {
		this.chooseSlice_stepDirection = chooseSlice_stepDirection;
	}
	/**
	 * @param chooseSlice_currentIndex the chooseSlice_currentIndex to set
	 * Internal index start from 0. UI index start from 1. 
	 */
	public void setChooseSlice_currentIndex(Integer chooseSlice_currentIndex) {
		if (this.chooseSlice_currentIndex != chooseSlice_currentIndex - 1){
			this.chooseSlice_currentIndex = chooseSlice_currentIndex - 1;
			isCurrentIndexChanged = true;
		}
//		List<Array> axisList = null;
//		try {
//			axisList = chooseSlice_patternInput.getAxesArrayList();
//			Array axisArray = axisList.get(axisList.size() - 1);
//			setAxisValue(String.valueOf(findValue(chooseSlice_currentIndex, axisArray)));
//		}catch (Exception e) {
//		}
	}
	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

	/**
	 * @param chooseSlice_patternInput the chooseSlice_patternInput to set
	 */
	public void setChooseSlice_patternInput(Plot chooseSlice_patternInput) {
		this.chooseSlice_patternInput = chooseSlice_patternInput;
	}
	
	/**
	 * @param chooseSlice_axisValue the chooseSlice_axisValue to set
	 */
	public void setChooseSlice_axisValue(Position chooseSlice_axisValue) {
		if (this.chooseSlice_axisValue != chooseSlice_axisValue){
			this.chooseSlice_axisValue = chooseSlice_axisValue;
			isAxisValueChanged = true;
		}
//		if (chooseSlice_axisValue == null || Double.isNaN(chooseSlice_axisValue.getX()))
//			return;
//		List<Array> axisList = null;
//		try {
//			axisList = chooseSlice_patternInput.getAxesArrayList();
//			Array axisArray = axisList.get(axisList.size() - 1);
//			setCurrentIndex(findIndex(chooseSlice_axisValue.getX(), axisArray));			
//		} catch (SignalNotAvailableException e) {
//			this.chooseSlice_axisValue = chooseSlice_axisValue;
//			e.printStackTrace();
//		}
	}

	private void updateAxisValueOptions(){
		List<Axis> axisList = null;
		try {
			axisList = chooseSlice_patternInput.getAxisList();
			Axis axis = axisList.get(axisList.size() - 1);
			IArray axisArray = axis.getData();
			List<Position> options = new ArrayList<Position>();
			IArrayIterator axisIterator = axisArray.getIterator();
			while (axisIterator.hasNext()){
				options.add(new Position(findAxisName(axis) + axisIterator.getDoubleNext()));
			}
			informVarOptionsChange("chooseSlice_axisValue", options);
		}catch (Exception e) {
		}		
	}
	
	private double findValue(int index, IArray axis){
		IIndex axisIndex = axis.getIndex();
		axisIndex.set(index);
		try{
			return axis.getDouble(axisIndex);
		}catch (Exception e) {
			return Double.NaN;
		}
	}
	
	private int findIndex(double value, IArray axis) {
		int index = 0;
		IArrayIterator axisIterator = axis.getIterator();
		IIndex axisIndex = axis.getIndex();
		double axisFirstValue = axis.getDouble(axisIndex.set(0));
		double axisLastValue = axis.getDouble(axisIndex.set((int) axis.getSize() - 1));
		if (axisFirstValue < axisLastValue){
			if (value < axisFirstValue)
				return 0;
			if (value > axisLastValue)
				return (int) axis.getSize() - 1;
			int counter = 0;
			double lastDifference = 0;
			double scale;
			double thisDifference;
			while (axisIterator.hasNext()){
				scale = axisIterator.getDoubleNext();
				thisDifference = Math.abs(value - scale);
				if (value < scale){
					if (thisDifference < lastDifference)
						index = counter;
					else
						index = counter - 1;
					break;
				}
				lastDifference = thisDifference;
				counter ++;
			}
		}else{
			if (value > axisFirstValue)
				return 0;
			if (value < axisLastValue)
				return (int) axis.getSize() - 1;
			int counter = 0;
			double lastDifference = 0;
			double scale;
			double thisDifference;
			while (axisIterator.hasNext()){
				scale = axisIterator.getDoubleNext();
				thisDifference = Math.abs(value - scale);
				if (value > scale){
					if (thisDifference < lastDifference)
						index = counter;
					else
						index = counter - 1;
					break;
				}
				lastDifference = thisDifference;
				counter ++;
			}
		}
		if (index > axis.getSize() - 1)
			index = (int) axis.getSize() - 1;
		return index;
	}

}
