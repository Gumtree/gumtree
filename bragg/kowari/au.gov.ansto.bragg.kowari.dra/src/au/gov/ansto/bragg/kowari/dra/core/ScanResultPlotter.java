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

import org.gumtree.data.Factory;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.math.EData;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.exception.IndexOutOfBoundException;
import au.gov.ansto.bragg.datastructures.core.exception.PlotMathException;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.core.plot.Point;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 04/09/2008
 */
public class ScanResultPlotter extends ConcreteProcessor {

	private IGroup scanResultPlotter_fitResultGroup;
	private IGroup scanResultPlotter_intensityGroup;
	private IGroup scanResultPlotter_outputGroup;
	private String scanResultPlotter_yAxisName = "Intensity";
	private String scanResultPlotter_fitParameter = "mean";
	private Boolean scanResultPlotter_stop = false;
	private Boolean scanResultPlotter_skip = false;
//	private Double scanResultPlotter_a;
	private YAxisType yType = YAxisType.Intensity;
	
	private enum YAxisType{
		Intensity ("Intensity"),
		LogI ("LOG(I)"),
		Peak ("Peak"), 
		FittingResult ("Fitting Result");
		
		public static YAxisType getInstance(String value){
			if (value.equals(Intensity.value))
				return Intensity;
			if (value.equals(LogI.value))
				return LogI;
			if (value.equals(Peak.value))
				return Peak;
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
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		// TODO Auto-generated method stub
		yType = YAxisType.getInstance(scanResultPlotter_yAxisName);

		EData<IArray> sliceData = null;
		switch (yType) {
		case Intensity:
			scanResultPlotter_outputGroup = scanResultPlotter_intensityGroup;
			IArray signalArray = ((NcGroup) scanResultPlotter_outputGroup).getSignalArray();
			IArray varianceArray = ((Plot) scanResultPlotter_outputGroup).findVarianceArray();
			IArray axisArray = ((NcGroup) scanResultPlotter_outputGroup).getAxesArrayList().get(0);
			if (signalArray.getSize() == 1){
				scanResultPlotter_outputGroup = PlotFactory.createPlot("intensityCopy", DataDimensionType.pattern);
				((Plot)scanResultPlotter_outputGroup).addData("dataCopy", Factory.createArray(
						new double[]{signalArray.getArrayMath().getMaximum() * 0.99, signalArray.getArrayMath().getMaximum() * 1.01}), "Data", "counts");
				try{
				((Plot)scanResultPlotter_outputGroup).addDataVariance(Factory.createArray(
						new double[]{varianceArray.getArrayMath().getMaximum() * 0.99, varianceArray.getArrayMath().getMaximum() * 1.01}));
				}catch (Exception e) {
					// TODO: handle exception
				}
				((Plot)scanResultPlotter_outputGroup).addAxis("axis0", Factory.createArray(
						new double[]{axisArray.getArrayMath().getMaximum() * 0.99, axisArray.getArrayMath().getMaximum() * 1.01}), "AXIS 0", "", 0);
			}
			return scanResultPlotter_stop;
		case LogI:
			scanResultPlotter_outputGroup = ((Plot) scanResultPlotter_intensityGroup).log10();
			return scanResultPlotter_stop;
		case Peak:
			sliceData = findPeak();
			break;
		case FittingResult:
			scanResultPlotter_outputGroup = findFittingResult();
			return scanResultPlotter_stop;
		default:
			scanResultPlotter_outputGroup = scanResultPlotter_intensityGroup;
			return scanResultPlotter_stop;
		}
	
		scanResultPlotter_outputGroup = PlotFactory.createPlot(
				scanResultPlotter_fitResultGroup, scanResultPlotter_yAxisName, 
				DataDimensionType.pattern);
		PlotFactory.addDataToPlot(scanResultPlotter_outputGroup, 
				scanResultPlotter_yAxisName, sliceData.getData(), scanResultPlotter_yAxisName, 
				"count", sliceData.getVariance());
		((Plot) scanResultPlotter_outputGroup).addAxis(
				((Plot) scanResultPlotter_fitResultGroup).getAxis(0));
		return scanResultPlotter_stop;
	}
	
	private IGroup findFittingResult() throws StructureTypeException {
		// TODO Auto-generated method stub
		IGroup fittingResult = scanResultPlotter_fitResultGroup.findGroup(
				scanResultPlotter_fitParameter);
		if (fittingResult == null){
			throw new StructureTypeException("can not find fitting result " + 
					scanResultPlotter_fitParameter);
		}
		return fittingResult;
	}

