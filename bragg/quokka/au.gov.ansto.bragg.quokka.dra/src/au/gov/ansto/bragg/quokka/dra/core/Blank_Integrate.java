/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Template source file for setting up processor block interfaces
* 
* Contributors: 
*    Paul Hathaway - February 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import java.io.IOException;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;
import au.gov.ansto.bragg.quokka.dra.core.internal.DataLib;

public class Blank_Integrate extends ConcreteProcessor {
	
	private static final String processClass = "Blank_Integrate"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009022011; 

	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.pattern;
    private Boolean isDebugMode = true;

	private Plot inPlot;
	private Plot outPlot;
	private Boolean doForceSkip = false;
	private Boolean doForceStop = false;
	
//	private final static String KEY_WAVELENGTH = "LambdaA";
	
	private Integer numberOfBin = 0;
	private Double  minRadius = 0.0;
	private Double  centreX = 0.0;
	private Double  centreZ = 0.0;

	public Blank_Integrate() {
		this.setReprocessable(false);
	}
	
	public Boolean process() throws Exception {

    	this.outPlot = this.inPlot;
    	
    	stampProcessLog();
    	
		if (this.doForceSkip) {
			stampProcessSkip();			
		} else {
			doForceStop = processIntegrate();
			stampProcessEnd();
		}
		return doForceStop;
	}

	private void stampProcessLog() {
		((NcGroup) outPlot).setLocation(inPlot.getLocation());
		outPlot.addProcessingLog("ProcessClass/Version/ID: ["
				+processClass+";"
				+processClassVersion+";"
				+processClassID+"]");		
	}
	
	private void stampProcessSkip() {
		((NcGroup) outPlot).setLocation(inPlot.getLocation());
		outPlot.addProcessingLog("["+processClassID+"]:"+"SKIP process");		
	}
	private void stampProcessEntry(String logEntry) {
		((NcGroup) outPlot).setLocation(inPlot.getLocation());
		outPlot.addProcessingLog("["+processClassID+"]: "+logEntry);		
	}
	
	private void stampProcessEnd() {
		((NcGroup) outPlot).setLocation(inPlot.getLocation());
		outPlot.addProcessingLog("["+processClassID+"]:"+"END process");		
	}
		
