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

import java.util.Formatter;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Data;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.kowari.dra.internal.InternalConstants;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 14/11/2008
 */
public class CalculateTTh extends ConcreteProcessor {

	private final static double DEGREE_RAD_COEFFICIENT = 180 / Math.PI;
	private Plot inputPlot;
	private Plot outputPlot;
	private Boolean skipTwoTheta = false;
	private Double sampleToDetector;
	
	/**
	 * 
	 */
	public CalculateTTh() {
		super();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.nbi.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		setReprocessable(false);
		if (skipTwoTheta){
			outputPlot = inputPlot;
			return false;
		}
		try{
			sampleToDetector = getSDD(inputPlot);
		}catch (Exception e) {
			LoggerFactory.getLogger(getClass()).info("can not read kowari property: " 
					+ InternalConstants.SAMPLE_TO_DETECTOR_DISTANCE_NAME);
		}
		Data inputData = inputPlot.findSingal();
		IArray inputArray = inputPlot.findSignalArray();
		Axis binOffset = inputPlot.getAxis(inputPlot.getAxisList().size() - 1);
		String units = binOffset.getUnitsString();
//		int[] shape = inputArray.getShape();		

//		if (units != null && units.equals("mm")){
		if (true){
			IArray binOffsetArray = binOffset.getData();
			IArray twoThetaArray;
			IArray stthArray = null;
			try{
				stthArray = inputPlot.findDataItem("stth").getData();
			}catch (Exception e) {
				try{
					stthArray = inputPlot.findDataItem("old_stth").getData();
				}catch (Exception e1) {
					throw new SignalNotAvailableException("can not find stth from the nexus data");
				}
			}
			double maxStth = stthArray.getArrayMath().getMaximum();
			double minStth = stthArray.getArrayMath().getMinimum();
			if (maxStth == minStth){
				maxStth = maxStth / DEGREE_RAD_COEFFICIENT;
				double sinStth = Math.sin(maxStth);
				double cosStth = Math.cos(maxStth);
				twoThetaArray = Factory.createArray(Double.TYPE, new int[]{(int) binOffsetArray.getSize()});
				IArrayIterator binIterator = binOffsetArray.getIterator();
				IArrayIterator twoThetaIterator = twoThetaArray.getIterator();
				while(twoThetaIterator.hasNext()){
					twoThetaIterator.next().setDoubleCurrent(getAngle(cosStth, sinStth, binIterator.getDoubleNext()));
				}
			}else{
				twoThetaArray = Factory.createArray(Double.TYPE, new int[]{(int) stthArray.getSize(), 
						(int) binOffsetArray.getSize()});
				IArrayIterator stthIterator = stthArray.getIterator();
				IArrayIterator twoThetaIterator = twoThetaArray.getIterator();
				while(stthIterator.hasNext()){
					double stth = stthIterator.getDoubleNext() / DEGREE_RAD_COEFFICIENT;
					double sinStth = Math.sin(stth);
					double cosStth = Math.cos(stth);
					IArrayIterator binIterator = binOffsetArray.getIterator();
					while(binIterator.hasNext()){
						twoThetaIterator.next().setDoubleCurrent(getAngle(cosStth, sinStth, binIterator.getDoubleNext()));
					}
				}
			}
//			if (binOffsetArray.getSize() == shape[shape.length - 1])
//				while(twoThetaIterator.hasNext()){
//					twoThetaIterator.setDoubleNext(getAngle(stth, binIterator.getDoubleNext()));
//				}
//			else if (binOffsetArray.getSize() == shape[shape.length - 1] + 1){
//				double lowerOffset = binIterator.getDoubleNext();
//				while(twoThetaIterator.hasNext()){
//					double upperOffset = binIterator.getDoubleNext();
//					twoThetaIterator.setDoubleNext(getAngle(stth, (lowerOffset + upperOffset) / 2.));
//				}
//			}
//			else 
//				throw new Exception("can not convert the two theta, wrong array size");
			outputPlot = (Plot) PlotFactory.createPlot(inputPlot, inputPlot.getShortName() + "_twoTheta", 
					inputPlot.getDimensionType());
			outputPlot.addData(inputData.getShortName(), inputArray, inputData.getTitle(), 
					inputData.getUnitsString(), inputPlot.findVarianceArray());
			List<Axis> axisList = inputPlot.getAxisList();
			axisList.remove(binOffset);
			outputPlot.copyAxes(axisList);
			outputPlot.addAxis("twoTheta", twoThetaArray, "Two Theta", "degrees", axisList.size());
			((NcGroup) outputPlot).setLocation(inputPlot.getLocation());
//			outputPlot.addLog(inputPlot.getProcessingLog(), null);
//			String stthString;
//			if (stthArray.getMaximum() == stthArray.getMinimum())
//				stthString = (new Formatter()).format("%.5f", stthArray.getMaximum()).toString();
//			else 
//				stthString = stthArray.toString();
			outputPlot.addProcessingLog("calculating two theta on LDS=" + 
					(new Formatter()).format("%.1f", sampleToDetector) + " mm");
			inputPlot.getGroupList().clear();
		}else
			outputPlot = inputPlot;
		return false;
	}

	private double getAngle(double cosStth, double sinStth, double binCenter) {
		
		return Math.acos((sampleToDetector * cosStth - binCenter * sinStth) / Math.sqrt(
				sampleToDetector * sampleToDetector + binCenter * binCenter)) * DEGREE_RAD_COEFFICIENT;
	}

//	private double getAngle(double stth, double binCenter) {
//		
//		return stth + Math.atan(binCenter / sampleToDetector) * 180 / Math.PI;
//	}

	/**
	 * @return the outputPlot
	 */
	public Plot getOutputPlot() {
		return outputPlot;
	}

	/**
	 * @param inputPlot the inputPlot to set
	 */
	public void setInputPlot(Plot inputPlot) {
		this.inputPlot = inputPlot;
	}

	/**
	 * @param sampleToDetector the sampleToDetector to set
	 */
	public void setSampleToDetector(Double sampleToDetector) {
		this.sampleToDetector = sampleToDetector;
	}

	/**
	 * @param skipTwoTheta the skipTwoTheta to set
	 */
	public void setSkipTwoTheta(Boolean skipTwoTheta) {
		this.skipTwoTheta = skipTwoTheta;
	}

	public static Double getSDD(Plot plot){
		try{
			double value = plot.getDataItem("sample_to_detector_distance").getData().getArrayMath().getMaximum();
			return Double.valueOf(value);
		}catch (Exception e) {
			System.out.println("can not find sample to detector distance value from the file");
		}
		String distanceValue = System.getProperty(
				InternalConstants.SAMPLE_TO_DETECTOR_DISTANCE_NAME);
		return Double.valueOf(distanceValue);
	}
}
