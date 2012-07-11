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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.math.EMath;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.core.plot.PlotSet;
import au.gov.ansto.bragg.freehep.jas3.core.Fitter;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 12/09/2008
 */
public class IntensityOrFitting extends ConcreteProcessor {

	private Boolean iof_stop = false;
	private Plot inputMap;
	private Plot inputGroup;
	private PlotSet plotSet;
	private Plot outputMap;
	private Plot outputGroup;
	private String yAxisType = "Intensity";
	private String fitParameter = "mean";
	private String functionName = "Gaussian";
	
	private Map<String, List<Double>> resultParameterMap;
	private Map<String, List<Double>> parameterErrorMap;
	private static List<String> fitParameterList;

	private enum YAxisType{
		Intensity ("Intensity"),
		FittingResult ("Fitting Result");
		
		public static YAxisType getInstance(String value){
			if (value.equals(Intensity.value))
				return Intensity;
			if (value.equals(FittingResult.value))
				return FittingResult;
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
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		
		if (inputGroup == null)
			throw new Exception("no input data available");
		outputMap = inputMap;
		YAxisType yType = YAxisType.getInstance(yAxisType);
		plotSet = (PlotSet) PlotFactory.createPlotSet(inputGroup, yAxisType);
		int[] shape = inputGroup.findSingal().getShape();
		Axis axis0 = inputGroup.getAxis(0);

		if (yType == YAxisType.FittingResult){
			resultParameterMap = new HashMap<String, List<Double>>();
			parameterErrorMap = new HashMap<String, List<Double>>();
//			double[] fitResult = new double[shape[0]];
			for (int i = 0; i < shape[0]; i++) {
				Plot subPlot = inputGroup.slice(0, i);
				subPlot.addCalculationData("scanVariable", axis0.getData().getArrayUtils().section(
						new int[]{i}, new int[]{1}).getArray(), axis0.getTitle(), axis0.getUnitsString(), null);
				Fitter fitter = Fitter.getFitter(functionName, subPlot);
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
				Plot fitResult = createNewPlot(subPlot, (Plot) fitter.getResult(), i);
				plotSet.addPlot(fitResult);
				fitter.reset();
			}
			outputGroup = createResultPlots(fitParameter, shape[1]);
			outputGroup.addProcessingLog("fitted with " + functionName + " function");
		}else {
			for (int i = 0; i < shape[0]; i++) {
				Plot subPlot = inputGroup.slice(0, i);
				Plot patternset = (Plot) PlotFactory.createPlot(plotSet, "slice" + i, 
						DataDimensionType.patternset);
				IArray dataArray = subPlot.findSignalArray();
				IArray varianceArray = subPlot.findVarianceArray();
				Axis axis1 = subPlot.getAxis(0);
				IArray newArray = Factory.createArray(Double.TYPE, new int[]{2, (int) dataArray.getSize()});
				IArrayIterator newIterator = newArray.getIterator();
				IArrayIterator dataIterator = dataArray.getIterator();
				while (dataIterator.hasNext()) {
//					double value = dataIterator.getDoubleNext();
//					if (Double.isNaN(value))
//						newIterator.setDoubleNext(0);
//					else
//						newIterator.setDoubleNext(value);
					newIterator.next().setDoubleCurrent(dataIterator.getDoubleNext());
				}
				dataIterator = dataArray.getIterator();
				while (dataIterator.hasNext()) {
//					double value = dataIterator.getDoubleNext();
//					if (Double.isNaN(value))
//						newIterator.setDoubleNext(0);
//					else
//						newIterator.setDoubleNext(value);
					newIterator.next().setDoubleCurrent(dataIterator.getDoubleNext());
				}
				IArray newVariance = Factory.createArray(Double.TYPE, new int[]{2, (int) varianceArray.getSize()});
				IArrayIterator newVarIterator = newVariance.getIterator();
				IArrayIterator dataVarIterator = varianceArray.getIterator();
				while (dataVarIterator.hasNext()) {
//					double value = dataVarIterator.getDoubleNext();
//					if (Double.isNaN(value))
//						newVarIterator.setDoubleNext(0);
//					else
//						newVarIterator.setDoubleNext(value);
					newVarIterator.next().setDoubleCurrent(dataVarIterator.getDoubleNext());
				}
				dataIterator = dataArray.getIterator();
				while (dataVarIterator.hasNext()) {
//					double value = dataVarIterator.getDoubleNext();
//					if (Double.isNaN(value))
//						newVarIterator.setDoubleNext(0);
//					else
//						newVarIterator.setDoubleNext(value);
					newVarIterator.next().setDoubleCurrent(dataVarIterator.getDoubleNext());
				}				
				patternset.addData(subPlot.findSingal().getShortName() + i + "Data", newArray, 
						subPlot.findSingal().getTitle(), subPlot.getDataUnits(), newVariance);
				patternset.addCalculationData("scanVariable", axis0.getData().getArrayUtils().section(
						new int[]{i}, new int[]{1}).getArray(), axis0.getTitle(), axis0.getUnitsString(), null);
				patternset.addAxis("patterns", Factory.createArray(new double[]{0, 1}), "pattern", "", 0);
				patternset.addAxis(axis1.getShortName(), axis1.getData(), axis1.getTitle(), axis1.getUnitsString(), 1);
				((NcGroup) patternset).addLog("duplicate 1D pattern");
//				plotSet.addPlot(patternset);
			}
			outputGroup = inputGroup.enclosedSumForDimension(0);
//			outputGroup.setLocation(inputGroup.getLocation());
			outputGroup.setShortName("IntensityPlot");
			outputGroup.findSingal().setTitle("Intensity Integration");
			outputGroup.findSingal().setUnits("counts");
			outputGroup.addProcessingLog("regional integration");
		}
		IArray outputArray = ((NcGroup) outputGroup).getSignalArray();
		int outputSize = (int) outputArray.getSize();
		if (outputSize == 1){
			double dataValue = outputArray.getArrayMath().getMaximum();
			double axisValue = outputGroup.getAxisArrayList().get(0).getArrayMath().getMaximum();
			double dataVariance = outputGroup.findVarianceArray().getArrayMath().getMaximum();
			Axis dataAxis = outputGroup.getAxis(0);
			Plot twinPlot = (Plot) PlotFactory.createPlot(outputGroup.getParentGroup(), 
					outputGroup.getShortName(), outputGroup.getDimensionType());
			twinPlot.addData(outputGroup.findSingal().getShortName(), 
					Factory.createArray(new double[]{dataValue, dataValue}), outputGroup.findSingal().getTitle(), 
					outputGroup.findSingal().getUnitsString(), 
					Factory.createArray(new double[]{dataVariance, dataVariance}));
			twinPlot.addAxis(dataAxis.getShortName(), Factory.createArray(new double[]{axisValue, 
					axisValue + axisValue * 1E-7}), 
					dataAxis.getTitle(), dataAxis.getUnitsString(), 0);
			outputGroup = twinPlot;
		}
		return iof_stop;			
	}

