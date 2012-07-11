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
package au.gov.ansto.bragg.wombat.dra.core;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 05/05/2009
 */
public class PlotGenerator extends ConcreteProcessor {

	Plot integrationPlot;
	Double plotDataInput;
	Integer currentIndex;
	Boolean plotSkip = false;
	Boolean plotStop = false;
	Plot generatorPlot1D;
	Plot generatorPlot2D;
	
	private List<Double> historyValues = new ArrayList<Double>();
	private List<IArray> historyArray = new ArrayList<IArray>();
	private boolean resetHistory = false;
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.processor.ConcreteProcessor#process()
	 */
	
	@Override
	public Boolean process() throws Exception {
		if (resetHistory){
			resetHistory = false;
			generatorPlot1D = (Plot) PlotFactory.createPlot("emptyData", DataDimensionType.pattern);
			generatorPlot2D = (Plot) PlotFactory.createPlot("emptyData", DataDimensionType.map);
//			return true;
		}
		if (currentIndex >= historyValues.size()){
			historyValues.add(plotDataInput);
			IArray pattern = integrationPlot.findSignalArray();
			historyArray.add(pattern);
		}else{
			historyValues.set(currentIndex, plotDataInput);
			historyArray.set(currentIndex, integrationPlot.findSignalArray());
		}
		generatePlot();
		return plotStop;
	}
	
	/**
	 * 
	 */
	public PlotGenerator() {
		super();
	}

	private void generatePlot() throws Exception {
		if (plotSkip) {
			return;
		}
		double[] dataStorage = new double[historyValues.size()];
		double[] axisStorage = new double[historyValues.size() == 1 ? 2 : historyValues.size()];
		int i = 0;
		for (Double value : historyValues){
			dataStorage[i] = value;
			axisStorage[i] = i;
			i ++;
		}
		if (dataStorage.length == 1)
			axisStorage[1] = axisStorage[0] + 1;
		IArray dataArray = Factory.createArray(dataStorage);
		IArray indexAxisArray = Factory.createArray(axisStorage);
		String units = "index";
		generatorPlot1D = (Plot) PlotFactory.createPlot("track", DataDimensionType.pattern);
		generatorPlot1D.addData("trackdata", dataArray, "Two-Theta", "");
		generatorPlot1D.addAxis("run_number", indexAxisArray, "run number", units, 0);
		generatorPlot2D = (Plot) PlotFactory.createPlot("strip", DataDimensionType.mapset);
		int size = historyArray.size();
		Axis patternAxis = integrationPlot.getAxis(0);
		IArray resultArray = Factory.createArray(int.class, new int[]{size, 
				(int) integrationPlot.findSignalArray().getSize()});
		IArrayIterator resultIterator = resultArray.getIterator();
		for (IArray array : historyArray){
			IArrayIterator sourceIterator = array.getIterator();
			while(sourceIterator.hasNext()){
				resultIterator.next().setIntCurrent((int) (sourceIterator.getDoubleNext()));
			}
		}
//		double value = 0;
//		while (resultIterator.hasNext()){
//			resultIterator.setDoubleNext(value++);
//		}
//		ArrayIterator iterator = resultArray.getIterator();
//		double value = 0;
//		while (iterator.hasNext())
//			iterator.setDoubleNext(value ++);

		PlotFactory.addDataToPlot(generatorPlot2D, "strip_data", resultArray, "Integration Strips", "counts");
		PlotFactory.addAxisToPlot(generatorPlot2D, "run_number", indexAxisArray, "run number", units, 0);
		PlotFactory.addAxisToPlot(generatorPlot2D, patternAxis.getShortName(), patternAxis.getData(), 
				patternAxis.getTitle(), patternAxis.getUnitsString(), 1);
		generatorPlot2D = PlotFactory.copyTo2DPlot(generatorPlot2D);
	}

	/**
	 * @return the generatorPlot 1D
	 */
	public Plot getGeneratorPlot1D() {
		return generatorPlot1D;
	}

	/**
	 * @return the generatorPlot 2D
	 */
	public Plot getGeneratorPlot2D() {
		return generatorPlot2D;
	}

	/**
	 * @param parentPlot the parentPlot to set
	 */
//	public void setParentPlot(Plot parentPlot) {
//		this.parentPlot = parentPlot;
//	}

	/**
	 * @param plotDataInput the plotDataInput to set
	 */
	public void setPlotDataInput(Double plotDataInput) {
		this.plotDataInput = plotDataInput;
	}

	/**
	 * @param currentIndex the currentIndex to set
	 */
	public void setCurrentIndex(Integer currentIndex) {
		this.currentIndex = currentIndex;
	}

	/**
	 * @param plotSkip the plotSkip to set
	 */
	public void setPlotSkip(Boolean plotSkip) {
		this.plotSkip = plotSkip;
	}

	/**
	 * @param plotStop the plotStop to set
	 */
	public void setPlotStop(Boolean plotStop) {
		this.plotStop = plotStop;
	}

	/**
	 * @param historyValues the historyValues to set
	 */
	public void setHistoryValues(List<Double> historyValues) {
		this.historyValues = historyValues;
	}

	public void setResetHistory(Boolean resetHistory) {
		this.resetHistory = resetHistory;
		if (resetHistory){
			plotDataInput = null;
			currentIndex = 0;
			generatorPlot1D = null;
			historyValues.clear();
			historyArray.clear();
		}
	}

	/**
	 * @param integrationPlot the integrationPlot to set
	 */
	public void setIntegrationPlot(Plot integrationPlot) {
		this.integrationPlot = integrationPlot;
	}

	public DataStructureType getDataStructureType() {
		return DataStructureType.plot;
	}
	public DataDimensionType getDataDimensionType() {
		return DataDimensionType.map;
	}
}
