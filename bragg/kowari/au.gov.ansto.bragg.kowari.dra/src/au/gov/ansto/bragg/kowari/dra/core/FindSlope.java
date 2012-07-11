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

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.math.EData;
import org.gumtree.data.math.EMath;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.freehep.jas3.core.Fitter;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 24/11/2008
 */
public class FindSlope extends ConcreteProcessor {

	private Plot oneDPlot;
	private Plot slopeOutput;
	private Double minSDD = 1200.;
	private Double maxSDD = 1500.;
	private Double step = 10.;
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		// TODO Auto-generated method stub
		IArray stthArray = oneDPlot.getAxisArrayList().get(0);
		IArray peakArray = oneDPlot.findSignalArray();
		int numberOfStep = (int) ((maxSDD - minSDD) / step);
		IArray sddAxisArray = Factory.createArray(Double.TYPE, new int[]{numberOfStep});
		IArray slopeArray = Factory.createArray(Double.TYPE, new int[]{numberOfStep});
		IArray slopeVarianceArray = Factory.createArray(Double.TYPE, new int[]{numberOfStep});
		IArray varianceArray = Factory.createArray(Double.TYPE, new int[]{numberOfStep});
		int stthSize = (int) stthArray.getSize();
		IIndex slopeIndex = slopeArray.getIndex();
		IIndex stthIndex = stthArray.getIndex();
		int rangeMin = (int) (stthSize * 0.25);
		int rangeMax = (int) (stthSize * 0.75);
		IArray rangeAxisArray = Factory.createArray(Double.TYPE, new int[]{rangeMax - rangeMin + 1});
		IArray rangeDataArray = Factory.createArray(Double.TYPE, new int[]{rangeMax - rangeMin + 1});
		IArrayIterator rangeAxisIterator = rangeAxisArray.getIterator();
		for (int j = rangeMin; j <= rangeMax; j ++){
			stthIndex.set0(j);
			rangeAxisIterator.next().setDoubleCurrent(stthArray.getDouble(stthIndex));
		}
		for (int i = 0; i < numberOfStep; i ++){
			slopeIndex.set0(i);
			double sdd = minSDD + i * step;
			sddAxisArray.setDouble(slopeIndex, sdd);
			IArrayIterator rangeDataIterator = rangeDataArray.getIterator();
			for (int j = rangeMin; j <= rangeMax; j ++){
				stthIndex.set0(j);
				rangeDataIterator.next().setDoubleCurrent(getAngle(stthArray.getDouble(stthIndex), 
						peakArray.getDouble(stthIndex), sdd));
			}
			double average = rangeDataArray.getArrayMath().sum() / rangeDataArray.getSize();
			double variance = 0;
			rangeDataIterator = rangeDataArray.getIterator();
			while (rangeDataIterator.hasNext())
				variance += Math.pow(rangeDataIterator.getDoubleNext() - average, 2);
			Plot rangePlot = (Plot) PlotFactory.createPlot("range", DataDimensionType.pattern);
			rangePlot.addData("rangeData", rangeDataArray, "Two Theta", "degree", null);
			rangePlot.addAxis("stth", rangeAxisArray, "STTH", "degree", 0);
			Fitter fitter = Fitter.getFitter("Linear", rangePlot);
			fitter.setResolutionMultiple(3);
			fitter.fit();
			double slope = fitter.getParameterValue("a");
			double error = fitter.getFitError("a");
			slopeArray.setDouble(slopeIndex, slope);
			varianceArray.setDouble(slopeIndex, variance);
			slopeVarianceArray.setDouble(slopeIndex, error);
			fitter.reset();
		}
		slopeOutput = (Plot) PlotFactory.createPlot(oneDPlot, "Align SDD", DataDimensionType.pattern);
		slopeOutput.addData("slope", slopeArray, "Slope", "", slopeVarianceArray);
		slopeOutput.addAxis("sdd", sddAxisArray, "Sample to Detector Distance", "mm", 0);
		Fitter slopeFitter = Fitter.getFitter("Linear", slopeOutput);
		slopeFitter.setResolutionMultiple(1);
		slopeFitter.fit();
		double a = slopeFitter.getParameterValue("a");
		double aVariance = slopeFitter.getFitError("a");
		double b = slopeFitter.getParameterValue("b");
		double bVariance = slopeFitter.getFitError("b");
		EData<Double> cut = EMath.scalarDivide(-b, a, bVariance * bVariance, aVariance * aVariance);
		slopeOutput.addCalculationData("cut", Factory.createArray(new double[]{cut.getData(), 
				cut.getVariance()}), "Cut at zero", "mm", null);
		slopeFitter.reset();
		Plot variancePlot = (Plot) PlotFactory.createPlot(oneDPlot, "variance", DataDimensionType.pattern);
		variancePlot.addData("varianceData", varianceArray, "Variance", "mm2", null);
		variancePlot.addAxis("sdd", sddAxisArray, "Sample to Detector Distance", "mm", 0);
		Fitter varianceFitter = Fitter.getFitter("Quadratic", variancePlot);
		varianceFitter.fit();
		double qa = varianceFitter.getParameterValue("a");
		double qb = varianceFitter.getParameterValue("b");
		double qaVariance = varianceFitter.getFitError("a");
		double qbVariance = varianceFitter.getFitError("b");
		EData<Double> minVariance = EMath.scalarDivide(-qb, 2 * qa, qbVariance * qbVariance, 
				qaVariance * qaVariance * 4);
		variancePlot.addCalculationData("minimum", Factory.createArray(new double[]{minVariance.getData(), 
				minVariance.getVariance()}), "Minimum", "mm", null);
		slopeOutput = variancePlot;
		return false;
	}

	private double getAngle(double stth, double binCenter, double sampleToDetector) {
		// TODO Auto-generated method stub
		
		return stth + Math.atan(binCenter / sampleToDetector) * 180 / Math.PI;
	}

	/**
	 * @return the slopeOutput
	 */
	public Plot getSlopeOutput() {
		return slopeOutput;
	}

	/**
	 * @param oneDPlot the oneDPlot to set
	 */
	public void setOneDPlot(Plot oneDPlot) {
		this.oneDPlot = oneDPlot;
	}

	/**
	 * @param minSDD the minSDD to set
	 */
	public void setMinSDD(Double minSDD) {
		this.minSDD = minSDD;
	}

	/**
	 * @param maxSDD the maxSDD to set
	 */
	public void setMaxSDD(Double maxSDD) {
		this.maxSDD = maxSDD;
	}

	/**
	 * @param step the step to set
	 */
	public void setStep(Double step) {
		this.step = step;
	}

}
