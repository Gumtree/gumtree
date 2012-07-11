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
package au.gov.ansto.bragg.kowari.dra.sgf;

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 12/09/2008
 */
public class IntegrationCollection extends ConcreteProcessor {

	private Boolean iof_stop = false;
	private Plot inputPlot;
	private Plot fittingResultPlot;
	private Plot scanResultPlot;
	private Integer currentIndex;
	private IDataItem scanAxis;
	
	private IArray intensityValues;
	private IArray intensityErrors;
	private int numberOfFrames;
	private int numberOfBins = 421;


	
	public IntegrationCollection(){
		super();
	}
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		
		if (inputPlot == null)
			throw new Exception("no input data available");
//		EData<Double> intensity = inputPlot.sum();
		IArray valueSlice = intensityValues.getArrayUtils().slice(0, currentIndex).getArray();
		IArrayIterator sliceIterator = valueSlice.getIterator();
		IArrayIterator dataIterator = inputPlot.findSingal().getData().getIterator();
		while (sliceIterator.hasNext() && dataIterator.hasNext())
			sliceIterator.next().setDoubleCurrent(dataIterator.getDoubleNext());
		IArray varianceSlice = intensityErrors.getArrayUtils().slice(0, currentIndex).getArray();
		sliceIterator = varianceSlice.getIterator();
		dataIterator = inputPlot.getVariance().getData().getIterator();
		while (sliceIterator.hasNext() && dataIterator.hasNext())
			sliceIterator.next().setDoubleCurrent(dataIterator.getDoubleNext());
		
		scanResultPlot = (Plot) PlotFactory.createPlot(inputPlot, "IntensityPlot", DataDimensionType.pattern);
		scanResultPlot.addData("intensityData", intensityValues, "Intensity Integration", 
				"counts", intensityErrors);
		scanResultPlot.addAxis(scanAxis.getShortName(), scanAxis.getData(), scanAxis.getShortName(), 
				scanAxis.getUnitsString(), 0);
		scanResultPlot.addAxis(inputPlot.getAxis(0), 1);
		scanResultPlot.addProcessingLog("regional integration");
//		Array outputArray = scanResultPlot.getSignalArray();
//		int outputSize = (int) outputArray.getSize();
//		if (outputSize == 1){
//			double dataValue = outputArray.getMaximum();
//			double axisValue = scanResultPlot.getAxisArrayList().get(0).getMaximum();
//			double dataVariance = scanResultPlot.findVarianceArray().getMaximum();
//			Axis dataAxis = scanResultPlot.getAxis(0);
//			Plot twinPlot = (Plot) PlotFactory.createPlot(scanResultPlot.getParentGroup(), 
//					scanResultPlot.getShortName(), scanResultPlot.getDimensionType());
//			twinPlot.addData(scanResultPlot.findSingal().getShortName(), 
//					Factory.createArray(new double[]{dataValue, dataValue}), scanResultPlot.findSingal().getTitle(), 
//					scanResultPlot.findSingal().getUnits(), 
//					Factory.createArray(new double[]{dataVariance, dataVariance}));
//			twinPlot.addAxis(dataAxis.getShortName(), Factory.createArray(new double[]{axisValue, 
//					axisValue + axisValue * 1E-7}), 
//					dataAxis.getTitle(), dataAxis.getUnits(), 0);
//			scanResultPlot = twinPlot;
//		}		
		return iof_stop;			
	}



	/**
	 * @return the outputGroup
	 */
	public Plot getScanResultPlot() {
		return scanResultPlot;
	}

	/**
	 * @param iof_stop the iof_stop to set
	 */
	public void setIof_stop(Boolean iof_stop) {
		this.iof_stop = iof_stop;
	}

	/**
	 * @param inputGroup the inputGroup to set
	 */
	public void setInputPlot(Plot inputPlot) {
		this.inputPlot = inputPlot;
	}


//	private void informYAxisChanged(){
//		if (YAxisType.getInstance(yAxisType) == YAxisType.FittingResult)
//			informVarOptionsChange("fitParameter", getFitParameterList());
//		else
//			informVarOptionsChange("fitParameter", null);
//	}

	/**
	 * @param currentIndex the currentIndex to set
	 */
	public void setCurrentIndex(Integer currentIndex) {
		this.currentIndex = currentIndex;
	}

//	/**
//	 * @param numberOfFrames the numberOfFrames to set
//	 */
//	public void setNumberOfFrames(Integer numberOfFrames) {
//		if (numberOfFrames != this.numberOfFrames){
//			resetResult();
//			this.numberOfFrames = numberOfFrames;
//		}
//	}

	
	private void resetResult() {
		intensityValues = Factory.createArray(Double.TYPE, new int[]{numberOfFrames, numberOfBins});
		intensityErrors = Factory.createArray(Double.TYPE, new int[]{numberOfFrames, numberOfBins});
	}

	/**
	 * @param scanAxis the scanAxis to set
	 */
	public void setScanAxis(IDataItem scanAxis) {
		if (numberOfFrames != scanAxis.getSize()){
			numberOfFrames = (int) scanAxis.getSize();
			resetResult();
		}
		this.scanAxis = scanAxis;
	}

	/**
	 * @return the fittingResultPlot
	 */
	public Plot getFittingResultPlot() {
		return fittingResultPlot;
	}

	
}
