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
package au.gov.ansto.bragg.quokka.exp.core.scanfunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.kakadu.core.data.DataType;
import au.gov.ansto.bragg.kakadu.dom.PlotDOM;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotDataItem;
import au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment;
import au.gov.ansto.bragg.quokka.exp.core.ScanResult;
import au.gov.ansto.bragg.quokka.exp.core.exception.GetDataFailedException;
import au.gov.ansto.bragg.quokka.exp.core.exception.PlotErrorException;
import au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice;

/**
 * @author nxi
 *
 */
public abstract class Function {

	public final static String FUNCTION_PACKAGE_NAME = 
		"au.gov.ansto.bragg.quokka.exp.core.scanfunction";
//	protected VisDOM kuranda;
	protected Composite composite;
	protected String plotTitle;
//	protected int numberOfEntry;
	protected double[] entryArray;
	protected IDataItem xAxis;
	protected List<Double> scanhistory;
	protected double peak;
	protected double max;
	protected double min;
	protected double centroid;
	protected double mean;
	protected double RMS;
	protected double total;
	protected VirtualDevice device;
//	protected List<PlotDOM> plotList;
//	protected List<Group> groupDataList;
	public enum FunctionalStatistic{peak, max, min, centroid};
	public enum NonFunctionalStatistic{mean, RMS, total};
	
	protected PlotDOM plot1D = null;
	protected PlotDataItem plotDataItem = null;

	protected Function(){
		peak = Double.NaN;
//		plotList = new ArrayList<PlotDOM>();
//		groupDataList = new ArrayList<Group>();
	}
	
	public List<Double> dataList = null;

	public abstract void addData(IGroup databag) throws GetDataFailedException;

	public void plotLastMarker() throws PlotErrorException{
//		double horizontalValue = (double) dataList.size() - 1;
		double horizontalValue = entryArray[dataList.size() - 1];
		double verticalValue = dataList.get(dataList.size() - 1);
		addMarker(horizontalValue, verticalValue);
	}

