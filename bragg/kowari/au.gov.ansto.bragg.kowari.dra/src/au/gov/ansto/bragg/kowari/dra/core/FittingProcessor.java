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
import java.util.Map.Entry;
import java.util.Set;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.core.plot.PlotSet;
import au.gov.ansto.bragg.freehep.jas3.core.Fitter;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 12/09/2008
 */
public class FittingProcessor extends ConcreteProcessor {

	public static final String FITTING_RESULT_NAME = "fittingResult";
	private String fittingProcessor_functionName;
	private Boolean fittingProcessor_skip = false;
	private Boolean fittingProcessor_stop = false;
	private Plot fittingProcessor_inputGroup;
	private Plot fittingProcessor_outputGroup;
	private PlotSet fittingProcessor_fittingResult;
	private Map<String, List<Double>> resultParameterMap;

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		fittingProcessor_outputGroup = fittingProcessor_inputGroup;
		fittingProcessor_fittingResult = (PlotSet) PlotFactory.createPlotSet(fittingProcessor_inputGroup, 
		"fittingResultSet");
		if (fittingProcessor_skip){
			return fittingProcessor_stop;
		}
		resultParameterMap = new HashMap<String, List<Double>>();
		int[] shape = fittingProcessor_inputGroup.findSingal().getShape();
//		double[] fitResult = new double[shape[0]];
		for (int i = 0; i < shape[0]; i++) {
			Plot subPlot = fittingProcessor_inputGroup.slice(0, i);
			Fitter fitter = Fitter.getFitter(fittingProcessor_functionName, subPlot);
			fitter.setResolutionMultiple(1);
			fitter.fit();
//			fitResult[i] = fitter.getParameterValue("mean");
			Map<String, Double> parameters = fitter.getParameters();
			insertToListMap(parameters);
			insertToListMap("Chi2", fitter.getQuality());
			System.out.println(Math.sqrt(2 * Math.PI));
			insertToListMap("area", fitter.getParameterValue("amplitude") * 2.506628 *
					fitter.getParameterValue("sigma"));
			Plot fitResult = createNewPlot(subPlot, (Plot) fitter.getResult());
			fittingProcessor_fittingResult.addPlot(fitResult);
			fitter.reset();
		}
		createResultPlots(shape[1]);
		return fittingProcessor_stop;
	}

	private Plot createNewPlot(Plot rawPlot, Plot fitResult) 
	throws IOException, InvalidArrayTypeException, PlotFactoryException {
		Plot newPlot = (Plot) PlotFactory.createPlot(fittingProcessor_outputGroup, 
				rawPlot.getShortName() + "_fitResult", DataDimensionType.patternset);
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

	private void createResultPlots(int scaler) throws Exception{
		if (resultParameterMap == null || resultParameterMap.size() == 0)
			return;
		for (Entry<String, List<Double>> parameterPair : resultParameterMap.entrySet()) {
			String name = parameterPair.getKey();
			List<Double> values = parameterPair.getValue();
			Plot resultPlot = (Plot) PlotFactory.createPlot(fittingProcessor_inputGroup, name, 
					DataDimensionType.pattern);
			IArray varianceArray = fittingProcessor_inputGroup.findSignalArray().getArrayMath().power(2).sumForDimension(0, true).
			scale(1./scaler).getArray();
			double[] valueArray = new double[values.size()];
			int index = 0;
			for(Double value : values){
				valueArray[index ++] = value;
			}
			resultPlot.addData(name + "_parameter", Factory.createArray(valueArray), 
					"Fitting Parameter " + name, 
					fittingProcessor_inputGroup.getAxis(1).getUnitsString(), varianceArray);
			resultPlot.addAxis(fittingProcessor_inputGroup.getAxis(0));
		}
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

	/**
	 * @return the fittingProcessor_outputGroup
	 */
	public Plot getFittingProcessor_outputGroup() {
		return fittingProcessor_outputGroup;
	}

	/**
	 * @param fittingProcessor_functionName the fittingProcessor_functionName to set
	 */
	public void setFittingProcessor_functionName(
			String fittingProcessor_functionName) {
		this.fittingProcessor_functionName = fittingProcessor_functionName;
	}

	/**
	 * @param fittingProcessor_skip the fittingProcessor_skip to set
	 */
	public void setFittingProcessor_skip(Boolean fittingProcessor_skip) {
		this.fittingProcessor_skip = fittingProcessor_skip;
	}

	/**
	 * @param fittingProcessor_stop the fittingProcessor_stop to set
	 */
	public void setFittingProcessor_stop(Boolean fittingProcessor_stop) {
		this.fittingProcessor_stop = fittingProcessor_stop;
	}

	/**
	 * @param fittingProcessor_inputGroup the fittingProcessor_inputGroup to set
	 */
	public void setFittingProcessor_inputGroup(Plot fittingProcessor_inputGroup) {
		this.fittingProcessor_inputGroup = fittingProcessor_inputGroup;
	}

	/**
	 * @return the fittingProcessor_fittingResult
	 */
	public IGroup getFittingProcessor_fittingResult() {
		return fittingProcessor_fittingResult;
	}

}
