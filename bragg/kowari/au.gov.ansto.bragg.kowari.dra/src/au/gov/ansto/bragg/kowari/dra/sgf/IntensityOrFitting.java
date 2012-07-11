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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.math.EData;
import org.gumtree.data.math.EMath;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.freehep.jas3.core.Fitter;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 12/09/2008
 */
public class IntensityOrFitting extends ConcreteProcessor {

	private Boolean iof_stop = false;
	private Plot inputPlot;
	private Plot fittingResultPlot;
	private Plot scanResultPlot;
	private String yAxisType = "Intensity";
//	private String fitParameter = "mean";
	private String functionName = "Gaussian";
	private Integer currentIndex;
	private IDataItem scanAxis;
	
	private Map<String, double[]> resultParameterMap;
	private Map<String, double[]> parameterErrorMap;
	private double[] intensityValues;
	private double[] intensityErrors;
	private static List<String> fitParameterList;
	private int numberOfFrames;

	private enum YAxisType{
		Intensity ("Intensity"),
		mean ("mean"),
		Chi2 ("Chi2"),
		amplitude ("amplitude"),
		sigma ("sigma"),
		area ("area");
		
		public static YAxisType getInstance(String value){
			for (YAxisType type : YAxisType.values())
				if (value.equals(type.value))
					return type;
			return Intensity;
		}
		
		private String value;

		YAxisType(String value){
			this.value = value;
		}
		
		public String getValue(){
			return value;
		}
	}
	
	private static List<String> getFitParameterList(){
		if (fitParameterList == null){
			fitParameterList = new ArrayList<String>();
			fitParameterList.add("mean");
			fitParameterList.add("Chi2");
			fitParameterList.add("amplitude");
			fitParameterList.add("sigma");
			fitParameterList.add("area");
		}
		return fitParameterList;
	}
	
	public IntensityOrFitting(){
		super();
	}
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		
		if (inputPlot == null)
			throw new Exception("no input data available");
		YAxisType yType = YAxisType.getInstance(yAxisType);
		int[] shape = inputPlot.findSingal().getShape();
//		Axis axis0 = inputPlot.getAxis(0);

//		if (yType == YAxisType.FittingResult){
//			double[] fitResult = new double[shape[0]];
			//			for (int i = 0; i < shape[0]; i++) {
//			Plot subPlot = inputPlot.slice(0, i);
//			subPlot.addCalculationData("scanVariable", axis0.getData().section(
//					new int[]{i}, new int[]{1}), axis0.getTitle(), axis0.getUnits(), null);
		
