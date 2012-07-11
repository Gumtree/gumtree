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
package au.gov.ansto.bragg.quokka.dra.core;

import java.io.IOException;

import javax.management.monitor.CounterMonitor;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 08/08/2008
 */
public class QIntegration extends ConcreteProcessor{

	private IGroup qIntegration_inputGroup;
	private IGroup qIntegration_outputGroup;
	private Integer qIntegration_numberOfBin = 0; 
	private Boolean qIntegration_skip = false;
	private Boolean qIntegration_stop = false;
	private Boolean isDebug = false;

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.processor.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		//TODO: Implement skip option?
		if (qIntegration_inputGroup instanceof Plot){
			Plot plot = (Plot) qIntegration_inputGroup;
			double QBoundary = Double.valueOf(plot.findDataItem("QBoundary").getData().toString());
			double QBin = Double.valueOf(plot.findDataItem("QBin").getData().toString());
			int numberOfBin = (int) Math.ceil(QBoundary / QBin);
			if (qIntegration_numberOfBin != 0){
				numberOfBin = qIntegration_numberOfBin;
				QBin = QBoundary / numberOfBin;
			}
			else 
				qIntegration_numberOfBin = numberOfBin;
			double[] QBins = new double[numberOfBin];
			for (int i = 0; i < QBins.length; i++) {
				QBins[i] = QBin * (i + 1);
			}
			IArray Q = Factory.createArray(Double.TYPE, new int[]{numberOfBin}, QBins);
			IArray I = Factory.createArray(Double.TYPE, new int[]{numberOfBin});
			IArray statisticVariance = Factory.createArray(Double.TYPE, new int[]{numberOfBin});
			IArray IVariance = Factory.createArray(Double.TYPE, new int[]{numberOfBin});
			IArray Counters = Factory.createArray(Double.TYPE, new int[]{numberOfBin});
			IArray rawI = ((NcGroup) plot).getSignalArray();
			IArray rawVariance = plot.getVariance().getData();
			IArray Tr = plot.findCalculationData(QuokkaConstants.T_OVER_r_NAME).getData();
			IArray rawQ = plot.findCalculationData(QuokkaConstants.Q_NAME).getData();

			IArrayIterator rawIIterator = rawI.getIterator();
			IArrayIterator rawQIterator = rawQ.getIterator();
			IArrayIterator trIterator = Tr.getIterator();
			IArrayIterator rawVarianceIterator = rawVariance.getIterator();

			IIndex index = I.getIndex();
			while (rawIIterator.hasNext()){
				findIndex(rawQIterator.getDoubleNext(), Q, index);
				double rawValue = rawIIterator.getDoubleNext();
				double rawVarianceValue = rawVarianceIterator.getDoubleNext();
				if (!Double.isNaN(rawValue)){
					I.setDouble(index, I.getDouble(index) + rawValue);
					if (isDebug) {
						if (index.currentElement() == 1) {
							System.out.println(rawValue);
						}
					}
					Counters.setDouble(index, Counters.getDouble(index) + 1);
					IVariance.setDouble(index, IVariance.getDouble(index) + rawVarianceValue);
					statisticVariance.setDouble(index, statisticVariance.getDouble(index) + rawValue * rawValue);
				}
			}
			normaliseIWithCounter(I, IVariance, Counters);
			statisticVariance.getArrayMath().eltMultiply(Counters.getArrayMath().toEltInverseSkipZero()).add(I.getArrayMath().toPower(2).scale(-1).getArray());
			
			/**
			 * 'statisticVariance' so far is the variance of any sampled pixel (detector histogram bin) in
			 * the population of bins with the same Q interval (Q bin).
			 * 
			 * Now we reuse 'statisticVariance' to assign the variance of the mean of each Qbin, which is
			 * the error bar plotted on each mean.
			 * 
			 *     variance(mean for Q bin) = variance(pixel) / (no. of observations in Q bin) 
			 */
			statisticVariance.getArrayMath().eltMultiply(Counters.getArrayMath().toEltInverseSkipZero().getArray());

			//			removeNegative(I, IVariance);
			Plot IvsQ = (Plot) PlotFactory.createPlot("IvsQ", DataDimensionType.pattern);
//			IvsQ.addData("intensity", I, "Intensity", "counts", IVariance);
			IvsQ.addData("intensity", I, "Intensity", "counts", statisticVariance);
			IvsQ.addAxis("Q", Q, "Q", "", 0);
			IvsQ.addCalculationData("counter", Counters, "Counters", "", null);
//			IvsQ.addCalculationData("oldVarianc", IVariance, "Old Variance", "", null);
//			IvsQ.addCalculationData("statisticVariance", statisticVariance, "Statistic Variance", "counts", null);
//			IvsQ.addCalculationData("RawQ", rawQ, "Q Map", "q", null);
//			IvsQ.addCalculationData("RawIntensity", rawI, "Raw Intensity", "", null);
//			Array centroidArray = Factory.createArrayNoCopy(new double[]{10, 12});
			IvsQ.addDataItem(plot.findCalculationData(QuokkaConstants.CENTROID_NAME));
//			IvsQ.addCalculationData(QuokkaConstants.CENTROID_NAME, centroidArray, "Centroid", "mm", null);
			qIntegration_outputGroup = IvsQ;
			addIvsLogQ(plot);
			addIvsr(plot);
			addIvsTwoTheta(plot);
		}
		return qIntegration_stop;
	}

	private void addIvsLogQ(Plot plot) throws NumberFormatException, IOException, 
	SignalNotAvailableException, InvalidArrayTypeException, PlotFactoryException {
		double QBoundary = Double.valueOf(plot.findDataItem("QBoundary").getData().toString());
		double QBin = Double.valueOf(plot.findDataItem("QBin").getData().toString());
		int numberOfBin = (int) Math.ceil(QBoundary / QBin);
		if (qIntegration_numberOfBin != 0){
			numberOfBin = qIntegration_numberOfBin;
			QBin = QBoundary / numberOfBin;
		}
		else 
			qIntegration_numberOfBin = numberOfBin;
		double[] QBins = new double[numberOfBin];
		IArray rawLogQ = plot.findCalculationData(QuokkaConstants.Q_NAME).getData().getArrayMath().log10().getArray();
		double logQBoundaryMax = rawLogQ.getArrayMath().getMaximum();
		double logQBoundaryMin = rawLogQ.getArrayMath().getMinimum();
		double logQBin = (logQBoundaryMax - logQBoundaryMin) / numberOfBin;
		for (int i = 0; i < QBins.length; i++) {
			QBins[i] = logQBoundaryMin + logQBin * (i + 1);
		}
		IArray logQ = Factory.createArray(Double.TYPE, new int[]{numberOfBin}, QBins);
		IArray I = Factory.createArray(Double.TYPE, new int[]{numberOfBin});
		IArray IVariance = Factory.createArray(Double.TYPE, new int[]{numberOfBin});
		IArray Counters = Factory.createArray(Double.TYPE, new int[]{numberOfBin});
		IArray rawI = ((NcGroup) plot).getSignalArray();
		IArray rawVariance = plot.getVariance().getData();
		IArray Tr = plot.findCalculationData(QuokkaConstants.T_OVER_r_NAME).getData();

		IArrayIterator rawIIterator = rawI.getIterator();
		IArrayIterator rawLogQIterator = rawLogQ.getIterator();
		IArrayIterator trIterator = Tr.getIterator();
		IArrayIterator rawVarianceIterator = rawVariance.getIterator();

		IIndex index = I.getIndex();
		while (rawIIterator.hasNext()){
			findIndex(rawLogQIterator.getDoubleNext(), logQ, index);
			double rawValue = rawIIterator.getDoubleNext();
			double rawVarianceValue = rawVarianceIterator.getDoubleNext();
			if (!Double.isNaN(rawValue)){
				I.setDouble(index, I.getDouble(index) + rawValue);
				Counters.setDouble(index, Counters.getDouble(index) + 1);
				IVariance.setDouble(index, IVariance.getDouble(index) +	rawVarianceValue);
			}
		}
		normaliseIWithCounter(I, IVariance, Counters);
		removeNegative(I, IVariance);
		Plot IvsLogQ = (Plot) PlotFactory.createPlot(qIntegration_outputGroup, 
				"IvsLogQ", DataDimensionType.pattern);
		IvsLogQ.addData("intensity", I, "Intensity", "counts", IVariance);
		IvsLogQ.addAxis("logQ", logQ, "Log(Q)", "", 0);
		IvsLogQ.addCalculationData("counter", Counters, "Counters", "", null);
		IvsLogQ.addCalculationData("RawLogQ", rawLogQ, "Log(Q) Map", "log(q)", null);
		IvsLogQ.addCalculationData("RawIntensity", rawI, "Raw Intensity", "", null);
	}

	private void removeNegative(IArray I, IArray IVariance) {
		IArrayIterator iIterator = I.getIterator();
		IArrayIterator varianceIterator = IVariance.getIterator();
		double minI = findMinGreaterThanZero(I);
		double minVariance = findMinGreaterThanZero(IVariance);
		while (iIterator.hasNext()){
			double value = iIterator.getDoubleNext();
			if (value <= 0){
				iIterator.setDoubleCurrent(minI);
			}
			double varianceValue = varianceIterator.getDoubleNext();
			if (varianceValue <= 0)
				varianceIterator.setDoubleCurrent(minVariance);
		}
	}

	private double findMinGreaterThanZero(IArray array) {
		double min = Double.MAX_VALUE;
		IArrayIterator iter = array.getIterator();
		while (iter.hasNext()){
			double value = iter.getDoubleNext();
			if (value > 0 && value < min)
				min = value;
		}
		return min;
	}

	private void addIvsTwoTheta(Plot plot) throws NumberFormatException, IOException, 
	SignalNotAvailableException, InvalidArrayTypeException, PlotFactoryException {
		double twoThetaBoundary = Double.valueOf(plot.findDataItem("twoThetaBoundary").getData().toString());
		double twoThetaBin = Double.valueOf(plot.findDataItem("twoThetaBin").getData().toString());
		int numberOfBin = (int) Math.ceil(twoThetaBoundary / twoThetaBin);
		if (qIntegration_numberOfBin != 0){
			numberOfBin = qIntegration_numberOfBin;
			twoThetaBin = twoThetaBoundary / numberOfBin;
		}
		else 
			qIntegration_numberOfBin = numberOfBin;
		double[] twoThetaBins = new double[numberOfBin];
		for (int i = 0; i < twoThetaBins.length; i++) {
			twoThetaBins[i] = twoThetaBin * (i + 1);
		}
		IArray twoTheta = Factory.createArray(Double.TYPE, new int[]{numberOfBin}, twoThetaBins);
		IArray I = Factory.createArray(Double.TYPE, new int[]{numberOfBin});
		IArray IVariance = Factory.createArray(Double.TYPE, new int[]{numberOfBin});
		IArray Counters = Factory.createArray(Double.TYPE, new int[]{numberOfBin});
		IArray rawI = ((NcGroup) plot).getSignalArray();
		IArray rawVariance = plot.getVariance().getData();
		IArray Tr = plot.findCalculationData(QuokkaConstants.T_OVER_r_NAME).getData();
		IArray rawTwoTheta = plot.findCalculationData(QuokkaConstants.THETA_NAME).getData();

		IArrayIterator rawIIterator = rawI.getIterator();
		IArrayIterator rawTwoThetaIterator = rawTwoTheta.getIterator();
		IArrayIterator trIterator = Tr.getIterator();
		IArrayIterator rawVarianceIterator = rawVariance.getIterator();

		IIndex index = I.getIndex();
		while (rawIIterator.hasNext()){
			findIndex(rawTwoThetaIterator.getDoubleNext() * 2, twoTheta, index);
			double rawValue = rawIIterator.getDoubleNext();
			double rawVarianceValue = rawVarianceIterator.getDoubleNext();
			if (!Double.isNaN(rawValue)){
				I.setDouble(index, I.getDouble(index) + rawValue);
				Counters.setDouble(index, Counters.getDouble(index) + 1);
				IVariance.setDouble(index, IVariance.getDouble(index) + 
					rawVarianceValue);
			}
		}
		normaliseIWithCounter(I, IVariance, Counters);
		removeNegative(I, IVariance);
		Plot IvsTwoTheta = (Plot) PlotFactory.createPlot(qIntegration_outputGroup, "IvsTwoTheta", 
				DataDimensionType.pattern);
		IvsTwoTheta.addData("intensity", I, "Intensity", "counts", IVariance);
		IvsTwoTheta.addAxis("twoTheta", twoTheta, "twoTheta", "rad", 0);
		IvsTwoTheta.addCalculationData("counter", Counters, "Counters", "counts", null);
		IvsTwoTheta.addCalculationData("RawTwoTheta", rawTwoTheta, "twoTheta Map", "rad", null);
		IvsTwoTheta.addCalculationData("RawIntensity", rawI, "Raw Intensity", "counts", null);
		
		Plot IvsTwoThetaDeg = (Plot) PlotFactory.createPlot(qIntegration_outputGroup, "IvsTwoThetaDeg", 
				DataDimensionType.pattern);
		IvsTwoThetaDeg.addData("intensity", I, "Intensity", "counts", IVariance);
		IvsTwoThetaDeg.addAxis("twoTheta", twoTheta.getArrayMath().scale(57.29578).getArray(), "twoThetaDeg", "degree", 0);
		IvsTwoThetaDeg.addCalculationData("counter", Counters, "Counters", "counts", null);
		IvsTwoThetaDeg.addCalculationData("RawTwoTheta", rawTwoTheta.getArrayMath().scale(57.29578).getArray(), "twoTheta Map", "degree", null);
		IvsTwoThetaDeg.addCalculationData("RawIntensity", rawI, "Raw Intensity", "counts", null);

	}

	private void addIvsr(Plot plot) throws NumberFormatException, IOException, 
	SignalNotAvailableException, InvalidArrayTypeException, PlotFactoryException {
		// TODO Auto-generated method stub
		double rBoundary = Double.valueOf(plot.findDataItem("rBoundary").getData().toString());
		double rBin = Double.valueOf(plot.findDataItem("rBin").getData().toString());
		int numberOfBin = (int) Math.ceil(rBoundary / rBin);
		if (qIntegration_numberOfBin != 0){
			numberOfBin = qIntegration_numberOfBin;
			rBin = rBoundary / numberOfBin;
		}
		else 
			qIntegration_numberOfBin = numberOfBin;
		double[] rBins = new double[numberOfBin];
		for (int i = 0; i < rBins.length; i++) {
			rBins[i] = rBin * (i + 1);
		}
		IArray r = Factory.createArray(Double.TYPE, new int[]{numberOfBin}, rBins);
		IArray I = Factory.createArray(Double.TYPE, new int[]{numberOfBin});
		IArray IVariance = Factory.createArray(Double.TYPE, new int[]{numberOfBin});
		IArray Counters = Factory.createArray(Double.TYPE, new int[]{numberOfBin});
		IArray rawI = ((NcGroup) plot).getSignalArray();
		IArray rawVariance = plot.getVariance().getData();
		IArray Tr = plot.findCalculationData(QuokkaConstants.T_OVER_r_NAME).getData();
		IArray rawr = plot.findCalculationData(QuokkaConstants.DISTANCE_TO_BEAMCENTER_NAME).getData();

		IArrayIterator rawIIterator = rawI.getIterator();
		IArrayIterator rawrIterator = rawr.getIterator();
		IArrayIterator trIterator = Tr.getIterator();
		IArrayIterator rawVarianceIterator = rawVariance.getIterator();

		IIndex index = I.getIndex();
		while (rawIIterator.hasNext()){
			findIndex(rawrIterator.getDoubleNext(), r, index);
			double rawValue = rawIIterator.getDoubleNext();
			double rawVarianceValue = rawVarianceIterator.getDoubleNext();
			if (!Double.isNaN(rawValue)){
				I.setDouble(index, I.getDouble(index) + rawValue);
				Counters.setDouble(index, Counters.getDouble(index) + 1);
				IVariance.setDouble(index, IVariance.getDouble(index) + 
					rawVarianceValue);
			}
		}
		normaliseIWithCounter(I, IVariance, Counters);
		removeNegative(I, IVariance);
		Plot Ivsr = (Plot) PlotFactory.createPlot(qIntegration_outputGroup, "Ivsr", DataDimensionType.pattern);
		Ivsr.addData("intensity", I, "Intensity", "counts", IVariance);
		Ivsr.addAxis("r", r, "r", "mm", 0);
		Ivsr.addCalculationData("counter", Counters, "Counters", "counts", null);
		Ivsr.addCalculationData("Rawr", rawr, "r Map", "mm", null);
		Ivsr.addCalculationData("RawIntensity", rawI, "Raw Intensity", "counts", null);
	}

	private void normaliseIWithCounter(IArray I, IArray variance, IArray counters) {
		IArrayIterator IIterator = I.getIterator();
		IArrayIterator counterIterator = counters.getIterator();
		IArrayIterator varianceIterator = variance.getIterator();
		while(IIterator.hasNext()){
			double counter = counterIterator.getDoubleNext();
			if (counter != 0){
				IIterator.setDoubleCurrent(IIterator.getDoubleNext() / counter);
				varianceIterator.setDoubleCurrent(varianceIterator.getDoubleNext() / counter / counter);
			}
			else {
				IIterator.next().setDoubleCurrent(0);
				varianceIterator.next().setDoubleCurrent(0);
			}
		}
	}

	/**
	 * @return the qIntegration_outputGroup
	 */
	public IGroup getQIntegration_outputGroup() {
		return qIntegration_outputGroup;
	}

	/**
	 * @param integration_inputGroup the qIntegration_inputGroup to set
	 */
	public void setQIntegration_inputGroup(IGroup integration_inputGroup) {
		qIntegration_inputGroup = integration_inputGroup;
	}

	/**
	 * @param integration_skip the qIntegration_skip to set
	 */
	public void setQIntegration_skip(Boolean integration_skip) {
		qIntegration_skip = integration_skip;
	}

	/**
	 * @param integration_stop the qIntegration_stop to set
	 */
	public void setQIntegration_stop(Boolean integration_stop) {
		qIntegration_stop = integration_stop;
	}

	/**
	 * @param integration_numberOfBin the qIntegration_numberOfBin to set
	 */
	public void setQIntegration_numberOfBin(Integer integration_numberOfBin) {
		qIntegration_numberOfBin = integration_numberOfBin;
	}

	private void findIndex(double q, IArray Q, IIndex index) {
		int counter = 0;
		IArrayIterator qIterator = Q.getIterator();
		while (qIterator.hasNext()){
			if (q <= qIterator.getDoubleNext()){
				index.set(counter);
				break;
			}else{
				counter ++;
			}
		}
	}

}
