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

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.exception.IndexOutOfBoundException;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotIterator;
import au.gov.ansto.bragg.datastructures.core.plot.Point;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 07/08/2008
 */
public class PreCalculation extends ConcreteProcessor {

	IGroup preCalculation_inputGroup;
	IGroup preCalculation_outputGroup;
	Boolean preCalculation_skip = false;
	Boolean preCalculation_stop = false;
	Double preCalculation_transmission = 1.;
	Double preCalculation_L2mm = 2000.;
	Boolean preCalculation_applyParameterManually = true;


	private double L2 = 5861;
	private Double preCalculation_lambda = 5.0;
	private double detectorSizeZ = 975.36;
	private double detectorSizeX = 975.36;
	private Double preCalculation_centroidZ = detectorSizeZ / 2;
	private Double preCalculation_centroidX = detectorSizeX / 2;
	private double binSizeX = 5.08;
	private double binSizeZ = 5.08;
	private double centroidZ;
	private double centroidX;
	private double lambda;
	private double transmission;
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.processor.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		if (preCalculation_inputGroup instanceof Plot){
			Plot plot = (Plot) preCalculation_inputGroup;
			if (plot.getRank() > 2){
				plot.reduce();
			}
			if (plot.getRank() > 3){
				throw new InvalidArrayTypeException();
			} else 	if (plot.getRank() > 2){

					} else {
						//	Plot outputPlot = plot.copyToDouble();
						preCalculate(plot);
						preCalculation_outputGroup = plot;
					}
		}
		return preCalculation_stop;
	}

	private void preCalculate(Plot inputPlot)
	throws IOException, SignalNotAvailableException, IndexOutOfBoundException, InvalidArrayTypeException {
		prepareParameters();
		Axis zAxis = inputPlot.getAxis(0);
		Axis xAxis = inputPlot.getAxis(1);
		double multiFactorZ = 1;
		double multiFactorX = 1;
		if (zAxis.getUnitsString() != null && zAxis.getUnitsString().toLowerCase().contains("raw"))
			multiFactorZ = binSizeZ;
		if (xAxis.getUnitsString() != null && xAxis.getUnitsString().toLowerCase().contains("raw"))
			multiFactorX = binSizeX;
		int axisSize = 0;
		int[] shape = ((NcGroup) inputPlot).getSignalArray().getShape();
		boolean isAxisBinCenter;
		if (zAxis.getSize() == shape[0]){
			axisSize = (int) zAxis.getSize();
			isAxisBinCenter = true;
		}
		else{
			axisSize = (int) zAxis.getSize() - 1;
			isAxisBinCenter = false;
		}
		IArray binCenterZ = Factory.createArray(Double.TYPE, new int[]{axisSize});
		IArray binCenterX = Factory.createArray(Double.TYPE, new int[]{axisSize});
		IArray binWidthZ = Factory.createArray(Double.TYPE, new int[]{axisSize});
		IArray binWidthX = Factory.createArray(Double.TYPE, new int[]{axisSize});
		//		int detectorSize = (int) inputPlot.findSingal().getSize();

		IArray rToBeamCenter = Factory.createArray(Double.TYPE, shape);
		IArray RToSample = Factory.createArray(Double.TYPE, shape);
		IArray Q = Factory.createArray(Double.TYPE, shape);
		IArray Tr = Factory.createArray(Double.TYPE, shape);
		IArray theta = Factory.createArray(Double.TYPE, shape);
		IArray binArea = Factory.createArray(Double.TYPE, shape);		
		IArray deltaOmiga = Factory.createArray(Double.TYPE, shape);

		IArrayIterator zIterator = zAxis.getData().getIterator();
		IArrayIterator binCenterZIterator = binCenterZ.getIterator();
		IArrayIterator binWidthZIterator = binWidthZ.getIterator();
		
		if (isAxisBinCenter){
			double lowBoundary = zIterator.getDoubleNext();
			binCenterZIterator.next().setDoubleCurrent(lowBoundary * multiFactorZ);
			double upBoundary = zIterator.getDoubleNext();
			binCenterZIterator.next().setDoubleCurrent(upBoundary * multiFactorZ);
			binWidthZIterator.next().setDoubleCurrent(Math.abs(upBoundary - lowBoundary) * multiFactorZ);
			lowBoundary = (upBoundary + lowBoundary) / 2;
			double lastCenter = upBoundary;
			double thisCenter = 0;
			while (zIterator.hasNext()) {
				thisCenter = zIterator.getDoubleNext();
				binCenterZIterator.next().setDoubleCurrent(thisCenter * multiFactorZ);
				upBoundary = (thisCenter + lastCenter) / 2;
				binWidthZIterator.next().setDoubleCurrent(Math.abs(upBoundary - lowBoundary) * multiFactorZ);
				lowBoundary = upBoundary;
				lastCenter = thisCenter;
			}
			binWidthZIterator.next().setDoubleCurrent(Math.abs(thisCenter - lowBoundary) * 2 * multiFactorZ);
		}else {
			double lowBoundary = zIterator.getDoubleNext();
			double upBoundary;
			int counter = 0;
			while (zIterator.hasNext()) {
				upBoundary = zIterator.getDoubleNext();
				binWidthZIterator.next().setDoubleCurrent(Math.abs(upBoundary - lowBoundary) * multiFactorZ);
				binCenterZIterator.next().setDoubleCurrent((upBoundary + lowBoundary) * multiFactorZ / 2);
				lowBoundary = upBoundary;
				counter ++;
			}
		}
		
		IArrayIterator xIterator = xAxis.getData().getIterator();
		IArrayIterator binCenterXIterator = binCenterX.getIterator();
		IArrayIterator binWidthXIterator = binWidthX.getIterator();
		if (isAxisBinCenter){
			double lowBoundary = xIterator.getDoubleNext();
			binCenterXIterator.next().setDoubleCurrent(lowBoundary * multiFactorX);
			double upBoundary = xIterator.getDoubleNext();
			binCenterXIterator.next().setDoubleCurrent(upBoundary * multiFactorX);
			binWidthXIterator.next().setDoubleCurrent(Math.abs(upBoundary - lowBoundary) * multiFactorX);
			lowBoundary = (upBoundary + lowBoundary) / 2;
			double lastCenter = upBoundary;
			double thisCenter = 0;
			while (xIterator.hasNext()) {
				thisCenter = xIterator.getDoubleNext();
				binCenterXIterator.next().setDoubleCurrent(thisCenter * multiFactorX);
				upBoundary = (thisCenter + lastCenter) / 2;
				binWidthXIterator.next().setDoubleCurrent(Math.abs(upBoundary - lowBoundary) * multiFactorX);
				lowBoundary = upBoundary;
				lastCenter = thisCenter;
			}
			binWidthXIterator.next().setDoubleCurrent(Math.abs(thisCenter - lowBoundary) * multiFactorX * 2);
		}else {
			int counter = 0;
			double lowBoundary = xIterator.getDoubleNext();
			double upBoundary;
			while (xIterator.hasNext()) {
				upBoundary = xIterator.getDoubleNext();
				binWidthXIterator.next().setDoubleCurrent(Math.abs(upBoundary - lowBoundary) * multiFactorX);
				binCenterXIterator.next().setDoubleCurrent((upBoundary + lowBoundary) * multiFactorX / 2);
				lowBoundary = upBoundary;
				counter ++;
			}
		}

		if (zAxis.getUnitsString() == null){
			binCenterZIterator = binCenterZ.getIterator();
			binWidthZIterator = binWidthZ.getIterator();
			while(binCenterZIterator.hasNext())
				binCenterZIterator.setDoubleCurrent(binCenterZIterator.getDoubleNext() * binSizeZ);
			while(binWidthZIterator.hasNext())
				binWidthZIterator.setDoubleCurrent(binWidthZIterator.getDoubleNext() * binSizeZ);

			binCenterXIterator = binCenterX.getIterator();
			binWidthXIterator = binWidthX.getIterator();
			while(binCenterXIterator.hasNext())
				binCenterXIterator.setDoubleCurrent(binCenterXIterator.getDoubleNext() * binSizeX);
			while(binWidthXIterator.hasNext())
				binWidthXIterator.setDoubleCurrent(binWidthXIterator.getDoubleNext() * binSizeX);
		}

		try {
			inputPlot.addAxis("z_bin_center", binCenterZ, "Z Bin Center", "mm", 0);
			inputPlot.addAxis("x_bin_center", binCenterX, "X Bin Center", "mm", 1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new SignalNotAvailableException(e);
		} 

		// TODO Auto-generated method stub
		PlotIterator inputIterator = new PlotIterator(inputPlot);
		IArrayIterator rIterator = rToBeamCenter.getIterator();
		IArrayIterator RIterator = RToSample.getIterator();
		IArrayIterator thetaIterator = theta.getIterator();
		IArrayIterator QIterator = Q.getIterator();
		IArrayIterator TrIterator = Tr.getIterator();
		IArrayIterator binAreaIterator = binArea.getIterator();
		IArrayIterator deltaOmigaIterator = deltaOmiga.getIterator();
		binWidthZIterator = binWidthZ.getIterator();
		binWidthXIterator = binWidthX.getIterator();
		double binWidthZValue = binWidthZIterator.getDoubleNext();
		while (inputIterator.hasNext()){
			Point point = inputIterator.getPointNext();
			double[] cordinate = point.getCoordinate();
			double r = Math.sqrt((cordinate[0] - centroidZ) * (cordinate[0] - centroidZ) +
					(cordinate[1] - centroidX) * (cordinate[1] - centroidX));
			rIterator.next().setDoubleCurrent(r);
			double R = Math.hypot(r,L2);
//			double R = Math.sqrt(r*r+L2*L2);
			RIterator.next().setDoubleCurrent(R);
			double sinTheta = Math.sqrt((1 - L2 / R) / 2);
			thetaIterator.next().setDoubleCurrent(Math.asin(sinTheta));
			double q = 4.0 * Math.PI * sinTheta / lambda;
			QIterator.next().setDoubleCurrent(q);
			double tr = Math.pow(transmission, 0.5 + 0.5 * Math.sqrt(L2 * L2 + r * r) / L2);
			TrIterator.next().setDoubleCurrent(tr);
			if (! binWidthXIterator.hasNext()){
				binWidthXIterator = binWidthX.getIterator();
				binWidthZValue = binWidthZIterator.getDoubleNext();
			}
			double area = binWidthZValue * binWidthXIterator.getDoubleNext();
			binAreaIterator.next().setDoubleCurrent(area);
			deltaOmigaIterator.next().setDoubleCurrent(area * L2 / R / R);
		}

		inputPlot.addCalculationData(QuokkaConstants.CENTROID_NAME, Factory.createArray(
				new double[]{centroidZ, centroidX}), "Centroid", "mm", null);
		inputPlot.addCalculationData(QuokkaConstants.DISTANCE_TO_BEAMCENTER_NAME, rToBeamCenter, 
				"Distance to Beam Center", "mm", null);
		inputPlot.addCalculationData(QuokkaConstants.DISTANCE_TO_SAMPLE_NAME, RToSample, 
				"Distance to Sample", "mm", null);
		inputPlot.addCalculationData(QuokkaConstants.THETA_NAME, theta, "Theta", "rad", null);
		inputPlot.addCalculationData(QuokkaConstants.Q_NAME, Q, "Q", "mm", null);
		inputPlot.addCalculationData(QuokkaConstants.T_OVER_r_NAME, Tr, "T for r", "", null);
		inputPlot.addCalculationData(QuokkaConstants.BIN_AREA_NAME, binArea, "Area of Bin", "mm2", null);
		inputPlot.addCalculationData(QuokkaConstants.DELTA_OMEGA_NAME, deltaOmiga, "delta Omega", "", null);

		//		double largestZ = Math.max(detectorSizeZ - centroidZ, centroidZ);
		//		double largestX = Math.max(detectorSizeX - centroidX, centroidX);
		Double largestQ = Q.getArrayMath().getMaximum();
		double largestArea = binArea.getArrayMath().getMaximum();
		Double largestQBin = Math.sqrt(1 - L2 / Math.sqrt((L2 * L2 + largestArea * largestArea))) * 
		8.885765876316732 / lambda;
		IDataItem QBoundary = Factory.createDataItem(inputPlot, "QBoundary", 
				Factory.createArray(largestQ.toString().toCharArray()));
		inputPlot.addDataItem(QBoundary);
		IDataItem QBin = Factory.createDataItem(inputPlot, "QBin", 
				Factory.createArray(largestQBin.toString().toCharArray()));
		inputPlot.addDataItem(QBin);
		Double largestr = rToBeamCenter.getArrayMath().getMaximum();
		Double largestrBin = Math.sqrt(binSizeX * binSizeX + binSizeZ * binSizeZ);
		IDataItem rBoundary = Factory.createDataItem(inputPlot, "rBoundary", 
				Factory.createArray(largestr.toString().toCharArray()));
		inputPlot.addDataItem(rBoundary);
		IDataItem rBin = Factory.createDataItem(inputPlot, "rBin", 
				Factory.createArray(largestrBin.toString().toCharArray()));
		inputPlot.addDataItem(rBin);
		Double largestTwoTheta = theta.getArrayMath().getMaximum() * 2;
		Double largestTwoThetaBin = Math.asin(largestrBin / 
				Math.sqrt(binSizeX * binSizeX + binSizeZ * binSizeZ + L2 * L2));
		IDataItem twoThetaBoundary = Factory.createDataItem(inputPlot, "twoThetaBoundary", 
				Factory.createArray(largestTwoTheta.toString().toCharArray()));
		inputPlot.addDataItem(twoThetaBoundary);
		IDataItem twoThetaBin = Factory.createDataItem(inputPlot, "twoThetaBin", 
				Factory.createArray(largestTwoThetaBin.toString().toCharArray()));
		inputPlot.addDataItem(twoThetaBin);
	}

	private Double prepareParameter(Double parameterValue, String shortName, String fieldName){
		Double returnValue = parameterValue;
		if (! preCalculation_applyParameterManually || parameterValue == null || parameterValue == 0){
			double nexusValue = 0;
			try {
				nexusValue = Double.valueOf(preCalculation_inputGroup.findDataItem(
						shortName).getData().toString());
			} catch (Exception e) {
				// TODO: handle exception
				try{
					nexusValue = Double.valueOf(preCalculation_inputGroup.getDataItem(
						shortName).getData().getArrayMath().getMaximum());
				}catch (Exception e1) {}
			}
			if (nexusValue != 0){
				returnValue = nexusValue;
				informVarValueChange(fieldName, returnValue);
			}
		}
		return returnValue;
	}
	
	private void prepareParameters() {
		// TODO Auto-generated method stub
		centroidZ = prepareParameter(preCalculation_centroidZ, "BeamCenterZ", "preCalculation_centroidZ");
		centroidX = prepareParameter(preCalculation_centroidX, "BeamCenterX", "preCalculation_centroidX");
		lambda = prepareParameter(preCalculation_lambda, "LambdaA", "preCalculation_lambda");
		transmission = prepareParameter(preCalculation_transmission, "Transmission", "preCalculation_transmission");
		L2 = prepareParameter(preCalculation_L2mm, "L2mm", "preCalculation_L2mm");
//		try{
//			L2 = Double.valueOf(preCalculation_inputGroup.findDataItem("L2mm").getData().toString());
//		}catch (Exception e) {
//			try{
//				L2 = Double.valueOf(preCalculation_inputGroup.getDataItem("L2mm").getData().getMaximum());
//			}catch (Exception e1) {}
//		}
//		try{
//			detectorSizeZ = Double.valueOf(preCalculation_inputGroup.findDataItem("detectorsizezmm").getData().toString());
//		}catch (Exception e) {}
//		try{
//			detectorSizeX = Double.valueOf(preCalculation_inputGroup.findDataItem("detectorsizexmm").getData().toString());
//		}catch (Exception e) {}

	}
	/**
	 * @return the preCalculation_outputGroup
	 */
	public IGroup getPreCalculation_outputGroup() {
		return preCalculation_outputGroup;
	}
	/**
	 * @param preCalculation_inputGroup the preCalculation_inputGroup to set
	 */
	public void setPreCalculation_inputGroup(IGroup preCalculation_inputGroup) {
		this.preCalculation_inputGroup = preCalculation_inputGroup;
	}
	/**
	 * @param preCalculation_skip the preCalculation_skip to set
	 */
	public void setPreCalculation_skip(Boolean preCalculation_skip) {
		this.preCalculation_skip = preCalculation_skip;
	}

	/**
	 * @param preCalculation_transmission the preCalculation_transmission to set
	 */
	public void setPreCalculation_transmission(Double preCalculation_transmission) {
		this.preCalculation_transmission = preCalculation_transmission;
	}
	
	/**
	 * @param preCalculation_stop the preCalculation_stop to set
	 */
	public void setPreCalculation_stop(Boolean preCalculation_stop) {
		this.preCalculation_stop = preCalculation_stop;
	}
	/**
	 * @param preCalculation_willLoadFromFile the preCalculation_willLoadFromFile to set
	 */
	public void setPreCalculation_applyParameterManually(
			Boolean preCalculation_applyParameterManually) {
		this.preCalculation_applyParameterManually = preCalculation_applyParameterManually;
	}
	/**
	 * @param preCalculation_lambda the preCalculation_lambda to set
	 */
	public void setPreCalculation_lambda(Double preCalculation_lambda) {
		this.preCalculation_lambda = preCalculation_lambda;
	}
	/**
	 * @param preCalculation_centroidZ the preCalculation_centroidZ to set
	 */
	public void setPreCalculation_centroidZ(Double preCalculation_centroidZ) {
		this.preCalculation_centroidZ = preCalculation_centroidZ;
	}

	/**
	 * @param preCalculation_L2mm the preCalculation_L2mm to set
	 */
	public void setPreCalculation_L2mm(Double preCalculation_L2mm) {
		this.preCalculation_L2mm = preCalculation_L2mm;
	}

	/**
	 * @param preCalculation_centroidX the preCalculation_centroidX to set
	 */
	public void setPreCalculation_centroidX(Double preCalculation_centroidX) {
		this.preCalculation_centroidX = preCalculation_centroidX;
	}

}