		Fitter fitter = Fitter.getFitter(functionName, inputPlot);
		fitter.setResolutionMultiple(1);
		fitter.fit();
		//				fitResult[i] = fitter.getParameterValue("mean");
		Map<String, Double> parameters = fitter.getParameters();
		Map<String, Double> errors = fitter.getFitErrors();
		insertToListMap(parameters);
		insertToErrorMap(errors);
		insertToListMap("Chi2", fitter.getQuality());
		insertToErrorMap("Chi2", 0.);
		insertToListMap("area", fitter.getParameterValue("amplitude") * 2.506628 *
				fitter.getParameterValue("sigma"));
		insertToErrorMap("area", EMath.scalarMultiply(fitter.getParameterValue("amplitude"), 
				fitter.getParameterValue("sigma"), Math.pow(fitter.getFitError("amplitude"), 2), 
				Math.pow(fitter.getFitError("sigma"), 2)).getVariance() * 6.283184);
		fittingResultPlot = createNewPlot(inputPlot, (Plot) fitter.getResult());
		fitter.reset();
		//			}
		EData<Double> intensity = inputPlot.sum();
		intensityValues[currentIndex] = intensity.getData();
		IArray intensityArray = Factory.createArray(intensityValues);
		intensityErrors[currentIndex] = intensity.getVariance();
		IArray intensityVariance = Factory.createArray(intensityErrors);
		if (yType == YAxisType.Intensity){
			scanResultPlot = (Plot) PlotFactory.createPlot(inputPlot, "IntensityPlot", DataDimensionType.pattern);
			scanResultPlot.addData("intensityData", intensityArray, "Intensity Integration", 
					"counts", intensityVariance);
			scanResultPlot.addAxis(scanAxis.getShortName(), scanAxis.getData(), scanAxis.getShortName(), 
					scanAxis.getUnitsString(), 0);
			scanResultPlot.addProcessingLog("regional integration");
		}else{
			scanResultPlot = createResultPlots(yAxisType, 0);
			scanResultPlot.addProcessingLog("fitted with " + functionName + " function");
		}
		scanResultPlot.addCalculationData("intensity", intensityArray, 
				"intensity integration", "counts", intensityVariance);
		for (String key : resultParameterMap.keySet()){
			double[] errorValues = parameterErrorMap.get(key);
			IArray varianceArray = Factory.createArray(errorValues);
			double[] values = resultParameterMap.get(key);
			IArray dataArray = Factory.createArray(values);
			scanResultPlot.addCalculationData(key, dataArray, 
					"Fitting parameter " + key, 
					"", varianceArray);
		}
//		}else {
//			for (int i = 0; i < shape[0]; i++) {
//				Plot subPlot = inputPlot.slice(0, i);
//			outputPlot = (Plot) PlotFactory.createPlot("inte", 
//					DataDimensionType.patternset);
//			Array dataArray = inputPlot.findSignalArray();
//			Array varianceArray = inputPlot.findVarianceArray();
//			Axis axis1 = inputPlot.getAxis(0);
//			Array newArray = Factory.createArray(Double.TYPE, new int[]{2, (int) dataArray.getSize()});
//			ArrayIterator newIterator = newArray.getIterator();
//			ArrayIterator dataIterator = dataArray.getIterator();
//			while (dataIterator.hasNext()) {
//				//					double value = dataIterator.getDoubleNext();
//				//					if (Double.isNaN(value))
//				//						newIterator.setDoubleNext(0);
//				//					else
//				//						newIterator.setDoubleNext(value);
//				newIterator.setDoubleNext(dataIterator.getDoubleNext());
//			}
//			dataIterator = dataArray.getIterator();
//			while (dataIterator.hasNext()) {
//				//					double value = dataIterator.getDoubleNext();
//				//					if (Double.isNaN(value))
//				//						newIterator.setDoubleNext(0);
//				//					else
//				//						newIterator.setDoubleNext(value);
//				newIterator.setDoubleNext(dataIterator.getDoubleNext());
//			}
//			Array newVariance = Factory.createArray(Double.TYPE, new int[]{2, (int) varianceArray.getSize()});
//			ArrayIterator newVarIterator = newVariance.getIterator();
//			ArrayIterator dataVarIterator = varianceArray.getIterator();
//			while (dataVarIterator.hasNext()) {
//				//					double value = dataVarIterator.getDoubleNext();
//				//					if (Double.isNaN(value))
//				//						newVarIterator.setDoubleNext(0);
//				//					else
//				//						newVarIterator.setDoubleNext(value);
//				newVarIterator.setDoubleNext(dataVarIterator.getDoubleNext());
//			}
//			dataIterator = dataArray.getIterator();
//			while (dataVarIterator.hasNext()) {
//				//					double value = dataVarIterator.getDoubleNext();
//				//					if (Double.isNaN(value))
//				//						newVarIterator.setDoubleNext(0);
//				//					else
//				//						newVarIterator.setDoubleNext(value);
//				newVarIterator.setDoubleNext(dataVarIterator.getDoubleNext());
//			}				
//			patternset.addData(subPlot.findSingal().getShortName() + i + "Data", newArray, 
//					subPlot.findSingal().getTitle(), subPlot.getDataUnits(), newVariance);
//			patternset.addCalculationData("scanVariable", axis0.getData().section(
//					new int[]{i}, new int[]{1}), axis0.getTitle(), axis0.getUnits(), null);
//			patternset.addAxis("patterns", Factory.createArray(new double[]{0, 1}), "pattern", "", 0);
//			patternset.addAxis(axis1.getShortName(), axis1.getData(), axis1.getTitle(), axis1.getUnits(), 1);
//			patternset.addLog("duplicate 1D pattern");
//			//				plotSet.addPlot(patternset);
//			//			}
//			outputPlot = inputPlot.enclosedSumForDimension(0);
////			outputGroup.setLocation(inputGroup.getLocation());
//			outputPlot.setShortName("IntensityPlot");
//			outputPlot.findSingal().setTitle("Intensity Integration");
//			outputPlot.findSingal().setUnits("counts");
//			outputPlot.addProcessingLog("regional integration");
//		}
		IArray outputArray = ((NcGroup) scanResultPlot).getSignalArray();
		int outputSize = (int) outputArray.getSize();
		if (outputSize == 1){
			double dataValue = outputArray.getArrayMath().getMaximum();
			double axisValue = scanResultPlot.getAxisArrayList().get(0).getArrayMath().getMaximum();
			double dataVariance = scanResultPlot.findVarianceArray().getArrayMath().getMaximum();
			Axis dataAxis = scanResultPlot.getAxis(0);
			Plot twinPlot = (Plot) PlotFactory.createPlot(scanResultPlot.getParentGroup(), 
					scanResultPlot.getShortName(), scanResultPlot.getDimensionType());
			twinPlot.addData(scanResultPlot.findSingal().getShortName(), 
					Factory.createArray(new double[]{dataValue, dataValue}), scanResultPlot.findSingal().getTitle(), 
					scanResultPlot.findSingal().getUnitsString(), 
					Factory.createArray(new double[]{dataVariance, dataVariance}));
			twinPlot.addAxis(dataAxis.getShortName(), Factory.createArray(new double[]{axisValue, 
					axisValue + axisValue * 1E-7}), 
					dataAxis.getTitle(), dataAxis.getUnitsString(), 0);
			twinPlot.addCalculationData("intensity", intensityArray, 
					"intensity integration", "counts", intensityVariance);
			for (String key : resultParameterMap.keySet()){
				double[] errorValues = parameterErrorMap.get(key);
				IArray varianceArray = Factory.createArray(errorValues);
				double[] values = resultParameterMap.get(key);
				IArray dataArray = Factory.createArray(values);
				twinPlot.addCalculationData(key, dataArray, 
						"Fitting parameter " + key, 
						"", varianceArray);
			}
			scanResultPlot = twinPlot;
		}
		return iof_stop;			
	}

	private Plot createNewPlot(Plot rawPlot, Plot fitResult) 
	throws IOException, InvalidArrayTypeException, PlotFactoryException {
		Plot newPlot = (Plot) PlotFactory.createPlot(inputPlot, 
				inputPlot.getShortName() + "_fit", DataDimensionType.patternset);
		IArray rawArray = rawPlot.findSignalArray();
		IArray fitArray = fitResult.findSignalArray();
		IArray newArray = Factory.createArray(Double.TYPE, new int[]{2, 
				(int) rawPlot.findSingal().getSize()});
		IArrayIterator newIterator = newArray.getIterator();
		IArrayIterator rawIterator = rawArray.getIterator();
		IArrayIterator fitIterator = fitArray.getIterator();
		while (fitIterator.hasNext() && newIterator.hasNext())
			newIterator.next().setDoubleCurrent(fitIterator.getDoubleNext());
		while (rawIterator.hasNext() && newIterator.hasNext()){
			double value = rawIterator.getDoubleNext();
//			if (Double.isNaN(value))
//				value = 0;
			newIterator.next().setDoubleCurrent(value);
		}
		newPlot.addData(rawPlot.getShortName() + "_data", newArray, "Raw Data & Fit Result", 
				rawPlot.getDataUnits());
		
		IArray rawVariance = rawPlot.findVarianceArray();
		IArray fitVariance = fitResult.findVarianceArray();
		IArray newVariance = Factory.createArray(Double.TYPE, newArray.getShape());
		IArrayIterator newVarIterator = newVariance.getIterator();
		if (fitVariance != null){
			IArrayIterator fitVarIterator = fitVariance.getIterator();
			while (fitVarIterator.hasNext() && newVarIterator.hasNext())
				newVarIterator.next().setDoubleCurrent(fitVarIterator.getDoubleNext());
		}
		if (rawVariance != null){
			IArrayIterator rawVarIterator = rawVariance.getIterator();
			while (rawVarIterator.hasNext() && newVarIterator.hasNext())
				newVarIterator.next().setDoubleCurrent(rawVarIterator.getDoubleNext());
		}
		newPlot.addData(rawPlot.getShortName() + "_data", newArray, "Raw Data & Fit Result", 
				rawPlot.getDataUnits(), newVariance);
		newPlot.addAxis("id", Factory.createArray(new double[]{0, 1}), "Name", "", 0);
		newPlot.addAxis(rawPlot.getAxis(0), 1);
		return newPlot;
	}

	private void insertToListMap(String name, double value) {
		if (!resultParameterMap.containsKey(name)){
			double[] values = new double[numberOfFrames];
			values[currentIndex] = value;
			resultParameterMap.put(name, values);
		}else{
			double[] values = resultParameterMap.get(name);
			values[currentIndex] = value;
		}
	}

	private void insertToErrorMap(String name, double value) {
		if (!parameterErrorMap.containsKey(name)){
			double[] values = new double[numberOfFrames]; 
			values[currentIndex] = value;
			parameterErrorMap.put(name, values);
		}else{
			double[] values = parameterErrorMap.get(name);
			values[currentIndex] = value;
		}
	}

	private Plot createResultPlots(String fittingParameter, int scaler) throws Exception{
		if (resultParameterMap == null || resultParameterMap.size() == 0)
			throw new Exception("the fitting result is not available");
		Plot resultPlot = (Plot) PlotFactory.createPlot(inputPlot, fittingParameter, 
				DataDimensionType.pattern);
//		Array varianceArray = inputGroup.findVarianceArray().sumForDimension(0).
//				scale(1./scaler);
		double[] errors = parameterErrorMap.get(fittingParameter);
		IArray varianceArray = Factory.createArray(errors);
		double[] values = resultParameterMap.get(fittingParameter);
		IArray dataArray = Factory.createArray(values);
		resultPlot.addData(fittingParameter + "_parameter", dataArray, 
				"Fitting parameter " + fittingParameter, 
				inputPlot.getAxis(0).getUnitsString(), varianceArray);
		resultPlot.addAxis(scanAxis.getShortName(), scanAxis.getData(), scanAxis.getShortName(), 
				scanAxis.getUnitsString(), 0);
		return resultPlot;
	}

	private void insertToListMap(Map<String, Double> parameters) {
		for (Entry<String, Double> entry : parameters.entrySet())
			insertToListMap(entry.getKey(), entry.getValue());
	}

	private void insertToErrorMap(Map<String, Double> errors) {
		for (Entry<String, Double> entry : errors.entrySet())
			insertToErrorMap(entry.getKey(), entry.getValue());
	}

	/**
	 * @return the outputGroup
	 */
	public Plot getScanResultPlot() {
		return scanResultPlot;
	}

	/**
	 * @param functionName the functionName to set
	 */
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
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

	/**
	 * @param axisType the yAxisType to set
	 */
	public void setYAxisType(String axisType) {
		yAxisType = axisType;
//		informYAxisChanged();
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
		intensityValues = new double[numberOfFrames];
		intensityErrors = new double[numberOfFrames];
		if (parameterErrorMap == null){
			parameterErrorMap = new HashMap<String, double[]>();
			for (String key : getFitParameterList())
				parameterErrorMap.put(key, new double[numberOfFrames]);
		}else
			for (String key : parameterErrorMap.keySet())
				parameterErrorMap.put(key, new double[numberOfFrames]);
			
		if (resultParameterMap == null){
			resultParameterMap = new HashMap<String, double[]>();
			for (String key : getFitParameterList())
				resultParameterMap.put(key, new double[numberOfFrames]);
		}else
			for (String key : resultParameterMap.keySet())
				resultParameterMap.put(key, new double[numberOfFrames]);
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
