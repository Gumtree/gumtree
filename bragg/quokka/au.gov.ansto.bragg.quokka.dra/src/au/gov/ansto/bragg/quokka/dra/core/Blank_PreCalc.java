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
*    Norman Xiong - preCalculate method 
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
import au.gov.ansto.bragg.datastructures.core.exception.IndexOutOfBoundException;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotIterator;
import au.gov.ansto.bragg.datastructures.core.plot.Point;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class Blank_PreCalc extends ConcreteProcessor {
	
	private static final String processClass = "Blank_PreCalc"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009022305; 

	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = true;

	private final static String KEY_WAVELENGTH = "LambdaA";
	private final static String KEY_L2MM = "L2mm";
	private final static String KEY_CENTERX = "BeamCenterX";
	private final static String KEY_CENTERZ = "BeamCenterZ";

	private Plot inPlot;
	private Plot outPlot;
	private Boolean synch = false;
	private Boolean doForceSkip = false;
	private Boolean doForceStop = false;
	private Boolean isDirty = false; /* force recalculate plot axes when true */
	
	private final double binSizeX =  5.08; // mm
	private final double binSizeZ =  5.08; // mm
	private final double detHeight = 192.0 * binSizeX; // 975.36mm
	private final double detWidth =  192.0 * binSizeZ; // 975.36mm

	private Double defaultL2mm = 2000.0;

	private Double wavelength = 5.0;;
	private Double centroidZ = detHeight/2.0;
	private Double centroidX = detWidth/2.0;
	private Double l2mm = 5861.0;

	public Blank_PreCalc() {
		this.setReprocessable(false);
	}
	
	public Boolean process() throws Exception {

    	this.outPlot = this.inPlot;
    	
    	stampProcessLog();
		stampProcessEntry("synch = "+synch.toString());
		synch = false;
		
		if (this.doForceSkip) {
			stampProcessSkip();			
		} else {
			checkPlot();
			checkParameters();
			/* As at 25/3 for next release, must always process 'preCalculate' */
			//if (this.isDirty) {
				preCalculate(outPlot);
				this.isDirty = false; // reset		
			//}
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
	
	private void checkPlot() throws InvalidArrayTypeException {
		try {
			if (inPlot.getRank() > 2) {
					inPlot.reduce();
			}
		} catch (PlotFactoryException e) {
			if (isDebugMode) e.printStackTrace();
			throw new InvalidArrayTypeException();
		}
		int rank = inPlot.getRank();
		if (3<rank) {
			throw new InvalidArrayTypeException();
		} else if (2<rank){
					/* process mapset or set plot to last two dimensions ? */
				}		
	}
	
	private Double updateParameter(String key, Double par, Double tol) throws IOException {
		double ref = par;
		IDataItem item = inPlot.findDataItem(key);
		
		
		if (null==item) {
			item = inPlot.findCalculationData(key);
		}
		par = item.getData().getArrayMath().getMaximum();
		this.isDirty = this.isDirty || (Math.abs(ref-par)>tol);
		return par;
	}
	
	private void checkParameters() throws IOException {
		/**
		 * check whether the following variables have changed
		 * 	- centreX
		 *  - centreZ
		 *  - wavelength
		 *  - L2mm
		 * If so, flag the requirement to recalculate plot axes
		 */
		double tolCentroid = 2.0; //mm
		double tolWavelength = 0.05; //Ao
		double tolL2mm = 5.0; //mm

		this.centroidX = updateParameter(KEY_CENTERX,this.centroidX,tolCentroid);
		this.centroidZ = updateParameter(KEY_CENTERZ,this.centroidZ,tolCentroid);
		this.wavelength = updateParameter(KEY_WAVELENGTH,this.wavelength,tolWavelength);
		this.l2mm = updateParameter(KEY_L2MM,this.l2mm,tolL2mm);
	}
	
	private void preCalculate(Plot inputPlot)
		throws 	IOException, 
				SignalNotAvailableException, 
				IndexOutOfBoundException, 
				InvalidArrayTypeException 
	{
		//prepareParameters();
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

		IArray rToBeamCenter = Factory.createArray(Double.TYPE, shape);
		IArray RToSample = Factory.createArray(Double.TYPE, shape);
		IArray Q = Factory.createArray(Double.TYPE, shape);
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
		} else {
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
		} else {
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
			throw new SignalNotAvailableException(e);
		} 

		PlotIterator inputIterator = new PlotIterator(inputPlot);
		IArrayIterator rIterator = rToBeamCenter.getIterator();
		IArrayIterator RIterator = RToSample.getIterator();
		IArrayIterator thetaIterator = theta.getIterator();
		IArrayIterator QIterator = Q.getIterator();
		IArrayIterator binAreaIterator = binArea.getIterator();
		IArrayIterator deltaOmigaIterator = deltaOmiga.getIterator();
		binWidthZIterator = binWidthZ.getIterator();
		binWidthXIterator = binWidthX.getIterator();
		double binWidthZValue = binWidthZIterator.getDoubleNext();
		
		while (inputIterator.hasNext()){
			Point    point = inputIterator.getPointNext();
			double[] coord = point.getCoordinate();
			double   r = Math.hypot(coord[0]-centroidZ,coord[1]-centroidX);
			         rIterator.next().setDoubleCurrent(r);
			double   R = Math.hypot(r,l2mm);
			         RIterator.next().setDoubleCurrent(R);
			double   sinTheta = Math.sqrt((1 - l2mm / R) / 2);
			         thetaIterator.next().setDoubleCurrent(Math.asin(sinTheta));
			double   q = 4.0 * Math.PI * sinTheta / wavelength;
			         QIterator.next().setDoubleCurrent(q);
			         
			if (! binWidthXIterator.hasNext()){
				binWidthXIterator = binWidthX.getIterator();
				binWidthZValue = binWidthZIterator.getDoubleNext();
			}
			double area = binWidthZValue * binWidthXIterator.getDoubleNext();
			binAreaIterator.next().setDoubleCurrent(area);
			deltaOmigaIterator.next().setDoubleCurrent(area * l2mm / R / R);
		}

		inputPlot.addCalculationData(QuokkaConstants.CENTROID_NAME, 
				Factory.createArray(
				new double[]{centroidZ, centroidX}), 
				"Centroid", 
				"mm", 
				null);
		inputPlot.addCalculationData(QuokkaConstants.DISTANCE_TO_BEAMCENTER_NAME, 
				rToBeamCenter, 
				"Distance to Beam Center", 
				"mm", 
				null);
		inputPlot.addCalculationData(QuokkaConstants.DISTANCE_TO_SAMPLE_NAME, 
				RToSample, 
				"Distance to Sample", 
				"mm", 
				null);
		inputPlot.addCalculationData(QuokkaConstants.THETA_NAME, 
				theta, 
				"Theta", 
				"rad", 
				null);
		inputPlot.addCalculationData(QuokkaConstants.Q_NAME, 
				Q, 
				"Q", 
				"mm", 
				null);
		inputPlot.addCalculationData(QuokkaConstants.BIN_AREA_NAME, 
				binArea, 
				"Area of Bin", 
				"mm2", 
				null);
		inputPlot.addCalculationData(QuokkaConstants.DELTA_OMEGA_NAME, 
				deltaOmiga, 
				"delta Omega", 
				"", 
				null);

		Double largestQ = Q.getArrayMath().getMaximum();
		double largestArea = binArea.getArrayMath().getMaximum();
		
		Double largestQBin = 
			Math.PI * Math.sqrt(8.0*(1-l2mm/Math.hypot(l2mm,largestArea)))
				/wavelength; 
		
		IDataItem QBoundary = Factory.createDataItem(inputPlot, 
				"QBoundary", 
				Factory.createArray(largestQ.toString().toCharArray()));
		inputPlot.addDataItem(QBoundary);
		
		IDataItem QBin = Factory.createDataItem(inputPlot, 
				"QBin", 
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
				Math.sqrt(binSizeX * binSizeX + binSizeZ * binSizeZ + l2mm * l2mm));
		
		IDataItem twoThetaBoundary = Factory.createDataItem(inputPlot, "twoThetaBoundary", 
				Factory.createArray(largestTwoTheta.toString().toCharArray()));
		inputPlot.addDataItem(twoThetaBoundary);
		
		IDataItem twoThetaBin = Factory.createDataItem(inputPlot, "twoThetaBin", 
				Factory.createArray(largestTwoThetaBin.toString().toCharArray()));
		inputPlot.addDataItem(twoThetaBin);
	}

	private Double prepareParameter(Double value, String shortName, String fieldName)
		throws IOException
	{
		Double retVal = value;
		if (value==null) {
			IDataItem item = inPlot.findDataItem(shortName);
			Double dataVal = 0.0;
			IIndex ima = item.getData().getIndex();
			dataVal = item.getData().getDouble(ima.set(0));
			if (dataVal!=null) {
				retVal = dataVal;
				informVarValueChange(fieldName, retVal);
			}
		}
		return retVal;
	}
	
	private void prepareParameters() throws IOException {
	centroidZ = prepareParameter(centroidZ, "BeamCenterZ", "centroidZ");
	centroidX = prepareParameter(centroidX, "BeamCenterX", "centroidX");
	wavelength = prepareParameter(wavelength, "LambdaA", "preCalculation_lambda");
	l2mm = prepareParameter(defaultL2mm, "L2mm", "preCalculation_L2mm");

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

	public void setSynch(Boolean synch) {
		this.synch = synch;
	}

	public void setCentroidZ(Double centroidZ) {
		this.centroidZ = centroidZ;
		//this.isDirty = true;
	}

	public void setCentroidX(Double centroidX) {
		this.centroidX = centroidX;
		//this.isDirty = true;
	}

	/* Out-Ports ----------------------------------------------------*/

	public Plot getOutPlot() {
		return this.outPlot;
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

    /* Var-Ports (tuners) -------------------------------------------*/

	public void setWavelength(Double wavelength) {
		this.wavelength = wavelength;
	}

	public void setL2mm(Double l2mm) {
		this.l2mm = l2mm;
	}

}