	public Boolean processIntegrate() throws Exception {
		if (inPlot instanceof Plot){
			double QBoundary = Double.valueOf(inPlot.findDataItem("QBoundary").getData().toString());
			double QBin = Double.valueOf(inPlot.findDataItem("QBin").getData().toString());
			int numBins = (int) Math.ceil(QBoundary / QBin);
			if (numberOfBin != 0){
				numBins = numberOfBin;
				QBin = QBoundary / numBins;
			}
			else 
				numberOfBin = numBins;
			double[] QBins = new double[numBins];
			for (int i = 0; i < QBins.length; i++) {
				QBins[i] = QBin * (i + 1);
			}
			IArray Q = Factory.createArray(Double.TYPE, new int[]{numBins}, QBins);
			IArray I = Factory.createArray(Double.TYPE, new int[]{numBins});
			IArray statisticVariance = Factory.createArray(Double.TYPE, new int[]{numBins});
			IArray IVariance = Factory.createArray(Double.TYPE, new int[]{numBins});
			IArray Counters = Factory.createArray(Double.TYPE, new int[]{numBins});
			IArray rawI = ((NcGroup) inPlot).getSignalArray();
			IArray rawVariance = inPlot.getVariance().getData();
			IArray rawQ = inPlot.findCalculationData(QuokkaConstants.Q_NAME).getData();

			IArrayIterator rawIIterator = rawI.getIterator();
			IArrayIterator rawQIterator = rawQ.getIterator();
			IArrayIterator rawVarianceIterator = rawVariance.getIterator();

			IIndex index = I.getIndex();
			while (rawIIterator.hasNext()){
				findIndex(rawQIterator.getDoubleNext(), Q, index);
				double rawValue = rawIIterator.getDoubleNext();
				double rawVarianceValue = rawVarianceIterator.getDoubleNext();
				if (!Double.isNaN(rawValue)){
					I.setDouble(index, I.getDouble(index) + rawValue);
					if (isDebugMode) {
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
			statisticVariance.getArrayMath().eltMultiply(Counters.getArrayMath().toEltInverseSkipZero()).add(I.getArrayMath().toPower(2).scale(-1));
			
			/**
			 * 'statisticVariance' so far is the variance of any sampled pixel (detector histogram bin) in
			 * the population of bins with the same Q interval (Q bin).
			 * 
			 * Now we reuse 'statisticVariance' to assign the variance of the mean of each Qbin, which is
			 * the error bar plotted on each mean.
			 * 
			 *     variance(mean for Q bin) = variance(pixel) / (no. of observations in Q bin) 
			 */
			statisticVariance.getArrayMath().eltMultiply(Counters.getArrayMath().toEltInverseSkipZero());

			Plot IvsQ = (Plot) PlotFactory.createPlot("IvsQ", DataDimensionType.pattern);
			IvsQ.addData("intensity", I, "Intensity", "counts", statisticVariance);
			IvsQ.addAxis("Q", Q, "Q", "", 0);
			IvsQ.addCalculationData("counter", Counters, "Counters", "", null);
			IvsQ.addDataItem(inPlot.findCalculationData(QuokkaConstants.CENTROID_NAME));
			outPlot = IvsQ;

			IDataItem notes = inPlot.getDataItem(DataLib.KEY_NOTES);
			if(null!=notes) {
				outPlot.addDataItem(notes);
			}
			
			addIvsLogQ(inPlot);
			addIvsr(inPlot);
			addIvsTwoTheta(inPlot);
		}
		return doForceStop;
	}

	private void addIvsLogQ(Plot plot) throws NumberFormatException, IOException, 
	SignalNotAvailableException, InvalidArrayTypeException, PlotFactoryException {
		double QBoundary = Double.valueOf(plot.findDataItem("QBoundary").getData().toString());
		double QBin = Double.valueOf(plot.findDataItem("QBin").getData().toString());
		int numBins = (int) Math.ceil(QBoundary / QBin);
		if (numberOfBin != 0){
			numBins = numberOfBin;
			QBin = QBoundary / numBins;
		}
		else 
			numberOfBin = numBins;
		double[] QBins = new double[numBins];
		IArray rawLogQ = plot.findCalculationData(QuokkaConstants.Q_NAME).getData().getArrayMath().log10().getArray();
		double logQBoundaryMax = rawLogQ.getArrayMath().getMaximum();
		double logQBoundaryMin = rawLogQ.getArrayMath().getMinimum();
		double logQBin = (logQBoundaryMax - logQBoundaryMin) / numBins;
		for (int i = 0; i < QBins.length; i++) {
			QBins[i] = logQBoundaryMin + logQBin * (i + 1);
		}
		IArray logQ = Factory.createArray(Double.TYPE, new int[]{numBins}, QBins);
		IArray I = Factory.createArray(Double.TYPE, new int[]{numBins});
		IArray IVariance = Factory.createArray(Double.TYPE, new int[]{numBins});
		IArray Counters = Factory.createArray(Double.TYPE, new int[]{numBins});
		IArray rawI = ((NcGroup) plot).getSignalArray();
		IArray rawVariance = plot.getVariance().getData();
		IArrayIterator rawIIterator = rawI.getIterator();
		IArrayIterator rawLogQIterator = rawLogQ.getIterator();
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
		Plot IvsLogQ = (Plot) PlotFactory.createPlot(outPlot, 
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
		int numBins = (int) Math.ceil(twoThetaBoundary / twoThetaBin);
		if (numberOfBin != 0){
			numBins = numberOfBin;
			twoThetaBin = twoThetaBoundary / numBins;
		}
		else 
			numberOfBin = numBins;
		double[] twoThetaBins = new double[numBins];
		for (int i = 0; i < twoThetaBins.length; i++) {
			twoThetaBins[i] = twoThetaBin * (i + 1);
		}
		IArray twoTheta = Factory.createArray(Double.TYPE, new int[]{numBins}, twoThetaBins);
		IArray I = Factory.createArray(Double.TYPE, new int[]{numBins});
		IArray IVariance = Factory.createArray(Double.TYPE, new int[]{numBins});
		IArray Counters = Factory.createArray(Double.TYPE, new int[]{numBins});
		IArray rawI = ((NcGroup) plot).getSignalArray();
		IArray rawVariance = plot.getVariance().getData();
		IArray rawTwoTheta = plot.findCalculationData(QuokkaConstants.THETA_NAME).getData();

		IArrayIterator rawIIterator = rawI.getIterator();
		IArrayIterator rawTwoThetaIterator = rawTwoTheta.getIterator();
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
		Plot IvsTwoTheta = (Plot) PlotFactory.createPlot(outPlot, "IvsTwoTheta", 
				DataDimensionType.pattern);
		IvsTwoTheta.addData("intensity", I, "Intensity", "counts", IVariance);
		IvsTwoTheta.addAxis("twoTheta", twoTheta, "twoTheta", "rad", 0);
		IvsTwoTheta.addCalculationData("counter", Counters, "Counters", "counts", null);
		IvsTwoTheta.addCalculationData("RawTwoTheta", rawTwoTheta, "twoTheta Map", "rad", null);
		IvsTwoTheta.addCalculationData("RawIntensity", rawI, "Raw Intensity", "counts", null);
		
		Plot IvsTwoThetaDeg = (Plot) PlotFactory.createPlot(outPlot, "IvsTwoThetaDeg", 
				DataDimensionType.pattern);
		IvsTwoThetaDeg.addData("intensity", I, "Intensity", "counts", IVariance);
		IvsTwoThetaDeg.addAxis("twoTheta", twoTheta.getArrayMath().scale(57.29578).getArray(), "twoThetaDeg", "degree", 0);
		IvsTwoThetaDeg.addCalculationData("counter", Counters, "Counters", "counts", null);
		IvsTwoThetaDeg.addCalculationData("RawTwoTheta", rawTwoTheta.getArrayMath().scale(57.29578).getArray(), "twoTheta Map", "degree", null);
		IvsTwoThetaDeg.addCalculationData("RawIntensity", rawI, "Raw Intensity", "counts", null);

	}

	private void addIvsr(Plot plot) throws NumberFormatException, IOException, 
	SignalNotAvailableException, InvalidArrayTypeException, PlotFactoryException {
		double rBoundary = Double.valueOf(plot.findDataItem("rBoundary").getData().toString());
		double rBin = Double.valueOf(plot.findDataItem("rBin").getData().toString());
		int numBins = (int) Math.ceil(rBoundary / rBin);
		if (numberOfBin != 0){
			numBins = numberOfBin;
			rBin = rBoundary / numBins;
		}
		else 
			numberOfBin = numBins;
		double[] rBins = new double[numBins];
		for (int i = 0; i < rBins.length; i++) {
			rBins[i] = rBin * (i + 1);
		}
		IArray r = Factory.createArray(Double.TYPE, new int[]{numBins}, rBins);
		IArray I = Factory.createArray(Double.TYPE, new int[]{numBins});
		IArray IVariance = Factory.createArray(Double.TYPE, new int[]{numBins});
		IArray Counters = Factory.createArray(Double.TYPE, new int[]{numBins});
		IArray rawI = ((NcGroup) plot).getSignalArray();
		IArray rawVariance = plot.getVariance().getData();
		IArray rawr = plot.findCalculationData(QuokkaConstants.DISTANCE_TO_BEAMCENTER_NAME).getData();

		IArrayIterator rawIIterator = rawI.getIterator();
		IArrayIterator rawrIterator = rawr.getIterator();
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
		Plot Ivsr = (Plot) PlotFactory.createPlot(outPlot, "Ivsr", DataDimensionType.pattern);
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

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/* Client Support methods -----------------------------------------*/
	
	public static DataStructureType getDataStructureType() {
		return dataStructureType;
	}

	public static DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

	public void setIsDebugMode(Boolean isDebugMode) {
		this.isDebugMode = isDebugMode;
	}

	public Boolean getIsDebugMode() {
		return isDebugMode;
	}	
	
	/* Port get/set methods -----------------------------------------*/
    
    /* In-Ports -----------------------------------------------------*/

	public void setInPlot(Plot inPlot) {
		this.inPlot = inPlot;
	}

    /* Out-Ports ----------------------------------------------------*/

	public Plot getOutPlot() {
		String title = DataLib.generateTitle(this.inPlot);
		this.outPlot.setTitle(title);
		return this.outPlot;
	}    

    /* Var-Ports (tuners) -------------------------------------------*/

	public void setNumberOfBin(Integer numberOfBin) {
		this.numberOfBin = numberOfBin;
	}

	public void setMinRadius(Double minRadius) {
		this.minRadius = minRadius;
	}

	public void setCentreX(Double centreX) {
		this.centreX = centreX;
		super.informVarValueChange("CentroidX", this.centreX);
	}

	public void setCentreZ(Double centreZ) {
		this.centreZ = centreZ;
		super.informVarValueChange("centreZ", this.centreZ);
	}

	/* Var-Ports (options) ------------------------------------------*/

	public Boolean getSkip() {
		return doForceSkip;
	}

	public void setSkip(Boolean doForceSkip) {
		this.doForceSkip = doForceSkip;
	}
	
	public Boolean getStop() {
		return doForceStop;
	}

	public void setStop(Boolean doForceStop) {
		this.doForceStop = doForceStop;
	}
}
