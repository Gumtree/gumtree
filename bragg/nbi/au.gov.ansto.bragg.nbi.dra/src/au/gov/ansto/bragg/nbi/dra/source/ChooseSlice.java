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

import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
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

	private Plot chooseSlice_inputPlot;
	private Plot chooseSlice_outputPlot;
	private Boolean chooseSlice_skip = false;
	private Boolean chooseSlice_stop = false;
	private StepDirection chooseSlice_stepDirection = new StepDirection(StepDirectionType.holding);
	private Integer chooseSlice_currentIndex = 1;
	private Position chooseSlice_axisValue = new Position("NaN");
	
	private int currentIndex;
	private double currentAxisValue;
	private IArray currentAxisArray;
	private int currentNumberOfSlice = 0;

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.nbi.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		if (chooseSlice_skip){
			chooseSlice_outputPlot = chooseSlice_inputPlot;
			return chooseSlice_stop;
		}
		
		// check if axis information has changed
		int numberOfSlice = 0;
		DataDimensionType dimensionType = PlotUtil.getDimensionType(chooseSlice_inputPlot);
		if (dimensionType.name().contains("set")){
			numberOfSlice = chooseSlice_inputPlot.findSingal().getShape()[0];
		}else{
			chooseSlice_outputPlot = chooseSlice_inputPlot;
			return chooseSlice_stop;
		}
		IArray axisArray = chooseSlice_inputPlot.getAxis(0).getData();
		if (numberOfSlice != currentNumberOfSlice){
			updateSliceIndex(numberOfSlice);
			updateAxisValueOptions(axisArray);
		}else{
			if (!axisArray.equals(currentAxisArray))
				updateAxisValueOptions(axisArray);
		}
		switch (chooseSlice_stepDirection.getDirectionType()) {
		case forward:
			if (chooseSlice_currentIndex < numberOfSlice)
				chooseSlice_currentIndex ++;
			break;
		case backward:
			if (chooseSlice_currentIndex > 1)
				chooseSlice_currentIndex --;
			break;
		default:
			break;
		}
		if (chooseSlice_currentIndex > numberOfSlice)
			chooseSlice_currentIndex = numberOfSlice;
		if (currentIndex != chooseSlice_currentIndex - 1){
			setCurrentIndex(chooseSlice_currentIndex - 1);
		}else if (chooseSlice_axisValue.getX() != currentAxisValue)
			setCurrentIndex(findIndex(chooseSlice_axisValue.getX(), axisArray));
		chooseSlice_outputPlot = chooseSlice_inputPlot.slice(0, currentIndex);
		chooseSlice_stepDirection = new StepDirection(StepDirectionType.holding);
		return chooseSlice_stop;
	}
	
	private void updateSliceIndex(int numberOfSlice) {
		List<Object> indexOptions = new ArrayList<Object>();
		for (int i = 1; i <= numberOfSlice; i++) {
			indexOptions.add(Integer.valueOf(i));
		}
		informVarOptionsChange("chooseSlice_currentIndex", indexOptions);
		currentNumberOfSlice = numberOfSlice;
	}
	
	private void updateAxisValueOptions(IArray axisArray){
		try {
			List<Position> options = new ArrayList<Position>();
			IArrayIterator axisIterator = axisArray.getIterator();
			while (axisIterator.hasNext()){
				options.add(new Position(findAxisName() + axisIterator.getDoubleNext()));
			}
			informVarOptionsChange("chooseSlice_axisValue", options);
			currentAxisArray = axisArray;
		}catch (Exception e) {
		}		
	}

	/**
	 * @return the chooseSlice_outputPlot
	 */
	public Plot getChooseSlice_outputPlot() {
		return chooseSlice_outputPlot;
	}
	/**
	 * @param chooseSlice_inputPlot the chooseSlice_inputPlot to set
	 */
	public void setChooseSlice_inputPlot(Plot chooseSlice_inputPlot) {
		this.chooseSlice_inputPlot = chooseSlice_inputPlot;
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
	 * @param chooseSlice_axisValue the chooseSlice_axisValue to set
	 */
	public void setChooseSlice_axisValue(Position chooseSlice_axisValue) {
		this.chooseSlice_axisValue = chooseSlice_axisValue;
	}

	private void setCurrentIndex(Integer index){
		informVarValueChange("chooseSlice_currentIndex", index + 1);
		currentIndex = index;
		chooseSlice_currentIndex = index + 1;
		setAxisIndex(index);
	}

	/**
	 * @param chooseSlice_currentIndex the chooseSlice_currentIndex to set
	 */
	public void setChooseSlice_currentIndex(Integer chooseSlice_currentIndex) {
		this.chooseSlice_currentIndex = chooseSlice_currentIndex;
	}

	private void setAxisIndex(int index){
		try {
			IArray axisArray = chooseSlice_inputPlot.getAxis(0).getData();
			String axisName = findAxisName();
			setAxisValue(axisName, String.valueOf(findValue(index, axisArray)));
		}catch (Exception e) {
		}		
	}

	private void setAxisValue(String name, String value){
		informVarValueChange("chooseSlice_axisValue", name + value);
		currentAxisValue = Double.valueOf(value);
		chooseSlice_axisValue = new Position(name + value);
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

	private String findAxisName(){
		if (chooseSlice_inputPlot == null)
			return "";
		return chooseSlice_inputPlot.getAxis(0).getTitle() + ": ";
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
