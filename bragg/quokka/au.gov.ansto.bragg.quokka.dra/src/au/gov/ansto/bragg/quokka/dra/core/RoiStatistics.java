/******************************************************************************* 
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong  (based on Class StatisticCalculation 22/12/2008)
 *    Paul Hathaway (27/01/2009)
 *******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core; 

// import java.net.URI;  - alternate source of tx data

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.math.EData;
import org.gumtree.data.math.EMath;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.region.RegionUtils;
import au.gov.ansto.bragg.datastructures.util.AxisRecord;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class RoiStatistics extends ConcreteProcessor {

	private static final String processClass = "RoiStatistics"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009012701; 

	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = true;

    private Plot inPlot;
	private Plot outPlot;
    private Boolean doForceSkip = true;
    private Boolean doForceStop = false;
	private IGroup   statsRoi;
    private Boolean isCalculated = false;
    
    private Double centroidX = 96.0 * 5.08; // (mm)
    private Double centroidZ = 96.0 * 5.08; // (mm)
// TODO: Add additional ROI statistics
//    private Double sum = 0.0;
//    private Double variance;
//    private Double rms;
//    private Double roiMax;
//    private Double roiMin;

	public Boolean process() throws Exception {

// TODO: Add additional ROI statistics
//		Array centroidArray;
//		Array centroidVariance;
//		Array totalSumArray;
//		Array totalSumVariance;
//		Array RMS;
//		Array RMSVariance;
//		Array maxArray;
//		Array maxVariance;
//		Array minArray;
//		Array minVariance;
		
		EData<IArray> centroidEData;
		
    	this.isCalculated = false; // clear flag
    	this.outPlot = this.inPlot;
    	stampProcessLog();
    	
		if (!this.doForceSkip) {

			// May be required for managing additional ROI statistics
			switch (inPlot.getDimensionType())
        	{
        		case map:
        			break;
        		case mapset:
        			break;
        		default:
        			throw new IllegalArgumentException(
        					"DataDimensionType of {map|mapset} required.");
        	}

        	centroidEData = calculateCentreProcedure();
        	setCentroid(centroidEData);
        	
        	publishParameterSet(centroidEData);
        	this.isCalculated = true;
        } else {
        	stampProcessSkip();
        }
        return this.doForceStop;
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
	
	private void setCentroid(EData<IArray> centroidEData) {
		double[] centroid;
    	IArray data = centroidEData.getData();
    	centroid = (double[]) data.getArrayUtils().copyTo1DJavaArray();
    	if (centroid.length > 1) {
    		this.centroidX = centroid[1];
    		this.centroidZ = centroid[0];
    	}

    	if (isDebugMode) {
    		System.out.println("centroid "+data);
    		System.out.println("error    "+centroidEData.getVariance());
    	}
	}

	private void publishParameterSet(EData<IArray> centroidEData) throws InvalidArrayTypeException 
	{
		outPlot.addCalculationData(
				"centroid", 
				centroidEData.getData(), 
				"centroid", 
				"mm", 
				centroidEData.getVariance());

    	if (isDebugMode) {
    		System.out.println("notify centroid start");
    	}
		super.informVarValueChange("centroidX", centroidX);
		super.informVarValueChange("centroidZ", centroidZ);
    	if (isDebugMode) {
    		System.out.println("notify centroid end");
    	}
	}
		
	private EData<IArray> calculateCentreProcedure()
	throws ShapeNotMatchException, IOException, StructureTypeException
	{
		IArray countData = inPlot.findSignalArray();
		IArray countVariance = inPlot.findVarianceArray();
		IArray countRoi;
		IArray centroidArray;
		IArray centroidVariance;
		List<Axis> allAxes = inPlot.getAxisList();
		int[] shape = inPlot.findSignalArray().getShape();
		EData<IArray> centroidEData;
		
		List<Axis> axes = new ArrayList<Axis>();

    	switch (inPlot.getDimensionType())
    	{
    		case map:
    			axes.add(allAxes.get(allAxes.size() - 2));
    			axes.add(allAxes.get(allAxes.size() - 1));
    			
    			countRoi = RegionUtils.applyRegion(inPlot, statsRoi);
    			
    			centroidEData = calculateCentroid(
    					countRoi.getArrayUtils().reduce().getArray(), 
    					countRoi.getArrayUtils().reduce().getArray(), 
    					axes, 
    					null, 
    					null);
    			break;
    		case mapset:
    			centroidArray = Factory.createArray(Double.TYPE, new int[]{shape[0], 2});
    			centroidVariance = Factory.createArray(Double.TYPE, new int[]{shape[0], 2});
    			axes.add(allAxes.get(allAxes.size() - 2));
    			axes.add(allAxes.get(allAxes.size() - 1));
    			for (int i = 0; i < shape[0]; i++) {
    				IArray dataSlice = countData.getArrayUtils().slice(0,i).getArray();
    				IArray varianceSlice = countVariance.getArrayUtils().slice(0, i).getArray();
    				IArray centroidSlice = centroidArray.getArrayUtils().slice(0, i).getArray();
    				IArray centroidVarianceSlice = centroidVariance.getArrayUtils().slice(0, i).getArray();
    				calculateCentroid(
    						dataSlice, 
    						varianceSlice, 
    						axes, 
    						centroidSlice, 
    						centroidVarianceSlice);
    			}
    			centroidEData = new EData<IArray>(centroidArray, centroidVariance);
    			break;
    		default:
    			throw new IllegalArgumentException(
    					"DataDimensionType of {map|mapset} required.");
    	}
    	return centroidEData;	
	}
	
	private EData<IArray> calculateCentroid(
			IArray data, 
			IArray variance, 
			List<Axis> axes, 
			IArray resultArray,
			IArray resultVariance) 
	throws ShapeNotMatchException, IOException 
	{
		int rank = data.getRank();
		if (axes.size() != rank) {
			throw new ShapeNotMatchException("the axes size does not match the data");
		}
		if (resultArray == null) { 
			resultArray = Factory.createArray(Double.TYPE, new int[]{rank});
		}
		if (resultVariance == null) {
			resultVariance = Factory.createArray(Double.TYPE, new int[]{rank});
		}
		IIndex resultIndex = resultArray.getIndex();
		for(int i = 0; i < rank; i ++)
		{
			resultIndex.set(i);
			AxisRecord axis = AxisRecord.createRecord(axes.get(i), i, data.getShape());
			IArray widthArray = Factory.createArray(Double.TYPE, new int[]{(int) axis.length()});
			IIndex widthIndex = widthArray.getIndex();
			for (int j = 0; j < widthArray.getSize(); j++) 
				widthArray.setDouble(widthIndex.set(j), axis.width(j));
			
			EData<IArray> sumForDimension = EMath.sumForDimension(data, i, variance);
			EData<Double> centroid = EMath.vecDot(sumForDimension.getData(), axis.centres(), 
					sumForDimension.getVariance(), widthArray);
			EData<Double> totalSum = EMath.sum(sumForDimension.getData(), sumForDimension.getVariance());
			resultArray.setDouble(resultIndex, centroid.getData() / totalSum.getData());
			resultVariance.setDouble(resultIndex, centroid.getVariance() / totalSum.getVariance() / totalSum.getVariance());
		}
		return new EData<IArray>(resultArray, resultVariance);
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
    
    /* In-Ports */

	public void setInPlot(Plot inPlot) {
		this.inPlot = (Plot) inPlot;
	}

    /* Out-Ports */

	public Plot getOutPlot() {
		return outPlot;
	}    

	public Double getCentroidX() {
		return centroidX;
	}

	public Double getCentroidZ() {
		return centroidZ;
	}

//    public Double getSum() {
//		return sum;
//	}
//
//	public Double getVariance() {
//		return variance;
//	}
//
//	public Double getRms() {
//		return rms;
//	}
//
//	public Double getRoiMax() {
//		return roiMax;
//	}
//
//	public Double getRoiMin() {
//		return roiMin;
//	}

	/* Var-Ports (tuners) */

	public void setStatsRoi(IGroup statsRoi) {
		this.statsRoi = statsRoi;
	}

	/* Var-Ports (options) */

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