	private EData<IArray> findPeak() throws IndexOutOfBoundException, 
	StructureTypeException, ShapeNotMatchException, IOException {
		// TODO Auto-generated method stub
		if (scanResultPlotter_fitResultGroup instanceof Plot){
			Plot inputPlot = (Plot) scanResultPlotter_fitResultGroup;
			int[] shape = inputPlot.findSingal().getShape();
			double[] peaks = new double[shape[0]];
			double[] peakVariances = new double[shape[0]];
			for (int i = 0; i < peaks.length; i++) {
				Point peakPoint = null;
				try {
					peakPoint = inputPlot.slice(0, i).getMaximumPoint();
				} catch (PlotMathException e) {
					// TODO Auto-generated catch block
					throw new IndexOutOfBoundException(e);
				}
				peaks[i] = peakPoint.getCoordinate(0);
			}
			IArray peakArray = Factory.createArray(Double.TYPE, new int[]{peaks.length}, peaks);
			IArray varianceArray = inputPlot.findSignalArray().getArrayMath().power(2).sumForDimension(0, true).
				scale(1./shape[1]).getArray();
			return new EData<IArray>(peakArray, varianceArray);
		}
		return null;
	}

//	private EData<Array> createSlice() throws Exception {
//		// TODO Auto-generated method stub
//		DataItem axis1 = null;
//		try {
//			List<?> axes = ((Plot) scanResultPlotter_fitResultGroup).getAxisList();
//			axis1 = (Axis) axes.get(axes.size() - 1);
//		} catch (Exception e) {
//			// TODO: handle exception
//			List<?> axes = scanResultPlotter_fitResultGroup.findAxes();
//			axis1 = (DataItem) axes.get(axes.size() - 1);
//		}
//		Array axisArray = null;
//		int column = 0;
//		if (axis1 != null){ 
//			axisArray = axis1.getData();
//			if (scanResultPlotter_fitParameter < axisArray.getMinimum() || 
//					scanResultPlotter_fitParameter > axisArray.getMaximum())
//				throw new Exception("no such column");
//			ArrayIterator axisIterator = axisArray.getIterator();
//			boolean ascending = true;
//			boolean doLoop = true;
//			if (axisIterator.hasNext()) { 
//				if (axisIterator.getDoubleNext() < scanResultPlotter_fitParameter)
//					ascending = true;
//				else if (axisIterator.getDoubleNext() > scanResultPlotter_fitParameter)
//					ascending = false;
//				else{
//					column = 0;
//					doLoop = false;
//				}
//			}
//			int index = 0;
//			while(doLoop && axisIterator.hasNext()){
//				if (ascending) {
//					if (axisIterator.getDoubleNext() > scanResultPlotter_fitParameter){
//						column = index;
//						break;
//					}
//				}else {
//					if (axisIterator.getDoubleNext() < scanResultPlotter_fitParameter){
//						column = index;
//						break;
//					}					
//				}
//				index ++;		
//			}
//		}else{
//			column = (int) scanResultPlotter_fitParameter.doubleValue();
//		}
//		Array signal = scanResultPlotter_fitResultGroup.getSignalArray().slice(1, column);
//		DataItem variance = null;
//		if (scanResultPlotter_fitResultGroup instanceof Plot)
//			variance = ((Plot) scanResultPlotter_fitResultGroup).getVariance();
//		else
//			try{
//				variance = scanResultPlotter_fitResultGroup.findDataItemWithAttribute(
//						"signal", "variance");
//			}catch (Exception e) {
//				// TODO: handle exception
//			}
//		Array varianceArray = null;
//		if (variance != null)
//			varianceArray = variance.getData().slice(1, column);
//		return new EData<Array>(signal, varianceArray);
//
//	}

	/**
	 * @return the scanResultPlotter_outputGroup
	 */
	public IGroup getScanResultPlotter_outputGroup() {
		return scanResultPlotter_outputGroup;
	}


	/**
	 * @param scanResultPlotter_yAxisName the scanResultPlotter_yAxisName to set
	 */
	public void setScanResultPlotter_yAxisName(String scanResultPlotter_yAxisName) {
		this.scanResultPlotter_yAxisName = scanResultPlotter_yAxisName;
	}


	/**
	 * @param scanResultPlotter_fitParameter the scanResultPlotter_fitParameter to set
	 */
	public void setScanResultPlotter_fitParameter(
			String scanResultPlotter_fitParameter) {
		this.scanResultPlotter_fitParameter = scanResultPlotter_fitParameter;
	}

	/**
	 * @param scanResultPlotter_stop the scanResultPlotter_stop to set
	 */
	public void setScanResultPlotter_stop(Boolean scanResultPlotter_stop) {
		this.scanResultPlotter_stop = scanResultPlotter_stop;
	}

	/**
	 * @param scanResultPlotter_skip the scanResultPlotter_skip to set
	 */
	public void setScanResultPlotter_skip(Boolean scanResultPlotter_skip) {
		this.scanResultPlotter_skip = scanResultPlotter_skip;
	}

	/**
	 * @param scanResultPlotter_fitResultGroup the scanResultPlotter_fitResultGroup to set
	 */
	public void setScanResultPlotter_fitResultGroup(
			IGroup scanResultPlotter_fitResultGroup) {
		this.scanResultPlotter_fitResultGroup = scanResultPlotter_fitResultGroup;
	}

	/**
	 * @param scanResultPlotter_intensityGroup the scanResultPlotter_intensityGroup to set
	 */
	public void setScanResultPlotter_intensityGroup(
			IGroup scanResultPlotter_intensityGroup) {
		this.scanResultPlotter_intensityGroup = scanResultPlotter_intensityGroup;
	}

	
}