	private Plot createNewPlot(Plot rawPlot, Plot fitResult, int index) 
	throws IOException, InvalidArrayTypeException, PlotFactoryException {
		Plot newPlot = (Plot) PlotFactory.createPlot(inputGroup, 
				rawPlot.getShortName() + index, DataDimensionType.patternset);
		IArray rawArray = rawPlot.findSignalArray();
		IArray fitArray = fitResult.findSignalArray();
		IArray newArray = Factory.createArray(Double.TYPE, new int[]{2, 
				(int) rawPlot.findSingal().getSize()});
		IArrayIterator newIterator = newArray.getIterator();
		IArrayIterator rawIterator = rawArray.getIterator();
		IArrayIterator fitIterator = fitArray.getIterator();
		while (rawIterator.hasNext() && newIterator.hasNext()){
			double value = rawIterator.getDoubleNext();
			if (Double.isNaN(value))
				value = 0;
			newIterator.next().setDoubleCurrent(value);
		}
		while (fitIterator.hasNext() && newIterator.hasNext())
			newIterator.next().setDoubleCurrent(fitIterator.getDoubleNext());
		newPlot.addData(rawPlot.getShortName() + "_data", newArray, "Raw Data & Fit Result", 
				rawPlot.getDataUnits());
		
		IArray rawVariance = rawPlot.findVarianceArray();
		IArray fitVariance = fitResult.findVarianceArray();
		IArray newVariance = Factory.createArray(Double.TYPE, newArray.getShape());
		IArrayIterator newVarIterator = newVariance.getIterator();
		if (rawVariance != null){
			IArrayIterator rawVarIterator = rawVariance.getIterator();
			while (rawVarIterator.hasNext() && newVarIterator.hasNext())
				newVarIterator.next().setDoubleCurrent(rawVarIterator.getDoubleNext());
		}
		if (fitVariance != null){
			IArrayIterator fitVarIterator = fitVariance.getIterator();
			while (fitVarIterator.hasNext() && newVarIterator.hasNext())
				newVarIterator.next().setDoubleCurrent(fitVarIterator.getDoubleNext());
		}
		newPlot.addData(rawPlot.getShortName() + "_data", newArray, "Raw Data & Fit Result", 
				rawPlot.getDataUnits(), newVariance);
		newPlot.addAxis("id", Factory.createArray(new double[]{0, 1}), "Name", "", 0);
		newPlot.addAxis(rawPlot.getAxis(0), 1);
		return newPlot;
	}

