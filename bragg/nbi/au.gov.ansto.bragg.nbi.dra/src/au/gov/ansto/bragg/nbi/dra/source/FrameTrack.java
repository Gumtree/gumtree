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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.Position;
import au.gov.ansto.bragg.datastructures.core.plot.StepDirection;
import au.gov.ansto.bragg.datastructures.core.plot.StepDirection.StepDirectionType;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 16/06/2009
 */
public class FrameTrack extends ConcreteProcessor {

	private Integer currentIndexOut = 0;
	private Integer currentIndexVar = 0;
	private StepDirection trackStepDirection = new StepDirection(StepDirectionType.holding);
	private IGroup inputGroup;
	private Boolean useCorrectedData = false;
	private Position scanVariablePosition = new Position("NaN");
	private Boolean stop = false;
	private Plot outputPlot;
	private IDataItem scanAxis;
	private Boolean isInLoop = false;
	private Integer numberOfSteps = 0;
	private Integer currentStepIndex = 0;
	private Boolean resetHistory = false;
	
	private boolean isInputChanged = false;
	private int loopIndex;
	private boolean isIndexChanged = false;
	private int numberOfFrames = 0;
	private double currentAxisValue = 0;
	private IArray currentAxisArray;
	private String axisName;
	private IGroup entryGroup;
	private boolean runOnce = false;
	private boolean stopNow = false;
	/**
	 * 
	 */
	public FrameTrack() {
		super();
		setReprocessable(true);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.processor.ConcreteProcessor#process()
	 */
	@Override
	public Boolean process() throws Exception {
		if (inputGroup == null){
			return true;
		}
		if (isInputChanged){
			isInputChanged = false;
			resetHistory = true;
			scanAxis = NexusUtils.getNexusAxis(inputGroup).get(0);
			axisName = scanAxis.getShortName();
			currentAxisArray = scanAxis.getData();
			int frameNumber = (int) currentAxisArray.getSize();
//			currentIndexVar = 0;
			if (frameNumber != numberOfFrames){
				numberOfFrames = frameNumber;
//				if (currentIndexVar >= numberOfFrames)
//					currentIndexVar = numberOfFrames;
				updateCurrentIndex(currentIndexVar, true);
			}else
				updateCurrentIndex(currentIndexVar, false);
			updateScanVariablePosition(currentIndexVar, true);
			isIndexChanged = false;
		} else {
			resetHistory = false;
		}
		if (runOnce){
			runOnce = false;
			updateCurrentStepIndex(2);
			return true;
		}
		if (isIndexChanged){
			isIndexChanged = false;
			runOnce = true;
			updateNumberOfSteps(1);
			updateCurrentStepIndex(1);
			switch (trackStepDirection.getDirectionType()) {
			case forward:
				if (currentIndexVar < numberOfFrames - 1)
					currentIndexVar ++;
				break;
			case backward:
				if (currentIndexVar > 0)
					currentIndexVar --;
				break;
			default:
				break;
			}
//			trackStepDirection = new StepDirection(StepDirectionType.holding);
			updateStepDirection(new StepDirection(StepDirectionType.holding));
			if (currentIndexVar > numberOfFrames - 1)
				currentIndexVar = numberOfFrames - 1;
			if (currentIndexOut != currentIndexVar){
				updateCurrentIndex(currentIndexVar, false);
				updateScanVariablePosition(currentIndexVar, false);
			}else
				if (scanVariablePosition.getX() != currentAxisValue){
					currentAxisValue = scanVariablePosition.getX();
					updateCurrentIndex(findAxisIndex(currentAxisValue), false);
					System.err.println("&&&&&&&&&&&&The selected index is " + currentIndexOut);
				}
			outputPlot = NexusUtils.createSlicePlot(inputGroup, currentIndexVar, useCorrectedData);
		}else{
			if (isInLoop){
				loopIndex ++;
				if (loopIndex >= numberOfFrames){
					loopIndex = -1;
					updateIsInLoop(false);
//					isInLoop = true;
					stopNow = true;
					outputPlot = NexusUtils.createSlicePlot(inputGroup, currentIndexVar, useCorrectedData);
					currentIndexOut = currentIndexVar;
					updateCurrentStepIndex(numberOfFrames);
				}else{
					outputPlot = NexusUtils.createSlicePlot(inputGroup, loopIndex, useCorrectedData);
					currentIndexOut = loopIndex;
					updateCurrentStepIndex(currentIndexOut);
				}
			}else{
				if (stopNow){
//					updateIsInLoop(false);
					isInLoop = false;
					updateCurrentStepIndex(numberOfFrames + 1);
					stopNow = false;
					return true;
				}
				updateIsInLoop(true);
				updateNumberOfSteps(numberOfFrames);
				loopIndex = 0;
				currentIndexOut = loopIndex;
				updateCurrentStepIndex(currentIndexOut);
				outputPlot = NexusUtils.createSlicePlot(inputGroup, loopIndex, useCorrectedData);
			}
		}
		if (inputGroup != null)
			inputGroup.getGroupList().clear();
		correctTwoTheta();
		return stop;
	}

	private void correctTwoTheta() throws IOException {
		List<Axis> axes = outputPlot.getAxisList();
		Axis twoTheta = axes.get(axes.size() - 1);
		if (twoTheta.getName().contains("offset")) {
			IArray stthArray = outputPlot.findDataItem("stth").getData();
			double stth = stthArray.getDouble(stthArray.getIndex().set(currentIndexOut));
			twoTheta.getData().getArrayMath().add(stth);
		}
	}

	private int findAxisIndex(double value){
		int index = 0;
		IArrayIterator axisIterator = currentAxisArray.getIterator();
		while (axisIterator.hasNext()){
			double axisValue = axisIterator.getDoubleNext();
			if (!Double.isNaN(axisValue)){
				if (axisValue == value){
					return index;
				}
			}
			index ++;
		}
		return -1;
	}
//	private int findAxisIndex(double value) {
//		int index = 0;
//		ArrayIterator axisIterator = currentAxisArray.getIterator();
//		Index axisIndex = currentAxisArray.getIndex();
//		double axisFirstValue = currentAxisArray.getDouble(axisIndex.set(0));
//		double axisLastValue = currentAxisArray.getDouble(axisIndex.set((int) currentAxisArray.getSize() - 1));
//		if (axisFirstValue < axisLastValue){
//			if (value < axisFirstValue)
//				return 0;
//			if (value > axisLastValue)
//				return (int) currentAxisArray.getSize() - 1;
//			int counter = 0;
//			double lastDifference = 0;
//			double scale;
//			double thisDifference;
//			while (axisIterator.hasNext()){
//				scale = axisIterator.getDoubleNext();
//				thisDifference = Math.abs(value - scale);
//				if (value <= scale){
//					if (thisDifference < lastDifference)
//						index = counter;
//					else
//						index = counter - 1;
//					break;
//				}
//				lastDifference = thisDifference;
//				counter ++;
//			}
//		}else{
//			if (value > axisFirstValue)
//				return 0;
//			if (value < axisLastValue)
//				return (int) currentAxisArray.getSize() - 1;
//			int counter = 0;
//			double lastDifference = 0;
//			double scale;
//			double thisDifference;
//			while (axisIterator.hasNext()){
//				scale = axisIterator.getDoubleNext();
//				thisDifference = Math.abs(value - scale);
//				if (value >= scale){
//					if (thisDifference < lastDifference)
//						index = counter;
//					else
//						index = counter - 1;
//					break;
//				}
//				lastDifference = thisDifference;
//				counter ++;
//			}
//		}
//		if (index > currentAxisArray.getSize() - 1)
//			index = (int) currentAxisArray.getSize() - 1;
//		return index;
//	}

	private void updateCurrentIndex(int index, boolean isNew){
		currentIndexOut = index;
		currentIndexVar = index;
		if (isNew){
			List<Object> indexOptions = new ArrayList<Object>();
			for (int i = 1; i <= numberOfFrames; i++) {
				indexOptions.add(Integer.valueOf(i));
			}
			informVarOptionsChange("currentIndexVar", indexOptions);
		}
		informVarValueChange("currentIndexVar", index + 1);
	}
	
	private void updateScanVariablePosition(int index, boolean isNew){
		Position position = new Position(axisName, Double.NaN, 
				currentAxisArray.getDouble(currentAxisArray.getIndex().set(index)), Double.NaN);
		if (isNew){
			try {
				List<Position> options = new ArrayList<Position>();
				IArrayIterator valueIter = currentAxisArray.getIterator();
				while (valueIter.hasNext()){
					options.add(new Position(axisName, Double.NaN, valueIter.getDoubleNext(), Double.NaN));
				}
				informVarOptionsChange("scanVariablePosition", options);
			}catch (Exception e) {
			}		
		}
		currentAxisValue = position.getX();
		informVarValueChange("scanVariablePosition", position);
	}

	private void updateStepDirection(StepDirection direction){
		trackStepDirection = direction;
		informVarValueChange("trackStepDirection", direction);
	}
	/**
	 * @return the currentIndexOut
	 */
	public Integer getCurrentIndexOut() {
		return currentIndexOut;
	}

	/**
	 * @return the outputPlot
	 */
	public Plot getOutputPlot() {
		return outputPlot;
	}

	/**
	 * @param currentIndexIn the currentIndexIn to set
	 */
	public void setCurrentIndexVar(Integer currentIndexVar) {
		if (!this.currentIndexVar.equals(currentIndexVar - 1)){
			this.currentIndexVar = currentIndexVar - 1;
			isIndexChanged = true;
		}
	}

	/**
	 * @param trackStepDirection the trackStepDirection to set
	 */
	public void setTrackStepDirection(StepDirection trackStepDirection) {
		if (this.trackStepDirection.getDirectionType() != trackStepDirection.getDirectionType()){
			this.trackStepDirection = trackStepDirection;
			isIndexChanged = true;
		}
	}

	/**
	 * @param inputGroup the inputGroup to set
	 */
	@SuppressWarnings("deprecation")
	public void setInputGroup(IGroup entryGroup) {
		IGroup groupData = null;
		try{
//			groupData = NexusUtils.getNexusEntryList(rootGroup).get(0);
			groupData = NexusUtils.getNexusData(entryGroup);
		}catch (Exception e) {
			throw new NullPointerException("can not find nexus data from the file");
		}
		if (entryGroup != this.entryGroup)
			isInputChanged = true;
		else{
			if (((NcGroup) groupData).findSignal().getSize() != ((NcGroup) inputGroup).findSignal().getSize())
				isInputChanged = true;
		}
		if (this.entryGroup == null || !(entryGroup.getLocation().equals(this.entryGroup.getLocation())))
			currentIndexVar = 0;
		this.inputGroup = groupData;
		this.entryGroup = entryGroup;
	}

	/**
	 * @param useCorrectedData the useCorrectedData to set
	 */
	public void setUseCorrectedData(Boolean useCorrectedData) {
		this.useCorrectedData = useCorrectedData;
	}

	/**
	 * @param scanVariablePosition the scanVariablePosition to set
	 */
	public void setScanVariablePosition(Position scanVariablePosition) {
		if (scanVariablePosition.getX() != this.scanVariablePosition.getX()){
			this.scanVariablePosition = scanVariablePosition;
			isIndexChanged = true;
		}
	}

	/**
	 * @param stop the stop to set
	 */
	public void setStop(Boolean stop) {
		this.stop = stop;
	}
	
	public IGroup getLoopOut(){
		return entryGroup;
	}

	/**
	 * @return the scanAxis
	 */
	public IDataItem getScanAxis() {
		return scanAxis;
	}

	/**
	 * @param isInLoop the isInLoop to set
	 */
	public void setIsInLoop(Boolean isInLoop) {
		this.isInLoop = isInLoop;
	}
	
	private void updateIsInLoop(boolean isInLoop){
		this.isInLoop = isInLoop;
		informVarValueChange("isInLoop", this.isInLoop);
		System.err.println("set inLoop " + isInLoop);
	}

	/**
	 * @param numberOfSteps the numberOfSteps to set
	 */
	public void setNumberOfSteps(Integer numberOfSteps) {
		this.numberOfSteps = numberOfSteps;
	}

	/**
	 * @param currentStepIndex the currentStepIndex to set
	 */
	public void setCurrentStepIndex(Integer currentStepIndex) {
		this.currentStepIndex = currentStepIndex;
	}
	
	private void updateNumberOfSteps(int numberOfSteps){
		this.numberOfSteps = numberOfSteps;
		informVarValueChange("numberOfSteps", this.numberOfSteps);
	}

	private void updateCurrentStepIndex(int index){
		this.currentStepIndex = index;
		informVarValueChange("currentStepIndex", this.currentStepIndex);
	}

	/**
	 * @return the isInLoop
	 */
	public Boolean getIsInLoop() {
		return isInLoop;
	}

	public Boolean getResetHistory() {
		return resetHistory;
	}

	
}