	protected void addMarker(final double horizontalValue, final double verticalValue) throws PlotErrorException{
//		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
//		plot1D.getDisplay().syncExec(new Runnable() {
//			public void run()  {
//				try {
//					System.out.println(plotTitle + " add marker " + horizontalValue + ", " + verticalValue);
//					plot1D.addMarker(horizontalValue , verticalValue);
////					plot1D.setYViewExtents(yMin, yMax)
////					kuranda.rPlot();
//					plot1D.refreshPlot();
//				} catch (KurandaException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
////					throw new PlotErrorException("failed to add a marker in the plot" + e.getMessage());
//				}
//			}
//		});
		while (plot1D == null || plot1D.isDisposed()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			double[] doubleArray = listToArray();
			IGroup dataGroup = makeDataGroup(doubleArray, entryArray);
//			plot1D.setDataSource(doubleArray, plotTitle);
//			plot1D.addMarker(horizontalValue , verticalValue);
//			plot1D.setDataSource(dataGroup);
			plot1D.plot(plotDataItem, dataGroup);
//			try {
//				System.out.println("Thread is sleeping");
//				Thread.sleep(10);
//			} catch (Exception e) {
//				// TODO: handle exception
//				e.printStackTrace();
//			}
//			System.out.println("Thread wakes up");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		try {
			setTitle(plotTitle + "(" + getUITitle() + ")");
			setViewExtents();
//			plot1D.refreshPlot();
//			plot1D.notifyRefreshPlot();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setViewExtents() {
		// TODO Auto-generated method stub
		double xExtentsMax = dataList.size();
		double yExtentsMax = getMax(listToArray());
		double yExtentsMin = getMin(listToArray());
		double extra = (yExtentsMax - yExtentsMin) / 10.;
		yExtentsMax += extra;
		yExtentsMin = yExtentsMin - extra;
//		plot1D.setXViewExtents(0, xExtentsMax);
//		plot1D.setYViewExtents(yExtentsMin, yExtentsMax);
	}

	public void addDoubleData(Double data) throws GetDataFailedException{
		if (dataList == null) dataList = new ArrayList<Double>();
		dataList.add(data);
		findPeak();
		updateUITitle();
	}

	protected double getMax(double[] array){
		double result = 0;
		if (array != null && array.length > 0){
			result = array[0];
			for (int i = 1; i < array.length; i++) {
				if (array[i] > result) result = array[i];
			}
		}
		return result;
	}

	protected double getMin(double[] array){
		double result = 0;
		if (array != null && array.length > 0){
			result = array[0];
			for (int i = 1; i < array.length; i++) {
				if (array[i] < result) result = array[i];
			}
		}
		return result;
	}

	protected double[] listToArray() {
		// TODO Auto-generated method stub
		double[] array = new double[dataList.size()];
		Iterator<Double> iterator =dataList.iterator();
		for (int i = 0; i < array.length; i++) {
			if(iterator.hasNext()) array[i] = iterator.next().doubleValue();
		}
		return array;
	}

	public PlotDOM plot(final Composite composite) 
	throws PlotErrorException {
		// TODO Auto-generated method stub
		clearPeak();
		this.composite = composite;
//		this.entryArray = entryArray;
		final double[] plotData = listToArray();
//		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
////		plot1D.getDisplay().syncExec(new Runnable() {
//			public void run()  {
//				try {
////					System.out.println();
//					plot1D = kuranda.plot1d(composite, makeDataGroup(plotData, numberOfEntry));
//					plot1D.notifyRefreshPlot();
////					plot1D.setYViewExtents(getMin(plotData), getMax(plotData));
////					plot1D.refreshPlot();
//				} catch (Exception e) {
//					// TODO: handle exception
////					throw new PlotErrorException("failed to initialize the plot " + e.getMessage());
//					e.printStackTrace();
//				}
//			}
//		});
		try {
//			System.out.println();
//			plot1D = kuranda.plot1d(composite, makeDataGroup(plotData, entryArray));
//			plot1D.setYViewExtents(1.2 * getMin(plotData), 0.8 * getMax(plotData));
//			plot1D.notifyRefreshPlot();
			plot1D = PlotDOM.getPlotDOM(composite, DataType.Pattern);
			IGroup plotGroup = makeDataGroup(plotData, entryArray);
			plotDataItem = new PlotDataItem(plotGroup, DataType.Pattern);
			plot1D.plot(plotDataItem, plotGroup);
//			plotList.add(plot1D);
//			plot1D.refreshPlot();
		} catch (Exception e) {
			// TODO: handle exception
//			throw new PlotErrorException("failed to initialize the plot " + e.getMessage());
			e.printStackTrace();
		}
		return plot1D;
	}

	private IGroup makeDataGroup(double[] plotData, double[] entryArray) throws IOException, InvalidArrayTypeException, PlotFactoryException {
		// TODO Auto-generated method stub
		IGroup dataGroup = Factory.createGroup(plotTitle);
		IDataItem item = Factory.createDataItem(dataGroup, plotTitle, Factory.createArray(plotData));
		
		IArray xAxisArray = null;
			xAxisArray = Factory.createArray(entryArray);
			xAxis = Factory.createDataItem(dataGroup, "scan_ID", xAxisArray);
		double yMax = getMax(plotData);
		double yMin = getMin(plotData);
		if (yMax == yMin) {
			yMax = yMax * 1.001;
			yMin = yMin * 0.999;
		}
		int yAxisSize = 8;
		double extra = (yMax - yMin) / yAxisSize;
		yMax += extra;
		yMin -= extra;
		double scale = ((double) (yMax - yMin))/ yAxisSize;
		double[] yAxisStorage = new double[yAxisSize];
		for (int i = 0; i < yAxisStorage.length; i++) {
			yAxisStorage[i] = yMin + i * scale;
		}
//		Array yAxisArray = Factory.createArray(yAxisStorage);
//		DataItem yAxis = Factory.createDataItem(dataGroup, plotTitle + " _Value", yAxisArray);
//		dataGroup.buildResultGroup(item, yAxis, xAxis);
//		dataGroup.buildResultGroup(item, xAxis);
//		xAxis.addOneAttribute("units", "");
//		dataGroup.addDataItem(item);
//		System.out.println(dataGroup);
		
//		double[] yAxisStorage = new double[1000];
		dataGroup = PlotFactory.createPlot(Factory.createGroup("Plot Set"), 
				plotTitle, StaticDefinition.DataDimensionType.pattern);
		PlotFactory.addDataToPlot(dataGroup, plotTitle + " data", Factory.createArray(plotData), plotTitle, "none");
		
//		PlotFactory.addAxisToPlot(dataGroup, plotTitle, yAxisArray, plotTitle, "", 0);
		PlotFactory.addAxisToPlot(dataGroup, "scanNumber", xAxisArray, "Scan", "scan", 1);
		
		return dataGroup;
	}

	public void rePlot() throws PlotErrorException {
		// TODO Auto-generated method stub
//		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
//		plot1D.getDisplay().syncExec(new Runnable() {
//			public void run()  {
//				try {
//					double[] doubleArray = listToArray();
//					Group dataGroup = makeDataGroup(doubleArray, numberOfEntry);
////					plot1D.setDataSource(doubleArray, plotTitle);
//					plot1D.setDataSource(dataGroup);
//					plot1D.setYViewExtents(getMin(doubleArray), getMax(doubleArray));
//					plot1D.refreshPlot();
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
////					throw new PlotErrorException("failed to re-plot the whole array");
//				}
////				System.out.println(plotTitle + " try to replot.");
////				kuranda.rPlot();
//			}
//		});
		try {
			double[] doubleArray = listToArray();
			IGroup dataGroup = makeDataGroup(doubleArray, entryArray);
//			plot1D.setDataSource(doubleArray, plotTitle);
//			plot1D.setDataSource(dataGroup);
			plot1D.plot(plotDataItem, dataGroup);
//			groupDataList.add(dataGroup);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		try {

//			plot1D.setYViewExtents(getMin(doubleArray), getMax(doubleArray));
			setViewExtents();
//			plot1D.refreshPlot();
//			plot1D.notifyRefreshPlot();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//			throw new PlotErrorException("failed to re-plot the whole array");
		}
	}

	public void clearDataHistory(){
		if (dataList!= null)
//		dataList.clear();
		dataList = new ArrayList<Double>();
		xAxis = null;
		entryArray = null;
	}

	public String getUITitle(){
//		String result = getClass().getSimpleName();
		String result = "";
		if (device != null){
//			result += " on device: " + device.getId() + " -- ";
			result += "ctr=" + (new Formatter()).format("%.2f", centroid) + ", RMS=" + 
				(new Formatter()).format("%.2f", RMS) + ", peak=" + peak + ", ttl=" + 
				(new Formatter()).format("%.2f", total) + ", max=" + 
				(new Formatter()).format("%.2f", max) + ", min=" + 
				(new Formatter()).format("%.2f", min);
		}
		return result;
	}
	
	public String toString(){
		String result = getClass().getSimpleName();
		if (device != null){
			result += " on device: " + device.getId() + "\n";
			result += "peak=" + peak + ", max=" + max + ", min=" + min + 
			", RMS=" + (new Formatter()).format("%.2f", RMS) + ", total=" + 
			(new Formatter()).format("%.2f", total) + ", centroid=" +
			(new Formatter()).format("%.2f", centroid) + "\n";
		}
		return result;
	}

	public abstract String getShortDescription();

	protected double findPeak() {
		int maxIndex = findMaxIndex();
		max = entryArray[maxIndex];
		int minIndex = findMinIndex();
		min = entryArray[minIndex];
		centroid = findCentroidIndex();
		boolean isMaxAtEnd = maxIndex == 0 || maxIndex == dataList.size() - 1;
		boolean isMinAtEnd = minIndex == 0 || minIndex == dataList.size() - 1;
		if (isMaxAtEnd && !isMinAtEnd) peak = entryArray[minIndex];
		if (!isMaxAtEnd && isMinAtEnd) peak = entryArray[maxIndex];
		if (!isMaxAtEnd && !isMinAtEnd) 
			try{
				peak = entryArray[comparePeak(maxIndex, minIndex)];
			}catch (GetDataFailedException e) {
				// TODO: handle exception
				peak = Double.NaN;
			}
		if (isMaxAtEnd && isMinAtEnd) {
			peak = Double.NaN;
		}
		if (isMainScanFunction())
			QuokkaExperiment.updateResult(getScanResult());
		return peak;
	}

	private double findCentroidIndex() {
		// TODO Auto-generated method stub
		double centroidValue = 0;
		double weightSum = 0;
		total = 0;
		RMS = 0;
		int index = 0;
		for (Iterator iterator = dataList.iterator(); iterator.hasNext();) {
			Double value = (Double) iterator.next();
			total += value;
			RMS += value * value;
			weightSum += value * entryArray[index];
			index ++;
		}
		centroidValue = weightSum / total;
		RMS = Math.sqrt(RMS / dataList.size());
		return centroidValue;
	}

	private int comparePeak(int maxIndex, int minIndex) throws GetDataFailedException {
		// TODO Auto-generated method stub
//		double maxSum = 0;
//		double minSum = 0;
//		double maxValue = dataList.get(maxIndex);
//		double minValue = dataList.get(minIndex);
//		for (Iterator iterator = dataList.iterator(); iterator.hasNext();) {
//			Double value = (Double) iterator.next();
//			maxSum += Math.abs(value - maxValue);
//			minSum += Math.abs(value - minValue);
//		}
//		if (maxSum < minSum) return maxIndex;
//		if (minSum < maxSum) return minIndex;
//		if (minSum == maxSum) throw new GetDataFailedException("Can not find a peak");
		double center = (dataList.size() - 1.) / 2.;
		double maxDistance = Math.abs(maxIndex - center);
		double minDistance = Math.abs(minIndex - center);
		if (maxDistance < minDistance) return maxIndex;
		if (minDistance < maxDistance) return minIndex;
		if (minDistance == maxDistance) throw new GetDataFailedException("Can not find a peak");
		return 0;
	}

	private int findMinIndex() {
		// TODO Auto-generated method stub
		int minIndex = 0;
		double minValue = dataList.get(0);
		int index = 0;
		for (Iterator iterator = dataList.iterator(); iterator.hasNext();) {
			Double value = (Double) iterator.next();
			if (value < minValue){
				minValue = value;
				minIndex = index;
			}
			index ++;
		}
		return minIndex;
	}

	private int findMaxIndex() {
		// TODO Auto-generated method stub
		int maxIndex = 0;
		double maxValue = dataList.get(0);
		int index = 0;
		for (Iterator iterator = dataList.iterator(); iterator.hasNext();) {
			Double value = (Double) iterator.next();
			if (value > maxValue){
				maxValue = value;
				maxIndex = index;
			}
			index ++;
		}
		return maxIndex;
	}

	public String getPlotTitle() {
		return plotTitle;
	}

	public double getPeak() {
		return peak;
	}
	
	public void clearPeak(){
		peak = Double.NaN;
	}
	
	public double getStatistic(FunctionalStatistic name){
		switch (name) {
		case peak:
			return peak;
		case max:
			return max;
		case min: 
			return min;
		case centroid: 
			return centroid;
		default:
			return Double.NaN;
		}
	}

	public double getStatistic(NonFunctionalStatistic name){
		switch (name) {
		case mean:
			return mean;
		case RMS:
			return RMS;
		case total: 
			return total;
		default:
			return Double.NaN;
		}
	}
	
	public VirtualDevice getDevice() {
		return device;
	}

	public void setDevice(VirtualDevice device) {
		this.device = device;
	}

	public void setEntryArray(double[] entryArray) {
		this.entryArray = entryArray;
	}
	
	public ScanResult getScanResult(){
		return new ScanResult(this);
	}
	
	private boolean isMainScanFunction(){
		Function experimentMainFunction;
		try {
			experimentMainFunction = QuokkaExperiment.getFunctionList().get(0);
		}catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		if (experimentMainFunction == this)
			return true;
		return false;
	}

	private void updateUITitle(){
		if (composite != null && !composite.isDisposed())
			composite.getDisplay().asyncExec(new Runnable(){

				public void run() {
					// TODO Auto-generated method stub
					Composite parent = composite.getParent();
					if (parent instanceof CTabFolder) {
						CTabFolder folder = (CTabFolder) parent;
						folder.getItem(0).setText(getUITitle());
					}
				}
				
			});
	}

	/**
	 * @return the plot1D
	 */
	public PlotDOM getPlot1D() {
		return plot1D;
	}

	public void setTitle(String title) {
		// TODO Auto-generated method stub
		plot1D.setTitle(title);
	}
}