	private void insertToListMap(String name, double value) {
		if (!resultParameterMap.containsKey(name)){
			List<Double> values = new ArrayList<Double>();
			values.add(value);
			resultParameterMap.put(name, values);
		}else{
			List<Double> values = resultParameterMap.get(name);
			values.add(value);
		}
	}

	private void insertToErrorMap(String name, double value) {
		if (!parameterErrorMap.containsKey(name)){
			List<Double> values = new ArrayList<Double>();
			values.add(value);
			parameterErrorMap.put(name, values);
		}else{
			List<Double> values = parameterErrorMap.get(name);
			values.add(value);
		}
	}

	private Plot createResultPlots(String fittingParameter, int scaler) throws Exception{
		if (resultParameterMap == null || resultParameterMap.size() == 0)
			throw new Exception("the fitting result is not available");
		List<Double> values = resultParameterMap.get(fittingParameter);
		Plot resultPlot = (Plot) PlotFactory.createPlot(inputGroup, fittingParameter, 
				DataDimensionType.pattern);
//		Array varianceArray = inputGroup.findVarianceArray().sumForDimension(0).
//				scale(1./scaler);
		List<Double> errorArray = parameterErrorMap.get(fittingParameter);
		IArray varianceArray = Factory.createArray(Double.TYPE, new int[]{errorArray.size()});
		IArrayIterator varianceIterator = varianceArray.getIterator();
		for (Double value : errorArray){
			varianceIterator.next().setDoubleCurrent(value);
		}
		double[] valueArray = new double[values.size()];
		int index = 0;
		for(Double value : values){
			valueArray[index ++] = value;
		}
		resultPlot.addData(fittingParameter + "_parameter", Factory.createArray(valueArray), 
				"Fitting parameter " + fittingParameter, 
				inputGroup.getAxis(1).getUnitsString(), varianceArray);
		resultPlot.addAxis(inputGroup.getAxis(0));
		return resultPlot;
	}

	private void insertToListMap(Map<String, Double> parameters) {
		if (resultParameterMap == null)
			resultParameterMap = new HashMap<String, List<Double>>();
		if (parameters == null || parameters.size() == 0)
			return;
		Set<String> parameterNames = parameters.keySet();
		for (String name : parameterNames){
			if (!resultParameterMap.containsKey(name)){
				List<Double> values = new ArrayList<Double>();
				values.add(parameters.get(name));
				resultParameterMap.put(name, values);
			}else{
				List<Double> values = resultParameterMap.get(name);
				values.add(parameters.get(name));
			}
		}
	}

	private void insertToErrorMap(Map<String, Double> errors) {
		if (parameterErrorMap == null)
			parameterErrorMap = new HashMap<String, List<Double>>();
		if (errors == null || errors.size() == 0)
			return;
		Set<String> parameterNames = errors.keySet();
		for (String name : parameterNames){
			if (!parameterErrorMap.containsKey(name)){
				List<Double> values = new ArrayList<Double>();
				values.add(Math.pow(errors.get(name), 2));
				parameterErrorMap.put(name, values);
			}else{
				List<Double> values = parameterErrorMap.get(name);
				values.add(Math.pow(errors.get(name), 2));
			}
		}
	}

	/**
	 * @return the outputGroup
	 */
	public Plot getOutputGroup() {
		return outputGroup;
	}

	/**
	 * @return the plotSet
	 */
	public PlotSet getPlotSet() {
		return plotSet;
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
	public void setInputGroup(Plot inputGroup) {
		this.inputGroup = inputGroup;
	}

	/**
	 * @param axisType the yAxisType to set
	 */
	public void setYAxisType(String axisType) {
		yAxisType = axisType;
//		informYAxisChanged();
	}

	private void informYAxisChanged(){
		if (YAxisType.getInstance(yAxisType) == YAxisType.FittingResult)
			informVarOptionsChange("fitParameter", getFitParameterList());
		else
			informVarOptionsChange("fitParameter", null);
	}
	
	/**
	 * @param fitParameter the fitParameter to set
	 */
	public void setFitParameter(String fitParameter) {
		this.fitParameter = fitParameter;
	}

	/**
	 * @return the outputMap
	 */
	public Plot getOutputMap() {
		return outputMap;
	}

	/**
	 * @param inputMap the inputMap to set
	 */
	public void setInputMap(Plot inputMap) {
		this.inputMap = inputMap;
	}



}
